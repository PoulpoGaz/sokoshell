package fr.valax.sokoshell.commands;

import fr.valax.sokoshell.SokoShellHelper;
import fr.valax.sokoshell.SolverTask;
import fr.valax.sokoshell.graphics.MapRenderer;
import fr.valax.sokoshell.graphics.TerminalEngine;
import fr.valax.sokoshell.solver.*;
import fr.valax.sokoshell.utils.Utils;
import org.jline.keymap.KeyMap;
import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.utils.InfoCmp;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

public class MonitorCommand extends AbstractCommand {

    @Override
    protected int executeImpl(InputStream in, PrintStream out, PrintStream err) {
        SolverTask runningTask = helper.getTaskList().getRunningTask();

        if (runningTask == null) {
            err.println("No task are running");
            return FAILURE;
        }


        Exception ex = null;
        State last = null;
        try (Monitor monitor = new Monitor(helper.getTerminal(), helper, runningTask)) {
            try {
                monitor.loop();
            } catch (Exception e) { // due to the voluntary lack of synchronization, actually never happen
                ex = e;
                last = monitor.state;
            }
        }

        if (ex != null) {
            ex.printStackTrace(err);
            err.println(last);
        }

        return 0;
    }

    @Override
    public String getName() {
        return "monitor";
    }

    @Override
    public String getShortDescription() {
        return "monitor";
    }

    @Override
    public String[] getUsage() {
        return new String[0];
    }

    private enum Key {

        ESCAPE,
        LEFT,
        RIGHT,
        DOWN,
        UP,
        ENTER,
        E
    }

    private static class Monitor extends TerminalEngine<Key> {

        private final SokoShellHelper helper;
        private final SolverTask task;
        private final List<Level> levels;
        private final Solver solver;
        private final Trackable trackable;

        private Pack currentPack;
        private Level currentLevel;
        private int index;

        private Map map;
        private BigInteger numberOfStates;
        private State state;

        public Monitor(Terminal terminal, SokoShellHelper helper, SolverTask task) {
            super(terminal);
            this.helper = helper;
            this.task = task;
            this.levels = task.getLevels();
            this.solver = task.getSolver();

            if (solver instanceof Trackable tr) {
                this.trackable = tr;
            } else {
                this.trackable = null;
            }

            changeLevel();
        }

        @Override
        protected void init() {
            keyMap.bind(Key.LEFT, KeyMap.key(terminal, InfoCmp.Capability.key_left));
            keyMap.bind(Key.RIGHT, KeyMap.key(terminal, InfoCmp.Capability.key_right));
            keyMap.bind(Key.DOWN, KeyMap.key(terminal, InfoCmp.Capability.key_down));
            keyMap.bind(Key.UP, KeyMap.key(terminal, InfoCmp.Capability.key_up));
            keyMap.bind(Key.ENTER, "\r");
            keyMap.bind(Key.E, "e");
            keyMap.bind(Key.ESCAPE, KeyMap.esc());
            keyMap.setAmbiguousTimeout(100L);
        }

        protected int render(Size size) {
            surface.clear();

            drawHeaderAndFooter(size);
            if (map != null) {
                drawMap(size);
            }

            surface.drawBuffer(display, 0);

            return 0;
        }

        private void drawMap(Size size) {
            if (size.getRows() <= 3) {
                return;
            }

            int width = size.getColumns();
            int height = size.getRows() - 3 - 2;
            int playerX = -1;
            int playerY = -1;

            MapRenderer renderer = helper.getRenderer();
            if (state != null) {
                map.addStateCrates(state);
                playerX = map.getX(state.playerPos());
                playerY = map.getY(state.playerPos());
            }

            renderer.draw(graphics, 0, 2, width, height, map, playerX, playerY, Direction.DOWN);
            if (state != null) {
                map.removeStateCrates(state);
            }
        }

        private void drawHeaderAndFooter(Size size) {
            // header
            drawRegularlyKeyMap(0, size.getColumns(),
                    "Task id", "#" + task.getTaskIndex(),
                    "Request pack", task.getPack(),
                    "Request level", task.getLevel(),
                    "Progress", task.getCurrentLevel() + "/" + task.getLevels().size());

            if (trackable != null) {
                long end = trackable.timeEnded();
                if (end < 0) {
                    end = System.currentTimeMillis();
                }

                drawRegularlyKeyMap(1, size.getColumns(),
                        "Running for", Utils.prettyDate(end - trackable.timeStarted()),
                        "State explored", trackable.nStateExplored(),
                        "Queue size", trackable.currentQueueSize(),
                        "", "");
            } else {
                long end = task.getFinishedAt();
                if (end < 0) {
                    end = System.currentTimeMillis();
                }

                drawRegularlyKeyMap(1, size.getColumns(),
                        "Running for (whole task)", Utils.prettyDate(end - task.getStartedAt()));
            }


            // footer
            if (currentPack != null && currentLevel != null) {
                drawRegularlyKeyMap(size.getRows() - 2, size.getColumns(),
                        "Pack", currentPack.name(),
                        "Level", currentLevel.getIndex() + 1);

                drawRegularlyKeyMap(size.getRows() - 1, size.getColumns(),
                        "Max number of states", numberOfStates);
            }
        }

