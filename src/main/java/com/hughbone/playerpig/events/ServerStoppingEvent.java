package com.hughbone.playerpig.events;

import com.hughbone.playerpig.piglist.SavePigList;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class ServerStoppingEvent {
    public static void init() {
        ServerLifecycleEvents.SERVER_STOPPING.register((server) -> {
            try {
                SavePigList.save(); // Save pig location and dimension to a file
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
