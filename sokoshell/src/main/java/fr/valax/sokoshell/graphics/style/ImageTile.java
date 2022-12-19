package fr.valax.sokoshell.graphics.style;

import fr.valax.sokoshell.graphics.Graphics;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageTile extends TileStyle {

    private final BufferedImage image;

    public ImageTile(int size, BufferedImage image) {
        super(size);
        this.image = image;

        if (image.getWidth() != size || image.getHeight() != size) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public TileStyle merge(TileStyle foreground) {
        if (foreground instanceof ImageTile fg) {
            BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = image.createGraphics();
            g2d.drawImage(this.image, 0, 0, null);
            g2d.drawImage(fg.getImage(), 0, 0, null);
            g2d.dispose();

            return new ImageTile(size, image);

        } else if (foreground instanceof AnsiTile fg) {
            return new AnsiTile(size, getAsString()).merge(fg);

        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public void draw(Graphics g, int x, int y) {
        g.drawImage(image, x, y);
    }

    @Override
    public AttributedString[] getAsString() {
        AttributedString[] strings = new AttributedString[image.getHeight()];
        AttributedStringBuilder asb = new AttributedStringBuilder();

        for (int y = 0; y < image.getHeight(); y++) {
            asb.setLength(0);
            asb.style(AttributedStyle.DEFAULT);

            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                int alpha = (rgb >> 24) & 0xFF;

                if (alpha < 255) {
                    asb.append(" ");
                } else {
                    int red = (rgb >> 16) & 0xFF;
                    int green = (rgb >> 8) & 0xFF;
                    int blue = rgb & 0xFF;

                    asb.styled(AttributedStyle.DEFAULT.background(red, green, blue), " ");
                }
            }

            strings[y] = asb.toAttributedString();
        }

        return strings;
    }

    public BufferedImage getImage() {
        return image;
    }
}
