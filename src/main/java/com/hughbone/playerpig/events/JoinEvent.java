package com.hughbone.playerpig.events;

import com.hughbone.playerpig.PlayerPigExt;
import com.hughbone.playerpig.piglist.LoadPigList;
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
import net.minecraft.server.world.ServerWorld;
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
                                PPUtil.getPigList().remove(piggy); // Remove from PigList
                                joinSuccess = true;
                                break;
                            }
                        }
                    }
                }
            } catch (Exception e) {}
        }

        public boolean loadFromData() {
            List<List<String>> unloadedPigList = LoadPigList.getAllData();
            if (!unloadedPigList.isEmpty()) {
                CommandManager cm = new CommandManager(CommandManager.RegistrationEnvironment.ALL);
                for (List<String> unloadedPiggy : unloadedPigList) {
                    if (unloadedPiggy.get(4).equals(player.getUuidAsString())) {
                        int posX = (int) Double.parseDouble(unloadedPiggy.get(0));
                        int posZ = (int) Double.parseDouble(unloadedPiggy.get(2));

                        // Temporarily load chunk in correct dimension so EntityLoadEvent adds pig to PigList
                        Iterable<ServerWorld> worlds = player.getServer().getWorlds();
                        for (ServerWorld sw : worlds) {
                            String dimension = sw.getRegistryKey().getValue().toString();
                            if (unloadedPiggy.get(3).equals(dimension)) {
                                try {
                                    boolean sendCommandFB = player.getServer().getGameRules().get(GameRules.SEND_COMMAND_FEEDBACK).get(); // original value
                                    player.getServer().getGameRules().get(GameRules.SEND_COMMAND_FEEDBACK).set(false, player.getServer()); // set to false

                                    cm.getDispatcher().execute("execute in " + dimension + " run forceload add " + posX + " " + posZ, player.getServer().getCommandSource());
                                    Thread.sleep(250);
                                    cm.getDispatcher().execute("execute in " + dimension + " run forceload remove " + posX + " " + posZ, player.getServer().getCommandSource());

                                    player.getServer().getGameRules().get(GameRules.SEND_COMMAND_FEEDBACK).set(sendCommandFB, player.getServer()); // reset to original
                                } catch (InterruptedException | CommandSyntaxException e) {}

                                PPUtil.removeFile(unloadedPiggy.get(4)); // Remove PlayerPig_Data file
                                return true;
                            }
                        }
                    }
                }
            }
            return false;
        }

        public boolean teleportPlayer(ServerPlayerEntity player) {
            for (PigEntity pigInList : PPUtil.getPigList()) {
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
                    } catch (CommandSyntaxException e) {}
                }
            }
            return false;
        }

        public void run() {
            PPUtil.joinNoCollision(player, player.getServer()); // Stop player from getting pushed by pig

            try { Thread.sleep(250); } catch (InterruptedException e) {}
            while (!joinSuccess && !player.isDisconnected()) {
                try {

                    if (!teleportPlayer(player)) { // Try to teleport player
                        if (loadFromData()) { // If no PlayerPig found, force load chunk to store PlayerPig in PigList
                            if (!teleportPlayer(player)) { // Try to teleport again
                                break; // If PlayerPig still not found, break.
                            }
                        }
                    }

                    Thread.sleep(50);
                    killPig(player); // Kill matching pig
                    if (joinSuccess) break;
                    Thread.sleep(3000);
                } catch (InterruptedException e) {}
            }
            PPUtil.leaveNoCollision(player, player.getServer()); // Let player get pushed again

            // Fix if player is suffocating
            while (player.isInsideWall() && joinSuccess) {
                double newY = player.getY() + 10;
                player.updatePosition(player.getX(), newY, player.getZ());
                player.updateTrackedPosition(player.getX(), newY, player.getZ());
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }
}
