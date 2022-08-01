package fr.valax.sokoshell.commands.table;

import fr.valax.args.api.Option;
import fr.valax.sokoshell.SolverTask;
import fr.valax.sokoshell.TaskStatus;
import fr.valax.sokoshell.utils.Alignment;
import fr.valax.sokoshell.utils.PrettyColumn;
import fr.valax.sokoshell.utils.PrettyTable;
import org.jline.utils.AttributedString;

import java.io.InputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

public class ListTasks extends TableCommand {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    @Option(names = {"P", "without-pending"})
    private boolean withoutPending;

    @Option(names = {"F", "without-finished"})
    private boolean withoutFinished;

    @Option(names = {"R", "without-running"})
    private boolean withoutRunning;

    @Override
    protected int executeImpl(InputStream in, PrintStream out, PrintStream err) {
        PrettyTable table = new PrettyTable();

        PrettyColumn<TaskStatus> status = new PrettyColumn<>("Status");
        PrettyColumn<Integer> taskIndex = new PrettyColumn<>("n°");
        taskIndex.setToString((i) -> new AttributedString[] {new AttributedString("#" + i)});

        PrettyColumn<String> pack = new PrettyColumn<>("Pack");
        PrettyColumn<String> level = new PrettyColumn<>("Level");

        PrettyColumn<Long> requestedAt = new PrettyColumn<>("Requested at");
        requestedAt.setToString(this::toString);

        PrettyColumn<Long> startedAt = new PrettyColumn<>("Started at");
        startedAt.setToString(this::toString);

        PrettyColumn<Long> finishedAt = new PrettyColumn<>("Finished at");
        finishedAt.setToString(this::toString);

        if (!withoutFinished) {
            for (SolverTask task : helper.getFinishedTasks()) {
                addTask(status, taskIndex, pack, level, requestedAt, startedAt, finishedAt, task);
            }
        }

        if (!withoutRunning) {
            SolverTask running = helper.getRunningTask();
            if (running != null) {
                addTask(status, taskIndex, pack, level, requestedAt, startedAt, finishedAt, running);
            }
        }

        if (!withoutPending) {
            for (SolverTask task : helper.getPendingTasks()) {
                addTask(status, taskIndex, pack, level, requestedAt, startedAt, finishedAt, task);
            }
        }

        table.addColumn(status);
        table.addColumn(taskIndex);
        table.addColumn(pack);
        table.addColumn(level);
        table.addColumn(requestedAt);
        table.addColumn(startedAt);
        table.addColumn(finishedAt);

        printTable(out, err, table);

        return 0;
    }

    private void addTask(PrettyColumn<TaskStatus> status,
                         PrettyColumn<Integer> taskIndex,
                         PrettyColumn<String> pack,
                         PrettyColumn<String> level,
                         PrettyColumn<Long> requestedAt,
                         PrettyColumn<Long> startedAt,
                         PrettyColumn<Long> finishedAt,
                         SolverTask task) {
        status.add(task.getTaskStatus());
        taskIndex.add(Alignment.RIGHT, task.getTaskIndex());
        pack.add(task.getPack());
        level.add(task.getLevel());
        requestedAt.add(task.getRequestedAt());
        startedAt.add(task.getStartedAt());
        finishedAt.add(task.getFinishedAt());
    }

    private AttributedString[] toString(long date) {
        String str;
        if (date < 0) {
            str = "";
        } else {
            str = DATE_FORMAT.format(Date.from(Instant.ofEpochMilli(date)));
        }

        return new AttributedString[]{new AttributedString(str)};
    }

    @Override
    public String getName() {
        return "tasks";
    }

    @Override
    public String getShortDescription() {
        return "list all tasks";
    }

    @Override
    public String[] getUsage() {
        return new String[0];
    }
}
