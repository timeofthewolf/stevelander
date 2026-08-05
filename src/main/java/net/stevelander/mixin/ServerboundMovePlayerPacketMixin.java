package net.stevelander.mixin;

import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.stevelander.interfaces.MovePacketAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerboundMovePlayerPacket.class)
public abstract class ServerboundMovePlayerPacketMixin implements MovePacketAccess {
    @Mutable
    @Shadow
    @Final
    protected boolean onGround;

    @Mutable
    @Shadow
    @Final
    protected double x;

    @Mutable
    @Shadow
    @Final
    protected double y;

    @Mutable
    @Shadow
    @Final
    protected double z;

    @Override
    public void stevelander$setOnGround(boolean onGround) {
        this.onGround = onGround;
    }

    @Override
    public void stevelander$offsetPosition(double dx, double dy, double dz) {
        this.x += dx;
        this.y += dy;
        this.z += dz;
    }
}
