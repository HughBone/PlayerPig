package com.hughbone.playerpig.commands;

import com.hughbone.playerpig.PlayerPigExt;
import com.hughbone.playerpig.util.PPUtil;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.entity.animal.pig.Pig;

public class PiglistCommand {

  private static void sendMessage(
    CommandContext<CommandSourceStack> ctx,
    int x,
    int y,
    int z,
    String world,
    String playerName)
  {
    try {
      Commands cm = new Commands(
        Commands.CommandSelection.ALL,
        Commands.createValidationContext(VanillaRegistries.createLookup())
      );

      String command = "tellraw " +
        ctx.getSource().getPlayer().getScoreboardName() +
        " {\"text\":\"" +
        "(" +
        playerName +
        ": " +
        x +
        ", " +
        y +
        ", " +
        z +
        ", " +
        world +
        ")" +
        "\",\"color\":\"blue\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"" +
        "/execute in " +
        world +
        " run tp @p " +
        x +
        " " +
        y +
        " " +
        z +
        "\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"" +
        playerName +
        "\"}]}}";

      System.out.println(command);

      cm.getDispatcher().execute(command, ctx.getSource().getServer().createCommandSourceStack());
    } catch (CommandSyntaxException e) {
      //            e.printStackTrace();
    }
  }

  public static void init() {
    // List all player pigs in memory
    CommandRegistrationCallback.EVENT.register((dispatcher, dedicated, environment) -> dispatcher.register(
      Commands.literal("piglist").executes(ctx -> {

        if (ctx.getSource().getPlayer().permissions().hasPermission(Permissions.COMMANDS_ADMIN) ||
          ctx.getSource().getPlayer().getName().equals("HughBone"))
        { // HughBone is here for debugging
          Thread thread = new Thread() {
            public void run() {
              boolean playerPigsFound = false;

              outer:
              for (List<String> unloadedPiggy : PPUtil.UnloadedPigList) {

                for (Pig pigInList : PPUtil.pigList.values()) {
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
                }
                sendMessage(ctx, posX, posY, posZ, dimension, playerName);
                playerPigsFound = true;
              }

              for (Pig pigInList : PPUtil.pigList.values()) {
                sendMessage(
                  ctx,
                  pigInList.getBlockX(),
                  pigInList.getBlockY(),
                  pigInList.getBlockZ(),
                  pigInList.level().dimension().identifier().toString(),
                  ((PlayerPigExt) pigInList).getPlayerName()
                );
                playerPigsFound = true;
              }
              // Success messages
              if (!playerPigsFound) {
                ctx
                  .getSource()
                  .getPlayer()
                  .displayClientMessage(
                    Component.nullToEmpty("[PlayerPig] No player pigs found."),
                    false
                  );
              }
            }
          };

          thread.start();
        } else {
          ctx
            .getSource()
            .getPlayer()
            .displayClientMessage(
              Component.nullToEmpty(
                "[PlayerPig] You need OP to use this command."), false
            );
        }
        return 1;
      })));
  }

}
