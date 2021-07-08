package com.hughbone.playerpig.commands;

import com.hughbone.playerpig.PlayerPigExt;
import com.hughbone.playerpig.piglist.LoadPigList;
import com.hughbone.playerpig.util.PPUtil;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.server.command.*;
import net.minecraft.text.LiteralText;

import java.util.List;

public class PiglistCommand {

    private static void sendMessage(CommandContext<ServerCommandSource> ctx, int x, int y, int z, String world, String playerName) {
        try {

            CommandManager cm = new CommandManager(CommandManager.RegistrationEnvironment.ALL);

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
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> dispatcher.register(CommandManager.literal("piglist").executes(ctx -> {

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
                            try {
                                ctx.getSource().getPlayer().sendMessage(new LiteralText("[PlayerPig] No player pigs found."), false);
                            } catch (CommandSyntaxException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                };

                thread.start();
            } else {
                ctx.getSource().getPlayer().sendMessage(new LiteralText("[PlayerPig] You need OP to use this command."), false);
            }
            return 1;
        })));
    }
}
