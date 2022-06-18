package fr.valax.sokoshell;

import fr.valax.args.api.Option;
import fr.valax.args.utils.ArgsUtils;
import fr.valax.sokoshell.solver.Level;
import fr.valax.sokoshell.solver.Pack;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.jline.terminal.Terminal;

import java.util.List;

/**
 * @author PoulpoGaz
 */
public class PrintCommand extends AbstractVoidCommand {

    @Option(names = {"p", "-pack"}, hasArgument = true, argName = "Pack name")
    private String packName;

    @Override
    public void run() {
        if (packName != null && !packName.isBlank()) {
            printPack(packName);
        }
    }

    private void printPack(String name) {
        Pack pack = helper.getPack(name);

        if (pack == null) {
            System.out.println("Can't find a pack named " + name);
            return;
        }

        Terminal terminal = helper.getTerminal();
        List<Level> levels = pack.levels();
        for (int i = 0; i < levels.size(); i++) {
            Level l = levels.get(i);

            terminal.writer().printf("<===== Level n°%d =====>%n", i + 1);
            helper.getRenderer().print(terminal, l);
        }
    }

    @Override
    public void completeOption(LineReader reader, ParsedLine line, List<Candidate> candidates, Option option) {
        if (ArgsUtils.contains(option.names(), "p")) {
            helper.addPackCandidates(candidates);
        }
    }

    @Override
    public String getName() {
        return "print";
    }

    @Override
    public String getUsage() {
        return "print a state";
    }
}
