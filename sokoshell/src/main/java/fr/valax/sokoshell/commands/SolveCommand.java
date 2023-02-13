package fr.valax.sokoshell.commands;

import fr.valax.args.CommandLine;
import fr.valax.args.api.Command;
import fr.valax.args.api.Option;
import fr.valax.args.api.VaArgs;
import fr.valax.args.utils.ArgsUtils;
import fr.valax.sokoshell.*;
import fr.valax.sokoshell.solver.*;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.*;

/**
 * @author PoulpoGaz
 */
public class SolveCommand extends AbstractCommand {

    @Option(names = {"p", "packs"}, hasArgument = true, argName = "Pack name", allowDuplicate = true)
    protected String[] packs;

    @Option(names = {"l", "levels"}, hasArgument = true, argName = "Levels")
    protected String levels;

    @Option(names = {"s", "solver-type"}, hasArgument = true, argName = "Solver type",
            description = "solving strategy: DFS (default) BFS, A*",
            defaultValue = "DFS")
    protected String solver;

    @Option(names = {"t", "tracker"}, hasArgument = true, argName = "Tracker",
            description = {
                    "Set the tracker. The tracker is the object responsible of gathering statistics",
                    "Possible values:",
                    "-none: no tracker will be added",
                    "-light: time start, time end, node per secondes, average queue size and total number of states are computed",
                    "-default: same as light but with more details"
            }, defaultValue = "default")
    protected String tracker;

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

        Solver solver = sokoshell().getSolver(this.solver);
        if (solver == null) {
            err.printf("No such solver: %s%n", this.solver);
            return FAILURE;
        }

        Tracker tracker = null;
        if (!this.tracker.equalsIgnoreCase("none")) {
            tracker = getTracker();
            if (tracker == null) {
                err.printf("No such tracker: %s%n", this.tracker);
                return FAILURE;
            }
        }

        List<SolverParameter> parameters = getParameters(solver, args);
        printTask(out, solver, parameters, levels);

        String packRequest = formatPackRequest();
        SolverTask lastTask = null;
        if (split) {
            for (Level level : levels) {
                lastTask = newTask(solver, tracker, parameters, List.of(level), packRequest);
            }
        } else {
            lastTask = newTask(solver, tracker, parameters, levels, packRequest);
        }

        if (waitUntilFinished) {
            while (lastTask.getTaskStatus() == TaskStatus.PENDING ||
                    lastTask.getTaskStatus() == TaskStatus.RUNNING) {
                Thread.onSpinWait();
            }
        }

        return Command.SUCCESS;
    }

    private Tracker getTracker() {
        return switch (tracker.toLowerCase()) {
            case "light" -> new LightweightTracker();
            case "default" -> new DefaultTracker();
            default -> null;
        };
    }

    private List<SolverParameter> getParameters(Solver solver, String[] args) throws InvalidArgument {
        List<SolverParameter> parameters = solver.getParameters();

        for (int i = 0; i < args.length; i += 2) {
            String name = args[i];
            SolverParameter param = getParameter(parameters, name);

            if (param == null) {
                throw new InvalidArgument("Solver " + solver.getName() +
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

    private void printTask(PrintStream out, Solver solver, List<SolverParameter> parameters, List<Level> levels) {
        if (levels.size() == 1) {
            out.printf("Solving 1 level with %s%n", solver.getName());
        } else {
            out.printf("Solving %d levels with %s%n", levels.size(), solver.getName());
        }

        out.println("Parameters:");
        for (SolverParameter p : parameters) {
            if (p.hasArgument()) {
                out.println(p.getName() + " = " + p.get());
            } else {
                out.println(p.getName() + " = " + p.getDefaultValue() + " (default value)");
            }
        }
    }

    private SolverTask newTask(Solver solver, Tracker tracker, List<SolverParameter> params, List<Level> levels, String packRequest) {
        SolverTask task = new SolverTask(solver, tracker, params, levels, packRequest, nullSafeToString(this.levels));
        TaskList list = sokoshell().getTaskList();

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
            Pack pack = sokoshell().getSelectedPack();

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
        List<String> usage = new ArrayList<>();
        usage.add("solve [OPTIONS]... LEVEL(S) PACK(S) [SOLVER PARAMETERS]...");
        usage.add("");
        usage.add("Solver parameters:");

        for (Solver solver : sokoshell().getSolvers().values()) {
            usage.add(solver.getName() + ":");

            for (SolverParameter param : solver.getParameters()) {
                if (param.getDescription() == null) {
                    usage.add(" -" + param.getName());
                } else {
                    usage.add(" -" + param.getName() + "     " + param.getDescription());
                }
            }
        }

        return usage.toArray(new String[0]);
    }

    @Override
    public void complete(LineReader reader,
                         String commandString,
                         CommandLine.CommandSpec command,
                         List<Candidate> candidates,
                         CommandLine.OptionSpec option,
                         String argument) {
        if (option != null) {
            if (ArgsUtils.contains(option.getShortNames(), 'p')) {
                sokoshell().addPackCandidates(candidates);
            } else if (ArgsUtils.contains(option.getShortNames(), 's')) {
                Set<String> solvers = SokoShell.INSTANCE.getSolvers().keySet();

                for (String solver : solvers) {
                    candidates.add(new Candidate(solver));
                }
            } else if (ArgsUtils.contains(option.getShortNames(), 't')) {
                candidates.add(new Candidate("none"));
                candidates.add(new Candidate("light"));
                candidates.add(new Candidate("default"));
            }
        } else {
            CommandLine.OptionSpec spec = command.findOption("s");

            Solver solver;
            if (spec.isPresent()) {
                List<String> args = spec.getArgumentsList();

                if (args.isEmpty()) {
                    return;
                }

                solver = sokoshell().getSolver(args.get(0));
            } else {
                solver = sokoshell().getSolver(Solver.BFS);
            }

            if (solver != null) {
                completeSolverParameter(reader, commandString, command, solver, candidates);
            }
        }
    }

    private void completeSolverParameter(LineReader reader,
                                         String commandString,
                                         CommandLine.CommandSpec command,
                                         Solver solver,
                                         List<Candidate> candidates) {
        boolean newToken = commandString.endsWith(" ");

        List<SolverParameter> params = solver.getParameters();

        List<String> vaArgs = command.getVaargs().getValues();
        Set<String> present = new HashSet<>();

        SolverParameter last = null;
        boolean inValue = false;
        for (String vaArg : vaArgs) {
            if (inValue) {
                inValue = false;
            } else {
                SolverParameter curr = getParameter(params, vaArg);

                if (curr != null) {
                    present.add(vaArg);
                    inValue = true;
                    last = curr;
                } else {
                    last = null;
                }
            }
        }

        if (inValue) {
            last.complete(reader, null, candidates);
        } else if (last != null && !newToken) {
            last.complete(reader, vaArgs.get(vaArgs.size() - 1), candidates);
        } else {
            for (SolverParameter param : params) {
                if (!present.contains(param.getName())) {
                    candidates.add(new Candidate(param.getName()));
                }
            }
        }
    }
}
