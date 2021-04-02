package com.hughbone.playerpig.piglist;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class LoadPigList {

    public static void playerNotInPigList(ServerPlayerEntity player) throws IOException {
        try {
            Files.createDirectory(Paths.get(System.getProperty("user.dir") + File.separator + "mods" + File.separator + "PlayerPig_Data"));
        } catch(Exception e){}

        try {
            File dir = new File(System.getProperty("user.dir") + File.separator + "mods" + File.separator + "PlayerPig_Data");
            File[] listDir = dir.listFiles();
            if (listDir != null) {
                for (File child : listDir) {
                    if (child.getName().toString().contains(player.getUuidAsString())) {
                        String fineLine;
                        double posX = 0;
                        double posY = 0;
                        double posZ = 0;
                        BufferedReader file = new BufferedReader(new FileReader(child));

                        for (int i = 0; i<4; i++) {
                            fineLine = file.readLine();
                            if (i == 0) {
                                posX = Double.parseDouble(fineLine);
                            }
                            else if (i == 1) {
                                posY = Double.parseDouble(fineLine);
                            }
                            else if (i == 2) {
                                posZ = Double.parseDouble(fineLine);
                            }
                            else if (i == 3){
                                if (fineLine.contains("overworld")) {
                                    player.teleport(player.getServer().getWorld(ServerWorld.OVERWORLD), posX, posY, posZ, player.yaw, player.pitch);
                                    player.updatePosition(posX, posY, posZ);
                                    player.updateTrackedPosition(posX, posY, posZ);
                                    break;
                                }
                                else if (fineLine.contains("the_nether")) {
                                    player.teleport(player.getServer().getWorld(ServerWorld.NETHER), posX, posY, posZ, player.yaw, player.pitch);
                                    player.updatePosition(posX, posY, posZ);
                                    player.updateTrackedPosition(posX, posY, posZ);
                                    break;
                                }
                                else if (fineLine.contains("the_end")) {
                                    player.teleport(player.getServer().getWorld(ServerWorld.END), posX, posY, posZ, player.yaw, player.pitch);
                                    player.updatePosition(posX, posY, posZ);
                                    player.updateTrackedPosition(posX, posY, posZ);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        } catch(Exception e){
            System.out.println(e);
        }
    }
}
