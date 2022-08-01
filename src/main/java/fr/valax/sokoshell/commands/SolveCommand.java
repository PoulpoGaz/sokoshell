package fr.valax.sokoshell.commands;

import fr.valax.args.api.Command;
import fr.valax.args.api.Option;
import fr.valax.sokoshell.solver.*;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.Map;

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

    @Override
    protected int executeImpl(InputStream in, PrintStream out, PrintStream err) {
        List<Pack> packs = getPackMultiple(pack);

        if (packs.isEmpty()) {
            return SUCCESS;
        }

        List<Level> levels = new ArrayList<>();

        try {
            for (Pack pack : packs) {
                levels.addAll(getLevelMultiple(pack, this.levels));
            }
        } catch (InvalidArgument e) {
            e.printStackTrace(err);
            return FAILURE;
        }

        if (levels.isEmpty()) {
            return SUCCESS;
        }

        Map<String, Object> params = new HashMap<>();
        params.put(SolverParameters.TIMEOUT, timeout);

        Solver solver = BasicBrutalSolver.newDFSSolver();

        helper.addTask(solver, params, levels, toString(this.pack), this.levels);

        return Command.SUCCESS;
    }

    private String toString(String[] array) {
        if (array.length == 1) {
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
}
