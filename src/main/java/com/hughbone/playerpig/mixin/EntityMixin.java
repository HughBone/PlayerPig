package com.hughbone.playerpig.mixin;

import com.hughbone.playerpig.util.PPUtil;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
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

  @Shadow public abstract String getUuidAsString();

  @Shadow private World world;

  @ModifyArgs(method = "readData",
    at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;setPos(DDD)V"))
  private void readNbt(Args args) {
    if (!(this.world instanceof ServerWorld serverWorld)) {
      return;
    }
    if (!this.type.equals(EntityType.PLAYER)) {
      return;
    }

    // Get Position from piglist
    PigEntity matchingPig = PPUtil.pigList.get(this.getUuidAsString());
    if (matchingPig != null) {
      args.set(0, matchingPig.getX());
      args.set(1, matchingPig.getY());
      args.set(2, matchingPig.getZ());
      return;
    }

    // Get Position from file
    try {
      for (List<String> unloadedPiggy : PPUtil.UnloadedPigList) {
        if (unloadedPiggy.get(4).equals(this.getUuidAsString())) {
          Iterable<ServerWorld> worlds = serverWorld.getServer().getWorlds();
          for (ServerWorld sw : worlds) {
            final String dimension = sw.getRegistryKey().getValue().toString();
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
