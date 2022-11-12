package fr.valax.sokoshell.graphics;

import org.jline.utils.AttributedStyle;

import java.awt.*;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;

public class MemoryBar extends Component {

    private static final AttributedStyle BG_WHITE = AttributedStyle.DEFAULT.background(AttributedStyle.WHITE);
    private static final AttributedStyle TEXT = AttributedStyle.DEFAULT;
    private static final AttributedStyle TEXT_HOVER = AttributedStyle.DEFAULT.background(AttributedStyle.WHITE).foreground(AttributedStyle.BLACK);

    private String text;
    private float percent;

    private long lastTime = 0;

    public MemoryBar() {

    }

    @Override
    public void draw(Graphics g) {
        int len = (int) (getWidth() * percent);

        g.setChar(' ');
        g.setStyle(BG_WHITE);
        g.fillRectangle(0, 0, len, getHeight());

        if (text != null) {
            int textX = (getWidth() - text.length()) / 2;
            int textY = (getHeight() - 1) / 2;

            Surface s = g.getSurface();
            if (textX + text.length() < len) {
                s.draw(text, TEXT_HOVER, textX, textY);

            } else if (textX < len) {
                String firstPart = text.substring(0, len - textX);
                s.draw(firstPart, TEXT_HOVER, textX, textY);

                String secondPart = text.substring(len - textX);
                s.draw(secondPart, TEXT, len, textY);
            } else {
                s.draw(text, TEXT, textX, textY);
            }
        }
    }

    @Override
    public void update() {
        if (lastTime + 1000 < System.currentTimeMillis()) {
            MemoryUsage usage = getMemoryUsage();

            text = asMegaByte(usage.getUsed()) + " of " + asMegaByte(usage.getCommitted()) + "M";
            percent = (float) usage.getUsed() / usage.getCommitted();

            lastTime = System.currentTimeMillis();
            repaint();
        }
    }

    private long asMegaByte(long bytes) {
        return bytes / (1024 * 1024);
    }

    @Override
    protected Dimension compPreferredSize() {
        MemoryUsage mem = getMemoryUsage();
        String text = asMegaByte(mem.getCommitted()) + " of " + asMegaByte(mem.getCommitted()) + "M";

        return new Dimension(text.length(), 1);
    }

    private MemoryUsage getMemoryUsage() {
        return ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
    }
}
