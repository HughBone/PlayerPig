package com.hughbone.playerpig.events;

import com.hughbone.playerpig.PlayerJoin;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class JoinEvent {
    public static void init() {
        ServerPlayConnectionEvents.JOIN.register((handler, packetSender, server) -> {
            if (!handler.player.isSpectator()) {
                new PlayerJoin().join(handler.player); // Teleport player & Remove player pig
            }
        });
    }
}
