package com.hughbone.playerpig.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.Leashable;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Leashable.class)
public interface LeashableMixin {

  // Disable lead breaking
  @Inject(method = "tickLeash",
    at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Leashable;detachLeash()V"),
    cancellable = true)
  private static <E extends Entity & Leashable> void tickLeash(
    ServerWorld world,
    E entity,
    CallbackInfo ci)
  {
    //        if (entity instanceof PigEntity pig
    //            && ((PlayerPigExt) pig).isPlayerPig()
    //        ) {
    //            Entity entity2 = entity.getLeashHolder();
    //            float f = entity.distanceTo(entity2);
    //            entity.applyLeashElasticity(entity2, f);
    //            entity.limitFallDistance();
    //            ci.cancel();
    //        }
  }

}