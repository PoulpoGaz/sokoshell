package fr.valax.sokoshell.commands.level;

import fr.valax.sokoshell.SokoShellHelper;
import fr.valax.sokoshell.graphics.*;
import fr.valax.sokoshell.graphics.layout.*;
import fr.valax.sokoshell.solver.Board;
import fr.valax.sokoshell.solver.Direction;
import fr.valax.sokoshell.solver.Level;
import fr.valax.sokoshell.solver.Tile;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Path;

/**
 * @author darth-mole
 */
public class PlayCommand extends LevelCommand {

    @Override
    protected int executeImpl(InputStream in, PrintStream out, PrintStream err) throws InvalidArgument {
        Level l = getLevel(pack, level);

        PlayCommand.GameController controller = new PlayCommand.GameController(l);

        try (TerminalEngine engine = new TerminalEngine(helper.getTerminal())) {
            Key.LEFT.bind(engine);
            Key.RIGHT.bind(engine);
            Key.DOWN.bind(engine);
            Key.UP.bind(engine);
            Key.ENTER.bind(engine);
            Key.CTRL_E.bind(engine);
            Key.ESCAPE.bind(engine);
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
            updateComponents();


            Component innerTop = new Component();
            innerTop.setLayout(new GridLayout());
            GridLayoutConstraints glc = new GridLayoutConstraints();
            glc.weightX = glc.weightY = 1;
            glc.fill = GridLayoutConstraints.BOTH;
            glc.x = glc.y = 0;
            innerTop.add(NamedComponent.create("Moves:", movesLabel), glc);
            glc.x++;
            innerTop.add(NamedComponent.create("Pushes:", pushesLabel), glc);

            Component innerCenter = new Component();
            innerCenter.setLayout(new HorizontalLayout());
            HorizontalConstraint hc = new HorizontalConstraint();
            hc.fillYAxis = true;
            hc.endComponent = true;
            innerCenter.add(new ExportComponent(this::export), hc);
            hc.endComponent = false;
            hc.orientation = HorizontalLayout.Orientation.RIGHT;
            innerCenter.add(new MemoryBar(), hc);


            Component bot = new Component();
            bot.setBorder(new BasicBorder(true, false, false, false));
            bot.setLayout(new BorderLayout());
            bot.add(innerTop, BorderLayout.NORTH);
            bot.add(innerCenter, BorderLayout.CENTER);

            setLayout(new BorderLayout());
            add(bot, BorderLayout.SOUTH);
            add(mapComponent, BorderLayout.CENTER);
        }

        private String export() {
            if (mapComponent.getMap() == null) {
                return null;
            }

            try {
                Path out = SokoShellHelper.INSTANCE
                        .exportPNG(level.getPack(), level,
                                controller.getMap(), controller.getPlayerX(), controller.getPlayerY(),
                                controller.getLastDir(), 16);

                return out.toString();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected void updateComponent() {
            if (keyPressed(Key.ESCAPE) || keyPressed(Key.ENTER)) {
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
        private final Board board;
        private int playerX;
        private int playerY;
        private int moves;
        private int push;

        private Direction lastDir;

        private boolean mapCompleted = false;

        GameController(Level level) {
            this.board = level.getMap();
            this.playerX = level.getPlayerX();
            this.playerY = level.getPlayerY();
        }

        public void move(Direction dir) {
            final int nextX = playerX + dir.dirX();
            final int nextY = playerY + dir.dirY();
            lastDir = dir;

            if (board.caseExists(nextX, nextY) && board.isTileEmpty(nextX, nextY)) {
                movePlayer(nextX, nextY);
            } else {
                final Tile next = board.getAt(nextX, nextY).getTile();
                if (next == Tile.CRATE || next == Tile.CRATE_ON_TARGET) {
                    final int nextNextX = nextX + dir.dirX();
                    final int nextNextY = nextY + dir.dirY();
                    if (board.caseExists(nextNextX, nextNextY) && board.isTileEmpty(nextNextX, nextNextY)) {
                        movePlayer(nextX, nextY);
                        moveCrate(nextX, nextY, nextNextX, nextNextY);
                        if (board.isCompleted()) {
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

            Tile curr = board.getAt(x, y).getTile();
            Tile next = board.getAt(nextX, nextY).getTile();

            switch (curr) {
                case CRATE -> board.setAt(x, y, Tile.FLOOR);
                case CRATE_ON_TARGET -> board.setAt(x, y, Tile.TARGET);
            }

            if (curr.isCrate()) {
                push++;

                switch (next) {
                    case FLOOR -> board.setAt(nextX, nextY, Tile.CRATE);
                    case TARGET -> board.setAt(nextX, nextY, Tile.CRATE_ON_TARGET);
                }
            }
        }

        public Board getMap() { return board; }

        public int getPlayerX() { return playerX; }
        public int getPlayerY() { return playerY; }

        public Direction getLastDir() { return lastDir; }

        public int getPushCount() { return push; }
        public int getMoveCount() { return moves; }

        public boolean isMapCompleted() { return mapCompleted; }
    }
}
