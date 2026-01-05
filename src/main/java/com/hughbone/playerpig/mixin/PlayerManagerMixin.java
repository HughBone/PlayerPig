package com.hughbone.playerpig.mixin;

import com.hughbone.playerpig.util.PPUtil;
import java.util.List;
import net.minecraft.network.Connection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.animal.pig.Pig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
//@Environment(EnvType.SERVER)
public abstract class PlayerManagerMixin {

  @Shadow public abstract MinecraftServer getServer();

  @Inject(method = "placeNewPlayer", at = @At("HEAD"))
  private void injected(
    Connection connection,
    ServerPlayer player,
    CommonListenerCookie clientData,
    CallbackInfo ci)
  {

    // Get dimension from piglist
    Pig matchingPig = PPUtil.pigList.get(player.getStringUUID());
    if (matchingPig != null) {
      player.setServerLevel((ServerLevel) matchingPig.level());
      player.absSnapTo(matchingPig.getX(), matchingPig.getY(), matchingPig.getZ());
      return;
    }

    // Get Position from file
    try {
      for (List<String> unloadedPiggy : PPUtil.UnloadedPigList) {
        if (unloadedPiggy.get(4).equals(player.getStringUUID())) {
          Iterable<ServerLevel> worlds = this.getServer().getAllLevels();
          for (ServerLevel sw : worlds) {
            final String dimension = sw.dimension().identifier().toString();
            if (unloadedPiggy.get(3).equals(dimension)) {
              player.setServerLevel(sw);
              player.setPosRaw(
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
