package com.hughbone.playerpig.commands;

import com.hughbone.playerpig.PlayerPigExt;
import com.hughbone.playerpig.piglist.PigList;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.server.command.*;
import net.minecraft.text.LiteralText;

public class PiglistCommand {

    public static void init() {
        // List all player pigs in memory
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {

            dispatcher.register(CommandManager.literal("piglist").executes(ctx -> {
                if (ctx.getSource().getPlayer().hasPermissionLevel(4) || ctx.getSource().getPlayer().getName().toString().contains("HughBone")) { // HughBone is there for debugging
                    CommandManager cm = new CommandManager(CommandManager.RegistrationEnvironment.ALL);
                    for (PigEntity pigInList: PigList.getList()) {
                        String textLine = "tellraw @p {\"text\":\"$text\",\"color\":\"blue\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/execute in $world run tp @p $posX $posY $posZ\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"$hovertext\"}]}}";

                        textLine = textLine.replace("$text", "(" + ((PlayerPigExt) pigInList).getPlayerName() + ": " + pigInList.getBlockPos().getX() + ", " + pigInList.getBlockPos().getY() + ", " + pigInList.getBlockPos().getZ() + ", " + pigInList.world.getRegistryKey().getValue() + ")");
                        textLine = textLine.replace("$hovertext", ((PlayerPigExt) pigInList).getPlayerName());
                        textLine = textLine.replace("$world", pigInList.world.getRegistryKey().getValue().toString());
                        textLine = textLine.replace("$posX", String.valueOf(pigInList.getBlockPos().getX()));
                        textLine = textLine.replace("$posY", String.valueOf(pigInList.getBlockPos().getY()));
                        textLine = textLine.replace("$posZ", String.valueOf(pigInList.getBlockPos().getZ()));

                        cm.getDispatcher().execute(textLine, ctx.getSource().getMinecraftServer().getCommandSource());
                    }
                }
                else {
                    ctx.getSource().sendFeedback(new LiteralText("[Player Pig] You need OP to use this command."), false);
                }
                return 1;
            }));

        });
    }

}
