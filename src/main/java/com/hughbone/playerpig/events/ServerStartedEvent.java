package com.hughbone.playerpig.events;

import com.hughbone.playerpig.piglist.LoadPigList;
import com.hughbone.playerpig.piglist.PigList;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;

import java.util.List;

// Saves all unloaded pigs into memory
public class ServerStartedEvent {

    private List<List<String>> unloadedPigList;
    private MinecraftServer server;
    public static Team noCollision;
    public static Scoreboard teamScoreboard;

    private void loadAndUnloadChunk() {
        try {
            CommandManager cm = new CommandManager(CommandManager.RegistrationEnvironment.ALL);

            for (List<String> unloadedPiggy : unloadedPigList) {
                int posX = (int) Double.parseDouble(unloadedPiggy.get(0));
                int posZ = (int) Double.parseDouble(unloadedPiggy.get(2));

                // Temporarily load chunk so that EntityLoadEvent can add pig to PigList
                if (unloadedPiggy.get(3).equals("overworld")) {
                    cm.getDispatcher().execute("execute in minecraft:overworld run forceload add " + posX + " " + posZ, server.getCommandSource());
                    cm.getDispatcher().execute("execute in minecraft:overworld run forceload remove all", server.getCommandSource());
                } else if (unloadedPiggy.get(3).equals("the_nether")) {
                    cm.getDispatcher().execute("execute in minecraft:the_nether run forceload add " + posX + " " + posZ, server.getCommandSource());
                    cm.getDispatcher().execute("execute in minecraft:the_nether run forceload remove all", server.getCommandSource());
                } else if (unloadedPiggy.get(3).equals("the_end")) {
                    cm.getDispatcher().execute("execute in minecraft:the_end run forceload add " + posX + " " + posZ, server.getCommandSource());
                    cm.getDispatcher().execute("execute in minecraft:the_end run forceload remove all", server.getCommandSource());
                }
            }
        } catch (Exception e) {}
    }

    public class ServerStartedThread extends Thread {

        public void run() {
            for (int i = 6; i > 0 && unloadedPigList.size() != PigList.getList().size(); i--) {
                if (i < 5) {
                    System.out.println("[PlayerPig] PLEASE WAIT " + i + " SECONDS OR LESS!");
                }
                try {
                    loadAndUnloadChunk();
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            LoadPigList.deleteAll();
        }

    }

    public void init() {
        ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
            // No collisions for players logging in (so they don't get pushed by pigs)
            teamScoreboard = server.getScoreboard();
            try {
                noCollision = teamScoreboard.addTeam("nocollision");
            } catch (Exception e){}
            noCollision = teamScoreboard.getTeam("nocollision");
            noCollision.setCollisionRule(AbstractTeam.CollisionRule.NEVER);
            // Temporarily load chunks so that EntityLoadEvent can add pigs to PigList
            List<List<String>> unloadedPigList = LoadPigList.getAllData();
            if (!unloadedPigList.isEmpty()) {
                this.unloadedPigList = unloadedPigList;
                this.server = server;
                new ServerStartedThread().start();
            }
        });
    }

}
