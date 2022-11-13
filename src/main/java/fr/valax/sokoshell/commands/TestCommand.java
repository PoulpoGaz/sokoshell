package fr.valax.sokoshell.commands;

import fr.valax.sokoshell.graphics.*;
import fr.valax.sokoshell.graphics.layout.*;

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
        try (TerminalEngine engine = new TerminalEngine(helper.getTerminal())) {
            initEngine(engine);
            engine.show();
        }

        return 0;
    }

    private void initEngine(TerminalEngine engine) {
        engine.getKeyMap().setAmbiguousTimeout(100L);

        Component root = new Component() {
            @Override
            public void updateComponent() {
                if (keyReleased(Key.ESCAPE)) {
                    getEngine().stop();
                }
            }
        };
        root.setLayout(new BorderLayout());


        Component east = new Component();
        east.setLayout(new VerticalLayout());

        VerticalConstraint c = new VerticalConstraint();
        c.xAlignment = 0;
        east.add(createLabel("top1"), c);
        c.xAlignment = 0.5f;
        east.add(createLabel("top2"), c);

        c.xAlignment = 1;
        c.orientation = VerticalLayout.Orientation.BOTTOM;
        east.add(createLabel("bot"), c);
        east.add(new MemoryBar(), c);

        Component centerCenter = new Component();
        GridLayoutConstraints glc = new GridLayoutConstraints();
        glc.x = 1;
        glc.y = 0;
        glc.fill = GridLayoutConstraints.HORIZONTAL;
        glc.weightY = 0.2;
        centerCenter.setLayout(new GridLayout());
        centerCenter.setBorder(new BasicBorder());
        centerCenter.add(createLabel("ccc"), glc);
        glc.x = 2;
        glc.y = 1;
        glc.weightY = 0;
        glc.fill = GridLayoutConstraints.BOTH;
        centerCenter.add(createLabel("ccc2"), glc);

        Component comp = new Component();
        comp.setLayout(new BorderLayout());
        comp.add(createLabel("north"), BorderLayout.NORTH);
        comp.add(createLabel("centeerrrrr"), BorderLayout.CENTER);
        comp.add(createLabel("south"), BorderLayout.SOUTH);

        glc.fill = GridLayoutConstraints.NONE;
        glc.x = 1;
        centerCenter.add(comp, glc);

        glc.weightY = 1;
        glc.y = 2;
        glc.x = 0;
        glc.weightX = 1;
        glc.fill = GridLayoutConstraints.BOTH;
        centerCenter.add(createLabel("I grow!"), glc);
        glc.y = 2;
        glc.x = 1;
        glc.weightX = 0;
        centerCenter.add(createLabel("I don't grow!"), glc);
        glc.y = 2;
        glc.x = 2;
        glc.weightX = 0.5;
        centerCenter.add(createLabel("I grow but less!"), glc);

        Component center = new Component();
        center.setLayout(new BorderLayout());
        center.setBorder(new BasicBorder());
        center.add(centerCenter, BorderLayout.CENTER);
        center.add(createLabel("Center north"), BorderLayout.NORTH);
        center.add(createLabel("Center east"), BorderLayout.EAST);


        root.add(new MemoryBar(), BorderLayout.SOUTH);
        root.add(createLabel("Hello world from north!"), BorderLayout.NORTH);
        root.add(createLabel("Hello world from west!"), BorderLayout.WEST);
        root.add(center, BorderLayout.CENTER);
        root.add(east, BorderLayout.EAST);


        engine.setRootComponent(root);
        Key.D.addTo(engine);
        Key.ESCAPE.addTo(engine);
    }

    private Label createLabel(String text) {
        Label label = new Label(text);
        label.setBorder(new BasicBorder());

        return label;
    }

    private int print(Component component, int y, int maxY) {
        Surface surface = null;
        surface.draw(toString(component), 0, y);

        y++;
        if (y > maxY) {
            return y;
        }
        for (int i = 0; i < component.getComponentCount(); i++) {
            Component comp = component.getComponent(i);

            if (comp instanceof Label label) {
                surface.draw(toString(label) + label.getText().toAnsi(), 0, y);
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
}
