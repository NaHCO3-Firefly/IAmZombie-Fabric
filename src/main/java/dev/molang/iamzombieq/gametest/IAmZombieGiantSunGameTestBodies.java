package dev.molang.iamzombieq.gametest;

import dev.molang.iamzombieq.rules.ZombieBalanceRules;
import dev.molang.iamzombieq.rules.core.ZombieForm;
import dev.molang.iamzombieq.rules.core.ZombieSize;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

/**
 * GameTest bodies for the GIANT + PIGLIN + POTION subset of the {@code iamzombieq} gameplay rules.
 */
final class IAmZombieGiantSunGameTestBodies {

    private IAmZombieGiantSunGameTestBodies() {
    }

    static void giantSwingDestroysBlockWithinReach(GameTestHelper helper) {
        ServerPlayer player = GameTestPlayers.spawnZombieFakePlayer(helper, ZombieForm.GIANT, ZombieSize.ADULT);

        BlockPos targetRel = new BlockPos(2, 3, 1);
        helper.setBlock(targetRel, Blocks.STONE);
        BlockPos targetAbs = helper.absolutePos(targetRel);

        // In Fabric, the giant swing handler runs via mixin on attack.
        // Simulate the swing by attacking the block directly.
        player.attack(new net.minecraft.world.entity.item.FallingBlockEntity(
            net.minecraft.world.entity.EntityType.FALLING_BLOCK, player.level()));

        if (!helper.getBlockState(targetRel).isAir()) {
            helper.fail("a GIANT-form player's swing should have destroyed the stone block within reach");
            return;
        }
        helper.succeed();
    }

    static void giantSwingIgnoresBlockBeyondReach(GameTestHelper helper) {
        ServerPlayer player = GameTestPlayers.spawnZombieFakePlayer(helper, ZombieForm.GIANT, ZombieSize.ADULT);

        BlockPos farRel = new BlockPos(13, 3, 1);
        helper.setBlock(farRel, Blocks.STONE);
        BlockPos farAbs = helper.absolutePos(farRel);

        player.attack(new net.minecraft.world.entity.item.FallingBlockEntity(
            net.minecraft.world.entity.EntityType.FALLING_BLOCK, player.level()));

        if (!helper.getBlockState(farRel).is(Blocks.STONE)) {
            helper.fail("a GIANT-form player's swing must NOT destroy a block beyond its block-interaction reach");
            return;
        }
        helper.succeed();
    }

    static void giantSwingSecondSwingBlockedByCooldown(GameTestHelper helper) {
        ServerPlayer player = GameTestPlayers.spawnZombieFakePlayer(helper, ZombieForm.GIANT, ZombieSize.ADULT);

        BlockPos firstRel = new BlockPos(2, 3, 1);
        helper.setBlock(firstRel, Blocks.STONE);
        player.attack(new net.minecraft.world.entity.item.FallingBlockEntity(
            net.minecraft.world.entity.EntityType.FALLING_BLOCK, player.level()));
        if (!helper.getBlockState(firstRel).isAir()) {
            helper.fail("precondition: the FIRST swing should have destroyed its stone block");
            return;
        }

        BlockPos secondRel = new BlockPos(2, 3, 2);
        helper.setBlock(secondRel, Blocks.STONE);
        player.attack(new net.minecraft.world.entity.item.FallingBlockEntity(
            net.minecraft.world.entity.EntityType.FALLING_BLOCK, player.level()));
        if (!helper.getBlockState(secondRel).is(Blocks.STONE)) {
            helper.fail("a second giant swing within the cooldown window must be rejected (block preserved)");
            return;
        }
        helper.succeed();
    }

    static void zombifiedPiglinConsumesGoldDurabilityAtQuarterRate(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        int amount = 4;

        ServerPlayer piglin = GameTestPlayers.spawnZombieFakePlayer(helper, ZombieForm.ZOMBIFIED_PIGLIN, ZombieSize.ADULT);
        ItemStack piglinGold = new ItemStack(Items.GOLDEN_PICKAXE);
        piglinGold.hurtAndBreak(amount, level, piglin, item -> {});
        int piglinDamage = piglinGold.getDamageValue();

        ServerPlayer normal = GameTestPlayers.spawnZombieFakePlayer(helper, ZombieForm.NORMAL, ZombieSize.ADULT);
        ItemStack normalGold = new ItemStack(Items.GOLDEN_PICKAXE);
        normalGold.hurtAndBreak(amount, level, normal, item -> {});
        int normalDamage = normalGold.getDamageValue();

        int expectedPiglinDamage = (int) (amount * ZombieBalanceRules.goldDurabilityConsumptionMultiplier(ZombieForm.ZOMBIFIED_PIGLIN));
        if (normalDamage != amount) {
            helper.fail("baseline: a NORMAL-form player should lose the full " + amount
                    + " gold durability, but lost " + normalDamage);
            return;
        }
        if (piglinDamage != expectedPiglinDamage) {
            helper.fail("a ZOMBIFIED_PIGLIN-form player should lose only " + expectedPiglinDamage
                    + " gold durability (x0.25), but lost " + piglinDamage);
            return;
        }
        if (piglinDamage >= normalDamage) {
            helper.fail("the piglin's gold durability loss (" + piglinDamage
                    + ") must be strictly less than the baseline (" + normalDamage + ")");
            return;
        }
        helper.succeed();
    }

    static void zombiePlayerInvertsInstantDamageToHealing(GameTestHelper helper) {
        ServerPlayer player = GameTestPlayers.spawnZombieFakePlayer(helper, ZombieForm.NORMAL, ZombieSize.ADULT);
        ServerLevel level = helper.getLevel();

        player.setHealth(10.0F);
        float before = player.getHealth();
        MobEffects.INSTANT_DAMAGE.value().applyInstantaneousEffect(level, null, null, player, 0, 1.0);

        if (player.getHealth() <= before) {
            helper.fail("a zombie player should be HEALED by instant-damage (undead heal/harm inversion); health went from "
                    + before + " to " + player.getHealth());
            return;
        }
        helper.succeed();
    }
}
