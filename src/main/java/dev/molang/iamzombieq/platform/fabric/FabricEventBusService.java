package dev.molang.iamzombieq.platform.fabric;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import dev.molang.iamzombieq.api.event.Cancellable;
import dev.molang.iamzombieq.api.event.ZombieEvent;
import dev.molang.iamzombieq.platform.EventBusService;
import org.jetbrains.annotations.ApiStatus;

/**
 * Fabric implementation of {@link EventBusService}. Maintains a simple list of listeners and dispatches events
 * synchronously. This replaces NeoForge's event bus with a lightweight loader-agnostic dispatch.
 */
@ApiStatus.Internal
public final class FabricEventBusService implements EventBusService {

    private final CopyOnWriteArrayList<Listener> listeners = new CopyOnWriteArrayList<>();

    /**
     * Registers a listener for events of the given type.
     *
     * @param eventClass the event class to listen for
     * @param handler    the handler to invoke
     * @param <T>        the event type
     */
    public <T extends ZombieEvent> void register(Class<T> eventClass, Consumer<? super T> handler) {
        listeners.add(new Listener(eventClass, handler));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends ZombieEvent> T post(T event) {
        for (Listener listener : listeners) {
            if (listener.eventClass.isInstance(event)) {
                ((Consumer<? super T>) listener.handler).accept(event);
            }
        }
        return event;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends ZombieEvent & Cancellable> boolean postCancelable(T event) {
        for (Listener listener : listeners) {
            if (listener.eventClass.isInstance(event)) {
                ((Consumer<? super T>) listener.handler).accept(event);
                if (event.isCanceled()) {
                    return true;
                }
            }
        }
        return false;
    }

    private static final class Listener {
        final Class<?> eventClass;
        final Consumer<?> handler;

        Listener(Class<?> eventClass, Consumer<?> handler) {
            this.eventClass = eventClass;
            this.handler = handler;
        }
    }
}
