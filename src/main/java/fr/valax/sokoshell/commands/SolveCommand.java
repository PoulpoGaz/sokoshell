package fr.valax.sokoshell.commands;

import fr.valax.args.api.Command;
import fr.valax.args.api.Option;
import fr.valax.args.api.VaArgs;
import fr.valax.args.utils.ArgsUtils;
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

    @VaArgs(description = "solver parameters")
    private String[] args;

    @Override
    protected int executeImpl(InputStream in, PrintStream out, PrintStream err) throws InvalidArgument {
        List<Level> levels = getLevels(this.levels, packs);

        if (levels.isEmpty()) {
            return SUCCESS;
        }

        Map<String, Object> params = new HashMap<>();
        if (timeout > 0) {
            params.put(SolverParameters.TIMEOUT, timeout);
        }
        addParameters(params);

        //Solver solver = BasicBruteforceSolver.newDFSSolver();
        Solver solver = BasicBruteforceSolver.newBFSSolver();

        String packRequest = formatPackRequest();
        SolverTask lastTask = null;
        if (split) {
            for (Level level : levels) {
                lastTask = newTask(solver, params, List.of(level), packRequest);
            }
        } else {
            lastTask = newTask(solver, params, levels, packRequest);
        }

        if (waitUntilFinished) {
            while (lastTask.getTaskStatus() == TaskStatus.RUNNING) {
                Thread.onSpinWait();
            }
        }

        return Command.SUCCESS;
    }

    // TODO: rework
    private void addParameters(Map<String, Object> params) throws InvalidArgument {
        if (args.length % 2 != 0) {
            throw new InvalidArgument("Odd number of arguments");
        }

        for (int i = 0; i < args.length; i += 2) {
            String name = args[i];
            String value = args[i + 1];

            if (SolverParameters.MAX_RAM.equals(name)) {
                params.put(SolverParameters.MAX_RAM, parseRAM(value));
            } else {
                params.put(name, value);
            }
        }
    }

    private long parseRAM(String value) {
        Pattern p = Pattern.compile("^(\\d+)\\s*([gmk])?b$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = p.matcher(value);

        if (matcher.matches() && matcher.groupCount() >= 1 && matcher.groupCount() <= 2) {
            long r = Long.parseLong(matcher.group(1));

            if (matcher.groupCount() == 2) {
                String unit = matcher.group(2).toLowerCase();

                r = switch (unit) {
                    case "g" -> r * 1024 * 1024 * 1024;
                    case "m" -> r * 1024 * 1024;
                    case "k" -> r * 1024;
                    default -> r;
                };
            }

            return r;
        } else {
            return -1;
        }
    }

    private SolverTask newTask(Solver solver, Map<String, Object> params, List<Level> levels, String packRequest) {
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
        }
    }
}
