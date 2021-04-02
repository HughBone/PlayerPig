package com.hughbone.playerpig.piglist;

import net.minecraft.entity.passive.PigEntity;

import java.util.ArrayList;
import java.util.List;

public class PigList {
    private static List<PigEntity> pigList = new ArrayList<PigEntity>();

    public static List<PigEntity> getList() {
        return pigList;
    }

    public static void appendList(PigEntity pig) {
        pigList.add(pig);
    }
}
