package fr.valax.sokoshell.graphics;

import fr.valax.sokoshell.graphics.layout.Layout;
import org.jline.terminal.Terminal;

import javax.swing.event.EventListenerList;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A class that is heavily inspired by {@link java.awt.Component}
 */
@SuppressWarnings("ForLoopReplaceableByForEach")
public class Component {

    TerminalEngine engine;
    Terminal terminal;

    private Component parent;
    private int x;
    private int y;
    private int width;
    private int height;
    private boolean visible = true;

    private Border border = null;

    protected final EventListenerList listeners = new EventListenerList();
    final List<Component> components = new ArrayList<>();


    private Layout layout = null;
    private boolean valid = false;
    private final ComponentListener componentListener;

    boolean repaint = true;

    private Dimension prefSize;
    private boolean prefSizeSet;

    public Component() {
        componentListener = new ComponentListener() {
            @Override
            public void componentResized(Component comp) {
                invalidate();
                repaint();
            }

            @Override
            public void componentMoved(Component comp) {
                invalidate();
                repaint();
            }
        };
    }

    /**
     * Draw this component with the specified {@link Graphics} object.
     * The surface is automatically translated to the absolute coordinate
     * of the component and clipped to the size of the component.
     * It calls in order:
     * <ul>
     *     <li>{@link #drawBorder(Graphics)}</li>
     *     <li>{@link #drawComponent(Graphics)}</li>
     *     <li>{@link #drawChildren(Graphics)}</li>
     * </ul>
     *
     * @param g Graphics object to draw with
     */
    public void draw(Graphics g) {
        repaint = false;

        if (isVisible() && getHeight() > 0 && getWidth() > 0) {
            if (border != null) {
                drawBorder(g);
            }
            drawComponent(g);
        }

        drawChildren(g);
    }

    /**
     * Draw the border of the component. The border is non-null
     *
     * @param g Graphics object to draw with
     */
    protected void drawBorder(Graphics g) {
        border.drawBorder(this, g, 0, 0, width, height);
    }

    /**
     * Draw this component. Implementation of Component usually
     * overrides this method to draw.
     * @param g Graphics object to draw with
     */
    protected void drawComponent(Graphics g) {

    }

    /**
     * Draw all children of this component. This is this method who
     * automatically translate and clip the surface.
     * @param g Graphics object to draw with
     */
    protected void drawChildren(Graphics g) {
        Surface s = g.getSurface();

        for (int i = 0; i < components.size(); i++) {
            Component c = components.get(i);

            if (c.isVisible() && c.getWidth() > 0 && c.getHeight() > 0) {
                s.translate(c.getX(), c.getY());
                Rectangle clip = s.getClip();
                s.intersectClip(0, 0, c.getWidth(), c.getHeight());
                c.draw(g);
                s.setClip(clip);
                s.translate(-c.getX(), -c.getY());
            }
        }
    }

    /**
     * Informs the {@link TerminalEngine} that this component need to be repainted.
     * Actually, blitting isn't supported so all component wil be repainted.
     */
    public void repaint() {
        repaint = true;

        if (parent != null && !parent.repaint) {
            parent.repaint();
        }
    }


    /**
     * Update this component and his children.
     * Implementation of Component usually overrides {@link #updateComponent()}
     */
    public void update() {
        updateComponent();

        for (int i = 0; i < components.size(); i++) {
            Component c = components.get(i);

            if (c.terminal != null && c.isVisible() && c.getWidth() > 0 && c.getHeight() > 0) {
                c.update();
            }
        }
    }

    /**
     * Update this component.
     * Implementation of Component usually overrides this method.
     */
    protected void updateComponent() {

    }

    // **********
    // * Layout *
    // **********

    /**
     * Lay out the component if needed i.e. if the component is not {@linkplain #isValid() valid}
     */
    void layoutIfNeeded() {
        if (isValid()) {
            return;
        }

        if (layout != null) {
            layout.layout(this);
        }

        for (int i = 0; i < components.size(); i++) {
            components.get(i).layoutIfNeeded();
        }

        valid = true;
    }

    /**
     * Marks this component as not valid which means that it needs to be lay out.
     */
    public void invalidate() {
        valid = false;

        if (parent != null && parent.isValid()) {
            parent.invalidate();
        }
    }

    /**
     * @return {@code true} if the component needs to be lay out
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * @return the layout of this component
     */
    public Layout getLayout() {
        return layout;
    }

