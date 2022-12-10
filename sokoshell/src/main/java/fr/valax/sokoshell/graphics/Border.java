package fr.valax.sokoshell.graphics;

import java.awt.*;

public interface Border {

    void drawBorder(Component c, Graphics g, int x, int y, int width, int height);

    Insets getBorderInsets(Component c);
}
