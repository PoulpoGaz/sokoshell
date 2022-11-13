package fr.valax.sokoshell.commands.level;

import fr.valax.sokoshell.graphics.*;
import fr.valax.sokoshell.graphics.layout.BorderLayout;
import fr.valax.sokoshell.graphics.layout.GridLayout;
import fr.valax.sokoshell.graphics.layout.GridLayoutConstraints;
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

        try (TerminalEngine engine = new TerminalEngine(helper.getTerminal())) {
            Key.LEFT.addTo(engine);
            Key.RIGHT.addTo(engine);
            Key.DOWN.addTo(engine);
            Key.UP.addTo(engine);
            Key.ENTER.addTo(engine);
            Key.E.addTo(engine);
            Key.ESCAPE.addTo(engine);
            engine.setRootComponent(new PlayComponent(l, controller));
            engine.show();
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

    public class PlayComponent extends Component {

        private final Level level;
        private final GameController controller;

        private Label movesLabel;
        private Label pushesLabel;

        private MapComponent mapComponent;

        public PlayComponent(Level level, GameController controller) {
            this.level = level;
            this.controller = controller;

            initComponent();
        }

        private void initComponent() {
            movesLabel = new Label();
            movesLabel.setHorizAlign(Label.WEST);
            pushesLabel = new Label();
            pushesLabel.setHorizAlign(Label.WEST);

            mapComponent = new MapComponent();
            mapComponent.setMap(controller.getMap());
            mapComponent.setPlayerX(controller.getPlayerX());
            mapComponent.setPlayerY(controller.getPlayerY());

            updateComponents();


            Component bot = new Component();
            bot.setBorder(new BasicBorder(true, false, false, false));
            bot.setLayout(new GridLayout());

            GridLayoutConstraints c = new GridLayoutConstraints();
            c.x = c.y = 0;
            c.weightX = c.weightY = 1;
            c.fill = GridLayoutConstraints.BOTH;
            bot.add(NamedComponent.create("Moves:", movesLabel), c);
            c.x++;
            bot.add(NamedComponent.create("Pushes:", pushesLabel), c);
            c.x++;
            c.weightX = c.weightY = 0;
            bot.add(new MemoryBar(), c);

            setLayout(new BorderLayout());
            add(bot, BorderLayout.SOUTH);
            add(mapComponent, BorderLayout.CENTER);
        }

        @Override
        protected void updateComponent() {
            if (keyReleased(Key.ESCAPE) || keyReleased(Key.ENTER)) {
                getEngine().stop();
            } else if (keyPressed(Key.LEFT)) {
                controller.move(Direction.LEFT);
                updateComponents();
            } else if (keyPressed(Key.RIGHT)) {
                controller.move(Direction.RIGHT);
                updateComponents();
            } else if (keyPressed(Key.UP)) {
                controller.move(Direction.UP);
                updateComponents();
            } else if (keyPressed(Key.DOWN)) {
                controller.move(Direction.DOWN);
                updateComponents();
            } /*else if (keyPressed(Key.E)) {
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
            }*/
        }

        private void updateComponents() {
            movesLabel.setText(Integer.toString(controller.getMoveCount()));
            pushesLabel.setText(Integer.toString(controller.getPushCount()));

            mapComponent.setPlayerX(controller.getPlayerX());
            mapComponent.setPlayerY(controller.getPlayerY());
            mapComponent.setPlayerDir(controller.getLastDir());
            mapComponent.repaint();
        }
    }

    public static class GameController {
        private final Map map;
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
