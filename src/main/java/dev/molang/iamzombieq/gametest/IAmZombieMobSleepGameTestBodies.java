package dev.molang.iamzombieq.gametest;

import dev.molang.iamzombieq.IAmZombieItems;
import dev.molang.iamzombieq.rules.core.ZombieForm;
import dev.molang.iamzombieq.rules.core.ZombieSize;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.equine.TraderLlama;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * GameTest bodies for the MOB-targeting, SLEEP (bed/coffin), DOOR (break-speed) domains.
 */
final class IAmZombieMobSleepGameTestBodies {

    private IAmZombieMobSleepGameTestBodies() {
    }

    // -------------------------------------------------------------------------
    // MOB: who is allowed to target the zombie player
    // -------------------------------------------------------------------------

    static void mobUndeadIgnoresZombiePlayer(GameTestHelper helper) {
        ServerPlayer player = GameTestPlayers.spawnZombieFakePlayer(helper, ZombieForm.NORMAL, ZombieSize.ADULT);
        Zombie zombie = helper.spawn(EntityTypes.ZOMBIE, new BlockPos(2, 2, 2));

        if (clearedTarget(zombie, player)) {
            helper.succeed();
        } else {
            helper.fail("a vanilla zombie's target on the zombie player should be cleared (undead ignore the zombie player)");
        }
    }

    static void mobIronGolemNotFooledByMask(GameTestHelper helper) {
        ServerPlayer player = GameTestPlayers.spawnZombieFakePlayer(helper, ZombieForm.NORMAL, ZombieSize.ADULT);
        player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(IAmZombieItems.DISGUISE_MASK));
        IronGolem golem = helper.spawn(EntityTypes.IRON_GOLEM, new BlockPos(2, 2, 2));

