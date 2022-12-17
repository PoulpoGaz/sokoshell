package fr.valax.sokoshell.commands.table;

import fr.valax.args.CommandLine;
import fr.valax.args.api.Command;
import fr.valax.args.api.Option;
import fr.valax.args.utils.ArgsUtils;
import fr.valax.sokoshell.solver.*;
import fr.valax.sokoshell.utils.Alignment;
import fr.valax.sokoshell.utils.PrettyColumn;
import fr.valax.sokoshell.utils.PrettyTable;
import fr.valax.sokoshell.utils.Utils;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;
import org.jline.utils.AttributedString;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.List;

public class StatsCommand extends TableCommand {

    @Option(names = {"p", "pack"}, hasArgument = true, argName = "Pack name")
    protected String pack;

    @Option(names = {"l", "level"}, hasArgument = true, argName = "Level index")
    protected Integer level;

    @Option(names = {"R", "report"}, hasArgument = true, argName = "Report index")
    private Integer report;

    @Override
    protected int executeImpl(InputStream in, PrintStream out, PrintStream err) throws InvalidArgument {
        Level l = getLevel(pack, level);

        if (!l.hasReport()) {
            err.println("Not solved");
            return FAILURE;
        }

        SolverReport s;
        if (report != null) {
            s = l.getSolverReport(report);

            if (s == null) {
                err.println("Index out of bounds");
                return FAILURE;
            }
        } else {
            s = l.getLastReport();
        }

        out.printf("Status: %s. Solver: %s%n", s.getStatus(), s.getSolverName());

        SolverParameters params = s.getParameters();
        out.printf("Level: %s - %d%n", params.getLevel().getPack().name(), params.getLevel().getIndex());

        Collection<SolverParameter> collection = params.getParameters();
        if (!collection.isEmpty()) {
            out.println("Parameters:");

            for (SolverParameter p : collection) {
                if (p.hasArgument()) {
                    out.printf("%s = %s%n", p.getName(), p.get());
                }
            }
        }

        SolverStatistics stats = s.getStatistics();
        if (stats != null) {
            printStats(stats, out, err);
        }

        return Command.SUCCESS;
    }

    private void printStats(SolverStatistics stats, PrintStream out, PrintStream err) {
        long start = stats.getTimeStarted();
        long end = stats.getTimeEnded();

        out.printf("Started at %s. Finished at %s. Run time: %s%n",
                Utils.formatDate(start),
                Utils.formatDate(end),
                Utils.prettyDate(end - start));

        List<SolverStatistics.InstantStatistic> iStats = stats.getStatistics();

        if (iStats != null && iStats.size() > 0) {
            out.printf("Average queue size: %d%n", averageQueueSize(start, end, iStats));
            out.printf("Node explored per seconds: %d%n", nodeExploredPerSeconds(start, end, iStats));
            out.println();

            PrettyTable table = new PrettyTable();
            PrettyColumn<Long> time = new PrettyColumn<>("Time");
            time.setToString((l) -> PrettyTable.wrap(Utils.prettyDate(l)));

            PrettyColumn<Integer> nodeExplored = new PrettyColumn<>("Node explored");
            nodeExplored.setToString(this::statsToString);
            PrettyColumn<Integer> queueSize = new PrettyColumn<>("Queue size");
            nodeExplored.setToString(this::statsToString);

            for (SolverStatistics.InstantStatistic iStat : iStats) {
                time.add(Alignment.RIGHT, iStat.time() - start);
                nodeExplored.add(Alignment.RIGHT, iStat.nodeExplored());
                queueSize.add(Alignment.RIGHT, iStat.queueSize());
            }

            table.addColumn(time);
            table.addColumn(nodeExplored);
            table.addColumn(queueSize);

            printTable(out, err, table);
        }
    }

    /**
     * Compute the average of the following function:
     * <pre>
     *      f : [start, end] -> real
     *          t -> if iStats contains an instant statistic at t then returns the queue size at t
     *               else it exists two t1 < t < t2 such as it exists two instant statistic with queue size q1 and q2
     *                    then returns (t - t1) * (q1 - q2) / (t1 - t2) + q1
     * </pre>
     * The function can be seen as follows: given two consecutive instant, we just draw a line between the two
     * queue size.
     */
    private long averageQueueSize(long start, long end, List<SolverStatistics.InstantStatistic> iStats) {
        double area = 0;

        SolverStatistics.InstantStatistic last = null;
        for (SolverStatistics.InstantStatistic i : iStats) {
            if (i.queueSize() > 0) {
                if (last != null) {
                    // sum of a rectangle of side (t2 - t1) x q1
                    // and triangle of side (t2 - t1) x (q2 - q1)
                    area += (i.time() - last.time()) * (last.queueSize() + i.queueSize()) / 2d;
                }

                last = i;
            }
        }

        return (long) (area / (end - start));
    }

    private long nodeExploredPerSeconds(long start, long end, List<SolverStatistics.InstantStatistic> iStats) {
        SolverStatistics.InstantStatistic first = iStats.get(0);
        SolverStatistics.InstantStatistic last = iStats.get(iStats.size() - 1);

        // first.nodeExplored() usually returns 0
        return 1000L * (last.nodeExplored() - first.nodeExplored()) / (end - start);
    }

    private AttributedString[] statsToString(Integer i) {
        if (i == null || i < 0) {
            return PrettyTable.EMPTY;
        } else {
            return PrettyTable.wrap(i);
        }
    }

    @Override
    public String getName() {
        return "stats";
    }

    @Override
    public String getShortDescription() {
        return "Print stats about a solution";
    }

    @Override
    public String[] getUsage() {
        return new String[0];
    }

    @Override
    public void complete(LineReader reader, CommandLine.CommandSpec command, List<Candidate> candidates, CommandLine.OptionSpec option, String argument) {
        if (option != null && ArgsUtils.contains(option.getShortNames(), 'p')) {
            helper.addPackCandidates(candidates);
        }
    }
}
