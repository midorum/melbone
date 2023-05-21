package midorum.melbone.settings.internal.defining;

import com.midorum.win32api.facade.Win32System;
import com.midorum.win32api.struct.PointFloat;
import com.midorum.win32api.struct.PointInt;
import com.midorum.win32api.struct.PointLong;
import com.midorum.win32api.util.RelativeCoordinates;
import midorum.melbone.model.exception.CriticalErrorException;
import midorum.melbone.model.settings.key.WindowHolder;

import java.util.function.BiFunction;

/**
 * predefined extractors
 */
public enum SettingExtractor {

    EXTRACTOR_UNSUPPORTED((windowHolder, point) -> {
        throw new UnsupportedOperationException("There is no extractor for this key");
    }),
    WINDOW_TITLE_EXTRACTOR((windowHolder, point) ->
            windowHolder.getWindow().getText().orElseThrow(() -> new CriticalErrorException("Window hasn't title"))),
    WINDOW_CLASS_NAME_EXTRACTOR((windowHolder, point) ->
            windowHolder.getWindow().getClassName().orElseThrow(() -> new CriticalErrorException("Window hasn't class name"))),
    WINDOW_PROCESS_NAME_EXTRACTOR((windowHolder, point) ->
            windowHolder.getWindow().getProcess().name().orElseThrow(() -> new CriticalErrorException("Window process hasn't name"))),
    WINDOW_RELATIVE_POINT_EXTRACTOR((windowHolder, point) -> {
        final RelativeCoordinates relativeCoordinates = Win32System.getInstance().getRelativeCoordinates(windowHolder.getWindow().getWindowRectangle());//FIXME
        return new PointFloat(relativeCoordinates.windowRelativeX(point.x()), relativeCoordinates.windowRelativeY(point.y()));
    }),
    SCREEN_POINT_EXTRACTOR((windowHolder, point) -> {
        final PointInt absoluteScreenPoint = Win32System.getInstance().getAbsoluteScreenPoint(point);//FIXME
        return new PointLong(absoluteScreenPoint.x(), absoluteScreenPoint.y());
    }),
    WINDOW_DIMENSIONS_EXTRACTOR((windowHolder, point) ->
            windowHolder.getWindow().getWindowRectangle()),
    FLOAT_EXTRACTOR((windowHolder, point) -> {
        throw new UnsupportedOperationException();//TODO
    });

    private final BiFunction<WindowHolder, PointInt, Object> extractor;

    SettingExtractor(final BiFunction<WindowHolder, PointInt, Object> extractor) {
        this.extractor = extractor;
    }

    public BiFunction<WindowHolder, PointInt, Object> extractor() {
        return extractor;
    }
}
