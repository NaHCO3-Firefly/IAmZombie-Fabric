package dev.molang.iamzombieq.gametest;

import java.util.UUID;

import dev.molang.iamzombieq.IAmZombieItems;
import dev.molang.iamzombieq.rules.core.ZombieForm;
import dev.molang.iamzombieq.rules.core.ZombieSize;
import dev.molang.iamzombieq.rules.mount.ZombieMountRules;
import dev.molang.iamzombieq.state.IAmZombieAttachments;
import dev.molang.iamzombieq.state.SpiderMountData;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.animal.equine.SkeletonHorse;
import net.minecraft.world.entity.animal.equine.ZombieHorse;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * GameTest bodies for the {@code iamzombieq} MOUNT GameTests (catalog §2.12 MNT).
 */
final class IAmZombieMountGameTestBodies {

    private IAmZombieMountGameTestBodies() {
    }

    static void spiderTameProgressRottenFlesh(GameTestHelper helper) {
        ServerPlayer player = GameTestPlayers.spawnZombieFakePlayer(helper, ZombieForm.NORMAL, ZombieSize.ADULT);
        Spider spider = helper.spawn(EntityTypes.SPIDER, new BlockPos(1, 2, 1));

        ItemStack food = new ItemStack(Items.ROTTEN_FLESH, 2);
        player.setItemInHand(InteractionHand.MAIN_HAND, food);
        interact(player, spider);

        SpiderMountData data = IAmZombieAttachments.getSpiderMount(spider);
        if (data.tameProgress() != ZombieMountRules.SPIDER_TAME_PROGRESS_ROTTEN_FLESH) {
            helper.fail("one rotten-flesh feed should add +" + ZombieMountRules.SPIDER_TAME_PROGRESS_ROTTEN_FLESH
                    + " taming progress, was " + data.tameProgress());
            return;
        }
        if (data.hasOwner()) {
            helper.fail("a single rotten-flesh feed must NOT instantly tame (taming is no longer instant)");
            return;
        }
        if (player.getMainHandItem().getCount() != 1) {
            helper.fail("the taming feed should consume exactly one rotten flesh (2 -> 1)");
            return;
        }
        helper.succeed();
    }

    static void spiderTameProgressSuperRottenFlesh(GameTestHelper helper) {
        ServerPlayer player = GameTestPlayers.spawnZombieFakePlayer(helper, ZombieForm.NORMAL, ZombieSize.ADULT);
        Spider spider = helper.spawn(EntityTypes.SPIDER, new BlockPos(1, 2, 1));

        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(IAmZombieItems.SUPER_ROTTEN_FLESH));
        interact(player, spider);

