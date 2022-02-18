package me.triller.screenchangenotifier.scanner;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public class BufferedImageProcessor {

    private final int width;
    private final int height;
    private final boolean hasAlphaChannel;
    private final int[] pixels;

    public BufferedImageProcessor(BufferedImage image) {
        pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        width = image.getWidth();
        height = image.getHeight();
        hasAlphaChannel = image.getAlphaRaster() != null;
    }

    public int[] getRGBA(int x, int y) {
        int pos = y * width + x;

        int a = 255; // 255 alpha
        if (hasAlphaChannel) {
            a = (pixels[pos] & 0xff000000) >>> 24; // alpha
        }

        int b = (pixels[pos] & 0xff); // blue
        int g = (pixels[pos] & 0xff00) >> 8; // green
        int r = (pixels[pos] & 0xff0000) >> 16; // red
        return new int[]{r, g, b, a};
    }
}