package fr.valax.sokoshell;

import fr.valax.args.api.Option;
import fr.valax.args.utils.ArgsUtils;
import fr.valax.sokoshell.loader.PackReaders;
import fr.valax.sokoshell.solver.Pack;
import fr.valax.sokoshell.utils.ScanUtils;
import fr.valax.sokoshell.utils.Utils;
import org.jline.builtins.Completers;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class LoadCommand extends AbstractVoidCommand {

    public LoadCommand(SokoShellHelper helper) {
        super(helper);
    }

    @Option(names = {"i", "-input"}, hasArgument = true, argName = "input file", optional = false)
    private Path input;

    @Override
    public void run() {
        if (!Files.exists(input)) {
            System.out.printf("%s doesn't exist%n", input);
            return;
        }

        Pack pack;
        try {
            pack = PackReaders.read(input);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to read pack");

            return;
        }

        if (!helper.addPack(pack)) {
            boolean answer = ScanUtils.yesNoQuestion(
                    "A pack with this name already exists. Did you want to overwrite it?",
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
    public String getUsage() {
        return "Load a pack (.8xv or sok)";
    }
}
