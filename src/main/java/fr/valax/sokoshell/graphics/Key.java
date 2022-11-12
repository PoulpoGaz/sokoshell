package fr.valax.sokoshell.graphics;

import org.jline.keymap.KeyMap;
import org.jline.utils.InfoCmp;

public interface Key {

    default void addTo(TerminalEngine engine) {
        engine.getKeyMap().bind(this, toString(engine));
    }

    String toString(TerminalEngine engine);


    record SimpleKey(String key) implements Key {

        @Override
        public String toString(TerminalEngine engine) {
            return key;
        }
    }

    record InfoCmpCapabilityKey(InfoCmp.Capability capability) implements Key {

        @Override
        public String toString(TerminalEngine engine) {
            return KeyMap.key(engine.getTerminal(), capability);
        }
    }

    Key A = new SimpleKey("a");
    Key B = new SimpleKey("b");
    Key C = new SimpleKey("c");
    Key D = new SimpleKey("d");
    Key E = new SimpleKey("e");
    Key F = new SimpleKey("f");
    Key G = new SimpleKey("g");
    Key H = new SimpleKey("h");
    Key I = new SimpleKey("i");
    Key J = new SimpleKey("j");
    Key K = new SimpleKey("k");
    Key L = new SimpleKey("l");
    Key M = new SimpleKey("m");
    Key N = new SimpleKey("n");
    Key O = new SimpleKey("o");
    Key P = new SimpleKey("p");
    Key Q = new SimpleKey("q");
    Key R = new SimpleKey("r");
    Key S = new SimpleKey("s");
    Key T = new SimpleKey("t");
    Key U = new SimpleKey("u");
    Key V = new SimpleKey("v");
    Key W = new SimpleKey("w");
    Key X = new SimpleKey("x");
    Key Y = new SimpleKey("y");
    Key Z = new SimpleKey("z");

    Key LEFT = new InfoCmpCapabilityKey(InfoCmp.Capability.key_left);
    Key RIGHT = new InfoCmpCapabilityKey(InfoCmp.Capability.key_right);
    Key DOWN = new InfoCmpCapabilityKey(InfoCmp.Capability.key_down);
    Key UP = new InfoCmpCapabilityKey(InfoCmp.Capability.key_up);
    Key ENTER = new InfoCmpCapabilityKey(InfoCmp.Capability.key_enter);
    Key BACKSPACE = new InfoCmpCapabilityKey(InfoCmp.Capability.key_backspace);

    Key ESCAPE = new SimpleKey(KeyMap.esc());
    Key DELETE = new SimpleKey(KeyMap.del());

    static Key alt(String c) {
        return new SimpleKey(KeyMap.alt(c));
    }

    static Key ctrl(char key) {
        return new SimpleKey(KeyMap.ctrl(key));
    }

    static Key concat(Key... keys) {
        if (keys.length == 0) {
            return null;
        }

        StringBuilder c = new StringBuilder();

        for (int i = 0; i < keys.length; i++) {
            c.append(keys[i]);
        }

        return new SimpleKey(c.toString());
    }
}
