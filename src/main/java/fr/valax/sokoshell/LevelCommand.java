package fr.valax.sokoshell;

import fr.valax.args.api.Option;
import fr.valax.args.utils.ArgsUtils;
import fr.valax.sokoshell.solver.Level;
import fr.valax.sokoshell.solver.Pack;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.util.List;

@Deprecated
public abstract class LevelCommand extends PackCommand {

    @Option(names = {"i", "index"}, hasArgument = true, argName = "Level index")
    protected Integer index;
}
