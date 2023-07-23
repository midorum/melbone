package midorum.melbone.settings.internal.setting;

import midorum.melbone.model.settings.setting.ApplicationSettings;
import midorum.melbone.settings.SettingKeys;
import midorum.melbone.settings.internal.management.SettingsFactoryInternal;
import midorum.melbone.settings.managment.SettingPropertyNaming;
import org.junit.jupiter.api.Test;

class ApplicationSettingsImplTest {

    private final SettingsFactoryInternal settingsFactoryInternal = new SettingsFactoryInternal.Builder()
            .settingPropertyNaming(new SettingPropertyNaming.Builder().inMemoryStorage().build()).build();
    private final ApplicationSettings applicationSettings = settingsFactoryInternal.settingsProvider().settings().application();

    @Test
    void actionsCount() {
        new SettingTester(settingsFactoryInternal, SettingKeys.Application.actionsCount)
                .settingGetter(applicationSettings::actionsCount)
                .normalValues(0, 1, 5)
                .invalidValues(-1, 6, 10)
                .wrongTypeValues("string")
                .extractFrom()
                .parseFrom(new SettingTester.ParsePair("1", 1))
                .test();
    }

    @Test
    void maxAccountsSimultaneously() {
        new SettingTester(settingsFactoryInternal, SettingKeys.Application.maxAccountsSimultaneously)
                .settingGetter(applicationSettings::maxAccountsSimultaneously)
                .normalValues(0, 1, 3)
                .invalidValues(-1)
                .wrongTypeValues("string")
                .extractFrom()
                .parseFrom(new SettingTester.ParsePair("1", 1))
                .test();
    }

    @Test
    void speedFactor() {
        new SettingTester(settingsFactoryInternal, SettingKeys.Application.speedFactor)
                .settingGetter(applicationSettings::speedFactor)
                .normalValues(0.0F, 0.3F, 1.0F, 1.1F)
                .invalidValues(-0.3F, -1.0F, -1.1F)
                .wrongTypeValues("string")
                .extractFrom()
                .parseFrom(new SettingTester.ParsePair("1.5", 1.5f))
                .test();
    }

    @Test
    void taskPerformingDelay() {
        new SettingTester(settingsFactoryInternal, SettingKeys.Application.taskPerformingDelay)
                .settingGetter(applicationSettings::taskPerformingDelay)
                .normalValues(0, 1, 5)
                .invalidValues(-1)
                .wrongTypeValues("string")
                .extractFrom()
                .parseFrom(new SettingTester.ParsePair("1", 1))
                .test();
    }

    @Test
    void stampDeviation() {
        new SettingTester(settingsFactoryInternal, SettingKeys.Application.stampDeviation)
                .settingGetter(applicationSettings::stampDeviation)
                .normalValues(0, 10, 50)
                .invalidValues(-1)
                .wrongTypeValues("string")
                .extractFrom()
                .parseFrom(new SettingTester.ParsePair("1", 1))
                .test();
    }

    @Test
    void randomRoutineDelayMax() {
        new SettingTester(settingsFactoryInternal, SettingKeys.Application.randomRoutineDelayMax)
                .settingGetter(applicationSettings::randomRoutineDelayMax)
                .normalValues(0L, 5L, 15L)
                .invalidValues(-1)
                .wrongTypeValues("string")
                .extractFrom()
                .parseFrom(new SettingTester.ParsePair("1", 1L))
                .test();
    }

    @Test
    void checkHealthBeforeLaunch() {
        new SettingTester(settingsFactoryInternal, SettingKeys.Application.checkHealthBeforeLaunch)
                .settingGetter(applicationSettings::checkHealthBeforeLaunch)
                .normalValues(true, false)
                .invalidValues()
                .wrongTypeValues("string")
                .extractFrom()
                .parseFrom(new SettingTester.ParsePair("true", true), new SettingTester.ParsePair("false", false))
                .test();
    }

    @Test
    void closeOverlappingWindows() {
        new SettingTester(settingsFactoryInternal, SettingKeys.Application.closeOverlappingWindows)
                .settingGetter(applicationSettings::closeOverlappingWindows)
                .normalValues(true, false)
                .invalidValues()
                .wrongTypeValues("string")
                .extractFrom()
                .parseFrom(new SettingTester.ParsePair("true", true), new SettingTester.ParsePair("false", false))
                .test();
    }

    @Test
    void shotOverlappingWindows() {
        new SettingTester(settingsFactoryInternal, SettingKeys.Application.shotOverlappingWindows)
                .settingGetter(applicationSettings::shotOverlappingWindows)
                .normalValues(true, false)
                .invalidValues()
                .wrongTypeValues("string")
                .extractFrom()
                .parseFrom(new SettingTester.ParsePair("true", true), new SettingTester.ParsePair("false", false))
                .test();
    }

    @Test
    void overlappingWindowsToSkip() {
        new SettingTester(settingsFactoryInternal, SettingKeys.Application.overlappingWindowsToSkip)
                .settingGetter(applicationSettings::overlappingWindowsToSkip)
                .normalValues(new String[]{}, new String[]{"name"})
                .invalidValues(new int[]{1}, new long[]{1L}, new double[]{1.0})
                .wrongTypeValues("string", 1, 1L, 1.0)
                .extractFrom()
                .parseFrom(new SettingTester.ParsePair("[\"name\"]", new String[]{"name"}),
                        new SettingTester.ParsePair("[\"name1\",\"name2\", \"\",\" \"]", new String[]{"name1", "name2"}),
                        new SettingTester.ParsePair("", new String[]{}),
                        new SettingTester.ParsePair("[\"\"]", new String[]{}),
                        new SettingTester.ParsePair("[]", new String[]{}))
                .test();
    }

    @Test
    void overlappingWindowsToClose() {
        new SettingTester(settingsFactoryInternal, SettingKeys.Application.overlappingWindowsToClose)
                .settingGetter(applicationSettings::overlappingWindowsToClose)
                .normalValues(new String[]{}, new String[]{"name"})
                .invalidValues(new int[]{1}, new long[]{1L}, new double[]{1.0})
                .wrongTypeValues("string", 1, 1L, 1.0)
                .extractFrom()
                .parseFrom(new SettingTester.ParsePair("[\"name\"]", new String[]{"name"}),
                        new SettingTester.ParsePair("[\"name1\",\"name2\", \"\",\" \"]", new String[]{"name1", "name2"}),
                        new SettingTester.ParsePair("", new String[]{}),
                        new SettingTester.ParsePair("[\"\"]", new String[]{}),
                        new SettingTester.ParsePair("[]", new String[]{}))
                .test();
    }

}