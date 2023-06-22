package midorum.melbone.window.internal.common;

import com.midorum.win32api.facade.IScreenShotMaker;
import com.midorum.win32api.facade.Rectangle;
import com.midorum.win32api.facade.Win32System;
import dma.validation.Validator;
import midorum.melbone.model.settings.PropertiesProvider;
import midorum.melbone.model.settings.setting.Settings;
import midorum.melbone.model.settings.stamp.Stamp;
import midorum.melbone.window.internal.util.StaticResources;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.IntStream;

public class StampValidator {

    private final Logger stampLogger = StaticResources.STAMP_LOGGER;
    private final Win32System win32System;
    private final Settings settings;
    private final PropertiesProvider propertiesProvider;
    private final FileServiceProvider fileServiceProvider;

    StampValidator(final Win32System win32System, final Settings settings, final PropertiesProvider propertiesProvider, final FileServiceProvider fileServiceProvider) {
        this.win32System = win32System;
        this.settings = settings;
        this.propertiesProvider = propertiesProvider;
        this.fileServiceProvider = fileServiceProvider;
    }

    public Optional<Stamp> validateStamp(final Stamp stamp) {
        final Stamp checkedStamp = Validator.checkNotNull(stamp).orThrowForSymbol("stamp");
        final BufferedImage bufferedImage = takeRectangleShot(checkedStamp.location());
        if (pixelsAreEquals(imageToArray(bufferedImage), checkedStamp.wholeData())) return Optional.of(checkedStamp);
        debugFailedStamps(stamp);
        return Optional.empty();
    }

    public Optional<Stamp> findFirstValidStamp(final Stamp... stamps) {
        final Stamp[] checkedStamps = Validator.checkNotNull(stamps).andCheckNot(ss -> ss.length == 0).orThrowForSymbol("stamps");
        record StampProcessing(Stamp stamp, BufferedImage bufferedImage) {
        }
        final BufferedImage screenImage = takeWholeScreenShot();
        final Optional<Stamp> maybe = Arrays.stream(checkedStamps)
                .map(stamp -> new StampProcessing(stamp, screenImage.getSubimage(stamp.location().left(), stamp.location().top(), stamp.location().width(), stamp.location().height())))
                .filter(stampProcessing -> pixelsAreEquals(imageToArray(stampProcessing.bufferedImage), stampProcessing.stamp.wholeData()))
                .map(StampProcessing::stamp)
                .findFirst();
        if (maybe.isEmpty()) debugFailedStamps(stamps);
        return maybe;
    }

    public void takeAndSaveWholeScreenShot(final String marker) {
        final BufferedImage screenImage = takeWholeScreenShot();
        saveImage(screenImage, "wholeScreenShot_" + marker);
    }

    private IScreenShotMaker getScreenShotMaker() {
        return win32System.getScreenShotMaker();
    }

    private BufferedImage takeRectangleShot(final Rectangle rectangle) {
        return getScreenShotMaker().takeRectangle(rectangle);
    }

    private BufferedImage takeWholeScreenShot() {
        return getScreenShotMaker().takeWholeScreen();
    }

    private void debugFailedStamps(final Stamp... stamps) {
        if (propertiesProvider.isModeSet("debug_stamps"))
            logStampsMismatching("debug_" + System.currentTimeMillis(), stamps);
    }

    public void logStampsMismatching(final String marker, final Stamp... stamps) {
        final Stamp[] checkedStamps = Validator.checkNotNull(stamps).andCheckNot(ss -> ss.length == 0).orThrowForSymbol("stamps");
        record StampProcessing(Stamp stamp, BufferedImage bufferedImage) {
        }
        final BufferedImage screenImage = takeWholeScreenShot();
        saveImage(screenImage, "wholeScreenShot_" + marker);
        stampLogger.warn("stamps mismatching: marker={}", marker);
        Arrays.stream(checkedStamps)
                .map(stamp -> new StampProcessing(stamp, screenImage.getSubimage(stamp.location().left(), stamp.location().top(), stamp.location().width(), stamp.location().height())))
                .forEach(stampProcessing -> logStampMismatching(marker, stampProcessing.bufferedImage, stampProcessing.stamp));
    }

