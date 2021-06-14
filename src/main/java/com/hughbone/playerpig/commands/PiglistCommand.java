package com.hughbone.playerpig.commands;

import com.hughbone.playerpig.PlayerPigExt;
import com.hughbone.playerpig.piglist.LoadPigList;
import com.hughbone.playerpig.util.PPUtil;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.server.command.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;

import java.util.List;

public class PiglistCommand {

    public static void init() {
        // List all player pigs in memory
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> dispatcher.register(CommandManager.literal("piglist").executes(ctx -> {


            if (ctx.getSource().getPlayer().hasPermissionLevel(4) || ctx.getSource().getPlayer().getEntityName().equals("HughBone")) { // HughBone is here for debugging
                Thread thread = new Thread() {
                    public void run() {
                        CommandManager cm = new CommandManager(CommandManager.RegistrationEnvironment.ALL);
                        // Load all in data folder
                        for (List<String> unloadedPiggy : LoadPigList.getAllData()) {
                            int posX = (int) Double.parseDouble(unloadedPiggy.get(0));
                            int posZ = (int) Double.parseDouble(unloadedPiggy.get(2));

                            // Temporarily load chunk in correct dimension so EntityLoadEvent adds pig to PigList
                            Iterable<ServerWorld> worlds = ctx.getSource().getMinecraftServer().getWorlds();
                            for (ServerWorld sw : worlds) {
                                String dimension = sw.getRegistryKey().getValue().toString();
                                if (unloadedPiggy.get(3).equals(dimension)) {
                                    PPUtil.loadPPDataChunks(ctx.getSource().getMinecraftServer(), dimension, posX, posZ);
                                    break;
                                }
                            }
                        }
                        PPUtil.deleteAllFiles();

                        for (PigEntity pigInList: PPUtil.getPigList()) {
                            String textLine = "tellraw $target {\"text\":\"$text\",\"color\":\"blue\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/execute in $world run tp @p $posX $posY $posZ\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"$hovertext\"}]}}";

                            try {
                                textLine = textLine.replace("$target", (ctx.getSource().getPlayer().getEntityName()));
                                textLine = textLine.replace("$text", "(" + ((PlayerPigExt) pigInList).getPlayerName() + ": " + pigInList.getBlockPos().getX() + ", " + pigInList.getBlockPos().getY() + ", " + pigInList.getBlockPos().getZ() + ", " + pigInList.world.getRegistryKey().getValue() + ")");
                                textLine = textLine.replace("$hovertext", ((PlayerPigExt) pigInList).getPlayerName());
                                textLine = textLine.replace("$world", pigInList.world.getRegistryKey().getValue().toString());
                                textLine = textLine.replace("$posX", String.valueOf(pigInList.getBlockPos().getX()));
                                textLine = textLine.replace("$posY", String.valueOf(pigInList.getBlockPos().getY()));
                                textLine = textLine.replace("$posZ", String.valueOf(pigInList.getBlockPos().getZ()));
                                cm.getDispatcher().execute(textLine, ctx.getSource().getMinecraftServer().getCommandSource());

                            } catch (CommandSyntaxException e) {}

                        }

                        // Success messages
                        if (PPUtil.getPigList().isEmpty()) {
                            ctx.getSource().sendFeedback(new LiteralText("[PlayerPig] No player pigs found."), false);
                        }

                    }
                };
                thread.start();
            }

            else {
                ctx.getSource().sendFeedback(new LiteralText("[PlayerPig] You need OP to use this command."), false);
            }
            return 1;
        })));
    }
}
