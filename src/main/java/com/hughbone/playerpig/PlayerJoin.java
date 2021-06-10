package com.hughbone.playerpig;

import com.hughbone.playerpig.events.ServerStartedEvent;
import com.hughbone.playerpig.piglist.PigList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;

public class PlayerJoin {
    private boolean joinSuccess = false;

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
                            PigList.getList().remove(piggy); // Remove from PigList
                            joinSuccess = true;
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {}
    }

    public void teleportPlayer(ServerPlayerEntity player) {
        for (PigEntity pigInList : PigList.getList()) {
            if (((PlayerPigExt) pigInList).getPlayerUUID().equals(player.getUuidAsString())) {
                player.teleport(player.getServer().getWorld(pigInList.getEntityWorld().getRegistryKey()), pigInList.getX(), pigInList.getY(), pigInList.getZ(), player.getYaw(), player.getPitch());
                player.updatePosition(pigInList.getX(), pigInList.getY(), pigInList.getZ());
                player.updateTrackedPosition(pigInList.getX(), pigInList.getY(), pigInList.getZ());
                break;
            }
        }
    }

    public class JoinThread implements Runnable {
        private ServerPlayerEntity player;

        public JoinThread(ServerPlayerEntity player) {
            this.player = player;
        }

        public void run() {
            ServerStartedEvent.teamScoreboard.addPlayerToTeam(player.getEntityName(), ServerStartedEvent.noCollision); // Stop player from getting pushed by pig
            for (int i = 0; i < 4; i++) {
                try {
                    Thread.sleep(1000);
                    teleportPlayer(player); // Teleport to pig
                    Thread.sleep(50);
                    killPig(player); // Kill matching pig
                    if (joinSuccess) break;
                    Thread.sleep(1000);
                } catch (InterruptedException e) {}
            }

            ServerStartedEvent.teamScoreboard.removePlayerFromTeam(player.getEntityName(), ServerStartedEvent.noCollision); // Let player get pushed again
            // Fix if player is suffocating
            while (player.isInsideWall()) {
                double newY = player.getY() + 10;
                player.updatePosition(player.getX(), newY, player.getZ());
                player.updateTrackedPosition(player.getX(), newY, player.getZ());
            }
            /* OLD FUNCTIONALITY
            // Remove file from PlayerPig_Data
            try {
                RemoveFile.remove(player.getUuidAsString());
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Remove all matching from PigList + in world
            for (PigEntity pigInList: PigList.getList()) {
                if (((PlayerPigExt) pigInList).getPlayerUUID().equals(player.getUuidAsString())) {
                    pigInList.remove();
                    PigList.getList().remove(pigInList);
                }
            }
             */
        }
    }

    public void join(ServerPlayerEntity player) {
        Runnable begin = new JoinThread(player);
        new Thread(begin).start();
    }
}