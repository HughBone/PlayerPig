package com.hughbone.playerpig;

import com.hughbone.playerpig.commands.PigfixCommand;
import com.hughbone.playerpig.commands.PiglistCommand;
import com.hughbone.playerpig.commands.PigremoveallCommand;
import com.hughbone.playerpig.events.*;
import net.fabricmc.api.ModInitializer;

public class PlayerPig implements ModInitializer {

    @Override
    public void onInitialize() {
        // Load Commands
        PigfixCommand.init();
        PiglistCommand.init();
        PigremoveallCommand.init();

        // Load Events
        DisconnectEvent.init();
        EntityLoadEvent.init();
        JoinEvent.init();
    }
}
