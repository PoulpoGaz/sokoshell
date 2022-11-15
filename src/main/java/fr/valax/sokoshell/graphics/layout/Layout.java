package fr.valax.sokoshell.graphics.layout;

import fr.valax.sokoshell.graphics.Component;

import java.awt.*;

/**
 * @see java.awt.LayoutManager
 */
public interface Layout {

    void addComponent(Component component, Object constraints);

    void removeComponent(Component component);

    Dimension preferredSize(Component parent);

    void layout(Component parent);
}
