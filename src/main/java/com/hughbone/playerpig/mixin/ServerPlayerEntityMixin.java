package com.hughbone.playerpig.mixin;

import com.hughbone.playerpig.PlayerExt;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin (ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin  extends Entity implements PlayerExt{

    @Shadow @Final public MinecraftServer server;
    private boolean justJoined = false;
    public Entity linkedPassenger = null;

    public ServerPlayerEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    public void setLinkedPassenger(Entity passenger) {
        this.linkedPassenger = passenger;
    }

    public Entity getLinkedPassenger() {
        return linkedPassenger;
    }

    public void setJustJoined(boolean justJoined) {
        this.justJoined = justJoined;
    }

    public boolean getJustJoined() {
        return justJoined;
    }

    @Inject(method="tick", at=@At("HEAD"))
    public void tick(CallbackInfo ci) {
        if (linkedPassenger != null) {
            if (this.world != linkedPassenger.world) {
                linkedPassenger = null;
            }
            else if (linkedPassenger.hasPassengerDeep((ServerPlayerEntity)(Object)this)) {
                linkedPassenger = null;
            }
            else {
                linkedPassenger.updatePosition(this.getX(), this.getY()+4D, this.getZ());
                linkedPassenger.updateTrackedPosition(this.getX(), this.getY()+4D, this.getZ());
                linkedPassenger.fallDistance = 0;
            }
        }

    }

}
