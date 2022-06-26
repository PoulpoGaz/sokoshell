package fr.valax.args.jline;

import fr.valax.args.CommandLine;
import fr.valax.args.api.Command;
import fr.valax.args.api.CommandDescriber;
import fr.valax.args.api.Option;
import fr.valax.args.api.OptionGroup;
import fr.valax.args.utils.ArgsUtils;
import fr.valax.args.utils.INode;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ShellCompleter implements Completer {

    protected final CommandLine cli;

    public ShellCompleter(CommandLine cli) {
        this.cli = cli;
    }

    @Override
    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
        String l = line.words().stream()
                .limit(line.wordIndex() + 1)
                .collect(Collectors.joining(" "));

        CommandLine.ParsedCommand cmd = cli.getFirstCommandWithIndex(l);
        INode<CommandDescriber> node = cmd.node();

        if (node != null && node.getValue() != null) {
            CommandDescriber desc = node.getValue();

            // don't show sub commands if user start to type options
            if (cmd.index() == line.wordIndex()) {
                for (INode<CommandDescriber> child : node.getChildren()) {
                    candidates.add(new Candidate(child.getValue().getName()));
                }
            }

            boolean addHyphenCandidate = desc.nOptions() > 0;

            if (line.words().get(line.wordIndex()).startsWith("-")) {
                for (Map.Entry<OptionGroup, List<Option>> options : desc.getOptions().entrySet()) {
                    completeOption(candidates, options.getKey(), options.getValue());
                }

                addHyphenCandidate = false;
            } else {
                if (completeOptionArgument(reader, line, candidates, desc, desc.getCommand())) {
                    addHyphenCandidate = false;
                }
            }

            if (addHyphenCandidate) {
                candidates.add(new Candidate("-", "-", null, null, null, null, false));
            }
        }
    }

    private void completeOption(List<Candidate> candidates, OptionGroup group, List<Option> options) {
        for (Option option : options) {
            String name = '-' + ArgsUtils.first(option.names());
            String groupName = group == null ? null : group.name();
            String description = ArgsUtils.first(option.description());

            candidates.add(new Candidate(name, name, groupName, description, null, null, true));
        }
    }

    /**
     * @return true if an option wait for his argument
     */
    private boolean completeOptionArgument(LineReader reader, ParsedLine line, List<Candidate> candidates,
                                           CommandDescriber desc, Command command) {
        if (line.wordIndex() == 0) {
            return false;
        }

        String last = line.words().get(line.wordIndex() - 1);

        if (last == null || !last.startsWith("-")) {
            return false;
        }

        Option option = desc.getOption(last.substring(1));

        if (option != null && option.hasArgument()) {
            if (command instanceof JLineCommand JLineCommand) {
                JLineCommand.completeOption(reader, line, candidates, option);
            }

            return true;
        } else {
            return false;
        }
    }
}