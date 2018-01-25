package com.neopetsconnect.utils.captcha;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import com.goodengineer.atibackend.model.Band;
import com.goodengineer.atibackend.model.ColorImage;
import com.goodengineer.atibackend.transformation.EqualizationTransformation;
import com.goodengineer.atibackend.transformation.SubstractImageTransformation;
import com.goodengineer.atibackend.transformation.Transformation;
import com.goodengineer.atibackend.transformation.flip.HorizontalFlipTransformation;
import com.goodengineer.atibackend.transformation.threshold.ThresholdingTransformation;
import com.goodengineer.atibackend.util.FilterUtils;
import com.goodengineer.atibackend.util.MaskFactory;
import com.httphelper.main.HttpHelper;
import com.httphelper.main.HttpHelperException;
import com.httphelper.main.HttpResponse;
import com.logger.main.TimeUnits;
import com.neopetsconnect.utils.Logger;

public class CaptchaSolver {
	
	public static final String category = "CAPTCHA_SOLVER";
	
	public static boolean logImages = false;
	public static String imagesPath = "images/";
	public static String basesPath = imagesPath + "bases/";
	private static final double threshold = 0.58;
	private static final List<ColorImage> bases;
			
	static {
		try {
			bases = Files.list(Paths.get(basesPath)).filter(p -> p.toFile().isFile())
					.map(p -> {
						try {
							return CaptchaSolver.getColorImage(ImageIO.read(p.toFile()));
						} catch (IOException e1) {
							return null;
						}
					}).collect(Collectors.toList());
		} catch (IOException e) {
			throw new RuntimeException("Failed loading bases.", e);
		}
	}
	
	public static void main(String[] args) throws HttpHelperException {
		logImages = true;
		Logger.out.logTimeStart("", TimeUnits.SECONDS);
		solve(null, "test=test");
		Logger.out.logTimeEnd("", category, "%.3f");
	}
	
	public static int[] solve(HttpHelper helper, String url) throws HttpHelperException {
		Logger.out.logTimeStart("", TimeUnits.SECONDS);
		BufferedImage buffImg = loadImage(helper, url);
		Logger.out.logTimeEnd("", category, "Load image: %.3f");
//		BufferedImage buffImg =  null;
//		try {
//			buffImg = ImageIO.read(new File("images/0/test.jpeg"));
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		ColorImage originalImg = getColorImage(buffImg);
		ColorImage colorImage = (ColorImage) originalImg.clone();
		Logger.out.logTimeStart("", TimeUnits.SECONDS);
		ColorImage base = findBase(colorImage, bases);
		Logger.out.logTimeEnd("", category, "Find base: %.3f");
		transform(new EqualizationTransformation(), colorImage);
		boolean flipped = unflip(base, colorImage);
		subtractImages(base, colorImage);
		Logger.out.logTimeStart("", TimeUnits.SECONDS);
		colorImage = CaptchaSolver.toBinary(colorImage);
		Logger.out.logTimeEnd("", category, "To binary: %.3f");
		int[] point = CaptchaSolver.findPoint(colorImage, flipped);
		if (logImages) {
			Logger.out.logTimeStart("", TimeUnits.SECONDS);
			saveImage(originalImg, url.split("=")[1] + ".jpeg");
			saveSolvedCaptcha(originalImg, "new-" + url.split("=")[1] + ".jpeg", point);
			Logger.out.log(category, "saved! " + url.split("=")[1] + ".jpeg");
			Logger.out.logTimeEnd("", category, "Save image: %.3f");
		}
		return point;
	}