    /**
     * Sets the layout of this component
     * @param layout new layout
     */
    public void setLayout(Layout layout) {
        this.layout = layout;
        invalidate();
    }

    /**
     * Sets the size of this component to (width, height).
     * This method is usually used by {@link Layout}
     *
     * @param width new width of the component
     * @param height new height of the component
     */
    public void setSize(int width, int height) {
        setBounds(x, y, width, height);
    }

    /**
     * Move the component to (x, y) relative to his ancestor
     * This method is usually used by {@link Layout}
     *
     * @param x x coordinate relative to his ancestor
     * @param y y coordinate relative to his ancestor
     */
    public void moveTo(int x, int y) {
        setBounds(x, y, width, height);
    }

    /**
     * Sets the new dimension and the new position of this component relative to his ancestor
     * This method is usually used by {@link Layout}
     *
     * @param x x coordinate relative to his ancestor
     * @param y y coordinate relative to his ancestor
     * @param width new width of the component
     * @param height new height of the component
     */
    public void setBounds(int x, int y, int width, int height) {
        boolean resized = this.width != width || this.height != height;
        boolean moved = this.x != x || this.y != y;

        if (!resized && !moved) {
            return;
        }

        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        invalidate();

        if (resized) {
            for (ComponentListener l : getComponentListeners()) {
                l.componentResized(this);
            }
        }

        if (moved) {
            for (ComponentListener l : getComponentListeners()) {
                l.componentMoved(this);
            }
        }
    }

    /**
     * returns x position of this component relative to his parent
     * @return x position of this component relative to his parent
     */
    public int getX() {
        return x;
    }

    /**
     * returns y position of this component relative to his parent
     * @return y position of this component relative to his parent
     */
    public int getY() {
        return y;
    }

    /**
     * @return the width of this component
     */
    public int getWidth() {
        return width;
    }

    /**
     * @return the height of this component
     */
    public int getHeight() {
        return height;
    }

    /**
     * @return the size of this component
     */
    public Dimension getSize() {
        return new Dimension(width, height);
    }

    /**
     * Returns the preferred size of this component.
     * It first checks if the user set a preferred size.
     * Otherwise, it uses {@link #compPreferredSize()} ()}. If this method
     * returns {@code null} then it uses if possible {@link Layout#preferredSize(Component)}.
     * Else, it returns the empty dimension
     *
     * @return the preferred size of this component
     */
    public Dimension getPreferredSize() {
        return preferredSize();
    }

    private Dimension preferredSize() {
        Dimension dim = prefSize;
        if (dim == null || !(isPreferredSizeSet() || isValid())) {
            dim = compPreferredSize();

            if (dim == null && layout != null) {
                dim = layout.preferredSize(this);
            }

            if (dim == null) {
                dim = new Dimension();
            }
        }

        prefSize = dim;
        return new Dimension(prefSize);
    }

    /**
     * Computes the preferred size of this component
     * @return the preferred size of this component or {@code null}
     */
    protected Dimension compPreferredSize() {
        return null;
    }

    /**
     * Sets the preferred size of this component
     * @param prefSize preferred size
     */
    public void setPreferredSize(Dimension prefSize) {
        this.prefSize = prefSize;
        prefSizeSet = prefSize != null;
    }

    /**
     * @return {@code true} if {@link #setPreferredSize(Dimension)} was used
     * to set the preferred size of this component
     */
    public boolean isPreferredSizeSet() {
        return prefSizeSet;
    }





    /**
     * Adds the specified component to this component.
     * The method does nothing if the component is null.
     * The new component is removed from his old component tree.
     * The component parent is invalidated.
     *
     * @param component the component ot add.
     * @throws IllegalArgumentException if you try to add this component to itself
     */
    public void add(Component component) {
        add(component, null);
    }

    /**
     * Adds the specified component to this component.
     * The method does nothing if the component is null.
     * The new component is removed from his old component tree.
     * The component parent is invalidated.
     *
     * @param component the component ot add.
     * @param constraints component constraints
     * @throws IllegalArgumentException if you try to add this component to itself
     */
    public void add(Component component, Object constraints) {
        if (component != null) {
            if (isAncestor(component)) {
                throw new IllegalArgumentException("Adding component's parent to itself");
            }

            components.add(component);

            if (component.parent != null) {
                component.parent.remove(component);
            }
            component.parent = this;

            if (layout != null) {
                layout.addComponent(component, constraints);
            }

            if (terminal != null || component.terminal != null) {
                component.setTerminal(terminal, engine);
            }

            component.addComponentListener(componentListener);
            invalidate();
        }
    }

