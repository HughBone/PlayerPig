package com.hughbone.playerpig.commands;

import com.hughbone.playerpig.PlayerPigExt;
import com.hughbone.playerpig.util.PPUtil;
import java.util.List;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.level.GameRules;

public class PigremoveallCommand {

  public static boolean allowPPSpawn = true;

  public static void init() {

    // Kills one player pig within 4 blocks of the player
    CommandRegistrationCallback.EVENT.register((dispatcher, dedicated, environment) -> dispatcher.register(
      Commands
        .literal("pigremoveall")
        .requires(source -> source.hasPermission(4))
        .executes(ctx -> {
          Thread thread = new Thread() {
            public void run() {
              allowPPSpawn = false; // stop player pigs from spawning

              if (ctx.getSource().hasPermission(4)) {
                Commands cm = new Commands(
                  Commands.CommandSelection.ALL,
                  Commands.createValidationContext(VanillaRegistries.createLookup())
                );
                ServerPlayer player = null;
                player = ctx.getSource().getPlayer();
                Iterable<ServerLevel> worlds = player.level().getServer().getAllLevels();

                ctx
                  .getSource()
                  .getPlayer()
                  .displayClientMessage(
                    Component.nullToEmpty(
                      "[PlayerPig] Removing all PlayerPigs (This may take a while...)"), false
                  );

                // Load all in data folder
                for (List<String> unloadedPiggy : PPUtil.UnloadedPigList) {
                  int posX = (int) Double.parseDouble(unloadedPiggy.get(0));
                  int posZ = (int) Double.parseDouble(unloadedPiggy.get(2));

                  // Temporarily load chunk in correct dimension so EntityLoadEvent adds pig to
                  // PigList
                  for (ServerLevel sw : worlds) {
                    String dimension = sw.dimension().location().toString();
                    if (unloadedPiggy.get(3).equals(dimension)) {
                      PPUtil.loadPPDataChunks(ctx.getSource().getServer(), dimension, posX, posZ);
                      break;
                    }
                  }
                  try {
                    Thread.sleep(500);
                  } catch (InterruptedException e) {
                  }
                }

                // Kill all in piglist
                for (Pig piggy : PPUtil.pigList.values()) {
                  for (ServerLevel sw : worlds) {
                    String dimension = sw.dimension().location().toString();
                    if (piggy
                      .level()
                      .dimension()
                      .location()
                      .toString()
                      .equals(dimension))
                    {
                      try {
                        boolean sendCommandFB = player
                          .level()
                          .getGameRules()
                          .getRule(GameRules.RULE_SENDCOMMANDFEEDBACK)
                          .get(); // original value
                        player
                          .level()
                          .getGameRules()
                          .getRule(GameRules.RULE_SENDCOMMANDFEEDBACK)
                          .set(false, player.level().getServer()); // set to false

                        cm.getDispatcher().execute(
                          "execute in " +
                            dimension +
                            " run forceload add " +
                            (int) piggy.getX() +
                            " " +
                            (int) piggy.getZ(),
                          player.level().getServer().createCommandSourceStack()
                        );

                        Thread.sleep(250);
                        for (Pig piggy2 : PPUtil.pigList.values()) {
                          piggy2.remove(Entity.RemovalReason.KILLED);
                        }
                        Thread.sleep(250);
                        player.displayClientMessage(
                          Component.nullToEmpty("PlayerPig " +
                            ((PlayerPigExt) piggy).getPlayerName() +
                            " was removed."), false
                        );
                        cm.getDispatcher().execute(
                          "execute in " +
                            dimension +
                            " run forceload remove " +
                            (int) piggy.getX() +
                            " " +
                            (int) piggy.getZ(),
                          player.level().getServer().createCommandSourceStack()
                        );

                        player
                          .level()
                          .getGameRules()
                          .getRule(GameRules.RULE_SENDCOMMANDFEEDBACK)
                          .set(
                            sendCommandFB,
                            player.level().getServer()
                          ); // reset to original
                      } catch (Exception e) {
                      }
                    }
                  }
                }
                PPUtil.pigList.clear(); // Clear all elements from the list
                PPUtil.deleteAllFiles(); // delete straggler files
                ctx.getSource().getPlayer().displayClientMessage(
                  Component.nullToEmpty(
                    "[PlayerPig] All PlayerPigs removed. (PlayerPigs will not spawn until the " +
                      "server reloads.)"), false
                );
              }
            }
          };
          thread.start();
          return 1;
        })));
  }

}
