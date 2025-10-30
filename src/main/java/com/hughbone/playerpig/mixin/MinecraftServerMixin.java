package com.hughbone.playerpig.mixin;

import com.hughbone.playerpig.piglist.SavePigList;
import com.hughbone.playerpig.util.PPUtil;
import java.io.IOException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Always make sure pigs spawn before server closes
@Mixin(MinecraftServer.class)
//@Environment(EnvType.SERVER)
public abstract class MinecraftServerMixin {

  @Shadow private PlayerManager playerManager;

  @Inject(method = "stop", at = @At("HEAD"))
  private void stopMixin(boolean bl, CallbackInfo ci) throws InterruptedException {
    if (!PPUtil.serverStopping) { // Makes sure this only runs one time
      PPUtil.serverStopping =
        true; // Prevents DisconnectEvent from spawning pigs (DisconnectEvent is fucky when
      // server stops)
      // Spawn a pig for all players in world
      for (ServerPlayerEntity player : playerManager.getPlayerList()) {
        PPUtil.spawnPlayerPig(player);
      }
      // Save player pig position data to files
      try {
        SavePigList.save();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

}