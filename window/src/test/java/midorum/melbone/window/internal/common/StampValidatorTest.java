package midorum.melbone.window.internal.common;

import com.midorum.win32api.facade.IScreenShotMaker;
import com.midorum.win32api.facade.Rectangle;
import com.midorum.win32api.facade.Win32System;
import midorum.melbone.model.settings.PropertiesProvider;
import midorum.melbone.model.settings.setting.ApplicationSettings;
import midorum.melbone.model.settings.setting.Settings;
import midorum.melbone.model.settings.stamp.Stamp;
import midorum.melbone.settings.StampKeys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StampValidatorTest {

    private final Win32System win32System = mock(Win32System.class);
    private final Settings settings = mock(Settings.class);
    private final ApplicationSettings applicationSettings = mock(ApplicationSettings.class);
    private final PropertiesProvider propertiesProvider = mock(PropertiesProvider.class);
    private final FileServiceProvider fileServiceProvider = mock(FileServiceProvider.class);
    private final FileServiceProvider.FileService fileService = mock(FileServiceProvider.FileService.class);
    private final IScreenShotMaker screenShotMaker = mock(IScreenShotMaker.class);

    @BeforeEach
    void beforeEach() throws IOException {
        when(settings.application()).thenReturn(applicationSettings);
        when(win32System.getScreenShotMaker()).thenReturn(screenShotMaker);
        when(fileServiceProvider.withFile(anyString(), anyString())).thenReturn(fileService);
    }

    @Test
    void validateStampWithFail() {
        //given
        final Rectangle stampLocation = new Rectangle(0, 0, 3, 2);
        final int[] stampWholeData = imageToArray(createImage(stampLocation, Color.RED, Color.BLACK));
        final Stamp stamp = getStampMock(stampLocation, stampWholeData);
        whenTakeRectangleShotThenReturn(createImage(stampLocation, Color.GRAY, Color.BLACK));
        whenTakeWholeScreenShotThenReturn(createImage(stampLocation, Color.GRAY, Color.BLACK));
        when(applicationSettings.stampDeviation()).thenReturn(0);
        when(propertiesProvider.isModeSet("debug_stamps")).thenReturn(true);
        //when
        final Optional<Stamp> result = new StampValidator(win32System, settings, propertiesProvider, fileServiceProvider).validateStamp(stamp);
        //then
        assertTrue(result.isEmpty());
    }

    @Test
    void validateStampWithSuccess_deviationIs128() {
        //given
        final Rectangle stampLocation = new Rectangle(0, 0, 3, 2);
        final int[] stampWholeData = imageToArray(createImage(stampLocation, Color.RED, Color.BLACK));
        final Stamp stamp = getStampMock(stampLocation, stampWholeData);
        whenTakeRectangleShotThenReturn(createImage(stampLocation, Color.GRAY, Color.BLACK));
        whenTakeWholeScreenShotThenReturn(createImage(stampLocation, Color.GRAY, Color.BLACK));
        when(applicationSettings.stampDeviation()).thenReturn(128);
        when(propertiesProvider.isModeSet("debug_stamps")).thenReturn(true);
        //when
        final Optional<Stamp> result = new StampValidator(win32System, settings, propertiesProvider, fileServiceProvider).validateStamp(stamp);
        //then
        assertTrue(result.isPresent());
    }

    @Test
    void validateStampWithSuccess_deviationIs0() {
        //given
        final Rectangle stampLocation = new Rectangle(0, 0, 3, 2);
        final BufferedImage image = createImage(stampLocation, Color.RED, Color.BLACK);
        final int[] stampWholeData = imageToArray(image);
        final Stamp stamp = getStampMock(stampLocation, stampWholeData);
        whenTakeRectangleShotThenReturn(image);
        when(applicationSettings.stampDeviation()).thenReturn(0);
        //when
        final Optional<Stamp> result = new StampValidator(win32System, settings, propertiesProvider, fileServiceProvider).validateStamp(stamp);
        //then
        assertTrue(result.isPresent());
    }

    @Test
    void findFirstValidStamp() {
        //given
        final Rectangle stamp1Location = new Rectangle(0, 0, 3, 2);
        final BufferedImage stamp1Image = createImage(stamp1Location, Color.RED, Color.BLACK);
        final int[] stamp1WholeData = imageToArray(stamp1Image);
        final Stamp stamp1 = getStampMock(stamp1Location, stamp1WholeData);
        final Rectangle stamp2Location = new Rectangle(0, 0, 5, 3);
        final BufferedImage stamp2Image = createImage(stamp2Location, Color.GRAY, Color.BLACK);
        final int[] stamp2WholeData = imageToArray(stamp2Image);
        final Stamp stamp2 = getStampMock(stamp2Location, stamp2WholeData);
        whenTakeWholeScreenShotThenReturn(stamp2Image);
        when(applicationSettings.stampDeviation()).thenReturn(0);
        //when
        final Optional<Stamp> result = new StampValidator(win32System, settings, propertiesProvider, fileServiceProvider).findFirstValidStamp(stamp1, stamp2);
        //then
        assertTrue(result.isPresent());
        assertEquals(stamp2, result.get());
    }

    @Test
    void takeAndSaveWholeScreenShot() throws IOException {
        //given
        final String lodMarker = "test-marker";
        final BufferedImage image = createImage(new Rectangle(0, 0, 5, 3), Color.GRAY, Color.BLACK);
        whenTakeWholeScreenShotThenReturn(image);
        when(applicationSettings.stampDeviation()).thenReturn(0);
        //when
        new StampValidator(win32System, settings, propertiesProvider, fileServiceProvider).takeAndSaveWholeScreenShot(lodMarker);
        //then
        verify(fileService).writeImage(image);
    }

    @Test
    void logStampsMismatching() throws IOException {
        //given
        final String lodMarker = "test-marker";
        final Rectangle stamp1Location = new Rectangle(0, 0, 3, 2);
        final BufferedImage stamp1Image = createImage(stamp1Location, Color.RED, Color.BLACK);
        final int[] stamp1WholeData = imageToArray(stamp1Image);
        final Stamp stamp1 = getStampMock(stamp1Location, stamp1WholeData);
        final Rectangle stamp2Location = new Rectangle(0, 0, 5, 3);
        final BufferedImage stamp2Image = createImage(stamp2Location, Color.GRAY, Color.BLACK);
        final int[] stamp2WholeData = imageToArray(stamp2Image);
        final Stamp stamp2 = getStampMock(stamp2Location, stamp2WholeData);
        final BufferedImage wholeScreenImage = createImage(new Rectangle(0, 0, 5, 3), Color.GREEN, Color.BLACK);
        whenTakeWholeScreenShotThenReturn(wholeScreenImage);
        when(applicationSettings.stampDeviation()).thenReturn(0);
        //when
        new StampValidator(win32System, settings, propertiesProvider, fileServiceProvider).logStampsMismatching(lodMarker, stamp1, stamp2);
        //then
        final ArgumentCaptor<BufferedImage> argument = ArgumentCaptor.forClass(BufferedImage.class);
        verify(fileService, times(5)).writeImage(argument.capture());
        final List<BufferedImage> wroteImages = argument.getAllValues();
        assertEquals(wholeScreenImage, wroteImages.get(0));
        assertArrayEquals(stamp1.wholeData(), imageToArray(wroteImages.get(2)));
        assertArrayEquals(stamp2.wholeData(), imageToArray(wroteImages.get(4)));
    }

    private Stamp getStampMock(final Rectangle location, final int[] stampWholeData) {
        final Stamp stamp = mock(Stamp.class);
        when(stamp.key()).thenReturn(StampKeys.TargetBaseApp.dailyTrackerPopupCaption);
        when(stamp.location()).thenReturn(location);
        when(stamp.wholeData()).thenReturn(stampWholeData);
        return stamp;
    }

    public void whenTakeRectangleShotThenReturn(final BufferedImage image) {
        when(screenShotMaker.takeRectangle(any(Rectangle.class))).thenReturn(image);
    }

    public void whenTakeWholeScreenShotThenReturn(final BufferedImage image) {
        when(screenShotMaker.takeWholeScreen()).thenReturn(image);
    }

    private BufferedImage createImage(final Rectangle rectangle, final Color foregroundColor, final Color backgroundColor) {
        // https://riptutorial.com/java/example/19496/creating-a-simple-image-programmatically-and-displaying-it
        final int width = rectangle.width();
        final int height = rectangle.height();
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setColor(backgroundColor);
        g2d.fillRect(0, 0, width, height);
        g2d.setColor(foregroundColor);
        g2d.drawLine(0, 0, width, height);
        g2d.drawLine(0, height, width, 0);
        g2d.dispose();
        return img;
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
}