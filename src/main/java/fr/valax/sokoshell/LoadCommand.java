package fr.valax.sokoshell;

import fr.valax.args.api.Option;
import fr.valax.args.api.VaArgs;
import fr.valax.args.utils.ArgsUtils;
import fr.valax.sokoshell.loader.PackReaders;
import fr.valax.sokoshell.solver.Pack;
import fr.valax.sokoshell.utils.GlobIterator;
import fr.valax.sokoshell.utils.ScanUtils;
import fr.valax.sokoshell.utils.Utils;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.List;

public class LoadCommand extends AbstractCommand {

    @VaArgs
    private String[] input;

    @Override
    public int executeImpl(InputStream in, PrintStream out, PrintStream err) {

        for (String input : this.input) {
            try (GlobIterator it = new GlobIterator(input)) {

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
        } catch (IOException e) {
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
            }
        }
    }

    @Override
    public void completeOption(LineReader reader, ParsedLine line, List<Candidate> candidates, Option option) {
        if (ArgsUtils.contains(option.names(), "i")) {
            Utils.FILE_NAME_COMPLETER.complete(reader, line, candidates);
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
