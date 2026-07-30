package dev.molang.iamzombieq.gameplay;

import dev.molang.iamzombieq.block.CoffinBlock;
import dev.molang.iamzombieq.rules.sleep.ZombieSleepRules;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;

/**
 * Coffin "true sleep" driver. Unlike the instant time-skip, this lets a zombie player actually sleep for a few seconds,
 * runs a per-dimension vote, and advances the dimension clock to NIGHT when enough are sleeping.
 *
 * <p>Server-thread only. The per-UUID nap map is a plain {@link HashMap} with no synchronization.
 */
public final class CoffinNapManager {
    private static final Map<UUID, Nap> NAPS = new HashMap<>();
    private static final long MAX_WAIT_TICKS = 200L;
    private static final long DEEP_SLEEP_TICKS = 100L;

    private CoffinNapManager() {
    }

    private static final class Nap {
        final BlockPos headPos;
        final long startTick;
        float lastHealth;

        Nap(BlockPos headPos, long startTick, float lastHealth) {
            this.headPos = headPos;
            this.startTick = startTick;
            this.lastHealth = lastHealth;
        }
    }

    public static boolean beginNap(ServerLevel level, ServerPlayer player, BlockPos headPos) {
        if (player.isSleeping() || player.isPassenger()) {
            return false;
        }
        BlockState headState = level.getBlockState(headPos);
        if (!(headState.getBlock() instanceof CoffinBlock)) {
            return false;
        }
        player.startSleeping(headPos);
        CoffinBlock.setCoffinRespawn(level, player, headPos);
        NAPS.put(player.getUUID(), new Nap(headPos, level.getGameTime(), player.getHealth()));
        return true;
    }

    public static boolean isNapping(UUID id) {
        return NAPS.containsKey(id);
    }

    /** Called from {@code ServerTickEvents.START_SERVER_TICK} in IAmZombieMod. */
    public static void onServerTick() {
        // TODO: Fabric port — iterate NAPS entries and tick sleeping players
    }

    /** Called per-player from a server tick or event handler. */
    public static void tickPlayer(ServerPlayer player) {
        Nap nap = NAPS.get(player.getUUID());
        if (nap == null) return;

        ServerLevel level = (ServerLevel) player.level();

        if (!player.isSleeping() || player.getSleepingPos().isEmpty()) {
            NAPS.remove(player.getUUID());
            return;
        }
        BlockState headState = level.getBlockState(nap.headPos);
        if (!(headState.getBlock() instanceof CoffinBlock)) {
            wake(player, "iamzombieq.message.coffin.disturbed");
            return;
        }

        if (player.getHealth() < nap.lastHealth) {
            wake(player, "iamzombieq.message.coffin.disturbed");
            return;
        }
        nap.lastHealth = player.getHealth();

        if ((level.getGameTime() - nap.startTick) % 20L == 0L && CoffinBlock.hasHostileNearby(level, player, nap.headPos)) {
            wake(player, "iamzombieq.message.coffin.disturbed");
            return;
        }

        int eligible = countEligibleZombies(level);
        int deep = countDeepCoffinSleepers(level);
        // TODO: MC 26.2 GameRules API changed — access PLAYERS_SLEEPING_PERCENTAGE through the new API
        int percentage = 50;
        if (!ZombieSleepRules.enoughCoffinSleepers(deep, eligible, percentage)) {
            if (player.isSleepingLongEnough() && level.getGameTime() - nap.startTick > DEEP_SLEEP_TICKS + MAX_WAIT_TICKS) {
                wake(player, "iamzombieq.message.coffin.not_enough");
                return;
            }
            int needed = ZombieSleepRules.coffinSleepersNeeded(eligible, percentage);
            player.sendSystemMessage(Component.translatable("iamzombieq.message.coffin.players_sleeping", deep, needed));
            return;
        }

        boolean skipped = advanceToNight(level);
        wakeAllInLevel(level, skipped);
    }

    private static int countEligibleZombies(ServerLevel level) {
        int n = 0;
        for (ServerPlayer p : level.players()) {
            if (!p.isSpectator()) n++;
        }
        return n;
    }

    private static int countDeepCoffinSleepers(ServerLevel level) {
        int deep = 0;
        for (UUID id : NAPS.keySet()) {
            ServerPlayer p = level.getServer().getPlayerList().getPlayer(id);
            if (p != null && p.level() == level && p.isSleepingLongEnough()) {
                deep++;
            }
        }
        return deep;
    }

    private static void wake(ServerPlayer player, String messageKey) {
        if (player.isSleeping()) player.stopSleeping();
        NAPS.remove(player.getUUID());
        player.sendSystemMessage(Component.translatable(messageKey));
    }

    private static void wakeAllInLevel(ServerLevel level, boolean skipped) {
        for (UUID id : new ArrayList<>(NAPS.keySet())) {
            ServerPlayer p = level.getServer().getPlayerList().getPlayer(id);
            if (p == null) { NAPS.remove(id); continue; }
            if (p.level() != level) continue;
            if (p.isSleeping()) p.stopSleeping();
            NAPS.remove(id);
            p.sendSystemMessage(Component.translatable(
                    skipped ? "iamzombieq.message.coffin.rested" : "iamzombieq.message.coffin.respawn_set_only"));
            level.playSound(null, p.blockPosition(), SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 0.8F, 0.6F);
        }
    }

    /** Advance this dimension's clock to NIGHT using MC 26.2 clock API. */
    private static boolean advanceToNight(ServerLevel level) {
        var clockHolder = level.dimensionType().defaultClock();
        if (clockHolder.isEmpty()) return false;
        var clock = clockHolder.get();
        var clockManager = (net.minecraft.world.clock.ServerClockManager) level.clockManager();
        boolean result = clockManager.moveToTimeMarker(clock, net.minecraft.world.clock.ClockTimeMarkers.NIGHT);
        if (level.isRaining()) {
            level.resetWeatherCycle();
        }
        return result;
    }

    /** Called from {@code ServerPlayConnectionEvents.DISCONNECT} in IAmZombieMod. */
    public static void onPlayerLoggedOut(UUID playerUuid) {
        NAPS.remove(playerUuid);
    }

    /** Called from {@code ServerLifecycleEvents.SERVER_STOPPED} in IAmZombieMod. */
    public static void onServerStopped() {
        NAPS.clear();
    }
}
