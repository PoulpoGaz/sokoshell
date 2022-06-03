package fr.valax.sokoshell;

import fr.valax.sokoshell.solver.Level;
import fr.valax.sokoshell.solver.Pack;
import fr.valax.sokoshell.solver.Solver;
import fr.valax.sokoshell.solver.SolverStatus;
import jdk.jshell.JShell;
import org.jline.reader.Candidate;
import org.jline.terminal.Terminal;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SokoShellHelper implements Lock {

    private final ReentrantLock lock = new ReentrantLock();

    private final SokoShell shell;
    private final Map<String, Pack> packs;

    private Terminal terminal;

    private CompletableFuture<Void> solverFuture;

    public SokoShellHelper(SokoShell shell) {
        this.shell = shell;
        packs = new HashMap<>();
    }

    public void solve(Solver solver, Level level) {
        Objects.requireNonNull(level);

        solverFuture = CompletableFuture.supplyAsync(() -> solver.solve(level))
                .thenAccept((status) -> printStatus(solver, level, status));
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
            solverFuture = null;
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

    public CompletableFuture<Void> getSolverFuture() {
        return solverFuture;
    }

    public boolean isSolving() {
        return solverFuture != null;
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
