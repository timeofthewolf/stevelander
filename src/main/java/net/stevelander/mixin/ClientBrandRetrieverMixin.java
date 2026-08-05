package net.stevelander.mixin;

import net.minecraft.client.ClientBrandRetriever;
import net.stevelander.Stevelander;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientBrandRetriever.class)
public abstract class ClientBrandRetrieverMixin {

    @Inject(method = "getClientModName", at = @At("HEAD"), cancellable = true)
    private static void stevelander$spoofBrand(CallbackInfoReturnable<String> cir) {
        if (Stevelander.config().spoofClientBrand) {
            cir.setReturnValue(ClientBrandRetriever.VANILLA_NAME);
        }
    }
}
