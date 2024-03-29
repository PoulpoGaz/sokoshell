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
import fr.valax.sokoshell.solver.SolverReport;
import fr.valax.sokoshell.utils.Alignment;
import fr.valax.sokoshell.utils.PrettyColumn;
import fr.valax.sokoshell.utils.PrettyTable;
import fr.valax.sokoshell.utils.Utils;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
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
                    .collect(Collectors.toList()));
        }
    }

    /**
     * Very repetitive method...
     */
    private void printStats(PrintStream out, List<SolverReport> solverReports) {
        Statistic stateStat = new Statistic();
        Statistic statePerSecondStat = new Statistic();
        Statistic timeStat = new Time();
        Statistic moveStat = new Statistic();
        Statistic pushStat = new Statistic();

        for (SolverReport report : solverReports) {
            ISolverStatistics stats = report.getStatistics();

            // states
            if (stats.totalStateExplored() >= 0) {
                stateStat.add(report, stats.totalStateExplored());
            }

            if (stats.stateExploredPerSeconds() >= 0) {
                statePerSecondStat.add(report, stats.stateExploredPerSeconds());
            }

            timeStat.add(report, stats.runTime());
            if (report.isSolved()) {
                moveStat.add(report, report.numberOfMoves());
                pushStat.add(report, report.numberOfPushes());
            }
        }

        out.println("* State statistics *");
        stateStat.print(out,
                "Total number of state explored: %s%n",
                "Average state explored per report: %s%n",
                "Level with the least explored state: %s - %s #%d%n",
                "Level with the most explored state: %s - %s #%d%n");
        statePerSecondStat.print(out, null, "Average state explored per second: %s%n", null, null);

        out.println();
        out.println("* Time statistics *");
        timeStat.print(out,
                "Total run time: %s%n",
                "Average run time: %s%n",
                "Fastest solved level: in %s - %s #%d%n",
                "Slowest solved level: in %s - %s #%d%n");

        out.println();
        out.println("* Solution length (moves) *");
        moveStat.print(out,
                null,
                "Average solution length: %s%n",
                "Level with the shortest solution: %s moves - %s #%d%n",
                "Level with the longest solution: %s moves - %s #%d%n");

        out.println();
        out.println("* Solution length (pushes) *");
        pushStat.print(out,
                null,
                "Average solution length: %s%n",
                "Level with the shortest solution: %s pushes - %s #%d%n",
                "Level with the longest solution: %s pushes - %s #%d%n");
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

    private static class Statistic {

        protected long min = Integer.MAX_VALUE;
        protected SolverReport minReport;
        protected long max = Integer.MIN_VALUE;
        protected SolverReport maxReport;
        protected long sum;

        protected int num; // number of elements

        public void add(SolverReport r, long l) {
            num++;
            sum += l;

            if (l < min) {
                minReport = r;
                min = l;
            }
            if (l > max) {
                maxReport = r;
                max = l;
            }
        }

        public void print(PrintStream out,
                          String totalFormat, String averageFormat,
                          String minFormat, String maxFormat) {
            if (sum == 0) {
                return;
            }

            if (totalFormat != null) {
                out.printf(totalFormat, numberToString(sum));
            }
            if (averageFormat != null) {
                out.printf(averageFormat, numberToString(getAverage()));
            }
            if (minFormat != null) {
                out.printf(minFormat, numberToString(min), minReport.getPack().name(), minReport.getLevel().getIndex() + 1);
            }
            if (maxFormat != null) {
                out.printf(maxFormat, numberToString(max), maxReport.getPack().name(), maxReport.getLevel().getIndex() + 1);
            }
        }

        protected String numberToString(long number) {
            return Long.toString(number);
        }

        public long getMin() {
            return min;
        }

        public SolverReport getMinReport() {
            return minReport;
        }

        public long getMax() {
            return max;
        }

        public SolverReport getMaxReport() {
            return maxReport;
        }

        public long getSum() {
            return sum;
        }

        public int getNum() {
            return num;
        }

        public long getAverage() {
            return sum / num;
        }
    }

    private static class Time extends Statistic {

        @Override
        public void add(SolverReport r, long l) {
            num++;
            sum += l;

            if (r.isSolved()) {
                if (l < min) {
                    minReport = r;
                    min = l;
                }
                if (l > max) {
                    maxReport = r;
                    max = l;
                }
            }
        }

        @Override
        protected String numberToString(long number) {
            return Utils.prettyDate(number);
        }
    }
}