package midorum.melbone.settings;

import com.midorum.win32api.facade.Rectangle;
import com.midorum.win32api.struct.PointFloat;
import com.midorum.win32api.struct.PointInt;
import com.midorum.win32api.struct.PointLong;
import midorum.melbone.model.persistence.StorageKey;
import midorum.melbone.model.settings.key.SettingData;
import midorum.melbone.model.settings.key.SettingKey;
import midorum.melbone.model.settings.key.SettingsManagerAction;
import midorum.melbone.model.settings.key.WindowHolder;
import midorum.melbone.settings.internal.defining.SettingDataImpl;

import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static midorum.melbone.settings.internal.defining.SettingExtractor.*;
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
        actionPeriod(Integer.class,
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
        stopAnimationDelay(Integer.class,
                "waiting to stop starting animation in seconds",
                30,
                INTEGER_POSITIVE_PREDICATE.predicate()),
        @Deprecated
        adjustWindows(Boolean.class,
                "when true - try adjust base window if it has wrong dimensions",
                false),
        stampDeviation(Integer.class,
                "stamp pixels components comparing deviation; 0 - exact congruence, >0 - deviation for each color component",
                0,
                INTEGER_POSITIVE_PREDICATE.predicate()),
        @Deprecated
        scheduledTaskPeriod(Integer.class,
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
                SettingsManagerAction.touchWindow,
                WINDOW_TITLE_EXTRACTOR.extractor()),
        windowClassName(String.class,
                "window class name",
                SettingsManagerAction.touchWindow,
                WINDOW_CLASS_NAME_EXTRACTOR.extractor()),
        processName(String.class,
                "process name",
                SettingsManagerAction.touchWindow,
                WINDOW_PROCESS_NAME_EXTRACTOR.extractor()),
        windowDimensions(Rectangle.class,
                "window dimensions",
                WINDOW_DIMENSIONS_PREDICATE.predicate(),
                SettingsManagerAction.touchWindow,
                WINDOW_DIMENSIONS_EXTRACTOR.extractor()),
        confirmQuitDialogTitle(String.class,
                "confirm quit dialog title",
                SettingsManagerAction.touchWindow,
                WINDOW_TITLE_EXTRACTOR.extractor()),
        confirmQuitDialogDimensions(Rectangle.class,
                "confirm quit dialog dimensions",
                WINDOW_DIMENSIONS_PREDICATE.predicate(),
                SettingsManagerAction.touchWindow,
                WINDOW_DIMENSIONS_EXTRACTOR.extractor()),
        initializationErrorDialogTitle(String.class,
                "initialization error dialog title",
                SettingsManagerAction.touchWindow,
                WINDOW_TITLE_EXTRACTOR.extractor()),
        initializationErrorDialogDimensions(Rectangle.class,
                "initialization error dialog dimensions",
                WINDOW_DIMENSIONS_PREDICATE.predicate(),
                SettingsManagerAction.touchWindow,
                WINDOW_DIMENSIONS_EXTRACTOR.extractor()),
        windowCloseButtonPoint(PointFloat.class,
                "window close button location; in range 0..1",
                POINT_FLOAT_RELATIVE_PREDICATE.predicate(),
                SettingsManagerAction.touchWindowElement,
                WINDOW_RELATIVE_POINT_EXTRACTOR.extractor()),
        desktopShortcutLocationAbsolutePoint(PointLong.class,
                "desktop icon location",
                POINT_LONG_POSITIVE_PREDICATE.predicate(),
                SettingsManagerAction.touchScreenElement,
                SCREEN_POINT_EXTRACTOR.extractor()),
        closeQuitConfirmPopupButtonPoint(PointFloat.class,
                "close quit confirm popup button location; in range 0..1",
                POINT_FLOAT_RELATIVE_PREDICATE.predicate(),
                SettingsManagerAction.touchWindowElement,
                WINDOW_RELATIVE_POINT_EXTRACTOR.extractor()),
        closeInitializationErrorDialogButtonPoint(PointFloat.class,
                "close initialization error dialog button location; in range 0..1",
                POINT_FLOAT_RELATIVE_PREDICATE.predicate(),
                SettingsManagerAction.touchWindowElement,
                WINDOW_RELATIVE_POINT_EXTRACTOR.extractor()),
        startButtonPoint(PointFloat.class,
                "start button location; in range 0..1",
                POINT_FLOAT_RELATIVE_PREDICATE.predicate(),
                SettingsManagerAction.touchWindowElement,
                WINDOW_RELATIVE_POINT_EXTRACTOR.extractor()),
        accountDropListPoint(PointFloat.class,
                "account drop list location; in range 0..1",
                POINT_FLOAT_RELATIVE_PREDICATE.predicate(),
                SettingsManagerAction.touchWindowElement,
                WINDOW_RELATIVE_POINT_EXTRACTOR.extractor()),
        accountLogoutPoint(PointFloat.class,
                "account drop list location; in range 0..1",
                POINT_FLOAT_RELATIVE_PREDICATE.predicate(),
                SettingsManagerAction.touchWindowElement,
                WINDOW_RELATIVE_POINT_EXTRACTOR.extractor()),
        loginInputPoint(PointFloat.class,
                "login input location; in range 0..1",
                POINT_FLOAT_RELATIVE_PREDICATE.predicate(),
                SettingsManagerAction.touchWindowElement,
                WINDOW_RELATIVE_POINT_EXTRACTOR.extractor()),
        passwordInputPoint(PointFloat.class,
                "password input location; in range 0..1",
                POINT_FLOAT_RELATIVE_PREDICATE.predicate(),
                SettingsManagerAction.touchWindowElement,
                WINDOW_RELATIVE_POINT_EXTRACTOR.extractor()),
        loginButtonPoint(PointFloat.class,
                "login button location; in range 0..1",
                POINT_FLOAT_RELATIVE_PREDICATE.predicate(),
                SettingsManagerAction.touchWindowElement,
                WINDOW_RELATIVE_POINT_EXTRACTOR.extractor()),
        maintenanceInProgressPopupConfirmButtonPoint(PointFloat.class,
                "\"Maintenance in progress\" popup Confirm button location; in range 0..1",
                POINT_FLOAT_RELATIVE_PREDICATE.predicate(),
                SettingsManagerAction.touchWindowElement,
                WINDOW_RELATIVE_POINT_EXTRACTOR.extractor());

        private final SettingData settingData;

        TargetLauncher(final Class<?> type,
                       final String description,
                       final SettingsManagerAction settingsManagerAction,
                       final BiFunction<WindowHolder, PointInt, Object> extractor) {
            this.settingData = new SettingDataImpl.Builder()
                    .type(type)
                    .description(description)
                    .storageKey(StorageKey.targetLauncher)
                    .settingsManagerAction(settingsManagerAction)
                    .extractor(extractor)
                    .build();
        }

        TargetLauncher(final Class<?> type,
                       final String description,
                       final Predicate<Object> validator,
                       final SettingsManagerAction settingsManagerAction,
                       final BiFunction<WindowHolder, PointInt, Object> extractor) {
            this.settingData = new SettingDataImpl.Builder()
                    .type(type)
                    .description(description)
                    .storageKey(StorageKey.targetLauncher)
                    .validator(validator)
                    .settingsManagerAction(settingsManagerAction)
                    .extractor(extractor)
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
                SettingsManagerAction.touchWindow,
                WINDOW_TITLE_EXTRACTOR.extractor()),
        confirmButtonPoint(PointFloat.class,
                "confirm button location; in range 0..1",
                POINT_FLOAT_RELATIVE_PREDICATE.predicate(),
                SettingsManagerAction.touchWindowElement,
                WINDOW_RELATIVE_POINT_EXTRACTOR.extractor()),
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
                           final SettingsManagerAction settingsManagerAction,
                           final BiFunction<WindowHolder, PointInt, Object> extractor) {
            this.settingData = new SettingDataImpl.Builder()
                    .type(type)
                    .description(description)
                    .storageKey(StorageKey.targetCountControl)
                    .settingsManagerAction(settingsManagerAction)
                    .extractor(extractor)
                    .build();
        }

        TargetCountControl(final Class<?> type,
                           final String description,
                           final Predicate<Object> validator,
                           final SettingsManagerAction settingsManagerAction,
                           final BiFunction<WindowHolder, PointInt, Object> extractor) {
            this.settingData = new SettingDataImpl.Builder()
                    .type(type)
                    .description(description)
                    .storageKey(StorageKey.targetCountControl)
                    .validator(validator)
                    .settingsManagerAction(settingsManagerAction)
                    .extractor(extractor)
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
                SettingsManagerAction.touchWindow,
                WINDOW_TITLE_EXTRACTOR.extractor()),
        windowClassName(String.class,
                "window class name",
                SettingsManagerAction.touchWindow,
                WINDOW_CLASS_NAME_EXTRACTOR.extractor()),
        processName(String.class,
                "process name",
                SettingsManagerAction.touchWindow,
                WINDOW_PROCESS_NAME_EXTRACTOR.extractor()),
        windowTimeout(Integer.class,
                "window appearing timeout in seconds",
                10,
                INTEGER_POSITIVE_PREDICATE.predicate()),
        selectCharacterButtonPoint(PointFloat.class,
                "select character button location; in range 0..1",
                POINT_FLOAT_RELATIVE_PREDICATE.predicate(),
                SettingsManagerAction.touchWindowElement,
                WINDOW_RELATIVE_POINT_EXTRACTOR.extractor()),
        startButtonPoint(PointFloat.class,
                "start game button location; in range 0..1",
                POINT_FLOAT_RELATIVE_PREDICATE.predicate(),
                SettingsManagerAction.touchWindowElement,
                WINDOW_RELATIVE_POINT_EXTRACTOR.extractor()),
        windowCloseButtonPoint(PointFloat.class,
                "window close button location; in range 0..1",
                POINT_FLOAT_RELATIVE_PREDICATE.predicate(),
                SettingsManagerAction.touchWindowElement,
                WINDOW_RELATIVE_POINT_EXTRACTOR.extractor()),
        windowMinimizeButtonPoint(PointFloat.class,
                "window minimize button location; in range 0..1",
                POINT_FLOAT_RELATIVE_PREDICATE.predicate(),
                SettingsManagerAction.touchWindowElement,
                WINDOW_RELATIVE_POINT_EXTRACTOR.extractor()),
        actionButtonPoint(PointFloat.class,
                "in-game action button location; in range 0..1",
                POINT_FLOAT_RELATIVE_PREDICATE.predicate(),
                SettingsManagerAction.touchWindowElement,
                WINDOW_RELATIVE_POINT_EXTRACTOR.extractor()),
        actionSecondButtonPoint(PointFloat.class,
                "in-game action second button location; in range 0..1",
                POINT_FLOAT_RELATIVE_PREDICATE.predicate(),
                SettingsManagerAction.touchWindowElement,
                WINDOW_RELATIVE_POINT_EXTRACTOR.extractor()),
        selectServerButtonPoint(PointFloat.class,
                "select server button offset for base scale; in range 0..1",
                POINT_FLOAT_RELATIVE_PREDICATE.predicate(),
                SettingsManagerAction.touchWindowElement,
                WINDOW_RELATIVE_POINT_EXTRACTOR.extractor()),
        connectServerButtonPoint(PointFloat.class,
                "select server button offset for base scale; in range 0..1",
                POINT_FLOAT_RELATIVE_PREDICATE.predicate(),
                SettingsManagerAction.touchWindowElement,
                WINDOW_RELATIVE_POINT_EXTRACTOR.extractor()),
        openOptionsButtonPoint(PointFloat.class,
                "open options button offset for default scale; in range 0..1",
                POINT_FLOAT_RELATIVE_PREDICATE.predicate(),
                SettingsManagerAction.touchWindowElement,
                WINDOW_RELATIVE_POINT_EXTRACTOR.extractor()),
        screenSettingsTabPoint(PointFloat.class,
                "open options button offset for default scale; in range 0..1",
                POINT_FLOAT_RELATIVE_PREDICATE.predicate(),
                SettingsManagerAction.touchWindowElement,
                WINDOW_RELATIVE_POINT_EXTRACTOR.extractor()),
        uiScaleChooser80Point(PointFloat.class,
                "open options button offset for default scale; in range 0..1",
                POINT_FLOAT_RELATIVE_PREDICATE.predicate(),
                SettingsManagerAction.touchWindowElement,
                WINDOW_RELATIVE_POINT_EXTRACTOR.extractor()),
        soundSettingsTabPoint(PointFloat.class,
                "open options button offset for default scale; in range 0..1",
                POINT_FLOAT_RELATIVE_PREDICATE.predicate(),
                SettingsManagerAction.touchWindowElement,
                WINDOW_RELATIVE_POINT_EXTRACTOR.extractor()),
        overallVolumeZeroLevelPoint(PointFloat.class,
                "open options button offset for default scale; in range 0..1",
                POINT_FLOAT_RELATIVE_PREDICATE.predicate(),
                SettingsManagerAction.touchWindowElement,
                WINDOW_RELATIVE_POINT_EXTRACTOR.extractor()),
        optionsApplyButtonPoint(PointFloat.class,
                "open options button offset for default scale; in range 0..1",
                POINT_FLOAT_RELATIVE_PREDICATE.predicate(),
                SettingsManagerAction.touchWindowElement,
                WINDOW_RELATIVE_POINT_EXTRACTOR.extractor()),
        needRestartPopupConfirmButtonPoint(PointFloat.class,
                "open options button offset for base scale; in range 0..1",
                POINT_FLOAT_RELATIVE_PREDICATE.predicate(),
                SettingsManagerAction.touchWindowElement,
                WINDOW_RELATIVE_POINT_EXTRACTOR.extractor()),
        menuExitOptionPoint(PointFloat.class,
                "Esc popup Exit option for base scale; in range 0..1",
                POINT_FLOAT_RELATIVE_PREDICATE.predicate(),
                SettingsManagerAction.touchWindowElement,
                WINDOW_RELATIVE_POINT_EXTRACTOR.extractor()),
        dailyTrackerButtonPoint(PointFloat.class,
                "In-game daly tracker button for base scale; in range 0..1",
                POINT_FLOAT_RELATIVE_PREDICATE.predicate(),
                SettingsManagerAction.touchWindowElement,
                WINDOW_RELATIVE_POINT_EXTRACTOR.extractor()),
        dailyTrackerTabPoint(PointFloat.class,
                "In-game daly tracker tab in popup for base scale; in range 0..1",
                POINT_FLOAT_RELATIVE_PREDICATE.predicate(),
                SettingsManagerAction.touchWindowElement,
                WINDOW_RELATIVE_POINT_EXTRACTOR.extractor()),
        trackLoginButtonPoint(PointFloat.class,
                "In-game track login button in popup for base scale; in range 0..1",
                POINT_FLOAT_RELATIVE_PREDICATE.predicate(),
                SettingsManagerAction.touchWindowElement,
                WINDOW_RELATIVE_POINT_EXTRACTOR.extractor()),
        closeDailyTrackerPopupButtonPoint(PointFloat.class,
                "In-game daily tracker popup close button for base scale; in range 0..1",
                POINT_FLOAT_RELATIVE_PREDICATE.predicate(),
                SettingsManagerAction.touchWindowElement,
                WINDOW_RELATIVE_POINT_EXTRACTOR.extractor()),
        manaIndicatorPoint(PointFloat.class,
                "In-game mana indicator for base scale; in range 0..1",
                POINT_FLOAT_RELATIVE_PREDICATE.predicate(),
                SettingsManagerAction.touchWindowElement,
                WINDOW_RELATIVE_POINT_EXTRACTOR.extractor()),
        disconnectedPopupCloseButtonPoint(PointFloat.class,
                "Disconnected popup Close button for base scale; in range 0..1",
                POINT_FLOAT_RELATIVE_PREDICATE.predicate(),
                SettingsManagerAction.touchWindowElement,
                WINDOW_RELATIVE_POINT_EXTRACTOR.extractor()),
        @Deprecated
        beforeMinimizingDelay(Long.class,
                "Delay before minimizing window frame: 0 - for disable, >0 - delay in milliseconds",
                5000L,
                LONG_POSITIVE_PREDICATE.predicate()),
        afterLaunchAccountDelay(Long.class,
                "Delay before minimizing window frame after account has launched: 0 - for disable, >0 - delay in milliseconds",
                5000L,
                LONG_POSITIVE_PREDICATE.predicate());

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
                      final SettingsManagerAction settingsManagerAction,
                      final BiFunction<WindowHolder, PointInt, Object> extractor) {
            this.settingData = new SettingDataImpl.Builder()
                    .type(type)
                    .description(description)
                    .storageKey(StorageKey.targetBaseApp)
                    .settingsManagerAction(settingsManagerAction)
                    .extractor(extractor)
                    .build();
        }

        TargetBaseApp(final Class<?> type,
                      final String description,
                      final Predicate<Object> validator,
                      final SettingsManagerAction settingsManagerAction,
                      final BiFunction<WindowHolder, PointInt, Object> extractor) {
            this.settingData = new SettingDataImpl.Builder()
                    .type(type)
                    .description(description)
                    .storageKey(StorageKey.targetBaseApp)
                    .validator(validator)
                    .settingsManagerAction(settingsManagerAction)
                    .extractor(extractor)
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
                SettingsManagerAction.touchWindow,
                WINDOW_CLASS_NAME_EXTRACTOR.extractor()),
        windowDimensions(Rectangle.class,
                "window dimensions",
                new Rectangle(0, 0, 456, 333),
                WINDOW_DIMENSIONS_PREDICATE.predicate(),
                SettingsManagerAction.touchWindow,
                WINDOW_DIMENSIONS_EXTRACTOR.extractor());

        private final SettingData settingData;

        Uac(final Class<?> type,
            final String description,
            final Object defaultValue,
            final SettingsManagerAction settingsManagerAction,
            final BiFunction<WindowHolder, PointInt, Object> extractor) {
            this.settingData = new SettingDataImpl.Builder()
                    .type(type)
                    .description(description)
                    .storageKey(StorageKey.uac)
                    .defaultValue(defaultValue)
                    .settingsManagerAction(settingsManagerAction)
                    .extractor(extractor)
                    .build();
        }

        Uac(final Class<?> type,
            final String description,
            final Object defaultValue,
            final Predicate<Object> validator,
            final SettingsManagerAction settingsManagerAction,
            final BiFunction<WindowHolder, PointInt, Object> extractor) {
            this.settingData = new SettingDataImpl.Builder()
                    .type(type)
                    .description(description)
                    .storageKey(StorageKey.uac)
                    .defaultValue(defaultValue)
                    .validator(validator)
                    .settingsManagerAction(settingsManagerAction)
                    .extractor(extractor)
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
