package com.hughbone.playerpig.piglist;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class LoadPigList {

    private static void createDataFolder() {
        try {
            Files.createDirectory(Paths.get(System.getProperty("user.dir") + File.separator + "mods" + File.separator + "PlayerPig_Data"));
        } catch (Exception e) {}
    }

    /* OLD FUNCTIONALITY - NOT NECCESSARY BECAUSE ALL UNLOADED PIGS GET STORED IN MEMORY AT START EVENT
    public static void playerNotInPigList(ServerPlayerEntity player) throws IOException {
        createDataFolder();
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

                        for (int i = 0; i < 4; i++) {
                            fineLine = file.readLine();
                            if (i == 0) {
                                posX = Double.parseDouble(fineLine);
                            } else if (i == 1) {
                                posY = Double.parseDouble(fineLine);
                            } else if (i == 2) {
                                posZ = Double.parseDouble(fineLine);
                            } else if (i == 3) {
                                if (fineLine.contains("overworld")) {
                                    player.teleport(player.getServer().getWorld(ServerWorld.OVERWORLD), posX, posY, posZ, player.yaw, player.pitch);
                                } else if (fineLine.contains("the_nether")) {
                                    player.teleport(player.getServer().getWorld(ServerWorld.NETHER), posX, posY, posZ, player.yaw, player.pitch);
                                } else if (fineLine.contains("the_end")) {
                                    player.teleport(player.getServer().getWorld(ServerWorld.END), posX, posY, posZ, player.yaw, player.pitch);
                                }
                                player.updatePosition(posX, posY, posZ);
                                player.updateTrackedPosition(posX, posY, posZ);
                                break;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }
     */

    public static void deleteAll() {
        createDataFolder();
        try {
            File dir = new File(System.getProperty("user.dir") + File.separator + "mods" + File.separator + "PlayerPig_Data");
            File[] listDir = dir.listFiles();
            if (listDir != null) {
                for (File child : listDir) {
                    child.delete();
                }
            }
        } catch (Exception e) {}
    }

    public static List<List<String>> getAllData() {
        createDataFolder();
        List<List<String>> pigDataList = new ArrayList<List<String>>();
        try {
            File dir = new File(System.getProperty("user.dir") + File.separator + "mods" + File.separator + "PlayerPig_Data");
            File[] listDir = dir.listFiles();
            if (listDir != null) {
                for (File child : listDir) {
                    String fineLine;
                    double posX = 0;
                    double posY = 0;
                    double posZ = 0;
                    BufferedReader file = new BufferedReader(new FileReader(child));
                    for (int i = 0; i < 4; i++) {
                        fineLine = file.readLine();
                        if (i == 0) {
                            posX = Double.parseDouble(fineLine);
                        } else if (i == 1) {
                            posY = Double.parseDouble(fineLine);
                        } else if (i == 2) {
                            posZ = Double.parseDouble(fineLine);
                        } else {
                            List<String> temp = new ArrayList<String>();
                            temp.add(""+ posX);
                            temp.add(""+ posY);
                            temp.add(""+ posZ);
                            if (fineLine.contains("overworld")) {
                                temp.add("overworld");
                            } else if (fineLine.contains("the_nether")) {
                                temp.add("the_nether");
                            } else if (fineLine.contains("the_end")) {
                                temp.add("the_end");
                            }
                            pigDataList.add(temp);
                        }
                    }
                }
            }
        } catch (Exception e) {}
        return pigDataList;
    }

}