    /**
     * Removes this component
     * @param component the component to remove
     * @return {@code true} if the component was successfully removed
     */
    public boolean remove(Component component) {
        if (component != null && components.remove(component)) {
            component.setTerminal(null, null);
            component.invalidate();

            if (layout != null) {
                layout.removeComponent(component);
            }

            invalidate();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Removes all children of this component
     */
    public void removeAll() {
        if (!components.isEmpty()) {
            while (!components.isEmpty()) {
                Component c = components.get(0);
                c.setTerminal(null, null);
                c.invalidate();

                if (layout != null) {
                    layout.removeComponent(c);
                }

                components.remove(0);
            }
            invalidate();
        }
    }

    /**
     * Returns true if the specified component is an ancestor of this component
     * @param component a component
     * @return true if the specified component is an ancestor of this component
     */
    private boolean isAncestor(Component component) {
        Component c = component;

        while (c != null) {
            if (c == this) {
                return true;
            }

            c = c.parent;
        }

        return false;
    }


    /**
     * @return {@code true} if ths component is visible
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Sets this component as visible or not.
     * It invalidates the component if needed
     * @param visible should the component be visible?
     */
    public void setVisible(boolean visible) {
        if (visible != this.visible) {
            this.visible = visible;
            invalidate();
        }
    }

    /**
     * @return {@code true} if this component is the root of the tree.
     */
    public boolean isRoot() {
        return parent == null;
    }

    /**
     * @return the number of children of this component
     */
    public int getComponentCount() {
        return components.size();
    }

    /**
     * @param i index of the component
     * @return the component at index i
     */
    public Component getComponent(int i) {
        return components.get(i);
    }

    /**
     * @return the border of this component
     */
    public Border getBorder() {
        return border;
    }

    /**
     * Sets the border of this component.
     * It invalidates if needed the component
     * @param border new border
     */
    public void setBorder(Border border) {
        if (border != this.border) {
            Border old = this.border;
            this.border = border;

            if (border == null || old == null ||
                    !border.getBorderInsets(this).equals(old.getBorderInsets(this))) {
                invalidate();
            }
        }
    }

    /**
     * @return the insets of this component ie the space occupied by the border
     */
    public Insets getInsets() {
        if (border == null) {
            return new Insets(0, 0, 0, 0);
        } else {
            return border.getBorderInsets(this);
        }
    }


    /**
     * Convert an absolute point in the terminal to a point relative to this component
     * @param absX absolute x
     * @param absY absolute y
     * @return relative point
     */
    public Point relativePoint(int absX, int absY) {
        Point p = new Point(absX, absY);

        Component comp = this;
        while (comp != null) {
            p.x -= comp.x;
            p.y -= comp.y;

            comp = comp.parent;
        }

        return p;
    }

    /**
     * @param absX absolute x
     * @param absY absolute y
     * @return {@code true} if the absolute point is inside this component
     */
    public boolean isInside(int absX, int absY) {
        int relX = absX;
        int relY = absY;

        Component comp = parent;

        while (comp != null) {
            relX -= comp.x;
            relY -= comp.y;

            comp = comp.parent;
        }

        return relX >= x && relX < x + width && relY >= y && relY < y + height;
    }

    /**
     * @param p relative point p
     * @return {@code true} if the relative point is inside this component
     */
    public boolean isInsideNotAbs(Point p) {
        return p.x >= 0 && p.x < width && p.y >= 0 && p.y < height;
    }


    // =========
    // * Input *
    // =========

    /**
     * @param k key
     * @return {@code true} if key k is pressed
     */
    public boolean keyPressed(Key k) {
        return engine.keyPressed(k);
    }

    // *************
    // * Listeners *
    // *************

    public void addComponentListener(ComponentListener listener) {
        listeners.add(ComponentListener.class, listener);
    }

    public void removeListener(ComponentListener listener) {
        listeners.remove(ComponentListener.class, listener);
    }

    public ComponentListener[] getComponentListeners() {
        return listeners.getListeners(ComponentListener.class);
    }


    void setTerminal(Terminal terminal, TerminalEngine engine) {
        this.terminal = terminal;
        this.engine = engine;

        for (int i = 0; i < components.size(); i++) {
            components.get(i).setTerminal(terminal, engine);
        }

        valid = false;
    }

    public Terminal getTerminal() {
        return terminal;
    }

    public TerminalEngine getEngine() {
        return engine;
    }
}
