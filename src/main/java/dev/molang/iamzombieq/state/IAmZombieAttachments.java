package dev.molang.iamzombieq.state;

import java.util.function.Supplier;

import dev.molang.iamzombieq.IAmZombieMod;
import dev.molang.iamzombieq.rules.core.ZombieForm;
import dev.molang.iamzombieq.rules.core.ZombieSize;
import dev.molang.iamzombieq.rules.core.ZombieState;
import net.minecraft.resources.Identifier;

/**
 * Attachment/component keys for the mod's per-player and per-entity data.
 * In Fabric, these use Cardinal Components API via component keys.
 */
public final class IAmZombieAttachments {

    // Component key identifiers (string constants for use with AttachmentService)
    public static final String PLAYER_ZOMBIE_KEY = "player_zombie";
    public static final String SPIDER_MOUNT_KEY = "spider_mount";
    public static final String HEROBRINE_PENDING_RESPAWN_KEY = "herobrine_pending_respawn";
    public static final String HEROBRINE_ENCOUNTER_KEY = "herobrine_encounter";

    /** Identifier for the player_zombie component. */
    public static final Identifier PLAYER_ZOMBIE_ID = Identifier.fromNamespaceAndPath(IAmZombieMod.MOD_ID, PLAYER_ZOMBIE_KEY);
    /** Identifier for the spider_mount component. */
    public static final Identifier SPIDER_MOUNT_ID = Identifier.fromNamespaceAndPath(IAmZombieMod.MOD_ID, SPIDER_MOUNT_KEY);
    /** Identifier for the herobrine_pending_respawn component. */
    public static final Identifier HEROBRINE_PENDING_RESPAWN_ID = Identifier.fromNamespaceAndPath(IAmZombieMod.MOD_ID, HEROBRINE_PENDING_RESPAWN_KEY);
    /** Identifier for the herobrine_encounter component. */
    public static final Identifier HEROBRINE_ENCOUNTER_ID = Identifier.fromNamespaceAndPath(IAmZombieMod.MOD_ID, HEROBRINE_ENCOUNTER_KEY);

    private IAmZombieAttachments() {
    }

    /**
     * Called by {@code IAmZombieRegistries.register()} during mod initialization.
     * No-op in the in-memory attachment regime; a real CCA-based implementation would
     * register component keys here.
     */
    public static void register() {
        // TODO: register CCA ComponentKey instances here when migrating to Cardinal Components
    }

    /**
     * Initialize attachment/component registration.
     * Called during mod initialization to register all components with the backing store.
     */
    public static void initialize() {
        // Components are registered via the platform AttachmentService or Cardinal Components.
        // The component types are:
        //   PLAYER_ZOMBIE_ID  -> PlayerZombieData (on Player entities)
        //   SPIDER_MOUNT_ID   -> SpiderMountData (on Spider entities)
        //   HEROBRINE_PENDING_RESPAWN_ID -> HerobrineRespawnSnapshot (on Player entities)
        //   HEROBRINE_ENCOUNTER_ID -> HerobrineEncounterState (on Player entities)
    }

    // -------------------------------------------------------------------------
    // Platform-independent access helpers (used by game tests and production code)
    // These abstract over the backing storage mechanism.
    // -------------------------------------------------------------------------

    public static PlayerZombieData getPlayerZombie(net.minecraft.world.entity.Entity entity) {
        return dev.molang.iamzombieq.platform.Services.ATTACHMENT.get(
            entity, PLAYER_ZOMBIE_KEY, PlayerZombieData.DEFAULT);
    }

    public static void setPlayerZombie(net.minecraft.world.entity.Entity entity, PlayerZombieData data) {
        dev.molang.iamzombieq.platform.Services.ATTACHMENT.set(entity, PLAYER_ZOMBIE_KEY, data);
    }

    public static SpiderMountData getSpiderMount(net.minecraft.world.entity.Entity entity) {
        return dev.molang.iamzombieq.platform.Services.ATTACHMENT.get(
            entity, SPIDER_MOUNT_KEY, SpiderMountData.DEFAULT);
    }

    public static void setSpiderMount(net.minecraft.world.entity.Entity entity, SpiderMountData data) {
        dev.molang.iamzombieq.platform.Services.ATTACHMENT.set(entity, SPIDER_MOUNT_KEY, data);
    }

    public static HerobrineRespawnSnapshot getHerobrineRespawn(net.minecraft.world.entity.Entity entity) {
        return dev.molang.iamzombieq.platform.Services.ATTACHMENT.get(
            entity, HEROBRINE_PENDING_RESPAWN_KEY, HerobrineRespawnSnapshot.EMPTY);
    }

    public static void setHerobrineRespawn(net.minecraft.world.entity.Entity entity, HerobrineRespawnSnapshot data) {
        dev.molang.iamzombieq.platform.Services.ATTACHMENT.set(entity, HEROBRINE_PENDING_RESPAWN_KEY, data);
    }

    public static HerobrineEncounterState getHerobrineEncounter(net.minecraft.world.entity.Entity entity) {
        return dev.molang.iamzombieq.platform.Services.ATTACHMENT.get(
            entity, HEROBRINE_ENCOUNTER_KEY, new HerobrineEncounterState(0, Long.MIN_VALUE, -1L, false));
    }

    public static void setHerobrineEncounter(net.minecraft.world.entity.Entity entity, HerobrineEncounterState data) {
        dev.molang.iamzombieq.platform.Services.ATTACHMENT.set(entity, HEROBRINE_ENCOUNTER_KEY, data);
    }
}
