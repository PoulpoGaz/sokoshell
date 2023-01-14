package fr.valax.sokoshell.commands.table;

import fr.valax.args.api.TypeConverter;
import fr.valax.args.api.VaArgs;
import fr.valax.args.utils.TypeException;
import fr.valax.sokoshell.SolverTask;
import fr.valax.sokoshell.TaskStatus;
import fr.valax.sokoshell.utils.Alignment;
import fr.valax.sokoshell.utils.PrettyColumn;
import fr.valax.sokoshell.utils.PrettyTable;
import fr.valax.sokoshell.utils.Utils;
import org.jline.utils.AttributedString;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Objects;

public class ListTasks extends TableCommand {

    @VaArgs(converter = ModeConverter.class,
            description =
                    """
                    Specify which type of tasks you want to see:
                    - none to see all tasks.
                    - a string starting by a '+' means that you wants to see all specified tasks type.
                    - a string starting by a '~' means that you wants to see all unspecified tasks type.
                    To specify (or no specify) a tasks you just add the first letter of the task type.
                    eg: +fp to see all finished and pending tasks
                        ~recs to not see running, error, cancelled, stopped tasks.
                    The first character is optional and is by default a '~'
                    """)
    private Mode[] mode;

    @Override
    protected int executeImpl(InputStream in, PrintStream out, PrintStream err) {
        Mode m;
        if (mode.length == 0) {
            m = new Mode();
            m.setAllAccepted();
        } else {
            m = Objects.requireNonNull(mode[0]);
        }


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

        for (SolverTask task : sokoshell().getTaskList().getTasks()) {
            if (accept(task, m)) {
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

    private boolean accept(SolverTask task, Mode m) {
        return switch (task.getTaskStatus()) {
            case PENDING -> m.withPending();
            case RUNNING -> m.withRunning();
            case ERROR -> m.withError();
            case STOPPED -> m.withStopped();
            case CANCELED -> m.withCancelled();
            case FINISHED -> m.withFinished();
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

    private static final class Mode {

        private boolean withPending;
        private boolean withRunning;
        private boolean withCancelled;
        private boolean withStopped;
        private boolean withError;
        private boolean withFinished;

        public void setAllAccepted() {
            setWithPending(true);
            setWithRunning(true);
            setWithCancelled(true);
            setWithStopped(true);
            setWithError(true);
            setWithFinished(true);
        }

        public boolean withPending() {
            return withPending;
        }

        public void setWithPending(boolean withPending) {
            this.withPending = withPending;
        }

        public boolean withRunning() {
            return withRunning;
        }

        public void setWithRunning(boolean withRunning) {
            this.withRunning = withRunning;
        }

        public boolean withCancelled() {
            return withCancelled;
        }

        public void setWithCancelled(boolean withCancelled) {
            this.withCancelled = withCancelled;
        }

        public boolean withStopped() {
            return withStopped;
        }

        public void setWithStopped(boolean withStopped) {
            this.withStopped = withStopped;
        }

        public boolean withError() {
            return withError;
        }

        public void setWithError(boolean withError) {
            this.withError = withError;
        }

        public boolean withFinished() {
            return withFinished;
        }

        public void setWithFinished(boolean withFinished) {
            this.withFinished = withFinished;
        }
    }

    private static class ModeConverter implements TypeConverter<Mode> {

        @Override
        public Mode convert(String value) throws TypeException {
            char first = value.charAt(0);

            Mode mode = new Mode();

            boolean v;
            int i = 0;
            if (first == '+') {
                v = true;

                i = 1;
            } else if (first == '~') {
                mode.setAllAccepted();
                v = false;

                i = 1;
            } else {
                mode.setAllAccepted();
                v = false;
            }


            for (; i < value.length(); i++) {
                set(mode, value.charAt(i), v);
            }


            return mode;
        }

        private void set(Mode mode, char c, boolean value) throws TypeException {
            switch (c) {
                case 'p', 'P' -> mode.setWithPending(value);
                case 'r', 'R' -> mode.setWithRunning(value);
                case 'c', 'C' -> mode.setWithCancelled(value);
                case 's', 'S' -> mode.setWithStopped(value);
                case 'e', 'E' -> mode.setWithError(value);
                case 'f', 'F' -> mode.setWithFinished(value);
                default -> throw new TypeException("Unknown mode: " + c + ". Valid one are: " +
                        "p (pending), r (running), c (cancelled), s (stopped), e (error) and f (finished) ");
            }
        }
    }
}
