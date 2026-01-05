package com.hughbone.playerpig.events;

import com.hughbone.playerpig.PlayerPigExt;
import com.hughbone.playerpig.util.PPUtil;
import java.util.List;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;

// Adds to PigList, fixes dimension change, fixes duplicate pig fuckyness
public class EntityLoadEvent {

  public static void init() {
    ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
      try {
        if (entity.getType().equals(EntityType.PIG) && ((PlayerPigExt) entity).isPlayerPig()) {
          Pig pig = (Pig) entity;
          final String matchingPlayerUUID = ((PlayerPigExt) pig).getPlayerUUID();
          Pig matchingPig = PPUtil.pigList.get(matchingPlayerUUID);
          final List<ServerPlayer> playerList =
            world.getServer().getPlayerList().getPlayers();

          if (matchingPig != null) {
            // Remove duplicate (Matching pig, different UUID)
            if (!matchingPig.getUUID().equals(pig.getUUID())) {
              pig.discard();
              return;
            }
          }

          // Add loaded player pig into PigList
          PPUtil.pigList.put(matchingPlayerUUID, pig);

          if (!PPUtil.serverStopping) {
            // Remove loaded pig if matching player is online
            for (ServerPlayer player : playerList) {
              if (player.hasDisconnected()) {
                continue;
              }
              if (player.getStringUUID().equals(matchingPlayerUUID)) {
                try {
                  pig.discard();
                  PPUtil.pigList.remove(player.getStringUUID());
                  return;
                } catch (Exception e) {
                  e.printStackTrace();
                }
              }
            }
          }
        }
      } catch (Exception e) {
      }
    });
  }

}
