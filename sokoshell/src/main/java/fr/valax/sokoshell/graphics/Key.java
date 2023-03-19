package fr.valax.sokoshell.graphics;

import org.jline.keymap.KeyMap;
import org.jline.utils.InfoCmp;

public interface Key {

    default void bind(TerminalEngine engine) {
        engine.getKeyMap().bind(this, toString(engine));
    }

    default void unbind(TerminalEngine engine) {
        engine.getKeyMap().unbind(toString(engine));
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

    record ConcatKey(Key[] keys) implements Key {

        @Override
        public String toString(TerminalEngine engine) {
            if (keys.length == 1) {
                return keys[0].toString(engine);
            } else {
                StringBuilder sb = new StringBuilder();

                for (Key key : keys) {
                    sb.append(key.toString(engine));
                }

                return sb.toString();
            }
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
    Key ENTER = new SimpleKey("\r"); // TODO: for some reason, using InfoCmp.Capability.key_enter doesn't work.
    Key BACKSPACE = new InfoCmpCapabilityKey(InfoCmp.Capability.key_backspace);

    Key ESCAPE = new SimpleKey(KeyMap.esc());
    Key DELETE = new SimpleKey(KeyMap.del());
    Key SPACE = new SimpleKey(" ");

    Key CTRL_A = ctrl('a');
    Key CTRL_B = ctrl('b');
    Key CTRL_C = ctrl('c');
    Key CTRL_D = ctrl('d');
    Key CTRL_E = ctrl('e');
    Key CTRL_F = ctrl('f');
    Key CTRL_G = ctrl('g');
    Key CTRL_H = ctrl('h');
    Key CTRL_I = ctrl('i');
    Key CTRL_J = ctrl('j');
    Key CTRL_K = ctrl('k');
    Key CTRL_L = ctrl('l');
    Key CTRL_M = ctrl('m');
    Key CTRL_N = ctrl('n');
    Key CTRL_O = ctrl('o');
    Key CTRL_P = ctrl('p');
    Key CTRL_Q = ctrl('q');
    Key CTRL_R = ctrl('r');
    Key CTRL_S = ctrl('s');
    Key CTRL_T = ctrl('t');
    Key CTRL_U = ctrl('u');
    Key CTRL_V = ctrl('v');
    Key CTRL_W = ctrl('w');
    Key CTRL_X = ctrl('x');
    Key CTRL_Y = ctrl('y');
    Key CTRL_Z = ctrl('z');

    Key MOUSE = new InfoCmpCapabilityKey(InfoCmp.Capability.key_mouse);

    static Key alt(String c) {
        return new SimpleKey(KeyMap.alt(c));
    }

    static Key ctrl(char key) {
        return new SimpleKey(KeyMap.ctrl(key));
    }

    static Key concat(Key... keys) {
        if (keys == null || keys.length == 0) {
            return null;
        } else if (keys.length == 1) {
            return keys[0];
        } else {
            return new ConcatKey(keys);
        }
    }
}
