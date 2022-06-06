package fr.valax.sokoshell.graphics;

import org.jline.terminal.Terminal;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;

public class TrueColorString extends AttributedString {

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
        return super.toAnsi(TRUE_COLORS, ForceMode.ForceTrueColors);
    }
}
