package com.hughbone.playerpig.commands;

import com.hughbone.playerpig.PlayerPigExt;
import com.hughbone.playerpig.piglist.LoadPigList;
import com.hughbone.playerpig.util.PPUtil;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.registry.DynamicRegistryManager;

import java.util.List;

public class PiglistCommand {

    private static void sendMessage(CommandContext<ServerCommandSource> ctx, int x, int y, int z, String world, String playerName) {
        try {
            CommandManager cm = new CommandManager(CommandManager.RegistrationEnvironment.ALL, new CommandRegistryAccess(DynamicRegistryManager.createAndLoad()));

            String command = "tellraw " + ctx.getSource().getPlayer().getEntityName() +
                    " {\"text\":\"" +
                    "(" + playerName + ": " + x + ", " + y + ", " + z + ", " + world + ")" +
                    "\",\"color\":\"blue\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"" + "/execute in " +
                    world +
                    " run tp @p " +
                    x + " " + y + " " + z +
                    "\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"" +
                    playerName + "\"}]}}";

            cm.getDispatcher().execute(command, ctx.getSource().getServer().getCommandSource());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void init() {
        // List all player pigs in memory
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated, okay) -> dispatcher.register(CommandManager.literal("piglist").executes(ctx -> {

            if (ctx.getSource().getPlayer().hasPermissionLevel(4) || ctx.getSource().getPlayer().getEntityName().equals("HughBone")) { // HughBone is here for debugging
                Thread thread = new Thread() {
                    public void run() {
                        boolean playerPigsFound = false;

                        outer:
                        for (List<String> unloadedPiggy : LoadPigList.getAllData()) {
                            for (PigEntity pigInList : PPUtil.getPigList()) {
                                if (((PlayerPigExt) pigInList).getPlayerUUID().equals(unloadedPiggy.get(4))) {
                                    continue outer;
                                }
                            }
                            int posX = 0;
                            int posY = 0;
                            int posZ = 0;
                            String dimension = null;
                            String playerName = "Unloaded_PlayerPig";
                            try {
                                posX = (int) Double.parseDouble(unloadedPiggy.get(0));
                                posY = (int) Double.parseDouble(unloadedPiggy.get(1));
                                posZ = (int) Double.parseDouble(unloadedPiggy.get(2));
                                dimension = unloadedPiggy.get(3);
                                playerName = unloadedPiggy.get(5);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            sendMessage(ctx, posX, posY, posZ, dimension, playerName);
                            playerPigsFound = true;
                        }

                        for (PigEntity pigInList : PPUtil.getPigList()) {
                            sendMessage(ctx, pigInList.getBlockX(), pigInList.getBlockY(), pigInList.getBlockZ(), pigInList.world.getRegistryKey().getValue().toString(), ((PlayerPigExt) pigInList).getPlayerName());
                            playerPigsFound = true;
                        }
                        // Success messages
                        if (!playerPigsFound) {
                            ctx.getSource().getPlayer().sendMessage(Text.literal("[PlayerPig] No player pigs found."), false);
                        }

                    }
                };

                thread.start();
            } else {
                ctx.getSource().getPlayer().sendMessage(Text.literal("[PlayerPig] You need OP to use this command."), false);
            }
            return 1;
        })));
    }

}