        SpiderMountData data = IAmZombieAttachments.getSpiderMount(spider);
        if (data.tameProgress() != ZombieMountRules.SPIDER_TAME_PROGRESS_SUPER_ROTTEN_FLESH) {
            helper.fail("one super_rotten_flesh feed should add +" + ZombieMountRules.SPIDER_TAME_PROGRESS_SUPER_ROTTEN_FLESH
                    + " taming progress, was " + data.tameProgress());
            return;
        }
        if (data.hasOwner()) {
            helper.fail("even one super_rotten_flesh feed (the strongest) must not reach the tame threshold");
            return;
        }
        helper.succeed();
    }

    static void spiderTameReachesThresholdBindsOwner(GameTestHelper helper) {
        ServerPlayer player = GameTestPlayers.spawnZombieFakePlayer(helper, ZombieForm.NORMAL, ZombieSize.ADULT);
        Spider spider = helper.spawn(EntityTypes.SPIDER, new BlockPos(1, 2, 1));
        IAmZombieAttachments.setSpiderMount(spider, SpiderMountData.DEFAULT.withProgress(80));

        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(IAmZombieItems.SUPER_ROTTEN_FLESH));
        interact(player, spider);

        SpiderMountData data = IAmZombieAttachments.getSpiderMount(spider);
        if (!data.isOwnedBy(player.getUUID())) {
            helper.fail("crossing the taming threshold should bind the spider's owner to the player");
            return;
        }
        if (data.tameProgress() != ZombieMountRules.SPIDER_TAME_PROGRESS_THRESHOLD) {
            helper.fail("a tamed spider's progress should be clamped to the threshold (" + data.tameProgress() + ")");
            return;
        }
        helper.succeed();
    }

    static void ownedSpiderHealsWhenFedSuperRottenFlesh(GameTestHelper helper) {
        ServerPlayer player = GameTestPlayers.spawnZombieFakePlayer(helper, ZombieForm.NORMAL, ZombieSize.ADULT);
        Spider spider = helper.spawn(EntityTypes.SPIDER, new BlockPos(1, 2, 1));
        IAmZombieAttachments.setSpiderMount(spider, SpiderMountData.ownedBy(player.getUUID()));

        float max = spider.getMaxHealth();
        float start = Math.max(1.0F, max - 12.0F);
        spider.setHealth(start);

        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(IAmZombieItems.SUPER_ROTTEN_FLESH, 2));
        interact(player, spider);

        float expected = Math.min(max, start + ZombieMountRules.spiderHealAmount("iamzombieq:super_rotten_flesh"));
        if (Math.abs(spider.getHealth() - expected) > 0.001F) {
            helper.fail("feeding super_rotten_flesh to an owned damaged spider should heal +10.0 (to "
                    + expected + "), was " + spider.getHealth());
            return;
        }
        if (player.getMainHandItem().getCount() != 1) {
            helper.fail("a successful spider heal should consume one super_rotten_flesh (2 -> 1)");
            return;
        }
        helper.succeed();
    }

    static void wildZombieHorseAutoTamesOnInteract(GameTestHelper helper) {
        ServerPlayer player = GameTestPlayers.spawnZombieFakePlayer(helper, ZombieForm.NORMAL, ZombieSize.ADULT);
        ZombieHorse horse = helper.spawn(EntityTypes.ZOMBIE_HORSE, new BlockPos(1, 2, 1));
        if (horse.isTamed()) {
            helper.fail("precondition: a freshly spawned zombie horse should start untamed");
            return;
        }

        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        interact(player, horse);

        if (!horse.isTamed()) {
            helper.fail("a zombie player's empty-hand interact should auto-tame a wild zombie horse");
            return;
        }
        UUID ownerUuid = horse.getOwnerReference() == null ? null : horse.getOwnerReference().getUUID();
        if (!player.getUUID().equals(ownerUuid)) {
            helper.fail("the auto-tamed zombie horse should be owned by the interacting zombie player");
            return;
        }
        helper.succeed();
    }

    static void wildSkeletonHorseAutoTamesOnInteract(GameTestHelper helper) {
        ServerPlayer player = GameTestPlayers.spawnZombieFakePlayer(helper, ZombieForm.NORMAL, ZombieSize.ADULT);
        SkeletonHorse horse = helper.spawn(EntityTypes.SKELETON_HORSE, new BlockPos(1, 2, 1));
        if (horse.isTamed()) {
            helper.fail("precondition: a freshly spawned skeleton horse should start untamed");
            return;
        }

        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        interact(player, horse);

        if (!horse.isTamed()) {
            helper.fail("a zombie player's empty-hand interact should auto-tame a wild skeleton horse");
            return;
        }
        helper.succeed();
    }

    static void damagedZombieHorseHealsWhenFed(GameTestHelper helper) {
        ServerPlayer player = GameTestPlayers.spawnZombieFakePlayer(helper, ZombieForm.NORMAL, ZombieSize.ADULT);
        ZombieHorse horse = helper.spawn(EntityTypes.ZOMBIE_HORSE, new BlockPos(1, 2, 1));

        float max = horse.getMaxHealth();
        float start = Math.max(1.0F, max - 12.0F);
        horse.setHealth(start);

        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(IAmZombieItems.SUPER_ROTTEN_FLESH, 2));
        interact(player, horse);

        float expected = Math.min(max, start + 10.0F);
        if (Math.abs(horse.getHealth() - expected) > 0.001F) {
            helper.fail("feeding super_rotten_flesh to a damaged zombie horse should heal +10.0 (to "
                    + expected + "), was " + horse.getHealth());
            return;
        }
        if (player.getMainHandItem().getCount() != 1) {
            helper.fail("a successful zombie-horse heal should consume one super_rotten_flesh (2 -> 1)");
            return;
        }
        helper.succeed();
    }

    static void fullHealthZombieHorseFeedKeepsFoodAndCancels(GameTestHelper helper) {
        ServerPlayer player = GameTestPlayers.spawnZombieFakePlayer(helper, ZombieForm.NORMAL, ZombieSize.ADULT);
        ZombieHorse horse = helper.spawn(EntityTypes.ZOMBIE_HORSE, new BlockPos(1, 2, 1));
        horse.setHealth(horse.getMaxHealth());

        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.ROTTEN_FLESH, 3));
        interact(player, horse);

        if (player.getMainHandItem().getCount() != 3) {
            helper.fail("feeding a full-health zombie horse must NOT consume the food (count stayed at 3)");
            return;
        }
        if (Math.abs(horse.getHealth() - horse.getMaxHealth()) > 0.001F) {
            helper.fail("a full-health zombie horse should remain at max health after a refused feed");
            return;
        }
        helper.succeed();
    }

    static void normalHorseInteractIsRefusedAndCancelled(GameTestHelper helper) {
        ServerPlayer player = GameTestPlayers.spawnZombieFakePlayer(helper, ZombieForm.NORMAL, ZombieSize.ADULT);
        Horse horse = helper.spawn(EntityTypes.HORSE, new BlockPos(1, 2, 1));
        boolean wasTamed = horse.isTamed();

        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        interact(player, horse);

        if (horse.isTamed() != wasTamed) {
            helper.fail("a refused normal horse must NOT be tamed/owned by the zombie player");
            return;
        }
        if (player.isPassenger()) {
            helper.fail("a zombie player must NOT end up riding a refused normal horse");
            return;
        }
        helper.succeed();
    }

    static void untamedSpiderRideRefused(GameTestHelper helper) {
        ServerPlayer player = GameTestPlayers.spawnZombieFakePlayer(helper, ZombieForm.NORMAL, ZombieSize.ADULT);
        Spider spider = helper.spawn(EntityTypes.SPIDER, new BlockPos(1, 2, 1));

        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        interact(player, spider);

        if (player.isPassenger()) {
            helper.fail("a zombie player must not be able to ride an UNTAMED spider");
            return;
        }
        helper.succeed();
    }

    static void adultCannotRideChicken(GameTestHelper helper) {
        ServerPlayer player = GameTestPlayers.spawnZombieFakePlayer(helper, ZombieForm.NORMAL, ZombieSize.ADULT);
        Chicken chicken = helper.spawn(EntityTypes.CHICKEN, new BlockPos(1, 2, 1));

        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        interact(player, chicken);

        if (player.isPassenger()) {
            helper.fail("an ADULT zombie player must NOT be able to ride a chicken (baby-only mount)");
            return;
        }
        helper.succeed();
    }

    private static void interact(ServerPlayer player, net.minecraft.world.entity.Entity target) {
        player.interactOn(target, InteractionHand.MAIN_HAND, target.position());
    }
}
