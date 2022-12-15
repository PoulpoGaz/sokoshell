package fr.valax.sokoshell.commands;

import fr.valax.args.api.Command;
import fr.valax.args.api.Option;
import fr.valax.args.api.TypeConverter;
import fr.valax.args.api.VaArgs;
import fr.valax.args.utils.ArgsUtils;
import fr.valax.args.utils.TypeException;
import fr.valax.sokoshell.SolverTask;
import fr.valax.sokoshell.TaskList;
import fr.valax.sokoshell.TaskStatus;
import fr.valax.sokoshell.solver.*;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author PoulpoGaz
 */
public class SolveCommand extends AbstractCommand {

    @Option(names = {"p", "packs"}, hasArgument = true, argName = "Pack name", allowDuplicate = true)
    protected String[] packs;

    @Option(names = {"l", "levels"}, hasArgument = true, argName = "Levels")
    protected String levels;

    @Option(names = {"s", "solver-type"}, hasArgument = true, argName = "Solver type",
            description = "solving strategy: DFS (default) BFS, ASTAR",
            defaultValue = "DFS",
            converter = SolverConverter.class)
    protected SolverType solverType;

    @Option(names = {"t", "timeout"}, hasArgument = true, argName = "Timeout", defaultValue = "-1", description = "in ms")
    private long timeout;

    @Option(names = {"P", "position"}, hasArgument = true)
    private Integer position;

    @Option(names = {"T", "haikyu", "to-the-top"}, description = "equivalent to --position 0")
    private boolean toTheTop;

    @Option(names = {"w", "wait"}, description = "wait until this task and task with higher position finish")
    private boolean waitUntilFinished;

    @Option(names = {"split"}, description = "split all levels in different tasks")
    private boolean split;

    @VaArgs(description = "solver parameters")
    private String[] args;

    @Override
    protected int executeImpl(InputStream in, PrintStream out, PrintStream err) throws InvalidArgument {
        List<Level> levels = getLevels(this.levels, packs);

        if (levels.isEmpty()) {
            out.println("No task added to task list");
            return SUCCESS;
        }

        System.out.println(solverType);

        final Solver solver = switch (solverType) {
            case BFS -> BasicBruteforceSolver.newBFSSolver();
            case ASTAR -> new AStarSolver();
            default -> BasicBruteforceSolver.newDFSSolver();
        };

        List<SolverParameter> parameters = getParameters(solver, args);

        String packRequest = formatPackRequest();
        SolverTask lastTask = null;
        if (split) {
            for (Level level : levels) {
                lastTask = newTask(solver, parameters, List.of(level), packRequest);
            }
        } else {
            lastTask = newTask(solver, parameters, levels, packRequest);
        }

        if (waitUntilFinished) {
            while (lastTask.getTaskStatus() == TaskStatus.PENDING ||
                    lastTask.getTaskStatus() == TaskStatus.RUNNING) {
                Thread.onSpinWait();
            }
        }

        return Command.SUCCESS;
    }

    private List<SolverParameter> getParameters(Solver solver, String[] args) throws InvalidArgument {
        List<SolverParameter> parameters = solver.getParameters();

        for (int i = 0; i < args.length; i += 2) {
            String name = args[i];
            SolverParameter param = getParameter(parameters, name);


            if (param == null) {
                throw new InvalidArgument("Solver " + solver.getSolverType() +
                        " doesn't have a parameter named " + name);
            }

            param.set(args[i + 1]);
        }

        return parameters;
    }

    private SolverParameter getParameter(List<SolverParameter> parameters, String name) {
        for (SolverParameter p : parameters) {
            if (p.getName().equals(name)) {
                return p;
            }
        }

        return null;
    }

    private SolverTask newTask(Solver solver, List<SolverParameter> params, List<Level> levels, String packRequest) {
        SolverTask task = new SolverTask(solver, params, levels, packRequest, nullSafeToString(this.levels));
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

    private String formatPackRequest() {
        if (packs == null) {
            Pack pack = helper.getSelectedPack();

            if (pack == null) {
                return "";
            } else {
                return pack.name();
            }
        } else if (packs.length == 1) {
            return packs[0];
        } else {
            return Arrays.toString(packs);
        }
    }

    private String nullSafeToString(String str) {
        if (str == null) {
            return "";
        } else {
            return str;
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
        } else if (ArgsUtils.contains(option.names(), "s")) {
            for (SolverType solver : SolverType.values()) {
                candidates.add(new Candidate(solver.name()));
            }
        }
    }

    private static class SolverConverter implements TypeConverter<SolverType> {

        @Override
        public SolverType convert(String value) throws TypeException {
            if (value == null) {
                return null;
            } else {
                return switch (value.toLowerCase()) {
                    case "dfs" -> SolverType.DFS;
                    case "bfs" -> SolverType.BFS;
                    case "a*", "astar" -> SolverType.ASTAR;
                    default -> throw new TypeException(String.format("Invalid solver type '%s'", value));
                };
            }
        }
    }
}
