package fr.valax.sokoshell.commands.level;

import fr.valax.args.api.Option;
import fr.valax.sokoshell.SokoShell;
import fr.valax.sokoshell.graphics.*;
import fr.valax.sokoshell.graphics.layout.*;
import fr.valax.sokoshell.solver.Level;
import fr.valax.sokoshell.solver.SolverReport;
import fr.valax.sokoshell.solver.board.Board;
import fr.valax.sokoshell.solver.board.Direction;
import fr.valax.sokoshell.solver.board.Move;
import fr.valax.sokoshell.solver.board.MutableBoard;
import fr.valax.sokoshell.solver.board.tiles.Tile;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.List;

public class SolutionCommand extends LevelCommand {

    @Option(names = {"s", "solution"}, hasArgument = true, argName = "Solution index")
    private Integer solution;

    @Option(names = {"n", "no-animation"})
    private boolean noAnimation;

    @Override
    protected int executeImpl(InputStream in, PrintStream out, PrintStream err) throws InvalidArgument {
        Level l = getLevel(pack, level);

        if (l.getLastSolution() == null) {
            err.println("Not solved");
            return FAILURE;
        }

        SolverReport s;
        if (solution != null) {
            s = l.getSolverReport(solution);

            if (s == null) {
                err.println("Index out of bounds");
                return FAILURE;
            }
        } else {
            s = l.getLastSolution();
        }

        if (!noAnimation) {
            showAnimator(s);
        } else {
            if (!sokoshell().isPromptEnabled()) {
                err.println("Animator not available when prompt is disabled");
                return FAILURE;
            }

            List<Move> moves = s.getFullSolution();

            for (Move m : moves) {
                switch (m.direction()) {
                    case RIGHT -> out.append('r');
                    case LEFT -> out.append('l');
                    case DOWN -> out.append('d');
                    case UP -> out.append('u');
                }
            }

            out.append(System.lineSeparator());
        }

        return SUCCESS;
    }

