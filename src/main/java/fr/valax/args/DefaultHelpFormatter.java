package fr.valax.args;

import fr.valax.args.api.*;
import fr.valax.args.utils.ArgsUtils;
import fr.valax.args.utils.INode;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * @author PoulpoGaz
 */
public class DefaultHelpFormatter implements HelpFormatter {

    protected static final String DEFAULT_ARG_NAME = "ARG";

    protected static final Comparator<Option> optComparator =
            ArgsUtils.comparing((o) -> o.names()[0]);

    protected static final Comparator<Map.Entry<OptionGroup, ?>> groupComparator =
            ArgsUtils.comparing((o) -> o.getKey() == null ? null : o.getKey().name());


    protected int maxTextBlockSize = 100;
    protected int spaceBetweenTextBlockAndName = 10;


    @Override
    public String commandHelp(CommandDescriber command) {
        Map<OptionGroup, List<Option>> options = command.getOptions();

        StringBuilder builder = new StringBuilder();

        appendCommandShortDescription(builder, command);
        appendCommandUsage(builder, command);
        builder.append('\n');

        appendVaArgs(builder, command);

        final int width = getOptionsNamesWidth(command.optionIterator());

        options.entrySet()
                .stream()
                .sorted(groupComparator)
                .forEach((opt) -> {
                    appendGroup(builder, width, opt.getKey(), opt.getValue());
                });

        return builder.toString();
    }

    protected void appendCommandShortDescription(StringBuilder sb, CommandDescriber desc) {
        String shortDesc = desc.getShortDescription();

        sb.append(desc.getName());;

        if (shortDesc != null && !shortDesc.isEmpty() && !shortDesc.isBlank()) {
            sb.append(" - ").append(shortDesc);
        }

        sb.append('\n');
    }

    protected void appendCommandUsage(StringBuilder sb, CommandDescriber desc) {
        String[] usage = desc.getUsage();

        if (usage == null || usage.length == 0) {
            return;
        }

        sb.append("Usage: ");

        for (String s : usage) {
            sb.append(s).append('\n');
        }
    }

    protected void appendVaArgs(StringBuilder sb, CommandDescriber desc) {
        if (desc.hasVaArgs()) {
            String description = desc.getVaArgsDescription();

            if (description != null) {
                sb.append("Vaargs: ");
                appendTextBlock(sb, description, " ".repeat(8), maxTextBlockSize);
            } else {
                sb.append("Has vaargs");
            }

            sb.append('\n');
        }
    }

    private void appendGroup(StringBuilder builder, int width, OptionGroup group, List<Option> options) {
        if (group != null) {
            builder.append(group.name()).append(":\n");
        }

        String descriptionIndent = " ".repeat(spaceBetweenTextBlockAndName + width);

        List<Option> opt = options.stream().sorted(optComparator).toList();
        for (Option option : opt) {
            appendOption(builder, option, group != null, descriptionIndent, width);
        }
    }

    protected void appendOption(StringBuilder builder,
                                Option option,
                                boolean inGroup,
                                String descriptionIndent,
                                int optionNamesWidth) {
        if (inGroup) {
            builder.append(" ");
        }

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
        builder.append(" ".repeat(spaceBetweenTextBlockAndName + optionNamesWidth - (pos2 - pos)));

        // description
        String desc =  ArgsUtils.first(option.description());
        if (desc != null) {
            appendTextBlock(builder, desc, descriptionIndent, maxTextBlockSize);
        }

        builder.append('\n');
    }

    private int getOptionsNamesWidth(OptionIterator it) {
        int width = 0;

        while (it.hasNext()) {
            Option option = it.next();

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

            if (it.currentGroup() != null) {
                w++;
            }

            width = Math.max(width, w);
        }

        return width;
    }

    @Override
    public String generalHelp(INode<CommandDescriber> commands) {

        StringBuilder builder = new StringBuilder();
        builder.append("Commands:\n");

        int maxCommandNameSize = getMaxCommandNameSize(commands, "");

        // +1 because of ':'
        addCommand(builder, commands, "", " ".repeat(maxCommandNameSize + 1 + spaceBetweenTextBlockAndName));

        return builder.toString();
    }

    protected int getMaxCommandNameSize(INode<CommandDescriber> commands, String fullCommandName) {
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

    protected void addCommand(StringBuilder builder,
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

            appendCommandName(builder, fullCommandName);

            String description = spec.getShortDescription();
            if (description != null) {
                builder.append(':');

                int emptySpaceLength = usageIdent.length() - fullCommandName.length() - 1;
                builder.append(" ".repeat(emptySpaceLength));

                builder.append(description);
            }

            builder.append('\n');
        }

        for (INode<CommandDescriber> child : command.getChildren()) {
            addCommand(builder, child, fullCommandName, usageIdent);
        }
    }

    protected void appendCommandName(StringBuilder sb, String name) {
        sb.append(name);
    }

    protected void appendTextBlock(StringBuilder builder, String text, String indent, int width) {
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
