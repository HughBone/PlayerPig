package com.hughbone.playerpig.mixin.client;

import com.hughbone.playerpig.QuadrupedHelper;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.AnimalModel;
import net.minecraft.client.render.entity.model.QuadrupedEntityModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.PigEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;

@Mixin(QuadrupedEntityModel.class)
public abstract class QuadrupedEntityModelMixin<T extends Entity> extends AnimalModel<T> implements QuadrupedHelper {

    @Final
    @Shadow
    protected ModelPart head;
    
    @Override
    public ModelPart getHead() {
        return head;
    }

    @Unique
    private boolean getHead = true;

    @Inject(method = "getHeadParts", at=@At("HEAD"), cancellable = true)
    public void getHeadParts(CallbackInfoReturnable<Iterable<ModelPart>> cir) {
        if (!getHead) {
            cir.setReturnValue(Collections.emptyList());
        }
    }

    @Inject(method = "setAngles", at=@At("TAIL"))
    public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch, CallbackInfo ci) {
        getHead = true;
        if (entity.hasCustomName() && entity instanceof PigEntity) {
            getHead = false;
        }
    }

}