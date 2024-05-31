package com.hughbone.playerpig.mixin;

import com.hughbone.playerpig.piglist.LoadPigList;
import com.hughbone.playerpig.util.PPUtil;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.network.ClientConnection;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(PlayerManager.class)
//@Environment(EnvType.SERVER)
public class PlayerManagerMixin {

    @Shadow
    @Final
    private MinecraftServer server;

    @Unique
    private ServerPlayerEntity player = null;

    @Inject(method = "onPlayerConnect", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getWorld(Lnet/minecraft/registry/RegistryKey;)Lnet/minecraft/server/world/ServerWorld;"))
    private void test1(ClientConnection connection, ServerPlayerEntity player, ConnectedClientData clientData, CallbackInfo ci) {
        this.player = player;
    }

    @Redirect(method = "onPlayerConnect", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getWorld(Lnet/minecraft/registry/RegistryKey;)Lnet/minecraft/server/world/ServerWorld;"))
    private ServerWorld injected(MinecraftServer minecraftServer, RegistryKey<World> key) {
        // Get dimension from piglist
        PigEntity matchingPig = PPUtil.pigList.get(player.getUuidAsString());
        if (matchingPig != null) {
            player = null;
            return (ServerWorld) matchingPig.getEntityWorld();
        }

        // Else, get dimension from file
        List<List<String>> unloadedPigList = LoadPigList.getAllData();
        try {
            for (List<String> unloadedPiggy : unloadedPigList) {
                if (unloadedPiggy.get(4).equals(player.getUuidAsString())) {

                    Iterable<ServerWorld> worlds = player.getServer().getWorlds();
                    for (ServerWorld sw : worlds) {
                        final String dimension = sw.getRegistryKey().getValue().toString();
                        if (unloadedPiggy.get(3).equals(dimension)) {
                            RegistryKey<World> registryKey = sw.getRegistryKey();
                            return sw;
                        }
                    }
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return server.getWorld(key);
    }

}
