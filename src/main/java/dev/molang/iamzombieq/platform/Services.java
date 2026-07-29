package dev.molang.iamzombieq.platform;

import dev.molang.iamzombieq.platform.fabric.FabricEventBusService;
import org.jetbrains.annotations.ApiStatus;

/**
 * Static holder for the platform services used by internal code. Implementations are the Fabric ones,
 * constructed with plain {@code new}.
 */
@ApiStatus.Internal
public final class Services {

    public static final EventBusService EVENTS = new FabricEventBusService();

    private Services() {
    }
}
