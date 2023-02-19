package fr.valax.sokoshell.commands;

import fr.valax.args.CommandLine;
import fr.valax.args.api.Option;
import fr.valax.args.utils.ArgsUtils;
import fr.valax.interval.ParseException;
import fr.valax.interval.Set;
import fr.valax.sokoshell.solver.Level;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;

public class RemoveReport extends AbstractCommand {

    @Option(names = {"p", "packs"}, hasArgument = true, argName = "Name of the packs", allowDuplicate = true)
    private String[] packsName;

    @Option(names = {"l", "levels"}, hasArgument = true, argName = "Index of the levels")
    private String levelsIndices;

    @Option(names = {"R", "reports"}, hasArgument = true, argName = "Index of the reports to remove", optional = false)
    private String reportsIndices;

    @Override
    protected int executeImpl(InputStream in, PrintStream out, PrintStream err) throws InvalidArgument {
        Set reportsSet;
        try {
            reportsSet = parser.parse(reportsIndices);
        } catch (ParseException e) {
            throw new InvalidArgument(e);
        }
        List<Level> levels = getLevels(levelsIndices, packsName);

        for (Level l : levels) {
            // when an element is removed, the number of solver report
            // is reduced by 1, therefore, reducing the index is needed
            // to avoid skipping an element.
            int offset = 0;
            for (int i = 0; i < l.numberOfSolverReport(); i++) {
                if (reportsSet.contains(i + offset)) {
                    l.removeSolverReport(i);
                    offset++;
                    i--;
                }
            }
        }

        return 0;
    }

    @Override
    public String getName() {
        return "remove-report";
    }

    @Override
    public String getShortDescription() {
        return "Remove reports";
    }

    @Override
    public String[] getUsage() {
        return new String[0];
    }

    @Override
    public void complete(LineReader reader, String commandString, CommandLine.CommandSpec command, List<Candidate> candidates, CommandLine.OptionSpec option, String argument) {
        if (ArgsUtils.contains(option.getShortNames(), 'p')) {
            sokoshell().addPackCandidates(candidates);
        }
    }
}
