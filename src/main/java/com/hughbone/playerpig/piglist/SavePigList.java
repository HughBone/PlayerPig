package com.hughbone.playerpig.piglist;

import com.hughbone.playerpig.PlayerPigExt;
import com.hughbone.playerpig.util.PPUtil;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.level.Level;

//@Environment(EnvType.SERVER)
public class SavePigList extends Pig {

  public SavePigList(EntityType<? extends Pig> entityType, Level world) {
    super(entityType, world);
  }

  public static void save() throws IOException {
    try {
      Files.createDirectory(Paths.get(System.getProperty("user.dir") +
        File.separator +
        "mods" +
        File.separator +
        "PlayerPig_Data"));
    } catch (Exception e) {
    }

    for (Pig piggy : PPUtil.pigList.values()) {
      try {
        // File name is the pig's UUID
        BufferedWriter file = new BufferedWriter(new FileWriter(System.getProperty("user.dir") +
          File.separator +
          "mods" +
          File.separator +
          "PlayerPig_Data" +
          File.separator +
          ((PlayerPigExt) piggy).getPlayerUUID()));
        file.write(piggy.getX() + "\n");
        file.write(piggy.getY() + "\n");
        file.write(piggy.getZ() + "\n");
        file.write(piggy.level().dimension().identifier().toString() + "\n");
        file.write(((PlayerPigExt) piggy).getPlayerName());
        file.close();
      } catch (Exception e) {
      }
    }
  }

}
