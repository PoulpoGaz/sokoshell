package fr.valax.sokoshell.commands;

import fr.valax.sokoshell.graphics.*;
import org.jline.terminal.Size;
import org.jline.terminal.Terminal;

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

        public Test(Terminal terminal) {
            super(terminal);
        }

        @Override
        protected void init() {
            Component root = new Component();
            root.setLayout(new BorderLayout());

            root.add(new Label("Hello world from south!"), BorderLayout.SOUTH);
            root.add(new Label("Hello world from north!"), BorderLayout.NORTH);
            root.add(new Label("Hello world from east!"), BorderLayout.EAST);
            root.add(new Label("Hello world from west!"), BorderLayout.WEST);

            Component center = new Component();
            center.setLayout(new BorderLayout());
            center.add(new Label("Center center"), BorderLayout.CENTER);
            center.add(new Label("Center north"), BorderLayout.NORTH);
            center.add(new Label("Center east"), BorderLayout.EAST);

            root.add(center, BorderLayout.CENTER);

            setRootComponent(root);
        }

        @Override
        protected int render(Size size) {
            Component root = getRootComponent();
            print(root, 0);

            return 0;
        }

        private int print(Component component, int y) {
            surface.draw(toString(component), 0, y);

            y++;
            for (int i = 0; i < component.getComponentCount(); i++) {
                Component comp = component.getComponent(i);

                if (comp instanceof Label label) {
                    surface.draw(label.getText().toAnsi() + toString(label), 0, y);
                    y++;
                } else {
                    y = print(comp, y + 1);
                }
            }

            return y;
        }

        private String toString(Component c) {
            return "(x=%d; y=%d; w=%d; h=%d)".formatted(c.getX(), c.getY(), c.getWidth(), c.getHeight());
        }

        @Override
        protected void update() {

        }
    }
}
