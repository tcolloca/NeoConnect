package com.neopetsconnect.utils.captcha;


public class ColorHelper {

  public static float[] getHSV(int rgb) {
    return java.awt.Color.RGBtoHSB(getRed(rgb), getGreen(rgb), getBlue(rgb), null);
  }

  public static int getRed(int color) {
    return (color >> 16) & 0xFF;
  }

  public static int getGreen(int color) {
    return (color >> 8) & 0xFF;
  }

  public static int getBlue(int color) {
    return color & 0xFF;
  }

  public static int getGrayInRgb(float v) {
    int i = Math.round(v * 255);
    return i + (i << 8) + (i << 16);
  }

  public static int convertToRgb(int red, int green, int blue) {
    return (red << 16) + (green << 8) + blue;
  }
}
