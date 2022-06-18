package com.hughbone.playerpig.util;

import com.hughbone.playerpig.PlayerExt;
import com.hughbone.playerpig.PlayerPigExt;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.world.GameRules;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;

public class PPUtil {

    private static LinkedList<PigEntity> pigList = new LinkedList<>();

    public static LinkedList<PigEntity> getPigList() {
        return pigList;
    }

    public static void createDataFolder() {
        try {
            Files.createDirectory(Paths.get(System.getProperty("user.dir") + File.separator + "mods" + File.separator + "PlayerPig_Data"));
        } catch (Exception e) {}
    }

    public static void deleteAllFiles() {
        createDataFolder();
        try {
            File dir = new File(System.getProperty("user.dir") + File.separator + "mods" + File.separator + "PlayerPig_Data");
            File[] listDir = dir.listFiles();
            if (listDir != null) {
                for (File child : listDir) {
                    child.delete();
                }
            }
        } catch (Exception e) {}
    }

    public static void removeFile(String filename) {
        File f = new File(System.getProperty("user.dir") + File.separator + "mods" + File.separator + "PlayerPig_Data" + File.separator + filename);
        f.delete();
    }

    public static void loadPPDataChunks(MinecraftServer server, String dimension, int posX, int PosZ) {
        try {
            CommandManager cm = new CommandManager(CommandManager.RegistrationEnvironment.ALL, new CommandRegistryAccess(DynamicRegistryManager.createAndLoad()));
            boolean sendCommandFB = server.getGameRules().get(GameRules.SEND_COMMAND_FEEDBACK).get(); // original value
            server.getGameRules().get(GameRules.SEND_COMMAND_FEEDBACK).set(false, server); // set to false

            cm.getDispatcher().execute("execute in " + dimension + " run forceload add " + posX + " " + PosZ, server.getCommandSource());
            Thread.sleep(500);
            cm.getDispatcher().execute("execute in " + dimension + " run forceload remove " + posX + " " + PosZ, server.getCommandSource());

            server.getGameRules().get(GameRules.SEND_COMMAND_FEEDBACK).set(sendCommandFB, server); // reset to original
        } catch (InterruptedException | CommandSyntaxException e) {}
    }

    public static void spawnPlayerPig(ServerPlayerEntity player) {
        PigEntity playerPig = EntityType.PIG.create(player.world);
        assert playerPig != null;
        ((PlayerPigExt) playerPig).setPlayerPig(true);
        // Store player name, player uuid as tags
        ((PlayerPigExt) playerPig).setPlayerName(player.getEntityName());
        ((PlayerPigExt) playerPig).setPlayerUUID(player.getUuidAsString());
        // Set display name, make silent, make invincible, add portal cooldown
        playerPig.setCustomNameVisible(true);
        playerPig.setCustomName(player.getName());
        playerPig.setSilent(true);
        playerPig.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 2147483647, 5, false, false));
        playerPig.resetNetherPortalCooldown();
        playerPig.saddle(null);

        // mount passengers to pig
        if (((PlayerExt) player).getLinkedPassenger() != null) {
            Entity passenger = ((PlayerExt) player).getLinkedPassenger();
            passenger.startRiding(playerPig, true);
        }
        // Mount pig to entity player was riding
        if (player.hasVehicle()) {
            playerPig.startRiding(player.getVehicle(), true);
            player.dismountVehicle();
        }

        // Spawn player pig in world
        playerPig.updatePosition(player.getPos().getX(), player.getPos().getY(), player.getPos().getZ());
        playerPig.updateTrackedPosition(player.getPos().getX(), player.getPos().getY(), player.getPos().getZ());
        player.world.spawnEntity(playerPig);
    }

}
