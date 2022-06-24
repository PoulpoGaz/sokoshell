package fr.valax.sokoshell;

import fr.valax.args.api.Option;
import fr.valax.args.utils.ArgsUtils;
import fr.valax.sokoshell.solver.BasicBrutalSolver;
import fr.valax.sokoshell.solver.Pack;
import fr.valax.sokoshell.solver.SolverParameters;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BenchmarkCommand extends PackCommand {

    @Option(names = {"t", "-timeout"}, hasArgument = true, argName = "Timeout")
    private Long timeout;

    @Override
    protected int executeImpl(InputStream in, PrintStream out, PrintStream err) {
        Pack pack = getPack();

        if (pack == null) {
            return FAILURE;
        }

        if (timeout == null) {
            System.out.println("Setting timeout to 10min");

            timeout = 10 * 60 * 1000L;
        } else if (timeout <= 0) {
            System.out.println("WARNING: You did not set a timeout.");
        }

        Map<String, Object> params = new HashMap<>();
        params.put(SolverParameters.TIMEOUT, timeout);

        helper.benchmark(BasicBrutalSolver.newDFSSolver(), params, pack);

        return SUCCESS;
    }

    @Override
    public String getName() {
        return "benchmark";
    }

    @Override
    public String getUsage() {
        return "Try to solve all level of a pack";
    }
}
