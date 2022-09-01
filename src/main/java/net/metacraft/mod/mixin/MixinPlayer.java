package net.metacraft.mod.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class MixinPlayer {
    @Inject(method = "collideWithEntity", at = @At(value = "HEAD"), cancellable = true)
    private void entityCollide(Entity entity, CallbackInfo info) {
    }
}
