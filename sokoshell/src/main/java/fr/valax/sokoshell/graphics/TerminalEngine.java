package fr.valax.sokoshell.graphics;

import fr.valax.sokoshell.SokoShell;
import org.jline.builtins.Nano;
import org.jline.keymap.BindingReader;
import org.jline.keymap.KeyMap;
import org.jline.terminal.Attributes;
import org.jline.terminal.MouseEvent;
import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.utils.Display;
import org.jline.utils.InfoCmp;

import java.io.IOError;
import java.io.InterruptedIOException;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Future;

public class TerminalEngine implements AutoCloseable {

    // the terminal
    protected final Terminal terminal;
    protected final int TPS;

    // old state
    private Attributes attr;

    // drawing
    protected Display display;
    protected Surface surface;
    protected Graphics graphics;

    private Component rootComponent;

    // input
    private final Object LOCK = new Object();

    private BindingReader reader;

    // can be Key or MouseEvent
    private final ArrayDeque<Object> keyEvents = new ArrayDeque<>();
    private final KeyMap<Key> keyMap = new KeyMap<>();
    private final Map<Key, KeyInfo> keyInfos = new HashMap<>();

    private final boolean hasMouseSupport;
    private MouseEvent lastMouseEvent;
    private boolean isMouseActivated = false;

    private Future<?> readerFuture;


    // state
    protected boolean running;
    protected int tps;

    public TerminalEngine(Terminal terminal) {
        this(terminal, 30);
    }

    public TerminalEngine(Terminal terminal, int TPS) {
        this.terminal = Objects.requireNonNull(terminal);
        this.TPS = TPS;
        this.hasMouseSupport = terminal.hasMouseSupport();

        keyMap.setAmbiguousTimeout(100L);

        // Must be here, see https://github.com/jline/jline3/issues/306
        // Otherwise, key like enter, up, etc. won't work properly
        attr = terminal.enterRawMode();

        terminal.puts(InfoCmp.Capability.enter_ca_mode);
        terminal.puts(InfoCmp.Capability.keypad_xmit);
        terminal.writer().flush();
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

        readerFuture = SokoShell.INSTANCE.getExecutor().submit(this::read);
        while (running) {
            long now = System.nanoTime();
            delta += (double) (now - lastTime) / ns;

            for (lastTime = now; delta >= 1.0; delta--) {

                // update
                pollEvents();
                if (rootComponent != null) {
                    rootComponent.layoutIfNeeded();
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
                        rootComponent.draw(graphics);
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

        running = true;
        tps = 0;

        SokoShell.INSTANCE.getNotificationHandler().suspend();

        display = new Display(terminal, true);
        display.setDelayLineWrap(false);

        reader = new BindingReader(terminal.reader());

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
                    if (object == Key.MOUSE) {
                        keyEvents.add(terminal.readMouseEvent());
                    } else {
                        keyEvents.add(object);
                    }
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

            lastMouseEvent = null;
            for (KeyInfo i : keyInfos.values()) {
                i.setPressed(false);
            }

            while (!keyEvents.isEmpty()) {
                Object o = keyEvents.poll();

                if (o instanceof MouseEvent event) {
                    lastMouseEvent = event;
                } else if (o instanceof Key k) {
                    KeyInfo key = keyInfos.get(k);

                    if (key == null) {
                        key = new KeyInfo(k);
                        keyInfos.put(k, key);
                    }

                    key.setPressed(true);
                } else {
                    throw new IllegalStateException("Unknown event: " + o);
                }
            }
        }
    }


    public KeyMap<Key> getKeyMap() {
        return keyMap;
    }

    /**
     * @param k the key
     * @return true if t is pressed
     */
    public boolean keyPressed(Key k) {
        KeyInfo key = keyInfos.get(k);

        return key != null && key.isPressed();
    }

    public boolean hasMouseSupport() {
        return hasMouseSupport;
    }

    public boolean trackMouse(Terminal.MouseTracking tracking) {
        if (hasMouseSupport && terminal.trackMouse(tracking)) {

            if (tracking != Terminal.MouseTracking.Off) {
                Key.MOUSE.bind(this);
                isMouseActivated = true;
            } else {
                Key.MOUSE.unbind(this);
                isMouseActivated = false;
            }

            return true;
        } else {
            return false;
        }
    }

    // TODO: maybe give a queue of all events that occurs between two update
    public MouseEvent getLastMouseEvent() {
        return lastMouseEvent;
    }

    public boolean hasMouseEvent() {
        return lastMouseEvent != null;
    }



    public Component getRootComponent() {
        return rootComponent;
    }

    public void setRootComponent(Component rootComponent) {
        if (this.rootComponent != rootComponent) {
            if (!rootComponent.isRoot()) {
                throw new IllegalArgumentException("Not root");
            }

            if (this.rootComponent != null) {
                this.rootComponent.setTerminal(null, null);
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
        if (isMouseActivated) {
            terminal.trackMouse(Terminal.MouseTracking.Off);
        }
        terminal.puts(InfoCmp.Capability.exit_ca_mode);
        terminal.puts(InfoCmp.Capability.keypad_local);
        terminal.writer().flush();

        SokoShell.INSTANCE.getNotificationHandler().restore();

        readerFuture = null;
        reader = null;
        display = null;
        attr = null;
        surface = null;
        graphics = null;
    }

    private static class KeyInfo {

        private final Key key;
        private boolean pressed = false;

        public KeyInfo(Key key) {
            this.key = key;
        }

        public void setPressed(boolean pressed) {
            this.pressed = pressed;
        }

        public boolean isPressed() {
            return pressed;
        }
    }
}
