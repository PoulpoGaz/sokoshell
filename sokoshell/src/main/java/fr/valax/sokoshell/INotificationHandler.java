package fr.valax.sokoshell;

import org.jline.utils.AttributedString;

import java.util.Queue;

public interface INotificationHandler {

    static INotificationHandler newFalseNotificationHandler() {
        return new INotificationHandler() {
            @Override
            public void newNotification(AttributedString message) {

            }

            @Override
            public void updateStatus() {

            }

            @Override
            public void suspend() {

            }

            @Override
            public void restore() {

            }

            @Override
            public void clear() {

            }

            @Override
            public void shutdown() {

            }

            @Override
            public Queue<Notification> getNotifications() {
                return null;
            }
        };
    }

    default void newNotification(String message) {
        newNotification(new AttributedString(message));
    }

    void newNotification(AttributedString message);

    void updateStatus();

    void suspend();

    void restore();

    void clear();

    void shutdown();

    Queue<Notification> getNotifications();

    record Notification(AttributedString message, long time) {

    }
}
