package com.hughbone.playerpig.piglist;

import java.io.File;
import java.io.IOException;

public class RemoveFile {

    public static void remove(String uuid) throws IOException {
        try {
            File dir = new File(System.getProperty("user.dir") + File.separator + "mods" + File.separator + "PlayerPig_Data");
            File[] listDir = dir.listFiles();
            if (listDir != null) {
                for (File child : listDir) {
                    if (child.getName().equals(uuid)) {
                        child.delete();
                        break;
                    }
                }
            }
        } catch(Exception e){
            System.out.println(e);
        }
    }
}
