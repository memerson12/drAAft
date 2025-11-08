package draaft.client.ws;

import draaft.draaft;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Simple typed EventBus. Register listeners by event class and post events by instance.
 * Listeners registered for a supertype will receive subtype events (uses Class.isAssignableFrom).
 */
public class EventBus {
    private final Map<Class<?>, CopyOnWriteArrayList<EventListener<?>>> listeners = new ConcurrentHashMap<>();

    public <E> void register(Class<E> eventClass, EventListener<? super E> listener) {
        if (eventClass == null || listener == null) return;
        listeners.computeIfAbsent(eventClass, k -> new CopyOnWriteArrayList<>()).add(listener);
    }

    public <E> void unregister(Class<E> eventClass, EventListener<? super E> listener) {
        if (eventClass == null || listener == null) return;
        List<EventListener<?>> list = listeners.get(eventClass);
        if (list != null) list.remove(listener);
    }

    public void post(Object event) {
        if (event == null) return;
        Class<?> eventClass = event.getClass();
        // dispatch to listeners whose registered class is assignable from the event's class
        for (Map.Entry<Class<?>, CopyOnWriteArrayList<EventListener<?>>> entry : listeners.entrySet()) {
            Class<?> key = entry.getKey();
            if (key.isAssignableFrom(eventClass)) {
                for (EventListener<?> rawListener : entry.getValue()) {
                    try {
                        @SuppressWarnings("unchecked")
                        EventListener<Object> l = (EventListener<Object>) rawListener;
                        l.onEvent(event);
                    } catch (Throwable t) {
                        draaft.LOGGER.warn("Exception in EventBus listener: ", t);
                    }
                }
            }
        }
    }
}

