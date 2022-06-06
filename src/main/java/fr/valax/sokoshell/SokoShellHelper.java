package fr.valax.sokoshell;

import fr.valax.sokoshell.solver.Level;
import fr.valax.sokoshell.solver.Pack;
import fr.valax.sokoshell.solver.Solver;
import fr.valax.sokoshell.solver.SolverStatus;
import fr.valax.sokoshell.graphics.MapRenderer;
import fr.valax.sokoshell.graphics.MapStyle;
import fr.valax.sokoshell.utils.SolverInfo;
import org.jline.reader.Candidate;
import org.jline.terminal.Terminal;

import java.util.*;
import java.util.concurrent.CompletableFuture;
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

    private SolverInfo solverInfo;

    private SokoShellHelper() {
        renderer.setStyle(style);
    }

    public void solve(Solver solver, Pack pack, Level level) {
        Objects.requireNonNull(level);

        if (solverInfo != null) {
            System.out.println("Already solving.");
            return;
        }
        CompletableFuture<SolverStatus> f = CompletableFuture.supplyAsync(() -> solver.solve(level));

        solverInfo = new SolverInfo(f, solver, pack, level);

        f.exceptionally((t) -> {
            t.printStackTrace();
            return null;
        }).thenAccept((status) -> printStatus(solver, level, status));
    }

    private void printStatus(Solver solver, Level level, SolverStatus status) {
        lock.lock();

        try {
            if (status == null) {
                System.out.println("Error, status is null");
            } else {
                switch (status) {
                    case NO_SOLUTION -> {
                        System.out.println("No solution found");
                    }
                    case SOLUTION_FOUND -> {
                        System.out.println("Solution found. Use 'print solution' to print the solution");
                        level.setSolution(solver.getSolution());
                    }
                    case PAUSED -> {
                        System.out.println("Paused");
                    }
                    case STOPPED -> {
                        System.out.println("Stopped");
                    }
                }
            }

            solverInfo = null;
        } finally {
            lock.unlock();
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

    public Collection<Pack> getPacks() {
        return packs.values();
    }

    public Terminal getTerminal() {
        return terminal;
    }

    public void setTerminal(Terminal terminal) {
        this.terminal = terminal;
    }

    public SolverInfo getSolverInfo() {
        return solverInfo;
    }

    public boolean isSolving() {
        return solverInfo != null;
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
