package fr.valax.sokoshell.graphics.style;

import org.jline.terminal.Terminal;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;

/**
 * A helper class to force use of "truecolor" (24 bits color)
 * when supported by the terminal
 */
public class TrueColorString extends AttributedString {

    public static boolean SUPPORTED = false;

    static {
        String str = System.getenv("COLORTERM");

        if (str != null && str.equalsIgnoreCase("truecolor")) {
            SUPPORTED = true;
        }
    }


    public TrueColorString(CharSequence str) {
        super(str);
    }

    public TrueColorString(CharSequence str, int start, int end) {
        super(str, start, end);
    }

    public TrueColorString(CharSequence str, AttributedStyle s) {
        super(str, s);
    }

    public TrueColorString(CharSequence str, int start, int end, AttributedStyle s) {
        super(str, start, end, s);
    }

    @Override
    public AttributedString subSequence(int start, int end) {
        return new TrueColorString(this, start, end);
    }

    @Override
    public String toAnsi(Terminal terminal) {
        if (SUPPORTED) {
            return super.toAnsi(TRUE_COLORS, ForceMode.ForceTrueColors);
        } else {
            return super.toAnsi(terminal);
        }
    }
}
