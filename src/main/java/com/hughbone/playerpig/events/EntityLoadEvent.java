package com.hughbone.playerpig.events;

import com.hughbone.playerpig.PlayerPigExt;
import com.hughbone.playerpig.util.PPUtil;
import java.util.List;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.server.network.ServerPlayerEntity;

// Adds to PigList, fixes dimension change, fixes duplicate pig fuckyness
public class EntityLoadEvent {

  public static void init() {
    ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
      try {
        if (entity.getType().equals(EntityType.PIG) && ((PlayerPigExt) entity).isPlayerPig()) {
          PigEntity pig = (PigEntity) entity;
          final String matchingPlayerUUID = ((PlayerPigExt) pig).getPlayerUUID();
          PigEntity matchingPig = PPUtil.pigList.get(matchingPlayerUUID);
          final List<ServerPlayerEntity> playerList =
            world.getServer().getPlayerManager().getPlayerList();

          if (matchingPig != null) {
            // Remove duplicate (Matching pig, different UUID)
            if (!matchingPig.getUuid().equals(pig.getUuid())) {
              pig.discard();
              return;
            }
          }

          // Add loaded player pig into PigList
          PPUtil.pigList.put(matchingPlayerUUID, pig);

          if (!PPUtil.serverStopping) {
            // Remove loaded pig if matching player is online
            for (ServerPlayerEntity player : playerList) {
              if (player.isDisconnected()) {
                continue;
              }
              if (player.getUuidAsString().equals(matchingPlayerUUID)) {
                try {
                  pig.discard();
                  PPUtil.pigList.remove(player.getUuidAsString());
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
