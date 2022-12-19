package fr.valax.sokoshell.commands;

import fr.valax.sokoshell.graphics.*;
import fr.valax.sokoshell.graphics.Button;
import fr.valax.sokoshell.graphics.Component;
import fr.valax.sokoshell.graphics.Graphics;
import fr.valax.sokoshell.graphics.Label;
import fr.valax.sokoshell.graphics.layout.*;
import fr.valax.sokoshell.graphics.layout.BorderLayout;
import fr.valax.sokoshell.graphics.layout.GridLayout;
import org.jline.terminal.MouseEvent;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;

import java.awt.*;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;

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
        engine.trackMouse(Terminal.MouseTracking.Any);

        fr.valax.sokoshell.graphics.Component root = new fr.valax.sokoshell.graphics.Component() {
            @Override
            public void updateComponent() {
                if (keyPressed(Key.ESCAPE)) {
                    getEngine().stop();
                }
            }
        };
        root.setLayout(new fr.valax.sokoshell.graphics.layout.BorderLayout());
        createDefault(root);

        engine.setRootComponent(root);
        Key.D.bind(engine);
        Key.E.bind(engine);
        Key.ESCAPE.bind(engine);
    }

    private void createDefault(fr.valax.sokoshell.graphics.Component root) {
        root.removeAll();

        fr.valax.sokoshell.graphics.Component east = new fr.valax.sokoshell.graphics.Component();
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

        fr.valax.sokoshell.graphics.Component centerCenter = new fr.valax.sokoshell.graphics.Component();
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

        fr.valax.sokoshell.graphics.Button button = new fr.valax.sokoshell.graphics.Button();
        button.setText(AttributedString.fromAnsi("click me to open painter"));
        button.addActionListener((s, co) -> openPainter(s, co, root));
        button.setBorder(new BasicBorder());

        fr.valax.sokoshell.graphics.Component comp = new fr.valax.sokoshell.graphics.Component();
        comp.setLayout(new fr.valax.sokoshell.graphics.layout.BorderLayout());
        comp.add(createLabel("north"), fr.valax.sokoshell.graphics.layout.BorderLayout.NORTH);
        comp.add(button, fr.valax.sokoshell.graphics.layout.BorderLayout.CENTER);
        comp.add(createLabel("south"), fr.valax.sokoshell.graphics.layout.BorderLayout.SOUTH);

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

        fr.valax.sokoshell.graphics.Component center = new fr.valax.sokoshell.graphics.Component();
        center.setLayout(new fr.valax.sokoshell.graphics.layout.BorderLayout());
        center.setBorder(new BasicBorder());
        center.add(centerCenter, fr.valax.sokoshell.graphics.layout.BorderLayout.CENTER);
        center.add(createLabel("Center north"), fr.valax.sokoshell.graphics.layout.BorderLayout.NORTH);
        center.add(createLabel("Center east"), fr.valax.sokoshell.graphics.layout.BorderLayout.EAST);


        root.add(new MemoryBar(), fr.valax.sokoshell.graphics.layout.BorderLayout.SOUTH);
        root.add(createLabel("Hello world from north!"), fr.valax.sokoshell.graphics.layout.BorderLayout.NORTH);
        root.add(createLabel("Hello world from west!"), fr.valax.sokoshell.graphics.layout.BorderLayout.WEST);
        root.add(center, fr.valax.sokoshell.graphics.layout.BorderLayout.CENTER);
        root.add(east, fr.valax.sokoshell.graphics.layout.BorderLayout.EAST);
    }

    private void openPainter(Object s_, String c_, fr.valax.sokoshell.graphics.Component root) {
        root.removeAll();

        Canvas c = new Canvas();

        fr.valax.sokoshell.graphics.Component left = new fr.valax.sokoshell.graphics.Component();
        left.setLayout(new VerticalLayout());
        left.setBorder(new BasicBorder(false, false, false, true));

        VerticalConstraint vc = new VerticalConstraint();
        vc.topGap = vc.bottomGap = 1;

        fr.valax.sokoshell.graphics.Button exit = new fr.valax.sokoshell.graphics.Button();
        exit.setText(new AttributedString("Quit"));
        exit.setBorder(new BasicBorder());
        exit.addActionListener((source, command) -> createDefault(root));

        fr.valax.sokoshell.graphics.Button erase = new fr.valax.sokoshell.graphics.Button();
        erase.setBorder(new BasicBorder());
        erase.setText(new AttributedString("Erase"));
        erase.addActionListener((source, command) -> {
            if (c.isPainting()) {
                erase.setText(new AttributedString("Paint"));
            } else {
                erase.setText(new AttributedString("Erase"));
            }

            c.setPainting(!c.isPainting());
        });

        fr.valax.sokoshell.graphics.Button clear = new Button();
        clear.setText(new AttributedString("Clear"));
        clear.setBorder(new BasicBorder());
        clear.addActionListener((source, command) -> c.clear());

        left.add(exit, vc);
        left.add(erase, vc);
        left.add(clear, vc);


        fr.valax.sokoshell.graphics.Component wrapper = new fr.valax.sokoshell.graphics.Component();
        wrapper.setLayout(new fr.valax.sokoshell.graphics.layout.BorderLayout());
        wrapper.setBorder(new BasicBorder());
        wrapper.add(c, fr.valax.sokoshell.graphics.layout.BorderLayout.CENTER);

        root.add(wrapper, fr.valax.sokoshell.graphics.layout.BorderLayout.CENTER);
        root.add(left, BorderLayout.WEST);
        root.repaint();
    }

    private Label createLabel(String text) {
        Label label = new Label(text);
        label.setBorder(new BasicBorder());

        return label;
    }

    private static class Canvas extends fr.valax.sokoshell.graphics.Component implements ComponentListener {

        private static final AttributedString FILLED =
                new AttributedString(" ", AttributedStyle.DEFAULT.background(AttributedStyle.WHITE));
        private static final AttributedString NOT_FILLED =
                new AttributedString(" ");

        private boolean[][] filled;

        private int lastX = -1;
        private int lastY = -1;

        private boolean filling = true;

        public Canvas() {
            addComponentListener(this);
        }

        @Override
        protected void drawComponent(Graphics g) {
            if (filled == null) {
                return;
            }

            Surface s = g.getSurface();
            for (int y = 0; y < getHeight(); y++) {
                for (int x = 0; x < getWidth(); x++) {
                    if (filled[y][x]) {
                        s.set(FILLED, x, y);
                    } else {
                        s.set(NOT_FILLED, x, y);
                    }
                }
            }
        }

        @Override
        protected void updateComponent() {
            if (getEngine().keyPressed(Key.E)) {
                lastX = -1;
                lastY = -1;
            }

            if (getEngine().hasMouseEvent()) {
                MouseEvent evt = getEngine().getLastMouseEvent();

                Point rel = relativePoint(evt.getX(), evt.getY());

                if (isInsideNotAbs(rel)) {
                    if (evt.getType() == MouseEvent.Type.Pressed) {
                        filled[rel.y][rel.x] = filling;
                        repaint();
                    } else if (evt.getType() == MouseEvent.Type.Dragged && lastX >= 0 && lastY >= 0) {
                        drawLine(lastX, lastY, rel.x, rel.y);
                        repaint();
                    }

                    lastX = rel.x;
                    lastY = rel.y;

                    if (evt.getType() == MouseEvent.Type.Released) {
                        lastX = -1;
                        lastY = -1;
                    }
                } else {
                    lastX = -1;
                    lastY = -1;
                }
            }
        }

        private void drawLine(int x0, int y0, int x1, int y1) {
            int dx = Math.abs(x1 - x0);
            int sx = x0 < x1 ? 1 : -1;
            int dy = -Math.abs(y1 - y0);
            int sy = y0 < y1 ? 1 : -1;
            int error = dx + dy;

            while (true) {
                filled[y0][x0] = filling;

                if (x0 == x1 && y0 == y1) {
                    break;
                }
                int e2 = 2 * error;

                if (e2 >= dy) {
                    if (x0 == x1) {
                        break;
                    }
                    error = error + dy;
                    x0 = x0 + sx;
                }

                if (e2 <= dx) {
                    if (y0 == y1) {
                        break;
                    }
                    error = error + dx;
                    y0 = y0 + sy;
                }
            }
        }

        @Override
        public void componentResized(fr.valax.sokoshell.graphics.Component comp) {
            if (filled == null) {
                filled = new boolean[getHeight()][getWidth()];
            } else {
                boolean[][] newFilled = new boolean[getHeight()][getWidth()];

                for (int y = 0; y < getHeight(); y++) {
                    newFilled[y] = new boolean[getWidth()];
                    System.arraycopy(filled[y], 0, newFilled[y], 0, Math.min(filled[y].length, getWidth()));
                }
            }
        }

        @Override
        public void componentMoved(Component comp) {

        }

        public boolean isPainting() {
            return filling;
        }

        public void setPainting(boolean filling) {
            this.filling = filling;
        }

        public void clear() {
            for (int y = 0; y < getHeight(); y++) {
                Arrays.fill(filled[y], false);
            }

            repaint();
        }
    }
}
