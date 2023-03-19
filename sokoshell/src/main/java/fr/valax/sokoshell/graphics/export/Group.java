package fr.valax.sokoshell.graphics.export;

import java.util.ArrayList;
import java.util.List;

public class Group {

    private int focused = 0;
    private final List<Focusable> components = new ArrayList<>();

    public Group() {

    }

    public void focusNext() {
        setFocused(focused + 1);
    }

    public void focusPrevious() {
        setFocused(focused - 1);
    }

    public void setFocused(int focused) {
        int newFocused = Math.floorMod(focused, components.size());

        components.get(this.focused).setFocus(false);
        components.get(newFocused).setFocus(true);
        this.focused = newFocused;
    }

    public void addComponent(Focusable component) {
        if (component == null) {
            return;
        }

        if (components.isEmpty()) {
            focused = 0;
            component.setFocus(true);
        }
        components.add(component);
    }
}