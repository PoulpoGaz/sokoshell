package fr.valax.sokoshell.graphics.style2;

import fr.valax.sokoshell.graphics.Graphics;
import fr.valax.sokoshell.solver.Map;

import java.awt.image.BufferedImage;
import java.util.Objects;

public class ImageTileStyle extends TileStyle {

    private final BufferedImage image;

    public ImageTileStyle(BufferedImage image) {
        this.image = Objects.requireNonNull(image);

        if (image.getWidth() != image.getHeight()) {
            throw new IllegalArgumentException("Image isn't a square");
        }
    }

    @Override
    public void draw(Graphics g, Map map, int x, int y, int size) {
        for (int y2 = 0; y2 < size; y2 += size) {
            for (int x2 = 0; x2 < size; x2 += size) {
                g.drawImage(image,
                        0, 0,
                        Math.min(size - x2, image.getWidth()), Math.min(size - y2, image.getHeight()),
                        x + x2, y + y2);
            }
        }
    }
}
