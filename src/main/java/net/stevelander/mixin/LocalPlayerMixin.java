package net.stevelander.mixin;

import net.minecraft.client.player.LocalPlayer;
import net.stevelander.feature.SpearKill;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {

    @Inject(method = "sendPosition", at = @At("HEAD"), cancellable = true)
    private void stevelander$holdSpoofedPosition(CallbackInfo ci) {
        if (SpearKill.isBusy()) {
            ci.cancel();
        }
    }
}
