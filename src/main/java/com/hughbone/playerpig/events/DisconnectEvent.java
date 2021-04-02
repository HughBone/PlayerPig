package com.hughbone.playerpig.events;

import com.hughbone.playerpig.PlayerPigExt;
import com.hughbone.playerpig.piglist.PigList;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.commons.lang3.StringUtils;

// Spawn player pig on disconnect
public class DisconnectEvent {

    public static void init() {
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayerEntity player = handler.player;

            if (!player.isSpectator()) {
                // Don't spawn player pig if matching one already in exists
                boolean inList = false;
                for (PigEntity pigInList: PigList.getList()) {
                    if (((PlayerPigExt) pigInList).getPlayerUUID().equals(player.getUuidAsString())) {
                        inList = true;
                        break;
                    }
                }


                if (!inList) {
                    PigEntity playerPig = EntityType.PIG.create(player.world);
                    ((PlayerPigExt) playerPig).setPlayerPig(true);

                    // Store player name, player uuid as tags
                    String playerName = StringUtils.substringBetween(player.getName().toString(), "text='", "', ");
                    ((PlayerPigExt) playerPig).setPlayerName(playerName);
                    ((PlayerPigExt) playerPig).setPlayerUUID(player.getUuidAsString());

                    // Set display name, make silent, make invincible
                    playerPig.setCustomNameVisible(true);
                    playerPig.setCustomName(player.getName());
                    playerPig.setSilent(true);
                    playerPig.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 2147483647, 5, false, false));

                    // Spawn player pig in world
                    playerPig.updatePosition(player.getPos().getX(), player.getPos().getY(), player.getPos().getZ());
                    playerPig.updateTrackedPosition(player.getPos().getX(), player.getPos().getY(), player.getPos().getZ());
                    player.world.spawnEntity(playerPig);
                }
            }
        });
    }
}