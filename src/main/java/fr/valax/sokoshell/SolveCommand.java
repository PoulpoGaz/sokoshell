package fr.valax.sokoshell;

import fr.valax.args.api.Option;
import fr.valax.sokoshell.solver.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author PoulpoGaz
 */
public class SolveCommand extends AbstractVoidCommand {

    @Option(names = {"p", "-pack"}, argName = "Pack name", optional = false)
    private String name;

    @Option(names = {"i", "-index"}, argName = "Level index", optional = false)
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

        List<Level> levels = pack.levels();
        Level l = levels.get(index);

        Solver solver = BasicBrutalSolver.newDFSSolver();
        List<State> solution = new ArrayList<>();

        System.out.println(solver.solve(l, solution));
        System.out.println(solution);
    }

    @Override
    public String getName() {
        return "solve";
    }

    @Override
    public String getUsage() {
        return "solve a sokoban";
    }

    @Override
    public boolean addHelp() {
        return true;
    }
}
