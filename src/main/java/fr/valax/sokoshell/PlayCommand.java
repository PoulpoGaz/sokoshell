package fr.valax.sokoshell;

import fr.valax.args.api.Option;
import fr.valax.args.utils.ArgsUtils;
import fr.valax.sokoshell.graphics.MapRenderer;
import fr.valax.sokoshell.graphics.View;
import fr.valax.sokoshell.solver.*;
import fr.valax.sokoshell.utils.Utils;
import org.jline.keymap.KeyMap;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.InfoCmp;

import java.util.List;

public class PlayCommand extends AbstractVoidCommand {

    @Option(names = {"p", "-pack"}, hasArgument = true, argName = "Pack name", optional = false)
    private String name;

    @Option(names = {"i", "-index"}, hasArgument = true, argName = "Level index", optional = false)
    private int index;

    public void run() {
        Pack pack = helper.getPack(name);

        if (pack == null) {
            System.out.printf("No pack named %s exists%n", name);
            return;
        }

        index--;
        if (index < 0 || index >= pack.levels().size()) {
            System.out.println("Index out of bounds");
            return;
        }

        Level l = pack.levels().get(index);
        PlayCommand.GameController controller = new PlayCommand.GameController(l);

        try (PlayCommand.PlayView view = new PlayCommand.PlayView(helper.getTerminal(), controller)) {
            view.loop();
        }
    }

    @Override
    public String getName() { return "play"; }

    @Override
    public String getUsage() { return "Allows you to play the Sokoban game"; }

    @Override
    public void completeOption(LineReader reader, ParsedLine line, List<Candidate> candidates, Option option) {
        if (ArgsUtils.contains(option.names(), "p")) {
            helper.addPackCandidates(candidates);
        }
    }

    private enum Key {

        ESCAPE,
        LEFT,
        RIGHT,
        DOWN,
        UP,
        ENTER
    }

    public class PlayView extends View<Key> {

        private GameController controller;

        public PlayView(Terminal terminal, GameController controller) {
            super(terminal);
            this.controller = controller;
        }

        @Override
        protected void init() {
            keyMap.bind(PlayCommand.Key.LEFT, KeyMap.key(terminal, InfoCmp.Capability.key_left));
            keyMap.bind(PlayCommand.Key.RIGHT, KeyMap.key(terminal, InfoCmp.Capability.key_right));
            keyMap.bind(PlayCommand.Key.DOWN, KeyMap.key(terminal, InfoCmp.Capability.key_down));
            keyMap.bind(PlayCommand.Key.UP, KeyMap.key(terminal, InfoCmp.Capability.key_up));
            keyMap.bind(PlayCommand.Key.ENTER, "\r");
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
            }
        }
    }

    public class GameController {
        MutableMap map;
        private int playerX;
        private int playerY;
        int moves;
        int push;

        Direction lastDir;

        private boolean mapCompleted = false;

        GameController(Level level) {
            this.map = new MutableMap(level.getMap());
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
                final Tile next = map.getAt(nextX, nextY);
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

            Tile curr = map.getAt(x, y);
            Tile next = map.getAt(nextX, nextY);

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

        public MutableMap getMap() { return map; }

        public int getPlayerX() { return playerX; }
        public int getPlayerY() { return playerY; }

        public Direction getLastDir() { return lastDir; }

        public int getPushCount() { return push; }
        public int getMoveCount() { return moves; }

        public boolean isMapCompleted() { return mapCompleted; }
    }
}
