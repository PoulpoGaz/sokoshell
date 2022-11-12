package fr.valax.sokoshell.graphics;

import java.awt.*;

/**
 * @see java.awt.LayoutManager
 */
public interface Layout {

    void addComponent(Component component, Object constraints);

    Dimension preferredSize(Component parent);

    void layout(Component parent);
}
