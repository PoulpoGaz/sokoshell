package fr.valax.sokoshell.commands.table;

import fr.valax.args.api.Option;
import fr.valax.sokoshell.SolverTask;
import fr.valax.sokoshell.TaskStatus;
import fr.valax.sokoshell.utils.Alignment;
import fr.valax.sokoshell.utils.PrettyColumn;
import fr.valax.sokoshell.utils.PrettyTable;
import fr.valax.sokoshell.utils.Utils;
import org.jline.utils.AttributedString;

import java.io.InputStream;
import java.io.PrintStream;

public class ListTasks extends TableCommand {

    @Option(names = {"P", "without-pending"})
    private boolean withoutPending;

    @Option(names = {"R", "without-running"})
    private boolean withoutRunning;

    @Option(names = {"C", "without-cancelled"})
    private boolean withoutCancelled;

    @Option(names = {"S", "without-stopped"})
    private boolean withoutStopped;

    @Option(names = {"E", "without-error"})
    private boolean withoutError;

    @Option(names = {"F", "without-finished"})
    private boolean withoutFinished;

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

        PrettyColumn<SolverTask> progress = new PrettyColumn<>("Progress");
        progress.setToString(this::progressToString);

       for (SolverTask task : helper.getTaskList().getTasks()) {
            if (accept(task)) {
                status.add(task.getTaskStatus());
                taskIndex.add(Alignment.RIGHT, task.getTaskIndex());
                pack.add(task.getPack());
                level.add(task.getLevel());
                requestedAt.add(task.getRequestedAt());
                startedAt.add(task.getStartedAt());
                finishedAt.add(task.getFinishedAt());
                progress.add(task);
            }
       }

        table.addColumn(status);
        table.addColumn(taskIndex);
        table.addColumn(pack);
        table.addColumn(level);
        table.addColumn(requestedAt);
        table.addColumn(startedAt);
        table.addColumn(finishedAt);
        table.addColumn(progress);

        printTable(out, err, table);

        return 0;
    }

    private boolean accept(SolverTask task) {
        return switch (task.getTaskStatus()) {
            case PENDING -> !withoutPending;
            case RUNNING -> !withoutRunning;
            case ERROR -> !withoutError;
            case STOPPED -> !withoutStopped;
            case CANCELED -> !withoutCancelled;
            case FINISHED -> !withoutFinished;
        };
    }

    private AttributedString[] toString(long date) {
        if (date < 0) {
            return PrettyTable.EMPTY;
        } else {
            return PrettyTable.wrap(Utils.formatDate(date));
        }
    }

    private AttributedString[] progressToString(SolverTask task) {
        if (task.getTaskStatus() == TaskStatus.FINISHED ||
                task.getTaskStatus() == TaskStatus.PENDING ||
                task.getTaskStatus() == TaskStatus.CANCELED) {
            return PrettyTable.EMPTY;
        } else {
            return PrettyTable.wrap("%d/%d".formatted(task.getCurrentLevel() + 1, task.size()));
        }
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
