package com.hughbone.playerpig.mixin.client;

import com.hughbone.playerpig.MyRenderState;
import net.minecraft.client.renderer.entity.state.PigRenderState;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PigRenderState.class)
public class PigEntityRenderStateMixin implements MyRenderState {

  @Unique private Identifier identifier = null;

  @Override public Identifier getIdentifier() {
    return identifier;
  }

  @Override public void setIdentifier(Identifier id) {
    this.identifier = id;
  }

}