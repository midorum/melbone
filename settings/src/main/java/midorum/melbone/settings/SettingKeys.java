package midorum.melbone.settings;

import com.midorum.win32api.facade.Rectangle;
import com.midorum.win32api.struct.PointFloat;
import com.midorum.win32api.struct.PointInt;
import com.midorum.win32api.struct.PointLong;
import midorum.melbone.model.dto.KeyShortcut;
import midorum.melbone.model.persistence.StorageKey;
import midorum.melbone.model.settings.key.SettingData;
import midorum.melbone.model.settings.key.SettingKey;
import midorum.melbone.model.settings.key.SettingObtainWay;
import midorum.melbone.settings.internal.defining.SettingDataImpl;
import midorum.melbone.settings.internal.defining.SettingObtainWays;

import java.util.function.Predicate;
import java.util.stream.Stream;

import static midorum.melbone.settings.internal.defining.SettingValidator.*;

public interface SettingKeys {

    static SettingKey[] values() {
        return Stream.of(Application.values(), TargetLauncher.values(), TargetCountControl.values(), TargetBaseApp.values(), Uac.values())
                .flatMap(Stream::of)
                .filter(e -> e.internal().isEnabled())
                .toArray(SettingKey[]::new);
    }

    enum Application implements SettingKey {
        @Deprecated
        actionPeriod(Integer.class,//FIXME >>> delete
                "0 - for disable, >0 - checking delay in minutes",
                35,
                INTEGER_POSITIVE_PREDICATE.predicate()),
        actionsCount(Integer.class,
                "0 - for disable, 1-5 - count of actions in base window",
                1,
                o -> ((int) o) >= 0 && ((int) o) <= 5),
        maxAccountsSimultaneously(Integer.class,
                "0 - for disable, >0 - count of simultaneously working base windows",
                1,
                INTEGER_POSITIVE_PREDICATE.predicate()),
        speedFactor(Float.class,
                "overall speed: <1 - faster, >1 - slowly, 0 - for disable",
                1F,
                FLOAT_POSITIVE_PREDICATE.predicate()),
        taskPerformingDelay(Integer.class,
                "delay before starting execution user's order in seconds",
                0,
                INTEGER_POSITIVE_PREDICATE.predicate()),
        @Deprecated
        stopAnimationDelay(Integer.class,//FIXME >>> delete
                "waiting to stop starting animation in seconds",
                30,
                INTEGER_POSITIVE_PREDICATE.predicate()),
        @Deprecated
        adjustWindows(Boolean.class,//FIXME >>> delete
                "when true - try adjust base window if it has wrong dimensions",
                false),
        stampDeviation(Integer.class,
                "stamp pixels components comparing deviation; 0 - exact congruence, >0 - deviation for each color component",
                0,
                INTEGER_POSITIVE_PREDICATE.predicate()),
        @Deprecated
        scheduledTaskPeriod(Integer.class,//FIXME >>> delete
                "0 - for disable, >0 - scheduled tasks delay in minutes",
                5,
                INTEGER_POSITIVE_PREDICATE.predicate()),
        randomRoutineDelayMax(Long.class,
                "0 - for disable, >0 - maximal random delay on every routine iteration in minutes",
                5L,
                LONG_POSITIVE_PREDICATE.predicate());

        private final SettingData settingData;

        Application(final Class<?> type,
                    final String description,
                    final Object defaultValue) {
            this.settingData = new SettingDataImpl.Builder()
                    .type(type)
                    .description(description)
                    .storageKey(StorageKey.application)
                    .defaultValue(defaultValue)
                    .build();
        }

        Application(final Class<?> type,
                    final String description,
                    final Object defaultValue,
                    final Predicate<Object> validator) {
            this.settingData = new SettingDataImpl.Builder()
                    .type(type)
                    .description(description)
                    .storageKey(StorageKey.application)
                    .defaultValue(defaultValue)
                    .validator(validator)
                    .build();
        }

        @Override
        public SettingData internal() {
            return settingData;
        }
    }

