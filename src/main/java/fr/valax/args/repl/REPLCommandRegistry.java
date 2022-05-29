package fr.valax.args.repl;

import fr.valax.args.CommandLine;
import fr.valax.args.api.Command;
import fr.valax.args.api.CommandDescriber;
import fr.valax.args.api.Option;
import fr.valax.args.api.OptionGroup;
import fr.valax.args.utils.INode;
import org.jline.console.CmdDesc;
import org.jline.console.CommandRegistry;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.jline.reader.impl.completer.SystemCompleter;
import org.jline.utils.AttributedString;

import java.util.*;
import java.util.stream.Collectors;

import static fr.valax.args.utils.ArgsUtils.first;

public class REPLCommandRegistry implements CommandRegistry {

    private final String name;
    private final CommandLine cli;
    private final Set<String> commands;

    public REPLCommandRegistry(CommandLine cli) {
        this(cli, null);
    }

    public REPLCommandRegistry(CommandLine cli, String name) {
        this.cli = cli;
        this.name = name == null ? CommandRegistry.super.name() : name;
        commands = new HashSet<>();

        addCommand(cli.getCommands(), null);
    }

    @Override
    public String name() {
        return name;
    }

    private void addCommand(INode<CommandDescriber> node, String previousName) {
        CommandDescriber spec = node.getValue();

        if (spec != null) {
            if (previousName == null) {
                previousName = spec.getName();
            } else {
                previousName = previousName + " " + spec.getName();
            }

            commands.add(previousName);
        }

        for (INode<CommandDescriber> child : node.getChildren()) {
            addCommand(child, previousName);
        }
    }

    @Override
    public Set<String> commandNames() {
        return commands;
    }

    @Override
    public Map<String, String> commandAliases() {
        return Map.of();
    }

    @Override
    public List<String> commandInfo(String command) {
        String info = cli.getCommandHelp(command);

        return Arrays.asList(info.split("[\\n\\r]"));
    }

    @Override
    public boolean hasCommand(String command) {
        return cli.getCommand(command).node() != null;
    }

    @Override
    public SystemCompleter compileCompleters() {
        SystemCompleter completer = new SystemCompleter();

        for (INode<CommandDescriber> command : cli.getCommands().getChildren()) {
            completer.add(command.getValue().getName(), new ShellCompleter());
        }
        completer.compile();

        return completer;
    }

    @Override
    public CmdDesc commandDescription(List<String> args) {
        INode<CommandDescriber> node = cli.getCommand(String.join(" ", args)).node();

        if (node == null || node.getValue() == null) {
            return null;
        }

        CommandDescriber desc = node.getValue();
        String usage = desc.getUsage();

        List<AttributedString> mainDesc;
        if (usage != null) {
            mainDesc = Arrays.stream(usage.split("[\\n\\r]"))
                    .map(AttributedString::new)
                    .toList();
        } else {
            mainDesc = List.of();
        }

        Map<String, List<AttributedString>> options = new HashMap<>();

        for (Option option : desc) {
            String key = Arrays.stream(option.names())
                    .map(s -> '-' + s)
                    .collect(Collectors.joining(" "));

            String description = first(option.description());
            if (description != null) {
                List<AttributedString> value = Arrays.stream(description.split("[\\n\\r]"))
                        .map(AttributedString::new)
                        .toList();

                options.put(key, value);
            } else {
                options.put(key, List.of());
            }
        }

        return new CmdDesc(mainDesc, List.of(), options);
    }

    @Override
    public Object invoke(CommandSession session, String command, Object... args) throws Exception {
        String[] arguments = new String[1 + args.length];
        arguments[0] = command;

        for (int i = 0; i < args.length; i++) {
            arguments[i + 1] = args[i].toString();
        }

        return cli.execute(arguments);
    }

    private class ShellCompleter implements Completer {

        @Override
        public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
            String l = line.words().stream()
                    .limit(line.wordIndex() + 1)
                    .collect(Collectors.joining(" "));

            CommandLine.ParsedCommandDesc cmd = cli.getCommand(l);
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
                String name = '-' + first(option.names());
                String groupName = group == null ? null : group.name();
                String description = first(option.description());

                candidates.add(new Candidate(name, name, groupName, description, null, null, true));
            }
        }

        /**
         * @return true if an option wait for his argument
         */
        private boolean completeOptionArgument(LineReader reader, ParsedLine line, List<Candidate> candidates,
                                               CommandDescriber desc, Command<?> command) {
            if (line.wordIndex() == 0) {
                return false;
            }

            String last = line.words().get(line.wordIndex() - 1);

            if (last == null || !last.startsWith("-")) {
                return false;
            }

            Option option = desc.getOption(last.substring(1));

            if (option != null && option.hasArgument()) {
                if (command instanceof REPLCommand<?> replCommand) {
                    replCommand.completeOption(reader, line, candidates, option);
                }

                return true;
            } else {
                return false;
            }
        }
    }
}
