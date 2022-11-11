package fr.valax.sokoshell.graphics;

import org.jline.keymap.KeyMap;
import org.jline.terminal.Terminal;
import org.jline.utils.InfoCmp;

public interface Key {

    void addTo(KeyMap<Key> keymap, Terminal terminal);

    record SimpleKey(String key) implements Key {

        @Override
        public void addTo(KeyMap<Key> keymap, Terminal terminal) {
            keymap.bind(this, key);
        }
    }

    record InfoCmpCapabilityKey(InfoCmp.Capability capability) implements Key {

        @Override
        public void addTo(KeyMap<Key> keymap, Terminal terminal) {
            keymap.bind(this, KeyMap.key(terminal, capability));
        }
    }

    Key KEY_A = new SimpleKey("a");
    Key KEY_B = new SimpleKey("b");
    Key KEY_C = new SimpleKey("c");
    Key KEY_D = new SimpleKey("d");
    Key KEY_E = new SimpleKey("e");
    Key KEY_F = new SimpleKey("f");
    Key KEY_G = new SimpleKey("g");
    Key KEY_H = new SimpleKey("h");
    Key KEY_I = new SimpleKey("i");
    Key KEY_J = new SimpleKey("j");
    Key KEY_K = new SimpleKey("k");
    Key KEY_L = new SimpleKey("l");
    Key KEY_M = new SimpleKey("m");
    Key KEY_N = new SimpleKey("n");
    Key KEY_O = new SimpleKey("o");
    Key KEY_P = new SimpleKey("p");
    Key KEY_Q = new SimpleKey("q");
    Key KEY_R = new SimpleKey("r");
    Key KEY_S = new SimpleKey("s");
    Key KEY_T = new SimpleKey("t");
    Key KEY_U = new SimpleKey("u");
    Key KEY_V = new SimpleKey("v");
    Key KEY_W = new SimpleKey("w");
    Key KEY_X = new SimpleKey("x");
    Key KEY_Y = new SimpleKey("y");
    Key KEY_Z = new SimpleKey("z");

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

}
