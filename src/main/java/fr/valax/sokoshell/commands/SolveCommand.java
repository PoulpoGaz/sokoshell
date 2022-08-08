package fr.valax.sokoshell.commands;

import fr.valax.args.api.Command;
import fr.valax.args.api.Option;
import fr.valax.args.utils.ArgsUtils;
import fr.valax.sokoshell.SolverTask;
import fr.valax.sokoshell.TaskList;
import fr.valax.sokoshell.TaskStatus;
import fr.valax.sokoshell.solver.*;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Map;
import java.util.*;

/**
 * @author PoulpoGaz
 */
public class SolveCommand extends AbstractCommand {

    @Option(names = {"p", "pack"}, hasArgument = true, argName = "Pack name", allowDuplicate = true)
    protected String[] pack;

    @Option(names = {"l", "levels"}, hasArgument = true, argName = "Levels")
    protected String levels;

    @Option(names = {"t", "timeout"}, hasArgument = true, argName = "Timeout", defaultValue = "-1", description = "in ms")
    private long timeout;

    @Option(names = {"P", "position"}, hasArgument = true)
    private Integer position;

    @Option(names = {"T", "haikyu", "to-the-top"}, description = "equivalent to --position 0")
    private boolean toTheTop;

    @Option(names = {"w", "wait"}, description = "wait until this task and task with higher position finish")
    private boolean waitUntilFinished;

    @Option(names = {"s", "split"}, description = "split all levels in different tasks")
    private boolean split;

    @Override
    protected int executeImpl(InputStream in, PrintStream out, PrintStream err) {
        Collection<Pack> packs = getPackMultiple(pack);

        if (packs.isEmpty()) {
            return SUCCESS;
        }

        List<Level> levels = new ArrayList<>();

        try {
            for (Pack pack : packs) {
                levels.addAll(getLevelMultiple(pack, this.levels));
            }
        } catch (InvalidArgument e) {
            e.print(err, true);
            return FAILURE;
        }

        if (levels.isEmpty()) {
            return SUCCESS;
        }

        Map<String, Object> params = new HashMap<>();
        params.put(SolverParameters.TIMEOUT, timeout);

        Solver solver = BasicBrutalSolver.newDFSSolver();

        SolverTask lastTask = null;
        if (split) {
            for (Level level : levels) {
                lastTask = newTask(solver, params, packs, List.of(level));
            }
        } else {
            lastTask = newTask(solver, params, packs, levels);
        }

        if (waitUntilFinished) {
            while (lastTask.getTaskStatus() == TaskStatus.RUNNING) {
                Thread.onSpinWait();
            }
        }

        return Command.SUCCESS;
    }

    private SolverTask newTask(Solver solver, Map<String, Object> params, Collection<Pack> packs, List<Level> levels) {
        SolverTask task = new SolverTask(solver, params, levels, toString(packs, this.pack), this.levels);
        TaskList list = helper.getTaskList();

        if (toTheTop) {
            list.offerTask(task, 0);
        } else if (position != null) {
            list.offerTask(task, position);
        } else {
            list.offerTask(task);
        }

        return task;
    }

    private String toString(Collection<Pack> packs, String[] array) {
        if (packs.size() == 1 || (array == null && !packs.isEmpty())) {
            if (packs instanceof List<Pack> list) {
                return list.get(0).name();
            } else {
                return packs.iterator()
                        .next()
                        .name();
            }
        } else if (array == null) {
            throw new IllegalStateException();
        } else if (array.length == 1) {
            return array[0];
        } else {
            return Arrays.toString(array);
        }
    }

    @Override
    public String getName() {
        return "solve";
    }

    @Override
    public String getShortDescription() {
        return "Solve a sokoban";
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
