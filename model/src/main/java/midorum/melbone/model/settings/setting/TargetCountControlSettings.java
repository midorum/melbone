package midorum.melbone.model.settings.setting;

import com.midorum.win32api.struct.PointFloat;

public interface TargetCountControlSettings {

    long windowTimeout();

    String windowTitle();

    PointFloat confirmButtonPoint();
}
