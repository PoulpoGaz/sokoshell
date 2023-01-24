package fr.valax.sokoshell.commands;

import fr.valax.sokoshell.SokoShell;
import fr.valax.sokoshell.SolverTask;
import fr.valax.sokoshell.TaskStatus;
import fr.valax.sokoshell.graphics.*;
import fr.valax.sokoshell.graphics.layout.*;
import fr.valax.sokoshell.solver.*;
import fr.valax.sokoshell.solver.board.Direction;
import fr.valax.sokoshell.solver.board.IBoard;
import fr.valax.sokoshell.solver.board.MutableBoard;
import fr.valax.sokoshell.solver.board.tiles.MutableTileInfo;
import fr.valax.sokoshell.utils.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.math.BigInteger;
import java.nio.file.Path;
import java.util.List;

public class MonitorCommand extends AbstractCommand {

    @Override
    protected int executeImpl(InputStream in, PrintStream out, PrintStream err) {
        SolverTask runningTask = sokoshell().getTaskList().getRunningTask();

        if (runningTask == null) {
            err.println("No task are running");
            return FAILURE;
        }


        Exception ex = null;
        try (TerminalEngine engine = new TerminalEngine(sokoshell().getTerminal())) {
            initEngine(engine, runningTask);
            try {
                engine.show();
            } catch (Exception e) { // due to the voluntary lack of synchronization, actually never happen
                ex = e;
            }
        }

        if (ex != null) {
            ex.printStackTrace(err);
        }

        return 0;
    }

