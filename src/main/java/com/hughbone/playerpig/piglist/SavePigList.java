package com.hughbone.playerpig.piglist;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.hughbone.playerpig.PlayerPigExt;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.world.World;

// Ignore the error
public class SavePigList extends PigEntity{

    public SavePigList(EntityType<? extends PigEntity> entityType, World world) {
        super(entityType, world);
    }

    public static void save() throws IOException {

        try {
            Files.createDirectory(Paths.get(System.getProperty("user.dir") + File.separator + "mods" + File.separator + "PlayerPig_Data"));
        } catch(Exception e){}

        for (PigEntity piggy: PigList.getList()) {
            try {
                BufferedWriter file = new BufferedWriter(
                        new FileWriter(System.getProperty("user.dir") + File.separator + "mods" + File.separator + "PlayerPig_Data" + File.separator + ((PlayerPigExt) piggy).getPlayerUUID()));
                file.write(piggy.getX() + "\n");
                file.write(piggy.getPos().getY() + "\n");
                file.write(piggy.getPos().getZ() + "\n");
                file.write(piggy.world.getRegistryKey().getValue().toString());
                file.close();
            } catch (Exception e){}
        }
    }
}
