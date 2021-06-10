package com.hughbone.playerpig.mixin;

import net.minecraft.entity.passive.PigEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import com.hughbone.playerpig.PlayerPigExt;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Adds playerPig, playerName, and playerUUID tags
@Mixin(PigEntity.class)
public abstract class PlayerPigMixin implements PlayerPigExt {

    private boolean playerPig = false;
    private String matchingPlayerName = "";
    private String matchingPlayerUUID = "";

    @Inject(method = "writeCustomDataToNbt", at = @At("RETURN"))
    public void writeCustomDataToTag (NbtCompound nbt, CallbackInfo ci) {
        if (playerPig) {
            nbt.putBoolean("playerPig", playerPig);
            nbt.putString("playerName", matchingPlayerName);
            nbt.putString("playerUUID", matchingPlayerUUID);
        }
    }
    @Inject(method = "readCustomDataFromNbt", at = @At("RETURN"))
    public void readCustomDataFromTag (NbtCompound nbt, CallbackInfo ci) {
        playerPig = nbt.getBoolean("playerPig");
        if (playerPig) {
            matchingPlayerName = nbt.getString("playerName");
            matchingPlayerUUID = nbt.getString("playerUUID");
        }
    }

    public boolean isPlayerPig() {
        if (playerPig) {
            return true;
        }
        else {
            return false;
        }
    }
    public void setPlayerPig(boolean setPP) {
        if (setPP)
            playerPig = true;
        else
            playerPig = false;
    }

    public String getPlayerName() {
        return matchingPlayerName;
    }
    public void setPlayerName(String name) {
        matchingPlayerName = name;
    }
    public String getPlayerUUID() {
        return matchingPlayerUUID;
    }
    public void setPlayerUUID(String uuid) {
        matchingPlayerUUID = uuid;
    }




}