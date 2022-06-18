package fr.valax.sokoshell;

import fr.valax.args.api.Option;
import fr.valax.args.utils.ArgsUtils;
import fr.valax.sokoshell.solver.BasicBrutalSolver;
import fr.valax.sokoshell.solver.Pack;
import fr.valax.sokoshell.solver.SolverParameters;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BenchmarkCommand extends AbstractVoidCommand {

    @Option(names = {"p", "-pack"}, hasArgument = true, argName = "Pack name")
    private String packName;

    @Option(names = {"t", "-timeout"}, hasArgument = true, argName = "Timeout")
    private Long timeout;

    @Override
    public void run() {
        Pack pack = helper.getPack(packName);

        if (pack == null) {
            System.out.println("Can't find a pack named " + packName);
            return;
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
    }

    @Override
    public String getName() {
        return "benchmark";
    }

    @Override
    public String getUsage() {
        return "Try to solve all level of a pack";
    }

    @Override
    public void completeOption(LineReader reader, ParsedLine line, List<Candidate> candidates, Option option) {
        if (ArgsUtils.contains(option.names(), "p")) {
            helper.addPackCandidates(candidates);
        }
    }
}
