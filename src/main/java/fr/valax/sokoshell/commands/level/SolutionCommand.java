package fr.valax.sokoshell.commands.level;

import fr.valax.args.api.Command;
import fr.valax.sokoshell.commands.AbstractCommand;
import fr.valax.sokoshell.graphics.MapRenderer;
import fr.valax.sokoshell.graphics.TerminalEngine;
import fr.valax.sokoshell.solver.Map;
import fr.valax.sokoshell.solver.*;
import fr.valax.sokoshell.utils.Utils;
import org.jline.keymap.KeyMap;
import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.utils.InfoCmp;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.*;

public class SolutionCommand extends LevelCommand {

    @Override
    protected int executeImpl(InputStream in, PrintStream out, PrintStream err) {
        Level l;
        try {
            Pack pack = getPack(name);
            l = getLevel(pack, index);

        } catch (AbstractCommand.InvalidArgument e) {
            e.print(err, true);
            return Command.FAILURE;
        }

        if (l.getLastSolution() == null) {
            err.println("Not solved");
            return Command.FAILURE;
        }

        SolutionAnimator animator = new SolutionAnimator(l.getLastSolution());

        try (SolutionView view = new SolutionView(helper.getTerminal(), animator)) {
            view.loop();
        }

        return Command.SUCCESS;
    }

    @Override
    public String getName() {
        return "solution";
    }

    @Override
    public String getShortDescription() {
        return "Show a solution";
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
        SPACE
    }

    public class SolutionView extends TerminalEngine<Key> {

        private final SolutionAnimator animator;

        private boolean paused = false;

        private long lastTime;

        // a value between 1 and 40
        private int speed = 20;

        public SolutionView(Terminal terminal, SolutionAnimator animator) {
            super(terminal);
            this.animator = animator;
        }

        @Override
        protected void init() {
            keyMap.bind(Key.LEFT, KeyMap.key(terminal, InfoCmp.Capability.key_left));
            keyMap.bind(Key.RIGHT, KeyMap.key(terminal, InfoCmp.Capability.key_right));
            keyMap.bind(Key.DOWN, KeyMap.key(terminal, InfoCmp.Capability.key_down));
            keyMap.bind(Key.UP, KeyMap.key(terminal, InfoCmp.Capability.key_up));
            keyMap.bind(Key.ENTER, "\r");
            keyMap.bind(Key.SPACE, " ");
            keyMap.bind(Key.ESCAPE, KeyMap.esc());
            keyMap.setAmbiguousTimeout(100L);
        }

        @Override
        protected void render(Size size) {
            surface.clear();

            int width = drawInfo(size.getColumns(), size.getRows());

            MapRenderer renderer = helper.getRenderer();
            Map map = animator.getMap();

            Direction lastMove = animator.getLastMove();
            if (lastMove == null) {
                lastMove = Direction.DOWN;
            }

            renderer.draw(graphics, 0, 0, size.getColumns() - width, size.getRows(),
                    map, animator.getPlayerX(), animator.getPlayerY(), lastMove);

            surface.drawBuffer(display, 0);
        }

        private int drawInfo(int width, int height) {
            int totalMoveLength = Utils.nDigit(animator.numberOfMoves());
            int totalPushLength = Utils.nDigit(animator.numberOfMoves());

            int moveLength = Utils.nDigit(animator.getMoveCount());
            int pushLength = Utils.nDigit(animator.getPushCount());

            int speedLength = Utils.nDigit(speed);

            int moveInfoLength = 8 + 2 * totalMoveLength;
            int pushInfoLength = 9 + 2 * totalPushLength;
            int speedInfoLength = 7 + speedLength;

            surface.draw("Moves:", width - moveInfoLength, 0);
            surface.draw("%d/%d".formatted(animator.getMoveCount(), animator.numberOfMoves()), width - totalMoveLength - moveLength - 1, 0);

            surface.draw("Pushes:", width - pushInfoLength, 1);
            surface.draw("%d/%d".formatted(animator.getPushCount(), animator.numberOfPushes()), width - totalPushLength - pushLength - 1, 1);

            surface.draw("Speed:", width - 7 - speedLength, 2);
            surface.draw(String.valueOf(speed), width - speedLength, 2);

            String fps = "FPS: " + getFPS();
            surface.draw(fps, width - fps.length(), 4);

            String tps = "TPS: " + getTPS();
            surface.draw(tps, width - tps.length(), 5);

            return Math.max(moveInfoLength, Math.max(pushInfoLength, speedInfoLength));
        }