    private void logStampMismatching(final String marker, final BufferedImage bufferedImage, final Stamp stamp) {
        logMismatchedPixels(marker, imageToArray(bufferedImage), stamp.wholeData(), stamp.key().internal().groupName() + "." + stamp.key().name());
        saveImage(bufferedImage, getFileName(marker, "shot", stamp));
        saveImage(stamp, getFileName(marker, "stamp", stamp));
    }

    private void saveImage(final BufferedImage bufferedImage, final String name) {
        try {
            final FileServiceProvider.FileService fileService = fileServiceProvider.withFile("wrong_shots" + File.separator + name.replaceAll("[<>:\"/\\\\|?*\s]", "_"), "png");
            fileService.writeImage(bufferedImage);
            stampLogger.trace("image saved to {}", fileService.getFileName());
        } catch (IOException e) {
            stampLogger.error("error while saving image:", e);
        }
    }

    private void saveImage(final Stamp stamp, final String name) {
        final Rectangle location = stamp.location();
        final int width = location.width();
        final int height = location.height();
        final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        image.setRGB(0, 0, width, height, stamp.wholeData(), 0, width);
        saveImage(image, name);
    }

    private String getFileName(final String marker, final String description, final Stamp stamp) {
        return stringifyStamp(stamp).concat("_").concat(description).concat("_").concat(marker);
    }

    private String stringifyStamp(final Stamp s) {
        return s.key().internal().groupName() + "." + s.key().name();
    }

    private void logMismatchedPixels(final String marker, final int[] a, final int[] b, final String stampName) {
        if (a.length != b.length) {
            stampLogger.error("marker={} stamp={} arrays lengths are different: {} != {}", marker, stampName, a.length, b.length);
            return;
        }
        stampLogger.info("marker={} stamp={} arrays lengths are same: {} == {}", marker, stampName, a.length, b.length);
        final int deviation = settings.application().stampDeviation();
        stampLogger.error("marker={} stamp={} below mismatched pixels with deviation={}:", marker, stampName, deviation);
        IntStream.range(0, a.length)
                .filter(i -> !colorsAreEquals(a[i], b[i], deviation))
                .forEach(i -> stampLogger.error("marker={} stamp={}  #{} - {} != {} ({},{},{})", marker, stampName,
                        i, new Color(a[i]), new Color(b[i]),
                        redDeviation(a[i], b[i]), greenDeviation(a[i], b[i]), blueDeviation(a[i], b[i])));
    }

    private int redDeviation(int a, int b) {
        return ((a >> 16) & 0xFF) - ((b >> 16) & 0xFF);
    }

    private int greenDeviation(int a, int b) {
        return ((a >> 8) & 0xFF) - ((b >> 8) & 0xFF);
    }

    private int blueDeviation(int a, int b) {
        return (a & 0xFF) - (b & 0xFF);
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

    private boolean pixelsAreEquals(final int[] a, final int[] b) {
        if (a.length != b.length) return false;
        final int deviation = settings.application().stampDeviation();
        return IntStream.range(0, a.length)
                .mapToObj(i -> colorsAreEquals(a[i], b[i], deviation))
                .dropWhile(Boolean.TRUE::equals)
                .findFirst()
                .orElse(true);
    }

    private boolean colorsAreEquals(int a, int b, int deviation) {
        if (a == b) return true;
        if (Math.abs((a & 0xFF) - (b & 0xFF)) > deviation) return false;
        if (Math.abs(((a >> 8) & 0xFF) - ((b >> 8) & 0xFF)) > deviation) return false;
        return Math.abs(((a >> 16) & 0xFF) - ((b >> 16) & 0xFF)) <= deviation;
    }
}
