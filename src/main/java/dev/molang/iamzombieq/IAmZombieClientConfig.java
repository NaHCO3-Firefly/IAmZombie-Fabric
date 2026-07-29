package dev.molang.iamzombieq;

import java.util.function.Supplier;
import dev.molang.iamzombieq.rules.ZombiePlayerSkinMode;

/**
 * Client-side configuration using Cloth Config API.
 * Replaces NeoForge's ModConfigSpec with simple Supplier-backed values.
 */
public final class IAmZombieClientConfig {

    /** Player skin mode. */
    public static final Supplier<ZombiePlayerSkinMode> PLAYER_SKIN_MODE = () -> ZombiePlayerSkinMode.MONSTER_TEXTURE;

    private IAmZombieClientConfig() {
    }
}
