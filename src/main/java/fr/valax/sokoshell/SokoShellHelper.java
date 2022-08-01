package fr.valax.sokoshell;

import fr.valax.args.CommandLine;
import fr.valax.sokoshell.graphics.MapRenderer;
import fr.valax.sokoshell.graphics.MapStyle;
import fr.valax.sokoshell.solver.*;
import org.jline.reader.Candidate;
import org.jline.terminal.Terminal;

import java.util.Map;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SokoShellHelper implements Lock {

    public static final SokoShellHelper INSTANCE = new SokoShellHelper();

    private final ReentrantLock lock = new ReentrantLock();

    private final Map<String, Pack> packs = new HashMap<>();
    private final Map<String, MapStyle> styles = new HashMap<>();

    private final MapRenderer renderer = new MapRenderer();

    private CommandLine cli;
    private Terminal terminal;

    private Queue<SolverTask> finishedTasks = new ArrayDeque<>();
    private SolverTask runningTask = null;
    private Queue<SolverTask> pendingTasks = new ArrayDeque<>();

    private Pack selectedPack = null;
    private int selectedLevel = -1;

    private SokoShellHelper() {
        addMapStyle(MapStyle.DEFAULT_STYLE);
        renderer.setStyle(MapStyle.DEFAULT_STYLE);
    }

    public void addTask(Solver solver, Map<String, Object> params, List<Level> levels) {
        Objects.requireNonNull(params);
        Objects.requireNonNull(levels);

        addTask(new SolverTask(solver, params, levels));
    }

    public void addTask(SolverTask task) {
        if (runningTask == null) {
            runningTask = task;
            task.start();
        } else {
            pendingTasks.offer(task);
        }

        task.onEnd(this::onTaskEnd);
    }

    private void onTaskEnd(List<Solution> solutions) {
        lock.lock();

        try {
            if (solutions == null) {
                System.out.println("An error has occurred.");
            } else {
                for (int i = 0; i < solutions.size(); i++) {
                    System.out.print("Level n°" + (i + 1) + ": ");
                    printSolution(solutions.get(i));
                }
            }

            finishedTasks.offer(runningTask);
            runningTask = pendingTasks.poll();
            if (runningTask != null) {
                runningTask.start();
            }
        } finally {
            lock.unlock();
        }
    }

    private void printSolution(Solution solution) {
        if (solution == null) {
            System.out.println("An error has occurred. Failed to find solution");

        } else {
            if (solution.isSolved()) {
                System.out.println("Solution found. Use 'print solution' to print the solution");

            } else if (solution.hasNoSolution()) {
                System.out.println("No solution found");

            } else if (solution.isStopped()) {
                System.out.println("Research stopped");

            } else if (solution.getStatus() == SolverStatus.TIMEOUT) {
                System.out.println("Timeout");
            }

            Level level = solution.getParameters().getLevel();
            level.addSolution(solution);
        }
    }

    public boolean addMapStyle(MapStyle mapStyle) {
        if (styles.containsKey(mapStyle.getName())) {
            return false;
        } else {
            styles.put(mapStyle.getName(), mapStyle);

            return true;
        }
    }

    public void addMapStyleReplace(MapStyle mapStyle) {
        styles.put(mapStyle.getName(), mapStyle);
    }

    public MapStyle getMapStyle(String name) {
        return styles.get(name);
    }

    public void addMapStyleCandidates(List<Candidate> candidates) {
        for (MapStyle mapStyle : getMapStyles()) {
            candidates.add(new Candidate(mapStyle.getName()));
        }
    }

    /**
     * Add a pack if there is no pack with his name
     * @param pack the pack to add
     * @return true if the pack was added
     */
    public boolean addPack(Pack pack) {
        if (packs.containsKey(pack.name())) {
            return false;
        } else {
            packs.put(pack.name(), pack);

            return true;
        }
    }

    public void addPackReplace(Pack pack) {
        packs.put(pack.name(), pack);
    }

    public Pack getPack(String name) {
        return packs.get(name);
    }

    public void addPackCandidates(List<Candidate> candidates) {
        for (Pack pack : getPacks()) {
            candidates.add(new Candidate(pack.name()));
        }
    }

    public void println(String s) {
        lock();

        try {
            terminal.writer().println(s);
            terminal.writer().flush();
        } finally {
            unlock();
        }
    }

    public void tryPrintln(String s) {
        if (tryLock()) {

            try {
                terminal.writer().println(s);
                terminal.writer().flush();
            } finally {
                unlock();
            }
        }
    }

    public void tryPrintln(String s, long time, TimeUnit unit) {
        try {
            if (!tryLock(time, unit)) {
                return;
            }
        } catch (InterruptedException e) {
            return;
        }

        try {
            terminal.writer().println(s);
            terminal.writer().flush();
        } finally {
            unlock();
        }
    }

    public void selectPack(Pack pack) {
        this.selectedPack = pack;
    }

    public Pack getSelectedPack() {
        return selectedPack;
    }

    public void selectLevel(int index) {
        this.selectedLevel = index;
    }

    public Level getSelectedLevel() {
        if (selectedPack == null || selectedLevel < 0 || selectedLevel >= selectedPack.levels().size()) {
            return null;
        } else {
            return selectedPack.levels().get(selectedLevel);
        }
    }

    public int getSelectedLevelIndex() {
        return selectedLevel;
    }

    public Collection<Pack> getPacks() {
        return packs.values();
    }

    public Terminal getTerminal() {
        return terminal;
    }

    public void setTerminal(Terminal terminal) {
        this.terminal = terminal;
    }

    public void setCli(CommandLine cli) {
        this.cli = cli;
    }

    public CommandLine getCli() {
        return cli;
    }

    public SolverTask getSolverTask() {
        return runningTask;
    }

    public boolean isSolving() {
        return runningTask != null;
    }

    public MapRenderer getRenderer() {
        return renderer;
    }

    public MapStyle getMapStyle() {
        return renderer.getStyle();
    }

    public void setMapStyle(MapStyle style) {
        renderer.setStyle(style);
    }

    public Collection<MapStyle> getMapStyles() {
        return styles.values();
    }

    @Override
    public void lock() {
        lock.lock();
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        lock.lockInterruptibly();
    }

    @Override
    public boolean tryLock() {
        return lock.tryLock();
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return lock.tryLock(time, unit);
    }

    @Override
    public void unlock() {
        lock.unlock();
    }

    @Override
    public Condition newCondition() {
        return lock.newCondition();
    }
}
