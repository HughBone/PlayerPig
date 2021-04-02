package com.hughbone.playerpig;

import com.hughbone.playerpig.piglist.PigList;
import com.hughbone.playerpig.piglist.RemoveFile;
import com.hughbone.playerpig.piglist.LoadPigList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.server.network.ServerPlayerEntity;

import java.io.IOException;
import java.util.List;

public class PlayerJoin {
    private boolean joinSuccess = false;

    public void killPig(ServerPlayerEntity player) {
        try {
            List<Entity> eList = player.world.getOtherEntities(player, player.getBoundingBox().expand(10, 10, 10));

            for (Entity entity: eList) {
                if (entity.getType().equals(EntityType.PIG)) {
                    if (((PlayerPigExt) entity).isPlayerPig()) {
                        PigEntity piggy = (PigEntity) entity;
                        if (((PlayerPigExt) piggy).getPlayerUUID().equals(player.getUuidAsString())) {
                            if (piggy.isOnFire()) {
                                player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 600, 0, false, false));
                            }
                            piggy.remove();
                            PigList.getList().remove(piggy);
                            joinSuccess = true;
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {}
    }

    public void teleportPlayer(ServerPlayerEntity player) {
        boolean inPigList = false;
        for (PigEntity pigInList: PigList.getList()) {
            if (((PlayerPigExt) pigInList).getPlayerUUID().equals(player.getUuidAsString())) {
                player.teleport(player.getServer().getWorld(pigInList.getEntityWorld().getRegistryKey()), pigInList.getX(), pigInList.getY(), pigInList.getZ(), player.yaw, player.pitch);
                player.updatePosition(pigInList.getX(), pigInList.getY(), pigInList.getZ());
                player.updateTrackedPosition(pigInList.getX(), pigInList.getY(), pigInList.getZ());
                inPigList = true;
                break;
            }
        }
        if (!inPigList) {
            try {
                LoadPigList.playerNotInPigList(player); // Tries to teleport to position saved in PlayerPig_Data
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class JoinThread implements Runnable {
        private ServerPlayerEntity player;

        public JoinThread(ServerPlayerEntity player) {
            this.player = player;
        }

        public void run() {
            for (int i = 0; i < 4; i++) {
                try {
                    Thread.sleep(1000);
                    teleportPlayer(player); // Teleport
                    Thread.sleep(50);
                    killPig(player); // Kill matching pigs near player
                    if (joinSuccess) break;
                    Thread.sleep(1000);
                } catch (InterruptedException e) {}
            }
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
            // Fix if player is suffocating
            while (player.isInsideWall()) {
                double newY = player.getY() + 10;
                player.updatePosition(player.getX(), newY, player.getZ());
                player.updateTrackedPosition(player.getX(), newY, player.getZ());
            }
        }
    }

    public void join(ServerPlayerEntity player) {
        Runnable begin = new JoinThread(player);
        new Thread(begin).start();
    }
}