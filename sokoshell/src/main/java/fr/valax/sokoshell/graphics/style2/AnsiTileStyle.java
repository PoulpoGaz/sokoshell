package fr.valax.sokoshell.graphics.style2;

import fr.valax.sokoshell.graphics.Graphics;
import fr.valax.sokoshell.graphics.Surface;
import fr.valax.sokoshell.solver.Map;
import org.jline.utils.AttributedString;

import java.util.Objects;

public class AnsiTileStyle extends TileStyle {

    private final AttributedString[] str;

    public AnsiTileStyle(AttributedString[] str) {
        this.str = Objects.requireNonNull(str);

        if (str.length == 0) {
            throw new IllegalArgumentException("Empty array");
        }

        for (int i = 0; i < str.length; i++) {
            Objects.requireNonNull(str[i]);

            if (str[i].columnLength() != str.length) {
                throw new IllegalArgumentException(i + "-th string doesn't have a length of " + str.length);
            }
        }
    }

    @Override
    public void draw(Graphics g, Map map, int x, int y, int size) {
        Surface s = g.getSurface();

        if (size <= str.length) {
            for (int y2 = 0; y2 < size; y2++) {
                s.draw(str[y2], 0, size, x, y + y2);
            }
        } else {
            for (int y2 = 0; y2 < size; y2++) {
                for (int x2 = 0; x2 < size; x2 += size) {
                    s.draw(str[y2], 0, Math.min(size - x2, str[y2].length()), x + x2, y + y2);
                }
            }
        }
    }
}
