package com.hughbone.playerpig;

import net.minecraft.entity.Entity;

public interface PlayerExt {
    void setJustJoined(boolean justJoined);
    void setLinkedPassenger(Entity passenger);
    Entity getLinkedPassenger();
    boolean getJustJoined();
}
