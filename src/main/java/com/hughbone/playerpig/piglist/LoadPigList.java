package com.hughbone.playerpig.piglist;

import com.hughbone.playerpig.util.PPUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class LoadPigList {

    public static List<List<String>> getAllData() {
        PPUtil.createDataFolder();
        List<List<String>> pigDataList = new ArrayList<List<String>>();
        try {
            File dir = new File(System.getProperty("user.dir") + File.separator + "mods" + File.separator + "PlayerPig_Data");
            File[] listDir = dir.listFiles();
            if (listDir != null) {
                for (File child : listDir) {
                    BufferedReader file = new BufferedReader(new FileReader(child));

                    List<String> temp = new ArrayList<String>();
                    try {
                        temp.add(file.readLine()); // X
                        temp.add(file.readLine()); // Y
                        temp.add(file.readLine()); // Z
                        temp.add(file.readLine()); // Dimension
                        temp.add(child.getName()); // Player UUID
                        temp.add(file.readLine()); // Player Name
                    } catch (Exception e) {}
                    pigDataList.add(temp);
                }
            }
        } catch (Exception e) {}
        return pigDataList;
    }

}
