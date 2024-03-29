package fr.valax.sokoshell.commands;

import fr.valax.args.CommandLine;
import fr.valax.args.api.VaArgs;
import fr.valax.args.jline.FileNameCompleter;
import fr.valax.sokoshell.graphics.style.BoardStyle;
import fr.valax.sokoshell.graphics.style.BoardStyleReader;
import fr.valax.sokoshell.utils.PathGlobIterator;
import fr.valax.sokoshell.utils.ScanUtils;
import fr.valax.sokoshell.utils.Utils;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public class LoadStyleCommand extends AbstractCommand {

    @VaArgs
    private String[] input;

    private final BoardStyleReader reader = new BoardStyleReader();

    @Override
    public int executeImpl(InputStream in, PrintStream out, PrintStream err) {

        for (String input : this.input) {
            try (PathGlobIterator it = new PathGlobIterator(input)) {

                boolean loaded = false;

                while (it.hasNext()) {
                    Path next = it.next();

                    if (Utils.getExtension(next).equals("style")) {
                        load(next, out, err);
                    }

                    loaded = true;
                }

                if (!loaded) {
                    out.println("No board style loaded");
                }

            } catch (IOException e) {
                e.printStackTrace(err);
                err.println("Failed to load board styles");

                return FAILURE;
            }
        }

        return SUCCESS;
    }

    private void load(Path input, PrintStream out, PrintStream err) {
        out.println("Loading " + input);

        BoardStyle style;
        try {
            style = reader.read(input);
        } catch (IOException e) {
            e.printStackTrace(err);
            err.println("Failed to read board style at " + input);

            return;
        }

        if (!sokoshell().addBoardStyle(style)) {
            boolean answer = ScanUtils.yesNoQuestion(
                    "A board style with named %s already exists. Did you want to overwrite it?".formatted(style.getName()),
                    ScanUtils.DEFAULT_NO);

            if (answer) {
                sokoshell().addBoardStyleReplace(style);
            }
        }
    }

    @Override
    public void complete(LineReader reader, String commandString, CommandLine.CommandSpec command, List<Candidate> candidates, CommandLine.OptionSpec option, String argument) {
        if (option == null) {
            FileNameCompleter.INSTANCE.complete(reader, Objects.requireNonNullElse(argument, ""), candidates);
        }
    }

    @Override
    public String getName() {
        return "style";
    }

    @Override
    public String getShortDescription() {
        return "Load a board style (.style)";
    }

    @Override
    public String[] getUsage() {
        return new String[0];
    }
}