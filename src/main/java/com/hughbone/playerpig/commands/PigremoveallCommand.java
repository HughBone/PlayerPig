package com.hughbone.playerpig.commands;

import com.hughbone.playerpig.util.PPUtil;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

import java.util.ArrayList;
import java.util.List;

public class PigremoveallCommand {

    public static void init() {
        // Kills one player pig within 4 blocks of the player
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> dispatcher.register(CommandManager.literal("pigremoveall")
                .requires(source -> source.hasPermissionLevel(4))
                .executes(ctx -> {
            if (ctx.getSource().hasPermissionLevel(4)) {
                ServerPlayerEntity player = ctx.getSource().getPlayer();
                if (PPUtil.getList().isEmpty()) {
                    ctx.getSource().sendFeedback(new LiteralText("[PlayerPig] No player pigs found."), false);
                    return 1;
                }
                else {
                    // Make a copy of pos/dimension data in PigList so that no fuckyness happens (i.e. the list changing w/o my consent #metoo)
                    List<List<String>> pigDataList = new ArrayList<List<String>>();
                    for (PigEntity pigInList : PPUtil.getList()) {
                        List<String> temp = new ArrayList<String>();
                        temp.add(""+ pigInList.getX());
                        temp.add(""+ pigInList.getY());
                        temp.add(""+ pigInList.getZ());
                        temp.add(pigInList.world.getRegistryKey().getValue().toString());
                        pigDataList.add(temp);
                    }
                    // Load chunk, remove, unload chunk again
                    CommandManager cm = new CommandManager(CommandManager.RegistrationEnvironment.ALL);
                    try {
                        for (List<String> pigInList : pigDataList) {
                            int posX = (int) Double.parseDouble(pigInList.get(0));
                            int posZ = (int) Double.parseDouble(pigInList.get(2));
                            // Forceload chunk
                            if (pigInList.get(3).contains("overworld")) {
                                cm.getDispatcher().execute("execute in minecraft:overworld run forceload add " + posX + " " + posZ, player.getServer().getCommandSource());
                            } else if (pigInList.get(3).contains("the_nether")) {
                                cm.getDispatcher().execute("execute in minecraft:the_nether run forceload add " + posX + " " + posZ, player.getServer().getCommandSource());
                            } else if (pigInList.get(3).contains("the_end")) {
                                cm.getDispatcher().execute("execute in minecraft:the_end run forceload add " + posX + " " + posZ, player.getServer().getCommandSource());
                            }
                            // Kill all player pigs
                            for (PigEntity pigEntity : PPUtil.getList()) {
                                pigEntity.remove(Entity.RemovalReason.KILLED);
                            }
                            // Stop forceloading chunk
                            if (pigInList.get(3).contains("overworld")) {
                                cm.getDispatcher().execute("execute in minecraft:overworld run forceload remove all", player.getServer().getCommandSource());
                            } else if (pigInList.get(3).contains("the_nether")) {
                                cm.getDispatcher().execute("execute in minecraft:the_nether run forceload remove all", player.getServer().getCommandSource());
                            } else if (pigInList.get(3).contains("the_end")) {
                                cm.getDispatcher().execute("execute in minecraft:the_end run forceload remove all", player.getServer().getCommandSource());
                            }
                        }
                        PPUtil.getList().clear();
                        // Result message
                        if (PPUtil.getList().isEmpty())
                            player.sendMessage(new LiteralText("[PlayerPig] All player pigs removed!"), false);
                        else
                            player.sendMessage(new LiteralText("[PlayerPig] Error: Not all player pigs removed. Please run this command again."), false);

                    } catch (Exception e){}
                }
            }
            return 1;
        })));
    }
}
