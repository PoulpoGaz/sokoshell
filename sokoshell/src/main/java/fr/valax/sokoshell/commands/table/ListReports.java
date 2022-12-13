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

public class ListReports extends TableCommand {

    @Option(names = {"p", "pack"}, hasArgument = true, argName = "Pack name", allowDuplicate = true)
    private String[] packName;

    @Option(names = {"l", "level"}, hasArgument = true, argName = "Level index")
    private String levelIndex;

    @Option(names = {"t", "task-index"}, hasArgument = true, argName = "Task index")
    private Integer taskIndex;

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
                printTable(out, err, task.getSolutions());
            }
        } else {
            List<Level> levels = getLevels(levelIndex, packName);

            List<SolverReport> solverReports = new ArrayList<>();
            for (Level level : levels) {
                solverReports.addAll(level.getSolverReports());
            }

            printTable(out, err, solverReports);
        }

        return SUCCESS;
    }

    private void printTable(PrintStream out, PrintStream err, List<SolverReport> solverReports) {
        PrettyTable table = new PrettyTable();

        PrettyColumn<String> packName = new PrettyColumn<>("Pack");
        PrettyColumn<Integer> index = new PrettyColumn<>("Index");
        PrettyColumn<String> status = new PrettyColumn<>("Status");
        PrettyColumn<SolverType> solverType = new PrettyColumn<>("Solver");
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
            solverType.add(s.getType());
            pushes.add(Alignment.RIGHT, s.numberOfPushes());
            moves.add(Alignment.RIGHT, s.numberOfMoves());
            date.add(stats.getTimeStarted());
            time.add(Alignment.RIGHT, stats.getTimeEnded() - stats.getTimeStarted());
        }

        table.addColumn(packName);
        table.addColumn(index);
        table.addColumn(status);
        table.addColumn(solverType);
        table.addColumn(pushes);
        table.addColumn(moves);
        table.addColumn(date);
        table.addColumn(time);

        printTable(out, err, table);
        out.printf("%nNumber of solutions: %d%n", solverReports.size());
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