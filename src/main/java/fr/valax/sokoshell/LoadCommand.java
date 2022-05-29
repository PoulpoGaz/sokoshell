package fr.valax.sokoshell;

import fr.valax.args.api.Option;
import fr.valax.sokoshell.loader.PackReaders;
import fr.valax.sokoshell.solver.Pack;
import fr.valax.sokoshell.utils.ScanUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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
    public String getName() {
        return "load";
    }

    @Override
    public String getUsage() {
        return null;
    }
}
