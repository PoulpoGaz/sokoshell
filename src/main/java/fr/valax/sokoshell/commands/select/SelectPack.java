package fr.valax.sokoshell.commands.select;

import fr.valax.args.api.VaArgs;
import fr.valax.sokoshell.commands.AbstractCommand;
import fr.valax.sokoshell.solver.Pack;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;

public class SelectPack extends AbstractCommand {

    @VaArgs
    private String[] pack;

    @Override
    protected int executeImpl(InputStream in, PrintStream out, PrintStream err) {
        if (pack.length == 0) {
            helper.selectPack(null);
        } else {
            Pack pack = helper.getPack(this.pack[0]);

            if (pack == null) {
                err.printf("No pack named %s%n", this.pack[0]);
                return FAILURE;
            } else {
                helper.selectPack(pack);
            }
        }

        return SUCCESS;
    }

    @Override
    public String getName() {
        return "pack";
    }

    @Override
    public String getShortDescription() {
        return "select a pack";
    }

    @Override
    public String[] getUsage() {
        return new String[0];
    }

    @Override
    public void completeVaArgs(LineReader reader, String argument, List<Candidate> candidates) {
        helper.addPackCandidates(candidates);
    }
}