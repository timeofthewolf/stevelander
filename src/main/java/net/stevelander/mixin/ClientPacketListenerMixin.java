package net.stevelander.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.stevelander.feature.AntiExploit;
import net.stevelander.feature.Flight;
import net.stevelander.feature.SpearKill;
import net.stevelander.feature.Warp;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
    @Inject(method = "handlePlayerAbilities", at = @At("RETURN"))
    private void stevelander$trackFlightPermission(ClientboundPlayerAbilitiesPacket packet, CallbackInfo ci) {
        Flight.onPlayerAbilities(packet);
    }

    @Inject(method = "handleMovePlayer", at = @At("RETURN"))
    private void stevelander$detectSetback(ClientboundPlayerPositionPacket packet, CallbackInfo ci) {
        Warp.onSetback();
        SpearKill.abort();
        Flight.onSetback();
    }

    @ModifyExpressionValue(
        method = "handleParticleEvent",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/game/ClientboundLevelParticlesPacket;getCount()I")
    )
    private int stevelander$limitParticleAmount(int original) {
        return AntiExploit.limitParticleAmount(original);
    }

    @ModifyExpressionValue(
        method = "handleParticleEvent",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/game/ClientboundLevelParticlesPacket;getMaxSpeed()F")
    )
    private float stevelander$limitParticleSpeed(float original) {
        return AntiExploit.limitParticleSpeed(original);
    }

    @ModifyExpressionValue(
        method = "handleExplosion",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/game/ClientboundExplodePacket;radius()F")
    )
    private float stevelander$limitExplosionStrength(float original) {
        return AntiExploit.limitExplosionStrength(original);
    }
}
