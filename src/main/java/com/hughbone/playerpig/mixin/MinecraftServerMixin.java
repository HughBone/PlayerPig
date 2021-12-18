package com.hughbone.playerpig.mixin;

import com.hughbone.playerpig.commands.PigremoveallCommand;
import com.hughbone.playerpig.events.DisconnectEvent;
import com.hughbone.playerpig.piglist.SavePigList;
import com.hughbone.playerpig.util.PPUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

// Always make sure pigs spawn before server closes
@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    @Shadow private PlayerManager playerManager;

    @Inject(method = "stop", at = @At("HEAD"))
    private void stopMixin(boolean bl, CallbackInfo ci) throws InterruptedException {
        if (!DisconnectEvent.serverStopping && PigremoveallCommand.allowPPSpawn) { // Makes sure this only runs one time
            DisconnectEvent.serverStopping = true; // Prevents DisconnectEvent from spawning pigs (DisconnectEvent is fucky when server stops)
            // Spawn a pig for all players in world
            for (ServerPlayerEntity player : playerManager.getPlayerList()) {
                PPUtil.spawnPlayerPig(player);
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