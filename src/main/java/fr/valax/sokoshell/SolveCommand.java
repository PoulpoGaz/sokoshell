package fr.valax.sokoshell;

import fr.valax.args.api.Option;
import fr.valax.args.utils.ArgsUtils;
import fr.valax.sokoshell.solver.*;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author PoulpoGaz
 */
public class SolveCommand extends LevelCommand {

    @Option(names = {"t", "-timeout"}, hasArgument = true, argName = "Timeout", defaultValue = "-1")
    private long timeout;

    @Override
    protected int executeImpl(InputStream in, PrintStream out, PrintStream err) {
        Level l = getLevel();

        if (l == null) {
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
    public String getUsage() {
        return "Solve a sokoban";
    }
}