    enum TargetLauncher implements SettingKey {
        windowTitle(String.class,
                "window title",
                SettingObtainWays.touchWindowAndGetTitle),
        windowClassName(String.class,
                "window class name",
                SettingObtainWays.touchWindowAndGetClassName),
        processName(String.class,
                "process name",
                SettingObtainWays.touchWindowAndGetProcessName),
        windowDimensions(Rectangle.class,
                "window dimensions",
                WINDOW_DIMENSIONS_PREDICATE.predicate(),
                SettingObtainWays.touchWindowAndGetDimensions),
        confirmQuitDialogTitle(String.class,
                "confirm quit dialog title",
                SettingObtainWays.touchWindowAndGetTitle),
        confirmQuitDialogDimensions(Rectangle.class,
                "confirm quit dialog dimensions",
                WINDOW_DIMENSIONS_PREDICATE.predicate(),
                SettingObtainWays.touchWindowAndGetDimensions),
        initializationErrorDialogTitle(String.class,
                "initialization error dialog title",
                SettingObtainWays.touchWindowAndGetTitle),
        initializationErrorDialogDimensions(Rectangle.class,
                "initialization error dialog dimensions",
                WINDOW_DIMENSIONS_PREDICATE.predicate(),
                SettingObtainWays.touchWindowAndGetDimensions),
        windowCloseButtonPoint(PointFloat.class,
                "window close button location; in range 0..1",
                POINT_FLOAT_RELATIVE_PREDICATE.predicate(),
                SettingObtainWays.touchWindowAndGetRelativePoint),
        @Deprecated
        desktopShortcutLocationAbsolutePoint(PointLong.class,//FIXME >>> delete
                "desktop icon location",
                POINT_LONG_POSITIVE_PREDICATE.predicate(),
                SettingObtainWays.touchScreenElementAndGetPoint),
        desktopShortcutLocationPoint(PointInt.class,
                "desktop icon location",
                POINT_INTEGER_POSITIVE_PREDICATE.predicate(),
                SettingObtainWays.touchScreenElementAndGetPoint),
        closeQuitConfirmPopupButtonPoint(PointFloat.class,
                "close quit confirm popup button location; in range 0..1",
                POINT_FLOAT_RELATIVE_PREDICATE.predicate(),
                SettingObtainWays.touchWindowAndGetRelativePoint),
        closeInitializationErrorDialogButtonPoint(PointFloat.class,
                "close initialization error dialog button location; in range 0..1",
                POINT_FLOAT_RELATIVE_PREDICATE.predicate(),
                SettingObtainWays.touchWindowAndGetRelativePoint),
        startButtonPoint(PointFloat.class,
                "start button location; in range 0..1",
                POINT_FLOAT_RELATIVE_PREDICATE.predicate(),
                SettingObtainWays.touchWindowAndGetRelativePoint),
        accountDropListPoint(PointFloat.class,
                "account drop list location; in range 0..1",
                POINT_FLOAT_RELATIVE_PREDICATE.predicate(),
                SettingObtainWays.touchWindowAndGetRelativePoint),
        accountLogoutPoint(PointFloat.class,
                "account drop list location; in range 0..1",
                POINT_FLOAT_RELATIVE_PREDICATE.predicate(),
                SettingObtainWays.touchWindowAndGetRelativePoint),
        loginInputPoint(PointFloat.class,
                "login input location; in range 0..1",
                POINT_FLOAT_RELATIVE_PREDICATE.predicate(),
                SettingObtainWays.touchWindowAndGetRelativePoint),
        passwordInputPoint(PointFloat.class,
                "password input location; in range 0..1",
                POINT_FLOAT_RELATIVE_PREDICATE.predicate(),
                SettingObtainWays.touchWindowAndGetRelativePoint),
        loginButtonPoint(PointFloat.class,
                "login button location; in range 0..1",
                POINT_FLOAT_RELATIVE_PREDICATE.predicate(),
                SettingObtainWays.touchWindowAndGetRelativePoint),
        maintenanceInProgressPopupConfirmButtonPoint(PointFloat.class,
                "\"Maintenance in progress\" popup Confirm button location; in range 0..1",
                POINT_FLOAT_RELATIVE_PREDICATE.predicate(),
                SettingObtainWays.touchWindowAndGetRelativePoint);

        private final SettingData settingData;

