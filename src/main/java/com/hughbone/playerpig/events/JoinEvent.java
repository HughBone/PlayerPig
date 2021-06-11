package com.hughbone.playerpig.events;

import com.hughbone.playerpig.PlayerPigExt;
import com.hughbone.playerpig.util.PPUtil;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameRules;

import java.util.List;

public class JoinEvent {
    public static void init() {
        ServerPlayConnectionEvents.JOIN.register((handler, packetSender, server) -> {

            if (!handler.player.isSpectator()) {
                Runnable begin = new JoinThread(handler.player);
                new Thread(begin).start(); // Teleport player to pig and remove pig
            }
        });
    }

    public static class JoinThread implements Runnable {
        private ServerPlayerEntity player;
        private boolean joinSuccess = false;

        public JoinThread(ServerPlayerEntity player) {
            this.player = player;
        }

        public void killPig(ServerPlayerEntity player) {
            try {
                // Kill matching player pig within 10 blocks
                List<Entity> eList = player.world.getOtherEntities(player, player.getBoundingBox().expand(10, 10, 10));
                for (Entity entity : eList) {
                    if (entity.getType().equals(EntityType.PIG)) {
                        if (((PlayerPigExt) entity).isPlayerPig()) {
                            PigEntity piggy = (PigEntity) entity;
                            if (((PlayerPigExt) piggy).getPlayerUUID().equals(player.getUuidAsString())) {
                                // Drop lead if pig was leaded
                                if (piggy.isLeashed()) {
                                    ItemEntity item = EntityType.ITEM.create(player.world);
                                    item.setStack(new ItemStack(Items.LEAD, 1));
                                    item.updatePosition(piggy.getX(), piggy.getY(), piggy.getZ());
                                    item.updateTrackedPosition(piggy.getX(), piggy.getY(), piggy.getZ());
                                    player.world.spawnEntity(item);
                                }
                                // Give player fire resistance for 20 seconds if on fire
                                if (piggy.isOnFire()) {
                                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 400, 0, false, false));
                                }
                                piggy.remove(Entity.RemovalReason.KILLED); // Kill pig
                                PPUtil.getList().remove(piggy); // Remove from PigList
                                joinSuccess = true;
                                break;
                            }
                        }
                    }
                }
            } catch (Exception e) {}
        }

        public boolean teleportPlayer(ServerPlayerEntity player) {

            for (PigEntity pigInList : PPUtil.getList()) {
                if (((PlayerPigExt) pigInList).getPlayerUUID().equals(player.getUuidAsString())) {
                    try {
                        CommandManager cm = new CommandManager(CommandManager.RegistrationEnvironment.ALL);

                        boolean sendCommandFB = player.getServer().getGameRules().get(GameRules.SEND_COMMAND_FEEDBACK).get(); // original value
                        player.getServer().getGameRules().get(GameRules.SEND_COMMAND_FEEDBACK).set(false, player.getServer());

                        cm.getDispatcher().execute("execute in " + pigInList.world.getRegistryKey().getValue().toString() + " run tp "
                                        + player.getEntityName() + " " + pigInList.getX() + " " + pigInList.getY() + " " + pigInList.getZ()
                                , player.getServer().getCommandSource());
                        player.getServer().getGameRules().get(GameRules.SEND_COMMAND_FEEDBACK).set(sendCommandFB, player.getServer());
                        return true;

                    } catch (CommandSyntaxException e){}
                }
            }
            return false;
        }

        public void run() {
            PPUtil.joinNoCollision(player, player.getServer()); // Stop player from getting pushed by pig
            while(!joinSuccess) {
                try {
                    Thread.sleep(200);
                    // Teleport if matching player pig exists, if not then break
                    if (!teleportPlayer(player)) break;
                    Thread.sleep(50);
                    killPig(player); // Kill matching pig
                    if (joinSuccess) break;
                    Thread.sleep(2000);
                } catch (InterruptedException e) { break; }
            }
            PPUtil.leaveNoCollision(player, player.getServer()); // Let player get pushed again

            // Fix if player is suffocating
            while (player.isInsideWall() && joinSuccess) {
                double newY = player.getY() + 10;
                player.updatePosition(player.getX(), newY, player.getZ());
                player.updateTrackedPosition(player.getX(), newY, player.getZ());
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) { break; }
            }
        }
    }
}
