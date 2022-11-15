package fr.valax.sokoshell.graphics;

import java.util.EventListener;

public interface ActionListener extends EventListener {

    void actionPerformed(Object source, String command);
}
