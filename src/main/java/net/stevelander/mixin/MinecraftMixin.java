package net.stevelander.mixin;

import net.minecraft.client.Minecraft;
import net.stevelander.feature.AirPlace;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Inject(method = "pick", at = @At("RETURN"))
    private void stevelander$airPlace(float partialTick, CallbackInfo ci) {
        final Minecraft minecraft = (Minecraft) (Object) this;
        minecraft.hitResult = AirPlace.adjust(minecraft, minecraft.hitResult);
    }
}
