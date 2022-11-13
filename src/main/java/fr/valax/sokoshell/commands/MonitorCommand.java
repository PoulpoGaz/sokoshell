package fr.valax.sokoshell.commands;

import fr.valax.sokoshell.SokoShellHelper;
import fr.valax.sokoshell.SolverTask;
import fr.valax.sokoshell.TaskStatus;
import fr.valax.sokoshell.graphics.*;
import fr.valax.sokoshell.graphics.Component;
import fr.valax.sokoshell.graphics.Graphics;
import fr.valax.sokoshell.graphics.Label;
import fr.valax.sokoshell.graphics.layout.*;
import fr.valax.sokoshell.graphics.layout.BorderLayout;
import fr.valax.sokoshell.graphics.layout.GridLayout;
import fr.valax.sokoshell.solver.*;
import fr.valax.sokoshell.utils.Utils;
import org.jline.terminal.Size;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.math.BigInteger;
import java.util.List;

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
        try (TerminalEngine engine = new TerminalEngine(helper.getTerminal())) {
            initEngine(engine, runningTask);
            try {
                engine.show();
            } catch (Exception e) { // due to the voluntary lack of synchronization, actually never happen
                ex = e;
                //last = monitor.state;
            }
        }

        if (ex != null) {
            ex.printStackTrace(err);
            err.println(last);
        }

        return 0;
    }

    private void initEngine(TerminalEngine engine, SolverTask task) {
        Key.ENTER.addTo(engine);
        Key.E.addTo(engine);
        Key.ESCAPE.addTo(engine);
        engine.getKeyMap().setAmbiguousTimeout(100L);

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


        // top labels
        private final Label progressLabel = new Label();

        private final Label runningForLabel = new Label();
        private final Label stateExploredLabel = new Label();
        private final Label queueSizeLabel = new Label();

        // bot labels
        private final Label packLabel = new Label();
        private final Label levelLabel = new Label();
        private final Label maxNumberOfStateLabel = new Label();

        private MapComponent mapComponent;

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
            mapComponent = new MapComponent();

            add(top, BorderLayout.NORTH);
            add(bottom, BorderLayout.SOUTH);
            add(mapComponent, BorderLayout.CENTER);
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
            innerCenter.add(new Label("export"), hc);
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

        @Override
        protected void updateComponent() {
            if (keyReleased(Key.ESCAPE)) {
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
                Level currentLevel = levels.get(index);
                Pack currentPack = currentLevel.getPack();

                Map map = currentLevel.getMap();

                BigInteger n = estimateMaxNumberOfStates(map);
                maxNumberOfStateLabel.setText(n.toString());

                map.forEach(TileInfo::removeCrate);

                progressLabel.setText(index + "/" + task.getLevels().size());
                levelLabel.setText(Integer.toString(currentLevel.getIndex() + 1));
                packLabel.setText(currentPack.name());

                mapComponent.setMap(map);
                mapComponent.setPlayerX(-1);
                mapComponent.setPlayerY(-1);
            } else if (task.getTaskStatus() == TaskStatus.FINISHED) {
                progressLabel.setText("Done!");
            } else {
                mapComponent.setMap(null);

                progressLabel.setText("?/" + task.getLevels().size());
                levelLabel.setText("?");
                packLabel.setText("?");
                maxNumberOfStateLabel.setText("?");
            }
        }

        private void changeState(State newState) {
            Map map = mapComponent.getMap();
            if (map == null || newState == null) {
                return;
            }

            if (currentState != null) {
                map.removeStateCrates(currentState);
            }

            this.currentState = newState;
            map.safeAddStateCrates(newState);

            int playerX = map.getX(currentState.playerPos());
            int playerY = map.getY(currentState.playerPos());

            mapComponent.setPlayerX(playerX);
            mapComponent.setPlayerY(playerY);
            mapComponent.repaint();
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
