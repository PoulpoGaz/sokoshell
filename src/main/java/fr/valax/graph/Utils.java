package fr.valax.graph;

import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Colors from discord dark theme
 */
public class Utils {

    public static final Color BACKGROUND_1 = new Color(54, 57, 63);
    public static final Color BACKGROUND_2 = new Color(47, 49, 54);
    public static final Color BACKGROUND_3 = new Color(32, 34, 37);

    public static final Color TEXT_COLOR = new Color(220, 221, 222);

    public static final Font DISCORD_FONT = new Font("Whitney", Font.PLAIN, 24);

    private static final BigDecimal _100_ = BigDecimal.valueOf(100);

    public static double asPercentage(int v, BigDecimal max, int precision) {
        return BigDecimal.valueOf(v)
                .multiply(_100_)
                .divide(max, precision, RoundingMode.HALF_EVEN)
                .doubleValue();
    }

    public static void setup(Graphics2D g2d, int width, int height, Font font, Color background) {
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        g2d.setBackground(background);
        g2d.clearRect(0, 0, width, height);

        if (font != null) {
            g2d.setFont(font);
        }
    }

    public static int roundUp(int value, int mod) {
        return (value + mod - 1) / mod * mod;
    }

    public static int roundDown(int value, int multiple) {
        return value / multiple * multiple;
    }
}
