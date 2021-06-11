package com.hughbone.playerpig.mixin;

import com.hughbone.playerpig.PlayerPigExt;
import com.hughbone.playerpig.events.DisconnectEvent;
import com.hughbone.playerpig.piglist.SavePigList;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.util.List;

// Always make sure pigs spawn before server closes
@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    @Shadow private PlayerManager playerManager;

    @Inject(method = "stop", at = @At("HEAD"))
    private void stopMixin(boolean bl, CallbackInfo ci) throws InterruptedException {

        if (!DisconnectEvent.serverStopping) { // Makes sure this only only runs one time
            DisconnectEvent.serverStopping = true; // Prevents DisconnectEvent from spawning pigs (DisconnectEvent is fucky when server stops)
            List<ServerPlayerEntity> playerList = playerManager.getPlayerList();
            // Spawn a pig for all players in world
            for (ServerPlayerEntity player : playerList) {
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
                // Spawn pig in world
                playerPig.setPosition(player.getPos().getX(), player.getPos().getY(), player.getPos().getZ());
                playerPig.updateTrackedPosition(player.getPos().getX(), player.getPos().getY(), player.getPos().getZ());
                player.world.spawnEntity(playerPig);
            }

            // Save player pig position data to files
            try {
                SavePigList.save();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
