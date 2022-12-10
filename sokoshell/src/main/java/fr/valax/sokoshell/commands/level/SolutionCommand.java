package fr.valax.sokoshell.commands.level;

import fr.valax.args.api.Option;
import fr.valax.sokoshell.SokoShellHelper;
import fr.valax.sokoshell.graphics.*;
import fr.valax.sokoshell.graphics.layout.*;
import fr.valax.sokoshell.solver.*;

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

        try (TerminalEngine engine = new TerminalEngine(helper.getTerminal())) {
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

        private MapComponent mapComponent;

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

            mapComponent = new MapComponent();
            mapComponent.setMap(animator.getMap());
            mapComponent.setPlayerX(animator.getPlayerX());
            mapComponent.setPlayerY(animator.getPlayerY());

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
            add(mapComponent, BorderLayout.CENTER);
        }

        private String export() {
            if (mapComponent.getMap() == null) {
                return null;
            }

            try {
                Level l = animator.getSolution().getLevel();

                Path out = SokoShellHelper.INSTANCE
                        .exportPNG(l.getPack(), l,
                                animator.getMap(), animator.getPlayerX(), animator.getPlayerY(),
                                animator.getLastMove(), 16);

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

            if (keyReleased(Key.ESCAPE)) {
                getEngine().stop();
            } else if (keyReleased(Key.SPACE)) {
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
            } else if (keyReleased(Key.R)) {
                animator.reset();
                updateComponents();
            } /*else if (keyReleased(Key.E)) {
                SolverReport report = animator.getSolution();
                Level level = report.getLevel();

                try {
                    helper.exportPNG(level.getPack(), level, animator.getMap(),
                            animator.getPlayerX(), animator.getPlayerY(), animator.getLastMove());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }*/
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

            mapComponent.setPlayerX(animator.getPlayerX());
            mapComponent.setPlayerY(animator.getPlayerY());
            mapComponent.setPlayerDir(animator.getLastMove());
            mapComponent.repaint();
        }
    }

    public static class SolutionAnimator {

        private final SolverReport solution;
        private Map map;

        private final List<Move> path;
        private int pathIndex;

        private int playerX;
        private int playerY;

        private int move;
        private int push;

        public SolutionAnimator(SolverReport solution) {
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

        public void reset() {
            if (pathIndex > 0) {
                move = 0;
                push = 0;
                pathIndex = 0;

                Level level = solution.getParameters().getLevel();
                playerX = level.getPlayerX();
                playerY = level.getPlayerY();

                map = level.getMap();
            }
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

        public SolverReport getSolution() {
            return solution;
        }
    }
}
