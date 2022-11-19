package fr.valax.sokoshell.commands.table;

import fr.valax.args.api.Command;
import fr.valax.args.api.Option;
import fr.valax.args.utils.ArgsUtils;
import fr.valax.sokoshell.solver.Level;
import fr.valax.sokoshell.solver.SolverParameters;
import fr.valax.sokoshell.solver.SolverReport;
import fr.valax.sokoshell.solver.SolverStatistics;
import fr.valax.sokoshell.utils.Alignment;
import fr.valax.sokoshell.utils.PrettyColumn;
import fr.valax.sokoshell.utils.PrettyTable;
import fr.valax.sokoshell.utils.Utils;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;
import org.jline.utils.AttributedString;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;

public class StatsCommand extends TableCommand {

    @Option(names = {"p", "pack"}, hasArgument = true, argName = "Pack name")
    protected String pack;

    @Option(names = {"i", "index"}, hasArgument = true, argName = "Level index")
    protected Integer index;

    @Option(names = {"s", "solution"}, hasArgument = true, argName = "Solution index")
    private Integer solution;

    @Override
    protected int executeImpl(InputStream in, PrintStream out, PrintStream err) throws InvalidArgument {
        Level l = getLevel(pack, index);

        if (!l.hasReport()) {
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
            s = l.getLastReport();
        }

        SolverStatistics stats = s.getStatistics();
        if (stats != null) {
            printStats(stats, out, err);
        }

        out.printf("Status: %s. Solver: %s%n", s.getStatus(), s.getType());

        SolverParameters params = s.getParameters();
        out.printf("Level: %s - %d%n", params.getLevel().getPack().name(), params.getLevel().getIndex());

        Map<String, Object> map = params.getParameters();
        if (!map.isEmpty()) {
            out.println("Parameters:");

            for (Map.Entry<String, Object> param : map.entrySet()) {
                out.printf("%s = %s%n", param.getKey(), param.getValue());
            }
        }

        return Command.SUCCESS;
    }

    private void printStats(SolverStatistics stats, PrintStream out, PrintStream err) {
        long start = stats.getTimeStarted();
        long end = stats.getTimeEnded();

        List<SolverStatistics.InstantStatistic> iStats = stats.getStatistics();

        if (iStats != null && iStats.size() > 0) {
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

        out.printf("Started at %s. Finished at %s. Run time: %s%n",
                Utils.formatDate(start),
                Utils.formatDate(end),
                Utils.prettyDate(end - start));
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
    public void completeOption(LineReader reader, String argument, List<Candidate> candidates, Option option) {
        if (ArgsUtils.contains(option.names(), "p")) {
            helper.addPackCandidates(candidates);
        }
    }
}
