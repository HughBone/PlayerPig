package com.hughbone.playerpig;

import com.hughbone.playerpig.commands.PigfixCommand;
import com.hughbone.playerpig.commands.PiglistCommand;
import com.hughbone.playerpig.commands.PigremoveallCommand;
import com.hughbone.playerpig.events.EntityLoadEvent;
import com.hughbone.playerpig.events.JoinEvent;
import com.hughbone.playerpig.util.PPUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class PlayerPig implements ModInitializer {

  @Override public void onInitialize() {
    if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
      PPUtil.createDataFolder();

      // Load Commands
      PigfixCommand.init();
      PiglistCommand.init();
      PigremoveallCommand.init();

      // Load Events
      EntityLoadEvent.init();
      JoinEvent.init();
    }
  }

}
