package midorum.melbone.window.internal.common;

import dma.util.DurationFormatter;
import midorum.melbone.model.settings.setting.Settings;
import midorum.melbone.model.settings.stamp.Stamp;

import java.awt.image.BufferedImage;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class StampSeeker {

    private final BufferedImage screenImage;
    private final Stamp[] stampsToSeek;
    private final int deviation;

    public StampSeeker(final BufferedImage screenImage, final Stamp[] stampsToSeek, final Settings settings) {
        this.screenImage = Objects.requireNonNull(screenImage);
        this.stampsToSeek = Objects.requireNonNull(stampsToSeek);
        this.deviation = settings.application().stampDeviation();
    }

    public List<Result> perform() {
        final int[] screen = imageToArray(screenImage);
        final int screenWidth = screenImage.getWidth();
        final int screenHeight = screenImage.getHeight();
        return Arrays.stream(stampsToSeek)
                .map(stamp -> findStampOnScreen(screen, screenWidth, screenHeight, stamp))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public List<Result> performParallel() {
        final int[] screen = imageToArray(screenImage);
        final int screenWidth = screenImage.getWidth();
        final int screenHeight = screenImage.getHeight();
//        return Arrays.stream(stampsToSeek)
//                .parallel()
//                .map(stamp -> findStampOnScreen(screen, screenWidth, screenHeight, stamp))
//                .reduce(new ArrayList<>(), new BiFunction<ArrayList<Result>, List<Result>, ArrayList<Result>>() {
//                    @Override
//                    public ArrayList<Result> apply(final ArrayList<Result> results, final List<Result> results2) {
//                        System.out.println("acc " + results + " " + results2);
//                        results.addAll(results2);
//                        return results;
//                    }
//                }, new BinaryOperator<ArrayList<Result>>() {
//                    @Override
//                    public ArrayList<Result> apply(final ArrayList<Result> results, final ArrayList<Result> results2) {
//                        System.out.println("comb " + results + " " + results2);
//                        ///results.addAll(results2);
//                        return results;
//                    }
//                });
        return Arrays.stream(stampsToSeek)
                .parallel()
                .map(stamp -> findStampOnScreen(screen, screenWidth, screenHeight, stamp))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private List<Result> findStampOnScreen(final int[] screen, final int screenWidth, final int screenHeight, final Stamp stamp) {
        ///System.out.println("look up stamp " + stamp.key() + " (" + Thread.currentThread().getName() + ")");
        final int[] stampData = stamp.wholeData();
        final int stampWidth = stamp.location().width();
        final int stampHeight = stamp.location().height();
        return findStampDataOnScreen(screen, screenWidth, screenHeight, stamp, stampData, stampWidth, stampHeight);
    }

    private List<Result> findFirstLineOnScreen(final int[] screen, final int screenWidth, final int screenHeight, final Stamp stamp) {
        ///System.out.println("look up first line " + stamp.key() + " (" + Thread.currentThread().getName() + ")");
        final int[] stampData = stamp.firstLine();
        final int stampWidth = stamp.location().width();
        final int stampHeight = 1;
        return findStampDataOnScreen(screen, screenWidth, screenHeight, stamp, stampData, stampWidth, stampHeight);
    }

    private List<Result> findStampDataOnScreen(final int[] screen,
                                               final int screenWidth,
                                               final int screenHeight,
                                               final Stamp stamp,
                                               final int[] stampData,
                                               final int stampWidth,
                                               final int stampHeight) {
        if (stampWidth > screenWidth)
            throw new IllegalArgumentException("Stamp width cannot be greater than whole screen width");
        if (stampHeight > screenHeight)
            throw new IllegalArgumentException("Stamp height cannot be greater than whole screen height");
        final List<Result> resultList = new ArrayList<>();
        final long start = System.nanoTime();
        for (int row = 0; row <= screenHeight - stampHeight; row++) {
            for (int col = 0; col <= screenWidth - stampWidth; col++) {
                final int baseIndex = col + row * screenWidth;
                ///System.out.println("row=" + row + " col=" + col + " baseIndex=" + baseIndex);
                stamp:
                {
                    for (int sRow = 0; sRow < stampHeight; sRow++) {
                        for (int sCol = 0; sCol < stampWidth; sCol++) {
                            final int sIndex = sCol + sRow * stampWidth;
                            final int index = (baseIndex + sRow * screenWidth + sCol);
                            ///System.out.println(" > sRow=" + sRow + " sCol=" + sCol + " sIndex=" + sIndex + " index=" + index);
                            if (!colorsAreEquals(screen[index], stampData[sIndex])) {
                                break stamp;
                            }
                        }
                    }
                    ///System.out.println("found stamp: " + stamp.key() + " x=" + col + " y=" + row);
                    resultList.add(new Result(stamp, col, row, System.nanoTime()-start));
                }
            }
        }
        ///System.out.println("done for stamp " + stamp.key());
        return resultList;
    }

    private int[] imageToArray(final BufferedImage image) {
        return image.getRGB(
                image.getMinX(),
                image.getMinY(),
                image.getWidth(),
                image.getHeight(),
                null,
                0,
                image.getWidth());
    }

    private boolean colorsAreEquals(int a, int b) {
        if (a == b) return true;
        ///System.out.printf("a=%#08x, b=%#08x\n", a, b);
        if (Math.abs((a & 0xFF) - (b & 0xFF)) > deviation) return false;
        if (Math.abs(((a >> 8) & 0xFF) - ((b >> 8) & 0xFF)) > deviation) return false;
        return Math.abs(((a >> 16) & 0xFF) - ((b >> 16) & 0xFF)) <= deviation;
    }

    public record Result(Stamp stamp, int x, int y, long time) {

        public Result(final Stamp stamp, final int x, final int y) {
            this(stamp, x, y, 0);
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final Result result = (Result) o;
            return x == result.x && y == result.y && Objects.equals(stamp, result.stamp);
        }

        @Override
        public int hashCode() {
            return Objects.hash(stamp, x, y);
        }

        @Override
        public String toString() {
            return "Result{" +
                    "stamp=" + stamp.key() +
                    ", x=" + x +
                    ", y=" + y +
                    ", time=" + new DurationFormatter(Duration.ofNanos(time)).toStringWithoutZeroParts() +
                    '}';
        }
    }
}
