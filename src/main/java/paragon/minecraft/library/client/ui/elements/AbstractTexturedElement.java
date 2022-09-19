package paragon.minecraft.library.client.ui.elements;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import paragon.minecraft.library.client.ClientUtilities;

public abstract class AbstractTexturedElement extends AbstractPlaceableGuiElement {
	
	/* Internal Fields */
	protected final ResourceLocation TEXTURE;
	protected final int TEXTURE_WIDTH, TEXTURE_HEIGHT;
	protected int u, v;
	
	protected float alpha = 1.0F;
	protected int z;
	
	/* Shared Constants */
	protected static final int DEFAULT_TEXTURE_SIZE = 256;
	
	protected AbstractTexturedElement(@Nonnull ResourceLocation texture, @Nullable IPlacementMethod placementMethod) {
		this(texture, DEFAULT_TEXTURE_SIZE, DEFAULT_TEXTURE_SIZE, placementMethod);
	}

	protected AbstractTexturedElement(@Nonnull ResourceLocation texture, int textureWidth, int textureHeight, @Nullable IPlacementMethod placementMethod) {
		super(placementMethod);
		this.TEXTURE = Objects.requireNonNull(texture, "Cannot instantiate textured element with null texture ResourceLocation!");
		this.TEXTURE_WIDTH = textureWidth;
		this.TEXTURE_HEIGHT = textureHeight;
	}
	
	/* Public Methods */
	
	public float getAlpha() {
		return this.alpha;
	}
	
	public void setAlpha(float alpha) {
		this.alpha = alpha;
	}
	
	public int getBlitOffset() {
		return this.z;
	}
	
	public void setBlitOffset(final int offset) {
		this.z = offset;
	}
	
	public void setUV(final int u, final int v) {
		this.u = u;
		this.v = v;
	}

	@Override
	public void render(PoseStack pose, int cursorX, int cursorY, float delta) {
		// Setup RenderSystem
		this.setupRender(pose, cursorX, cursorY, delta);
		
		// Initialize Buffer
		BufferBuilder buffer = Tesselator.getInstance().getBuilder();
		buffer.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
		
		// Perform render
		this.doRender(pose, buffer, cursorX, cursorY, delta);
		
		// Upload buffer
		buffer.end();
		BufferUploader.end(buffer);
		
		// Clenaup RenderSystem
		this.cleanupRender(pose, cursorX, cursorY, delta);
	}
	
	/* Abstract Methods */
	
	protected abstract void doRender(PoseStack pose, BufferBuilder buffer, int cursorX, int cursorY, float delta);
	
	/* Internal Methods */
	
	protected void setupRender(PoseStack pose, int cursorX, int cursorY, float delta) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1, 1, 1, this.getAlpha()); // TODO: Color?
		RenderSystem.setShaderTexture(0, this.TEXTURE);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
	}

	protected void cleanupRender(PoseStack pose, int cursorX, int cursorY, float delta) {
		ClientUtilities.Render.resetShaderColor();
		RenderSystem.disableBlend();
	}
	
}
