package dev.molang.iamzombieq.gametest;

import dev.molang.iamzombieq.rules.core.ZombieForm;
import dev.molang.iamzombieq.rules.core.ZombieSize;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.animal.equine.ZombieHorse;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import dev.molang.iamzombieq.IAmZombieConfig;
import dev.molang.iamzombieq.IAmZombieItems;

/**
 * Additional bodies for the {@code iamzombieq} FOOD + INF acceptance domains.
 */
final class IAmZombieFoodInfGameTestBodies {

    private IAmZombieFoodInfGameTestBodies() {
    }

    static void foodGoldenApple(GameTestHelper helper) {
        ServerPlayer player = GameTestPlayers.spawnZombieFakePlayer(helper, ZombieForm.NORMAL, ZombieSize.ADULT);
        player.removeEffect(MobEffects.ABSORPTION);
        player.removeEffect(MobEffects.HUNGER);

        feed(player, new ItemStack(Items.GOLDEN_APPLE));

        if (!assertEffect(helper, player, MobEffects.ABSORPTION, 0, "golden_apple -> Absorption I")) {
            return;
        }
        if (!assertEffect(helper, player, MobEffects.HUNGER, 0, "golden_apple -> Hunger I")) {
            return;
        }
        helper.succeed();
    }

    static void foodEnchantedGoldenApple(GameTestHelper helper) {
        ServerPlayer player = GameTestPlayers.spawnZombieFakePlayer(helper, ZombieForm.NORMAL, ZombieSize.ADULT);
        player.removeEffect(MobEffects.ABSORPTION);
        player.removeEffect(MobEffects.RESISTANCE);
        player.removeEffect(MobEffects.HUNGER);

        feed(player, new ItemStack(Items.ENCHANTED_GOLDEN_APPLE));

        if (!assertEffect(helper, player, MobEffects.ABSORPTION, 1, "enchanted_golden_apple -> Absorption II")) {
            return;
        }
        if (!assertEffect(helper, player, MobEffects.RESISTANCE, 0, "enchanted_golden_apple -> Resistance I")) {
            return;
        }
        if (!assertEffect(helper, player, MobEffects.HUNGER, 0, "enchanted_golden_apple -> Hunger I")) {
            return;
        }
        helper.succeed();
    }

    static void foodPufferfish(GameTestHelper helper) {
        ServerPlayer player = GameTestPlayers.spawnZombieFakePlayer(helper, ZombieForm.NORMAL, ZombieSize.ADULT);
        player.removeEffect(MobEffects.ABSORPTION);
        player.removeEffect(MobEffects.REGENERATION);
        player.removeEffect(MobEffects.HUNGER);

        feed(player, new ItemStack(Items.PUFFERFISH));

        if (!assertEffect(helper, player, MobEffects.ABSORPTION, 0, "pufferfish -> Absorption I")) {
            return;
        }
        if (!assertEffect(helper, player, MobEffects.REGENERATION, IAmZombieConfig.PUFFERFISH_REGENERATION_AMPLIFIER.get(), "pufferfish -> Regeneration at the configured amplifier")) {
            return;
        }
        if (player.getEffect(MobEffects.HUNGER) != null) {
            helper.fail("pufferfish (T4 SPECIAL) must NOT inflict the human-food Hunger punishment");
            return;
        }
        helper.succeed();
    }

    static void foodSpiderEye(GameTestHelper helper) {
        ServerPlayer player = GameTestPlayers.spawnZombieFakePlayer(helper, ZombieForm.NORMAL, ZombieSize.ADULT);
        player.removeEffect(MobEffects.NIGHT_VISION);
        player.removeEffect(MobEffects.HUNGER);

        feed(player, new ItemStack(Items.SPIDER_EYE));

        if (!assertEffect(helper, player, MobEffects.NIGHT_VISION, 0, "spider_eye -> Night Vision")) {
            return;
        }
        if (player.getEffect(MobEffects.HUNGER) != null) {
            helper.fail("spider_eye (T1 CARRION) must NOT inflict the human-food Hunger punishment");
            return;
        }
        helper.succeed();
    }

    static void foodHumanHungerAmplifier(GameTestHelper helper) {
        ServerPlayer player = GameTestPlayers.spawnZombieFakePlayer(helper, ZombieForm.NORMAL, ZombieSize.ADULT);
        player.removeEffect(MobEffects.HUNGER);
        player.removeEffect(MobEffects.NAUSEA);

        feed(player, new ItemStack(Items.COOKED_PORKCHOP));

        if (!assertEffect(helper, player, MobEffects.HUNGER, IAmZombieConfig.HUMAN_FOOD_HUNGER_AMPLIFIER.get(), "cooked_porkchop -> Hunger at the configured human-food amplifier")) {
            return;
        }
        if (player.getEffect(MobEffects.NAUSEA) == null) {
            helper.fail("T3 HUMAN_COOKED should also inflict Nausea");
            return;
        }
        helper.succeed();
    }

