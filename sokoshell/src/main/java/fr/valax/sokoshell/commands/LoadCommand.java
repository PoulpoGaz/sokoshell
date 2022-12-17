package fr.valax.sokoshell.commands;

import fr.poulpogaz.json.JsonException;
import fr.valax.args.CommandLine;
import fr.valax.args.api.VaArgs;
import fr.valax.args.jline.FileNameCompleter;
import fr.valax.sokoshell.readers.PackReaders;
import fr.valax.sokoshell.solver.Pack;
import fr.valax.sokoshell.utils.PathGlobIterator;
import fr.valax.sokoshell.utils.ScanUtils;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public class LoadCommand extends AbstractCommand {

    @VaArgs
    private String[] input;

    @Override
    public int executeImpl(InputStream in, PrintStream out, PrintStream err) {

        for (String input : this.input) {
            try (PathGlobIterator it = new PathGlobIterator(input)) {
                it.setLimit(1000);

                boolean loaded = false;

                while (it.hasNext()) {
                    load(it.next(), out, err);

                    loaded = true;
                }

                if (!loaded) {
                    out.println("No pack loaded");
                }

            } catch (IOException e) {
                e.printStackTrace(err);
                err.println("Failed to load packs");

                return FAILURE;
            }
        }

        return SUCCESS;
    }

    private void load(Path input, PrintStream out, PrintStream err) {
        out.println("Loading " + input);

        Pack pack;
        try {
            pack = PackReaders.read(input);
        } catch (IOException | JsonException e) {
            e.printStackTrace(err);
            err.println("Failed to read pack at " + input);

            return;
        }

        if (!helper.addPack(pack)) {
            boolean answer = ScanUtils.yesNoQuestion(
                    "A pack with named %s already exists. Did you want to overwrite it?".formatted(pack.name()),
                    ScanUtils.DEFAULT_NO);

            if (answer) {
                helper.addPackReplace(pack);
                helper.selectPack(pack);
            }
        } else {
            helper.selectPack(pack);
        }
    }

    @Override
    public void complete(LineReader reader, CommandLine.CommandSpec command, List<Candidate> candidates, CommandLine.OptionSpec option, String argument) {
        if (option == null) {
            FileNameCompleter.INSTANCE.complete(reader, Objects.requireNonNullElse(argument, ""), candidates);
        }
    }

    @Override
    public String getName() {
        return "load";
    }

    @Override
    public String getShortDescription() {
        return "Load a pack (.8xv or sok)";
    }

    @Override
    public String[] getUsage() {
        return new String[0];
    }
}
