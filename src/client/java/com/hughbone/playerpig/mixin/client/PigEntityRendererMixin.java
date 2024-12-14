package com.hughbone.playerpig.mixin.client;

import com.hughbone.playerpig.ClientUtil;
import com.hughbone.playerpig.MyRenderState;
import com.mojang.authlib.GameProfile;
import net.minecraft.block.AbstractSkullBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.*;
import net.minecraft.client.render.entity.model.*;
import net.minecraft.client.render.entity.state.PigEntityRenderState;
import net.minecraft.client.texture.*;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

@Mixin(PigEntityRenderer.class)
public abstract class PigEntityRendererMixin extends AgeableMobEntityRenderer<PigEntity, PigEntityRenderState, PigEntityModel> {

	@Shadow @Final private static Identifier TEXTURE;
	@Unique private static BufferedImage pigTexture;

	public PigEntityRendererMixin(EntityRendererFactory.Context context, PigEntityModel model, PigEntityModel babyModel, float shadowRadius) {
		super(context, model, babyModel, shadowRadius);
	}

	@Inject(at = @At("TAIL"), method = "<init>")
	public void init(EntityRendererFactory.Context context, CallbackInfo ci) {
		try {
			InputStream inputStream = MinecraftClient.getInstance().getResourceManager().getResource(TEXTURE).get().getInputStream();
			pigTexture = ImageIO.read(inputStream);
		} catch (Exception e) {
			System.out.println("womp womp pigtexture is null");
		}
	}

	@Inject(at = @At("HEAD"), method = "getTexture(Lnet/minecraft/client/render/entity/state/PigEntityRenderState;)Lnet/minecraft/util/Identifier;", cancellable = true)
	public void getTexture(PigEntityRenderState pigEntityRenderState, CallbackInfoReturnable<Identifier> cir) {
		Identifier id = ((MyRenderState) pigEntityRenderState).getIdentifier();
		if (id != null) {
			cir.setReturnValue(id);
		} else {
			cir.setReturnValue(TEXTURE);
		}
	}

