package dev.molang.iamzombieq.client;

import dev.molang.iamzombieq.IAmZombieEntities;
import dev.molang.iamzombieq.entity.HerobrineEntity;
import dev.molang.iamzombieq.util.ModIds;
import dev.molang.iamzombieq.client.HerobrineRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.Identifier;

public final class IAmZombieClient implements ClientModInitializer {
    private static final ZombiePlayerShapeEntities ZOMBIE_PLAYER_SHAPES = new ZombiePlayerShapeEntities();
    private static final Identifier HEROBRINE_HEAD_TEXTURE = ModIds.id("textures/entity/herobrine_head.png");
    private static boolean mutedByHerobrine;
    private static int herobrinePresenceCount;
    private static int heartbeatCooldown;
    private static int joltVignetteTicks;
    private static final Identifier HEARTBEAT_ID = Identifier.fromNamespaceAndPath("minecraft", "warden_heartbeat");
    private static final Identifier JOLT_STINGER_ID = Identifier.fromNamespaceAndPath("minecraft", "warden_roar");

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(IAmZombieEntities.HEROBRINE, HerobrineRenderer::new);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // TODO: Herobrine audio/vignette effects
        });

        ClientEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity instanceof HerobrineEntity) herobrinePresenceCount++;
        });

        ClientEntityEvents.ENTITY_UNLOAD.register((entity, world) -> {
            if (entity instanceof net.minecraft.client.player.AbstractClientPlayer p) {
                ZOMBIE_PLAYER_SHAPES.remove(p);
            } else if (entity instanceof HerobrineEntity) {
                herobrinePresenceCount = Math.max(0, herobrinePresenceCount - 1);
            }
        });

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            ZOMBIE_PLAYER_SHAPES.clear();
            herobrinePresenceCount = 0;
            heartbeatCooldown = 0;
            joltVignetteTicks = 0;
        });
    }

    public static ZombiePlayerShapeEntities getShapeEntities() { return ZOMBIE_PLAYER_SHAPES; }
    public static int getJoltVignetteTicks() { return joltVignetteTicks; }
    public static void setJoltVignetteTicks(int ticks) { joltVignetteTicks = ticks; }
    public static int getHerobrinePresenceCount() { return herobrinePresenceCount; }
}
