package fr.valax.sokoshell.commands;

import fr.valax.args.api.Option;
import fr.valax.sokoshell.SolverTask;
import fr.valax.sokoshell.TaskList;
import fr.valax.sokoshell.TaskStatus;

import java.io.InputStream;
import java.io.PrintStream;

public class CancelCommand extends AbstractCommand {

    @Option(names = {"t", "task-index"}, hasArgument = true, argName = "Task index")
    private Integer taskIndex;

    @Option(names = {"A", "all"})
    private boolean all;

    @Override
    protected int executeImpl(InputStream in, PrintStream out, PrintStream err) {
        TaskList list = helper.getTaskList();

        synchronized (list) {
            if (all) {
                list.stopAll();
            } else if (taskIndex == null) {
                SolverTask task = list.getRunningTask();

                if (task != null) {
                    stop(task, err);
                }

            } else {
                SolverTask task = list.getTask(taskIndex);

                if (task != null) {
                    stop(task, err);
                } else {
                    err.println("Invalid index");
                }
            }
        }

        return 0;
    }

    private void stop(SolverTask task, PrintStream err) {
        if (task.getTaskStatus() == TaskStatus.PENDING || task.getTaskStatus() == TaskStatus.RUNNING) {
            task.stop();
        } else {
            err.println("This is task is already finished");
        }
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
