package com.hughbone.playerpig.mixin;

import com.hughbone.playerpig.util.PPUtil;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import com.hughbone.playerpig.PlayerPigExt;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


// Adds playerPig, playerName, and playerUUID tags
@Mixin(PigEntity.class)
public abstract class PlayerPigMixin extends LivingEntity implements PlayerPigExt {

    @Unique
    private boolean playerPig = false;
    @Unique
    private String matchingPlayerName = "";
    @Unique
    private String matchingPlayerUUID = "";

    protected PlayerPigMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

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
        return playerPig;
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

    // Stop turning into zombified piglin
    @Inject(method = "onStruckByLightning", at = @At("HEAD"), cancellable = true)
    public void lightningStrike(ServerWorld world, LightningEntity lightning, CallbackInfo ci) {
        if (playerPig) {
            ci.cancel();
        }
    }

    // Stop from dying
    @Inject(method = "getDeathSound", at = @At("HEAD"))
    public void stopPPDeath(CallbackInfoReturnable<SoundEvent> cir) {
        // Allow pig to die from /kill command
        if (playerPig) {
            if (String.valueOf(lastDamageTaken).equals("3.4028235E38")) {
                this.remove(RemovalReason.DISCARDED);
                PPUtil.pigList.remove(matchingPlayerUUID);
                PPUtil.removeFile(matchingPlayerUUID);
            }
            else {
                this.setHealth(20F);
//                this.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 2147483647, 5, false, false));
            }
        }
    }

    // Stop from falling into the void (teleports to 0, 100, 0)
    @Inject(method = "getHurtSound", at = @At("HEAD"))
    public void stopPPVoidDamage(DamageSource source, CallbackInfoReturnable<SoundEvent> cir) {
        try {
            if (playerPig) {
                if (source.getTypeRegistryEntry().matchesKey(DamageTypes.OUT_OF_WORLD)) {
                    this.teleport(0, 100, 0, false);
                    this.setHealth(20F);
                    this.setVelocity(Vec3d.ZERO);
                }
            }
        } catch (Exception e) {
            System.out.println("[PlayerPig] Error preventing void damage / TP");
        }
    }

}