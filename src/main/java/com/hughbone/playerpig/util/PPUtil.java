package com.hughbone.playerpig.util;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.world.GameRules;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class PPUtil {

    private static List<PigEntity> pigList = new ArrayList<PigEntity>();

    public static List<PigEntity> getPigList() {
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
            CommandManager cm = new CommandManager(CommandManager.RegistrationEnvironment.ALL);
            boolean sendCommandFB = server.getGameRules().get(GameRules.SEND_COMMAND_FEEDBACK).get(); // original value
            server.getGameRules().get(GameRules.SEND_COMMAND_FEEDBACK).set(false, server); // set to false

            cm.getDispatcher().execute("execute in " + dimension + " run forceload add " + posX + " " + PosZ, server.getCommandSource());
            Thread.sleep(500);
            cm.getDispatcher().execute("execute in " + dimension + " run forceload remove " + posX + " " + PosZ, server.getCommandSource());

            server.getGameRules().get(GameRules.SEND_COMMAND_FEEDBACK).set(sendCommandFB, server); // reset to original
        } catch (InterruptedException | CommandSyntaxException e) {}
    }

}
