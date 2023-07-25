package midorum.melbone.window;

import com.midorum.win32api.facade.Either;
import com.midorum.win32api.facade.IScreenShotMaker;
import com.midorum.win32api.facade.Rectangle;
import com.midorum.win32api.facade.WindowPoint;
import com.midorum.win32api.facade.exception.Win32ApiException;
import com.midorum.win32api.struct.PointInt;
import dma.validation.Validator;
import midorum.melbone.model.settings.PropertiesProvider;
import midorum.melbone.model.settings.account.AccountBinding;
import midorum.melbone.model.settings.setting.Settings;
import midorum.melbone.model.settings.stamp.Stamps;
import midorum.melbone.model.window.appcountcontrol.ApplicationsCountControlWindow;
import midorum.melbone.model.window.baseapp.BaseAppWindow;
import midorum.melbone.model.window.launcher.LauncherWindow;
import midorum.melbone.window.internal.appcountcontrol.ApplicationsCountControlWindowFactory;
import midorum.melbone.window.internal.baseapp.BaseAppWindowFactory;
import midorum.melbone.window.internal.common.CommonWindowService;
import midorum.melbone.window.internal.launcher.LauncherWindowFactory;
import midorum.melbone.window.internal.uac.UacWindowFactory;

import java.util.List;
import java.util.Optional;

public class WindowFactory {

    private final CommonWindowService commonWindowService;
    private final LauncherWindowFactory launcherWindowFactory;
    private final ApplicationsCountControlWindowFactory applicationsCountControlWindowFactory;
    private final BaseAppWindowFactory baseAppWindowFactory;

    private WindowFactory(final Settings settings, final AccountBinding accountBinding, final Stamps stamps, final PropertiesProvider propertiesProvider) {
        this.commonWindowService = new CommonWindowService(settings, propertiesProvider);
        final UacWindowFactory uacWindowFactory = new UacWindowFactory(commonWindowService, settings);
        this.launcherWindowFactory = new LauncherWindowFactory(commonWindowService, settings, uacWindowFactory, stamps);
        this.applicationsCountControlWindowFactory = new ApplicationsCountControlWindowFactory(commonWindowService, settings);
        this.baseAppWindowFactory = new BaseAppWindowFactory(commonWindowService, settings, accountBinding, stamps);
    }

    public Optional<LauncherWindow> findOrTryStartLauncherWindow() throws InterruptedException, Win32ApiException {
        return launcherWindowFactory.findWindowOrTryStartLauncher();
    }

    public Optional<LauncherWindow> findLauncherWindow() {
        return launcherWindowFactory.findWindow();
    }

    public Optional<ApplicationsCountControlWindow> findApplicationsCountControlWindow() {
        return applicationsCountControlWindowFactory.findWindow();
    }

    public List<BaseAppWindow> getAllBaseAppWindows() {
        return baseAppWindowFactory.getAllWindows();
    }

    public Optional<BaseAppWindow> findFirstUnboundBaseAppWindow() {
        return baseAppWindowFactory.findFirstUnboundWindow();
    }

    public Either<Optional<BaseAppWindow>> findUnboundBaseAppWindowAndBindWithAccount(String characterName) {
        return Either.resultOf(() -> baseAppWindowFactory.findUnboundWindowAndBindWithAccount(characterName));
    }

    public Optional<WindowPoint> getWindowByPoint(final PointInt screenPoint) {
        return commonWindowService.getWin32System().getWindowByPoint(Validator.checkNotNull(screenPoint).orThrowForSymbol("point"));
    }

    public void minimizeAllWindows() {
        commonWindowService.getWin32System().minimizeAllWindows();
    }

    public IScreenShotMaker getScreenShotMaker() {
        return commonWindowService.getWin32System().getScreenShotMaker();
    }

    public Rectangle screenResolution() {
        return commonWindowService.getWin32System().getScreenResolution();
    }

    public Either<Integer> countAllTargetProcesses() {
        return baseAppWindowFactory.listAllTargetProcesses().map(List::size);
    }

    public static class Builder {

        private Settings settings;
        private AccountBinding accountBinding;
        private Stamps stamps;
        private PropertiesProvider propertiesProvider;

        public Builder settings(final Settings settings) {
            this.settings = settings;
            return this;
        }

        public Builder accountBinding(final AccountBinding accountBinding) {
            this.accountBinding = accountBinding;
            return this;
        }

        public Builder stamps(final Stamps stamps) {
            this.stamps = stamps;
            return this;
        }

        public Builder propertiesProvider(final PropertiesProvider propertiesProvider) {
            this.propertiesProvider = propertiesProvider;
            return this;
        }

        public WindowFactory build() {
            return new WindowFactory(Validator.checkNotNull(this.settings).orThrowForSymbol("settings"),
                    Validator.checkNotNull(this.accountBinding).orThrowForSymbol("account storage"),
                    Validator.checkNotNull(this.stamps).orThrowForSymbol("stamp storage"),
                    Validator.checkNotNull(this.propertiesProvider).orThrowForSymbol("propertiesProvider"));
        }
    }

}
