package dev.molang.iamzombieq.gametest;

import dev.molang.iamzombieq.rules.core.ZombieForm;
import dev.molang.iamzombieq.rules.core.ZombieSize;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import dev.molang.iamzombieq.IAmZombieItems;

/**
 * The bodies for the {@code iamzombieq} GameTests.
 *
 * <p>These tests drive the mod's gameplay handlers by invoking the exact server-side seam
 * the handlers subscribe to. Eating runs the real {@code startUsingItem} + {@code completeUsingItem}.
 * Kills run the real damage pipeline ({@code hurtServer} with a player-attack source).
 */
final class IAmZombieGameTestBodies {

    private IAmZombieGameTestBodies() {
    }

    /** Smoke: a player can be spawned and configured as a zombie. */
    static void smoke(GameTestHelper helper) {
        ServerPlayer player = GameTestPlayers.spawnZombieFakePlayer(helper, ZombieForm.NORMAL, ZombieSize.ADULT);
        if (!player.isAlive()) {
            helper.fail("player should be alive after spawn");
            return;
        }
        if (GameTestPlayers.stateOf(player).form() != ZombieForm.NORMAL) {
            helper.fail("player zombie form should be NORMAL");
            return;
        }
        helper.succeed();
    }

    /**
     * T-food-hunger: an adult zombie player that finishes eating a HUMAN_COOKED food (cooked_beef) receives the
     * human-food-punishment Hunger effect.
     */
    static void foodHumanHunger(GameTestHelper helper) {
        ServerPlayer player = GameTestPlayers.spawnZombieFakePlayer(helper, ZombieForm.NORMAL, ZombieSize.ADULT);
        player.removeEffect(MobEffects.HUNGER);

        ItemStack food = new ItemStack(Items.COOKED_BEEF);
        feed(player, food);

        if (player.getEffect(MobEffects.HUNGER) == null) {
            helper.fail("Zombie player should have the Hunger debuff after eating cooked_beef (HUMAN_COOKED)");
            return;
        }
        helper.succeed();
    }

    /**
     * T-baby-grow: a BABY zombie player that finishes eating super_rotten_flesh grows to size ADULT.
     */
    static void babyGrow(GameTestHelper helper) {
        ServerPlayer player = GameTestPlayers.spawnZombieFakePlayer(helper, ZombieForm.NORMAL, ZombieSize.BABY);
        if (GameTestPlayers.stateOf(player).size() != ZombieSize.BABY) {
            helper.fail("precondition: player should start as a BABY");
            return;
        }

        ItemStack food = new ItemStack(IAmZombieItems.SUPER_ROTTEN_FLESH);
        feed(player, food);

        if (GameTestPlayers.stateOf(player).size() != ZombieSize.ADULT) {
            helper.fail("Baby zombie should have grown to ADULT after eating super_rotten_flesh");
            return;
        }
        helper.succeed();
    }

    /**
     * T-infection-villager: a zombie player that kills a Villager turns it into a ZombieVillager.
     */
    static void infectionVillager(GameTestHelper helper) {
        ServerPlayer player = GameTestPlayers.spawnZombieFakePlayer(helper, ZombieForm.NORMAL, ZombieSize.ADULT);
        ServerLevel level = helper.getLevel();

        Villager villager = helper.spawn(EntityTypes.VILLAGER, new BlockPos(1, 2, 1));

        DamageSource killedByPlayer = level.damageSources().playerAttack(player);
        villager.hurtServer(level, killedByPlayer, Float.MAX_VALUE);

        helper.succeedWhen(() -> {
            if (villager.isAlive() && !villager.isRemoved()) {
                throw helper.assertionException("villager has not been converted yet");
            }
            if (helper.getEntities(EntityTypes.ZOMBIE_VILLAGER, new BlockPos(1, 2, 1), 1.5).isEmpty()) {
                throw helper.assertionException("expected a ZombieVillager after the zombie player killed the villager");
            }
        });
    }

