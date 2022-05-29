package fr.valax.sokoshell.utils;

import org.jline.keymap.BindingReader;
import org.jline.keymap.KeyMap;
import org.jline.terminal.Attributes;
import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.utils.*;

import static org.jline.keymap.KeyMap.key;

public class YesNoSelector {

    public enum Answer {
        YES,
        NO,
        CANCEL
    }

    public static Answer select(Terminal terminal, String title) {
        YesNoSelector selector = new YesNoSelector(terminal, title);

        return selector.select();
    }

    private enum Operation {
        LEFT,
        RIGHT,
        EXIT
    }

    private final Terminal terminal;
    private final String title;
    private final Size size = new Size();
    private final BindingReader bindingReader;

    private boolean cancel = false;

    public YesNoSelector(Terminal terminal, String title) {
        this.terminal = terminal;
        this.bindingReader = new BindingReader(terminal.reader());
        this.title = title;
    }

    private void bindKeys(KeyMap<Operation> map) {
        map.bind(Operation.LEFT, key(terminal, InfoCmp.Capability.key_left));
        map.bind(Operation.RIGHT, key(terminal, InfoCmp.Capability.key_right));
        map.bind(Operation.EXIT, "\r");
    }

    public Answer select() {
        Attributes attr = terminal.enterRawMode();

        Terminal.SignalHandler old = null;
        try {
            old = terminal.handle(Terminal.Signal.INT, this::exit);

            terminal.puts(InfoCmp.Capability.keypad_xmit);
            terminal.puts(InfoCmp.Capability.cursor_invisible);
            terminal.writer().flush();
            size.copy(terminal.getSize());

            boolean yes = false;

            KeyMap<Operation> keyMap = new KeyMap<>();
            bindKeys(keyMap);

            while (true) {
                print(yes);

                Operation op = bindingReader.readBinding(keyMap, null, false);

                if (op == Operation.LEFT || op == Operation.RIGHT) {
                    yes = !yes;
                } else if (op == Operation.EXIT) {
                    if (yes) {
                        return Answer.YES;
                    } else {
                        return Answer.NO;
                    }
                } else if (cancel) {
                    terminal.writer().write(System.lineSeparator());
                    terminal.writer().flush();
                    return Answer.CANCEL;
                }
            }
        } finally {
            if (old != null) {
                terminal.handle(Terminal.Signal.INT, old);
            }

            terminal.setAttributes(attr);
            terminal.puts(InfoCmp.Capability.keypad_local);
            terminal.puts(InfoCmp.Capability.cursor_visible);
            terminal.writer().flush();
        }
    }

    private void print(boolean yes) {
        AttributedStringBuilder builder = new AttributedStringBuilder();
        builder.append(title).append(" [");

        if (yes) {
            builder.styled(AttributedStyle.INVERSE, "YES");
            builder.append("/no");
        }  else {
            builder.append("yes/");
            builder.styled(AttributedStyle.INVERSE, "NO");
        }
        builder.append("]");

        AttributedString str = builder.toAttributedString();
        str.print(terminal);

        terminal.writer().write('\r');
        terminal.flush();
    }

    private void exit(Terminal.Signal signal) {
        cancel = true;
    }
}
