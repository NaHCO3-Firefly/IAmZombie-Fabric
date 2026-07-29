package dev.molang.iamzombieq.api.event;

/**
 * Mixin interface for events that can be canceled by a listener. Replaces NeoForge's
 * {@code net.neoforged.bus.api.ICancellableEvent} so cancellable events are loader-agnostic.
 *
 * <p>Part of the STABLE public API surface (semver 1.x).
 */
public interface Cancellable {

    /**
     * @return {@code true} if a listener has canceled this event; {@code false} otherwise.
     */
    boolean isCanceled();

    /**
     * Cancels this event. Once canceled, later listeners see {@link #isCanceled()} == {@code true} and can
     * choose to bail out.
     */
    void setCanceled(boolean canceled);
}
