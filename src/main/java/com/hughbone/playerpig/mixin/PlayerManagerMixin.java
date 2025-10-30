package com.hughbone.playerpig.mixin;

import com.hughbone.playerpig.util.PPUtil;
import java.util.List;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
//@Environment(EnvType.SERVER)
public abstract class PlayerManagerMixin {

  @Shadow public abstract MinecraftServer getServer();

  @Inject(method = "onPlayerConnect", at = @At("HEAD")) private void injected(
    ClientConnection connection,
    ServerPlayerEntity player,
    ConnectedClientData clientData,
    CallbackInfo ci)
  {

    // Get dimension from piglist
    PigEntity matchingPig = PPUtil.pigList.get(player.getUuidAsString());
    if (matchingPig != null) {
      player.setServerWorld((ServerWorld) matchingPig.getEntityWorld());
      player.updatePosition(matchingPig.getX(), matchingPig.getY(), matchingPig.getZ());
      return;
    }

    // Get Position from file
    try {
      for (List<String> unloadedPiggy : PPUtil.UnloadedPigList) {
        if (unloadedPiggy.get(4).equals(player.getUuidAsString())) {
          Iterable<ServerWorld> worlds = this.getServer().getWorlds();
          for (ServerWorld sw : worlds) {
            final String dimension = sw.getRegistryKey().getValue().toString();
            if (unloadedPiggy.get(3).equals(dimension)) {
              player.setServerWorld(sw);
              player.setPos(
                Double.parseDouble(unloadedPiggy.get(0)),
                Double.parseDouble(unloadedPiggy.get(1)),
                Double.parseDouble(unloadedPiggy.get(2))
              );
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
