package fr.valax.sokoshell.graphics;

import fr.valax.sokoshell.utils.Utils;
import org.jline.keymap.BindingReader;
import org.jline.keymap.KeyMap;
import org.jline.terminal.Attributes;
import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.utils.Display;
import org.jline.utils.InfoCmp;

import java.io.IOError;
import java.io.InterruptedIOException;
import java.util.*;
import java.util.concurrent.Future;

/**
 * TerminalEngine is an object used to facilitate drawing
 * and input reading in fullscreen mode.
 *
 * It's divided in three methods: {@link TerminalEngine#start()},
 * {@link TerminalEngine#render(Size)} and {@link TerminalEngine#update()}
 *
 * In the init method, you define shortcuts.
 * In render, you draw things with {@link Surface} and {@link Graphics} objects.
 * After drawing, you should call {@link Surface#drawBuffer(Display, int)}
 * In update method, you can read key events.
 *
 * Input:
 * Implementations declare in the init method the binding.
 * They associate to an object (which type is defined by the generic)
 * a string that represents a shortcut. For complex shortcut like ctrl+A,
 * you can use functions in {@link KeyMap}
 *
 * Usage of implementations:
 * <pre>
 *     try (MyEngine engine = new MyEngine(terminal)) {
 *         engine.loop();
 *     }
 * </pre>
 */
public class TerminalEngine implements AutoCloseable {

    public static final int TPS = 60;

    // old state
    private Attributes attr;

    // drawing
    protected final Terminal terminal;
    protected Display display;
    protected Surface surface;
    protected Graphics graphics;

    private Component rootComponent;

    // input
    private final Object LOCK = new Object();

    private BindingReader reader;
    private final ArrayDeque<Key> keyEvents = new ArrayDeque<>();
    protected final KeyMap<Key> keyMap = new KeyMap<>();
    protected final Map<Key, KeyInfo> keyInfos = new HashMap<>();

    private Future<?> readerFuture;


    // state
    protected boolean running;
    protected int tps;

    public TerminalEngine(Terminal terminal) {
        this.terminal = Objects.requireNonNull(terminal);
    }

    /**
     * The main loop.
     * It calls 60 times the update and render function.
     * It automatically, resize the surface and the display.
     * The terminal is also cleared.
     * After updating, key events are reset and then pooled from
     * the input thread.
     */
    public void show() {
        start();

        long lastTime = System.nanoTime();
        double ns = 1000000000.0 / TPS;
        double delta = 0.0;

        long timer = System.currentTimeMillis();
        int tps = 0;

        Size lastSize = null;

        readerFuture = Utils.SOKOSHELL_EXECUTOR.submit(this::read);
        while (running) {
            long now = System.nanoTime();
            delta += (double) (now - lastTime) / ns;

            for (lastTime = now; delta >= 1.0; delta--) {

                // update
                pollEvents();
                if (rootComponent != null) {
                    rootComponent.update();

                    if (!running) {
                        break;
                    }
                }


                // draw
                Size size = terminal.getSize();

                display.resize(size.getRows(), size.getColumns());
                surface.resize(size.getColumns(), size.getRows());
                surface.setTranslation(0, 0);
                surface.setClip(0, 0, size.getColumns(), size.getRows());

                if (lastSize != null && !lastSize.equals(size)) {
                    display.clear();
                }

                if (rootComponent != null) {
                    surface.clear();
                    rootComponent.setSize(size.getColumns(), size.getRows());

                    if (rootComponent.repaint) {
                        drawComponents();
                        surface.drawBuffer(display, 0);
                    }
                }

                lastSize = size;
                tps++;
            }

            if (System.currentTimeMillis() - timer > 1000) {
                timer += 1000;
                this.tps = tps;
                tps = 0;
            }
        }
    }

    protected void start() {
        if (running) {
            throw new IllegalStateException("Already running");
        }

        Objects.requireNonNull(terminal);
        running = true;
        tps = 0;

        display = new Display(terminal, true);
        display.setDelayLineWrap(false);

        reader = new BindingReader(terminal.reader());

        attr = terminal.enterRawMode();

        terminal.puts(InfoCmp.Capability.enter_ca_mode);
        terminal.puts(InfoCmp.Capability.keypad_xmit);
        terminal.writer().flush();

        display.clear();
        display.reset();

        surface = new Surface();
        surface.resize(terminal.getSize());
        graphics = new Graphics(surface);
    }


