package dev.molang.iamzombieq.mixin.client;

import dev.molang.iamzombieq.rules.ZombieRenderRules;
import dev.molang.iamzombieq.IAmZombieClientConfig;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Modifies the player model's arm pose to zombie stance (arms forward)
 * when the zombie skin mode is active.
 */
@Mixin(PlayerModel.class)
abstract class PlayerModelMixin {

    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)V",
            at = @At("TAIL"))
    private void iamzombieq$zombieArmPose(AvatarRenderState state, CallbackInfo ci) {
        if (!ZombieRenderRules.usesMonsterTexture(IAmZombieClientConfig.PLAYER_SKIN_MODE.get())) {
            return;
        }

        PlayerModel self = (PlayerModel) (Object) this;
        // Set arms to zombie position: rotate forward (pitch ~ -90 = reaching forward)
        // Vanilla idle: arms hang down (rightArm.xRot ~ 0)
        // Zombie attack: arms reach forward (rightArm.xRot ~ -PI/2)
        self.rightArm.xRot = (float) (-Math.PI * 0.5);
        self.leftArm.xRot = (float) (-Math.PI * 0.5);
        // Slight inward rotation for zombie arms
        self.rightArm.zRot = 0.05F;
        self.leftArm.zRot = -0.05F;
    }
}
