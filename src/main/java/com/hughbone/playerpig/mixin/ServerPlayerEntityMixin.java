package com.hughbone.playerpig.mixin;

import com.hughbone.playerpig.util.PPUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin (ServerPlayerEntity.class)
//@Environment(EnvType.SERVER)
public class ServerPlayerEntityMixin {

    @Inject(at=@At("TAIL"), method="onDisconnect")
    private void onDisconnect(CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;

        if (!PPUtil.serverStopping) {
            PPUtil.spawnPlayerPig(player);
        }
    }

}
