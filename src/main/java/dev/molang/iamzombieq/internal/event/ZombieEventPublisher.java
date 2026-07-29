package dev.molang.iamzombieq.internal.event;

import dev.molang.iamzombieq.IAmZombieMod;
import dev.molang.iamzombieq.api.event.Cancellable;
import dev.molang.iamzombieq.api.event.ZombieEvent;
import dev.molang.iamzombieq.platform.Services;
import org.jetbrains.annotations.ApiStatus;

/**
 * Central, isolation-wrapped publisher for the mod's lifecycle events. EVERY event post goes through here so a
 * misbehaving (future) addon listener cannot crash a gameplay handler or interrupt a player's evolution.
 *
 * <p>Both methods wrap the bus post in {@code try/catch(Exception)}, log via {@link IAmZombieMod#LOGGER}, and
 * never rethrow a listener {@link Exception}. JVM {@link Error}s are NOT caught — they propagate so a genuine VM
 * failure (e.g. {@code OutOfMemoryError}) is never silently swallowed.
 */
@ApiStatus.Internal
public final class ZombieEventPublisher {

    private ZombieEventPublisher() {
    }

    /** Posts an observer event, isolating any listener {@link Exception} (Errors propagate); never rethrows. */
    public static void post(ZombieEvent event) {
        try {
            Services.EVENTS.post(event);
        } catch (Exception e) {
            IAmZombieMod.LOGGER.error("A zombie event listener threw while handling {}", event.getClass().getName(), e);
        }
    }

    /**
     * Posts a cancellable event, isolating any listener {@link Exception} (Errors propagate).
     *
     * @return {@code true} if a listener canceled the event; {@code false} otherwise. If a listener throws, the
     *         event's current canceled state is returned.
     */
    public static <T extends ZombieEvent & Cancellable> boolean postCancelable(T event) {
        try {
            return Services.EVENTS.postCancelable(event);
        } catch (Exception e) {
            IAmZombieMod.LOGGER.error("A zombie event listener threw while handling {}", event.getClass().getName(), e);
            return event.isCanceled();
        }
    }
}
