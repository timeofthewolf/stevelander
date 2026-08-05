package net.stevelander.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.stevelander.feature.KeepSprint;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(Player.class)
public abstract class PlayerMixin {

    @ModifyArgs(
        method = "causeExtraKnockback",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;multiply(DDD)Lnet/minecraft/world/phys/Vec3;")
    )
    private void stevelander$keepSprint(Args args) {
        if (!KeepSprint.isEnabled()) {
            return;
        }

        if ((Object) this != Minecraft.getInstance().player) {
            return;
        }

        final double motion = KeepSprint.getMotion();
        args.set(0, motion);
        args.set(2, motion);
    }
}
