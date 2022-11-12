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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * TerminalEngine is an object used to facilitate drawing
 * and input reading in fullscreen mode.
 *
 * It's divided in three methods: {@link TerminalEngine#init()},
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
 *
 * @param <T> type of binding
 */
public abstract class TerminalEngine<T> implements AutoCloseable {

    public static final int TPS = 60;

    // old state
    private final Attributes attr;

    // drawing
    protected final Terminal terminal;
    protected final Display display;
    protected final Surface surface;
    protected final Graphics graphics;

    private Component rootComponent;

    // input
    private final Object LOCK = new Object();

    private final BindingReader reader;
    private final List<T> keyEvents;
    protected final KeyMap<T> keyMap;
    protected final Map<T, Integer> occurrences;

    private Future<?> readerFuture;


    // state
    protected boolean running;

    protected int fps;
    protected int tps;

    public TerminalEngine(Terminal terminal) {
        this.terminal = terminal;
        this.display = new Display(terminal, true);
        display.setDelayLineWrap(false);

        keyMap = new KeyMap<>();
        reader = new BindingReader(terminal.reader());
        keyEvents = new ArrayList<>();
        occurrences = new HashMap<>();

        init();

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
                T object = reader.readBinding(keyMap);

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

            for (T t : keyEvents) {
                Integer v = occurrences.get(t);

                if (v == null) {
                    v = 0;
                }

                occurrences.put(t, v + 1);
            }

            keyEvents.clear();
        }
    }

    /**
     * Clear occurrences
     */
    private void resetOccurrences() {
        occurrences.clear();
    }

    /**
     * The main loop.
     * It calls 60 times the update function.
     * The render function is not regulated.
     * It automatically, resize the surface and the display.
     * The terminal is also cleared.
     * After updating, key events are reset and then pooled from
     * the input thread.
     */
    public void loop() {
        long lastTime = System.nanoTime();
        double ns = 1000000000.0 / TPS;
        double delta = 0.0;

        long timer = System.currentTimeMillis();
        int fps = 0;
        int tps = 0;

        running = true;

        Size lastSize = null;

        readerFuture = Utils.SOKOSHELL_EXECUTOR.submit(this::read);
        while (running) {
            long now = System.nanoTime();
            delta += (double) (now - lastTime) / ns;

            for (lastTime = now; delta >= 1.0; delta--) {
                if (rootComponent != null) {
                    rootComponent.update();
                }
                update();
                resetOccurrences();
                pollEvents();

                tps++;
            }

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
                rootComponent.draw(graphics);
            }

            int cursor = render(size);

            if (cursor >= 0) {
                surface.drawBuffer(display, cursor);
            }

            fps++;
            if (System.currentTimeMillis() - timer > 1000) {
                timer += 1000;

                this.fps = fps;
                this.tps = tps;

                fps = 0;
                tps = 0;
            }

            lastSize = size;
        }
    }

    /**
     * The init method is called once by the constructor.
     * It is used to bind keys
     */
    protected abstract void init();

    /**
     * Called as often as possible by {@link #loop()}.
     * It is used to draw on the screen. Implementations can use
     * the {@link Surface} and {@link Graphics} object
     *
     * @param size terminal size
     * @return an integer indicating the position of cursor or negative to prevent drawing
     */
    protected abstract int render(Size size);

    /**
     * Called 60 times per seconds by {@link #loop()}.
     * It is used to read binding and update objets
     */
    protected abstract void update();

    /**
     * @param t the key
     * @return true if t is pressed
     */
    protected boolean pressed(T t) {
        return pressedNTime(t) > 0;
    }

    /**
     * @param t the key
     * @return the number of time t was pressed
     * between now and the last time the update function
     * was called
     */
    protected int pressedNTime(T t) {
        Integer v = occurrences.get(t);

        return v == null ? 0 : v;
    }

    /**
     * @param t the key
     * @return {@code true} if the key was just pressed
     */
    protected boolean justPressed(T t) {
        Integer v = occurrences.get(t);

        return v != null && v == 1;
    }

    /**
     * @return frame per second
     */
    public int getFPS() {
        return fps;
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
            setTerminal(rootComponent);
        }
    }

    private void setTerminal(Component component) {
        component.terminal = terminal;
        component.engine = this;

        List<Component> comps = component.components;
        for (int i = 0; i < comps.size(); i++) {
            setTerminal(comps.get(i));
        }
    }

    /**
     * Disable fullscreen mode and stop {@link #loop()}
     */
    @Override
    public void close() {
        running = false;
        readerFuture.cancel(true);
        terminal.setAttributes(attr);
        terminal.puts(InfoCmp.Capability.exit_ca_mode);
        terminal.puts(InfoCmp.Capability.keypad_local);
        terminal.writer().flush();
    }
}
