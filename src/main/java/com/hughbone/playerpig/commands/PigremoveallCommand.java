package com.hughbone.playerpig.commands;

import com.hughbone.playerpig.PlayerPigExt;
import com.hughbone.playerpig.piglist.LoadPigList;
import com.hughbone.playerpig.util.PPUtil;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.world.GameRules;

import java.util.List;

public class PigremoveallCommand {

    public static void init() {

        Thread thread = new Thread() {
            public void run() {
                // Kills one player pig within 4 blocks of the player
                CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> dispatcher.register(CommandManager.literal("pigremoveall").requires(source -> source.hasPermissionLevel(4)).executes(ctx -> {
                    ctx.getSource().sendFeedback(new LiteralText("[PlayerPig] Removing all PlayerPigs..."), false);
                    if (ctx.getSource().hasPermissionLevel(4)) {
                        ServerPlayerEntity player = ctx.getSource().getPlayer();

                        // Load all from data folder
                        try {
                            List<List<String>> unloadedPigList = LoadPigList.getAllData();
                            CommandManager cm = new CommandManager(CommandManager.RegistrationEnvironment.ALL);

                            for (List<String> unloadedPiggy : unloadedPigList) {
                                int posX = (int) Double.parseDouble(unloadedPiggy.get(0));
                                int posZ = (int) Double.parseDouble(unloadedPiggy.get(2));

                                // Temporarily load chunk in correct dimension so EntityLoadEvent adds pig to PigList
                                Iterable<ServerWorld> worlds = player.getServer().getWorlds();
                                for (ServerWorld sw : worlds) {
                                    String dimension = sw.getRegistryKey().getValue().toString();
                                    if (unloadedPiggy.get(3).equals(dimension)) {
                                        boolean sendCommandFB = player.getServer().getGameRules().get(GameRules.SEND_COMMAND_FEEDBACK).get(); // original value
                                        player.getServer().getGameRules().get(GameRules.SEND_COMMAND_FEEDBACK).set(false, player.getServer()); // set to false

                                        cm.getDispatcher().execute("execute in " + dimension + " run forceload add " + posX + " " + posZ, player.getServer().getCommandSource());
                                        Thread.sleep(250);
                                        for (PigEntity piggy : PPUtil.getPigList()) {
                                            player.sendMessage(new LiteralText("Removing " + ((PlayerPigExt) piggy).getPlayerName() + ".."), false);
                                            piggy.remove(Entity.RemovalReason.KILLED);
                                        }
                                        Thread.sleep(50);
                                        cm.getDispatcher().execute("execute in " + dimension + " run forceload remove " + posX + " " + posZ, player.getServer().getCommandSource());

                                        player.getServer().getGameRules().get(GameRules.SEND_COMMAND_FEEDBACK).set(sendCommandFB, player.getServer()); // reset to original
                                        break;
                                    }
                                }
                            }
                        } catch (Exception e) {}
                    }
                    ctx.getSource().sendFeedback(new LiteralText("[PlayerPig] All PlayerPigs removed."), false);
                    return 1;
                })));

                PPUtil.getPigList().clear(); // Clear all elements from the list
            }
        };

        thread.start();
    }
}
