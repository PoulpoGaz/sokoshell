package fr.valax.sokoshell.graphics;

import fr.valax.sokoshell.utils.Utils;
import org.jline.keymap.BindingReader;
import org.jline.keymap.KeyMap;
import org.jline.terminal.Attributes;
import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedString;
import org.jline.utils.Display;
import org.jline.utils.InfoCmp;

import java.io.IOError;
import java.io.InterruptedIOException;
import java.util.*;
import java.util.concurrent.Future;

public abstract class View<T> implements AutoCloseable {

    public static final int TPS = 60;

    // old state
    private final Attributes attr;

    // drawing
    protected final Terminal terminal;
    protected final Display display;
    protected final Surface surface;
    protected final Graphics graphics;

    // input
    private final Object LOCK = new Object();

    private final BindingReader reader;
    private final List<T> keyEvents;
    protected final KeyMap<T> keyMap;
    protected final Map<T, Integer> occurrences;

    private Future<?> readerFuture;


    // state
    protected boolean running;

    private int fps;
    private int tps;

    public View(Terminal terminal) {
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

    private void resetOccurrences() {
        occurrences.clear();
    }

    // LOOP

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
                update();
                resetOccurrences();
                pollEvents();

                tps++;
            }

            Size size = terminal.getSize();
            display.resize(size.getRows(), size.getColumns());
            surface.resize(size.getColumns(), size.getRows());

            if (lastSize != null && !lastSize.equals(size)) {
                display.clear();
            }

            render(size);
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

    protected abstract void init();

    protected abstract void render(Size size);

    protected abstract void update();

    protected boolean pressed(T t) {
        return pressedNTime(t) > 0;
    }

    protected int pressedNTime(T t) {
        Integer v = occurrences.get(t);

        return v == null ? 0 : v;
    }

    public int getFPS() {
        return fps;
    }

    public int getTPS() {
        return tps;
    }

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
