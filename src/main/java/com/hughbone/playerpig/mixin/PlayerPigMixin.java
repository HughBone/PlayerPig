package com.hughbone.playerpig.mixin;

import com.hughbone.playerpig.PlayerPigExt;
import com.hughbone.playerpig.util.PPUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Adds playerPig, playerName, and playerUUID tags
@Mixin(Pig.class)
public abstract class PlayerPigMixin extends LivingEntity implements PlayerPigExt {

  @Unique private boolean playerPig = false;
  @Unique private String matchingPlayerName = "";
  @Unique private String matchingPlayerUUID = "";

  protected PlayerPigMixin(EntityType<? extends LivingEntity> entityType, Level world) {
    super(entityType, world);
  }

  @ModifyVariable(method = "addAdditionalSaveData", at = @At("HEAD"), ordinal = 0)
  public ValueOutput writeCustomDataToTag(ValueOutput view) {
    if (playerPig) {
      view.putBoolean("playerPig", true);
      view.putString("playerName", matchingPlayerName);
      view.putString("playerUUID", matchingPlayerUUID);
    }
    return view;
  }

  @ModifyVariable(method = "readAdditionalSaveData", at = @At("HEAD"), ordinal = 0)
  public ValueInput readCustomDataFromTag(ValueInput view) {
    boolean ppOptional = view.getBooleanOr("playerPig", false);
    if (ppOptional) {
      this.playerPig = true;
      this.matchingPlayerName = view.getStringOr("playerName", "");
      this.matchingPlayerUUID = view.getStringOr("playerUUID", "");
    }
    return view;
  }

  public boolean isPlayerPig() {
    return playerPig;
  }

  public void setPlayerPig(boolean setPP) {
    if (setPP) {
      playerPig = true;
    } else {
      playerPig = false;
    }
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
  @Inject(method = "thunderHit", at = @At("HEAD"), cancellable = true)
  public void lightningStrike(ServerLevel world, LightningBolt lightning, CallbackInfo ci) {
    if (playerPig) {
      ci.cancel();
    }
  }

  // Stop from dying
  @Inject(method = "getDeathSound", at = @At("HEAD"))
  public void stopPPDeath(CallbackInfoReturnable<SoundEvent> cir) {
    // Allow pig to die from /kill command
    if (playerPig) {
      if (String.valueOf(lastHurt).equals("3.4028235E38")) {
        this.remove(RemovalReason.DISCARDED);
        PPUtil.pigList.remove(matchingPlayerUUID);
        PPUtil.removeFile(matchingPlayerUUID);
      } else {
        this.setHealth(20F);
        //                this.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE,
        //                2147483647, 5, false, false));
      }
    }
  }

  // Stop from falling into the void (teleports to 0, 100, 0)
  @Inject(method = "getHurtSound", at = @At("HEAD"))
  public void stopPPVoidDamage(DamageSource source, CallbackInfoReturnable<SoundEvent> cir) {
    try {
      if (playerPig) {
        if (source.typeHolder().is(DamageTypes.FELL_OUT_OF_WORLD)) {
          this.randomTeleport(0, 100, 0, false);
          this.setHealth(20F);
          this.setDeltaMovement(Vec3.ZERO);
        }
      }
    } catch (Exception e) {
      System.out.println("[PlayerPig] Error preventing void damage / TP");
    }
  }

}