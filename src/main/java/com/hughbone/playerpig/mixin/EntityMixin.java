package com.hughbone.playerpig.mixin;

import com.hughbone.playerpig.util.PPUtil;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(Entity.class)
//@Environment(EnvType.SERVER)
public abstract class EntityMixin {

  @Shadow @Final private EntityType<?> type;

  @Shadow public abstract String getStringUUID();

  @Shadow private Level level;

  @ModifyArgs(method = "load",
    at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;setPosRaw(DDD)V"))
  private void readNbt(Args args) {
    if (!(this.level instanceof ServerLevel serverWorld)) {
      return;
    }
    if (!this.type.equals(EntityType.PLAYER)) {
      return;
    }

    // Get Position from piglist
    Pig matchingPig = PPUtil.pigList.get(this.getStringUUID());
    if (matchingPig != null) {
      args.set(0, matchingPig.getX());
      args.set(1, matchingPig.getY());
      args.set(2, matchingPig.getZ());
      return;
    }

    // Get Position from file
    try {
      for (List<String> unloadedPiggy : PPUtil.UnloadedPigList) {
        if (unloadedPiggy.get(4).equals(this.getStringUUID())) {
          Iterable<ServerLevel> worlds = serverWorld.getServer().getAllLevels();
          for (ServerLevel sw : worlds) {
            final String dimension = sw.dimension().location().toString();
            if (unloadedPiggy.get(3).equals(dimension)) {
              args.set(0, Double.parseDouble(unloadedPiggy.get(0)));
              args.set(1, Double.parseDouble(unloadedPiggy.get(1)));
              args.set(2, Double.parseDouble(unloadedPiggy.get(2)));
              return;
            }
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
