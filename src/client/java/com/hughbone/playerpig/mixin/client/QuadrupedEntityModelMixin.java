package com.hughbone.playerpig.mixin.client;

import com.hughbone.playerpig.QuadrupedHelper;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.AnimalModel;
import net.minecraft.client.render.entity.model.QuadrupedEntityModel;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(QuadrupedEntityModel.class)
public abstract class QuadrupedEntityModelMixin<T extends Entity> extends AnimalModel<T> implements QuadrupedHelper {

    @Final
    @Shadow
    protected ModelPart head;
    
    @Override
    public ModelPart getHead() {
        return head;
    }

}