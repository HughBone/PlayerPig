package com.hughbone.playerpig.events;

import com.hughbone.playerpig.util.PPUtil;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class JoinEvent {

  private static void killPig(ServerPlayer player) {
    final String playerUUID = player.getStringUUID();
    Pig matchingPig = PPUtil.pigList.get(playerUUID);

    if (matchingPig != null) {
      // Mount player to what playerpig is riding
      if (matchingPig.isPassenger()) {
        player.startRiding(matchingPig.getVehicle(), true, true);
      }
      // Drop lead if pig was leaded
      if (matchingPig.isLeashed()) {
        ItemEntity item = EntityType.ITEM.create(player.level(), EntitySpawnReason.MOB_SUMMONED);
        item.setItem(new ItemStack(Items.LEAD, 1));
        item.absSnapTo(matchingPig.getX(), matchingPig.getY(), matchingPig.getZ());
        item.syncPacketPositionCodec(matchingPig.getX(), matchingPig.getY(), matchingPig.getZ());
        player.level().addFreshEntity(item);
      }
      // Give player fire resistance for 20 seconds if on fire
      if (matchingPig.isOnFire()) {
        player.addEffect(new MobEffectInstance(
          MobEffects.FIRE_RESISTANCE,
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
        ServerPlayer pl = (ServerPlayer) handler.player;
        killPig(pl);
        PPUtil.removeFile(pl.getStringUUID());
      }
    });
  }

}
