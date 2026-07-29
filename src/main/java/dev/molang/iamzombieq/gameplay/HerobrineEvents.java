package dev.molang.iamzombieq.gameplay;

import dev.molang.iamzombieq.util.ModIds;
import dev.molang.iamzombieq.IAmZombieConfig;
import dev.molang.iamzombieq.IAmZombieEntities;
import dev.molang.iamzombieq.entity.HerobrineEntity;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.HitResult;

/** Herobrine gameplay events. Stubbed for Fabric port. */
public final class HerobrineEvents {
    private static final double NEARBY_HEROBRINE_RANGE = 64.0;
    private static final ResourceKey<DamageType> HEROBRINE_DAMAGE = ResourceKey.create(
            Registries.DAMAGE_TYPE, ModIds.id("herobrine"));
    private static int liveHerobrineCount = 0;

    private HerobrineEvents() {}

    // Event stubs — logic will be re-registered via Fabric API
    public static void onPlayerTick(ServerPlayer player) { /* TODO */ }
    public static void onAttackEntity(ServerPlayer player, Entity target) { /* TODO */ }
    public static void onProjectileImpact(Projectile projectile, HitResult hitResult) { /* TODO */ }
    public static boolean onEntityInteract(ServerPlayer player, Entity target) { return target instanceof HerobrineEntity; }
    public static void onPlayerClone(ServerPlayer original, ServerPlayer newPlayer) { /* TODO */ }
    public static void onPlayerRespawn(ServerPlayer player) { /* TODO */ }
    public static void onEntityJoinLevel(Entity entity, ServerLevel level) { if (entity instanceof HerobrineEntity) liveHerobrineCount++; }
    public static void onEntityLeaveLevel(Entity entity, ServerLevel level) { if (entity instanceof HerobrineEntity) liveHerobrineCount = Math.max(0, liveHerobrineCount - 1); }
    public static void onServerStopped() { liveHerobrineCount = 0; }

    public static boolean isHerobrineNearby(ServerPlayer player) { return false; /* TODO */ }
    public static int getLiveHerobrineCount() { return liveHerobrineCount; }
}