    private void showAnimator(SolverReport report) {
        SolutionAnimator animator = new SolutionAnimator(report);

        try (TerminalEngine engine = new TerminalEngine(sokoshell().getTerminal())) {
            Key.LEFT.bind(engine);
            Key.RIGHT.bind(engine);
            Key.DOWN.bind(engine);
            Key.UP.bind(engine);
            Key.ENTER.bind(engine);
            Key.SPACE.bind(engine);
            Key.R.bind(engine);
            Key.CTRL_E.bind(engine);
            Key.ESCAPE.bind(engine);
            engine.setRootComponent(new SolutionComponent(animator));

            engine.show();
        }
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

    public static class SolutionComponent extends Component {

        private final SolutionAnimator animator;

        private boolean paused = false;

        private long lastTime;

        // a value between 1 and 60
        private int speed = 20;


        private Label movesLabel;
        private Label pushesLabel;
        private Label speedLabel;

        private BoardComponent boardComponent;

        public SolutionComponent(SolutionAnimator animator) {
            this.animator = animator;
            lastTime = System.currentTimeMillis();

            initComponent();
        }

        private void initComponent() {
            movesLabel = new Label();
            movesLabel.setHorizAlign(Label.WEST);
            pushesLabel = new Label();
            pushesLabel.setHorizAlign(Label.WEST);
            speedLabel = new Label();
            speedLabel.setHorizAlign(Label.WEST);

            boardComponent = new BoardComponent();
            boardComponent.setBoard(animator.getBoard());
            boardComponent.setPlayerX(animator.getPlayerX());
            boardComponent.setPlayerY(animator.getPlayerY());

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
            glc.x++;
            innerTop.add(NamedComponent.create("Speed:", speedLabel), glc);

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
            add(boardComponent, BorderLayout.CENTER);
        }

        private String export() {
            if (boardComponent.getBoard() == null) {
                return null;
            }

            try {
                Level l = animator.getSolution().getLevel();

                Path out = SokoShell.INSTANCE
                        .exportPNG(l.getPack(), l,
                                animator.getBoard(), animator.getPlayerX(), animator.getPlayerY(),
                                animator.getLastMove());

                return out.toString();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private int speedToMillis() {
            double a = -0.000072192;
            double b = 0.0073860;
            double c = -0.33061;
            double d = 8.8405;

            double speedSquare = speed * speed;
            return (int) Math.exp(
                a * speed * speedSquare  + b * speedSquare + c * speed + d
            );
        }

        public void updateComponent() {
            if (!paused) {
                animate();
            }

            if (keyPressed(Key.ESCAPE)) {
                getEngine().stop();
            } else if (keyPressed(Key.SPACE)) {
                paused = !paused;
                lastTime = System.currentTimeMillis();
            } else if (keyPressed(Key.LEFT) && paused) {

                if (animator.hasPrevious()) {
                    animator.moveBackward();
                    updateComponents();
                }

            } else if (keyPressed(Key.RIGHT) && paused) {

                if (animator.hasNext()) {
                    animator.move();
                    updateComponents();
                }
            } else if (keyPressed(Key.UP)) {
                if (speed < 60) {
                    speed++;
                }

            } else if (keyPressed(Key.DOWN)) {
                if (speed > 1) {
                    speed--;
                }
            } else if (keyPressed(Key.R)) {
                animator.reset();
                updateComponents();
            }
        }

        private void animate() {
            boolean move = false;

            while (lastTime + speedToMillis() < System.currentTimeMillis()) {
                if (animator.hasNext() && !paused) {
                    animator.move();

                    lastTime += speedToMillis();

                    move = true;
                    if (!animator.hasNext()) {
                        paused = true;
                        break;
                    }
                }
            }

            if (move) {
                updateComponents();

                lastTime = System.currentTimeMillis();
            }
        }

        private void updateComponents() {
            movesLabel.setText("%d/%d".formatted(animator.getMoveCount(), animator.numberOfMoves()));
            pushesLabel.setText("%d/%d".formatted(animator.getPushCount(), animator.numberOfPushes()));
            speedLabel.setText(Integer.toString(speed));

            boardComponent.setPlayerX(animator.getPlayerX());
            boardComponent.setPlayerY(animator.getPlayerY());
            boardComponent.setPlayerDir(animator.getLastMove());
            boardComponent.repaint();
        }
    }

    public static class SolutionAnimator {

        private final SolverReport solution;
        private Board board;

        private final SolverReport.SolutionIterator solutionIt;

        private int playerX;
        private int playerY;

        public SolutionAnimator(SolverReport solution) {
            this.solution = solution;
            Level level = solution.getParameters().getLevel();

            this.board = new MutableBoard(level);
            this.playerX = level.getPlayerX();
            this.playerY = level.getPlayerY();

            solutionIt = solution.getSolutionIterator();
        }

        public void move() {
            if (!solutionIt.hasNext()) {
                return;
            }

            Direction dir = solutionIt.next().direction();

            playerX += dir.dirX();
            playerY += dir.dirY();

            moveCrate(playerX, playerY, dir);
        }

        // move crate if needed
        private void moveCrate(int x, int y, Direction dir) {
            int newX = x + dir.dirX();
            int newY = y + dir.dirY();

            Tile curr = board.getAt(x, y).getTile();
            Tile next = board.getAt(newX, newY).getTile();

            switch (curr) {
                case CRATE -> board.setAt(x, y, Tile.FLOOR);
                case CRATE_ON_TARGET -> board.setAt(x, y, Tile.TARGET);
            }

            if (curr.isCrate()) {
                switch (next) {
                    case FLOOR -> board.setAt(newX, newY, Tile.CRATE);
                    case TARGET -> board.setAt(newX, newY, Tile.CRATE_ON_TARGET);
                }
            }
        }

        public void moveBackward() {
            if (!solutionIt.hasPrevious()) {
                return;
            }

            Move move = solutionIt.previous();

            Direction dir = move.direction();

            if (move.moveCrate()) {
                int crateX = playerX + dir.dirX();
                int crateY = playerY + dir.dirY();

                Tile crate = board.getAt(crateX, crateY).getTile();

                switch (crate) {
                    case CRATE -> board.setAt(crateX, crateY, Tile.FLOOR);
                    case CRATE_ON_TARGET -> board.setAt(crateX, crateY, Tile.TARGET);
                }

                switch (board.getAt(playerX, playerY).getTile()) {
                    case FLOOR -> board.setAt(playerX, playerY, Tile.CRATE);
                    case TARGET -> board.setAt(playerX, playerY, Tile.CRATE_ON_TARGET);
                }
            }

            playerX -= dir.dirX();
            playerY -= dir.dirY();
        }

        public void reset() {
            if (solutionIt.hasPrevious()) {
                solutionIt.reset();

                Level level = solution.getParameters().getLevel();
                playerX = level.getPlayerX();
                playerY = level.getPlayerY();

                board = new MutableBoard(level);
            }
        }

        public boolean hasNext() {
            return solutionIt.hasNext();
        }

        public boolean hasPrevious() {
            return solutionIt.hasPrevious();
        }

        public Board getBoard() {
            return board;
        }

        public int getPlayerX() {
            return playerX;
        }

        public int getPlayerY() {
            return playerY;
        }

        public int getMoveCount() {
            return solutionIt.getMoveCount();
        }

        public int getPushCount() {
            return solutionIt.getPushCount();
        }

        public int numberOfMoves() {
            return solution.numberOfMoves();
        }

        public int numberOfPushes() {
            return solution.numberOfPushes();
        }

        public Direction getLastMove() {
            if (!solutionIt.hasPrevious()) {
                return null;
            }

            Direction last = solutionIt.previous().direction();
            solutionIt.next();

            return last;
        }

        public SolverReport getSolution() {
            return solution;
        }
    }
}
