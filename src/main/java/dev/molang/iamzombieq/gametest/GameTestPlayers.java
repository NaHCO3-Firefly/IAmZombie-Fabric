package dev.molang.iamzombieq.gametest;

import java.util.UUID;

import com.mojang.authlib.GameProfile;

import dev.molang.iamzombieq.rules.core.ZombieForm;
import dev.molang.iamzombieq.rules.core.ZombieSize;
import dev.molang.iamzombieq.rules.core.ZombieState;
import dev.molang.iamzombieq.state.IAmZombieAttachments;
import dev.molang.iamzombieq.state.PlayerZombieData;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

/**
 * Helpers to spawn and configure a {@link FakePlayer} inside a running GameTest. A {@code FakePlayer} extends
 * {@code ServerPlayer}, so it passes every server-side handler gate (server thread, non-spectator, no connection
 * reads) that the mod's gameplay handlers apply.
 *
 * <p>The mod's facade ({@code IZombiePlayerAPI}) and the raw attachment are both FakePlayer-safe: the network
 * {@code syncData} step is a no-op for a connectionless player.
 */
final class GameTestPlayers {

    private GameTestPlayers() {
    }

    /**
     * A {@link ServerPlayer} placed at the test's local-origin {@code (1,2,1)} (one block above the 1x1x1 structure
     * floor) in SURVIVAL mode. A fresh {@link GameProfile} per call avoids any per-level
     * cache returning a stale player carrying state from a prior test.
     */
    static ServerPlayer spawnZombieFakePlayer(GameTestHelper helper, ZombieForm form, ZombieSize size) {
        ServerLevel level = helper.getLevel();
        GameProfile profile = new GameProfile(UUID.randomUUID(), "iamzombieq-test-" + UUID.randomUUID());
        MinecraftServer server = level.getServer();
        ServerPlayer player = new ServerPlayer(server, level, profile);

        // Survival, so isCreative()-gated branches in the handlers behave as for an ordinary zombie player.
        player.setGameMode(GameType.SURVIVAL);
        // Player may set itself invulnerable; clear it so sunlight fire / damage paths apply.
        player.setInvulnerable(false);

        BlockPos origin = helper.absolutePos(new BlockPos(1, 2, 1));
        Vec3 pos = Vec3.atBottomCenterOf(origin);
        player.snapTo(pos.x, pos.y, pos.z, 0.0F, 0.0F);

        // Establish zombie state directly on the attachment (player-safe; syncData no-ops for a connectionless
        // player). This is the same write the facade performs, used here so the starting state is unambiguous.
        PlayerZombieData data = PlayerZombieData.DEFAULT.withState(new ZombieState(form, size));
        IAmZombieAttachments.setPlayerZombie(player, data);

        return player;
    }

    static ZombieState stateOf(ServerPlayer player) {
        return IAmZombieAttachments.getPlayerZombie(player).state();
    }
}
