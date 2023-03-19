package fr.valax.sokoshell.graphics.export;

import fr.valax.sokoshell.graphics.Component;

public class Focusable extends Component {

    protected boolean hasFocus;

    public boolean hasFocus() {
        return hasFocus;
    }

    public void setFocus(boolean hasFocus) {
        if (this.hasFocus != hasFocus) {
            this.hasFocus = hasFocus;
            repaint();
        }
    }
}
