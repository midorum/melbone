package midorum.melbone.model.settings.stamp;

import com.midorum.win32api.facade.Rectangle;
import midorum.melbone.model.settings.key.StampKey;

public interface Stamp {
    StampKey key();

    String description();

    int[] wholeData();

    int[] firstLine();

    Rectangle location();

    Rectangle windowRect();

    Rectangle windowClientRect();

    Rectangle windowClientToScreenRect();
}
