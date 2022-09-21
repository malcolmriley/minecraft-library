package paragon.minecraft.library.client.ui.elements;

import com.mojang.math.Matrix4f;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import paragon.minecraft.library.client.ClientUtilities;

/**
 * Base class for a {@link ClientTooltipComponent} that draws icons in addition to text.
 * <p>
 * The text rendering is already implemented by this class, so subclasses should override {@link #renderImage(Font, int, int, com.mojang.blaze3d.vertex.PoseStack, net.minecraft.client.renderer.entity.ItemRenderer, int)}.
 * 
 * @author Malcolm Riley
 * @param <T> The {@link Component} type to be used for drawing text.
 */
public abstract class AbstractIconTooltip<T extends Component> implements ClientTooltipComponent {

	/* Internal Fields */
	protected final int ICON_WIDTH;
	protected final int HEIGHT;
	protected final boolean TEXT_DROP_SHADOW;
	protected final int TEXT_BACKGROUND_COLOR;
	protected final int TEXT_PACKED_LIGHT;
	protected final T TEXT;
	
	/* Internal Constants */
	protected static final int DEFAULT_ICON_WIDTH = 12;
	protected static final int DEFAULT_LINE_HEIGHT_PADDING = 2;
	protected static final int DEFAULT_FONT_PACKED_LIGHT = 0xF000F0; // Magic number from game internals
	
	protected AbstractIconTooltip(final T text) {
		this(text, AbstractIconTooltip.DEFAULT_ICON_WIDTH, -1, 0, false, AbstractIconTooltip.DEFAULT_FONT_PACKED_LIGHT);
	}
	
	protected AbstractIconTooltip(final T text, final int iconWidth, final int height, final int textBackgroundColor, final boolean doShadow, final int textLight) {
		this.TEXT = text;
		this.ICON_WIDTH = iconWidth;
		this.HEIGHT = Math.max(height, ClientUtilities.Text.getDefaultTextLineHeight(AbstractIconTooltip.DEFAULT_LINE_HEIGHT_PADDING));
		this.TEXT_BACKGROUND_COLOR = textBackgroundColor;
		this.TEXT_DROP_SHADOW = doShadow;
		this.TEXT_PACKED_LIGHT = textLight;
	}
	
	/* Supertype Override Methods */
	
	@Override
	public void renderText(final Font font, final int xPos, final int yPos, Matrix4f matrix, MultiBufferSource.BufferSource buffer) {
		font.drawInBatch(this.TEXT, xPos + this.ICON_WIDTH, yPos + 1, -1, this.TEXT_DROP_SHADOW, matrix, buffer, false, this.TEXT_BACKGROUND_COLOR, this.TEXT_PACKED_LIGHT);
	}
	
	/* ClientTooltipComponent Compliance Methods */

	@Override
	public int getHeight() {
		return this.HEIGHT;
	}

	@Override
	public int getWidth(Font font) {
		return font.width(this.TEXT) + this.ICON_WIDTH;
	}

}
