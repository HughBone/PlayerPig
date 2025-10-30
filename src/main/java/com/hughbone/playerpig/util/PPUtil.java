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
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.BuiltinRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.ServerWorldAccess;

public class PPUtil {

  public static boolean serverStopping = false;
  public static HashMap<String, PigEntity> pigList = new HashMap<>();
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

  public static void loadPPDataChunks(
    MinecraftServer server,
    String dimension,
    int posX,
    int PosZ)
  {
    try {
      CommandManager cm = new CommandManager(
        CommandManager.RegistrationEnvironment.ALL,
        CommandManager.createRegistryAccess(BuiltinRegistries.createWrapperLookup())
      );
      boolean sendCommandFB =
        server.getGameRules().get(GameRules.SEND_COMMAND_FEEDBACK).get(); // original value
      server.getGameRules().get(GameRules.SEND_COMMAND_FEEDBACK).set(false, server); // set to false

      cm
        .getDispatcher()
        .execute(
          "execute in " + dimension + " run forceload add " + posX + " " + PosZ,
          server.getCommandSource()
        );
      Thread.sleep(500);
      cm
        .getDispatcher()
        .execute(
          "execute in " + dimension + " run forceload remove " + posX + " " + PosZ,
          server.getCommandSource()
        );

      server
        .getGameRules()
        .get(GameRules.SEND_COMMAND_FEEDBACK)
        .set(sendCommandFB, server); // reset to original
    } catch (InterruptedException | CommandSyntaxException e) {
    }
  }

  public static void spawnPlayerPig(ServerPlayerEntity player) {
    if (PPUtil.pigList.get(player.getUuidAsString()) != null ||
      player.isSpectator() ||
      !PigremoveallCommand.allowPPSpawn)
    {
      return;
    }

    ServerWorldAccess world = (ServerWorldAccess) player.getEntityWorld();
    PigEntity playerPig = EntityType.PIG.create(world.toServerWorld(), SpawnReason.MOB_SUMMONED);
    if (playerPig == null) {
      return;
    }

    PlayerPigExt playerPig1 = (PlayerPigExt) playerPig;
    playerPig1.setPlayerPig(true);
    playerPig1.setPlayerName(player.getName().toString());
    playerPig1.setPlayerUUID(player.getUuid().toString());

    BlockPos playerPos = player.getBlockPos();
    playerPig.refreshPositionAndAngles(playerPos, player.getYaw(), player.getPitch());
    world.spawnEntityAndPassengers(playerPig);

    // Set display name, make silent, make invincible, add portal cooldown
    playerPig.setCustomNameVisible(true);
    playerPig.setCustomName(player.getName());
    playerPig.setSilent(true);
    playerPig.addStatusEffect(new StatusEffectInstance(
      StatusEffects.RESISTANCE,
      2147483647,
      5,
      false,
      false
    ));
    playerPig.resetPortalCooldown();
    playerPig.equipStack(EquipmentSlot.SADDLE, new ItemStack(Items.SADDLE));

    ItemStack skull = Items.PLAYER_HEAD.getDefaultStack();
    skull.set(DataComponentTypes.PROFILE, ProfileComponent.ofStatic(player.getGameProfile()));
    playerPig.equipStack(EquipmentSlot.HEAD, skull);

    // Mount pig to entity player was riding
    if (player.hasVehicle()) {
      playerPig.startRiding(player.getVehicle(), true, true);
      player.dismountVehicle();
    }
  }

}
