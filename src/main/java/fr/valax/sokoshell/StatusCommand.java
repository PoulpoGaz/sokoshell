package fr.valax.sokoshell;

import fr.valax.sokoshell.solver.*;
import fr.valax.sokoshell.solver.tasks.ISolverTask;
import fr.valax.sokoshell.solver.tasks.SolverTask;

import java.math.BigInteger;

public class StatusCommand extends AbstractVoidCommand {

    @Override
    public void run() {
        if (!helper.isSolving()) {
            System.out.println("Solver isn't running");
        } else {
            ISolverTask<?> solverTask = helper.getSolverTask();

            if (solverTask instanceof SolverTask task) {
                Solver solver = task.getSolver();
                Level level = task.getParameters().getLevel();

                BigInteger maxState = estimateMaxNumberOfStates(level);

                //System.out.printf("Solving level n°%d from %s by %s%n", level.getIndex(), pack.name(), pack.author());
                System.out.printf("Solving level n°%d%n", level.getIndex());

                if (solver instanceof Trackable t) {
                    System.out.printf("Number of state processed: %d%n", t.nStateExplored());
                }
                System.out.printf("Estimated maximal number of state: %d%n", maxState);
            }
        }
    }

    /**
     * let c the number of crate
     * let f the number of floor
     *
     * An upper bounds of the number of states is:
     * (f (c + 1))     where (n k) is n choose k
     *
     * (f c) counts the number of way to organize the crate (c) and the player ( + 1)
     */
    private BigInteger estimateMaxNumberOfStates(Level level) {
        Map map = level.getMap();

        int nCrate = 0;
        int nFloor = 0;

        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {

                if (map.getAt(x, y).isCrate()) {
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

    @Override
    public String getName() {
        return "status";
    }

    @Override
    public String getUsage() {
        return "Print status of the solver";
    }

    private record Tuple(BigInteger a, BigInteger b, BigInteger c) {}
}