	@Inject(at = @At("HEAD"), method = "updateRenderState(Lnet/minecraft/entity/passive/PigEntity;Lnet/minecraft/client/render/entity/state/PigEntityRenderState;F)V")
	public void updateRenderState(PigEntity pigEntity, PigEntityRenderState pigEntityRenderState, float f, CallbackInfo ci) {
		// Set pigEntityRenderState based on id
		Identifier playerPigId = ClientUtil.pigToIdMap.get(pigEntity.getUuid());
		((MyRenderState) pigEntityRenderState).setIdentifier(playerPigId);

		if (!pigEntity.hasCustomName() || ClientUtil.chillout || playerPigId != null) {
			return;
		}

		// return if no head item, or already has id
		ItemStack itemStack = pigEntity.getEquippedStack(EquipmentSlot.HEAD);
		if (itemStack == null || itemStack.isEmpty()) {
			return;
		}

		Item item = itemStack.getItem();
		if (item instanceof BlockItem && ((BlockItem)item).getBlock() instanceof AbstractSkullBlock) {
			ProfileComponent profileComp = itemStack.get(DataComponentTypes.PROFILE);
			if (profileComp != null && profileComp.gameProfile() != null) {
				// Potential player pig found!
				GameProfile profile = profileComp.gameProfile();
				PlayerSkinProvider skinProvider = MinecraftClient.getInstance().getSkinProvider();

				// gotta enable lock here
				ClientUtil.chillout = true;

				skinProvider.fetchSkinTextures(profile).thenAcceptAsync((skinTexturesOptional) -> {
					if (skinTexturesOptional.isEmpty()) {
						ClientUtil.chillout = false;
						return;
					}

					SkinTextures skinTextures = skinTexturesOptional.get();
					if (skinTextures.texture() == null) {
						ClientUtil.chillout = false;
						return;
					}

					// Get playerTexture
					BufferedImage playerTexture = null;
					try {
						ResourceManager rm = MinecraftClient.getInstance().getResourceManager();
						TextureManager tm = MinecraftClient.getInstance().getTextureManager();
						AbstractTexture texture = tm.getTexture(skinTextures.texture());

						if (texture instanceof NativeImageBackedTexture nativeImageTexture) {
							NativeImage nativeImage = nativeImageTexture.getImage();
							if (nativeImage != null) {
								playerTexture = nativeToBufferedImage(nativeImage);
							}
						} else if (rm.getResource(skinTextures.texture()).isPresent()) {
							InputStream inputStream = rm.getResource(skinTextures.texture()).get().getInputStream();
							if (inputStream != null) {
								playerTexture = ImageIO.read(inputStream);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}

					// Add new identifier w/ texture to hashmap
					if (playerTexture != null) {
						Identifier newId = getNewIdentifier(playerTexture, skinTextures.texture().getPath());
						ClientUtil.pigToIdMap.put(pigEntity.getUuid(), newId);
					}

					ClientUtil.chillout = false;
				});
			}
		}
	}

	@Unique
	private Identifier getNewIdentifier(BufferedImage playerTexture, String id_string) {
		try {
			// Create a new BufferedImage with the same type (TYPE_INT_ARGB) as pigTexture
			BufferedImage pigTextureCopy = new BufferedImage(pigTexture.getWidth(), pigTexture.getHeight(), BufferedImage.TYPE_INT_ARGB);

			// Copy the pixel data from the original pigTexture into pigTextureCopy
			pigTextureCopy.setRGB(
				0, 0, pigTexture.getWidth(), pigTexture.getHeight(),
				pigTexture.getRGB(0, 0, pigTexture.getWidth(), pigTexture.getHeight(),
				null, 0, pigTexture.getWidth()), 0, pigTexture.getWidth()
			);

			// Create a subimage of the playerTexture
			BufferedImage subImage = playerTexture.getSubimage(0, 0, 32, 16);

			// Create a Graphics2D object to draw onto the pigTextureCopy
			Graphics2D g = pigTextureCopy.createGraphics();
			// Ensure that transparency is preserved
			g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
			// Draw the subimage onto pigTextureCopy at position (0,0)
			g.drawImage(subImage, 0, 0, null);
			// Prevent fresh animations blinking weirdness
			g.setComposite(AlphaComposite.Clear);
			g.fillRect(0, 0, 8, 8);
			// need to do this
			g.dispose();

			// Write the modified image to a byte array
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(pigTextureCopy, "PNG", baos);
			byte[] imageData = baos.toByteArray();

			// Unique id
			Identifier newIdentifier = Identifier.of("playerpig", id_string);
			// Read byteArray to image
			NativeImage nativeImage = NativeImage.read(imageData);
			MinecraftClient.getInstance().getTextureManager().registerTexture(newIdentifier, new NativeImageBackedTexture(nativeImage));

			return newIdentifier;
		} catch (Exception e) {
			System.out.println("problem creating new texture!");
		}

		return null;
	}

	@Unique
	private BufferedImage nativeToBufferedImage(NativeImage nativeImage) {
		// Create a BufferedImage of the same width and height as the NativeImage
		BufferedImage bufferedImage = new BufferedImage(
				nativeImage.getWidth(),
				nativeImage.getHeight(),
				BufferedImage.TYPE_INT_ARGB
		);

		// Get the pixel data from the BufferedImage
		int[] pixels = ((DataBufferInt) bufferedImage.getRaster().getDataBuffer()).getData();

		// Copy the pixel data from NativeImage to the BufferedImage pixel array
		for (int y = 0; y < nativeImage.getHeight(); y++) {
			for (int x = 0; x < nativeImage.getWidth(); x++) {
				int color = nativeImage.getColorArgb(x, y);  // Get the ARGB value from NativeImage
				pixels[y * nativeImage.getWidth() + x] = color;  // Set the pixel in the BufferedImage
			}
		}

		return bufferedImage;
	}

}