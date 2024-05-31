package com.hughbone.playerpig.mixin.client;

import com.hughbone.playerpig.PigHeadFeatureRenderer;
import net.minecraft.client.render.entity.*;
import net.minecraft.client.render.entity.model.*;
import net.minecraft.entity.passive.PigEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PigEntityRenderer.class)
public abstract class PigEntityRendererMixin extends MobEntityRenderer<PigEntity, PigEntityModel<PigEntity>> {

	public PigEntityRendererMixin(EntityRendererFactory.Context ctx, PigEntityModel<PigEntity> entityModel, float f) {
		super(ctx, entityModel, f);
	}

	@Inject(at = @At("TAIL"), method = "<init>")
	public void init(EntityRendererFactory.Context context, CallbackInfo ci) {
		this.addFeature(new PigHeadFeatureRenderer<>((PigEntityRenderer)(Object)this, context.getModelLoader()));
	}

}