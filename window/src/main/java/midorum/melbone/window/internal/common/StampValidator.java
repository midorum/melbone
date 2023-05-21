package midorum.melbone.window.internal.common;

import com.midorum.win32api.facade.*;
import com.midorum.win32api.facade.Rectangle;
import com.midorum.win32api.struct.PointFloat;
import com.midorum.win32api.struct.PointLong;
import dma.flow.Waiting;
import dma.function.VoidActionThrowing;
import dma.util.DurationFormatter;
import dma.validation.Validator;
import midorum.melbone.model.settings.PropertiesProvider;
import midorum.melbone.model.settings.stamp.Stamp;
import midorum.melbone.model.settings.setting.Settings;
import midorum.melbone.window.internal.util.StaticResources;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class StampValidator {

    private final Logger logger = StaticResources.LOGGER;
    private final Logger stampLogger = StaticResources.STAMP_LOGGER;
    private final Win32System win32System;
    private final Settings settings;
    private final PropertiesProvider propertiesProvider;

    StampValidator(final Win32System win32System, final Settings settings, final PropertiesProvider propertiesProvider) {
        this.win32System = win32System;
        this.settings = settings;
        this.propertiesProvider = propertiesProvider;
    }

    public Optional<Stamp> validateStampWholeData(final IWindow window, final Stamp stamp) throws InterruptedException {
        return validateStampWholeData(window, stamp, new PointLong(0, 0));
    }

    public Optional<Stamp> validateStampWholeData(final IWindow window, final Stamp stamp, final PointLong mousePosition) throws InterruptedException {
        return checkStampIsValidForWindow(window,
                //FIXME temporarily hide mouse pointer
                () -> win32System.getScreenMouse(settings.application().speedFactor())
                        .move(Validator.checkNotNull(mousePosition).andMap(PointLong::toPointInt).orThrowForSymbol("mousePosition")),
                stamp);
    }

    public Optional<Stamp> validateStampWholeData(final IWindow window, final Stamp stamp, final IMouse mouse, final PointFloat mousePosition) throws InterruptedException {
        return checkStampIsValidForWindow(window,
                //FIXME temporarily hide mouse pointer
                () -> Validator.checkNotNull(mouse).orThrowForSymbol("mouse")
                        .move(Validator.checkNotNull(mousePosition).orThrowForSymbol("mousePosition")),
                stamp);
    }

    public Optional<Stamp> validateStampWholeData(final IWindow window, final Stamp... stamps) throws InterruptedException {
        return findFirstValidStampForWindow(window,
                //FIXME temporarily hide mouse pointer
                () -> win32System.getScreenMouse(settings.application().speedFactor()).move(0, 0),
                stamps);
    }

    public boolean validateAllStampsWholeData(final IWindow window, final Stamp... stamps) throws InterruptedException {
        return checkAllStampsAreValidForWindow(window,
                //FIXME temporarily hide mouse pointer
                () -> win32System.getScreenMouse(settings.application().speedFactor()).move(0, 0),
                stamps);
    }

    public boolean validateAllStampsWholeData(final IWindow window, final IMouse mouse, final PointFloat mousePosition, final Stamp... stamps) throws InterruptedException {
        return checkAllStampsAreValidForWindow(window,
                //FIXME temporarily hide mouse pointer
                () -> Validator.checkNotNull(mouse).orThrowForSymbol("mouse")
                        .move(Validator.checkNotNull(mousePosition).orThrowForSymbol("mousePosition")),
                stamps);
    }

    private Optional<Stamp> checkStampIsValidForWindow(final IWindow window, final VoidActionThrowing<InterruptedException> mousePointerHider, final Stamp stamp) throws InterruptedException {
        final IWindow checkedWindow = Validator.checkNotNull(window).orThrowForSymbol("window");
        final Stamp checkedStamp = Validator.checkNotNull(stamp).orThrowForSymbol("stamp");
        final Rectangle stampWindowRect = checkedStamp.windowRect();
        if (!adjustWindow(checkedWindow, stampWindowRect.left(), stampWindowRect.top())) return Optional.empty();
        if (!rectanglesAreEquals(checkedWindow.getWindowRectangle(), stampWindowRect)) return Optional.empty();
        Validator.checkNotNull(mousePointerHider).orThrowForSymbol("mouse pointer hider").perform();
        final BufferedImage bufferedImage = takeRectangleShot(checkedStamp.location());
        if (pixelsAreEquals(imageToArray(bufferedImage), checkedStamp.wholeData())) return Optional.of(checkedStamp);
        debugFailedStamps(window, stamp);
        return Optional.empty();
    }

    private Optional<Stamp> findFirstValidStampForWindow(final IWindow window, final VoidActionThrowing<InterruptedException> mousePointerHider, final Stamp[] stamps) throws InterruptedException {
        final IWindow checkedWindow = Validator.checkNotNull(window).orThrowForSymbol("window");
        final Stamp[] checkedStamps = Validator.checkNotNull(stamps).andCheckNot(ss -> ss.length == 0).orThrowForSymbol("stamps");
        final Rectangle stampWindowRect = checkedStamps[0].windowRect();
        if (!adjustWindow(checkedWindow, stampWindowRect.left(), stampWindowRect.top())) return Optional.empty();
        if (!rectanglesAreEquals(checkedWindow.getWindowRectangle(), stampWindowRect)) return Optional.empty();
        Validator.checkNotNull(mousePointerHider).orThrowForSymbol("mouse pointer hider").perform();
        record StampProcessing(Stamp stamp, BufferedImage bufferedImage) {
        }
        final BufferedImage screenImage = takeWholeScreenShot();
        final Optional<Stamp> maybe = Arrays.stream(checkedStamps)
                .map(stamp -> new StampProcessing(stamp, screenImage.getSubimage(stamp.location().left(), stamp.location().top(), stamp.location().width(), stamp.location().height())))
                .filter(stampProcessing -> pixelsAreEquals(imageToArray(stampProcessing.bufferedImage), stampProcessing.stamp.wholeData()))
                .map(StampProcessing::stamp)
                .findFirst();
        if (maybe.isEmpty()) debugFailedStamps(window, stamps);
        return maybe;
    }

    private boolean checkAllStampsAreValidForWindow(final IWindow window, final VoidActionThrowing<InterruptedException> mousePointerHider, final Stamp[] stamps) throws InterruptedException {
        final IWindow checkedWindow = Validator.checkNotNull(window).orThrowForSymbol("window");
        final Stamp[] checkedStamps = Validator.checkNotNull(stamps).andCheckNot(ss -> ss.length == 0).orThrowForSymbol("stamps");
        final Rectangle stampWindowRect = checkedStamps[0].windowRect();
        if (!adjustWindow(checkedWindow, stampWindowRect.left(), stampWindowRect.top())) return false;
        if (!rectanglesAreEquals(checkedWindow.getWindowRectangle(), stampWindowRect)) return false;
        Validator.checkNotNull(mousePointerHider).orThrowForSymbol("mouse pointer hider").perform();
        record StampProcessing(Stamp stamp, BufferedImage bufferedImage) {
        }
        final BufferedImage screenImage = takeWholeScreenShot();
        final boolean result = Arrays.stream(checkedStamps)
                .map(stamp -> new StampProcessing(stamp, screenImage.getSubimage(stamp.location().left(), stamp.location().top(), stamp.location().width(), stamp.location().height())))
                .allMatch(stampProcessing -> pixelsAreEquals(imageToArray(stampProcessing.bufferedImage), stampProcessing.stamp.wholeData()));
        if (!result) debugFailedStamps(window, stamps);
        return result;
    }

    private void debugFailedStamps(final IWindow window, final Stamp... stamps) throws InterruptedException {
        if (propertiesProvider.isModeSet("debug_stamps")) logFailedStamps(window, stamps);
    }

    public String logFailedStamps(final IWindow window, final Stamp... stamps) throws InterruptedException {
        final String marker = Long.toString(System.currentTimeMillis());
        logger.warn("stamps validation failed: marker={}", marker);
        logFailedStamps(marker, window, stamps);
        return marker;
    }

    public void logFailedStamps(final String marker, final IWindow window, final Stamp... stamps) throws InterruptedException {
        final IWindow checkedWindow = Validator.checkNotNull(window).orThrowForSymbol("window");
        final Stamp[] checkedStamps = Validator.checkNotNull(stamps).andCheckNot(ss -> ss.length == 0).orThrowForSymbol("stamps");
        win32System.getScreenMouse(settings.application().speedFactor()).move(0, 0);
        final Rectangle stampWindowRect = checkedStamps[0].windowRect();
        if (!adjustWindow(checkedWindow, stampWindowRect.left(), stampWindowRect.top())) return;
        record StampProcessing(Stamp stamp, BufferedImage bufferedImage) {
        }
        final BufferedImage screenImage = takeWholeScreenShot();
        saveImage(screenImage, "wholeScreenShot_" + marker);
        Arrays.stream(checkedStamps)
                .map(stamp -> new StampProcessing(stamp, screenImage.getSubimage(stamp.location().left(), stamp.location().top(), stamp.location().width(), stamp.location().height())))
                .forEach(stampProcessing -> debugPrint(marker, stampProcessing.bufferedImage, stampProcessing.stamp));
    }

    private IScreenShotMaker getScreenShotMaker() {
        return win32System.getScreenShotMaker();
    }

    private BufferedImage takeRectangleShot(final Rectangle rectangle) {
        return getScreenShotMaker().takeRectangle(Validator.checkNotNull(rectangle).orThrow());
    }

    private BufferedImage takeWholeScreenShot() {
        return getScreenShotMaker().takeWholeScreen();
    }

    private int[] imageToArray(final BufferedImage image) {
        return Validator.checkNotNull(image)
                .andMap(img -> img.getRGB(
                        img.getMinX(),
                        img.getMinY(),
                        img.getWidth(),
                        img.getHeight(),
                        null,
                        0,
                        img.getWidth()))
                .orThrow();
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

    private boolean rectanglesAreEquals(final Rectangle windowRectangle, final Rectangle stampRectangle) {
        return Validator.checkNotNull(windowRectangle)
                .andMap(r -> r.equals(stampRectangle))
                .orThrow("window rectangle is null");
    }

    public void takeAndSaveWholeScreenShot(final String marker) {
        final BufferedImage screenImage = takeWholeScreenShot();
        saveImage(screenImage, "wholeScreenShot_" + marker);
    }

    private void debugPrint(final String marker, final BufferedImage bufferedImage, final Stamp stamp) {
        printMismatchedPixels(marker, imageToArray(bufferedImage), stamp.wholeData(), stamp.key().internal().groupName() + "." + stamp.key().name());
        saveImage(bufferedImage, safeFileName(marker, "shot", stamp));
        saveImage(stamp, safeFileName(marker, "stamp", stamp));
    }

    private void printMismatchedPixels(final String marker, final int[] a, final int[] b, final String stampName) {
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

    private void saveImage(final BufferedImage bufferedImage, final String name) {
        try {
            final File outputFile = new File("wrong_shots" + File.separator + name + ".png");
            outputFile.mkdirs();
            ImageIO.write(bufferedImage, "png", outputFile);
            stampLogger.trace("shot saved to {}", outputFile.getName());
        } catch (IOException e) {
            stampLogger.error("error while saving shot: " + e.getMessage(), e);
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

    private String safeFileName(final String marker, final String description, final Stamp stamp) {
        return Validator.checkNotNull(stamp)
                .andMap(s -> s.key().internal().groupName() + "." + s.key().name())
                .andMap(s -> s.replaceAll("[<>:\"/\\\\|?*\s]", "_").concat("_").concat(description).concat("_").concat(marker))
                .orDefault("stamp_" + description + "_" + marker);
    }

    private String getBinaryString(int valueA) {
        final String s = Integer.toBinaryString(valueA);
        final int part = s.length() / 4;
        return s.substring(0, part) + " " + s.substring(part, part * 2) + " " + s.substring(part * 2, part * 3) + " " + s.substring(part * 3, part * 4);
    }

    //FIXME чужеродная ответственность для валидатора
    private boolean adjustWindow(final IWindow checkedWindow, final int x, final int y) throws InterruptedException {
        if (!bringWindowForeground(checkedWindow)) return false;
        checkedWindow.moveWindow(x, y);
        return true;
    }

    //FIXME чужеродная ответственность для валидатора; он должен работать в соответствующем контексте, а не выталкивать окна на передний план
    // у валидатора слишком много ответственности
    public boolean bringWindowForeground(final IWindow window) throws InterruptedException {
        final boolean result = new Waiting()
                .withDelay(500, TimeUnit.MILLISECONDS)
                .maxTimes(10)
                .doOnEveryFailedIteration(i -> {
                    logger.debug("{}: another window lays over our and has user input", new DurationFormatter(i.fromStart()).toStringWithoutZeroParts());
                    logger.warn("another window lays over our and has user input");
                    //TODO check if it is overlay window and try close it
                    //FIXME debug detecting possible overlay
                    logPossibleOverlay();
                    //FIXME try to close possible overlay modal
                    // this is temporarily solution - it's not safe
                    win32System.getScreenMouse(settings.application().speedFactor()).move(1, 1).leftClick();
                })
                .waitForBoolean(window::bringForeground);
        if (!result) logger.warn("Can not bring window {} foreground - skip", window.getSystemId());
        return result;
    }

    //FIXME чужеродная ответственность для валидатора
    public void logPossibleOverlay() {
        final String marker = "overlay_" + System.currentTimeMillis();
        win32System.getForegroundWindow().ifPresentOrElse(window -> {
            logger.debug("found overlay window: marker={}, title={}, className={}, style={}, extendedStyle={}, windowRect={}",
                    marker,
                    window.getText(), window.getClassName(),
                    window.getStyle(), window.getExtendedStyle(),
                    window.getWindowRectangle());
            takeAndSaveWholeScreenShot(marker);
        }, () -> win32System.listAllWindows().forEach(window -> {
            if (window.isVisible()) {
                logger.debug("found window: marker={}, title={}, className={}, style={}, extendedStyle={}, windowRect={}",
                        marker,
                        window.getText(), window.getClassName(),
                        window.getStyle(), window.getExtendedStyle(),
                        window.getWindowRectangle());
            }
        }));
    }
}
