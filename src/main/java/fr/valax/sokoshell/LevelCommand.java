package fr.valax.sokoshell;

import fr.valax.args.api.Option;
import fr.valax.args.utils.ArgsUtils;
import fr.valax.sokoshell.solver.Level;
import fr.valax.sokoshell.solver.Pack;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.util.List;

public abstract class LevelCommand extends PackCommand {

    @Option(names = {"i", "index"}, hasArgument = true, argName = "Level index")
    protected Integer index;

    protected Level getLevel() {
        Pack pack = getPack();

        if (pack == null) {
            return null;
        }

        if (index == null) {
            Level selected = helper.getSelectedLevel();

            if (selected == null) {
                System.err.println("No level selected");
            }

            return selected;
        } else {
            index--;
            if (index < 0 || index >= pack.levels().size()) {
                System.out.println("Index out of bounds");
                return null;
            }

            return pack.levels().get(index);
        }
    }
}
