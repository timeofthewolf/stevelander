package net.stevelander.mixin;

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.stevelander.feature.AntiHunger;
import net.stevelander.feature.NoFall;
import net.stevelander.interfaces.MovePacketAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public abstract class ConnectionMixin {
    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"))
    private void stevelander$applyNoFall(Packet<?> packet, CallbackInfo ci) {
        if (packet instanceof ServerboundMovePlayerPacket movePacket) {
            AntiHunger.onMovePacket(movePacket, (MovePacketAccess) movePacket);
            NoFall.onMovePacket(movePacket);
        }
    }
}
