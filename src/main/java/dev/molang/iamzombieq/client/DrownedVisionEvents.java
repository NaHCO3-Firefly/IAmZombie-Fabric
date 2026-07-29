package dev.molang.iamzombieq.client;

import dev.molang.iamzombieq.rules.core.ZombieForm;
import dev.molang.iamzombieq.state.IAmZombieAttachments;
import dev.molang.iamzombieq.state.PlayerZombieData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

/**
 * Client-only handler for G3 (drowned wet-state clear vision).
 * TODO: Register via Fabric fog event callback.
 */
public final class DrownedVisionEvents {
    private static final boolean CLEAR_DROWNED_WATER_FOG = true;
    private static final float CLEAR_FAR_PLANE = 1024.0F;

    private DrownedVisionEvents() {
    }

    /** Called from Fabric fog event. Clears underwater fog for drowned-form players. */
    public static void applyDrownedClearFog(float[] nearFar, LocalPlayer player) {
        if (!CLEAR_DROWNED_WATER_FOG || player == null) return;

        PlayerZombieData data = dev.molang.iamzombieq.platform.Services.ATTACHMENT.get(
                player, IAmZombieAttachments.PLAYER_ZOMBIE_KEY, PlayerZombieData.DEFAULT);
        if (data == null || data.state().form() != ZombieForm.DROWNED) return;
        if (!player.isInWaterOrRain()) return;

        nearFar[0] = 0.0F;
        nearFar[1] = CLEAR_FAR_PLANE;
    }
}
