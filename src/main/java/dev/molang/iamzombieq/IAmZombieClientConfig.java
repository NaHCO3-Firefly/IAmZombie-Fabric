package dev.molang.iamzombieq;

import java.util.function.Supplier;

/**
 * Client-side configuration using Cloth Config API.
 * Replaces NeoForge's ModConfigSpec with simple Supplier-backed values.
 */
public final class IAmZombieClientConfig {

    /** Player skin mode: 0 = zombie, 1 = human, 2 = auto-detect. */
    public static final Supplier<Integer> PLAYER_SKIN_MODE = () -> 0;

    private IAmZombieClientConfig() {
    }
}
