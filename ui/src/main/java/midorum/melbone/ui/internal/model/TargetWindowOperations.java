package midorum.melbone.ui.internal.model;

import com.midorum.win32api.facade.WindowPoint;
import com.midorum.win32api.struct.PointInt;
import midorum.melbone.model.window.baseapp.BaseAppWindow;

import java.util.List;
import java.util.Optional;

public interface TargetWindowOperations {

    boolean isExistUnboundWindows();

    Optional<BaseAppWindow> getFirstNotBoundWindow();

    List<BaseAppWindow> getAllWindows();

    List<String> getBoundAccounts();

    List<String> getNotBoundAccounts();

    void minimizeAllWindows();

    Optional<WindowPoint> getWindowByPoint(PointInt point);
}
