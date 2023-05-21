package midorum.melbone.window.dev;

import midorum.melbone.model.window.launcher.LauncherWindow;
import midorum.melbone.window.WindowFactory;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class TargetLauncherDev {

    public static void main(String[] args) {
        final WindowFactory windowFactory = new WindowFactory.Builder().build();
        final Optional<LauncherWindow> launcherWindow = windowFactory.findLauncherWindow();
        assertThat(launcherWindow.isPresent(), is(true));
    }
}
