package com.hughbone.playerpig;

import com.hughbone.playerpig.commands.PigfixCommand;
import com.hughbone.playerpig.commands.PiglistCommand;
import com.hughbone.playerpig.events.DisconnectEvent;
import com.hughbone.playerpig.events.EntityLoadEvent;
import com.hughbone.playerpig.events.JoinEvent;
import com.hughbone.playerpig.events.ServerStoppingEvent;
import net.fabricmc.api.ModInitializer;

public class PlayerPig implements ModInitializer {

    @Override
    public void onInitialize() {
        // Load Commands
        PigfixCommand.init();
        PiglistCommand.init();

        // Load Events
        ServerStoppingEvent.init();
        DisconnectEvent.init();
        EntityLoadEvent.init();
        JoinEvent.init();
    }
}
