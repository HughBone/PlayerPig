package com.hughbone.playerpig.commands;

import com.hughbone.playerpig.PlayerPigExt;
import com.hughbone.playerpig.piglist.LoadPigList;
import com.hughbone.playerpig.util.PPUtil;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.registry.BuiltinRegistries;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.GameRules;

import java.util.List;

public class PigremoveallCommand {

    public static boolean allowPPSpawn = true;

    public static void init() {

        // Kills one player pig within 4 blocks of the player
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> dispatcher.register(CommandManager.literal("pigremoveall").requires(source -> source.hasPermissionLevel(4)).executes(ctx -> {
            Thread thread = new Thread() {
                public void run() {
                    allowPPSpawn = false; // stop player pigs from spawning

                    if (ctx.getSource().hasPermissionLevel(4)) {
                        CommandManager cm = new CommandManager(CommandManager.RegistrationEnvironment.ALL, CommandManager.createRegistryAccess(BuiltinRegistries.createWrapperLookup()));
                        ServerPlayerEntity player = null;
                        player = ctx.getSource().getPlayer();
                        Iterable<ServerWorld> worlds = player.getServer().getWorlds();

                        ctx.getSource().getPlayer().sendMessage(Text.of("[PlayerPig] Removing all PlayerPigs (This may take a while...)"), false);

                        // Load all from data folder
                        List<List<String>> unloadedPigList = LoadPigList.getAllData();

                        // Load all in data folder
                        for (List<String> unloadedPiggy : unloadedPigList) {
                            int posX = (int) Double.parseDouble(unloadedPiggy.get(0));
                            int posZ = (int) Double.parseDouble(unloadedPiggy.get(2));

                            // Temporarily load chunk in correct dimension so EntityLoadEvent adds pig to PigList
                            for (ServerWorld sw : worlds) {
                                String dimension = sw.getRegistryKey().getValue().toString();
                                if (unloadedPiggy.get(3).equals(dimension)) {
                                    PPUtil.loadPPDataChunks(ctx.getSource().getServer(), dimension, posX, posZ);
                                    break;
                                }
                            }
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {}
                        }

                        // Kill all in piglist
                        for (PigEntity piggy : PPUtil.pigList.values()) {
                            for (ServerWorld sw : worlds) {
                                String dimension = sw.getRegistryKey().getValue().toString();
                                if (piggy.getWorld().getRegistryKey().getValue().toString().equals(dimension)) {
                                    try {
                                        boolean sendCommandFB = player.getServer().getGameRules().get(GameRules.SEND_COMMAND_FEEDBACK).get(); // original value
                                        player.getServer().getGameRules().get(GameRules.SEND_COMMAND_FEEDBACK).set(false, player.getServer()); // set to false

                                        cm.getDispatcher().execute("execute in " + dimension + " run forceload add " + (int)piggy.getX() + " " + (int)piggy.getZ(), player.getServer().getCommandSource());

                                        Thread.sleep(250);
                                        for (PigEntity piggy2 : PPUtil.pigList.values()) {
                                            piggy2.remove(Entity.RemovalReason.KILLED);
                                        }
                                        Thread.sleep(250);
                                        player.sendMessage(Text.of("PlayerPig " + ((PlayerPigExt) piggy).getPlayerName() + " was removed."), false);
                                        cm.getDispatcher().execute("execute in " + dimension + " run forceload remove " + (int)piggy.getX() + " " + (int)piggy.getZ(), player.getServer().getCommandSource());

                                        player.getServer().getGameRules().get(GameRules.SEND_COMMAND_FEEDBACK).set(sendCommandFB, player.getServer()); // reset to original
                                    } catch (Exception e) {}
                                }
                            }
                        }
                        PPUtil.pigList.clear(); // Clear all elements from the list
                        PPUtil.deleteAllFiles(); // delete straggler files
                        ctx.getSource().getPlayer().sendMessage(Text.of("[PlayerPig] All PlayerPigs removed. (PlayerPigs will not spawn until the server reloads.)"), false);
                    }
                }
            };
            thread.start();
            return 1;
        })));

    }
}
