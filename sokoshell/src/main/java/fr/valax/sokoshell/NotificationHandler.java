package fr.valax.sokoshell;

import fr.valax.sokoshell.utils.Utils;
import org.jline.reader.impl.LineReaderImpl;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.Status;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

public class NotificationHandler implements INotificationHandler {

    private static final int MAX_NOTIFICATION = 100;

    private static final SokoShell SOKOSHELL = SokoShell.INSTANCE;

    private final LineReaderImpl lineReader;
    private final Status status;
    private final ArrayDeque<Notification> notifications;
    private boolean suspend = false;


    public NotificationHandler(LineReaderImpl reader) {
        this.lineReader = Objects.requireNonNull(reader);
        this.status = Status.getStatus(reader.getTerminal());

        notifications = new ArrayDeque<>();

        if (status != null && !SOKOSHELL.isShutdown()) {
            SOKOSHELL.getScheduledExecutor()
                    .scheduleWithFixedDelay(this::updateStatus, 0, 1, TimeUnit.MINUTES);
        }
    }

    @Override
    public void newNotification(AttributedString message) {
        notifications.offer(new Notification(message, System.currentTimeMillis()));

        while (notifications.size() > MAX_NOTIFICATION) {
            notifications.poll();
        }

        updateStatus();
    }

    @Override
    public void updateStatus() {
        if (status == null || suspend || SOKOSHELL.isShutdown()) {
            return;
        }

        if (notifications.isEmpty()) {
            if (status.size() == 0) {
                return;
            }

            status.clear();
            status.reset();
        } else {
            Notification notification = notifications.peekLast();

            AttributedStringBuilder asb = new AttributedStringBuilder();
            asb.append(notification.message());

            long time = System.currentTimeMillis();
            long diff = time - notification.time();
            if (diff < 60_000) {
                asb.append(" (moments ago)");
            } else {
                asb.append(" (").append(Utils.prettyDate(diff, true)).append(" ago)");
            }

            status.update(List.of(asb.toAttributedString()));
        }

        lineReader.redisplay();
    }

    @Override
    public void suspend() {
        if (status != null) {
            status.suspend();
            suspend = true;
        }
    }

    @Override
    public void restore() {
        if (status != null) {
            status.restore();
            updateStatus();
            suspend = false;
        }
    }

    @Override
    public void clear() {
        if (status != null) {
            status.reset();
        }
    }

    @Override
    public void shutdown() {
        if (status != null) {
            status.reset();
        }
    }

    @Override
    public Queue<Notification> getNotifications() {
        return notifications;
    }
}
