package fr.valax.args.jline;

import fr.valax.args.DefaultHelpFormatter;
import fr.valax.args.api.CommandDescriber;
import fr.valax.args.api.Option;
import fr.valax.args.utils.ArgsUtils;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

public class REPLHelpFormatter extends DefaultHelpFormatter {

    @Override
    protected void appendCommandShortDescription(StringBuilder sb, CommandDescriber desc) {
        String shortDesc = desc.getShortDescription();

        sb.append(withAttribute(desc.getName(), AttributedStyle.BOLD));

        if (shortDesc != null && !shortDesc.isEmpty() && !shortDesc.isBlank()) {
            sb.append(" - ").append(shortDesc);
        }

        sb.append('\n');
    }

    @Override
    protected void appendCommandUsage(StringBuilder sb, CommandDescriber desc) {
        String[] usage = desc.getUsage();

        if (usage == null || usage.length == 0) {
            return;
        }

        sb.append(withAttribute("Usage", AttributedStyle.BOLD.foreground(AttributedStyle.BLUE)));
        sb.append(": ");

        for (String s : usage) {
            sb.append(s).append('\n');
        }
    }

    @Override
    protected void appendOption(StringBuilder builder, Option option, boolean inGroup, String descriptionIndent, int optionNamesWidth) {
        if (inGroup) {
            builder.append(" ");
        }

        AttributedStringBuilder asb = new AttributedStringBuilder();
        asb.style(AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));

        String[] names = option.names();
        for (int i = 0; i < names.length; i++) {
            String name = names[i];

            asb.append('-').append(name);

            if (i + 1 < names.length) {
                asb.append(", ");
            }
        }

        // arg
        if (option.hasArgument()) {
            String arg = ArgsUtils.first(option.argName());

            asb.style(AttributedStyle.DEFAULT.foreground(AttributedStyle.BRIGHT));
            asb.append(" <")
                    .append(arg == null ? DEFAULT_ARG_NAME : arg)
                    .append(">");
        }

        // space
        int pos = asb.length();
        asb.append(" ".repeat(spaceBetweenTextBlockAndName + optionNamesWidth - pos));

        builder.append(asb.toAnsi());


        // description
        String desc =  ArgsUtils.first(option.description());
        if (desc != null) {
            appendTextBlock(builder, desc, descriptionIndent, maxTextBlockSize);
        }

        builder.append('\n');
    }


    @Override
    protected void appendCommandName(StringBuilder sb, String name) {
        sb.append(withAttribute(name, AttributedStyle.BOLD));
    }

    private String withAttribute(String str, AttributedStyle style) {
        return new AttributedString(str, style).toAnsi();
    }
}
