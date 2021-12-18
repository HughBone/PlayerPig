package com.hughbone.playerpig.events;

import com.hughbone.playerpig.PlayerExt;
import com.hughbone.playerpig.PlayerPigExt;
import com.hughbone.playerpig.commands.PigremoveallCommand;
import com.hughbone.playerpig.util.PPUtil;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.server.network.ServerPlayerEntity;

// Spawn player pig on disconnect
public class DisconnectEvent {

    public static boolean serverStopping = false;

    public static void init() {
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayerEntity player = handler.player;
            ((PlayerExt) player).setJustJoined(false);

            if (!player.isSpectator() && !serverStopping && PigremoveallCommand.allowPPSpawn) {
                // Don't spawn player pig if matching one already exists
                for (PigEntity pigInList : PPUtil.getPigList()) {
                    if (((PlayerPigExt) pigInList).getPlayerUUID().equals(player.getUuidAsString())) {
                        return;
                    }
                }
                PPUtil.spawnPlayerPig(player);
            }
        });
    }
}