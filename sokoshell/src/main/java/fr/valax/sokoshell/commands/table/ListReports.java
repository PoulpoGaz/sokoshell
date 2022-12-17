package fr.valax.sokoshell.commands.table;

import fr.valax.args.api.Option;
import fr.valax.args.utils.ArgsUtils;
import fr.valax.sokoshell.SolverTask;
import fr.valax.sokoshell.solver.*;
import fr.valax.sokoshell.utils.Alignment;
import fr.valax.sokoshell.utils.PrettyColumn;
import fr.valax.sokoshell.utils.PrettyTable;
import fr.valax.sokoshell.utils.Utils;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ListReports extends TableCommand {

    @Option(names = {"p", "pack"}, hasArgument = true, argName = "Pack name", allowDuplicate = true)
    private String[] packName;

    @Option(names = {"l", "level"}, hasArgument = true, argName = "Level index")
    private String levelIndex;

    @Option(names = {"t", "task-index"}, hasArgument = true, argName = "Task index")
    private Integer taskIndex;

    @Option(names = {"s", "stats"})
    private boolean stats;

    @Override
    public int executeImpl(InputStream in, PrintStream out, PrintStream err) throws InvalidArgument {
        if (taskIndex != null) {
            SolverTask task = helper.getTaskList().getTask(taskIndex);

            if (task == null) {
                err.printf("Can't find task n°%d%n", taskIndex);
                return FAILURE;
            } else if (task.getSolutions() == null) {
                err.println("This task is running or has no solution");
                return FAILURE;
            } else {
                list(out, err, task.getSolutions());
            }
        } else {
            List<Level> levels = getLevels(levelIndex, packName);

            List<SolverReport> solverReports = new ArrayList<>();
            for (Level level : levels) {
                solverReports.addAll(level.getSolverReports());
            }

            list(out, err, solverReports);
        }

        return SUCCESS;
    }

    private void list(PrintStream out, PrintStream err, List<SolverReport> solverReports) {
        if (solverReports.isEmpty()) {
            out.println("No report found");
            return;
        }

        PrettyTable table = new PrettyTable();

        PrettyColumn<String> packName = new PrettyColumn<>("Pack");
        PrettyColumn<Integer> index = new PrettyColumn<>("Index");
        PrettyColumn<String> status = new PrettyColumn<>("Status");
        PrettyColumn<String> solverName = new PrettyColumn<>("Solver");
        PrettyColumn<Integer> pushes = new PrettyColumn<>("Pushes");
        PrettyColumn<Integer> moves = new PrettyColumn<>("Moves");

        PrettyColumn<Long> date = new PrettyColumn<>("Date");
        date.setToString(start -> PrettyTable.wrap(Utils.formatDate(start)));

        PrettyColumn<Long> time = new PrettyColumn<>("Time");
        time.setToString(time1 -> PrettyTable.wrap(Utils.prettyDate(time1)));

        for (SolverReport s : solverReports) {
            SolverStatistics stats = s.getStatistics();

            packName.add(s.getParameters().getLevel().getPack().name());
            index.add(Alignment.RIGHT, s.getParameters().getLevel().getIndex() + 1);
            status.add(s.getStatus());
            solverName.add(s.getSolverName());
            pushes.add(Alignment.RIGHT, s.numberOfPushes());
            moves.add(Alignment.RIGHT, s.numberOfMoves());
            date.add(stats.getTimeStarted());
            time.add(Alignment.RIGHT, stats.getTimeEnded() - stats.getTimeStarted());
        }

        table.addColumn(packName);
        table.addColumn(index);
        table.addColumn(status);
        table.addColumn(solverName);
        table.addColumn(pushes);
        table.addColumn(moves);
        table.addColumn(date);
        table.addColumn(time);

        printTable(out, err, table);
        out.printf("%nNumber of solutions: %d%n", solverReports.size());

        if (stats) {
            out.println();
            // use stream api to simplify printStats
            printStats(out, solverReports.stream().filter(SolverReport::isSolved).collect(Collectors.toList()));
        }
    }

    /**
     * Very repetitive method...
     */
    private void printStats(PrintStream out, List<SolverReport> solverReports) {
        int minState = Integer.MAX_VALUE;
        SolverReport minStateReport = null;
        int maxState = 0;
        SolverReport maxStateReport = null;
        long stateSum = 0; // a long is probably is good idea here
        // number of report that have precise statistics about the number of state explored
        int numReportWithState = 0;

        long minTime = Integer.MAX_VALUE;
        SolverReport minTimeReport = null;
        long maxTime = 0;
        SolverReport maxTimeReport = null;
        long timeSum = 0;

        int minMoves = Integer.MAX_VALUE;
        SolverReport minMovesReport = null;
        int maxMoves = 0;
        SolverReport maxMovesReport = null;
        int movesSum = 0;

        int minPushes = Integer.MAX_VALUE;
        SolverReport minPushesReport = null;
        int maxPushes = 0;
        SolverReport maxPushesReport = null;
        int pushesSum = 0;

        for (SolverReport report : solverReports) {
            SolverStatistics stats = report.getStatistics();

            List<SolverStatistics.InstantStatistic> iStats = stats.getStatistics();
            if (iStats != null && iStats.size() > 0) {
                int state = iStats.get(iStats.size() - 1).nodeExplored();
                if (state < minState) {
                    minState = state;
                    minStateReport = report;
                }
                if (state > maxState) {
                    maxState = state;
                    maxStateReport = report;
                }
                stateSum += state;

                numReportWithState++;
            }

            long time = stats.getTimeEnded() - stats.getTimeStarted();
            if (time < minMoves) {
                minTime = time;
                minTimeReport = report;
            }
            if (time > maxMoves) {
                maxTime = time;
                maxTimeReport = report;
            }
            timeSum += time;

            if (report.numberOfMoves() < minMoves) {
                minMoves = report.numberOfMoves();
                minMovesReport = report;
            }
            if (report.numberOfMoves() > maxMoves) {
                maxMoves = report.numberOfMoves();
                maxMovesReport = report;
            }
            movesSum += report.numberOfMoves();

            if (report.numberOfPushes() < minPushes) {
                minPushes = report.numberOfPushes();
                minPushesReport = report;
            }
            if (report.numberOfPushes() > maxPushes) {
                maxPushes = report.numberOfPushes();
                maxPushesReport = report;
            }
            pushesSum += report.numberOfPushes();
        }


        out.println("Statics below are valid for solved levels");

        // finally print!
        if (numReportWithState > 0) {
            out.println("* State statistics *");
            out.printf("Total number of state explored: %d%n", stateSum);
            out.printf("Average state explored per report: %d%n", stateSum / numReportWithState);

            Level l = minStateReport.getLevel();
            out.printf("Level with the least explored state: %d - %s #%d%n", minState, l.getPack().name(), l.getIndex() + 1);

            l = maxStateReport.getLevel();
            out.printf("Level with the most explored state: %d - %s #%d%n", maxState, l.getPack().name(), l.getIndex() + 1);
            out.println();
        }


        out.println("* Time statistics *");
        out.printf("Total run time: %s%n", Utils.prettyDate(timeSum));
        out.printf("Average run time per report: %s%n", Utils.prettyDate(timeSum / solverReports.size()));

        Level l = minTimeReport.getLevel();
        out.printf("Fastest solved level: in %s - %s #%d%n", Utils.prettyDate(minTime), l.getPack().name(), l.getIndex() + 1);

        l = maxTimeReport.getLevel();
        out.printf("Slowest solved level: in %s - %s #%d%n", Utils.prettyDate(maxTime), l.getPack().name(), l.getIndex() + 1);
        out.println();


        out.println("* Solution length (moves) *");
        out.printf("Average solution length per report: %d%n", movesSum / solverReports.size());

        l = minMovesReport.getLevel();
        out.printf("Level with shortest solution: %s moves - %s #%d%n", minMoves, l.getPack().name(), l.getIndex() + 1);

        l = maxMovesReport.getLevel();
        out.printf("Level with longest solution: %s moves - %s #%d%n", maxMoves, l.getPack().name(), l.getIndex() + 1);
        out.println();


        out.println("* Solution length (pushes) *");
        out.printf("Average solution length per report: %d%n", pushesSum / solverReports.size());

        l = minPushesReport.getLevel();
        out.printf("Level with shortest solution: %s pushes - %s #%d%n", minPushes, l.getPack().name(), l.getIndex() + 1);

        l = maxPushesReport.getLevel();
        out.printf("Level with longest solution: %s pushes - %s #%d%n", maxPushes, l.getPack().name(), l.getIndex() + 1);
    }

    @Override
    public String getName() {
        return "reports";
    }

    @Override
    public String getShortDescription() {
        return "List all solver reports of a level, a pack or a task";
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