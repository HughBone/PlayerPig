package com.hughbone.playerpig.events;

import com.hughbone.playerpig.piglist.LoadPigList;
import com.hughbone.playerpig.piglist.PigList;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.world.ServerWorld;

import java.util.List;

// Saves all unloaded pigs into memory
public class ServerStartedEvent {

    private List<List<String>> unloadedPigList;
    private MinecraftServer server;

    public void init() {
        ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
            // Temporarily load chunks so that EntityLoadEvent can add pigs to PigList
            List<List<String>> unloadedPigList = LoadPigList.getAllData();
            if (!unloadedPigList.isEmpty()) {
                this.unloadedPigList = unloadedPigList;
                this.server = server;
                new ServerStartedThread().start();
            }
        });
    }

    public class ServerStartedThread extends Thread {
        private void loadUnloadChunk() {
            try {
                CommandManager cm = new CommandManager(CommandManager.RegistrationEnvironment.ALL);

                for (List<String> unloadedPiggy : unloadedPigList) {
                    int posX = (int) Double.parseDouble(unloadedPiggy.get(0));
                    int posZ = (int) Double.parseDouble(unloadedPiggy.get(2));

                    // Temporarily load chunk in correct dimension so EntityLoadEvent adds pig to PigList
                    Iterable<ServerWorld> worlds = server.getWorlds();
                    for (ServerWorld sw : worlds) {
                        String dimension = sw.getRegistryKey().getValue().toString();
                        if (unloadedPiggy.get(3).equals(dimension)) {
                            cm.getDispatcher().execute("execute in " + dimension + " run forceload add " + posX + " " + posZ, server.getCommandSource());
                            Thread.sleep(100);
                            cm.getDispatcher().execute("execute in " + dimension + " run forceload remove " + posX + " " + posZ, server.getCommandSource());
                            break;
                        }
                    }
                }
            } catch (Exception e) {}
        }

        public void run() {
            for (int i = 6; i > 0 && unloadedPigList.size() != PigList.getList().size(); i--) {
                if (i < 5) {
                    System.out.println("[PlayerPig] PLEASE WAIT " + i + " SECONDS OR LESS!");
                }
                try {
                    loadUnloadChunk();
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            LoadPigList.deleteAll();
        }
    }

}