        /**
         * f(x) = e^(ax²+bx+c)
         * f(1) = 5000
         * f(20) = 100
         * f(40) = 1000 / View.TPS
         */
        private int speedToMillis() {
            double a = 0.0011166751;
            double b = -0.1920345101;
            double c = 8.708111026;

            return (int) Math.exp(
                a * speed * speed + b * speed + c
            );
        }

        @Override
        protected void update() {
            if (lastTime + speedToMillis() < System.currentTimeMillis()) {
                if (animator.hasNext() && !paused) {
                    animator.move();

                    lastTime = System.currentTimeMillis();

                    if (!animator.hasNext()) {
                        paused = true;
                    }
                }
            }

            if (pressed(Key.ESCAPE)) {
                running = false;
            } else if (pressed(Key.ENTER) && !animator.hasNext()) {
                running = false;
            } else if (pressed(Key.SPACE)) {
                paused = !paused;
                lastTime = 0;
            } else if (pressed(Key.LEFT) && paused) {

                if (animator.hasPrevious()) {
                    animator.moveBackward();
                }

            } else if (pressed(Key.RIGHT) && paused) {

                if (animator.hasNext()) {
                    animator.move();
                }
            } else if (pressed(Key.UP)) {
                if (speed < 40) {
                    speed++;
                }

            } else if (pressed(Key.DOWN)) {
                if (speed > 1) {
                    speed--;
                }
            }
        }
    }

    public static class SolutionAnimator {

        private final Solution solution;
        private final Map map;

        private final List<Move> path;
        private int pathIndex;

        private int playerX;
        private int playerY;

        private int move;
        private int push;

        public SolutionAnimator(Solution solution) {
            this.solution = solution;
            Level level = solution.getParameters().getLevel();

            this.map = level.getMap();
            this.playerX = level.getPlayerX();
            this.playerY = level.getPlayerY();

            path = solution.getFullSolution();
        }

        public void move() {
            if (!hasNext()) {
                return;
            }

            Direction dir = path.get(pathIndex).direction();

            playerX += dir.dirX();
            playerY += dir.dirY();
            move++;

            moveCrate(playerX, playerY, dir);

            pathIndex++;
        }

        // move crate if needed
        private void moveCrate(int x, int y, Direction dir) {
            int newX = x + dir.dirX();
            int newY = y + dir.dirY();

            Tile curr = map.getAt(x, y).getTile();
            Tile next = map.getAt(newX, newY).getTile();

            switch (curr) {
                case CRATE -> map.setAt(x, y, Tile.FLOOR);
                case CRATE_ON_TARGET -> map.setAt(x, y, Tile.TARGET);
            }

            if (curr.isCrate()) {
                push++;

                switch (next) {
                    case FLOOR -> map.setAt(newX, newY, Tile.CRATE);
                    case TARGET -> map.setAt(newX, newY, Tile.CRATE_ON_TARGET);
                }
            }
        }

        public boolean hasNext() {
            return pathIndex < path.size();
        }

        public void moveBackward() {
            if (!hasPrevious()) {
                return;
            }

            pathIndex--;
            Move move = path.get(pathIndex);

            Direction dir = move.direction();

            if (move.moveCrate()) {
                int crateX = playerX + dir.dirX();
                int crateY = playerY + dir.dirY();

                Tile crate = map.getAt(crateX, crateY).getTile();

                switch (crate) {
                    case CRATE -> map.setAt(crateX, crateY, Tile.FLOOR);
                    case CRATE_ON_TARGET -> map.setAt(crateX, crateY, Tile.TARGET);
                }

                switch (map.getAt(playerX, playerY).getTile()) {
                    case FLOOR -> map.setAt(playerX, playerY, Tile.CRATE);
                    case TARGET -> map.setAt(playerX, playerY, Tile.CRATE_ON_TARGET);
                }

                push--;
            }

            this.move--;
            playerX -= dir.dirX();
            playerY -= dir.dirY();
        }

        public boolean hasPrevious() {
            return pathIndex > 0;
        }

        public Map getMap() {
            return map;
        }

        public int getPlayerX() {
            return playerX;
        }

        public int getPlayerY() {
            return playerY;
        }

        public int getMoveCount() {
            return move;
        }

        public int getPushCount() {
            return push;
        }

        public int numberOfMoves() {
            return solution.numberOfMoves();
        }

        public int numberOfPushes() {
            return solution.numberOfPushes();
        }

        public Direction getLastMove() {
            if (pathIndex == 0) {
                return null;
            }

            return path.get(pathIndex - 1).direction();
        }
    }
}