    private void initEngine(TerminalEngine engine, SolverTask task) {
        Key.ENTER.bind(engine);
        Key.CTRL_E.bind(engine);
        Key.ESCAPE.bind(engine);

        engine.setRootComponent(new Monitor(task));
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

    private static class Monitor extends Component {

        private final SolverTask task;
        private final List<Level> levels;
        private final Solver solver;
        private final Trackable trackable;

        private int index;

        private State currentState;
        private Level currentLevel;
        private Pack currentPack;


        // top labels
        private final Label progressLabel = new Label();

        private final Label runningForLabel = new Label();
        private final Label stateExploredLabel = new Label();
        private final Label queueSizeLabel = new Label();

        // bot labels
        private final Label packLabel = new Label();
        private final Label levelLabel = new Label();
        private final Label maxNumberOfStateLabel = new Label();

        private BoardComponent boardComponent;

        public Monitor(SolverTask task) {
            this.task = task;
            this.levels = task.getLevels();
            this.solver = task.getSolver();

            if (solver instanceof Trackable tr) {
                this.trackable = tr;
            } else {
                this.trackable = null;
            }

            initComponent();
            changeLevel();
        }

        private void initComponent() {
            setLayout(new BorderLayout());

            Component top = createTopComponent();
            Component bottom = createBotComponent();
            boardComponent = new BoardComponent();

            add(top, BorderLayout.NORTH);
            add(bottom, BorderLayout.SOUTH);
            add(boardComponent, BorderLayout.CENTER);
        }

        private Component createTopComponent() {
            progressLabel.setHorizAlign(Label.WEST);
            runningForLabel.setHorizAlign(Label.WEST);
            stateExploredLabel.setHorizAlign(Label.WEST);
            queueSizeLabel.setHorizAlign(Label.WEST);

            Component top = new Component();
            top.setLayout(new GridLayout());
            top.setBorder(new BasicBorder(false, false, true, false));


            GridLayoutConstraints c = new GridLayoutConstraints();
            c.weightX = 1;
            c.weightY = 1;
            c.x = 0;
            c.y = 0;
            c.fill = GridLayoutConstraints.BOTH;

            top.add(NamedComponent.create("Task id:", new Label("#" + task.getTaskIndex(), Label.WEST)), c);
            c.x++;
            top.add(NamedComponent.create("Request pack:", new Label(task.getPack(), Label.WEST)), c);
            c.x++;
            top.add(NamedComponent.create("Request level:", new Label(task.getLevel(), Label.WEST)), c);
            c.x++;
            top.add(NamedComponent.create("Progress:", progressLabel), c);

            c.x = 0;
            c.y++;
            top.add(NamedComponent.create("Running for:", runningForLabel), c);
            c.x++;
            top.add(NamedComponent.create("State explored:", stateExploredLabel), c);
            c.x++;
            top.add(NamedComponent.create("Queue size:", queueSizeLabel), c);

            return top;
        }

        private Component createBotComponent() {
            packLabel.setHorizAlign(Label.WEST);
            levelLabel.setHorizAlign(Label.WEST);
            maxNumberOfStateLabel.setHorizAlign(Label.WEST);

            Component innerTop = new Component();
            innerTop.setLayout(new GridLayout());
            GridLayoutConstraints glc = new GridLayoutConstraints();
            glc.weightX = glc.weightY = 1;
            glc.fill = GridLayoutConstraints.BOTH;
            glc.x = glc.y = 0;
            innerTop.add(NamedComponent.create("Pack:", packLabel), glc);
            glc.x++;
            innerTop.add(NamedComponent.create("Level:", levelLabel), glc);
            glc.x++;
            innerTop.add(NamedComponent.create("Max number of state:", maxNumberOfStateLabel), glc);
            glc.x++;


            Component innerCenter = new Component();
            innerCenter.setLayout(new HorizontalLayout());
            HorizontalConstraint hc = new HorizontalConstraint();
            hc.fillYAxis = true;
            hc.endComponent = true;
            innerCenter.add(new ExportComponent(this::export), hc);
            hc.endComponent = false;
            hc.orientation = HorizontalLayout.Orientation.RIGHT;
            innerCenter.add(new MemoryBar(), hc);


            Component component = new Component();
            component.setLayout(new BorderLayout());
            component.setBorder(new BasicBorder(true, false, false, false));
            component.add(innerTop, BorderLayout.NORTH);
            component.add(innerCenter, BorderLayout.CENTER);

            return component;
        }

        private String export() {
            if (boardComponent.getBoard() == null) {
                return null;
            }

            try {
                Path out = SokoShell.INSTANCE
                        .exportPNG(currentPack, currentLevel,
                                boardComponent.getBoard(), boardComponent.getPlayerX(), boardComponent.getPlayerY(),
                                Direction.DOWN);

                return out.toString();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected void updateComponent() {
            if (keyPressed(Key.ESCAPE)) {
                getEngine().stop();
            }

            if (task.getCurrentLevel() != index) {
                changeLevel();
            }

            if (trackable != null) {
                State state = trackable.currentState();

                if (state != currentState) {
                    changeState(state);
                }

                long end = trackable.timeEnded();

                if (end < 0) {
                    end = System.currentTimeMillis();
                }

                runningForLabel.setText(Utils.prettyDate(end - trackable.timeStarted()));
                stateExploredLabel.setText(Integer.toString(trackable.nStateExplored()));
                queueSizeLabel.setText(Integer.toString(trackable.currentQueueSize()));
            }
        }

        private void changeLevel() {
            index = task.getCurrentLevel();

            if (index >= 0 && index < levels.size()) {
                currentLevel = levels.get(index);
                currentPack = currentLevel.getPack();

                MutableBoard board = new MutableBoard(currentLevel.getBoard());

                BigInteger n = estimateMaxNumberOfStates(board);
                maxNumberOfStateLabel.setText(n.toString());

                board.forEach(MutableTileInfo::removeCrate);

                progressLabel.setText(index + "/" + task.getLevels().size());
                levelLabel.setText(Integer.toString(currentLevel.getIndex() + 1));
                packLabel.setText(currentPack.name());

                boardComponent.setBoard(board);
                boardComponent.setPlayerX(-1);
                boardComponent.setPlayerY(-1);
            } else if (task.getTaskStatus() == TaskStatus.FINISHED) {
                progressLabel.setText("Done!");
            } else {
                currentLevel = null;
                currentPack = null;
                boardComponent.setBoard(null);

                progressLabel.setText("?/" + task.getLevels().size());
                levelLabel.setText("?");
                packLabel.setText("?");
                maxNumberOfStateLabel.setText("?");
            }
        }

        private void changeState(State newState) {
            MutableBoard board = boardComponent.getBoard();
            if (board == null || newState == null) {
                return;
            }

            if (currentState != null) {
                board.safeRemoveStateCrates(currentState);
            }

            this.currentState = newState;
            board.safeAddStateCrates(newState);

            int playerX = board.getX(currentState.playerPos());
            int playerY = board.getY(currentState.playerPos());

            boardComponent.setPlayerX(playerX);
            boardComponent.setPlayerY(playerY);
            boardComponent.repaint();
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
        private BigInteger estimateMaxNumberOfStates(IBoard<?> board) {
            int nCrate = 0;
            int nFloor = 0;

            for (int y = 0; y < board.getHeight(); y++) {
                for (int x = 0; x < board.getWidth(); x++) {

                    if (board.getAt(x, y).anyCrate()) {
                        nCrate++;
                        nFloor++;
                    } else if (!board.getAt(x, y).isSolid()) {
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
