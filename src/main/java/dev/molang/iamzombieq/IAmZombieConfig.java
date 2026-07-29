package dev.molang.iamzombieq;

import java.util.List;
import java.util.function.Supplier;

/**
 * Configuration for I Am Zombie? mod using Cloth Config API.
 * Replaces NeoForge's ModConfigSpec with simple Supplier-backed values.
 *
 * <p>Each field is a Supplier so existing callers (which expect .get()) work without signature changes.
 * A full Cloth Config GUI screen will be added in a follow-up; for now values are hardcoded defaults.
 */
public final class IAmZombieConfig {

    // ---- Debug / Diagnostics ----
    public static final Supplier<Boolean> DEBUG_LOGGING = () -> false;

    // ---- Core Gameplay ----
    public static final Supplier<Integer> STARTING_ROTTEN_FLESH = () -> 8;
    public static final Supplier<Boolean> UNLOCK_COFFIN_RECIPES_ON_FIRST_JOIN = () -> true;
    public static final Supplier<Boolean> UNDEAD_IGNORE_ZOMBIE_PLAYER = () -> true;

    // ---- Bed Explosion ----
    public static final Supplier<Float> BED_EXPLOSION_POWER = () -> 5.0F;
    public static final Supplier<Boolean> BED_EXPLOSION_FIRE = () -> true;

    // ---- Food ----
    public static final Supplier<Integer> COOKED_HUMAN_FOOD_HUNGER_DURATION = () -> 360;
    public static final Supplier<Integer> COOKED_HUMAN_FOOD_NAUSEA_DURATION = () -> 240;
    public static final Supplier<Integer> SPIDER_EYE_NIGHT_VISION_DURATION = () -> 900;
    public static final Supplier<Integer> GLOW_BERRIES_NIGHT_VISION_DURATION = () -> 120;
    public static final Supplier<Integer> FISH_WATER_BREATHING_DURATION = () -> 400;
    public static final Supplier<Integer> TROPICAL_FISH_WATER_BREATHING_DURATION = () -> 300;
    public static final Supplier<Integer> RAW_RABBIT_SPEED_DURATION = () -> 160;
    public static final Supplier<Integer> PUFFERFISH_ABSORPTION_AMPLIFIER = () -> 2;
    public static final Supplier<Integer> PUFFERFISH_ABSORPTION_DURATION = () -> 200;
        public static final Supplier<Integer> PUFFERFISH_REGENERATION_DURATION = () -> 200;
    public static final Supplier<Integer> SUPER_ROTTEN_FLESH_STRENGTH_DURATION = () -> 900;
    public static final Supplier<Integer> GOLDEN_APPLE_ABSORPTION_DURATION = () -> 1200;
    public static final Supplier<Integer> GOLDEN_APPLE_ABSORPTION_AMPLIFIER = () -> 1;
    public static final Supplier<List<? extends String>> ZOMBIE_FOODS = List::of;

    // ---- Mounts ----
    public static final Supplier<Double> SPIDER_MOUNT_SPEED = () -> 0.30;

    // ---- Herobrine ----
    public static final Supplier<Boolean> HEROBRINE_ENABLED = () -> true;
    public static final Supplier<Boolean> HEROBRINE_HEARTBEAT_ENABLED = () -> true;
    public static final Supplier<Double> HEROBRINE_HEARTBEAT_FAR_DISTANCE = () -> 32.0;
    public static final Supplier<Double> HEROBRINE_HEARTBEAT_NEAR_DISTANCE = () -> 8.0;
    public static final Supplier<Boolean> HEROBRINE_JOLT_ENABLED = () -> true;

    // ---- Food (detailed — used by ZombieFoodRules) ----
    public static final Supplier<Integer> SWEET_SLOWNESS_DURATION_TICKS = () -> 200;
    public static final Supplier<Integer> SUPER_ROTTEN_FLESH_STRENGTH_DURATION_TICKS = () -> 900;
    public static final Supplier<Integer> SUPER_ROTTEN_FLESH_STRENGTH_AMPLIFIER = () -> 1;
    public static final Supplier<Integer> SUPER_ROTTEN_FLESH_SATURATION_DURATION_TICKS = () -> 160;
    public static final Supplier<Integer> SPIDER_EYE_NIGHT_VISION_DURATION_TICKS = () -> 900;
    public static final Supplier<Integer> T1_CARRION_WATER_BREATHING_DURATION_TICKS = () -> 400;
    public static final Supplier<Integer> GOLDEN_APPLE_ABSORPTION_DURATION_TICKS = () -> 1200;
    public static final Supplier<Integer> GOLDEN_APPLE_HUNGER_DURATION_TICKS = () -> 600;
    public static final Supplier<Integer> ENCHANTED_GOLDEN_APPLE_ABSORPTION_DURATION_TICKS = () -> 2400;
    public static final Supplier<Integer> ENCHANTED_GOLDEN_APPLE_RESISTANCE_DURATION_TICKS = () -> 600;
    public static final Supplier<Integer> ENCHANTED_GOLDEN_APPLE_HUNGER_DURATION_TICKS = () -> 400;
    public static final Supplier<Integer> PUFFERFISH_ABSORPTION_DURATION_TICKS = () -> 200;
    public static final Supplier<Integer> PUFFERFISH_REGENERATION_DURATION_TICKS = () -> 200;
    public static final Supplier<Integer> PUFFERFISH_REGENERATION_AMPLIFIER = () -> 1;
        public static final Supplier<Integer> CHORUS_SLOW_FALLING_DURATION_TICKS = () -> 200;
    public static final Supplier<Integer> CHORUS_NAUSEA_DURATION_TICKS = () -> 200;
    public static final Supplier<Integer> HONEY_NAUSEA_DURATION_TICKS = () -> 200;

    // ---- Combat ----
    public static final Supplier<Boolean> REINFORCEMENTS_ENABLED = () -> true;
    public static final Supplier<Integer> REINFORCEMENT_SPAWN_ATTEMPTS = () -> 50;
    public static final java.util.function.Function<dev.molang.iamzombieq.rules.core.ZombieForm, Integer> configuredInnateArmor = form -> switch (form) {
        case HUSK -> 4;
        case DROWNED, ZOMBIFIED_PIGLIN, NORMAL -> 2;
        case GIANT -> 0;
    };

    private IAmZombieConfig() {
    }
}
