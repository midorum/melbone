package midorum.melbone.ui.internal.common;

import com.midorum.win32api.facade.Rectangle;
import midorum.melbone.model.settings.stamp.Stamp;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class Preview extends JPanel {

    private final Stamp stamp;

    public Preview(Stamp stamp) {
        this.stamp = stamp;
        setPreferredSize(new Dimension(stamp.location().width(), stamp.location().height()));
    }

    @Override
    public void paint(Graphics g) {
        final Rectangle location = stamp.location();
        final int width = location.width();
        final int height = location.height();
        final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        image.setRGB(0, 0, width, height, stamp.wholeData(), 0, width);
        g.drawImage(image, 0, 0, this);
    }
}
