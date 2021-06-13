package com.hughbone.playerpig.util;

import net.minecraft.entity.passive.PigEntity;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PPUtil {

    private static List<PigEntity> pigList = new ArrayList<PigEntity>();

    public static List<PigEntity> getPigList() {
        return pigList;
    }

    public static void removeFile(String filename) {
        File f = new File(System.getProperty("user.dir") + File.separator + "mods" + File.separator + "PlayerPig_Data" + File.separator + filename);
        f.delete();
    }

    public static void joinNoCollision(ServerPlayerEntity player, MinecraftServer server) {
        // No collisions for players logging in (so they don't get pushed by pigs)
        Scoreboard teamScoreboard = server.getScoreboard();
        Team noCollision;
        try {
            noCollision = teamScoreboard.addTeam("nocollision");
            noCollision.setCollisionRule(AbstractTeam.CollisionRule.NEVER);
        } catch (Exception e) {}
        noCollision = teamScoreboard.getTeam("nocollision");
        teamScoreboard.addPlayerToTeam(player.getEntityName(), noCollision); // Add player to team
    }

    public static void leaveNoCollision(ServerPlayerEntity player, MinecraftServer server) {
        Scoreboard teamScoreboard = server.getScoreboard();
        Team noCollision = teamScoreboard.getTeam("nocollision");
        teamScoreboard.removePlayerFromTeam(player.getEntityName(), noCollision); // Remove player from team
    }

}
