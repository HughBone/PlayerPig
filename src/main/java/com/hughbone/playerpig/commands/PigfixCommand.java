package com.hughbone.playerpig.commands;

import com.hughbone.playerpig.PlayerPigExt;
import com.hughbone.playerpig.util.PPUtil;
import java.util.List;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.player.Player;

public class PigfixCommand {

  public static void init() {
    // Kills one player pig within 4 blocks of the player
    CommandRegistrationCallback.EVENT.register((dispatcher, regirstryAccess, environment) -> {
      dispatcher.register(Commands
        .literal("pigfix")
        .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_ADMIN))
        .executes(ctx -> {
          Player player = ctx.getSource().getPlayer();
          if (player == null) {
            return 0;
          }

          List<Entity> eList =
            player.level().getEntities(player, player.getBoundingBox().inflate(4, 4, 4));
          for (Entity entity : eList) {
            if (entity.getType().equals(EntityType.PIG)) {
              Pig nearbyPig = (Pig) entity;
              if (((PlayerPigExt) nearbyPig).isPlayerPig()) {
                nearbyPig.remove(Entity.RemovalReason.DISCARDED);

                final String playerUUID = ((PlayerPigExt) nearbyPig).getPlayerUUID();
                PPUtil.removeFile(playerUUID);
                PPUtil.pigList.remove(playerUUID);
                ctx
                  .getSource()
                  .getPlayer()
                  .displayClientMessage(
                    Component.nullToEmpty(
                      "[PlayerPig] Piggy removed successfully."), false
                  );
                return 1;
              }
            }
          }
          ctx
            .getSource()
            .getPlayer()
            .displayClientMessage(
              Component.nullToEmpty("[PlayerPig] No player pigs found."),
              false
            );
          return 1;
        }));
    });
  }

}
