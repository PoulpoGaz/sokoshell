package fr.valax.sokoshell;

import fr.valax.args.api.Option;
import fr.valax.args.utils.ArgsUtils;
import fr.valax.sokoshell.solver.*;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.util.ArrayList;
import java.util.List;

/**
 * @author PoulpoGaz
 */
public class SolveCommand extends AbstractVoidCommand {

    @Option(names = {"p", "-pack"}, hasArgument = true, argName = "Pack name", optional = false)
    private String name;

    @Option(names = {"i", "-index"}, hasArgument = true, argName = "Level index", optional = false)
    private int index;

    public SolveCommand(SokoShellHelper helper) {
        super(helper);
    }

    @Override
    public void run() {
        Pack pack = helper.getPack(name);

        if (pack == null) {
            System.out.printf("No pack named %s exists%n", name);
            return;
        }

        index--;
        if (index < 0 || index >= pack.levels().size()) {
            System.out.println("Index out of bounds");
            return;
        }

        List<Level> levels = pack.levels();
        Level l = levels.get(index);

        Solver solver = BasicBrutalSolver.newDFSSolver();

        helper.solve(solver, l);
    }

    @Override
    public void completeOption(LineReader reader, ParsedLine line, List<Candidate> candidates, Option option) {
        if (ArgsUtils.contains(option.names(), "p")) {
            helper.addPackCandidates(candidates);
        }
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