        TargetLauncher(final Class<?> type,
                       final String description,
                       final SettingObtainWay obtainWay) {
            this.settingData = new SettingDataImpl.Builder()
                    .type(type)
                    .description(description)
                    .storageKey(StorageKey.targetLauncher)
                    .obtainWay(obtainWay)
                    .build();
        }

        TargetLauncher(final Class<?> type,
                       final String description,
                       final Predicate<Object> validator,
                       final SettingObtainWay obtainWay) {
            this.settingData = new SettingDataImpl.Builder()
                    .type(type)
                    .description(description)
                    .storageKey(StorageKey.targetLauncher)
                    .validator(validator)
                    .obtainWay(obtainWay)
                    .build();
        }

        @Override
        public SettingData internal() {
            return settingData;
        }
    }

    enum TargetCountControl implements SettingKey {
        windowTitle(String.class,
                "window title",
                SettingObtainWays.touchWindowAndGetTitle),
        confirmButtonPoint(PointFloat.class,
                "confirm button location; in range 0..1",
                POINT_FLOAT_RELATIVE_PREDICATE.predicate(),
                SettingObtainWays.touchWindowAndGetRelativePoint),
        windowTimeout(Long.class,
                "window appearing timeout in seconds",
                10L,
                LONG_POSITIVE_PREDICATE.predicate());

        private final SettingData settingData;

        TargetCountControl(final Class<?> type,
                           final String description,
                           final Object defaultValue,
                           final Predicate<Object> validator) {
            this.settingData = new SettingDataImpl.Builder()
                    .type(type)
                    .description(description)
                    .storageKey(StorageKey.targetCountControl)
                    .defaultValue(defaultValue)
                    .validator(validator)
                    .build();
        }

        TargetCountControl(final Class<?> type,
                           final String description,
                           final SettingObtainWay obtainWay) {
            this.settingData = new SettingDataImpl.Builder()
                    .type(type)
                    .description(description)
                    .storageKey(StorageKey.targetCountControl)
                    .obtainWay(obtainWay)
                    .build();
        }

        TargetCountControl(final Class<?> type,
                           final String description,
                           final Predicate<Object> validator,
                           final SettingObtainWay obtainWay) {
            this.settingData = new SettingDataImpl.Builder()
                    .type(type)
                    .description(description)
                    .storageKey(StorageKey.targetCountControl)
                    .validator(validator)
                    .obtainWay(obtainWay)
                    .build();
        }

        @Override
        public SettingData internal() {
            return settingData;
        }
    }