    // INPUT

    /**
     * Read bindings and add them to the keyEvents list.
     * Because {@link BindingReader#readBinding(KeyMap)} block
     * the thread, it is executed in another thread.
     */
    private void read() {
        try {
            while (running) {
                Key object = reader.readBinding(keyMap);

                synchronized (LOCK) {
                    keyEvents.add(object);
                }
            }
        } catch (IOError e) {
            if (e.getCause() instanceof InterruptedIOException && !running) {
                return;
            }

            throw e;
        }
    }

    /**
     * Count occurrences of all key events and clear all key events
     */
    private void pollEvents() {
        synchronized (LOCK) {

            for (KeyInfo i : keyInfos.values()) {
                i.pressed = false;
            }

            while (!keyEvents.isEmpty()) {
                Key k = keyEvents.poll();
                KeyInfo key = keyInfos.get(k);

                if (key == null) {
                    key = new KeyInfo(k);
                    keyInfos.put(k, key);
                }

                key.press();
            }

            for (KeyInfo i : keyInfos.values()) {
                if (!i.pressed && i.count > 0) {
                    i.released = true;
                    i.count = 0;
                } else if (i.released) {
                    i.released = false;
                }
            }
        }
    }


    protected void drawComponents() {
        rootComponent.draw(graphics);
    }

    /**
     * @param k the key
     * @return true if t is pressed
     */
    protected boolean keyPressed(Key k) {
        KeyInfo key = keyInfos.get(k);

        return key != null && key.isPressed();
    }

    /**
     * @param k the key
     * @return the number of time t was pressed
     * between now and the last time the update function
     * was called
     */
    protected int keyPressedCount(Key k) {
        KeyInfo key = keyInfos.get(k);

        return key == null ? 0 : key.pressedFor();
    }

    public boolean keyReleased(Key k) {
        KeyInfo key = keyInfos.get(k);

        return key != null && key.isReleased();
    }

    /**
     * @return tick per seconds. It should be around 60
     */
    public int getTPS() {
        return tps;
    }

    public Component getRootComponent() {
        return rootComponent;
    }

    public void setRootComponent(Component rootComponent) {
        if (this.rootComponent != rootComponent) {
            if (!rootComponent.isRoot()) {
                throw new IllegalArgumentException("Not root");
            }

            this.rootComponent = rootComponent;
            rootComponent.setTerminal(terminal, this);
        }
    }

    public boolean isRunning() {
        return running;
    }

    public Terminal getTerminal() {
        return terminal;
    }

    public KeyMap<Key> getKeyMap() {
        return keyMap;
    }

    public void stop() {
        running = false;
    }

    /**
     * Disable fullscreen mode and stop {@link #show()}
     */
    @Override
    public synchronized void close() {
        running = false;

        if (readerFuture != null) {
            readerFuture.cancel(true);
        }

        if (attr != null) {
            terminal.setAttributes(attr);
        }
        terminal.puts(InfoCmp.Capability.exit_ca_mode);
        terminal.puts(InfoCmp.Capability.keypad_local);
        terminal.writer().flush();

        readerFuture = null;
        reader = null;
        display = null;
        attr = null;
        surface = null;
        graphics = null;
    }

    private static class KeyInfo {

        /**
         * The key that represent this KeyInfo
         */
        private final Key key;

        /**
         * How many times this key was pressed
         */
        private int count;

        /**
         * If the key is pressed
         */
        private boolean pressed = false;

        private boolean released = false;

        public KeyInfo(Key key) {
            this.key = key;
        }

        public void press() {
            count++;
            pressed = true;
            released = false;
        }

        public boolean isPressed() {
            return pressed;
        }

        public boolean isReleased() {
            return released;
        }

        public boolean isPressed(int count) {
            return pressed && this.count > count;
        }

        private int pressedFor() {
            return count;
        }
    }
}
