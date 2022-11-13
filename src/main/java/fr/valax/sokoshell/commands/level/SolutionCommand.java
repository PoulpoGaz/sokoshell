package fr.valax.sokoshell.commands.level;

import fr.valax.args.api.Option;
import fr.valax.sokoshell.graphics.*;
import fr.valax.sokoshell.graphics.layout.BorderLayout;
import fr.valax.sokoshell.graphics.layout.GridLayout;
import fr.valax.sokoshell.graphics.layout.GridLayoutConstraints;
import fr.valax.sokoshell.solver.*;

import java.io.InputStream;
import java.io.PrintStream;
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
            Key.LEFT.addTo(engine);
            Key.RIGHT.addTo(engine);
            Key.DOWN.addTo(engine);
            Key.UP.addTo(engine);
            Key.ENTER.addTo(engine);
            Key.SPACE.addTo(engine);
            Key.R.addTo(engine);
            Key.E.addTo(engine);
            Key.ESCAPE.addTo(engine);
            engine.getKeyMap().setAmbiguousTimeout(100L);
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
            setLabels();

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
            bot.add(NamedComponent.create("Speed:", speedLabel), c);
            c.x++;
            c.weightX = c.weightY = 0;
            bot.add(new MemoryBar(), c);

            mapComponent = new MapComponent();
            mapComponent.setMap(animator.getMap());
            mapComponent.setPlayerX(animator.getPlayerX());
            mapComponent.setPlayerY(animator.getPlayerY());

            setLayout(new BorderLayout());
            add(bot, BorderLayout.SOUTH);
            add(mapComponent, BorderLayout.CENTER);
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
                    setLabels();
                    updateMapComponent();
                }

            } else if (keyPressed(Key.RIGHT) && paused) {

                if (animator.hasNext()) {
                    animator.move();
                    setLabels();
                    updateMapComponent();
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
                setLabels();
                updateMapComponent();
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
                updateMapComponent();
                setLabels();

                lastTime = System.currentTimeMillis();
            }
        }

        private void setLabels() {
            movesLabel.setText("%d/%d".formatted(animator.getMoveCount(), animator.numberOfMoves()));
            pushesLabel.setText("%d/%d".formatted(animator.getPushCount(), animator.numberOfPushes()));
            speedLabel.setText(Integer.toString(speed));
        }

        private void updateMapComponent() {
            mapComponent.setPlayerX(animator.getPlayerX());
            mapComponent.setPlayerY(animator.getPlayerY());
            mapComponent.setPlayerDir(animator.getLastMove());
            mapComponent.repaint();
        }
    }

    public static class SolutionAnimator {

        private final SolverReport solution;
        private final Map map;

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

                map.set(level.getMap());
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
