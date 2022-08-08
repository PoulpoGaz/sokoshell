package fr.valax.sokoshell.commands;

import fr.valax.args.api.Option;
import fr.valax.sokoshell.SolverTask;
import fr.valax.sokoshell.TaskList;
import fr.valax.sokoshell.TaskStatus;

import java.io.InputStream;
import java.io.PrintStream;

public class MoveTaskCommand extends AbstractCommand {

    @Option(names = {"t", "task-index"}, hasArgument = true, optional = false)
    private int taskIndex;

    @Option(names = {"p", "position"}, hasArgument = true, optional = false)
    private int position;

    @Option(names = {"s", "swap"})
    private boolean swap;

    @Override
    protected int executeImpl(InputStream in, PrintStream out, PrintStream err) {
        TaskList list = helper.getTaskList();

        synchronized (list) {
            SolverTask task = list.getTask(taskIndex);

            if (task == null) {
                err.println("Index out of bounds");
                return FAILURE;
            } else if (task.getTaskStatus() != TaskStatus.PENDING) {
                err.println("Task isn't pending");
                return FAILURE;
            } else if (swap) {
                list.swap(task, position);
            } else {
                list.move(task, position);
            }
        }

        return SUCCESS;
    }

    @Override
    public String getName() {
        return "move-task";
    }

    @Override
    public String getShortDescription() {
        return "Move a task";
    }

    @Override
    public String[] getUsage() {
        return new String[0];
    }
}