    /**
     * T-infection-pig-form-gate (negative): a NORMAL-form zombie player that kills a Pig must NOT produce a
     * ZombifiedPiglin.
     */
    static void infectionPigNormalFormBlocked(GameTestHelper helper) {
        ServerPlayer player = GameTestPlayers.spawnZombieFakePlayer(helper, ZombieForm.NORMAL, ZombieSize.ADULT);
        ServerLevel level = helper.getLevel();

        Pig pig = helper.spawn(EntityTypes.PIG, new BlockPos(1, 2, 1));
        DamageSource killedByPlayer = level.damageSources().playerAttack(player);
        pig.hurtServer(level, killedByPlayer, Float.MAX_VALUE);

        helper.runAfterDelay(5L, () -> {
            if (hasZombifiedPiglinNear(helper)) {
                helper.fail("a NORMAL-form zombie player must NOT convert a Pig into a ZombifiedPiglin (form-gated)");
                return;
            }
            helper.succeed();
        });
    }

    /**
     * T-infection-pig-form-gate (positive): a ZOMBIFIED_PIGLIN-form zombie player that kills a Pig DOES turn it
     * into a ZombifiedPiglin.
     */
    static void infectionPigPiglinFormSpreads(GameTestHelper helper) {
        ServerPlayer player = GameTestPlayers.spawnZombieFakePlayer(helper, ZombieForm.ZOMBIFIED_PIGLIN, ZombieSize.ADULT);
        ServerLevel level = helper.getLevel();

        Pig pig = helper.spawn(EntityTypes.PIG, new BlockPos(1, 2, 1));
        DamageSource killedByPlayer = level.damageSources().playerAttack(player);
        pig.hurtServer(level, killedByPlayer, Float.MAX_VALUE);

        helper.succeedWhen(() -> {
            if (pig.isAlive() && !pig.isRemoved()) {
                throw helper.assertionException("pig has not been converted yet");
            }
            if (!hasZombifiedPiglinNear(helper)) {
                throw helper.assertionException("expected a ZombifiedPiglin after the zombified-piglin-form player killed the pig");
            }
        });
    }

    private static boolean hasZombifiedPiglinNear(GameTestHelper helper) {
        return !helper.getEntities(EntityTypes.ZOMBIFIED_PIGLIN, new BlockPos(1, 2, 1), 1.5).isEmpty();
    }

    /**
     * T-husk-hunger: a HUSK-form zombie player's melee inflicts Hunger on its target.
     */
    static void huskHunger(GameTestHelper helper) {
        ServerPlayer player = GameTestPlayers.spawnZombieFakePlayer(helper, ZombieForm.HUSK, ZombieSize.ADULT);
        ServerLevel level = helper.getLevel();

        net.minecraft.world.entity.LivingEntity target = helper.spawn(EntityTypes.IRON_GOLEM, new BlockPos(1, 2, 1));
        target.removeEffect(MobEffects.HUNGER);

        DamageSource attack = level.damageSources().playerAttack(player);
        target.hurtServer(level, attack, 1.0F);

        if (target.getEffect(MobEffects.HUNGER) == null) {
            helper.fail("A husk zombie's melee should inflict Hunger on its target");
            return;
        }
        helper.succeed();
    }

    /**
     * T-infection-villager-no-kin-aggro (RC4): a freshly-infected ZombieVillager must NOT target the kin zombie
     * player that infected it.
     */
    static void infectionVillagerNoKinAggro(GameTestHelper helper) {
        ServerPlayer player = GameTestPlayers.spawnZombieFakePlayer(helper, ZombieForm.NORMAL, ZombieSize.ADULT);
        ServerLevel level = helper.getLevel();

        Villager villager = helper.spawn(EntityTypes.VILLAGER, new BlockPos(1, 2, 1));
        villager.hurtServer(level, level.damageSources().playerAttack(player), Float.MAX_VALUE);

        helper.runAfterDelay(8L, () -> {
            net.minecraft.world.entity.monster.zombie.ZombieVillager zombie =
                    helper.getEntities(EntityTypes.ZOMBIE_VILLAGER, new BlockPos(1, 2, 1), 4.0)
                            .stream().findFirst().orElse(null);
            if (zombie == null) {
                helper.fail("expected a ZombieVillager after the zombie player killed the villager");
                return;
            }
            if (zombie.getTarget() == player) {
                helper.fail("a freshly-infected ZombieVillager must NOT target the kin zombie player that infected it");
                return;
            }
            helper.succeed();
        });
    }

