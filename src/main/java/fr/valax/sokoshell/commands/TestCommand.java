package fr.valax.sokoshell.commands;

import fr.valax.sokoshell.graphics.*;
import org.jline.terminal.Size;
import org.jline.terminal.Terminal;

import javax.swing.*;
import java.io.InputStream;
import java.io.PrintStream;

public class TestCommand extends AbstractCommand {
    @Override
    public String getName() {
        return "test";
    }

    @Override
    public String getShortDescription() {
        return null;
    }

    @Override
    public String[] getUsage() {
        return new String[0];
    }

    @Override
    protected int executeImpl(InputStream in, PrintStream out, PrintStream err) throws InvalidArgument {
        try (Test test = new Test(helper.getTerminal())) {
            test.loop();
        }

        return 0;
    }

    private static class Test extends TerminalEngine<Key> {

        private boolean debugInfo = false;

        public Test(Terminal terminal) {
            super(terminal);
        }

        @Override
        protected void init() {
            Component root = new Component();
            root.setLayout(new BorderLayout());

            root.add(createLabel("Hello world from south!"), BorderLayout.SOUTH);
            root.add(createLabel("Hello world from north!"), BorderLayout.NORTH);
            root.add(createLabel("Hello world from east!"), BorderLayout.EAST);
            root.add(createLabel("Hello world from west!"), BorderLayout.WEST);

            Component center = new Component();
            center.setLayout(new BorderLayout());
            center.setBorder(new BasicBorder());
            center.add(createLabel("Center center"), BorderLayout.CENTER);
            center.add(createLabel("Center north"), BorderLayout.NORTH);
            center.add(createLabel("Center east"), BorderLayout.EAST);

            root.add(center, BorderLayout.CENTER);

            setRootComponent(root);


            Key.KEY_D.addTo(keyMap, terminal);
            Key.ESCAPE.addTo(keyMap, terminal);
        }

        private Label createLabel(String text) {
            Label label = new Label(text);
            label.setBorder(new BasicBorder());

            return label;
        }

        @Override
        protected int render(Size size) {
            if (debugInfo) {
                Component root = getRootComponent();
                print(root, 0, size.getRows());
            }

            return 0;
        }

        private int print(Component component, int y, int maxY) {
            surface.draw(toString(component), 0, y);

            y++;
            if (y > maxY) {
                return y;
            }
            for (int i = 0; i < component.getComponentCount(); i++) {
                Component comp = component.getComponent(i);

                if (comp instanceof Label label) {
                    surface.draw(label.getText().toAnsi() + toString(label), 0, y);
                    y++;
                } else {
                    y = print(comp, y + 1, maxY);
                }

                if (y > maxY) {
                    return y;
                }
            }

            return y;
        }

        private String toString(Component c) {
            return "(x=%d; y=%d; w=%d; h=%d)".formatted(c.getX(), c.getY(), c.getWidth(), c.getHeight());
        }

        @Override
        protected void update() {
            if (pressed(Key.ESCAPE)) {
                running = false;
                return;
            }

            if (justPressed(Key.KEY_D)) {
                debugInfo = !debugInfo;
            }
        }
    }
}
