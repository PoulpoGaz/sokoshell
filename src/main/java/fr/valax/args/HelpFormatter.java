package fr.valax.args;

import fr.valax.args.annotation.Command;
import fr.valax.args.utils.ArgsUtils;

import java.util.Comparator;
import java.util.List;

public class HelpFormatter {

    /** Max number of characters for each help string per line  */
    private static final int MAX_OPT_HELP_WIDTH = 74;

    private static final String DEFAULT_ARG_NAME = "ARG";

    private static final Comparator<OptionSpec> optComparator = ArgsUtils.comparing(OptionSpec::firstName);
    private static final Comparator<OptionGroupSpec> groupComparator = ArgsUtils.comparing(OptionGroupSpec::getName);

    public static void printHelp(Runnable runnable) {
        System.out.println(helpString(runnable));
    }

    public static String helpString(Runnable runnable) {
        Options options = Options.createFrom(runnable);

        String usage = runnable.getClass()
                .getAnnotation(Command.class)
                .usage();

        return helpString(usage, options);
    }

    public static void printHelp(String usage, Options options) {
        System.out.println(helpString(usage, options));
    }

    public static String helpString(String usage, Options options) {
        StringBuilder builder = new StringBuilder();
        builder.append("Usage: ").append(usage).append("\n\n");

        OptionGroupSpec unnamed = options.getGroup(null);
        if (unnamed != null) {
            printGroup(builder, unnamed);
        }

        List<OptionGroupSpec> groups = options.getOptions().values().stream().sorted(groupComparator).toList();

        for (OptionGroupSpec g : groups) {
            if (g != unnamed) {
                printGroup(builder, g);
            }
        }

        return builder.toString();
    }

    private static void printGroup(StringBuilder builder, OptionGroupSpec group) {
        String indent = "";
        if (group.getName() != null) {
            builder.append(group.getName()).append(":\n");
            indent = " ";
        }

        int width = getWidth(group);
        String descIndent = indent + " ".repeat(10 + width);

        List<OptionSpec> options = group.getOptions().stream().sorted(optComparator).toList();
        for (OptionSpec option : options) {
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
            builder.append(" ".repeat(10 + width - (pos2 - pos)));

            // description
            String desc = option.getDescription();
            if (desc != null) {
                appendTextBlock(builder, desc, descIndent, MAX_OPT_HELP_WIDTH);
            }

            builder.append('\n');
        }
    }

    private static void appendTextBlock(StringBuilder builder, String text, String indent, int width) {
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

    private static int wordLength(String text, int start) {
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

    private static int getWidth(OptionGroupSpec group) {
        int width = 0;

        for (OptionSpec option : group.getOptions()) {
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
}
