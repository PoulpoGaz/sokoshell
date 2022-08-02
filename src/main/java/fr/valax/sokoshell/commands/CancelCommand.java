package fr.valax.sokoshell.commands;

import fr.valax.args.api.Option;
import fr.valax.sokoshell.SolverTask;
import fr.valax.sokoshell.TaskList;
import fr.valax.sokoshell.TaskStatus;

import java.io.InputStream;
import java.io.PrintStream;

public class CancelCommand extends AbstractCommand {

    @Option(names = {"t", "task-index"}, hasArgument = true, argName = "Task index")
    private int taskIndex;

    @Option(names = {"A", "all"})
    private boolean all;

    @Override
    protected int executeImpl(InputStream in, PrintStream out, PrintStream err) {
        if (all) {
            helper.getTaskList().stopAll();
        } else {
            TaskList list = helper.getTaskList();
            SolverTask task = list.getTask(taskIndex);

            if (task != null) {
                if (task.getTaskStatus() == TaskStatus.PENDING || task.getTaskStatus() == TaskStatus.RUNNING) {
                    task.stop();
                } else {
                    err.println("This is task is already finished");
                }
            } else {
                err.println("Invalid index");
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
