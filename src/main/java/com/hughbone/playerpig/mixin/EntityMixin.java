package com.hughbone.playerpig.mixin;

import com.hughbone.playerpig.PlayerPigExt;
import com.hughbone.playerpig.piglist.LoadPigList;
import com.hughbone.playerpig.util.PPUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.List;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Shadow @Final private EntityType<?> type;

    @Shadow public abstract String getUuidAsString();

    @Shadow @Nullable public abstract MinecraftServer getServer();

    @ModifyArgs(method = "readNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;setPos(DDD)V"))
    private void test(Args args) {

        // Get Pos from piglist
        if (this.type.equals(EntityType.PLAYER)) {
            for (PigEntity pigInList : PPUtil.getPigList()) {
                try {
                    if (((PlayerPigExt) pigInList).getPlayerUUID().equals(this.getUuidAsString())) {
                        args.set(0, pigInList.getX());
                        args.set(1, pigInList.getY());
                        args.set(2, pigInList.getZ());
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // Get Pos from file
            List<List<String>> unloadedPigList = LoadPigList.getAllData();

            try {
                for (List<String> unloadedPiggy : unloadedPigList) {
                    if (unloadedPiggy.get(4).equals(this.getUuidAsString())) {
                        Iterable<ServerWorld> worlds = getServer().getWorlds();
                        for (ServerWorld sw : worlds) {
                            final String dimension = sw.getRegistryKey().getValue().toString();
                            if (unloadedPiggy.get(3).equals(dimension)) {
                                args.set(0, Double.parseDouble(unloadedPiggy.get(0)));
                                args.set(1, Double.parseDouble(unloadedPiggy.get(1)));
                                args.set(2, Double.parseDouble(unloadedPiggy.get(2)));
                                return;
                            }
                        }
                    }
                }
            } catch (Exception e){
                e.printStackTrace();
            }

        }

    }


}
