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
        // TODO: Fabric port — FallingBlockEntity constructor / EntityType.FALLING_BLOCK changed in MC 26.2
    }

    static void giantSwingIgnoresBlockBeyondReach(GameTestHelper helper) {
        // TODO: Fabric port — FallingBlockEntity constructor / EntityType.FALLING_BLOCK changed in MC 26.2
    }

    static void giantSwingSecondSwingBlockedByCooldown(GameTestHelper helper) {
        // TODO: Fabric port — FallingBlockEntity constructor / EntityType.FALLING_BLOCK changed in MC 26.2
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
