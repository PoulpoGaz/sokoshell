package fr.valax.sokoshell.commands.level;

import fr.valax.sokoshell.graphics.MapRenderer;
import fr.valax.sokoshell.graphics.TerminalEngine;
import fr.valax.sokoshell.solver.*;
import org.jline.keymap.KeyMap;
import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.utils.InfoCmp;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

/**
 * @author darth-mole
 */
public class PlayCommand extends LevelCommand {

    @Override
    protected int executeImpl(InputStream in, PrintStream out, PrintStream err) throws InvalidArgument {
        Level l = getLevel(pack, level);

        PlayCommand.GameController controller = new PlayCommand.GameController(l);

        try (PlayCommand.PlayView view = new PlayCommand.PlayView(helper.getTerminal(), l, controller)) {
            view.loop();
        }

        return SUCCESS;
    }

    @Override
    public String getName() { return "play"; }

    @Override
    public String getShortDescription() { return "Allows you to play the Sokoban game"; }

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

    public class PlayView extends TerminalEngine<Key> {

        private final Level level;
        private final GameController controller;

        public PlayView(Terminal terminal, Level level, GameController controller) {
            super(terminal);
            this.level = level;
            this.controller = controller;
        }

        @Override
        protected void init() {
            keyMap.bind(PlayCommand.Key.LEFT, KeyMap.key(terminal, InfoCmp.Capability.key_left));
            keyMap.bind(PlayCommand.Key.RIGHT, KeyMap.key(terminal, InfoCmp.Capability.key_right));
            keyMap.bind(PlayCommand.Key.DOWN, KeyMap.key(terminal, InfoCmp.Capability.key_down));
            keyMap.bind(PlayCommand.Key.UP, KeyMap.key(terminal, InfoCmp.Capability.key_up));
            keyMap.bind(PlayCommand.Key.ENTER, "\r");
            keyMap.bind(PlayCommand.Key.E, "e");
            keyMap.bind(PlayCommand.Key.ESCAPE, KeyMap.esc());
            keyMap.setAmbiguousTimeout(100L);
        }

        @Override
        protected void render(Size size) {
            surface.clear();

            int width = drawInfo(size.getColumns(), size.getRows());

            MapRenderer renderer = helper.getRenderer();
            Map map = controller.getMap();

            Direction lastMove = controller.getLastDir();
            if (lastMove == null) {
                lastMove = Direction.DOWN;
            }

            double yRatio = (double) size.getRows() / map.getHeight();
            double xRatio = (double) (size.getColumns() - width) / map.getWidth();

            int s = (int) Math.min(xRatio, yRatio);

            renderer.draw(graphics, 0, 0, s,
                    controller.getMap(), controller.getPlayerX(), controller.getPlayerY(), lastMove);

            surface.drawBuffer(display, 0);
        }

        private int drawInfo(int width, int height) {
            String moves = "Moves: " + controller.getMoveCount();
            surface.draw(moves, width - moves.length(), 0);

            String pushes = "Pushes: " + controller.getPushCount();
            surface.draw(pushes, width - pushes.length(), 1);

            String fps = "FPS: " + getFPS();
            surface.draw(fps, width - fps.length(), 4);

            String tps = "TPS: " + getTPS();
            surface.draw(tps, width - tps.length(), 5);

            return Math.max(moves.length(), pushes.length());
        }

        @Override
        protected void update() {
            if (pressed(PlayCommand.Key.ESCAPE) || pressed(PlayCommand.Key.ENTER)) {
                running = false;
            } else if (pressed(PlayCommand.Key.LEFT)) {
                controller.move(Direction.LEFT);
            } else if (pressed(PlayCommand.Key.RIGHT)) {
                controller.move(Direction.RIGHT);
            } else if (pressed(PlayCommand.Key.UP)) {
                controller.move(Direction.UP);
            } else if (pressed(PlayCommand.Key.DOWN)) {
                controller.move(Direction.DOWN);
            } else if (justPressed(PlayCommand.Key.E)) {
                Direction lastMove = controller.getLastDir();
                if (lastMove == null) {
                    lastMove = Direction.DOWN;
                }

                try {
                    helper.exportPNG(level.getPack(), level, controller.getMap(),
                            controller.getPlayerX(), controller.getPlayerY(), lastMove);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public class GameController {
        private Map map;
        private int playerX;
        private int playerY;
        private int moves;
        private int push;

        private Direction lastDir;

        private boolean mapCompleted = false;

        GameController(Level level) {
            this.map = level.getMap();
            this.playerX = level.getPlayerX();
            this.playerY = level.getPlayerY();
        }

        public void move(Direction dir) {
            final int nextX = playerX + dir.dirX();
            final int nextY = playerY + dir.dirY();
            lastDir = dir;

            if (map.caseExists(nextX, nextY) && map.isTileEmpty(nextX, nextY)) {
                movePlayer(nextX, nextY);
            } else {
                final Tile next = map.getAt(nextX, nextY).getTile();
                if (next == Tile.CRATE || next == Tile.CRATE_ON_TARGET) {
                    final int nextNextX = nextX + dir.dirX();
                    final int nextNextY = nextY + dir.dirY();
                    if (map.caseExists(nextNextX, nextNextY) && map.isTileEmpty(nextNextX, nextNextY)) {
                        movePlayer(nextX, nextY);
                        moveCrate(nextX, nextY, nextNextX, nextNextY);
                        if (map.isCompleted()) {
                            mapCompleted = true;
                        }
                    }
                }
            }
        }

        /**
         * Moves the player to the given coordinates. The move MUST be valid (no check preformed).
         * @param x x-coordinate
         * @param y y-coordinate
         */
        private void movePlayer(int x, int y) {
            playerX = x;
            playerY = y;
            moves++;
        }

        /**
         * Moves a crate from (x,y) to (nextX, nextY). The move MUST be valid (no check preformed).
         * @param x x-coordinate
         * @param y y-coordinate
         * @param nextX new x-coordinate
         * @param nextY new y-coordinate
         */
        private void moveCrate(int x, int y, int nextX, int nextY) {

            Tile curr = map.getAt(x, y).getTile();
            Tile next = map.getAt(nextX, nextY).getTile();

            switch (curr) {
                case CRATE -> map.setAt(x, y, Tile.FLOOR);
                case CRATE_ON_TARGET -> map.setAt(x, y, Tile.TARGET);
            }

            if (curr.isCrate()) {
                push++;

                switch (next) {
                    case FLOOR -> map.setAt(nextX, nextY, Tile.CRATE);
                    case TARGET -> map.setAt(nextX, nextY, Tile.CRATE_ON_TARGET);
                }
            }
        }

        public Map getMap() { return map; }

        public int getPlayerX() { return playerX; }
        public int getPlayerY() { return playerY; }

        public Direction getLastDir() { return lastDir; }

        public int getPushCount() { return push; }
        public int getMoveCount() { return moves; }

        public boolean isMapCompleted() { return mapCompleted; }
    }
}
