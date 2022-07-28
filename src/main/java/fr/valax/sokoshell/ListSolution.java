package fr.valax.sokoshell;

import fr.valax.args.api.Option;
import fr.valax.args.utils.ArgsUtils;
import fr.valax.sokoshell.solver.Level;
import fr.valax.sokoshell.solver.Pack;
import fr.valax.sokoshell.solver.Solution;
import fr.valax.sokoshell.solver.SolverStatistics;
import fr.valax.sokoshell.utils.PrettyTable;
import fr.valax.sokoshell.utils.PrettyTable.Alignment;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;

import java.io.InputStream;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

import static fr.valax.sokoshell.utils.PrettyTable.*;

public class ListSolution extends TableCommand<Solution> {

    @Option(names = {"p", "pack"}, hasArgument = true, argName = "Pack name")
    private String packName;

    @Option(names = {"i", "index"}, hasArgument = true, argName = "Level index")
    private Integer levelIndex;

    @Override
    public int executeImpl(InputStream in, PrintStream out, PrintStream err) {
        List<Solution> solutions = getSolutions();

        if (solutions != null) {
            printTable(out, solutions);
        }

        return SUCCESS;
    }

    private List<Solution> getSolutions() {
        if (packName == null) {
            List<Solution> solutions = new ArrayList<>();
            Iterator<Pack> it = helper.getPacks().stream()
                    .sorted(Comparator.comparing(Pack::name))
                    .iterator();

            while (it.hasNext()) {
                Pack pack = it.next();
                for (Level level : pack.levels()) {
                    solutions.addAll(level.getSolutions());
                }
            }

            return solutions;
        } else {
            Pack pack = helper.getPack(packName);

            if (pack == null) {
                System.out.printf("No pack named %s exists%n", packName);
                return null;
            }

            if (levelIndex == null) {
                List<Solution> solutions = new ArrayList<>();
                for (Level level : pack.levels()) {
                    solutions.addAll(level.getSolutions());
                }

                return solutions;
            } else {
                int index = levelIndex - 1;
                if (index < 0 || index >= pack.levels().size()) {
                    System.out.println("Index out of bounds");
                    return null;
                }

                 return pack.levels().get(index).getSolutions();
            }
        }
    }

    @Override
    protected String[] getHeaders() {
        if (packName == null) {
            return new String[] {"Pack", "Index", "Status", "Solver", "Pushes", "Moves", "Date", "Time"};
        } else if (levelIndex == null) {
            return new String[] {"Index", "Status", "Solver", "Pushes", "Moves", "Date", "Time"};
        } else {
            return new String[] {"Status", "Solver", "Pushes", "Moves", "Date", "Time"};
        }
    }

    @Override
    protected Cell extract(Solution solution, int x) {
        if (packName != null && levelIndex != null) {
            x += 2;
        } else if (packName != null) {
            x++;
        }

        return switch (x) {
            case 0 -> new Cell(solution.getParameters().getLevel().getPack().name());
            case 1 -> new Cell(Alignment.RIGHT, String.valueOf(solution.getParameters().getLevel().getIndex() + 1));
            case 2 -> new Cell(solution.getStatus().toString());
            case 3 -> new Cell(solution.getParameters().getSolver().toString());
            case 4 -> new Cell(Alignment.RIGHT, String.valueOf(solution.numberOfPushes()));
            case 5 -> new Cell(Alignment.RIGHT, String.valueOf(solution.numberOfMoves()));
            case 6 -> {
                SolverStatistics stats = solution.getStatistics();

                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                String date = format.format(Date.from(Instant.ofEpochMilli(stats.getTimeStarted())));

                yield new Cell(date);
            }
            case 7 -> {
                SolverStatistics stats = solution.getStatistics();

                long time = stats.getTimeEnded() - stats.getTimeStarted();

                yield new Cell(Alignment.RIGHT, prettyDate(time));
            }
            default -> throw new IllegalArgumentException();
        };
    }

    @Override
    protected String countLine() {
        return "Number of solutions: %d%n";
    }

    @Override
    protected String whenEmpty() {
        if (packName == null) {
            return "No level solved";
        } else if (levelIndex == null) {
            return "No level in this pack are solved";
        } else {
            return "Level not solved";
        }
    }

    private String prettyDate(long millis) {
        if (millis < 1000) {
            return millis + " ms";
        } else if (millis < 1000 * 60) {
            double sec = millis / 1000d;

            return round(sec) + " s";
        } else if (millis < 1000 * 60 * 60) {
            int minute = (int) (millis / (1000 * 60d));
            double sec = (millis - minute * 1000 * 60) / 1000d;

            return minute + " min " + sec + " s";
        } else {
            int hour = (int) (millis / (1000 * 60 * 60d));
            int minute = (int) (millis - hour * 1000 * 60 * 60) / (1000 * 60);
            double sec = (millis - hour * 1000 * 60 * 60 - minute * 1000 * 60) / 1000d;

            return hour + " h " + minute + " min " + sec + " s";
        }
    }

    private String round(double d) {
        DecimalFormat format = new DecimalFormat("#.##");

        return format.format(d);
    }

    @Override
    public String getName() {
        return "solution";
    }

    @Override
    public String getShortDescription() {
        return "List all solutions of a level";
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