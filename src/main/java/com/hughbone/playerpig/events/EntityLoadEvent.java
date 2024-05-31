package com.hughbone.playerpig.events;

import com.hughbone.playerpig.PlayerPigExt;
import com.hughbone.playerpig.util.PPUtil;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;

// Adds to PigList, fixes dimension change, fixes duplicate pig fuckyness
public class EntityLoadEvent {

    public static void init() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            try {
                if (entity.getType().equals(EntityType.PIG)) {
                    if (((PlayerPigExt) entity).isPlayerPig()) {
                        PigEntity pigLoaded = (PigEntity) entity;
                        final String pigLoadedPlayerUUID = ((PlayerPigExt) pigLoaded).getPlayerUUID();
                        PigEntity matchingPig = PPUtil.pigList.get(pigLoadedPlayerUUID);
                        final List<ServerPlayerEntity> playerList = world.getServer().getPlayerManager().getPlayerList();

                        if (matchingPig != null) {
                            // Remove duplicate (Matching pig, different UUID)
                            if (!matchingPig.getUuid().equals(pigLoaded.getUuid())) {
                                pigLoaded.remove(Entity.RemovalReason.DISCARDED);
                                return;
                            }
                        }

                        // Add loaded player pig into PigList
                        PPUtil.pigList.put(pigLoadedPlayerUUID, pigLoaded);

                        if (!PPUtil.serverStopping) {
                            // Remove loaded pig if matching player is online
                            for (ServerPlayerEntity player : playerList) {
                                if (player.isDisconnected()) {
                                    continue;
                                }
                                if (player.getUuidAsString().equals(pigLoadedPlayerUUID)) {
                                    try {
                                        pigLoaded.remove(Entity.RemovalReason.DISCARDED);
                                        PPUtil.pigList.remove(player.getUuidAsString());
                                        return;
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }

                    }
                }
            } catch (Exception e) {}
        });
    }

}