        if (!clearedTarget(golem, player)) {
            helper.succeed();
        } else {
            helper.fail("the iron golem must keep its target on the zombie player even through the disguise mask");
        }
    }

    static void mobAxolotlAttacksDrownedForm(GameTestHelper helper) {
        ServerPlayer player = GameTestPlayers.spawnZombieFakePlayer(helper, ZombieForm.DROWNED, ZombieSize.ADULT);
        Axolotl axolotl = helper.spawn(EntityTypes.AXOLOTL, new BlockPos(2, 2, 2));

        if (!clearedTarget(axolotl, player)) {
            helper.succeed();
        } else {
            helper.fail("the axolotl must keep its target on a DROWNED-form zombie player");
        }
    }

    static void mobAxolotlIgnoresNormalForm(GameTestHelper helper) {
        ServerPlayer player = GameTestPlayers.spawnZombieFakePlayer(helper, ZombieForm.NORMAL, ZombieSize.ADULT);
        Axolotl axolotl = helper.spawn(EntityTypes.AXOLOTL, new BlockPos(2, 2, 2));

        if (clearedTarget(axolotl, player)) {
            helper.succeed();
        } else {
            helper.fail("the axolotl must NOT target a NORMAL-form zombie player (it hunts only the drowned form)");
        }
    }

    static void mobTraderLlamaAttacksNormalForm(GameTestHelper helper) {
        ServerPlayer player = GameTestPlayers.spawnZombieFakePlayer(helper, ZombieForm.NORMAL, ZombieSize.ADULT);
        TraderLlama llama = helper.spawn(EntityTypes.TRADER_LLAMA, new BlockPos(2, 2, 2));

        if (!clearedTarget(llama, player)) {
            helper.succeed();
        } else {
            helper.fail("the trader llama must keep its target on a NORMAL-form zombie player");
        }
    }

    static void mobTraderLlamaIgnoresZombifiedPiglinForm(GameTestHelper helper) {
        ServerPlayer player = GameTestPlayers.spawnZombieFakePlayer(helper, ZombieForm.ZOMBIFIED_PIGLIN, ZombieSize.ADULT);
        TraderLlama llama = helper.spawn(EntityTypes.TRADER_LLAMA, new BlockPos(2, 2, 2));

        if (clearedTarget(llama, player)) {
            helper.succeed();
        } else {
            helper.fail("the trader llama must NOT target a ZOMBIFIED_PIGLIN-form zombie player (its spit list excludes piglins)");
        }
    }

    /**
     * Check if the targeting deny-list clears the mob's target on the player.
     * The deny-list runs via mixin on Mob.setTarget() in Fabric.
     */
    private static boolean clearedTarget(Mob mob, ServerPlayer player) {
        mob.setTarget(player);
        return mob.getTarget() != player;
    }

    // -------------------------------------------------------------------------
    // SLEEP: a zombie player's bed right-click explodes the bed (SLEEP-001)
    // -------------------------------------------------------------------------

    static void sleepBedExplodesOnRightClick(GameTestHelper helper) {
        ServerPlayer player = GameTestPlayers.spawnZombieFakePlayer(helper, ZombieForm.NORMAL, ZombieSize.ADULT);

        BlockPos footRel = new BlockPos(2, 2, 2);
        BlockPos headRel = new BlockPos(2, 2, 1);
        Direction facing = Direction.NORTH;
        Block bed = Blocks.BED.pick(DyeColor.RED);
        BlockState footState = bed.defaultBlockState()
                .setValue(HorizontalDirectionalBlock.FACING, facing)
                .setValue(BedBlock.PART, BedPart.FOOT);
        BlockState headState = bed.defaultBlockState()
                .setValue(HorizontalDirectionalBlock.FACING, facing)
                .setValue(BedBlock.PART, BedPart.HEAD);
        helper.setBlock(footRel, footState);
        helper.setBlock(headRel, headState);

        BlockPos footAbs = helper.absolutePos(footRel);
        helper.useBlock(footAbs, player);

        if (helper.getBlockState(footRel).getBlock() instanceof BedBlock
                || helper.getBlockState(headRel).getBlock() instanceof BedBlock) {
            helper.fail("a zombie player's bed right-click should explode/destroy both bed halves");
            return;
        }
        helper.succeed();
    }

    static void coffinBreakDropsExactlyOne(GameTestHelper helper) {
        BlockPos footRel = new BlockPos(2, 2, 2);
        BlockPos headRel = new BlockPos(2, 2, 1);
        Direction facing = Direction.NORTH;
        Block coffin = dev.molang.iamzombieq.IAmZombieBlocks.COFFIN;
        helper.setBlock(footRel, coffin.defaultBlockState()
                .setValue(HorizontalDirectionalBlock.FACING, facing)
                .setValue(dev.molang.iamzombieq.block.CoffinBlock.PART, BedPart.FOOT));
        helper.setBlock(headRel, coffin.defaultBlockState()
                .setValue(HorizontalDirectionalBlock.FACING, facing)
                .setValue(dev.molang.iamzombieq.block.CoffinBlock.PART, BedPart.HEAD));

        helper.getLevel().destroyBlock(helper.absolutePos(footRel), true);

        helper.runAfterDelay(5L, () -> {
            int coffins = 0;
            for (net.minecraft.world.entity.item.ItemEntity item :
                    helper.getEntities(EntityTypes.ITEM, footRel, 4.0)) {
                if (item.getItem().is(IAmZombieItems.COFFIN)) {
                    coffins += item.getItem().getCount();
                }
            }
            if (coffins != 1) {
                helper.fail("breaking one half of a 2-part coffin must drop exactly 1 coffin (got " + coffins + ")");
                return;
            }
            helper.succeed();
        });
    }

    // -------------------------------------------------------------------------
    // DOOR: empty-hand wooden-door break-speed x3 (DOOR-001) / item-in-hand no boost (DOOR-002)
    // -------------------------------------------------------------------------

    static void doorEmptyHandBoostsWoodenDoorBreak(GameTestHelper helper) {
        ServerPlayer player = GameTestPlayers.spawnZombieFakePlayer(helper, ZombieForm.NORMAL, ZombieSize.ADULT);
        player.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);

        float original = 1.0F;
        float boosted = breakSpeed(helper, player, Blocks.OAK_DOOR.defaultBlockState(), original);
        if (Math.abs(boosted - original * 3.0F) > 1.0e-4F) {
            helper.fail("an empty-handed zombie should break a wooden door 3x faster (got " + boosted + ")");
            return;
        }
        helper.succeed();
    }

    static void doorItemInHandNoBoost(GameTestHelper helper) {
        ServerPlayer player = GameTestPlayers.spawnZombieFakePlayer(helper, ZombieForm.NORMAL, ZombieSize.ADULT);
        player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(net.minecraft.world.item.Items.STICK));

        float original = 1.0F;
        float speed = breakSpeed(helper, player, Blocks.OAK_DOOR.defaultBlockState(), original);
        if (Math.abs(speed - original) > 1.0e-4F) {
            helper.fail("a zombie holding an item should get NO wooden-door break boost (got " + speed + ")");
            return;
        }
        helper.succeed();
    }

    private static float breakSpeed(GameTestHelper helper, ServerPlayer player, BlockState state, float original) {
        BlockPos doorRel = new BlockPos(2, 2, 2);
        helper.setBlock(doorRel, state);
        BlockPos doorAbs = helper.absolutePos(doorRel);
        // In Fabric, the break speed boost is applied via mixin on Player.getDestroySpeed().
        return player.getDestroySpeed(state);
    }

    // -------------------------------------------------------------------------
    // MOB-GRUDGE (Fix1, vanilla-faithful self-refresh)
    // -------------------------------------------------------------------------

    static void mobGrudgeStickyRetaliation(GameTestHelper helper) {
        ServerPlayer player = GameTestPlayers.spawnZombieFakePlayer(helper, ZombieForm.NORMAL, ZombieSize.ADULT);
        helper.setBlock(new BlockPos(2, 1, 2), Blocks.STONE);
        Skeleton skeleton = helper.spawn(EntityTypes.SKELETON, new BlockPos(2, 2, 2));
        skeleton.setNoAi(true);
        skeleton.setInvulnerable(true);

        skeleton.setLastHurtByMob(player);
        if (clearedTarget(skeleton, player)) {
            helper.fail("a freshly-struck IGNORED monster (retaliating) must be ALLOWED to target the zombie player at t0");
            return;
        }

        helper.runAfterDelay(120L, () -> {
            if (!skeleton.isAlive()) {
                helper.fail("precondition: the struck skeleton must still be alive at +120t");
                return;
            }
            skeleton.setLastHurtByMob(null);
            if (clearedTarget(skeleton, player)) {
                helper.fail("while engaged (+120t, lastHurtByMob cleared), the self-refreshing grudge must keep the monster ALLOWED to target the player");
                return;
            }
            helper.runAfterDelay(120L, () -> {
                if (!skeleton.isAlive()) {
                    helper.fail("precondition: the struck skeleton must still be alive at +240t");
                    return;
                }
                skeleton.setLastHurtByMob(null);
                if (clearedTarget(skeleton, player)) {
                    helper.fail("while engaged (+240t), the self-refreshing grudge must keep the monster ALLOWED to target the player");
                    return;
                }
                helper.runAfterDelay(120L, () -> {
                    if (!skeleton.isAlive()) {
                        helper.fail("precondition: the struck skeleton must still be alive at +360t");
                        return;
                    }
                    skeleton.setLastHurtByMob(null);
                    if (clearedTarget(skeleton, player)) {
                        helper.fail("while engaged (+360t), the self-refreshing grudge must keep the monster ALLOWED to target the player");
                        return;
                    }
                    helper.runAfterDelay(300L, () -> {
                        if (!skeleton.isAlive()) {
                            helper.fail("precondition: the struck skeleton must still be alive at +660t");
                            return;
                        }
                        skeleton.setLastHurtByMob(null);
                        if (!clearedTarget(skeleton, player)) {
                            helper.fail("after the mob loses the player (no re-post for GRUDGE_TICKS), the forgiven grudge must let the IGNORED deny-list CLEAR the target again");
                            return;
                        }
                        helper.succeed();
                    });
                });
            });
        });
    }
}
