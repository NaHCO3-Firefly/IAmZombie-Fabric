package dev.molang.iamzombieq;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
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

        // Register all content
        IAmZombieRegistries.register();

        // Server lifecycle
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            currentServer = server;
            dev.molang.iamzombieq.gameplay.DifficultyGuardEvents.onServerStarted(server);
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            dev.molang.iamzombieq.gameplay.HerobrineEvents.onServerStopped();
            dev.molang.iamzombieq.gameplay.ZombiePlayerEvents.onServerStopped();
            currentServer = null;
        });

        // Player login/logout
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            dev.molang.iamzombieq.gameplay.ZombiePlayerEvents.onPlayerLoggedIn(handler.getPlayer());
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            var player = handler.getPlayer();
            dev.molang.iamzombieq.gameplay.ZombiePlayerEvents.onPlayerLoggedOut(player);
            dev.molang.iamzombieq.gameplay.CoffinNapManager.onPlayerLoggedOut(player.getUUID());
        });

        // Entity tracking
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (entity instanceof net.minecraft.server.level.ServerPlayer player) {
                dev.molang.iamzombieq.gameplay.ZombiePlayerEvents.onPlayerClone(player);
            }
        });
        ServerEntityEvents.ENTITY_UNLOAD.register((entity, level) -> {
            // handled by DISCONNECT for players
        });

        // Tick - iterate all players
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (var player : server.getPlayerList().getPlayers()) {
                dev.molang.iamzombieq.gameplay.ZombiePlayerEvents.onPlayerTick(player);
                dev.molang.iamzombieq.gameplay.CoffinNapManager.tickPlayer(player);
            }
        });

        // Block interaction
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) ->
                dev.molang.iamzombieq.gameplay.ZombieSleepEvents.onRightClickBlock(
                        player, world, hand, hitResult.getBlockPos()));

        // Item use (eating) — zombie food rules
        UseItemCallback.EVENT.register((player, world, hand) -> {
            dev.molang.iamzombieq.gameplay.ZombieFoodEvents.onRightClickItem();
            return net.minecraft.world.InteractionResult.PASS;
        });

        LOGGER.info("{} initialized", ENGLISH_NAME);
    }

    public static MinecraftServer getCurrentServer() { return currentServer; }
}
