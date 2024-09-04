package midorum.melbone.window;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JDialog;
import javax.swing.JPanel;

import com.midorum.win32api.facade.Rectangle;

public final class VisualImageGenerator {

    private VisualImageGenerator() {
    }

    public static void main(String[] args) {
        System.out.println("VisualImageGenerator");
        // final Visualizer visualizer = new VisualImageGenerator().new Visualizer();
        // // visualizer.x1();
        // visualizer.x2();
    }

    private BufferedImage generateImage(final Rectangle rectangle) {
        final int width = rectangle.width();
        final int height = rectangle.height();
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, width, height);
        g2d.setColor(Color.RED);
        g2d.drawLine(0, 0, width, height);
        g2d.drawLine(0, height, width, 0);
        g2d.dispose();
        return img;
    }

    private BufferedImage generateImage(final Rectangle rectangle, final TextLabel... labels) {
        final int width = rectangle.width();
        final int height = rectangle.height();
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, width, height);
        for (final TextLabel label : labels) {
            g2d.setColor(label.color);
            g2d.drawString(label.text, label.x, label.y);
        }
        g2d.dispose();
        return img;
    }

    private record TextLabel(String text, int x, int y, Color color) {
    }

    private class Visualizer {

        private void x1() {
            final BufferedImage screenImage = generateImage(new Rectangle(0, 0, 1000, 1000));
            final BufferedImage subImage = screenImage.getSubimage(490, 490, 100, 100);
            showImage("screen", screenImage);
            showImage("subImage", subImage);
        }

        private void x2() {
            final BufferedImage screenImage = generateImage(new Rectangle(0, 0, 2000, 2000),
                    new TextLabel("test", 245, 178, Color.BLUE),
                    new TextLabel("test", 34, 12, Color.RED),
                    new TextLabel("test", 320, 98, Color.BLUE),
                    new TextLabel("873509", 800, 723, Color.CYAN));
            final BufferedImage stampImage = generateImage(new Rectangle(0, 0, 50, 15),
                    new TextLabel("test", 0, 10, Color.BLUE));
            showImage("screen", screenImage);
            showImage("stamp", stampImage);
        }

        public void showImage(final String title, final BufferedImage image) {
            JDialog dialog = new JDialog();
            dialog.setTitle(title);
            dialog.setPreferredSize(new Dimension(image.getWidth() + 10, image.getHeight() + 30));
            dialog.add(new JPanel() {
                @Override
                public void paint(final Graphics g) {
                    g.drawImage(image, 0, 0, this);
                }
            });
            dialog.pack();
            dialog.validate();
            dialog.setVisible(true);
        }

    }
}
/*
 * c:; cd 'c:\workspace\gitrepo\win32\test-module';
 * & 'C:\.jdks\graalvm-jdk-22\bin\java.exe'
 * '--enable-preview'
 * '-XX:+ShowCodeDetailsInExceptionMessages'
 * '--module-path' 'C:\workspace\gitrepo\win32\test-module\bin'
 * '-m' 'com.example/com.example.App'
 * 
 * 
 * 
 */