package dev.molang.iamzombieq.mixin.client;

import dev.molang.iamzombieq.rules.ZombieRenderRules;
import dev.molang.iamzombieq.IAmZombieClientConfig;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Replaces the player avatar texture with a zombie skin when zombie rules are active.
 * This mixin intercepts getTextureLocation to return a zombie texture.
 *
 * In MC 26.2, AvatarRenderer uses extractRenderState + standard LivingEntityRenderer pipeline.
 * getTextureLocation is called during render to determine the skin texture.
 */
@Mixin(AvatarRenderer.class)
abstract class AvatarRendererMixin {

    @Inject(method = "getTextureLocation(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)Lnet/minecraft/resources/Identifier;",
            at = @At("HEAD"), cancellable = true)
    private void iamzombieq$zombieTexture(AvatarRenderState state, CallbackInfoReturnable<Identifier> cir) {
        // Check if the player should use zombie skin based on client config
        if (ZombieRenderRules.usesMonsterTexture(IAmZombieClientConfig.PLAYER_SKIN_MODE.get())) {
            // Return zombie skin texture
            cir.setReturnValue(Identifier.fromNamespaceAndPath("minecraft", "textures/entity/zombie/zombie.png"));
        }
    }
}
