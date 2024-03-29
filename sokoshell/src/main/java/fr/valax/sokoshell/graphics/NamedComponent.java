package fr.valax.sokoshell.graphics;

import fr.valax.sokoshell.graphics.layout.Layout;
import org.jline.utils.AttributedString;

import java.awt.*;
import java.util.Objects;

public class NamedComponent {

    public static Component create(String name, Component comp) {
        return create(new AttributedString(name), comp, 1);
    }

    public static Component create(String name, Component comp, int minGap) {
        return create(new AttributedString(name), comp, minGap);
    }

    public static Component create(AttributedString name, Component comp, int minGap) {
        Component container = new Component();
        container.setLayout(new MyLayout(minGap));
        container.add(new Label(name), MyLayout.NAME);
        container.add(comp, MyLayout.COMPONENT);

        return container;
    }

    private static class MyLayout implements Layout {

        private static final String NAME = "name";
        private static final String COMPONENT = "comp";

        private final int minGap;

        private Label name;
        private Component comp;

        public MyLayout(int minGap) {
            this.minGap = minGap;
        }

        @Override
        public void addComponent(Component component, Object constraints) {
            if (Objects.equals(constraints, NAME)) {
                this.name = (Label) component;
            } else if (Objects.equals(constraints, COMPONENT)) {
                this.comp = component;
            } else {
                throw new IllegalArgumentException();
            }
        }

        @Override
        public void removeComponent(Component component) {
            if (name == component) {
                name = null;
            } else if (comp == component) {
                comp = null;
            }
        }

        @Override
        public Dimension preferredSize(Component parent) {
            Insets i = parent.getInsets();
            Dimension dim = new Dimension();

            dim.width = i.left + i.right;
            dim.height = i.top + i.bottom;

            if (name != null) {
                Dimension namePrefSize = name.getPreferredSize();

                dim.width = namePrefSize.width;
                dim.height = namePrefSize.height;
            }

            if (comp != null) {
                Dimension compPrefSize = comp.getPreferredSize();

                dim.width += compPrefSize.width;
                dim.height = Math.max(compPrefSize.height, dim.height);
            }
            if (comp != null && name != null) {
                dim.width += minGap;
            }

            dim.width += i.left + i.right;
            dim.height += i.top + i.bottom;

            return dim;
        }

        @Override
        public void layout(Component parent) {
            if (name != null || comp != null) {
                Insets i = parent.getInsets();

                int w = parent.getWidth() - i.left - i.right;
                int h = parent.getHeight() - i.top - i.bottom;
                int x = i.left;
                int y = i.top;

                if (name == null) {
                    comp.setBounds(x, y, w, h);
                } else if (comp == null) {
                    name.setBounds(x, y, w, h);
                } else {
                    Dimension namePrefSize = name.getPreferredSize();
                    Dimension compPrefSize = comp.getPreferredSize();

                    if (compPrefSize.width >= w || compPrefSize.width + minGap >= w) {
                        name.setBounds(0, 0, 0, 0);
                        comp.setBounds(x, y, w, h);
                    } else if (namePrefSize.width + minGap + compPrefSize.width >= w) {
                        int nameW = w - compPrefSize.width - minGap;

                        name.setBounds(x, y, nameW, h);
                        comp.setBounds(x + nameW + minGap, y, w - nameW - minGap, h);
                    } else {
                        name.setBounds(x, y, namePrefSize.width, h);
                        comp.setBounds(x + namePrefSize.width + minGap, y, w - namePrefSize.width - minGap, h);
                    }
                }
            }
        }
    }
}
