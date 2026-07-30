package dev.molang.iamzombieq.gameplay;
import dev.molang.iamzombieq.util.Difficulties;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import dev.molang.iamzombieq.IAmZombieConfig;
import dev.molang.iamzombieq.IAmZombieItems;
import dev.molang.iamzombieq.rules.difficulty.GameDifficulty;
import dev.molang.iamzombieq.rules.mount.MountKind;
import dev.molang.iamzombieq.rules.ZombieInfectionRules;
import dev.molang.iamzombieq.rules.mount.ZombieMountRules;
import dev.molang.iamzombieq.rules.core.ZombieSize;
import dev.molang.iamzombieq.state.IAmZombieAttachments;
import dev.molang.iamzombieq.state.PlayerZombieData;
import dev.molang.iamzombieq.state.SpiderMountData;
import dev.molang.iamzombieq.util.RideHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.animal.equine.SkeletonHorse;
import net.minecraft.world.entity.animal.equine.ZombieHorse;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.animal.nautilus.Nautilus;
import net.minecraft.world.entity.animal.nautilus.ZombieNautilus;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public final class ZombieMountEvents {
    // Keyed by horse UUID. Bounded LinkedHashMap with insertion-order eviction: entries are normally
    // removed when the horse dies to a zombie player, but horses that die from other sources (lava, fall,
    // etc.) would otherwise leak their snapshot until server stop. The cap prevents unbounded growth while
    // the eldest (least recently inserted) entry is dropped first; 256 pending dying-horse snapshots is far
    // more than can realistically be in flight, so eviction never disturbs a real in-progress conversion.
    private static final int PENDING_HORSE_HEALTH_RATIOS_CAP = 256;
    private static final Map<UUID, Float> PENDING_HORSE_HEALTH_RATIOS =
            new LinkedHashMap<>(16, 0.75F, false) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<UUID, Float> eldest) {
                    return size() > PENDING_HORSE_HEALTH_RATIOS_CAP;
                }
            };

    private ZombieMountEvents() {
    }

    public static InteractionResult onEntityInteract(Player player, Level level, InteractionHand hand, Entity target) {
        if (level.isClientSide() || !isZombiePlayer(player) || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }
        ItemStack stack = player.getItemInHand(hand);

        // Spider taming/interaction
        if (target instanceof Spider spider) {
            if (!stack.isEmpty() && ZombieMountRules.isSpiderTamingFood(spiderFoodId(stack))) {
                handleSpiderFood(serverPlayer, spider, stack, null);
                return InteractionResult.CONSUME;
            }
            return InteractionResult.PASS;
        }

        // Big zombie mounting
        if (target instanceof Zombie zombie && !zombie.isBaby()) {
            // TODO: Fabric port - create proper handler
            if (isRideableBigZombie(zombie) && !isBigZombieProvokedBy(zombie, player)) {
                if (zombie.getFirstPassenger() == player) {
                    player.stopRiding();
                } else {
                    player.startRiding(zombie);
                }
            }
            return InteractionResult.CONSUME;
        }

        // Chicken mounting (baby only)
        if (target instanceof Chicken chicken && dataOf(serverPlayer).state().size() == ZombieSize.BABY) {
            // TODO: Fabric port - create proper handler
            if (chicken.getFirstPassenger() == player) {
                player.stopRiding();
            } else {
                player.startRiding(chicken);
            }
            return InteractionResult.CONSUME;
        }

        // Horse feeding → zombie horse conversion tracking
        if (isNormalHorse(target) && !stack.isEmpty() && isZombieHorseFood(stack)) {
            // TODO: Fabric port - create proper handler
            // FALLBACK: stub only - return PASS to allow continued interaction
            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }

    public static void onLivingDeath(ServerPlayer player, Entity victim) {
        if (victim instanceof Horse horse && isNormalHorse(horse)) {
            Float healthRatio = PENDING_HORSE_HEALTH_RATIOS.remove(horse.getUUID());
            convertHorseToZombieHorse((ServerLevel) horse.level(), horse, player, healthRatio);
        }
        if (victim instanceof Nautilus nautilus) {
            convertNautilusToZombieNautilus((ServerLevel) nautilus.level(), nautilus, player);
        }
    }

    public static void onServerStopped() {
        PENDING_HORSE_HEALTH_RATIOS.clear();
    }

    private static boolean isZombiePlayer(Player player) {
        // N6: creative players follow zombie mount rules too (flight/invuln stay inherent). Only spectators are excluded.
        return !player.isSpectator();
    }

    private static boolean isZombieHorseFood(ItemStack stack) {
        return stack.is(Items.ROTTEN_FLESH) || stack.is(IAmZombieItems.SUPER_ROTTEN_FLESH);
    }

    // A vanilla (living) horse: the blocked mount kind. ZombieHorse/SkeletonHorse extend AbstractHorse (siblings of
    // Horse, not subclasses), so instanceof Horse already excludes them; the explicit !ZombieHorse/!SkeletonHorse
    // guards are defensive, keeping these undead, zombie-rideable mounts out of the "normal horse" check.
    private static boolean isNormalHorse(Entity target) {
        return target instanceof Horse && !(target instanceof ZombieHorse) && !(target instanceof SkeletonHorse);
    }

    private static PlayerZombieData dataOf(ServerPlayer player) {
        return dev.molang.iamzombieq.platform.Services.ATTACHMENT.get(
                player, IAmZombieAttachments.PLAYER_ZOMBIE_KEY, PlayerZombieData.DEFAULT);
    }

    private static InteractionResult handleSpiderInteract(ServerPlayer player, Spider spider) {
        // Already mounted? Dismount
        if (spider.getFirstPassenger() == player) {
            player.stopRiding();
            return InteractionResult.CONSUME;
        }
        // Mount tamed spider
        if (spiderOwnedBy(spider, player)) {
            player.startRiding(spider);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    private static void handleSpiderFood(ServerPlayer player, Spider spider, ItemStack stack, SpiderMountData data) {
        if (data == null) {
            data = dev.molang.iamzombieq.platform.Services.ATTACHMENT.get(
                    spider, IAmZombieAttachments.SPIDER_MOUNT_KEY, SpiderMountData.DEFAULT);
        }
        if (data.isOwnedBy(player.getUUID())) {
            // Already owned: heal
            spider.heal(ZombieMountRules.spiderHealAmount(spiderFoodId(stack)));
            stack.consume(1, player);
            return;
        }
        // Tame progress
        int progress = data.tameProgress() + ZombieMountRules.spiderTameProgressFor(spiderFoodId(stack));
        boolean tamed = ZombieMountRules.spiderIsTamed(progress);
        var newData = new SpiderMountData(
                tamed ? player.getUUID().toString() : "",
                tamed ? 0 : progress);
        dev.molang.iamzombieq.platform.Services.ATTACHMENT.set(
                spider, IAmZombieAttachments.SPIDER_MOUNT_KEY, newData);
        if (tamed) {
            player.sendSystemMessage(Component.translatable("iamzombieq.message.spider.tamed"));
        }
        stack.consume(1, player);
    }

    private static InteractionResult handleBigZombieInteract(ServerPlayer player, Zombie zombie) {
        if (!isRideableBigZombie(zombie) || isBigZombieProvokedBy(zombie, player)) {
            return InteractionResult.PASS;
        }
        // Already riding? Dismount
        if (zombie.getFirstPassenger() == player) {
            player.stopRiding();
            return InteractionResult.CONSUME;
        }
        // Mount
        player.startRiding(zombie);
        return InteractionResult.CONSUME;
    }

    private static InteractionResult handleChickenInteract(ServerPlayer player, Chicken chicken) {
        if (!isBabyZombiePlayer(player)) {
            return InteractionResult.PASS;
        }
        if (chicken.getFirstPassenger() == player) {
            player.stopRiding();
        } else {
            player.startRiding(chicken);
        }
        return InteractionResult.CONSUME;
    }

    private static void handleHorseFeed(ServerPlayer player, Horse horse, ItemStack stack) {
        // Track health ratio for zombie conversion on death
        PENDING_HORSE_HEALTH_RATIOS.put(horse.getUUID(), preDamageHorseHealthRatio(horse));
        horse.heal(4.0F);
        stack.consume(1, player);
    }

    private static void convertHorseToZombieHorse(Horse horse, ServerPlayer player) {
        Float healthRatio = PENDING_HORSE_HEALTH_RATIOS.remove(horse.getUUID());
        convertHorseToZombieHorse((ServerLevel) horse.level(), horse, player, healthRatio);
    }

    private static boolean isSpiderFood(ItemStack stack) {
        return ZombieMountRules.isSpiderTamingFood(spiderFoodId(stack));
    }

    private static String spiderFoodId(ItemStack stack) {
        if (stack.is(Items.ROTTEN_FLESH)) {
            return "minecraft:rotten_flesh";
        }
        if (stack.is(Items.SPIDER_EYE)) {
            return "minecraft:spider_eye";
        }
        if (stack.is(IAmZombieItems.SUPER_ROTTEN_FLESH)) {
            return "iamzombieq:super_rotten_flesh";
        }
        return "";
    }

    private static GameDifficulty gameDifficulty(Difficulty difficulty) {
        return Difficulties.toGameDifficulty(difficulty);
    }

    private static boolean convertHorseToZombieHorse(ServerLevel level, Horse horse, Player owner, Float pendingHorseHealthRatio) {
        ZombieHorse zombieHorse = EntityTypes.ZOMBIE_HORSE.create(level, EntitySpawnReason.CONVERSION);
        if (zombieHorse == null) {
            return false;
        }

        zombieHorse.snapTo(horse.getX(), horse.getY(), horse.getZ(), horse.getYRot(), horse.getXRot());
        zombieHorse.finalizeSpawn(level, level.getCurrentDifficultyAt(horse.blockPosition()), EntitySpawnReason.CONVERSION, null);
        zombieHorse.setTamed(true);
        zombieHorse.setOwner(owner);
        zombieHorse.setPersistenceRequired();
        copyHorseStateToZombieHorse(horse, zombieHorse, pendingHorseHealthRatio);

        level.addFreshEntity(zombieHorse);
        horse.discard();
        level.levelEvent(null, 1026, horse.blockPosition(), 0);
        return true;
    }

    private static void copyHorseStateToZombieHorse(Horse horse, ZombieHorse zombieHorse, Float pendingHorseHealthRatio) {
        zombieHorse.setItemSlot(EquipmentSlot.SADDLE, horse.getItemBySlot(EquipmentSlot.SADDLE).copy());
        zombieHorse.setItemSlot(EquipmentSlot.BODY, horse.getItemBySlot(EquipmentSlot.BODY).copy());
        zombieHorse.setAge(horse.getAge());

        float healthRatio = pendingHorseHealthRatio != null ? pendingHorseHealthRatio : horse.getHealth() / horse.getMaxHealth();
        zombieHorse.setHealth(Math.max(1.0F, zombieHorse.getMaxHealth() * healthRatio));
        if (horse.hasCustomName()) {
            zombieHorse.setCustomName(horse.getCustomName());
            zombieHorse.setCustomNameVisible(horse.isCustomNameVisible());
        }
    }

    private static float preDamageHorseHealthRatio(Horse horse) {
        return Math.max(0.0F, horse.getHealth() / horse.getMaxHealth());
    }

    private static void handleNautilusDeath(Object event, ServerLevel level, Nautilus nautilus) {
    }

    private static boolean convertNautilusToZombieNautilus(ServerLevel level, Nautilus nautilus, Player owner) {
        ZombieNautilus zombieNautilus = EntityTypes.ZOMBIE_NAUTILUS.create(level, EntitySpawnReason.CONVERSION);
        if (zombieNautilus == null) {
            return false;
        }

        zombieNautilus.snapTo(nautilus.getX(), nautilus.getY(), nautilus.getZ(), nautilus.getYRot(), nautilus.getXRot());
        zombieNautilus.finalizeSpawn(level, level.getCurrentDifficultyAt(nautilus.blockPosition()), EntitySpawnReason.CONVERSION, null);
        zombieNautilus.setTame(true, true);
        zombieNautilus.setOwner(owner);
        zombieNautilus.setPersistenceRequired();
        zombieNautilus.setHealth(zombieNautilus.getMaxHealth());
        zombieNautilus.setItemSlot(EquipmentSlot.SADDLE, new ItemStack(Items.SADDLE));
        if (nautilus.hasCustomName()) {
            zombieNautilus.setCustomName(nautilus.getCustomName());
            zombieNautilus.setCustomNameVisible(nautilus.isCustomNameVisible());
        }

        level.addFreshEntity(zombieNautilus);
        nautilus.discard();
        level.levelEvent(null, 1026, nautilus.blockPosition(), 0);
        return true;
    }

    private static boolean isBabyZombiePlayer(Player player) {
        return isZombiePlayer(player) && zombieSize(player) == ZombieSize.BABY;
    }

    private static ZombieSize zombieSize(Player player) { // TODO: Fabric port
        return ZombieSize.ADULT;
    }

    private static MountKind mountKindFor(Entity mounted) {
        if (mounted instanceof Spider) {
            return MountKind.SPIDER;
        }
        if (mounted instanceof ZombieHorse) {
            return MountKind.ZOMBIE_HORSE;
        }
        if (mounted instanceof SkeletonHorse) {
            return MountKind.SKELETON_HORSE;
        }
        if (mounted instanceof Horse) {
            return MountKind.NORMAL_HORSE;
        }
        if (mounted instanceof Chicken) {
            return MountKind.CHICKEN;
        }
        if (mounted instanceof ZombieNautilus) {
            return MountKind.ZOMBIE_NAUTILUS;
        }
        if (mounted instanceof Strider) {
            return MountKind.STRIDER;
        }
        if (mounted instanceof Zombie zombie) {
            return isRideableBigZombie(zombie) ? MountKind.BIG_ZOMBIE : MountKind.OTHER;
        }
        return MountKind.OTHER;
    }

    private static boolean spiderOwnedBy(Entity mounted, Player player) {
        if (!(mounted instanceof Spider spider)) return false;
        var data = dev.molang.iamzombieq.platform.Services.ATTACHMENT.get(
                spider, IAmZombieAttachments.SPIDER_MOUNT_KEY, SpiderMountData.DEFAULT);
        return data.isOwnedBy(player.getUUID());
    }

    private static boolean isRideableBigZombie(Zombie zombie) {
        // Delegate to the shared classifier so the events layer and the MountCapability/mixin layer agree on
        // exactly which zombies are BIG_ZOMBIE mounts (was a byte-identical copy of RideHelper's predicate).
        return RideHelper.isRideableBigZombie(zombie);
    }

    private static boolean isMountedBigZombieRider(Zombie zombie, Player target) {
        return isRideableBigZombie(zombie) && zombie.getFirstPassenger() == target;
    }

    /**
     * A big zombie the player has provoked is no longer mountable ("if I hit it, I can't ride it"): it is either
     * already hunting the player or the player recently struck it (so it is retaliating). Both signals are
     * transient combat memory, so a zombie the player has left alone becomes rideable again once it calms down.
     */
    private static boolean isBigZombieProvokedBy(Zombie zombie, Player player) {
        return zombie.getTarget() == player || zombie.getLastHurtByMob() == player;
    }

    private static void maybeAutoTargetForMountedBigZombie(ServerLevel level, Zombie zombie, Player rider) {
        if (zombie.tickCount % 10 != 0) {
            return;
        }
        LivingEntity current = zombie.getTarget();
        boolean haveValidTarget = current != null
                && current.isAlive()
                && ZombieMountRules.bigZombieShouldAutoAttack(Math.sqrt(zombie.distanceToSqr(current)));
        if (!haveValidTarget) {
            current = selectMountedBigZombieTarget(level, zombie, rider);
            if (current != null) {
                zombie.setTarget(current);
            }
        }

        // While a player controls the mount, the zombie's own melee AI goal is suppressed, so setting a target
        // is not enough -- it would never swing. Actively swing + hurt when a valid target is in melee reach,
        // throttled to ~once per second so it is a normal attack cadence rather than a per-tick blender.
        if (current != null
                && current.isAlive()
                && current != rider
                && zombie.tickCount % 20 == 0
                && zombie.isWithinMeleeAttackRange(current)) {
            zombie.swing(InteractionHand.MAIN_HAND);
            zombie.doHurtTarget(level, current);
        }
    }

    // How long (ticks) the mount remembers who the rider attacked / was attacked by. ~5s, like vanilla aggro memory.
    private static final int RIDER_COMBAT_MEMORY_TICKS = 100;

    /**
     * Target priority for a ridden big zombie (design): (1) whoever the rider just attacked, (2) whoever just
     * attacked the rider, then (3) the nearest creature zombies naturally aggro (villager > iron golem > other
     * monster). (1)/(2) come from the rider's own recent combat so the mount fights alongside the player; (3) is
     * the proximity scan.
     */
    private static LivingEntity selectMountedBigZombieTarget(ServerLevel level, Zombie zombie, Player rider) {
        LivingEntity riderTarget = rider.getLastHurtMob();
        if (isMountAttackable(zombie, rider, riderTarget)
                && rider.tickCount - rider.getLastHurtMobTimestamp() <= RIDER_COMBAT_MEMORY_TICKS) {
            return riderTarget;
        }
        LivingEntity riderAttacker = rider.getLastHurtByMob();
        if (isMountAttackable(zombie, rider, riderAttacker)
                && rider.tickCount - rider.getLastHurtByMobTimestamp() <= RIDER_COMBAT_MEMORY_TICKS) {
            return riderAttacker;
        }
        return findMountedBigZombieTarget(level, zombie, rider);
    }

    /** A target the ridden mount may attack: alive, not the rider, not the mount itself, not the rider's own
     *  tamed spider. (Rider-driven targets intentionally allow fellow zombies -- if the rider hits one, the mount
     *  helps -- unlike the proximity scan, which excludes fellow zombies.) */
    private static boolean isMountAttackable(Zombie zombie, Player rider, LivingEntity candidate) { // TODO: Fabric port
        return false;
    }

    private static LivingEntity findMountedBigZombieTarget(ServerLevel level, Zombie zombie, Player rider) { // TODO: Fabric port
        return null;
    }
}
