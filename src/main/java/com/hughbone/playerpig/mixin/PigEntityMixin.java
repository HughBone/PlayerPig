package com.hughbone.playerpig.mixin;

import com.hughbone.playerpig.PlayerPigExt;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Adds custom playerpig tag to LivingEntities
@Mixin(PigEntity.class)
public abstract class PigEntityMixin extends LivingEntity {

    public PigEntityMixin(EntityType<? extends PigEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "getDeathSound", at = @At("HEAD"))
    public void stopPPDeath(CallbackInfoReturnable<SoundEvent> cir) {
        try {
            if (((PlayerPigExt) this).isPlayerPig()) {
                this.setHealth(20F);
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 2147483647, 5, false, false));
            }
        } catch (Exception e) {}
    }

    @Inject(method = "getHurtSound", at = @At("HEAD"))
    public void stopPPVoidDamage(DamageSource source, CallbackInfoReturnable<SoundEvent> cir) {
        try {
            if (((PlayerPigExt) this).isPlayerPig()) {
                if (source.isOutOfWorld()) {
                    this.updatePosition(0, 100, 0);
                    this.updateTrackedPosition(0, 100, 0);
                    this.setHealth(10F);
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
