package dev.molang.iamzombieq.platform;

import dev.molang.iamzombieq.api.event.Cancellable;
import dev.molang.iamzombieq.api.event.ZombieEvent;
import org.jetbrains.annotations.ApiStatus;

/**
 * Portability seam for posting the mod's lifecycle events. The Fabric implementation dispatches to registered
 * listeners; abstracting it lets internal code post without naming the concrete event bus.
 *
 * <p>Consumed only by internal code (via {@code internal.event.ZombieEventPublisher}).
 */
@ApiStatus.Internal
public interface EventBusService {

    /** Posts an observer event. Returns the same event instance. */
    <T extends ZombieEvent> T post(T event);

    /**
     * Posts a cancellable event and reports whether a listener canceled it.
     *
     * @return {@code true} if a listener canceled the event; {@code false} otherwise.
     */
    <T extends ZombieEvent & Cancellable> boolean postCancelable(T event);
}
