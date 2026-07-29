package dev.molang.iamzombieq;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
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

        // Register all content (blocks, items, entities, attachments)
        IAmZombieRegistries.register();

        // Register server lifecycle handlers
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            currentServer = server;
            // Initialize shared state managers
            dev.molang.iamzombieq.gameplay.HerobrineEvents.onServerStarted();
            dev.molang.iamzombieq.gameplay.DifficultyGuardEvents.onServerStarted();
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            dev.molang.iamzombieq.gameplay.HerobrineEvents.onServerStopped();
            dev.molang.iamzombieq.gameplay.OmenLightsSavedData.onServerStopped();
            currentServer = null;
        });

        ServerLifecycleEvents.BEFORE_SAVE.register((server, flush, force) -> {
            dev.molang.iamzombieq.gameplay.OmenLightsSavedData.beforeSave();
        });

        // Register tick-based event handlers
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            dev.molang.iamzombieq.gameplay.CoffinNapManager.onServerTick();
        });

        LOGGER.info("{} initialized", ENGLISH_NAME);
    }

    public static MinecraftServer getCurrentServer() {
        return currentServer;
    }
}
