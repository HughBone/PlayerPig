package com.hughbone.playerpig.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Leashable.class)
public interface LeashableMixin {

  // Disable lead breaking
  @Inject(method = "tickLeash",
    at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Leashable;dropLeash()V"),
    cancellable = true)
  private static <E extends Entity & Leashable> void tickLeash(
    ServerLevel world,
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