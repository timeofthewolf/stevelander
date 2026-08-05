package net.stevelander.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.stevelander.Stevelander;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @ModifyVariable(method = "pick(DFZ)Lnet/minecraft/world/phys/HitResult;", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private boolean stevelander$liquidPlace(boolean hitFluids) {
        if (!Stevelander.config().liquidPlace.enabled) {
            return hitFluids;
        }

        if ((Object) this != Minecraft.getInstance().player) {
            return hitFluids;
        }

        return true;
    }
}