	public static ColorImage getColorImage(BufferedImage bufferedImage) {
		int width = bufferedImage.getWidth();
		int height = bufferedImage.getHeight();
		double[][] red = new double[width][height];
		double[][] green = new double[width][height];
		double[][] blue = new double[width][height];

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				int rgb = bufferedImage.getRGB(i, j);
				red[i][j] = ColorHelper.getRed(rgb);
				green[i][j] = ColorHelper.getGreen(rgb);
				blue[i][j] = ColorHelper.getBlue(rgb);
			}
		}

		return new ColorImage(new Band(red, "R"), new Band(green, "G"),
				new Band(blue, "B"));
	}

	public static BufferedImage getBufferedImage(ColorImage colorImage) {
		BufferedImage bufferedImage = new BufferedImage(colorImage.getWidth(),
				colorImage.getHeight(), BufferedImage.TYPE_INT_RGB);

		for (int i = 0; i < colorImage.getWidth(); i++) {
			for (int j = 0; j < colorImage.getHeight(); j++) {
				bufferedImage.setRGB(i, j, ColorHelper.convertToRgb(
						colorImage.getRed(i, j), colorImage.getGreen(i, j),
						colorImage.getBlue(i, j)));
			}
		}

		return bufferedImage;
	}

	public static ColorImage toGrayScale(ColorImage colorImage) {
	    BufferedImage buffOriginalImage = getBufferedImage(colorImage);
	    BufferedImage buffGrayImage = new BufferedImage(colorImage.getWidth(), colorImage.getHeight(),
	        BufferedImage.TYPE_INT_RGB);

	    for (int w = 0; w < colorImage.getWidth(); w++) {
	      for (int h = 0; h < colorImage.getHeight(); h++) {
	        float[] hsv = ColorHelper.getHSV(buffOriginalImage.getRGB(w, h));
	        buffGrayImage.setRGB(w, h, ColorHelper.getGrayInRgb(hsv[2]));
	      }
	    }
	    return getColorImage(buffGrayImage);
	  }
	
	private static BufferedImage loadImage(HttpHelper helper, String url)
			throws HttpHelperException {
		HttpResponse resp = helper.get(url).send();

		byte[] bytesArr = resp.getContentBytes();

		try {
			return ImageIO.read(new ByteArrayInputStream(bytesArr));
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
	
	static void saveImage(ColorImage img, String name) {
		try {
			if (Files.notExists(Paths.get(imagesPath))) {
				Files.createDirectories(Paths.get(imagesPath));
			}
			ImageIO.write(getBufferedImage(img), "JPEG", new File(imagesPath + name));
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
	
	public static int[] findPoint(ColorImage binaryImg, boolean flipped) {
		int count = 0;
		int[] point = new int[] {0, 0};
		for (int x = 0; x < binaryImg.getWidth(); x++) {
			for (int y = 0; y < binaryImg.getHeight(); y++) {
				int p = binaryImg.getBands().get(0).getPixel(x, y);
				if (p == 0) {
					count++;
					point[0] += x;
					point[1] += y;
				}
			}
		}
		point[0] = (int) (point[0] / (double) count);
		point[1] = (int) (point[1] / (double) count);
		if (flipped) {
			point[0] = binaryImg.getWidth() - point[0];
		}
		return point;
	}
	
	static ColorImage toBinary(ColorImage colorImg) {
		Logger.out.logTimeStart("", TimeUnits.SECONDS);
		ColorImage grayImg = toGrayScale(colorImg);
		Logger.out.logTimeEnd("", category, "To grayscale: %.3f");
		
		double[][] avgMask = MaskFactory.average(grayImg.getWidth(), grayImg.getHeight());
		double v = FilterUtils.applyMask(grayImg.getBands().get(0), avgMask, 
				grayImg.getWidth() / 2, grayImg.getHeight() / 2);
		
		grayImg.transform(new ThresholdingTransformation((int)(v * threshold)));

		return grayImg;
	}
	
	public static void saveSolvedCaptcha(ColorImage img, String name, int[] point) {
		for (int dx = -3; dx < 3; dx++) {
			for (int dy = -3; dy < 3; dy++) {
				img.getBands().get(0).setPixel(point[0] + dx, point[1] + dy, 255);
				img.getBands().get(1).setPixel(point[0] + dx, point[1] + dy, 0);
				img.getBands().get(2).setPixel(point[0] + dx, point[1] + dy, 0);
			}
		}
		
		saveImage(img, name);
	}
	
	private static ColorImage findBase(ColorImage colorImage, List<ColorImage> bases) {
		return bases.stream()
				.filter(base -> base.getWidth() == colorImage.getWidth() &&
					base.getHeight() == colorImage.getHeight())
				.collect(Collectors.toMap(
				base -> {
					ColorImage flipped = (ColorImage) colorImage.clone();
					transform(new HorizontalFlipTransformation(), flipped);
					return Math.min(subtract(base, colorImage), subtract(base, flipped));
				}, x -> x))
			.entrySet().stream().sorted(Map.Entry.comparingByKey()).findFirst().get().getValue();
	}
	
	private static boolean unflip(ColorImage ref, ColorImage img) {
		double diff = subtract(ref, img);
		ColorImage flipped = (ColorImage) img.clone();
		transform(new HorizontalFlipTransformation(), flipped);
		double diff2 = subtract(ref, flipped);
		if (diff < diff2) {
			return false;
		} else {
			img.setBands(flipped.getBands());
			return true;
		}
	}
	
	private static void subtractImages(ColorImage src, ColorImage dst) {
		for (int i = 0; i < 3; i++) {
			Band band1 = src.getBands().get(i);
			Band band2 = dst.getBands().get(i);
			new SubstractImageTransformation(band1).transform(band2);
		}
	}
	
	private static double subtract(ColorImage img1, ColorImage img2) {
		double total = 0;
		for (int i = 0; i < 3; i++) {
			Band band1 = img1.getBands().get(i);
			Band band2 = img2.getBands().get(i).clone();
			new SubstractImageTransformation(band1).transform(band2);
			total += Arrays.stream(band2.pixels).mapToDouble(row -> 
					Arrays.stream(row).map(v -> Math.abs(v)).sum())
				.sum();
		}
		return total;
	}
	
	private static void transform(Transformation transformation, ColorImage colorImage) {
		for (Band band : colorImage.getBands()) {
			transformation.transform(band);
		}
	}
}
