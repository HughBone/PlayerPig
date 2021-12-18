package com.hughbone.playerpig.mixin;

import com.hughbone.playerpig.PlayerPigExt;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Stop leads from breaking (50 blocks) when player pig is leashed
// Useful when flying w/ elytra
@Mixin(PathAwareEntity.class)
public abstract class PathAwareEntityMixin extends MobEntity implements PlayerPigExt {

    protected PathAwareEntityMixin(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "updateLeash", at = @At("HEAD"), cancellable = true)
    public void updateLeash(CallbackInfo ci) {
        if (this.isLeashed()) {
            if (this.getType().equals(EntityType.PIG)) {
                if (((PlayerPigExt) this).isPlayerPig()) {
                    // I have no idea what's going on here I just copy pasted the code
                    super.updateLeash();
                    Entity entity = this.getHoldingEntity();
                    if (entity != null && entity.world == this.world) {
                        this.setPositionTarget(entity.getBlockPos(), 5);
                        float f = this.distanceTo(entity);
                        if (f > 50.0F) { // OKAY 50 BLOCK LEAD BREAKAGE
                            this.detachLeash(true, true);
                            this.goalSelector.disableControl(Goal.Control.MOVE);
                        } else if (f > 6.0F) {
                            double d = (entity.getX() - this.getX()) / (double) f;
                            double e = (entity.getY() - this.getY()) / (double) f;
                            double g = (entity.getZ() - this.getZ()) / (double) f;
                            this.setVelocity(this.getVelocity().add(Math.copySign(d * d * 1D, d), Math.copySign(e * e * 0.4D, e), Math.copySign(g * g * 0.4D, g)));
                        }
                    }
                    ci.cancel();
                }
            }
        }
    }
}
