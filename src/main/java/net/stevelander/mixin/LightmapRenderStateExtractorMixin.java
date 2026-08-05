package net.stevelander.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import net.stevelander.feature.XRay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LightmapRenderStateExtractor.class)
public abstract class LightmapRenderStateExtractorMixin {
    @ModifyExpressionValue(
        method = "extract",
        at = @At(value = "INVOKE", target = "Ljava/lang/Double;floatValue()F", ordinal = 0)
    )
    private float stevelander$applyFullBright(float brightnessOption) {
        return XRay.isEnabled() ? Float.MAX_VALUE : brightnessOption;
    }
}
