package com.hughbone.playerpig.piglist;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.hughbone.playerpig.PlayerPigExt;
import com.hughbone.playerpig.util.PPUtil;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.world.World;

//@Environment(EnvType.SERVER)
public class SavePigList extends PigEntity{

    public SavePigList(EntityType<? extends PigEntity> entityType, World world) {
        super(entityType, world);
    }

    public static void save() throws IOException {
        try {
            Files.createDirectory(Paths.get(System.getProperty("user.dir") + File.separator + "mods" + File.separator + "PlayerPig_Data"));
        } catch(Exception e){}

        for (PigEntity piggy : PPUtil.pigList.values()) {
            try {
                // File name is the pig's UUID
                BufferedWriter file = new BufferedWriter(
                        new FileWriter(System.getProperty("user.dir") + File.separator + "mods" + File.separator + "PlayerPig_Data" + File.separator + ((PlayerPigExt) piggy).getPlayerUUID()));
                file.write(piggy.getX() + "\n");
                file.write(piggy.getPos().getY() + "\n");
                file.write(piggy.getPos().getZ() + "\n");
                file.write(piggy.getWorld().getRegistryKey().getValue().toString() + "\n");
                file.write(((PlayerPigExt) piggy).getPlayerName());
                file.close();
            } catch (Exception e){}
        }
    }

}
