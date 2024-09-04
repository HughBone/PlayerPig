package com.hughbone.playerpig;

import java.util.*;

import com.hughbone.playerpig.mixin.client.QuadrupedAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.AbstractSkullBlock;
import net.minecraft.block.SkullBlock;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.SkullBlockEntityModel;
import net.minecraft.client.render.block.entity.SkullBlockEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LimbAnimator;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

@Environment(value=EnvType.CLIENT)
public class PigHeadFeatureRenderer<T extends LivingEntity, M extends EntityModel<T>> extends FeatureRenderer<T, M> {

	private final Map<SkullBlock.SkullType, SkullBlockEntityModel> headModels;
	private final ModelPart head;

	private final SkullBlock.SkullType skullType = SkullBlock.SkullType.TYPES.get("player");
	private final SkullBlockEntityModel skullBlockEntityModel;

	public PigHeadFeatureRenderer(FeatureRendererContext<T, M> context, EntityModelLoader loader) {
		super(context);
		this.headModels = SkullBlockEntityRenderer.getModels(loader);
		QuadrupedEntityModel quadModel = (QuadrupedEntityModel) this.getContextModel();
		this.head = ((QuadrupedAccessor) quadModel).getHead();
		this.skullBlockEntityModel = headModels.get(this.skullType);
    }


	@Override
	public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, T livingEntity, float f, float g, float h, float j, float k, float l) {

		ItemStack itemStack = livingEntity.getEquippedStack(EquipmentSlot.HEAD);
		if (!livingEntity.hasCustomName()
			|| itemStack == null
			|| itemStack.isEmpty()
		) {
			return;
		}

		Item item = itemStack.getItem();
		if (item instanceof BlockItem && ((BlockItem)item).getBlock() instanceof AbstractSkullBlock) {
			ProfileComponent profileComponent = (ProfileComponent)itemStack.get(DataComponentTypes.PROFILE);
			SkullBlock.SkullType skullType = ((AbstractSkullBlock)((BlockItem)item).getBlock()).getSkullType();
			SkullBlockEntityModel skullBlockEntityModel = (SkullBlockEntityModel)this.headModels.get(skullType);
			RenderLayer renderLayer = SkullBlockEntityRenderer.getRenderLayer(skullType, profileComponent);

			matrixStack.push();
			matrixStack.scale(head.xScale, head.yScale, head.zScale);
			head.rotate(matrixStack);

			LimbAnimator limbAnimator;
			matrixStack.scale(1.15f, -1.15f, -1.15f);
			matrixStack.translate(-0.5, -0.25, -0.26);

			Entity entity = livingEntity.getVehicle();
			if (entity instanceof LivingEntity) {
				LivingEntity livingEntity2 = (LivingEntity)entity;
				limbAnimator = livingEntity2.limbAnimator;
			} else {
				limbAnimator = ((LivingEntity)livingEntity).limbAnimator;
			}

			float o = limbAnimator.getPos(h);
			SkullBlockEntityRenderer.renderSkull(null, 180f, o, matrixStack, vertexConsumerProvider, i, skullBlockEntityModel, renderLayer);

			matrixStack.pop();
		}
	}

}

