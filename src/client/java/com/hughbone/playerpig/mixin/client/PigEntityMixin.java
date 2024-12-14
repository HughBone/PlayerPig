package com.hughbone.playerpig.mixin.client;

import com.hughbone.playerpig.MyRenderState;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PigEntity.class)
public class PigEntityMixin implements MyRenderState {
    @Unique
    private Identifier identifier = null;

    @Override
    public Identifier getIdentifier() {
        return identifier;
    }

    @Override
    public void setIdentifier(Identifier id) {
        this.identifier = id;
    }
}
