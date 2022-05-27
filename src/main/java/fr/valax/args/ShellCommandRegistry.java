package fr.valax.args;

import fr.valax.args.api.Command;
import fr.valax.args.utils.Node;
import org.jline.console.ArgDesc;
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

public class ShellCommandRegistry implements CommandRegistry {

    private final CommandLine cli;
    private final Set<String> commands;
    private final Map<String, String> aliases;

    public ShellCommandRegistry(CommandLine cli) {
        this.cli = cli;
        commands = new HashSet<>();
        aliases = new HashMap<>();

        Node<CommandSpecification> commandRoot = cli.getRoot();
        addCommand(commandRoot, null);
    }

    private void addCommand(Node<CommandSpecification> node, String previousName) {
        CommandSpecification spec = node.getValue();

        if (spec != null) {
            if (previousName == null) {
                previousName = spec.getName();
            } else {
                previousName = previousName + " " + spec.getName();
            }

            commands.add(previousName);
        }

        for (Node<CommandSpecification> child : node.getChildren()) {
            addCommand(child, previousName);
        }
    }

    @Override
    public Set<String> commandNames() {
        return commands;
    }

    @Override
    public Map<String, String> commandAliases() {
        return aliases;
    }

    @Override
    public List<String> commandInfo(String command) {
        String info = cli.getCommandHelp(command);

        return Arrays.asList(info.split("[\\n\\r]"));
    }

    @Override
    public boolean hasCommand(String command) {
        return cli.getCommand(command) != null;
    }

    @Override
    public SystemCompleter compileCompleters() {
        SystemCompleter completer = new SystemCompleter();
        completer.add(new ArrayList<>(commands), new ShellCompleter());
        completer.compile();

        return completer;
    }

    @Override
    public CmdDesc commandDescription(List<String> args) {
        CommandSpecification spec = cli.getCommand(args.toArray(new String[0]));

        if (spec == null) {
            return null;
        }

        String usage = spec.getCommand().getUsage();

        List<AttributedString> mainDesc;
        if (usage != null) {
            mainDesc = Arrays.stream(usage.split("[\\n\\r]"))
                    .map(AttributedString::new)
                    .toList();
        } else {
            mainDesc = List.of();
        }

        Map<String, List<AttributedString>> options = new HashMap<>();

        for (OptionSpecification optSpec : spec.getOptions()) {
            String key = Arrays.stream(optSpec.getNames())
                    .map(s -> '-' + s)
                    .collect(Collectors.joining(" "));

            List<AttributedString> value = Arrays.stream(optSpec.getDescription().split("[\\n\\r]"))
                    .map(AttributedString::new)
                    .toList();


            options.put(key, value);
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

        }
    }
}