    static void foodSweetSlowness(GameTestHelper helper) {
        ServerPlayer player = GameTestPlayers.spawnZombieFakePlayer(helper, ZombieForm.NORMAL, ZombieSize.ADULT);
        player.removeEffect(MobEffects.SLOWNESS);
        player.removeEffect(MobEffects.HUNGER);

        feed(player, new ItemStack(Items.COOKIE));

        if (!assertEffect(helper, player, MobEffects.SLOWNESS, 0, "cookie (SWEET) -> Slowness I")) {
            return;
        }
        if (player.getEffect(MobEffects.HUNGER) == null) {
            helper.fail("cookie is still T3 HUMAN_COOKED, so it should also inflict the human-food Hunger");
            return;
        }
        helper.succeed();
    }

    static void foodSuperRottenFleshStrength(GameTestHelper helper) {
        ServerPlayer player = GameTestPlayers.spawnZombieFakePlayer(helper, ZombieForm.NORMAL, ZombieSize.ADULT);
        player.removeEffect(MobEffects.STRENGTH);

        feed(player, new ItemStack(IAmZombieItems.SUPER_ROTTEN_FLESH));

        if (!assertEffect(helper, player, MobEffects.STRENGTH, IAmZombieConfig.SUPER_ROTTEN_FLESH_STRENGTH_AMPLIFIER.get(), "super_rotten_flesh -> Strength at the configured amplifier")) {
            return;
        }
        helper.succeed();
    }

    static void foodChorusFruit(GameTestHelper helper) {
        ServerPlayer player = GameTestPlayers.spawnZombieFakePlayer(helper, ZombieForm.NORMAL, ZombieSize.ADULT);
        player.removeEffect(MobEffects.SLOW_FALLING);
        player.removeEffect(MobEffects.NAUSEA);

        feed(player, new ItemStack(Items.CHORUS_FRUIT));

        if (!assertEffect(helper, player, MobEffects.SLOW_FALLING, 0, "chorus_fruit -> Slow Falling")) {
            return;
        }
        if (!assertEffect(helper, player, MobEffects.NAUSEA, 0, "chorus_fruit -> Nausea")) {
            return;
        }
        helper.succeed();
    }

    static void foodHoneyBottle(GameTestHelper helper) {
        ServerPlayer player = GameTestPlayers.spawnZombieFakePlayer(helper, ZombieForm.NORMAL, ZombieSize.ADULT);
        player.removeEffect(MobEffects.NAUSEA);
        player.removeEffect(MobEffects.HUNGER);

        feed(player, new ItemStack(Items.HONEY_BOTTLE));

        if (!assertEffect(helper, player, MobEffects.NAUSEA, 0, "honey_bottle -> Nausea")) {
            return;
        }
        if (player.getEffect(MobEffects.HUNGER) != null) {
            helper.fail("honey_bottle (T4 SPECIAL) must NOT inflict the human-food Hunger punishment");
            return;
        }
        helper.succeed();
    }

    static void infectionHorse(GameTestHelper helper) {
        ServerPlayer player = GameTestPlayers.spawnZombieFakePlayer(helper, ZombieForm.NORMAL, ZombieSize.ADULT);
        ServerLevel level = helper.getLevel();

        Horse horse = helper.spawn(EntityTypes.HORSE, new BlockPos(1, 2, 1));
        DamageSource killedByPlayer = level.damageSources().playerAttack(player);
        horse.hurtServer(level, killedByPlayer, Float.MAX_VALUE);

        helper.succeedWhen(() -> {
            if (horse.isAlive() && !horse.isRemoved()) {
                throw helper.assertionException("horse has not been converted yet");
            }
            if (helper.getEntities(EntityTypes.ZOMBIE_HORSE, new BlockPos(1, 2, 1), 1.5).isEmpty()) {
                throw helper.assertionException("expected a ZombieHorse after the zombie player killed the horse");
            }
        });
    }

    private static boolean assertEffect(GameTestHelper helper, ServerPlayer player, Holder<MobEffect> effect, int expectedAmplifier, String what) {
        MobEffectInstance instance = player.getEffect(effect);
        if (instance == null) {
            helper.fail("expected effect missing: " + what);
            return false;
        }
        if (instance.getAmplifier() != expectedAmplifier) {
            helper.fail("wrong amplifier for " + what + ": expected " + expectedAmplifier + " but was " + instance.getAmplifier());
            return false;
        }
        return true;
    }

    private static void feed(ServerPlayer player, ItemStack food) {
        player.setItemInHand(InteractionHand.MAIN_HAND, food);
        player.startUsingItem(InteractionHand.MAIN_HAND);
        player.completeUsingItem();
        player.stopUsingItem();
    }
}
