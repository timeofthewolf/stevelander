package net.stevelander.mixin;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.stevelander.feature.Criticals;
import net.stevelander.feature.MaceKill;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {

    @Inject(method = "attack", at = @At("HEAD"))
    private void stevelander$beforeAttack(Player player, Entity target, CallbackInfo ci) {
        Criticals.onAttack(target);
        MaceKill.onAttack();
    }
}
