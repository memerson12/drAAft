package draaft.client.ws;

/**
 * Generic event listener contract.
 *
 * @param <E> event type
 */
public interface EventListener<E> {
    void onEvent(E event);
}

