package dev.molang.iamzombieq.platform.neoforge;

import dev.molang.iamzombieq.api.event.Cancellable;
import dev.molang.iamzombieq.api.event.ZombieEvent;
import dev.molang.iamzombieq.platform.EventBusService;
import org.jetbrains.annotations.ApiStatus;

/**
 * Placeholder — NeoForge is no longer supported. This file is retained only to satisfy references
 * during the Fabric port transition.
 */
@ApiStatus.Internal
@Deprecated
public final class NeoForgeEventBusService implements EventBusService {

    @Override
    public <T extends ZombieEvent> T post(T event) {
        return event;
    }

    @Override
    public <T extends ZombieEvent & Cancellable> boolean postCancelable(T event) {
        return event.isCanceled();
    }
}
