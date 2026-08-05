package net.stevelander.mixin;

import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.player.Input;
import net.stevelander.feature.VehicleControl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin {
    @Inject(method = "tick", at = @At("RETURN"))
    private void stevelander$holdOntoVehicle(CallbackInfo ci) {
        if (!VehicleControl.shouldWithholdSneak()) {
            return;
        }

        final ClientInput input = (ClientInput) (Object) this;
        final Input keys = input.keyPresses;
        if (!keys.shift()) {
            return;
        }

        input.keyPresses = new Input(
            keys.forward(),
            keys.backward(),
            keys.left(),
            keys.right(),
            keys.jump(),
            false,
            keys.sprint()
        );
    }
}