    enum TargetBaseApp implements SettingKey {
        windowTitle(String.class,
                "window title",
                SettingObtainWays.touchWindowAndGetTitle),
        windowClassName(String.class,
                "window class name",
                SettingObtainWays.touchWindowAndGetClassName),
        processName(String.class,
                "process name",
                SettingObtainWays.touchWindowAndGetProcessName),
        windowTimeout(Integer.class,
                "window appearing timeout in seconds",
                10,
                INTEGER_POSITIVE_PREDICATE.predicate()),
        selectCharacterButtonPoint(PointFloat.class,
                "select character button location; in range 0..1",
                POINT_FLOAT_RELATIVE_PREDICATE.predicate(),
                SettingObtainWays.touchWindowAndGetRelativePoint),
        startButtonPoint(PointFloat.class,
                "start game button location; in range 0..1",
                POINT_FLOAT_RELATIVE_PREDICATE.predicate(),
                SettingObtainWays.touchWindowAndGetRelativePoint),
        windowCloseButtonPoint(PointFloat.class,
                "window close button location; in range 0..1",
                POINT_FLOAT_RELATIVE_PREDICATE.predicate(),
                SettingObtainWays.touchWindowAndGetRelativePoint),
        windowMinimizeButtonPoint(PointFloat.class,
                "window minimize button location; in range 0..1",
                POINT_FLOAT_RELATIVE_PREDICATE.predicate(),
                SettingObtainWays.touchWindowAndGetRelativePoint),
        actionButtonPoint(PointFloat.class,
                "in-game action button location; in range 0..1",
                POINT_FLOAT_RELATIVE_PREDICATE.predicate(),
                SettingObtainWays.touchWindowAndGetRelativePoint),
        actionSecondButtonPoint(PointFloat.class,
                "in-game action second button location; in range 0..1",
                POINT_FLOAT_RELATIVE_PREDICATE.predicate(),
                SettingObtainWays.touchWindowAndGetRelativePoint),
        selectServerButtonPoint(PointFloat.class,
                "select server button offset for base scale; in range 0..1",
                POINT_FLOAT_RELATIVE_PREDICATE.predicate(),
                SettingObtainWays.touchWindowAndGetRelativePoint),
        connectServerButtonPoint(PointFloat.class,
                "select server button offset for base scale; in range 0..1",
                POINT_FLOAT_RELATIVE_PREDICATE.predicate(),
                SettingObtainWays.touchWindowAndGetRelativePoint),
        openOptionsButtonPoint(PointFloat.class,
                "open options button offset for default scale; in range 0..1",
                POINT_FLOAT_RELATIVE_PREDICATE.predicate(),
                SettingObtainWays.touchWindowAndGetRelativePoint),
        screenSettingsTabPoint(PointFloat.class,
                "open options button offset for default scale; in range 0..1",
                POINT_FLOAT_RELATIVE_PREDICATE.predicate(),
                SettingObtainWays.touchWindowAndGetRelativePoint),
        uiScaleChooser80Point(PointFloat.class,
                "open options button offset for default scale; in range 0..1",
                POINT_FLOAT_RELATIVE_PREDICATE.predicate(),
                SettingObtainWays.touchWindowAndGetRelativePoint),
        soundSettingsTabPoint(PointFloat.class,
                "open options button offset for default scale; in range 0..1",
                POINT_FLOAT_RELATIVE_PREDICATE.predicate(),
                SettingObtainWays.touchWindowAndGetRelativePoint),
        overallVolumeZeroLevelPoint(PointFloat.class,
                "open options button offset for default scale; in range 0..1",
                POINT_FLOAT_RELATIVE_PREDICATE.predicate(),
                SettingObtainWays.touchWindowAndGetRelativePoint),
        optionsApplyButtonPoint(PointFloat.class,
                "open options button offset for default scale; in range 0..1",
                POINT_FLOAT_RELATIVE_PREDICATE.predicate(),
                SettingObtainWays.touchWindowAndGetRelativePoint),
        needRestartPopupConfirmButtonPoint(PointFloat.class,
                "open options button offset for base scale; in range 0..1",
                POINT_FLOAT_RELATIVE_PREDICATE.predicate(),
                SettingObtainWays.touchWindowAndGetRelativePoint),
        menuExitOptionPoint(PointFloat.class,
                "Esc popup Exit option for base scale; in range 0..1",
                POINT_FLOAT_RELATIVE_PREDICATE.predicate(),
                SettingObtainWays.touchWindowAndGetRelativePoint),
        dailyTrackerButtonPoint(PointFloat.class,
                "In-game daly tracker button for base scale; in range 0..1",
                POINT_FLOAT_RELATIVE_PREDICATE.predicate(),
                SettingObtainWays.touchWindowAndGetRelativePoint),
        dailyTrackerTabPoint(PointFloat.class,
                "In-game daly tracker tab in popup for base scale; in range 0..1",
                POINT_FLOAT_RELATIVE_PREDICATE.predicate(),
                SettingObtainWays.touchWindowAndGetRelativePoint),
        trackLoginButtonPoint(PointFloat.class,
                "In-game track login button in popup for base scale; in range 0..1",
                POINT_FLOAT_RELATIVE_PREDICATE.predicate(),
                SettingObtainWays.touchWindowAndGetRelativePoint),
        closeDailyTrackerPopupButtonPoint(PointFloat.class,
                "In-game daily tracker popup close button for base scale; in range 0..1",
                POINT_FLOAT_RELATIVE_PREDICATE.predicate(),
                SettingObtainWays.touchWindowAndGetRelativePoint),
        manaIndicatorPoint(PointFloat.class,
                "In-game mana indicator for base scale; in range 0..1",
                POINT_FLOAT_RELATIVE_PREDICATE.predicate(),
                SettingObtainWays.touchWindowAndGetRelativePoint),
        disconnectedPopupCloseButtonPoint(PointFloat.class,
                "Disconnected popup Close button for base scale; in range 0..1",
                POINT_FLOAT_RELATIVE_PREDICATE.predicate(),
                SettingObtainWays.touchWindowAndGetRelativePoint),
        @Deprecated
        beforeMinimizingDelay(Long.class,//FIXME >>> delete
                "Delay before minimizing window frame: 0 - for disable, >0 - delay in milliseconds",
                5000L,
                LONG_POSITIVE_PREDICATE.predicate()),
        afterLaunchAccountDelay(Long.class,
                "Delay before minimizing window frame after account has launched: 0 - for disable, >0 - delay in milliseconds",
                5000L,
                LONG_POSITIVE_PREDICATE.predicate()),
        stopAnimationHotkey(KeyShortcut.class,
                "Hot key to stop animation",
                SettingObtainWays.pressHotkey),
        cancelCurrentOperationHotkey(KeyShortcut.class,
                "Hot key to cancel current operation in window",
                SettingObtainWays.pressHotkey),
        openMenuHotkey(KeyShortcut.class,
                "Hot key to open menu popup",
                SettingObtainWays.pressHotkey),
        openAccountInfoHotkey(KeyShortcut.class,
                "Hot key to open account info popup",
                SettingObtainWays.pressHotkey);

