package com.hughbone.playerpig.mixin;

import com.hughbone.playerpig.PlayerExt;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin (ServerPlayerEntity.class)
public class ServerPlayerEntityMixin implements PlayerExt {

    private boolean justJoined = false;

    public void setJustJoined(boolean justJoined) {
        this.justJoined = justJoined;
    }

    public boolean getJustJoined() {
        return justJoined;
    }

}
