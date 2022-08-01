package fr.valax.sokoshell.commands;

import fr.valax.args.api.Option;
import fr.valax.sokoshell.SolverTask;
import fr.valax.sokoshell.TaskStatus;

import java.io.InputStream;
import java.io.PrintStream;

public class CancelCommand extends AbstractCommand {

    @Option(names = {"t", "task-index"}, hasArgument = true, argName = "Task index")
    private int taskIndex;

    @Override
    protected int executeImpl(InputStream in, PrintStream out, PrintStream err) {
        SolverTask task = helper.getTask(taskIndex);

        if (task != null) {
            if (task.getTaskStatus() == TaskStatus.PENDING) {
                task.cancel();
                helper.moveToFinished(task);
            } else {
                err.println("This is not a pending task");
            }
        }

        return 0;
    }

    @Override
    public String getName() {
        return "cancel";
    }

    @Override
    public String getShortDescription() {
        return "cancel a pending task";
    }

    @Override
    public String[] getUsage() {
        return new String[0];
    }
}
