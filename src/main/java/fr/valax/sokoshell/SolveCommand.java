package fr.valax.sokoshell;

import fr.valax.args.api.Option;
import fr.valax.sokoshell.solver.*;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author PoulpoGaz
 */
public class SolveCommand extends LevelCommand {

    @Option(names = {"t", "timeout"}, hasArgument = true, argName = "Timeout", defaultValue = "-1")
    private long timeout;

    @Override
    protected int executeImpl(InputStream in, PrintStream out, PrintStream err) {
        Level l;
        try {
            Pack pack = getPack(name);
            l = getLevel(pack, index);

        } catch (InvalidArgument e) {
            e.print(err, true);
            return FAILURE;
        }

        Map<String, Object> params = new HashMap<>();
        params.put(SolverParameters.TIMEOUT, timeout);

        Solver solver = BasicBrutalSolver.newDFSSolver();

        helper.solve(solver, new SolverParameters(solver.getSolverType(), l, params));

        return SUCCESS;
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
