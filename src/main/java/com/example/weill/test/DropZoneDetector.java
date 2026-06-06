package com.example.weill.test;

import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DropZoneDetector {

    private static final int DARK_THRESHOLD = 165;
    private static final int MIN_RECTANGLE_WIDTH = 28;
    private static final int MIN_RECTANGLE_HEIGHT = 18;

    private final Map<String, DropZoneBounds> cache = new ConcurrentHashMap<>();

    public DropZoneBounds detect(String publicImagePath) {
        return cache.computeIfAbsent(publicImagePath, this::detectUncached);
    }

    private DropZoneBounds detectUncached(String publicImagePath) {
        String resourcePath = "static" + publicImagePath;
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (input == null) {
                return DropZoneBounds.centeredFallback();
            }
            BufferedImage image = ImageIO.read(input);
            if (image == null) {
                return DropZoneBounds.centeredFallback();
            }
            return findBestRectangle(image)
                .map(candidate -> candidate.toBounds(image.getWidth(), image.getHeight()))
                .orElseGet(DropZoneBounds::centeredFallback);
        } catch (IOException exception) {
            return DropZoneBounds.centeredFallback();
        }
    }

    private java.util.Optional<RectangleCandidate> findBestRectangle(BufferedImage image) {
        boolean[][] dark = darkPixels(image);
        List<HorizontalRun> runs = horizontalRuns(dark, image.getWidth(), image.getHeight());
        List<RectangleCandidate> candidates = new ArrayList<>();

        for (int first = 0; first < runs.size(); first++) {
            HorizontalRun top = runs.get(first);
            for (int second = first + 1; second < runs.size(); second++) {
                HorizontalRun bottom = runs.get(second);
                int height = bottom.y() - top.y();
                if (height < MIN_RECTANGLE_HEIGHT || height > image.getHeight() * 0.48) {
                    continue;
                }

                int left = Math.max(top.left(), bottom.left());
                int right = Math.min(top.right(), bottom.right());
                int width = right - left + 1;
                if (width < MIN_RECTANGLE_WIDTH || width > image.getWidth() * 0.55) {
                    continue;
                }
                if (Math.abs(top.left() - bottom.left()) > 12 || Math.abs(top.right() - bottom.right()) > 12) {
                    continue;
                }

                double horizontalCoverage = (
                    horizontalCoverage(dark, top.y(), left, right)
                        + horizontalCoverage(dark, bottom.y(), left, right)
                ) / 2.0;
                double verticalCoverage = (
                    verticalCoverage(dark, left, top.y(), bottom.y())
                        + verticalCoverage(dark, right, top.y(), bottom.y())
                ) / 2.0;
                double interiorInk = interiorInk(dark, left, top.y(), right, bottom.y());

                if (horizontalCoverage < 0.62 || verticalCoverage < 0.32 || interiorInk > 0.16) {
                    continue;
                }

                double score = horizontalCoverage
                    + verticalCoverage
                    - (interiorInk * 5.0)
                    + Math.min(width * height / 5000.0, 0.35);
                candidates.add(new RectangleCandidate(left, top.y(), right, bottom.y(), score));
            }
        }

        return candidates.stream().max(Comparator.comparingDouble(RectangleCandidate::score));
    }

    private boolean[][] darkPixels(BufferedImage image) {
        boolean[][] dark = new boolean[image.getHeight()][image.getWidth()];
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                int red = (rgb >> 16) & 0xff;
                int green = (rgb >> 8) & 0xff;
                int blue = rgb & 0xff;
                dark[y][x] = ((red + green + blue) / 3) < DARK_THRESHOLD;
            }
        }
        return dark;
    }

    private List<HorizontalRun> horizontalRuns(boolean[][] dark, int width, int height) {
        List<HorizontalRun> runs = new ArrayList<>();
        for (int y = 1; y < height - 1; y++) {
            int start = -1;
            int lastDark = -1;
            for (int x = 1; x < width - 1; x++) {
                if (hasDarkNearby(dark, x, y, 1)) {
                    if (start < 0) {
                        start = x;
                    }
                    lastDark = x;
                } else if (start >= 0 && x - lastDark > 3) {
                    addRun(runs, y, start, lastDark, width);
                    start = -1;
                    lastDark = -1;
                }
            }
            if (start >= 0) {
                addRun(runs, y, start, lastDark, width);
            }
        }
        return runs;
    }

    private void addRun(List<HorizontalRun> runs, int y, int left, int right, int imageWidth) {
        int length = right - left + 1;
        if (length >= MIN_RECTANGLE_WIDTH && length <= imageWidth * 0.55) {
            runs.add(new HorizontalRun(y, left, right));
        }
    }

    private double horizontalCoverage(boolean[][] dark, int y, int left, int right) {
        int covered = 0;
        for (int x = left; x <= right; x++) {
            if (hasDarkNearby(dark, x, y, 2)) {
                covered++;
            }
        }
        return covered / (double) (right - left + 1);
    }

    private double verticalCoverage(boolean[][] dark, int x, int top, int bottom) {
        int covered = 0;
        for (int y = top; y <= bottom; y++) {
            if (hasDarkNearby(dark, x, y, 3)) {
                covered++;
            }
        }
        return covered / (double) (bottom - top + 1);
    }

    private double interiorInk(boolean[][] dark, int left, int top, int right, int bottom) {
        int inset = 5;
        int darkCount = 0;
        int total = 0;
        for (int y = top + inset; y <= bottom - inset; y++) {
            for (int x = left + inset; x <= right - inset; x++) {
                total++;
                if (dark[y][x]) {
                    darkCount++;
                }
            }
        }
        return total == 0 ? 1 : darkCount / (double) total;
    }

    private boolean hasDarkNearby(boolean[][] dark, int x, int y, int radius) {
        int minY = Math.max(0, y - radius);
        int maxY = Math.min(dark.length - 1, y + radius);
        int minX = Math.max(0, x - radius);
        int maxX = Math.min(dark[0].length - 1, x + radius);
        for (int nearbyY = minY; nearbyY <= maxY; nearbyY++) {
            for (int nearbyX = minX; nearbyX <= maxX; nearbyX++) {
                if (dark[nearbyY][nearbyX]) {
                    return true;
                }
            }
        }
        return false;
    }

    private record HorizontalRun(int y, int left, int right) {
    }

    private record RectangleCandidate(int left, int top, int right, int bottom, double score) {

        DropZoneBounds toBounds(int imageWidth, int imageHeight) {
            return new DropZoneBounds(
                percentage(left, imageWidth),
                percentage(top, imageHeight),
                percentage(right - left + 1, imageWidth),
                percentage(bottom - top + 1, imageHeight)
            );
        }

        private double percentage(int value, int total) {
            return Math.round((value * 10000.0) / total) / 100.0;
        }
    }
}
