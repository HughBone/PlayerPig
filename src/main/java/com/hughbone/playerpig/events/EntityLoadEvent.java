package com.hughbone.playerpig.events;

import com.hughbone.playerpig.PlayerPigExt;
import com.hughbone.playerpig.util.PPUtil;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.PigEntity;

// Adds to PigList, fixes dimension change, fixes duplicate pig fuckyness
public class EntityLoadEvent {

    public static void init() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            try {
                if (entity.getType().equals(EntityType.PIG)) {
                    if (((PlayerPigExt) entity).isPlayerPig()) {
                        PigEntity pigLoaded = (PigEntity) entity;

                        for (PigEntity pigInList : PPUtil.getList()) {
                            if (((PlayerPigExt) pigInList).getPlayerUUID().equals(((PlayerPigExt) pigLoaded).getPlayerUUID())) { // Check to see if the loaded pig's corresponding player matches one in PigList
                                // Dimension change fix (Same UUID, different dimension)
                                if (pigInList.getUuid().equals(pigLoaded.getUuid())) {
                                    PPUtil.getList().remove(pigInList);
                                    PPUtil.appendList(pigLoaded);
                                }
                                // Fix duplicate pigs (rarely happens, different UUIDs)
                                else {
                                    pigLoaded.remove(Entity.RemovalReason.KILLED);
                                    PPUtil.getList().remove(pigLoaded);
                                }
                                return;
                            }
                        }

                        // Add loaded player pig into PigList if not already there
                        PPUtil.appendList(pigLoaded);
                    }
                }
            } catch (Exception e) {}
        });
    }
}
