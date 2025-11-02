package draaft.client.ws;

import draaft.draaft;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class RoomEventDispatcher {
    private final List<RoomEventListener> listeners = new CopyOnWriteArrayList<>();

    public void addListener(RoomEventListener listener) {
        if (listener != null)
            listeners.add(listener);
    }

    public void removeListener(RoomEventListener listener) {
        if (listener != null)
            listeners.remove(listener);
    }

    public void emit(RoomEvent event) {
        for (RoomEventListener listener : listeners) {
            try {
                listener.onEvent(event);
            } catch (Throwable error) {
                draaft.LOGGER.warn("Exception in RoomEventListener: ", error);
            }
        }
    }
}

