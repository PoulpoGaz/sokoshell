package fr.valax.sokoshell.commands.table;

import fr.valax.args.CommandLine;
import fr.valax.args.api.Command;
import fr.valax.args.api.Option;
import fr.valax.args.utils.ArgsUtils;
import fr.valax.sokoshell.solver.*;
import fr.valax.sokoshell.utils.Alignment;
import fr.valax.sokoshell.utils.PrettyColumn;
import fr.valax.sokoshell.utils.PrettyTable;
import fr.valax.sokoshell.utils.Utils;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;
import org.jline.utils.AttributedString;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.List;

public class StatsCommand extends TableCommand {

    @Option(names = {"p", "pack"}, hasArgument = true, argName = "Pack name")
    protected String pack;

    @Option(names = {"l", "level"}, hasArgument = true, argName = "Level index")
    protected Integer level;

    @Option(names = {"R", "report"}, hasArgument = true, argName = "Report index")
    private Integer report;

    @Override
    protected int executeImpl(InputStream in, PrintStream out, PrintStream err) throws InvalidArgument {
        Level l = getLevel(pack, level);

        if (!l.hasReport()) {
            err.println("Not solved");
            return FAILURE;
        }

        SolverReport s;
        if (report != null) {
            s = l.getSolverReport(report);

            if (s == null) {
                err.println("Index out of bounds");
                return FAILURE;
            }
        } else {
            s = l.getLastReport();
        }

        out.printf("Status: %s. Solver: %s%n", s.getStatus(), s.getSolverName());

        SolverParameters params = s.getParameters();
        out.printf("Level: %s - %d%n", params.getLevel().getPack().name(), params.getLevel().getIndex());

        Collection<SolverParameter> collection = params.getParameters();
        if (!collection.isEmpty()) {
            out.println("Parameters:");

            for (SolverParameter p : collection) {
                if (p.hasArgument()) {
                    out.printf("%s = %s%n", p.getName(), p.get());
                }
            }
        }

        ISolverStatistics stats = s.getStatistics();
        if (stats != null) {
            PrettyTable table = stats.printStatistics(out, err);

            if (table != null) {
                printTable(out, err, table);
            }
        }

        return Command.SUCCESS;
    }

    @Override
    public String getName() {
        return "stats";
    }

    @Override
    public String getShortDescription() {
        return "Print stats about a solution";
    }

    @Override
    public String[] getUsage() {
        return new String[0];
    }

    @Override
    public void complete(LineReader reader, String commandString, CommandLine.CommandSpec command, List<Candidate> candidates, CommandLine.OptionSpec option, String argument) {
        if (option != null && ArgsUtils.contains(option.getShortNames(), 'p')) {
            sokoshell().addPackCandidates(candidates);
        }
    }
}
