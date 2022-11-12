package fr.valax.sokoshell.graphics;

import org.jline.terminal.Terminal;

import javax.swing.event.EventListenerList;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A class that is heavily inspired by {@link java.awt.Component}
 */
public class Component {

    TerminalEngine<?> engine;
    Terminal terminal;

    private Component parent;
    private int x;
    private int y;
    private int width;
    private int height;
    private boolean visible = true;

    private Border border = null;

    private final EventListenerList listeners = new EventListenerList();
    final List<Component> components = new ArrayList<>();


    private Layout layout = null;
    private boolean valid = false;
    private final ComponentListener componentListener;

    private Dimension prefSize;
    private boolean prefSizeSet;

    public Component() {
        componentListener = comp -> invalidate();
    }

    public void draw(Graphics g) {
        if (isVisible() && getHeight() > 0 && getWidth() > 0) {
            drawBorder(g);
            drawComponent(g);
        }

        drawChildren(g);
    }

    protected void drawBorder(Graphics g) {
        if (border != null) {
            border.drawBorder(this, g, 0, 0, width, height);
        }
    }

    protected void drawComponent(Graphics g) {

    }

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

    public void update() {
        if (!valid) {
            doLayout();
        }

        for (int i = 0; i < components.size(); i++) {
            Component c = components.get(i);

            if (c.isVisible() && c.getWidth() > 0 && c.getHeight() > 0) {
                c.update();
            }
        }
    }

    //
    // Layout
    //

    private void doLayout() {
        if (layout != null) {
            layout.layout(this);
        }

        valid = true;
    }

    public void invalidate() {
        valid = false;

        if (parent != null && parent.isValid()) {
            parent.invalidate();
        }
    }

    public boolean isValid() {
        return valid;
    }

    public void add(Component component) {
        add(component, null);
    }

    public void add(Component component, Object constraints) {
        if (component != null) {
            if (isAncestor(component)) {
                throw new IllegalStateException();
            }

            components.add(component);

            if (component.parent != null) {
                component.parent.remove(component);
            }
            component.parent = this;

            if (layout != null) {
                layout.addComponent(component, constraints);
            }

            component.addComponentListener(componentListener);
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

    public boolean remove(Component component) {
        if (component != null && components.remove(component)) {
            invalidate();
            return true;
        } else {
            return false;
        }
    }

    public Layout getLayout() {
        return layout;
    }

    public void setLayout(Layout layout) {
        this.layout = layout;
        valid = true;
    }

    public void setSize(int width, int height) {
        setBounds(x, y, width, height);
    }

    public void moveTo(int x, int y) {
        setBounds(x, y, width, height);
    }

    public void setBounds(int x, int y, int width, int height) {
        if (width == this.width && height == this.height && this.x == x && this.y == y) {
            return;
        }

        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        invalidate();

        for (ComponentListener l : getComponentListeners()) {
            l.componentResized(this);
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

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }



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

    protected Dimension compPreferredSize() {
        return null;
    }

    public void setPrefSize(Dimension prefSize) {
        this.prefSize = prefSize;
        prefSizeSet = prefSize != null;
    }

    public boolean isPreferredSizeSet() {
        return prefSizeSet;
    }



    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        if (visible != this.visible) {
            this.visible = visible;
            invalidate();
        }
    }




    public boolean isRoot() {
        return parent == null;
    }

    public int getComponentCount() {
        return components.size();
    }

    public Component getComponent(int i) {
        return components.get(i);
    }

    public Terminal getTerminal() {
        return terminal;
    }

    public Border getBorder() {
        return border;
    }

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

    public Insets getInsets() {
        if (border == null) {
            return new Insets(0, 0, 0, 0);
        } else {
            return border.getBorderInsets(this);
        }
    }

    //
    // Listeners
    //

    public void addComponentListener(ComponentListener listener) {
        listeners.add(ComponentListener.class, listener);
    }

    public void removeListener(ComponentListener listener) {
        listeners.remove(ComponentListener.class, listener);
    }

    public ComponentListener[] getComponentListeners() {
        return listeners.getListeners(ComponentListener.class);
    }
}
