package com.hughbone.playerpig.util;

import com.hughbone.playerpig.PlayerPigExt;
import com.hughbone.playerpig.commands.PigremoveallCommand;
import com.hughbone.playerpig.piglist.LoadPigList;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.ServerLevelAccessor;

public class PPUtil {

  public static boolean serverStopping = false;
  public static HashMap<String, Pig> pigList = new HashMap<>();
  public static List<List<String>> UnloadedPigList = LoadPigList.getAllData();

  public static void createDataFolder() {
    try {
      Files.createDirectory(Paths.get(System.getProperty("user.dir") +
        File.separator +
        "mods" +
        File.separator +
        "PlayerPig_Data"));
    } catch (Exception e) {
    }
  }

  public static void deleteAllFiles() {
    createDataFolder();
    try {
      File dir = new File(System.getProperty("user.dir") +
        File.separator +
        "mods" +
        File.separator +
        "PlayerPig_Data");
      File[] listDir = dir.listFiles();
      if (listDir != null) {
        for (File child : listDir) {
          child.delete();
        }
      }
    } catch (Exception e) {
    }
  }

  public static void removeFile(String filename) {
    File f = new File(System.getProperty("user.dir") +
      File.separator +
      "mods" +
      File.separator +
      "PlayerPig_Data" +
      File.separator +
      filename);
    f.delete();
  }

  public static void loadPPDataChunks(MinecraftServer server, String dimension, int posX, int PosZ)
  {
    try {
      Commands cm = new Commands(
        Commands.CommandSelection.ALL,
        Commands.createValidationContext(VanillaRegistries.createLookup())
      );

      //      boolean sendCommandFB =
      //        server.getGameRules().getRule(GameRules.SEND_COMMAND_FEEDBACK).get(); // original
      //        value
      //      server
      //        .getGameRules()
      //        .getRule(GameRules.SEND_COMMAND_FEEDBACK)
      //        .set(false, server); // set to false

      cm.getDispatcher().execute(
        "execute in " + dimension + " run forceload add " + posX + " " + PosZ,
        server.createCommandSourceStack()
      );
      Thread.sleep(500);
      cm.getDispatcher().execute(
        "execute in " + dimension + " run forceload remove " + posX + " " + PosZ,
        server.createCommandSourceStack()
      );

      //      server
      //        .getGameRules()
      //        .getRule(GameRules.SEND_COMMAND_FEEDBACK)
      //        .set(sendCommandFB, server); // reset to original
    } catch (InterruptedException | CommandSyntaxException e) {
    }
  }

  public static void spawnPlayerPig(ServerPlayer player) {
    if (PPUtil.pigList.get(player.getStringUUID()) != null ||
      player.isSpectator() ||
      !PigremoveallCommand.allowPPSpawn)
    {
      return;
    }

    ServerLevelAccessor world = (ServerLevelAccessor) player.level();
    Pig playerPig = EntityType.PIG.create(world.getLevel(), EntitySpawnReason.MOB_SUMMONED);
    if (playerPig == null) {
      return;
    }

    PlayerPigExt playerPig1 = (PlayerPigExt) playerPig;
    playerPig1.setPlayerPig(true);
    playerPig1.setPlayerName(player.getName().toString());
    playerPig1.setPlayerUUID(player.getUUID().toString());

    BlockPos playerPos = player.blockPosition();
    playerPig.snapTo(playerPos, player.getYRot(), player.getXRot());
    world.addFreshEntityWithPassengers(playerPig);

    // Set display name, make silent, make invincible, add portal cooldown
    playerPig.setCustomNameVisible(true);
    playerPig.setCustomName(player.getName());
    playerPig.setSilent(true);
    playerPig.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 2147483647, 5, false, false));
    playerPig.setPortalCooldown();
    playerPig.setItemSlot(EquipmentSlot.SADDLE, new ItemStack(Items.SADDLE));

    ItemStack skull = Items.PLAYER_HEAD.getDefaultInstance();
    skull.set(DataComponents.PROFILE, ResolvableProfile.createResolved(player.getGameProfile()));
    playerPig.setItemSlot(EquipmentSlot.HEAD, skull);

    // Mount pig to entity player was riding
    if (player.isPassenger()) {
      playerPig.startRiding(player.getVehicle(), true, true);
      player.removeVehicle();
    }
  }

}
