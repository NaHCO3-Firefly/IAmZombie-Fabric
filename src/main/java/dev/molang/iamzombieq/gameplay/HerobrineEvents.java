package dev.molang.iamzombieq.gameplay;

import dev.molang.iamzombieq.util.ModIds;
import dev.molang.iamzombieq.IAmZombieConfig;
import dev.molang.iamzombieq.IAmZombieEntities;
import dev.molang.iamzombieq.entity.HerobrineEntity;
import dev.molang.iamzombieq.rules.herobrine.HerobrineEncounter;
import dev.molang.iamzombieq.rules.herobrine.HerobrineRules;
import dev.molang.iamzombieq.state.HerobrineEncounterState;
import dev.molang.iamzombieq.state.HerobrineRespawnSnapshot;
import dev.molang.iamzombieq.state.IAmZombieAttachments;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Herobrine gameplay events — Fabric port. */
public final class HerobrineEvents {
    private static final double NEARBY_HEROBRINE_RANGE = 64.0;
    private static final ResourceKey<DamageType> HEROBRINE_DAMAGE = ResourceKey.create(
            Registries.DAMAGE_TYPE, ModIds.id("herobrine"));
    private static int liveHerobrineCount = 0;

    private HerobrineEvents() {}

    /**
     * Called each player tick. Attempts to spawn Herobrine if the player is in a cave
     * and no Herobrine is nearby.
     */
    public static void onPlayerTick(ServerPlayer player) {
        if (player.level().isClientSide()) return;
        if (player.isSpectator()) return;
        if (player.tickCount % 100 != 0) return; // Check every 5 seconds

        ServerLevel level = (ServerLevel) player.level();
        if (level.getBrightness(net.minecraft.world.level.LightLayer.BLOCK, player.blockPosition()) <= 4 && !isHerobrineNearby(player)) {
            double roll = level.getRandom().nextDouble();
            if (HerobrineRules.shouldAttemptCaveSpawn(roll, true, true)) {
                spawnHerobrine(player);
            }
        }
    }

    /**
     * Called when a player attacks an entity. If the target is a Herobrine,
     * handle the attack based on encounter phase (vanish or kill).
     */
    public static void onAttackEntity(ServerPlayer player, Entity target) {
        if (!(target instanceof HerobrineEntity herobrine)) return;
        if (player.level().isClientSide()) return;

        HerobrineEncounterState state = IAmZombieAttachments.getHerobrineEncounter(player);
        HerobrineEncounter.Phase phase = HerobrineEncounter.phaseFor(state.sightings, state.escalatedBefore);

        if (HerobrineEncounter.isLethal(phase)) {
            // Lethal phase: player takes damage
            ResourceKey<DamageType> damageType = HEROBRINE_DAMAGE;
            DamageSource source = new DamageSource(
                    player.level().registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(damageType));
            player.hurtServer((ServerLevel) player.level(), source, 6.0F);
            state.lastLethalTick = player.level().getGameTime();
            state.escalatedBefore = true;
            IAmZombieAttachments.setHerobrineEncounter(player, state);
        } else {
            // Non-lethal: vanish and record sighting
            state.sightings++;
            state.lastSightingTick = player.level().getGameTime();
            IAmZombieAttachments.setHerobrineEncounter(player, state);
            herobrine.discard();
        }
    }

    /**
     * Called when a projectile impacts. If it hits a Herobrine, handle like an attack.
     */
    public static void onProjectileImpact(Projectile projectile, HitResult hitResult) {
        if (projectile.getOwner() instanceof ServerPlayer player
                && hitResult.getType() == HitResult.Type.ENTITY) {
            Entity target = ((net.minecraft.world.phys.EntityHitResult) hitResult).getEntity();
            if (target instanceof HerobrineEntity) {
                onAttackEntity(player, target);
            }
        }
    }

    /**
     * Called when a player interacts with an entity. Returns true if the target is a Herobrine.
     */
    public static boolean onEntityInteract(ServerPlayer player, Entity target) {
        return target instanceof HerobrineEntity;
    }

    /** No-op: HerobrineEncounterState is stored in a durable attachment that persists across death/respawn. */

    /**
     * Called when a player respawns. Restores pending respawn snapshot if present.
     */
    public static void onPlayerRespawn(ServerPlayer player) {
        HerobrineRespawnSnapshot snapshot = IAmZombieAttachments.getHerobrineRespawn(player);
        if (snapshot.isPresent()) {
            player.snapTo(snapshot.x(), snapshot.y(), snapshot.z(), snapshot.yRot(), snapshot.xRot());
            // Experience restore omitted - ServerPlayer fields are not directly writable in MC 26.2
            // Clear the snapshot after restore
            IAmZombieAttachments.setHerobrineRespawn(player, HerobrineRespawnSnapshot.EMPTY);
        }
    }

    public static void onEntityJoinLevel(Entity entity, ServerLevel level) {
        if (entity instanceof HerobrineEntity) liveHerobrineCount++;
    }

    public static void onEntityLeaveLevel(Entity entity, ServerLevel level) {
        if (entity instanceof HerobrineEntity) liveHerobrineCount = Math.max(0, liveHerobrineCount - 1);
    }

    public static void onServerStopped() {
        liveHerobrineCount = 0;
    }

    /** Checks if any Herobrine entity is within range of the player. */
    public static boolean isHerobrineNearby(ServerPlayer player) {
        if (player.level() instanceof ServerLevel level) {
            AABB area = player.getBoundingBox().inflate(NEARBY_HEROBRINE_RANGE);
            return !level.getEntitiesOfClass(HerobrineEntity.class, area).isEmpty();
        }
        return false;
    }

    public static int getLiveHerobrineCount() {
        return liveHerobrineCount;
    }

    /**
     * Spawns a Herobrine entity near the player.
     */
    private static void spawnHerobrine(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) return;
        HerobrineEntity herobrine = IAmZombieEntities.HEROBRINE.create(level, net.minecraft.world.entity.EntitySpawnReason.EVENT);
        if (herobrine == null) return;

        // Spawn at a random offset within view distance
        double offsetX = (level.getRandom().nextDouble() - 0.5) * 20;
        double offsetZ = (level.getRandom().nextDouble() - 0.5) * 20;
        Vec3 pos = player.position().add(offsetX, 0, offsetZ);
        herobrine.snapTo(pos.x, player.getY(), pos.z, 0, 0);
        level.addFreshEntity(herobrine);
    }
}