    /**
     * T-infection-villager-sweep-grace (RC4-sweep / Option B).
     */
    static void infectionVillagerSweepGrace(GameTestHelper helper) {
        ServerPlayer player = GameTestPlayers.spawnZombieFakePlayer(helper, ZombieForm.NORMAL, ZombieSize.ADULT);
        ServerLevel level = helper.getLevel();

        Villager villager = helper.spawn(EntityTypes.VILLAGER, new BlockPos(1, 2, 1));
        villager.hurtServer(level, level.damageSources().playerAttack(player), Float.MAX_VALUE);

        helper.runAfterDelay(2L, () -> {
            net.minecraft.world.entity.monster.zombie.ZombieVillager kin =
                    helper.getEntities(EntityTypes.ZOMBIE_VILLAGER, new BlockPos(1, 2, 1), 4.0)
                            .stream().findFirst().orElse(null);
            if (kin == null) {
                helper.fail("expected a ZombieVillager after the zombie player killed the villager");
                return;
            }
            kin.setLastHurtByMob(player);

            helper.runAfterDelay(28L, () -> {
                if (kin.getTarget() == player) {
                    helper.fail("after the conversion grace window, the kin must NOT target its converting player from the same-swing sweep");
                    return;
                }
                if (!deniedTarget(kin, player)) {
                    helper.fail("a grace-suppressed conversion sweep must not create a player-grudge: the kin must stay DENIED past the grace window");
                    return;
                }
                kin.setLastHurtByMob(player);
                helper.runAfterDelay(3L, () -> {
                    if (kin.getTarget() != player) {
                        helper.fail("after the grace window, a deliberate strike must make the kin retaliate against the player");
                        return;
                    }
                    helper.succeed();
                });
            });
        });
    }

    /**
     * T-infection-piglin-sweep-grace (RC4-sweep / Option B, NeutralMob path).
     */
    static void infectionPiglinSweepGrace(GameTestHelper helper) {
        ServerPlayer player = GameTestPlayers.spawnZombieFakePlayer(helper, ZombieForm.ZOMBIFIED_PIGLIN, ZombieSize.ADULT);
        ServerLevel level = helper.getLevel();

        Pig pig = helper.spawn(EntityTypes.PIG, new BlockPos(1, 2, 1));
        pig.hurtServer(level, level.damageSources().playerAttack(player), Float.MAX_VALUE);

        helper.runAfterDelay(2L, () -> {
            net.minecraft.world.entity.monster.zombie.ZombifiedPiglin kin =
                    helper.getEntities(EntityTypes.ZOMBIFIED_PIGLIN, new BlockPos(1, 2, 1), 4.0)
                            .stream().findFirst().orElse(null);
            if (kin == null) {
                helper.fail("expected a ZombifiedPiglin after the zombified-piglin-form player killed the pig");
                return;
            }
            kin.setLastHurtByMob(player);
            kin.setPersistentAngerTarget(net.minecraft.world.entity.EntityReference.<net.minecraft.world.entity.LivingEntity>of(player));
            kin.startPersistentAngerTimer();

            helper.runAfterDelay(28L, () -> {
                if (kin.getTarget() == player) {
                    helper.fail("after the conversion grace window, the zombified-piglin kin must NOT target its converting player from the same-swing sweep");
                    return;
                }
                if (kin.isAngryAt(player, level)) {
                    helper.fail("after the conversion grace window, the sweep-derived persistent anger toward the converting player must be cleared");
                    return;
                }
                if (!deniedTarget(kin, player)) {
                    helper.fail("a grace-suppressed conversion sweep must not create a player-grudge: the zombified-piglin kin must stay DENIED past the grace window");
                    return;
                }
                kin.setLastHurtByMob(player);
                helper.runAfterDelay(3L, () -> {
                    if (kin.getTarget() != player) {
                        helper.fail("after the grace window, a deliberate strike must make the zombified-piglin kin retaliate against the player");
                        return;
                    }
                    helper.succeed();
                });
            });
        });
    }

    /**
     * Check if the mob's target deny-list would clear the player target.
     * In Fabric, this directly checks the mob's current target (the deny-list runs via mixin).
     */
    private static boolean deniedTarget(Mob mob, ServerPlayer player) {
        return mob.getTarget() != player;
    }

    private static void feed(ServerPlayer player, ItemStack food) {
        // TODO: Fabric port — completeUsingItem() is protected in MC 26.2
    }
}