        private final SettingData settingData;

        TargetBaseApp(final Class<?> type,
                      final String description,
                      final Object defaultValue,
                      final Predicate<Object> validator) {
            this.settingData = new SettingDataImpl.Builder()
                    .type(type)
                    .description(description)
                    .storageKey(StorageKey.targetBaseApp)
                    .defaultValue(defaultValue)
                    .validator(validator)
                    .build();
        }

        TargetBaseApp(final Class<?> type,
                      final String description,
                      final SettingObtainWay obtainWay) {
            this.settingData = new SettingDataImpl.Builder()
                    .type(type)
                    .description(description)
                    .storageKey(StorageKey.targetBaseApp)
                    .obtainWay(obtainWay)
                    .build();
        }

        TargetBaseApp(final Class<?> type,
                      final String description,
                      final Predicate<Object> validator,
                      final SettingObtainWay obtainWay) {
            this.settingData = new SettingDataImpl.Builder()
                    .type(type)
                    .description(description)
                    .storageKey(StorageKey.targetBaseApp)
                    .validator(validator)
                    .obtainWay(obtainWay)
                    .build();
        }

        @Override
        public SettingData internal() {
            return settingData;
        }
    }

    enum Uac implements SettingKey {
        windowClassName(String.class,
                "window class name",
                "Optional[Credential Dialog Xaml Host]",
                SettingObtainWays.touchWindowAndGetClassName),
        windowDimensions(Rectangle.class,
                "window dimensions",
                new Rectangle(0, 0, 456, 333),
                WINDOW_DIMENSIONS_PREDICATE.predicate(),
                SettingObtainWays.touchWindowAndGetDimensions);

        private final SettingData settingData;

        Uac(final Class<?> type,
            final String description,
            final Object defaultValue,
            final SettingObtainWay obtainWay) {
            this.settingData = new SettingDataImpl.Builder()
                    .type(type)
                    .description(description)
                    .storageKey(StorageKey.uac)
                    .defaultValue(defaultValue)
                    .obtainWay(obtainWay)
                    .build();
        }

        Uac(final Class<?> type,
            final String description,
            final Object defaultValue,
            final Predicate<Object> validator,
            final SettingObtainWay obtainWay) {
            this.settingData = new SettingDataImpl.Builder()
                    .type(type)
                    .description(description)
                    .storageKey(StorageKey.uac)
                    .defaultValue(defaultValue)
                    .validator(validator)
                    .obtainWay(obtainWay)
                    .build();
        }

        @Override
        public SettingData internal() {
            return settingData;
        }

    }

    enum Internal implements SettingKey {
        version(String.class, "Version of setting storage");

        private final SettingData settingData;

        Internal(final Class<?> type,
                 final String description) {
            this.settingData = new SettingDataImpl.Builder()
                    .type(type)
                    .description(description)
                    .storageKey(StorageKey.internal)
                    .build();
        }

        @Override
        public SettingData internal() {
            return settingData;
        }
    }
}
