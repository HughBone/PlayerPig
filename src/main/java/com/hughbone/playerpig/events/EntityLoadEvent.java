package com.hughbone.playerpig.events;

import com.hughbone.playerpig.PlayerExt;
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

                        Thread loadThread = new Thread() {

                            public void run() {
                                PigEntity pigLoaded = (PigEntity) entity;
                                final String pigLoadedPlayerUUID = ((PlayerPigExt) pigLoaded).getPlayerUUID();
                                final List<ServerPlayerEntity> playerList = world.getServer().getPlayerManager().getPlayerList();

                                for (PigEntity pigInList : PPUtil.getPigList()) {
                                    if (((PlayerPigExt) pigInList).getPlayerUUID().equals(pigLoadedPlayerUUID)) { // Check to see if the loaded pig's corresponding player matches one in PigList
                                        // Dimension change fix (Same UUID, different dimension)
                                        if (pigInList.getUuid().equals(pigLoaded.getUuid())) {
                                            PPUtil.getPigList().remove(pigInList);
                                            PPUtil.getPigList().add(pigLoaded);
                                        }
                                        // Fix duplicate pigs (rarely happens, different UUIDs)
                                        else {
                                            pigLoaded.remove(Entity.RemovalReason.DISCARDED);
                                            PPUtil.getPigList().remove(pigLoaded);
                                        }
                                        return;
                                    }
                                }

                                // Add loaded player pig into PigList if not already there
                                PPUtil.getPigList().add(pigLoaded);

                                // Kill pig if corresponding player is online
                                try {
                                    Thread.sleep(250);
                                    for (ServerPlayerEntity player : playerList) {
                                        if (((PlayerExt) player).getJustJoined()) {
                                            if (player.getUuidAsString().equals(pigLoadedPlayerUUID)) {
                                                pigLoaded.remove(Entity.RemovalReason.DISCARDED);
                                                PPUtil.getPigList().remove(pigLoaded);
                                                ((PlayerExt) player).setJustJoined(false);
                                                return;
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                        };
                        loadThread.start();
                    }
                }
            } catch (Exception e) {}
        });
    }

}
