package fr.valax.sokoshell.commands;

import fr.valax.args.api.VaArgs;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;

public class AutoSaveSolutionCommand extends AbstractCommand {

    @VaArgs
    private String[] vaargs;

    @Override
    protected int executeImpl(InputStream in, PrintStream out, PrintStream err) {
        if (vaargs == null || vaargs.length == 0) {
            if (helper.isAutoSaveSolution()) {
                out.println("Auto save is enabled");
            } else {
                out.println("Auto save is disabled");
            }

        } else {
            String arg = vaargs[0];

            helper.setAutoSaveSolution(arg.equalsIgnoreCase("y") || arg.equalsIgnoreCase("on"));
        }

        return 0;
    }

    @Override
    public String getName() {
        return "auto-save-solution";
    }

    @Override
    public String getShortDescription() {
        return "auto save solution on program exit";
    }

    @Override
    public String[] getUsage() {
        return new String[0];
    }

    @Override
    public void completeVaArgs(LineReader reader, String argument, List<Candidate> candidates) {
        candidates.add(new Candidate("on"));
        candidates.add(new Candidate("off"));
    }
}
