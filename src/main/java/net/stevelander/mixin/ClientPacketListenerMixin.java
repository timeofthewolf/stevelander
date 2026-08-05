package net.stevelander.mixin;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.stevelander.feature.Flight;
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
        Flight.onSetback();
    }
}
