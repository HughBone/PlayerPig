package com.hughbone.playerpig.mixin.client;

import com.hughbone.playerpig.MyRenderState;
import net.minecraft.client.renderer.entity.state.PigRenderState;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PigRenderState.class)
public class PigEntityRenderStateMixin implements MyRenderState {

  @Unique private ResourceLocation identifier = null;

  @Override public ResourceLocation getIdentifier() {
    return identifier;
  }

  @Override public void setIdentifier(ResourceLocation id) {
    this.identifier = id;
  }

}