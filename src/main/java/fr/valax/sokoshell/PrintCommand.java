package fr.valax.sokoshell;

import fr.valax.args.api.Option;
import fr.valax.args.utils.ArgsUtils;
import fr.valax.sokoshell.solver.Level;
import fr.valax.sokoshell.solver.Pack;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.jline.terminal.Terminal;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;

/**
 * @author PoulpoGaz
 */
public class PrintCommand extends PackCommand {

    @Override
    protected int executeImpl(InputStream in, PrintStream out, PrintStream err) {
        Pack pack = getPack();

        if (pack == null) {
            return FAILURE;
        }

        List<Level> levels = pack.levels();
        for (int i = 0; i < levels.size(); i++) {
            Level l = levels.get(i);

            out.printf("<===== Level n°%d =====>%n", i + 1);
            helper.getRenderer().print(out, l);
        }

        return SUCCESS;
    }

    @Override
    public String getName() {
        return "print";
    }

    @Override
    public String getShortDescription() {
        return "print a state";
    }

    @Override
    public String[] getUsage() {
        return new String[0];
    }
}
