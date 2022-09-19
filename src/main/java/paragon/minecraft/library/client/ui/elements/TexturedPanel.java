package paragon.minecraft.library.client.ui.elements;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.resources.ResourceLocation;
import paragon.minecraft.library.client.ClientUtilities;

public class TexturedPanel extends AbstractTexturedElement {

	public TexturedPanel(@Nonnull ResourceLocation texture, @Nullable IPlacementMethod placementMethod) {
		super(texture, AbstractTexturedElement.DEFAULT_TEXTURE_SIZE, AbstractTexturedElement.DEFAULT_TEXTURE_SIZE, placementMethod);
	}

	public TexturedPanel(@Nonnull ResourceLocation texture, int textureWidth, int textureHeight, @Nullable IPlacementMethod placementMethod) {
		super(texture, textureWidth, textureHeight, placementMethod);
	}

	@Override
	protected void doRender(PoseStack pose, BufferBuilder buffer, int cursorX, int cursorY, float delta) {
		ClientUtilities.Render.drawQuad(buffer, pose.last().pose(), this.getBlitOffset(), this.getX(), this.getY(), this.getWidth(), this.getHeight(), this.u, this.v, this.TEXTURE_WIDTH, this.TEXTURE_HEIGHT);
	}

}
