package com.hughbone.playerpig.mixin.client;

import com.hughbone.playerpig.ClientUtil;
import com.hughbone.playerpig.MyRenderState;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.NativeImage;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PigModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.PigRenderer;
import net.minecraft.client.renderer.entity.state.PigRenderState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.AbstractSkullBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PigRenderer.class)
public abstract class PigEntityRendererMixin
  extends MobRenderer<Pig, PigRenderState, PigModel>
{

  @Unique private static BufferedImage pigTexture;

  public PigEntityRendererMixin(
    EntityRendererProvider.Context context,
    PigModel entityModel,
    float f)
  {
    super(context, entityModel, f);
  }

  @Inject(at = @At("TAIL"), method = "<init>")
  public void init(EntityRendererProvider.Context context, CallbackInfo ci) {
    try {
      ResourceLocation PIG_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/pig/temperate_pig.png");
      InputStream inputStream = Minecraft
        .getInstance()
        .getResourceManager()
        .getResource(PIG_TEXTURE)
        .get()
        .open();
      pigTexture = ImageIO.read(inputStream);
    } catch (Exception e) {
      System.out.println("womp womp pigtexture is null");
    }
  }

  @Inject(at = @At("HEAD"),
    method = "getTextureLocation(Lnet/minecraft/client/renderer/entity/state/PigRenderState;)Lnet/minecraft/resources/ResourceLocation;",
    cancellable = true)
  public void getTexture(
    PigRenderState pigEntityRenderState,
    CallbackInfoReturnable<ResourceLocation> cir)
  {
    ResourceLocation id = ((MyRenderState) pigEntityRenderState).getIdentifier();
    if (id != null) {
      cir.setReturnValue(id);
    }
  }

  @Inject(at = @At("HEAD"),
    method = "extractRenderState(Lnet/minecraft/world/entity/animal/Pig;Lnet/minecraft/client/renderer/entity/state/PigRenderState;F)V")
  public void updateRenderState(
    Pig pigEntity,
    PigRenderState pigEntityRenderState,
    float f,
    CallbackInfo ci)
  {
    // Set pigEntityRenderState based on id
    ResourceLocation playerPigId = ClientUtil.pigToIdMap.get(pigEntity.getUUID());
    ((MyRenderState) pigEntityRenderState).setIdentifier(playerPigId);

    if (!pigEntity.hasCustomName() || ClientUtil.chillout || playerPigId != null) {
      return;
    }

    // return if no head item, or already has id
    ItemStack itemStack = pigEntity.getItemBySlot(EquipmentSlot.HEAD);
    if (itemStack == null || itemStack.isEmpty()) {
      return;
    }

    Item item = itemStack.getItem();
    if (item instanceof BlockItem && ((BlockItem) item).getBlock() instanceof AbstractSkullBlock) {
      ResolvableProfile profileComp = itemStack.get(DataComponents.PROFILE);
      if (profileComp != null && profileComp.partialProfile() != null) {
        // Potential player pig found!
        GameProfile profile = profileComp.partialProfile();
        SkinManager skinProvider = Minecraft.getInstance().getSkinManager();

        // gotta enable lock here
        ClientUtil.chillout = true;

        skinProvider.get(profile).thenAcceptAsync((skinTexturesOptional) -> {
          Minecraft.getInstance().execute(() -> {
            if (skinTexturesOptional.isEmpty()) {
              ClientUtil.chillout = false;
              return;
            }

            PlayerSkin skinTextures = skinTexturesOptional.get();
            if (skinTextures.body().texturePath() == null) {
              ClientUtil.chillout = false;
              return;
            }

            // Get playerTexture
            BufferedImage playerTexture = null;
            try {
              ResourceManager rm = Minecraft.getInstance().getResourceManager();
              TextureManager tm = Minecraft.getInstance().getTextureManager();
              AbstractTexture texture = tm.getTexture(skinTextures.body().texturePath());

              if (texture instanceof DynamicTexture nativeImageTexture) {
                NativeImage nativeImage = nativeImageTexture.getPixels();
                if (nativeImage != null) {
                  playerTexture = nativeToBufferedImage(nativeImage);
                }
              } else if (rm.getResource(skinTextures.body().texturePath()).isPresent()) {
                InputStream inputStream =
                  rm.getResource(skinTextures.body().texturePath()).get().open();
                if (inputStream != null) {
                  playerTexture = ImageIO.read(inputStream);
                }
              }
            } catch (Exception e) {
              System.out.println("problem getting player texture");
              e.printStackTrace();
              ClientUtil.chillout = false;
              return;
            }

            // Add new identifier w/ texture to hashmap
            if (playerTexture != null) {
              ResourceLocation newId =
                getNewIdentifier(playerTexture, skinTextures.body().texturePath().getPath());
              ClientUtil.pigToIdMap.put(pigEntity.getUUID(), newId);
            }

            ClientUtil.chillout = false;
          });
        });
      }
    }
  }

  @Unique private ResourceLocation getNewIdentifier(BufferedImage playerTexture, String id_string) {
    try {
      // Create a new BufferedImage with the same type (TYPE_INT_ARGB) as pigTexture
      BufferedImage pigTextureCopy = new BufferedImage(
        pigTexture.getWidth(),
        pigTexture.getHeight(),
        BufferedImage.TYPE_INT_ARGB
      );

      // Copy the pixel data from the original pigTexture into pigTextureCopy
      pigTextureCopy.setRGB(
        0,
        0,
        pigTexture.getWidth(),
        pigTexture.getHeight(),
        pigTexture.getRGB(
          0,
          0,
          pigTexture.getWidth(),
          pigTexture.getHeight(),
          null,
          0,
          pigTexture.getWidth()
        ),
        0,
        pigTexture.getWidth()
      );

      // Create a subimage of the playerTexture
      BufferedImage subImage = playerTexture.getSubimage(0, 0, 32, 16);

      // Create a Graphics2D object to draw onto the pigTextureCopy
      Graphics2D g = pigTextureCopy.createGraphics();
      // Ensure that transparency is preserved
      g.setRenderingHint(
        RenderingHints.KEY_ALPHA_INTERPOLATION,
        RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED
      );
      // Draw the subimage onto pigTextureCopy at position (0,0)
      g.drawImage(subImage, 0, 0, null);
      // Prevent fresh animations weirdness
      g.setComposite(AlphaComposite.Clear);
      g.fillRect(0, 0, 8, 8);
      g.fillRect(24, 0, 8, 8);
      // need to do this
      g.dispose();

      // Write the modified image to a byte array
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ImageIO.write(pigTextureCopy, "PNG", baos);
      byte[] imageData = baos.toByteArray();

      // Unique id
      ResourceLocation newIdentifier = ResourceLocation.fromNamespaceAndPath("playerpig", id_string);
      // Read byteArray to image
      NativeImage nativeImage = NativeImage.read(imageData);
      Minecraft
        .getInstance()
        .getTextureManager()
        .register(
          newIdentifier,
          new DynamicTexture(newIdentifier::toString, nativeImage)
        );

      return newIdentifier;
    } catch (Exception e) {
      System.out.println("problem creating new texture!");
    }

    return null;
  }

  @Unique private BufferedImage nativeToBufferedImage(NativeImage nativeImage) {
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
        int color = nativeImage.getPixel(x, y);  // Get the ARGB value from NativeImage
        pixels[y * nativeImage.getWidth() + x] = color;  // Set the pixel in the BufferedImage
      }
    }

    return bufferedImage;
  }

}