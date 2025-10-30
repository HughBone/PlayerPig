package com.hughbone.playerpig.events;

import com.hughbone.playerpig.util.PPUtil;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;

public class JoinEvent {

  private static void killPig(ServerPlayerEntity player) {
    final String playerUUID = player.getUuidAsString();
    PigEntity matchingPig = PPUtil.pigList.get(playerUUID);

    if (matchingPig != null) {
      // Mount player to what playerpig is riding
      if (matchingPig.hasVehicle()) {
        player.startRiding(matchingPig.getVehicle(), true, true);
      }
      // Drop lead if pig was leaded
      if (matchingPig.isLeashed()) {
        ItemEntity item = EntityType.ITEM.create(player.getEntityWorld(), SpawnReason.MOB_SUMMONED);
        item.setStack(new ItemStack(Items.LEAD, 1));
        item.updatePosition(matchingPig.getX(), matchingPig.getY(), matchingPig.getZ());
        item.updateTrackedPosition(matchingPig.getX(), matchingPig.getY(), matchingPig.getZ());
        player.getEntityWorld().spawnEntity(item);
      }
      // Give player fire resistance for 20 seconds if on fire
      if (matchingPig.isOnFire()) {
        player.addStatusEffect(new StatusEffectInstance(
          StatusEffects.FIRE_RESISTANCE,
          20 * 20,
          0,
          false,
          false
        ));
      }

      PPUtil.pigList.remove(playerUUID);
      // Remove pig if inside loaded chunks
      if (!matchingPig.isRemoved()) {
        matchingPig.discard();
      }
    }
  }

  public static void init() {
    ServerPlayConnectionEvents.JOIN.register((handler, packetSender, server) -> {
      if (!handler.player.isSpectator()) {
        ServerPlayerEntity pl = (ServerPlayerEntity) handler.player;
        killPig(pl);
        PPUtil.removeFile(pl.getUuidAsString());
      }
    });
  }

}
