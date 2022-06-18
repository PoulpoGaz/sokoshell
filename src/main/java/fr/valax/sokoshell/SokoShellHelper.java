package fr.valax.sokoshell;

import fr.valax.sokoshell.graphics.MapRenderer;
import fr.valax.sokoshell.graphics.MapStyle;
import fr.valax.sokoshell.solver.*;
import fr.valax.sokoshell.solver.tasks.BenchmarkTask;
import fr.valax.sokoshell.solver.tasks.ISolverTask;
import fr.valax.sokoshell.solver.tasks.SolverTask;
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

    private final MapStyle style = new MapStyle();
    private final MapRenderer renderer = new MapRenderer();

    private Terminal terminal;

    private ISolverTask<?> task;

    private SokoShellHelper() {
        renderer.setStyle(style);
    }

    public void solve(Solver solver, SolverParameters parameters) {
        Objects.requireNonNull(parameters);
        Objects.requireNonNull(solver);

        if (task != null) {
            System.out.println("Already solving.");
            return;
        }

        SolverTask task = new SolverTask(solver, parameters);
        task.start();
        task.onEnd(this::printSolution);
        this.task = task;
    }

    private void printSolution(Solution solution) {
        lock.lock();

        try {
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
                level.setSolution(solution);
            }

            task = null;
        } finally {
            lock.unlock();
        }
    }

    public void benchmark(Solver solver, Map<String, Object> params, Pack pack) {
        Objects.requireNonNull(params);
        Objects.requireNonNull(pack);

        BenchmarkTask task = new BenchmarkTask(solver, params, pack);
        task.start();
        task.onEnd((solutions -> {

            if (solutions == null) {
                System.out.println("An error has occurred.");
            } else {
                for (int i = 0; i < solutions.size(); i++) {
                    System.out.print("Level n°" + i + ": ");
                    printSolution(solutions.get(i));
                }
            }
        }));
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


    public Collection<Pack> getPacks() {
        return packs.values();
    }

    public Terminal getTerminal() {
        return terminal;
    }

    public void setTerminal(Terminal terminal) {
        this.terminal = terminal;
    }

    public ISolverTask<?> getSolverTask() {
        return task;
    }

    public boolean isSolving() {
        return task != null;
    }

    public MapStyle getStyle() {
        return style;
    }

    public MapRenderer getRenderer() {
        return renderer;
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