        private void drawRegularlyKeyMap(int y, int width, Object... objects) {
            if (objects.length % 2 != 0) {
                throw new IllegalArgumentException();
            }

            int subWidth = 2 * width / objects.length;

            int x = 0;
            for (int i = 0, objectsLength = objects.length; i < objectsLength; i += 2, x += subWidth) {
                Object k = objects[i];
                Object v = objects[i + 1];

                if (k == null && v == null) {
                    continue;
                }

                String key = Objects.toString(k);
                String value = Objects.toString(v);

                if (key.isEmpty() && value.isEmpty()) {
                    continue;
                }

                int w = key.length() + value.length();

                if (w >= subWidth) {
                    if (value.length() >= subWidth) {
                        surface.draw(value.substring(0, subWidth), x, y);
                    } else {
                        String key2 = key.substring(0, subWidth - value.length());
                        surface.draw(key2, x, y);
                        surface.draw(value, x + key2.length(), y);
                    }

                } else if (w + 1 >= subWidth) {
                    surface.draw(key, x, y);
                    surface.draw(value, x + 1 + key.length(), y);
                } else if (w + 2 >= subWidth) {
                    surface.draw(key, x, y);
                    surface.draw(":", x + 1 + key.length(), y);
                    surface.draw(value, x + 2 + key.length(), y);
                } else {
                    surface.draw(key, x, y);
                    surface.draw(":", x + 1 + key.length(), y);
                    surface.draw(value, x + 3 + key.length(), y);
                }
            }
        }

        private void drawCentered(String str, int y, int width) {
            surface.draw(str, (width - str.length()) / 2, y);
        }

        protected void update() {
            if (pressed(Key.ESCAPE)) {
                running = false;
            }

            if (task.getCurrentLevel() != index) {
                changeLevel();
            }

            if (trackable != null) {
                state = trackable.currentState();
            }

            if (justPressed(Key.E)) {
                int playerX = -1;
                int playerY = -1;
                if (state != null) {
                    map.addStateCrates(state);
                    playerX = map.getX(state.playerPos());
                    playerY = map.getY(state.playerPos());
                }

                try {
                    helper.exportPNG(currentPack, currentLevel, map, playerX, playerY, Direction.DOWN, 16);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                if (state != null) {
                    map.removeStateCrates(state);
                }
            }
        }

        private void changeLevel() {
            index = task.getCurrentLevel();

            if (index >= 0 && index < levels.size()) {
                currentLevel = levels.get(index);
                currentPack = currentLevel.getPack();

                map = currentLevel.getMap();
                numberOfStates = estimateMaxNumberOfStates(map);
                map.forEach((t) -> {
                    if (t.isCrate()) {
                        t.setTile(Tile.FLOOR);
                    } else if (t.isCrateOnTarget()) {
                        t.setTile(Tile.TARGET);
                    }
                });
            }
        }

        /**
         * let c the number of crate<br>
         * let f the number of floor<br>
         * <br>
         * An upper bounds of the number of states is:<br>
         * (f (c + 1))     where (n k) is n choose k<br>
         * <br>
         * (f c) counts the number of way to organize the crate (c) and the player ( + 1)<br>
         */
        private BigInteger estimateMaxNumberOfStates(Map map) {
            int nCrate = 0;
            int nFloor = 0;

            for (int y = 0; y < map.getHeight(); y++) {
                for (int x = 0; x < map.getWidth(); x++) {

                    if (map.getAt(x, y).anyCrate()) {
                        nCrate++;
                        nFloor++;
                    } else if (!map.getAt(x, y).isSolid()) {
                        nFloor++;
                    }
                }
            }

            Tuple t = factorial(nFloor, nCrate + 1, nFloor - nCrate - 1);

            return t.a()
                    .divide(t.b().multiply(t.c()));
        }


        private Tuple factorial(int nA, int nB, int nC) {
            int max = Math.max(nA, Math.max(nB, nC));

            BigInteger a = nA == 0 ? BigInteger.ZERO : null;
            BigInteger b = nB == 0 ? BigInteger.ZERO : null;
            BigInteger c = nC == 0 ? BigInteger.ZERO : null;

            BigInteger fac = BigInteger.ONE;
            for (int k = 1; k <= max; k++) {

                fac = fac.multiply(BigInteger.valueOf(k));

                if (k == nA) {
                    a = fac;
                }
                if (k == nB) {
                    b = fac;
                }
                if (k == nC) {
                    c = fac;
                }
            }

            return new Tuple(a, b, c);
        }
    }


    private record Tuple(BigInteger a, BigInteger b, BigInteger c) {}
}
