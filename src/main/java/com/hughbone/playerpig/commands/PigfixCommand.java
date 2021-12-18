package com.hughbone.playerpig.commands;

import com.hughbone.playerpig.PlayerPigExt;
import com.hughbone.playerpig.util.PPUtil;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.LiteralText;

import java.util.List;

public class PigfixCommand {

    public static void init() {
        // Kills one player pig within 4 blocks of the player
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> dispatcher.register(CommandManager.literal("pigfix")
                .requires(source -> source.hasPermissionLevel(4))
                .executes(ctx -> {
                    PlayerEntity player = ctx.getSource().getPlayer();
                    List<Entity> eList = player.world.getOtherEntities(player, player.getBoundingBox().expand(4, 4, 4));
                    for (Entity entity : eList) {
                        if (entity.getType().equals(EntityType.PIG)) {
                            PigEntity nearbyPig = (PigEntity) entity;
                            if (((PlayerPigExt) nearbyPig).isPlayerPig()) {
                                nearbyPig.remove(Entity.RemovalReason.DISCARDED);
                                PPUtil.removeFile(((PlayerPigExt) nearbyPig).getPlayerUUID());
                                PPUtil.getPigList().remove(nearbyPig);
                                ctx.getSource().getPlayer().sendMessage(new LiteralText("[PlayerPig] Piggy removed successfully."), false);
                                return 1;
                            }
                        }
                    }
                    ctx.getSource().getPlayer().sendMessage(new LiteralText("[PlayerPig] No player pigs found."), false);
                    return 1;
                })));
    }
}
