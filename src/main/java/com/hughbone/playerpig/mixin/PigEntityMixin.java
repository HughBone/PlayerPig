package com.hughbone.playerpig.mixin;

import com.hughbone.playerpig.PlayerPigExt;
import com.hughbone.playerpig.piglist.PigList;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.server.command.TeleportCommand;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Stops pigs from dying / falling into the void
@Mixin(PigEntity.class)
public abstract class PigEntityMixin extends LivingEntity {

    public PigEntityMixin(EntityType<? extends PigEntity> entityType, World world) {
        super(entityType, world);
    }

    // Stop turning into zombified piglin
    @Inject(method = "onStruckByLightning", at = @At("HEAD"), cancellable = true)
    public void lightningStrike(ServerWorld world, LightningEntity lightning, CallbackInfo ci) {
        if (((PlayerPigExt) this).isPlayerPig()) {
            ci.cancel();
        }
    }

    // Stop from dying
    @Inject(method = "getDeathSound", at = @At("HEAD"))
    public void stopPPDeath(CallbackInfoReturnable<SoundEvent> cir) {
        // Allow pig to die from /kill command
        if (String.valueOf(lastDamageTaken).equals("3.4028235E38")) {
            PigList.getList().remove(this);
        }
        else {
            if (((PlayerPigExt) this).isPlayerPig()) {
                this.setHealth(20F);
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 2147483647, 5, false, false));
            }
        }
    }

    // Stop from falling into the void (teleports to 0, 100, 0)
    @Inject(method = "getHurtSound", at = @At("HEAD"))
    public void stopPPVoidDamage(DamageSource source, CallbackInfoReturnable<SoundEvent> cir) {
        try {
            if (((PlayerPigExt) this).isPlayerPig()) {
                if (source.isOutOfWorld()) {
                    this.updatePosition(0, 100, 0);
                    this.updateTrackedPosition(0, 100, 0);
                    this.setHealth(20F);
                    this.setVelocity(Vec3d.ZERO);

                    while (this.isInsideWall()) {
                        double newY = this.getY() + 10;
                        this.updatePosition(this.getX(), newY, this.getZ());
                        this.updateTrackedPosition(this.getX(), newY, this.getZ());
                    }
                }
            }
        } catch (Exception e) {
        }
    }

}
