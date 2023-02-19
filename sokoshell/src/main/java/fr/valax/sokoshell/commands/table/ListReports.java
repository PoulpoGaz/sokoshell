package fr.valax.sokoshell.commands.table;

import fr.poulpogaz.json.utils.Pair;
import fr.valax.args.CommandLine;
import fr.valax.args.api.Option;
import fr.valax.args.utils.ArgsUtils;
import fr.valax.interval.Interval;
import fr.valax.interval.ParseException;
import fr.valax.interval.Set;
import fr.valax.sokoshell.SolverTask;
import fr.valax.sokoshell.solver.ISolverStatistics;
import fr.valax.sokoshell.solver.Level;
import fr.valax.sokoshell.solver.Pack;
import fr.valax.sokoshell.solver.SolverReport;
import fr.valax.sokoshell.utils.Alignment;
import fr.valax.sokoshell.utils.PrettyColumn;
import fr.valax.sokoshell.utils.PrettyTable;
import fr.valax.sokoshell.utils.Utils;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
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

    @Option(names = {"R", "reports"}, hasArgument = true)
    private String reportIndex;

    @Option(names = {"s", "stats"})
    private boolean stats;

    @Option(names = {"e", "export-csv"}, hasArgument = true)
    private Path exportCSV;

    @Override
    public int executeImpl(InputStream in, PrintStream out, PrintStream err) throws InvalidArgument {
        if (index == null && column == null) {
            column = "Date";
        }

        List<Pair<SolverReport, Integer>> reports = getReports(err);
        if (reports == null) {
            return FAILURE;
        }

        if (exportCSV != null) {
            try {
                exportCSV(reports);
            } catch (IOException e) {
                e.printStackTrace(err);
                return FAILURE;
            }
        } else {
            print(out, err, reports);
        }

        return SUCCESS;
    }

    private List<Pair<SolverReport, Integer>> getReports(PrintStream err) throws InvalidArgument {
        Set reportsSet;
        try {
            if (reportIndex == null) {
                reportsSet = Interval.all();
            } else {
                reportsSet = parser.parse(reportIndex);
            }
        } catch (ParseException e) {
            throw new InvalidArgument(e);
        }

        if (taskIndex != null) {
            SolverTask task = sokoshell().getTaskList().getTask(taskIndex);

            if (task == null) {
                err.printf("Can't find task n°%d%n", taskIndex);
                return null;
            } else {
                List<SolverReport> reports = task.getSolutions();

                if (reports == null) {
                    err.println("This task is running or has no solution");
                    return null;
                } else {
                    List<Pair<SolverReport, Integer>> reportsWithIndex = new ArrayList<>();

                    for (SolverReport r : reports) {
                        int i = r.getLevel().indexOf(r);

                        if (i >= 0  // happen when the report was removed
                                && reportsSet.contains(i)) {
                            reportsWithIndex.add(new Pair<>(r, i));
                        }
                    }

                    return reportsWithIndex;
                }
            }
        } else {
            List<Level> levels = getLevels(levelIndex, packName);

            List<Pair<SolverReport, Integer>> solverReports = new ArrayList<>();
            for (Level level : levels) {
                for (int i = 0; i < level.numberOfSolverReport(); i++) {
                    if (reportsSet.contains(i)) {
                        solverReports.add(new Pair<>(level.getSolverReport(i), i));
                    }
                }
            }

            return solverReports;
        }
    }


    private void print(PrintStream out, PrintStream err, List<Pair<SolverReport, Integer>> solverReports) {
        if (solverReports.isEmpty()) {
            out.println("No report found");
            return;
        }

        PrettyTable table = new PrettyTable();

        PrettyColumn<String> packName = new PrettyColumn<>("Pack");
        PrettyColumn<Integer> level = new PrettyColumn<>("Level");
        PrettyColumn<Integer> report = new PrettyColumn<>("Report");
        PrettyColumn<String> status = new PrettyColumn<>("Status");
        PrettyColumn<String> solverName = new PrettyColumn<>("Solver");
        PrettyColumn<Integer> pushes = new PrettyColumn<>("Pushes");
        PrettyColumn<Integer> moves = new PrettyColumn<>("Moves");

        PrettyColumn<Long> date = new PrettyColumn<>("Date");
        date.setToString(start -> PrettyTable.wrap(Utils.formatDate(start)));

        PrettyColumn<Long> time = new PrettyColumn<>("Time");
        time.setToString(time1 -> PrettyTable.wrap(Utils.prettyDate(time1)));

        for (Pair<SolverReport, Integer> p : solverReports) {
            SolverReport s = p.getLeft();
            ISolverStatistics stats = s.getStatistics();

            packName.add(s.getParameters().getLevel().getPack().name());
            level.add(Alignment.RIGHT, s.getParameters().getLevel().getIndex() + 1);
            report.add(p.getRight());
            status.add(s.getStatus());
            solverName.add(s.getSolverName());
            pushes.add(Alignment.RIGHT, s.numberOfPushes());
            moves.add(Alignment.RIGHT, s.numberOfMoves());
            date.add(stats.timeStarted());
            time.add(Alignment.RIGHT, stats.runTime());
        }

        table.addColumn(packName);
        table.addColumn(level);
        table.addColumn(report);
        table.addColumn(status);
        table.addColumn(solverName);
        table.addColumn(pushes);
        table.addColumn(moves);
        table.addColumn(date);
        table.addColumn(time);

        printTable(out, err, table);
        out.printf("%nNumber of reports: %d%n", solverReports.size());

        if (stats) {
            out.println();
            // use stream api to simplify printStats
            printStats(out, solverReports.stream()
                    .map(Pair::getLeft)
                    .filter(SolverReport::isSolved)
                    .collect(Collectors.toList()));
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
            ISolverStatistics stats = report.getStatistics();

            if (stats.totalStateExplored() >= 0) {
                int state = stats.totalStateExplored();
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

            long time = stats.runTime();
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


        out.println("Below statics are valid for solved levels");

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

    private void exportCSV(List<Pair<SolverReport, Integer>> reports) throws IOException {
        try (BufferedWriter bw = Files.newBufferedWriter(exportCSV)) {
            // wow
            bw.write("Pack,Level,Report,Solver,Status,Date,Moves,Pushes,Time (sec),Width,Height,Floors,Crates,LowerBound");
            bw.newLine();

            for (Pair<SolverReport, Integer> pair : reports) {
                SolverReport report = pair.getLeft();
                Level level = report.getLevel();

                bw.write(quote(level.getPack().name()));
                bw.write(',');
                bw.write(Integer.toString(level.getIndex()));
                bw.write(',');
                bw.write(pair.getRight().toString());
                bw.write(',');
                bw.write(report.getSolverName());
                bw.write(',');
                bw.write(report.getStatus());
                bw.write(',');

                ISolverStatistics stats = report.getStatistics();
                bw.write(Utils.formatDate(stats.timeStarted()));
                bw.write(',');
                bw.write(Integer.toString(report.numberOfMoves()));
                bw.write(',');
                bw.write(Integer.toString(report.numberOfPushes()));
                bw.write(',');
                bw.write(Long.toString(stats.runTime() / 1000));
                bw.write(',');
                bw.write(Integer.toString(level.getWidth()));
                bw.write(',');
                bw.write(Integer.toString(level.getHeight()));
                bw.write(',');
                bw.write(Integer.toString(level.getNumberOfNonWalls()));
                bw.write(',');
                bw.write(Integer.toString(level.getNumberOfCrates()));
                bw.write(',');
                bw.write(Integer.toString(stats.lowerBound()));
                bw.newLine();
            }
        }
    }

    private String quote(String str) {
        if (str.contains(",")) {
            StringBuilder sb = new StringBuilder();

            sb.append('"');
            for (int i = 0; i < str.length(); i++) {
                if (str.charAt(i) == '"') {
                    sb.append("\"\""); // double quote!
                } else {
                    sb.append(str.charAt(i));
                }
            }
            sb.append('"');

            return sb.toString();
        } else {
            return str;
        }
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
    public void complete(LineReader reader, String commandString, CommandLine.CommandSpec command, List<Candidate> candidates, CommandLine.OptionSpec option, String argument) {
        if (option != null && ArgsUtils.contains(option.getShortNames(), 'p')) {
            sokoshell().addPackCandidates(candidates);
        }
    }
}