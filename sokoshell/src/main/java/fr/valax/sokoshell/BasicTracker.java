package fr.valax.sokoshell;

import fr.valax.sokoshell.solver.ISolverStatistics;
import fr.valax.sokoshell.solver.Trackable;
import fr.valax.sokoshell.solver.Tracker;
import fr.valax.sokoshell.utils.Alignment;
import fr.valax.sokoshell.utils.PrettyColumn;
import fr.valax.sokoshell.utils.PrettyTable;
import fr.valax.sokoshell.utils.Utils;
import org.jline.utils.AttributedString;

import java.io.PrintStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Basic implementation of {@link Tracker}. It just tries to get information
 * as much as possible from a {@link Trackable}
 */
public class BasicTracker implements Tracker {

    private final List<InstantStatistic> stats = new ArrayList<>();

    private boolean end = false;

    @Override
    public synchronized void updateStatistics(Trackable trackable) {
        if (end) {
            return;
        }

        long time = System.currentTimeMillis();

        int state = trackable.nStateExplored();
        int queue = trackable.currentQueueSize();

        stats.add(new InstantStatistic(time, state, queue));
    }

    @Override
    public synchronized void reset() {
        end = false;
        stats.clear();
    }

    @Override
    public synchronized ISolverStatistics getStatistics(Trackable trackable) {
        this.end = true;

        long end = trackable.timeEnded();
        add(end, trackable.nStateExplored(), trackable.currentQueueSize());

        stats.add(0, new InstantStatistic(trackable.timeStarted(), 0, 0));
        return new SolverStatistics(stats, trackable.timeStarted(), trackable.timeEnded());
    }

    private void add(long time, int state, int queue) {
        if (!stats.isEmpty()) {
            InstantStatistic last = stats.get(stats.size() - 1);

            if (last.nodeExplored() == state && last.queueSize() == queue && Math.abs(last.time() - time) <= 5) {
                return;
            }
        }

        stats.add(new InstantStatistic(time, state, queue));
    }

    public static class SolverStatistics implements ISolverStatistics {

        @Serial
        private static final long serialVersionUID = 1L;

        private final List<InstantStatistic> statistics;

        private final long timeStarted; // in millis
        private final long timeEnded; // in millis

        public SolverStatistics(List<InstantStatistic> statistics, long timeStarted, long timeEnded) {
            this.statistics = statistics;
            this.timeStarted = timeStarted;
            this.timeEnded = timeEnded;
        }

        @Override
        public long timeStarted() {
            return timeStarted;
        }

        @Override
        public long timeEnded() {
            return timeEnded;
        }

        @Override
        public int totalStateExplored() {
            if (statistics.isEmpty()) {
                return -1;
            } else {
                return statistics.get(statistics.size() - 1).nodeExplored();
            }
        }

        @Override
        public long nodeExploredPerSeconds() {
            if (statistics.isEmpty()) {
                return -1;
            }

            InstantStatistic first = statistics.get(0);
            InstantStatistic last = statistics.get(statistics.size() - 1);

            // first.nodeExplored() usually returns 0
            return 1000L * (last.nodeExplored() - first.nodeExplored()) / runTime();
        }

        @Override
        public int averageQueueSize() {
            if (statistics.isEmpty()) {
                return -1;
            }

            double area = 0;

            InstantStatistic last = null;
            for (InstantStatistic i : statistics) {
                if (i.queueSize() > 0) {
                    if (last != null) {
                        // sum of a rectangle of side (t2 - t1) x q1
                        // and triangle of side (t2 - t1) x (q2 - q1)
                        area += (i.time() - last.time()) * (last.queueSize() + i.queueSize()) / 2d;
                    }

                    last = i;
                }
            }

            return (int) (area / runTime());
        }

        @Override
        public PrettyTable printStatistics(PrintStream out, PrintStream err) {
            ISolverStatistics.super.printStatistics(out, err);

            if (!statistics.isEmpty()) {
                out.printf("Average queue size: %d%n", averageQueueSize());
                out.printf("Node explored per seconds: %d%n",  nodeExploredPerSeconds());
                out.println();

                PrettyTable table = new PrettyTable();
                PrettyColumn<Long> time = new PrettyColumn<>("Time");
                time.setToString((l) -> PrettyTable.wrap(Utils.prettyDate(l)));

                PrettyColumn<Integer> nodeExplored = new PrettyColumn<>("Node explored");
                nodeExplored.setToString(this::statsToString);
                PrettyColumn<Integer> queueSize = new PrettyColumn<>("Queue size");
                nodeExplored.setToString(this::statsToString);

                for (InstantStatistic iStat : statistics) {
                    time.add(Alignment.RIGHT, iStat.time() - timeStarted);
                    nodeExplored.add(Alignment.RIGHT, iStat.nodeExplored());
                    queueSize.add(Alignment.RIGHT, iStat.queueSize());
                }

                table.addColumn(time);
                table.addColumn(nodeExplored);
                table.addColumn(queueSize);

                return table;
            } else {
                return null;
            }
        }

        private AttributedString[] statsToString(Integer i) {
            if (i == null || i < 0) {
                return PrettyTable.EMPTY;
            } else {
                return PrettyTable.wrap(i);
            }
        }
    }

    /**
     * Contains statistics at a specific instant
     */
    public record InstantStatistic(long time, int nodeExplored, int queueSize) implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;
    }
}