package dev.molang.iamzombieq.gameplay;
import dev.molang.iamzombieq.util.Difficulties;

import dev.molang.iamzombieq.IAmZombieConfig;
import dev.molang.iamzombieq.rules.difficulty.GameDifficulty;
import dev.molang.iamzombieq.rules.ZombieInfectionRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.monster.zombie.ZombieVillager;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;

public final class ZombieInfectionEvents {
    private ZombieInfectionEvents() {
    }

    /**
     * Called when a living entity dies. If the killer is a zombie player, check for infection opportunities.
     */
    public static void onLivingDeath(LivingEntity entity, DamageSource source) {
        if (entity.level().isClientSide()) {
            return;
        }
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!isZombiePlayer(player)) {
            return;
        }
        if (!(entity.level() instanceof ServerLevel level)) {
            return;
        }

        if (entity instanceof Villager villager) {
            tryInfectVillager(level, villager, player);
        } else if (entity instanceof Pig || entity instanceof AbstractPiglin) {
            tryInfectIntoZombifiedPiglin(level, (Mob) entity, player);
        }
    }

    private static void tryInfectVillager(ServerLevel level, Villager villager, Player player) {
        GameDifficulty difficulty = gameDifficulty(level.getDifficulty());
        double roll = level.getRandom().nextDouble();
        if (ZombieInfectionRules.shouldInfect(difficulty, roll)) {
            if (convertVillagerToZombieVillager(level, villager, player)) {
                awardInfection(player);
            }
        }
    }

    private static void tryInfectIntoZombifiedPiglin(ServerLevel level, Mob victim, Player player) {
        boolean isPig = victim instanceof Pig;
        boolean isPiglin = victim instanceof AbstractPiglin;
        if (!ZombieInfectionRules.canInfectIntoZombifiedPiglin(isPig, isPiglin)) {
            return;
        }
        GameDifficulty difficulty = gameDifficulty(level.getDifficulty());
        double roll = level.getRandom().nextDouble();
        if (ZombieInfectionRules.shouldInfect(difficulty, roll)) {
            if (convertToZombifiedPiglin(level, victim, player)) {
                awardInfection(player);
            }
        }
    }

    private static void awardInfection(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            IAmZombieAdvancements.award(serverPlayer, IAmZombieAdvancements.INFECTION);
        }
    }

    private static boolean isZombiePlayer(Player player) {
        return !player.isSpectator();
    }

    private static GameDifficulty gameDifficulty(Difficulty difficulty) {
        return Difficulties.toGameDifficulty(difficulty);
    }

    private static boolean convertVillagerToZombieVillager(ServerLevel level, Villager villager, Player player) {
        ZombieVillager zombieVillager = villager.convertTo(
                EntityTypes.ZOMBIE_VILLAGER,
                ConversionParams.single(villager, true, true),
                zombie -> {
                    zombie.setVillagerDataFinalized(villager.getVillagerDataFinalized());
                    zombie.finalizeSpawn(
                            level,
                            level.getCurrentDifficultyAt(zombie.blockPosition()),
                            EntitySpawnReason.CONVERSION,
                            new Zombie.ZombieGroupData(false, true)
                    );
                    zombie.setVillagerData(villager.getVillagerData());
                    zombie.setGossips(villager.getGossips().copy());
                    zombie.setTradeOffers(villager.getOffers().copy());
                    zombie.setVillagerXp(villager.getVillagerXp());
                    if (!villager.isSilent()) {
                        level.levelEvent(null, 1026, villager.blockPosition(), 0);
                    }
                }
        );
        if (zombieVillager != null) {
            ZombieMobTargetingEvents.recordConversionGrace(zombieVillager, player);
        }
        return zombieVillager != null;
    }

    private static boolean convertToZombifiedPiglin(ServerLevel level, Mob victim, Player player) {
        ZombifiedPiglin zombifiedPiglin = victim.convertTo(
                EntityTypes.ZOMBIFIED_PIGLIN,
                ConversionParams.single(victim, false, true),
                piglin -> {
                    piglin.populateDefaultEquipmentSlots(victim.getRandom(), level.getCurrentDifficultyAt(piglin.blockPosition()));
                    piglin.finalizeSpawn(
                            level,
                            level.getCurrentDifficultyAt(piglin.blockPosition()),
                            EntitySpawnReason.CONVERSION,
                            null
                    );
                    piglin.setPersistenceRequired();
                    if (!victim.isSilent()) {
                        level.levelEvent(null, 1026, victim.blockPosition(), 0);
                    }
                }
        );
        if (zombifiedPiglin != null) {
            ZombieMobTargetingEvents.recordConversionGrace(zombifiedPiglin, player);
        }
        return zombifiedPiglin != null;
    }
}
