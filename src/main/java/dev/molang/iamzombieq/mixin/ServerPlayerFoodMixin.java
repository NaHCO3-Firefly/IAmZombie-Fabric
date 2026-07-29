package dev.molang.iamzombieq.mixin;

import dev.molang.iamzombieq.gameplay.ZombieFoodEvents;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Intercepts ServerPlayer.completeUsingItem() to apply zombie food effects
 * when a zombie player finishes eating.
 */
@Mixin(ServerPlayer.class)
abstract class ServerPlayerFoodMixin {

    @Inject(method = "completeUsingItem", at = @At("HEAD"))
    private void iamzombieq$onItemUseFinish(CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        ZombieFoodEvents.onItemUseFinished(player);
    }
}
