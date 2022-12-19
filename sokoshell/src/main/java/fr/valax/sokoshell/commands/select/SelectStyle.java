package fr.valax.sokoshell.commands.select;

import fr.valax.args.CommandLine;
import fr.valax.args.api.Option;
import fr.valax.args.utils.ArgsUtils;
import fr.valax.sokoshell.commands.AbstractCommand;
import fr.valax.sokoshell.graphics.style.MapStyle;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;

public class SelectStyle extends AbstractCommand {

    @Option(names = {"s", "style"}, optional = false, hasArgument = true, argName = "style")
    private String style;

    @Override
    protected int executeImpl(InputStream in, PrintStream out, PrintStream err) {
        MapStyle mapStyle = helper.getMapStyle(style);

        if (mapStyle == null) {
            err.printf("%s: no such map style%n", style);
        } else {
            helper.setMapStyle(mapStyle);
        }

        return 0;
    }

    @Override
    public void complete(LineReader reader, String commandString, CommandLine.CommandSpec command, List<Candidate> candidates, CommandLine.OptionSpec option, String argument) {
        if (option != null && ArgsUtils.contains(option.getShortNames(), 's')) {
            helper.addMapStyleCandidates(candidates);
        }
    }

    @Override
    public String getName() {
        return "style";
    }

    @Override
    public String getShortDescription() {
        return "Set current map style";
    }

    @Override
    public String[] getUsage() {
        return new String[0];
    }
}
