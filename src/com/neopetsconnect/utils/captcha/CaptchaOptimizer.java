package com.neopetsconnect.utils.captcha;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import com.goodengineer.atibackend.model.Band;
import com.goodengineer.atibackend.model.ColorImage;
import com.goodengineer.atibackend.transformation.EqualizationTransformation;
import com.goodengineer.atibackend.transformation.SubstractImageTransformation;
import com.goodengineer.atibackend.transformation.Transformation;
import com.goodengineer.atibackend.transformation.flip.HorizontalFlipTransformation;
import com.neopetsconnect.utils.Logger;

public class CaptchaOptimizer {

	public static final String category = "CAPTCHA_SOLVER";
	
	public static void main(String[] args) throws IOException {
		List<ColorImage> bases = Files.list(Paths.get("images")).filter(p -> p.toFile().isFile())
				.map(p -> {
					try {
						return CaptchaSolver.getColorImage(ImageIO.read(p.toFile()));
					} catch (IOException e1) {
						return null;
					}
				})
				.collect(Collectors.toList());
		for (int i = 0; i < 7; i++) {
			Path dir = Paths.get("images/" + i);
			Map<Path, ColorImage> imagesMap = Files.list(dir)
				.filter(path -> path.toFile().isFile())
				.filter(path -> {
					Logger.out.log(category, path.getFileName());
					return !path.getFileName().toString().startsWith("sol") && 
							!path.getFileName().toString().startsWith("new-");
				})
				.collect(Collectors.toMap(x -> x, path -> {
				try {
					return CaptchaSolver.getColorImage(ImageIO.read(path.toFile()));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}));
			
			for (Entry<Path, ColorImage> e : imagesMap.entrySet()) {
				String name = e.getKey().getFileName().toString();
				ColorImage colorImage = e.getValue();
				ColorImage base = findBase(colorImage, bases);
				ColorImage original = (ColorImage) colorImage.clone();
				transform(new EqualizationTransformation(), colorImage);
				boolean flipped = unflip(base, colorImage);
				subtractImages(base, colorImage);
				colorImage = CaptchaSolver.toBinary(colorImage);
				int[] point = CaptchaSolver.findPoint(colorImage, flipped);
				CaptchaSolver.saveImage(base, i + "/sol-base.jpg");
				CaptchaSolver.saveSolvedCaptcha(original, i + "/sol-" + name, point);
			}
		}
	}
	
//	public static void main(String[] args) throws IOException {
//		for (int i = 0; i < 7; i++) {
//			Path dir = Paths.get("images/" + i);
//			Map<Path, ColorImage> imagesMap = Files.list(dir)
//				.filter(path -> path.toFile().isFile())
//				.collect(Collectors.toMap(x -> x, path -> {
//				try {
//					return CaptchaSolver.getColorImage(ImageIO.read(path.toFile()));
//				} catch (IOException e) {
//					return null;
//				}
//			}));
//			Map<Path, ColorImage> unflipped = unflipped(imagesMap);
//			ColorImage firstImg = new ArrayList<>(unflipped.values()).get(0);
//			ColorImage avgImage = new ColorImage(firstImg.getWidth(), firstImg.getHeight());
//			for (Entry<Path, ColorImage> e : unflipped.entrySet()) {
//				ColorImage colorImage = e.getValue();
//				addAllPixels(colorImage, avgImage);
//			}
//			ImageIO.write(CaptchaSolver.getBufferedImage(avgImage), "jpg", new File("images/sum" + i + ".jpg"));
//		}
//	}
	
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

	@SuppressWarnings("unused")
	private static Map<Path, ColorImage> unflipped(Map<Path, ColorImage> colorImages) {
		ColorImage ref = new ArrayList<>(colorImages.values()).get(0);
		Map<Path, ColorImage> unflipped = new HashMap<>();
		for (Entry<Path, ColorImage> e : colorImages.entrySet()) {
			Path path = e.getKey();
			ColorImage colorImage = e.getValue();
			transform(new EqualizationTransformation(), colorImage);
			unflip(ref, colorImage);
			unflipped.put(path, colorImage);
		}
		return unflipped;
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
	
	@SuppressWarnings("unused")
	private static void addAllPixels(ColorImage image, ColorImage avgImage) {
		for (int x = 0; x < image.getWidth(); x++) {
			for (int y = 0; y < image.getHeight(); y++) {
				for (int i = 0; i < 3; i++) {
					Band src = image.getBands().get(i);
					Band dst = avgImage.getBands().get(i);
					dst.setRawPixel(x, y, dst.getRawPixel(x, y) + src.getRawPixel(x, y));
				}
			}
		}
	}
	
	@SuppressWarnings("unused")
	private static void normalize(ColorImage colorImage) {
		for (Band band : colorImage.getBands()) {
			normalize(band);
		}
	}
	
	private static void normalize(Band band) {
		double total = 0;
		for (int x = 0; x < band.getWidth(); x++) {
			for (int y = 0; y < band.getHeight(); y++) {
				total += band.getPixel(x, y);
			}
		}
		total /= band.getWidth() * band.getHeight();
		for (int x = 0; x < band.getWidth(); x++) {
			for (int y = 0; y < band.getHeight(); y++) {
				band.setRawPixel(x, y, band.getRawPixel(x, y) / total);
			}
		}
	}
	
	private static void transform(Transformation transformation, ColorImage colorImage) {
		for (Band band : colorImage.getBands()) {
			transformation.transform(band);
		}
	}
}
