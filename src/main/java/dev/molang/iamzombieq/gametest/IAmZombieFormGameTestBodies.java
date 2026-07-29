package dev.molang.iamzombieq.gametest;

import dev.molang.iamzombieq.rules.core.ZombieForm;
import dev.molang.iamzombieq.rules.core.ZombieSize;
import dev.molang.iamzombieq.state.IAmZombieAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.Giant;
import net.minecraft.world.level.GameType;

/**
 * GameTest bodies for the FORM (§2.1) and ATTR (§2.2) acceptance rows of {@code iamzombieq}.
 */
final class IAmZombieFormGameTestBodies {

    private IAmZombieFormGameTestBodies() {
    }

    /**
     * FORM-001: a freshly-attached zombie player carries the default NORMAL/ADULT state.
     */
    static void formDefaultState(GameTestHelper helper) {
        ServerPlayer player = GameTestPlayers.spawnZombieFakePlayer(helper, ZombieForm.NORMAL, ZombieSize.ADULT);
        if (GameTestPlayers.stateOf(player).form() != ZombieForm.NORMAL
                || GameTestPlayers.stateOf(player).size() != ZombieSize.ADULT) {
            helper.fail("a freshly-attached zombie player must default to NORMAL/ADULT");
            return;
        }
        helper.succeed();
    }

    /**
     * FORM-007: a CREATIVE-mode zombie player that kills a vanilla giant transforms into the GIANT form.
     */
    static void formCreativeGiantKillBecomesGiant(GameTestHelper helper) {
        ServerPlayer player = GameTestPlayers.spawnZombieFakePlayer(helper, ZombieForm.NORMAL, ZombieSize.ADULT);
        player.setGameMode(GameType.CREATIVE);
        player.setInvulnerable(false);
        ServerLevel level = helper.getLevel();

        Giant giant = helper.spawn(EntityTypes.GIANT, new BlockPos(1, 2, 1));
        DamageSource killedByPlayer = level.damageSources().playerAttack(player);
        giant.hurtServer(level, killedByPlayer, Float.MAX_VALUE);

        if (GameTestPlayers.stateOf(player).form() != ZombieForm.GIANT) {
            helper.fail("a creative player killing a vanilla giant must transform into the GIANT form");
            return;
        }
        if (player.getMaxHealth() != 100.0F) {
            helper.fail("GIANT form max health should be 100 after the transform, was " + player.getMaxHealth());
            return;
        }
        if (player.getHealth() != player.getMaxHealth()) {
            helper.fail("a giant-kill transform must respawn the player at full health");
            return;
        }
        helper.succeed();
    }
}
