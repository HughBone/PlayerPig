package com.hughbone.playerpig.events;

import com.hughbone.playerpig.PlayerExt;
import com.hughbone.playerpig.PlayerPigExt;
import com.hughbone.playerpig.util.PPUtil;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;

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

        public JoinThread(ServerPlayerEntity player) {
            this.player = player;
        }

        public void killPig() {
            final String playerUUID = player.getUuidAsString();
            for (PigEntity piggy : PPUtil.getPigList()) {
                if (((PlayerPigExt) piggy).getPlayerUUID().equals(playerUUID)) {
                    if (((PlayerPigExt) piggy).getPlayerUUID().equals(player.getUuidAsString())) {

                        // Mount player to what playerpig is riding
                        if (piggy.hasVehicle()) {
                            player.startRiding(piggy.getVehicle(),true);
                        }

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
                        piggy.remove(Entity.RemovalReason.DISCARDED); // Kill pig
                        PPUtil.getPigList().remove(piggy); // Remove from PigList
                        return;
                    }
                }
            }
        }

        public void run() {
            ((PlayerExt) player).setJustJoined(true);
            killPig();
            PPUtil.removeFile(player.getUuidAsString());
        }

    }
}
