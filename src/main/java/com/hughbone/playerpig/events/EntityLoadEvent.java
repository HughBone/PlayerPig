package com.hughbone.playerpig.events;

import com.hughbone.playerpig.PlayerPigExt;
import com.hughbone.playerpig.piglist.PigList;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.PigEntity;

// Fixes dimension change + duplicate pig fuckyness
public class EntityLoadEvent {

    public static void init() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            boolean inPigList = false;
            try {
                if (entity.getType().equals(EntityType.PIG)) {
                    if (((PlayerPigExt) entity).isPlayerPig()) {
                        PigEntity pigLoaded = (PigEntity) entity;

                        for (PigEntity pigInList: PigList.getList()) {
                            // Check to see if the loaded pig's corresponding player matches one in PigList
                            if (((PlayerPigExt) pigInList).getPlayerUUID().equals(((PlayerPigExt) pigLoaded).getPlayerUUID())) {
                                // Dimension change fix (Same UUID)
                                if (pigInList.getUuid().equals(pigLoaded.getUuid())) {
                                    PigList.getList().remove(pigInList);
                                    PigList.appendList(pigLoaded);
                                    inPigList = true;
                                }
                                // Fix duplicate pigs (rarely happens, different UUIDs)
                                // Removes loaded pig
                                else {
                                    pigLoaded.remove();
                                    PigList.getList().remove(pigLoaded);
                                    inPigList = true;
                                    break;
                                }
                            }
                        }
                        // Add loaded player pig into PigList if not already there (useful for server restarts)
                        if (!inPigList) {
                            PigList.appendList(pigLoaded);
                        }
                    }
                }
            } catch (Exception e) {}
        });
    }
}
