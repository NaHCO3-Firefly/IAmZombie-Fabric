package dev.molang.iamzombieq.gameplay;

import net.minecraft.server.MinecraftServer;

/**
 * Applies the startup half of the Peaceful guard: when the server has started, correct a world that was saved on
 * Peaceful to the playable fallback. The runtime half — coercing every later difficulty change — lives in
 * {@code MinecraftServerMixin}. See {@link PeacefulGuard}.
 */
public final class DifficultyGuardEvents {
    private DifficultyGuardEvents() {
    }

    public static void onServerStarted(MinecraftServer server) {
        PeacefulGuard.enforce(server);
    }
}
