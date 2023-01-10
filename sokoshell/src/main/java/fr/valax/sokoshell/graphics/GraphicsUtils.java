package fr.valax.sokoshell.graphics;

import org.jline.utils.AttributedCharSequence;
import org.jline.utils.WCWidth;

public class GraphicsUtils {

    public static int columnLength(AttributedCharSequence string) {
        return columnLength(string, 0, string.length());
    }

    public static int columnLength(AttributedCharSequence string, int start, int end) {
        int cols = 0;
        int len = end - start;
        for (int cur = 0; cur < len; ) {
            int cp = string.codePointAt(cur);
            if (!string.isHidden(cur)) {
                cols += WCWidth.wcwidth(cp);
            }

            cur += Character.charCount(cp);
        }
        return cols;
    }
}
