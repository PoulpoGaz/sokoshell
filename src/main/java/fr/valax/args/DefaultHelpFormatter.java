package fr.valax.args;

import fr.valax.args.api.HelpFormatter;
import fr.valax.args.utils.ArgsUtils;
import fr.valax.args.utils.Node;
import fr.valax.args.utils.ParseException;

import java.util.Comparator;
import java.util.List;

/**
 * @author PoulpoGaz
 */
public class DefaultHelpFormatter implements HelpFormatter {

    private static final String DEFAULT_ARG_NAME = "ARG";

    private static final Comparator<OptionSpecification> optComparator =
            ArgsUtils.comparing(OptionSpecification::firstName);

    private static final Comparator<OptionGroupSpecification> groupComparator =
            ArgsUtils.comparing(OptionGroupSpecification::getName);


    private int maxTextBlockSize = 100;
    private int spaceBetweenTextBlockAndName = 10;


    @Override
    public String commandHelp(ParseException error, CommandSpecification spec) {
        String usage = spec.getCommand().getUsage();
        Options options = spec.getOptions();

        StringBuilder builder = new StringBuilder();
        if (error != null) {
            builder.append(error.getMessage()).append('\n');
        }

        builder.append("Command: ").append(spec.getName()).append('\n');
        if (usage != null) {
            builder.append("Usage: ").append(usage).append("\n\n");
        }

        OptionGroupSpecification unnamed = options.getGroup(null);
        if (unnamed != null) {
            printGroup(builder, unnamed);
        }

        List<OptionGroupSpecification> groups = options.getOptions()
                .values()
                .stream()
                .sorted(groupComparator)
                .toList();

        for (OptionGroupSpecification g : groups) {
            if (g != unnamed) {
                printGroup(builder, g);
            }
        }

        return builder.toString();
    }

    private void printGroup(StringBuilder builder, OptionGroupSpecification group) {
        String indent = "";
        if (group.getName() != null) {
            builder.append(group.getName()).append(":\n");
            indent = " ";
        }

        int width = getWidth(group);
        String descIndent = indent + " ".repeat(spaceBetweenTextBlockAndName + width);

        List<OptionSpecification> options = group.getOptions().stream().sorted(optComparator).toList();
        for (OptionSpecification option : options) {
            builder.append(indent);

            int pos = builder.length();

            String[] names = option.getNames();
            for (int i = 0; i < names.length; i++) {
                String name = names[i];

                builder.append('-').append(name);

                if (i + 1 < names.length) {
                    builder.append(", ");
                }
            }

            // arg
            if (option.hasArgument()) {
                String arg = option.getArgumentName();

                builder.append(" <")
                        .append(arg == null ? DEFAULT_ARG_NAME : arg)
                        .append(">");
            }

            // space
            int pos2 = builder.length();
            builder.append(" ".repeat(spaceBetweenTextBlockAndName + width - (pos2 - pos)));

            // description
            String desc = option.getDescription();
            if (desc != null) {
                appendTextBlock(builder, desc, descIndent, maxTextBlockSize);
            }
        }
    }

    private int getWidth(OptionGroupSpecification group) {
        int width = 0;

        for (OptionSpecification option : group.getOptions()) {
            int w = 0;

            String[] names = option.getNames();
            for (int i = 0; i < names.length; i++) {
                String name = names[i];

                w += name.length() + 1; // -

                if (i + 1 < names.length) {
                    w += 2; // comma + space
                }
            }

            String argName = option.getArgumentName();

            if (option.hasArgument()) {
                if (option.getArgumentName() != null) {
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
    public String generalHelp(CommandSpecification parent,
                              Node<CommandSpecification> commands,
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

    private int getMaxCommandNameSize(Node<CommandSpecification> commands, String fullCommandName) {
        if (commands.getValue() != null) {
            CommandSpecification spec = commands.getValue();

            if (fullCommandName.isBlank()) {
                fullCommandName = spec.getName();
            } else {
                fullCommandName = fullCommandName + " " + spec.getName();
            }
        }

        int w = fullCommandName.length();

        for (Node<CommandSpecification> child : commands.getChildren()) {
            w = Math.max(getMaxCommandNameSize(child, fullCommandName), w);
        }

        return w;
    }

    private void addCommand(StringBuilder builder,
                            Node<CommandSpecification> command,
                            String fullCommandName,
                            String usageIdent) {
        if (command.getValue() != null) {
            CommandSpecification spec = command.getValue();

            if (fullCommandName.isBlank()) {
                fullCommandName = spec.getName();
            } else {
                fullCommandName = fullCommandName + " " + spec.getName();
            }

            builder.append(fullCommandName);

            String usage = spec.getCommand().getUsage();
            if (usage != null) {
                builder.append(':');

                int emptySpaceLength = usageIdent.length() - fullCommandName.length() - 1;
                builder.append(" ".repeat(emptySpaceLength));

                appendTextBlock(builder, usage, usageIdent, maxTextBlockSize);
            } else {
                builder.append('\n');
            }
        }

        for (Node<CommandSpecification> child : command.getChildren()) {
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

        builder.append('\n');
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
