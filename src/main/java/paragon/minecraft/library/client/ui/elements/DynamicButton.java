package paragon.minecraft.library.client.ui.elements;

import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import paragon.minecraft.library.client.ClientUtilities;

public class DynamicButton extends Button implements IPlaceableGuiElement {
	
	/* Shared Constants */
	protected static final OnPress NO_PRESS_EFFECT = (instance) -> { };

	/* Internal Fields */
	protected final SoundEvent SOUND;
	protected final ResourceLocation TEXTURE;
	protected final int TEXTURE_U, TEXTURE_V, FOCUS_TEXTURE_U, FOCUS_TEXTURE_V;
	protected Optional<IPlacementMethod> placementMethod = Optional.empty();

	protected DynamicButton(@Nonnull ResourceLocation texture, int xPos, int yPos, int width, int height, int u, int v, int focusU, int focusV, @Nonnull OnPress onPressedEvent, @Nonnull OnTooltip onTooltipEvent, @Nullable SoundEvent sound) {
		super(xPos, yPos, width, height, TextComponent.EMPTY, Objects.requireNonNull(onPressedEvent, "Cannot instantiate DynamicButton with null OnPress event!"), Objects.requireNonNull(onTooltipEvent, "Cannot instantiate DynamicButton with null OnTooltip event!"));
		this.TEXTURE = Objects.requireNonNull(texture, "Cannot instantiate DynamicButton with null texture ResourceLocation!");
		this.SOUND = sound;
		this.TEXTURE_U = u;
		this.TEXTURE_V = v;
		this.FOCUS_TEXTURE_U = focusU;
		this.FOCUS_TEXTURE_V = focusV;
	}

	/* Supertype Override Methods */

	@Override
	public void renderButton(PoseStack pose, int xPos, int yPos, float delta) {
		// Calculate values
		final int u = this.isHoveredOrFocused() ? this.FOCUS_TEXTURE_U : this.TEXTURE_U;
		final int v = this.isHoveredOrFocused() ? this.FOCUS_TEXTURE_V : this.TEXTURE_V;
		
		// RenderSystem setup
		ClientUtilities.Render.setupForBlit();
		RenderSystem.setShaderTexture(0, this.TEXTURE);
		
		// Render
		this.blit(pose, this.x, this.y, u, v, this.width, this.height);
	}

	@Override
	public void playDownSound(final SoundManager sounds) {
		if (Objects.nonNull(this.SOUND)) {
			sounds.play(SimpleSoundInstance.forUI(this.SOUND, 1.0F));
		}
	}
	
	/* IPlaceableGuiElement Compliance Methods */

	@Override
	public int getX() {
		return this.x;
	}

	@Override
	public int getY() {
		return this.y;
	}

	@Override
	public void setX(int xPos) {
		this.x = xPos;
	}

	@Override
	public void setY(int yPos) {
		this.y = yPos;
	}

	@Override
	public Optional<IPlacementMethod> getPlacementMethod() {
		return this.placementMethod;
	}
	
	/* Internal Methods */
	
	protected void setPlacementMethod(@Nullable IPlacementMethod method) {
		this.placementMethod = Optional.ofNullable(method);
	}

	/* Builder Instance */

	public static class Builder {
		
		/* Internal Fields */
		protected ResourceLocation texture;
		protected int width, height, u, v, focusU, focusV;
		protected OnPress onPressEvent;
		protected OnTooltip onTooltip;
		protected SoundEvent sound;
		protected IPlacementMethod method;
		
		public static Builder create() {
			return new Builder();
		}
		
		public Builder withTexture(final ResourceLocation texture, final int width, final int height) {
			return this.withTexture(texture, width, height, 0, 0);
		}
		

		public Builder withTexture(final ResourceLocation texture, final int width, final int height, final int u, final int v) {
			return this.withTexture(texture, width, height, u, v, 0);
		}
		
		public Builder withTexture(final ResourceLocation texture, final int width, final int height, final int u, final int v, final int padding) {
			final int paddedU = u + padding;
			final int paddedV = v + padding;
			return this.withTexture(texture, width, height, paddedU, paddedV, paddedU, paddedV + height + (2 * padding));
		}
		
		public Builder withTexture(final ResourceLocation texture, final int width, final int height, final int u, final int v, final int focusU, final int focusV) {
			this.texture = texture;
			this.width = width;
			this.height = height;
			this.u = u;
			this.v = v;
			this.focusU = focusU;
			this.focusV = focusV;
			return this;
		}
		
		public Builder withClickSound(final SoundEvent sound) {
			this.sound = sound;
			return this;
		}
		
		public Builder withDefaultClickSound() {
			return this.withClickSound(SoundEvents.UI_BUTTON_CLICK);
		}
		
		public Builder withClickEvent(final OnPress event) {
			this.onPressEvent = event;
			return this;
		}
		
		public Builder withTooltipEvent(final OnTooltip event) {
			this.onTooltip = event;
			return this;
		}
		
		public Builder withEvents(final OnPress clicked, final OnTooltip hover) {
			return this.withClickEvent(clicked).withTooltipEvent(hover);
		}
		
		public Builder withPlacementMethod(@Nullable IPlacementMethod method) {
			this.method = method;
			return this;
		}
		
		public DynamicButton build() {
			return this.build(0, 0);
		}
		
		public DynamicButton build(int xPos, int yPos) {
			DynamicButton instance = new DynamicButton(this.texture, xPos, yPos, this.width, this.height, this.u, this.v, this.focusU, this.focusV, Objects.requireNonNullElse(this.onPressEvent, DynamicButton.NO_PRESS_EFFECT), Objects.requireNonNullElse(this.onTooltip, Button.NO_TOOLTIP), this.sound);
			instance.setPlacementMethod(method);
			return instance;
		}
		
	}

}
