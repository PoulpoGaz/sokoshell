package fr.valax.sokoshell;

import fr.valax.args.api.Option;
import fr.valax.args.utils.ArgsUtils;
import fr.valax.sokoshell.solver.Pack;
import fr.valax.sokoshell.utils.GlobIterator;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.util.ArrayList;
import java.util.List;

public abstract class PackCommand extends AbstractCommand {

    @Option(names = {"p", "pack"}, hasArgument = true, argName = "Pack name")
    protected String name;

    protected Pack getPack() {
        if (name == null) {
            Pack selected = helper.getSelectedPack();

            if (selected == null) {
                System.out.println("No pack selected");
            }

            return selected;
        } else {

            Pack pack = helper.getPack(name);

            if (pack == null) {
                System.out.printf("No pack named %s exists%n", name);
            }

            return pack;
        }
    }

    protected List<Pack> getPackMultiple() {
        if (name == null) {
            Pack selected = helper.getSelectedPack();

            if (selected == null) {
                return List.of();
            }

            return List.of(selected);
        } else {
            GlobIterator<Pack> it = new GlobIterator<>(name, helper.getPacks(), Pack::name);
            List<Pack> packs = new ArrayList<>();

            while (it.hasNext()) {
                packs.add(it.next());
            }

            return packs;
        }
    }

    @Override
    public void completeOption(LineReader reader, String argument, List<Candidate> candidates, Option option) {
        if (ArgsUtils.contains(option.names(), "p")) {
            helper.addPackCandidates(candidates);
        }
    }
}
