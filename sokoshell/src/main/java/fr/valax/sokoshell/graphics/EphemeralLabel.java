package fr.valax.sokoshell.graphics;

import org.jline.utils.AttributedString;

import java.util.Objects;

public class EphemeralLabel extends Label {

    private long timeout = 10_000;
    private long last;

    public EphemeralLabel() {
    }

    public EphemeralLabel(AttributedString text) {
        super(text);
    }

    public EphemeralLabel(String text) {
        super(text);
    }

    public EphemeralLabel(AttributedString text, int horizAlign) {
        super(text, horizAlign);
    }

    public EphemeralLabel(String text, int horizAlign) {
        super(text, horizAlign);
    }

    public EphemeralLabel(AttributedString text, int horizAlign, int vertAlign) {
        super(text, horizAlign, vertAlign);
    }

    @Override
    public void drawComponent(Graphics g) {
        if (last + timeout > System.currentTimeMillis()) {
            super.drawComponent(g);
        }
    }

    public void show() {
        last = System.currentTimeMillis();
    }

    public void hide() {
        last = System.currentTimeMillis() - timeout;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }
}
