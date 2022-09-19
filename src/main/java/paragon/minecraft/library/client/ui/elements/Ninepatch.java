package paragon.minecraft.library.client.ui.elements;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.resources.ResourceLocation;
import paragon.minecraft.library.client.ClientUtilities;

public class Ninepatch extends AbstractTexturedElement {
	
	/* Internal Fields */
	protected int patchSize = 8;
	protected int patchPadding = 1;
	
	public Ninepatch(@Nonnull ResourceLocation texture, @Nullable IPlacementMethod placementMethod) {
		this(texture, AbstractTexturedElement.DEFAULT_TEXTURE_SIZE, AbstractTexturedElement.DEFAULT_TEXTURE_SIZE, placementMethod);
	}
	
	public Ninepatch(@Nonnull ResourceLocation texture, int textureWidth, int textureHeight, @Nullable IPlacementMethod placementMethod) {
		super(texture, textureWidth, textureHeight, placementMethod);
	}
	
	/* Public Methods */
	
	public void setPatchSize(int size) {
		this.patchSize = Math.max(0, size);
	}
	
	public void setPatchPadding(int padding) {
		this.patchPadding = Math.max(0, padding);
	}

	/* Widget Compliance Method */

	@Override
	protected void doRender(PoseStack pose, BufferBuilder buffer, int cursorX, int cursorY, float delta) {
		ClientUtilities.Render.drawNinepatch(buffer, pose.last().pose(), this.getBlitOffset(), this.getX(), this.getY(), this.getWidth(), this.getHeight(), this.u, this.v, this.patchSize, this.patchPadding, this.TEXTURE_WIDTH);
	}

}
