package fr.valax.sokoshell.commands.table;

import fr.valax.args.api.Option;
import fr.valax.args.utils.ArgsUtils;
import fr.valax.sokoshell.solver.*;
import fr.valax.sokoshell.utils.Alignment;
import fr.valax.sokoshell.utils.PrettyColumn;
import fr.valax.sokoshell.utils.PrettyTable;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;
import org.jline.utils.AttributedString;

import java.io.InputStream;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

public class ListSolution extends TableCommand {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    @Option(names = {"p", "pack"}, hasArgument = true, argName = "Pack name")
    private String packName;

    @Option(names = {"i", "index"}, hasArgument = true, argName = "Level index")
    private Integer levelIndex;

    @Override
    public int executeImpl(InputStream in, PrintStream out, PrintStream err) {
        List<Solution> solutions = getSolutions();

        if (solutions == null) {
            return FAILURE;
        }

        PrettyTable table = new PrettyTable();

        if (packName == null) {
            PrettyColumn<String> packName = new PrettyColumn<>("Pack");

            for (Solution s : solutions) {
                packName.add(s.getParameters().getLevel().getPack().name());
            }

            table.addColumn(packName);
        }

        if (levelIndex == null) {
            PrettyColumn<Integer> index = new PrettyColumn<>("Index");

            for (Solution s : solutions) {
                index.add(Alignment.RIGHT, s.getParameters().getLevel().getIndex() + 1);
            }

            table.addColumn(index);
        }

        PrettyColumn<SolverStatus> status = new PrettyColumn<>("Status");
        PrettyColumn<SolverType> solverType = new PrettyColumn<>("Solver");
        PrettyColumn<Integer> pushes = new PrettyColumn<>("Pushes");
        PrettyColumn<Integer> moves = new PrettyColumn<>("Moves");

        PrettyColumn<Long> date = new PrettyColumn<>("Date");
        date.setToString(start -> {
            String d = DATE_FORMAT.format(Date.from(Instant.ofEpochMilli(start)));

            return new AttributedString[] {new AttributedString(d)};
        });

        PrettyColumn<Long> time = new PrettyColumn<>("Time");
        time.setToString(time1 -> new AttributedString[] {new AttributedString(prettyDate(time1))});

        for (Solution s : solutions) {
            SolverStatistics stats = s.getStatistics();

            status.add(s.getStatus());
            solverType.add(s.getType());
            pushes.add(Alignment.RIGHT, s.numberOfPushes());
            moves.add(Alignment.RIGHT, s.numberOfMoves());
            date.add(stats.getTimeStarted());
            time.add(Alignment.RIGHT, stats.getTimeEnded() - stats.getTimeStarted());
        }

        table.addColumn(status);
        table.addColumn(solverType);
        table.addColumn(pushes);
        table.addColumn(moves);
        table.addColumn(date);
        table.addColumn(time);

        printTable(out, err, table);
        out.printf("%nNumber of solutions: %d%n", solutions.size());

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

    /*@Override
    protected String whenEmpty() {
        if (packName == null) {
            return "No level solved";
        } else if (levelIndex == null) {
            return "No level in this pack are solved";
        } else {
            return "Level not solved";
        }
    }*/

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