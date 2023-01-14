package fr.valax.sokoshell.commands.unix;

import fr.valax.args.CommandLine;
import fr.valax.args.api.VaArgs;
import fr.valax.args.jline.FileNameCompleter;
import fr.valax.args.utils.CommandLineException;
import fr.valax.sokoshell.StartupScript;
import fr.valax.sokoshell.commands.AbstractCommand;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public class Source extends AbstractCommand {

    @VaArgs
    private Path[] vaArgs;

    @Override
    protected int executeImpl(InputStream in, PrintStream out, PrintStream err) {
        if (vaArgs.length == 0) {
            err.println("file required");
            return FAILURE;
        } else {
            StartupScript script = new StartupScript(sokoshell().getCli());

            try {
                script.run(vaArgs[0]);
            } catch (IOException | CommandLineException e) {
                e.printStackTrace();
                return FAILURE;
            }

            return SUCCESS;
        }
    }

    @Override
    public String getName() {
        return "source";
    }

    @Override
    public String getShortDescription() {
        return "execute a script";
    }

    @Override
    public String[] getUsage() {
        return new String[0];
    }

    @Override
    public void complete(LineReader reader, String commandString, CommandLine.CommandSpec command, List<Candidate> candidates, CommandLine.OptionSpec option, String argument) {
        if (option == null) {
            FileNameCompleter.INSTANCE.complete(reader, Objects.requireNonNullElse(argument, ""), candidates);
        }
    }
}
