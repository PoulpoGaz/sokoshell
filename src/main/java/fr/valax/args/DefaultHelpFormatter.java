package fr.valax.args;

import fr.valax.args.api.CommandDescriber;
import fr.valax.args.api.HelpFormatter;
import fr.valax.args.api.Option;
import fr.valax.args.api.OptionGroup;
import fr.valax.args.utils.ArgsUtils;
import fr.valax.args.utils.CommandLineException;
import fr.valax.args.utils.INode;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * @author PoulpoGaz
 */
public class DefaultHelpFormatter implements HelpFormatter {

    private static final String DEFAULT_ARG_NAME = "ARG";

    private static final Comparator<Option> optComparator =
            ArgsUtils.comparing((o) -> o.names()[0]);

    private static final Comparator<Map.Entry<OptionGroup, ?>> groupComparator =
            ArgsUtils.comparing((o) -> o.getKey() == null ? null : o.getKey().name());


    private int maxTextBlockSize = 100;
    private int spaceBetweenTextBlockAndName = 10;


    @Override
    public String commandHelp(CommandLineException error, CommandDescriber command) {
        Map<OptionGroup, List<Option>> options = command.getOptions();

        StringBuilder builder = new StringBuilder();
        if (error != null) {
            builder.append(error.getMessage()).append('\n');
        }

        builder.append("Command: ").append(command.getName()).append('\n');

        String usage = command.getUsage();
        if (usage != null) {
            builder.append("Usage: ").append(usage).append("\n\n");
        }

        options.entrySet()
                .stream()
                .sorted(groupComparator)
                .forEach((opt) -> {
                    printGroup(builder, opt.getKey(), opt.getValue());
                });

        return builder.toString();
    }

    private void printGroup(StringBuilder builder, OptionGroup group, List<Option> options) {
        String indent = "";

        if (group != null) {
            builder.append(group.name()).append(":\n");
            indent = " ";
        }

        int width = getWidth(options);
        String descIndent = indent + " ".repeat(spaceBetweenTextBlockAndName + width);

        List<Option> opt = options.stream().sorted(optComparator).toList();
        for (Option option : opt) {
            builder.append(indent);

            int pos = builder.length();

            String[] names = option.names();
            for (int i = 0; i < names.length; i++) {
                String name = names[i];

                builder.append('-').append(name);

                if (i + 1 < names.length) {
                    builder.append(", ");
                }
            }

            // arg
            if (option.hasArgument()) {
                String arg = ArgsUtils.first(option.argName());

                builder.append(" <")
                        .append(arg == null ? DEFAULT_ARG_NAME : arg)
                        .append(">");
            }

            // space
            int pos2 = builder.length();
            builder.append(" ".repeat(spaceBetweenTextBlockAndName + width - (pos2 - pos)));

            // description
            String desc =  ArgsUtils.first(option.description());
            if (desc != null) {
                appendTextBlock(builder, desc, descIndent, maxTextBlockSize);
            }

            builder.append('\n');
        }
    }

    private int getWidth(List<Option> options) {
        int width = 0;

        for (Option option : options) {
            int w = 0;

            String[] names = option.names();
            for (int i = 0; i < names.length; i++) {
                String name = names[i];

                w += name.length() + 1; // -

                if (i + 1 < names.length) {
                    w += 2; // comma + space
                }
            }

            if (option.hasArgument()) {
                String argName = ArgsUtils.first(option.argName());

                if (argName != null) {
                    w += argName.length() + 2; // < and >
                } else {
                    w += DEFAULT_ARG_NAME.length() + 2; // < and >
                }
            }

            width = Math.max(width, w);
        }

        return width;
    }

    @Override
    public String generalHelp(INode<CommandDescriber> commands,
                              String[] args,
                              boolean unrecognizedCommand) {

        StringBuilder builder = new StringBuilder();

        if (unrecognizedCommand) {
            builder.append("Unrecognized command");

            if (args != null) {
                String commandString = String.join(" ", args);
                builder.append(": ")
                        .append(commandString)
                        .append("\n\n");

            }
        }

        builder.append("Commands:\n");

        int maxCommandNameSize = getMaxCommandNameSize(commands, "");

        // +1 because of ':'
        addCommand(builder, commands, "", " ".repeat(maxCommandNameSize + 1 + spaceBetweenTextBlockAndName));

        return builder.toString();
    }

    private int getMaxCommandNameSize(INode<CommandDescriber> commands, String fullCommandName) {
        if (commands.getValue() != null) {
            CommandDescriber spec = commands.getValue();

            if (fullCommandName.isBlank()) {
                fullCommandName = spec.getName();
            } else {
                fullCommandName = fullCommandName + " " + spec.getName();
            }
        }

        int w = fullCommandName.length();

        for (INode<CommandDescriber> child : commands.getChildren()) {
            w = Math.max(getMaxCommandNameSize(child, fullCommandName), w);
        }

        return w;
    }

    private void addCommand(StringBuilder builder,
                            INode<CommandDescriber> command,
                            String fullCommandName,
                            String usageIdent) {
        if (command.getValue() != null) {
            CommandDescriber spec = command.getValue();

            if (fullCommandName.isBlank()) {
                fullCommandName = spec.getName();
            } else {
                fullCommandName = fullCommandName + " " + spec.getName();
            }

            builder.append(fullCommandName);

            String usage = spec.getUsage();
            if (usage != null) {
                builder.append(':');

                int emptySpaceLength = usageIdent.length() - fullCommandName.length() - 1;
                builder.append(" ".repeat(emptySpaceLength));

                appendTextBlock(builder, usage, usageIdent, maxTextBlockSize);
            }

            builder.append('\n');
        }

        for (INode<CommandDescriber> child : command.getChildren()) {
            addCommand(builder, child, fullCommandName, usageIdent);
        }

    }

    private void appendTextBlock(StringBuilder builder, String text, String indent, int width) {
        int index = 0;
        int x = 0;
        while (index < text.length()) {
            int l = wordLength(text, index);

            if (l + x < width) {
                builder.append(text, index, index + l).append(" ");
                x += l + 1;
                index += l + 1;
            } else if (x == 0) { // the whole word doesn't fit
                l = width - 1;
                builder.append(text, index, index + l).append("-");
                builder.append("\n").append(indent);
                index += l + 1;
            } else {
                x = 0;
                builder.append("\n").append(indent);
            }
        }
    }

    private int wordLength(String text, int start) {
        int length = 0;
        for (int i = start; i < text.length(); i++) {
            char c = text.charAt(i);

            if (Character.isWhitespace(c)) {
                break;
            }
            length++;
        }

        return length;
    }

    public int getMaxTextBlockSize() {
        return maxTextBlockSize;
    }

    public void setMaxTextBlockSize(int maxTextBlockSize) {
        this.maxTextBlockSize = maxTextBlockSize;
    }

    public int getSpaceBetweenTextBlockAndName() {
        return spaceBetweenTextBlockAndName;
    }

    public void setSpaceBetweenTextBlockAndName(int spaceBetweenTextBlockAndName) {
        this.spaceBetweenTextBlockAndName = spaceBetweenTextBlockAndName;
    }
}
