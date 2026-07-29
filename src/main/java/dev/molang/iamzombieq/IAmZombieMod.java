package dev.molang.iamzombieq;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;

public final class IAmZombieMod implements ModInitializer {
    public static final String MOD_ID = "iamzombieq";
    public static final String ENGLISH_NAME = "I Am Zombie?";
    public static final String CHINESE_NAME = "我是僵尸？";
    public static final Logger LOGGER = LogUtils.getLogger();
    private static MinecraftServer currentServer;

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing {}", ENGLISH_NAME);

        // Register all content (blocks, items, entities)
        IAmZombieRegistries.register();

        // Register server lifecycle handlers
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            currentServer = server;
            dev.molang.iamzombieq.gameplay.DifficultyGuardEvents.onServerStarted(server);
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            dev.molang.iamzombieq.gameplay.CoffinNapManager.onServerStopped();
            currentServer = null;
        });

        // Register gameplay callbacks
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) ->
                dev.molang.iamzombieq.gameplay.ZombieSleepEvents.onRightClickBlock(
                        player, world, hand, hitResult.getBlockPos()));

        // Player disconnect → clean up per-player state
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            dev.molang.iamzombieq.gameplay.CoffinNapManager.onPlayerLoggedOut(handler.getPlayer().getUUID());
        });

        // Tick-based handlers — iterate all online players for coffin sleep
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (net.minecraft.server.level.ServerPlayer player : server.getPlayerList().getPlayers()) {
                dev.molang.iamzombieq.gameplay.CoffinNapManager.tickPlayer(player);
            }
        });

        LOGGER.info("{} initialized", ENGLISH_NAME);
    }

    public static MinecraftServer getCurrentServer() {
        return currentServer;
    }
}
