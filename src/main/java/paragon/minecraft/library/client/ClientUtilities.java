package paragon.minecraft.library.client;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.math.Matrix4f;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import paragon.minecraft.library.Utilities;

/**
 * Container class for client-only utilities.
 *
 * @author MalcolmRiley
 */
public class ClientUtilities {

	private ClientUtilities() {}
	
	/**
	 * Container class for renderable text component utility methods.
	 * 
	 * @author Malcolm Riley
	 */
	public static class Text {
		
		/**
		 * Attempts to localize and format the provided {@link String} with the provided {@link Style}.
		 * 
		 * @param key - The lang key to localize
		 * @param style - The {@link Style} to use
		 * @return A suitably localized and formatted {@link FormattedText}.
		 */
		public static FormattedText localize(@Nonnull String key, Style style) {
			return FormattedText.of(Text.translate(key), style);
		}
		
		/**
		 * Attempts to localize and format the provided {@link String}.
		 * 
		 * @param key - The lang key to localize
		 * @return A suitably localized and formatted {@link FormattedText}.
		 */
		public static FormattedText localize(@Nonnull String key) {
			return FormattedText.of(Text.translate(key));
		}
		
		/* Internal Methods */
		
		protected static String translate(String key) {
			return Language.getInstance().getOrDefault(key);
		}
		
	}

	/**
	 * Container class for color-related utility methods.
	 *
	 * @author MalcolmRiley
	 */
	public static class Colors {

		private Colors() {}

		/**
		 * Unpacks the red component of the provided packed color, returning it as a {@code float}.
		 *
		 * @param packedColor - The color to unpack
		 * @return The red component of the provided packed color.
		 */
		public static float unpackRed(final int packedColor) {
			return Colors.unpackWithShift(packedColor, 16); // Beginning: 0xFF0000
		}
		
		/**
		 * Returns the value of the provided component as the red channel in a packed color integer.
		 * <p>
		 * The other channels are assumed to have a value of {@literal 0}.
		 * 
		 * @param value - The 0 - 1 value of the desired red channel
		 * @return A packed integer color with the red channel set to the desired value.
		 */
		public static int packRed(final float value) {
			return Colors.packWithShift(value, 16); // To beginning: 0xFF0000;
		}

		/**
		 * Unpacks the green component of the provided packed color, returning it as a {@code float}.
		 *
		 * @param packedColor - The color to unpack
		 * @return The green component of the provided packed color.
		 */
		public static float unpackGreen(final int packedColor) {
			return Colors.unpackWithShift(packedColor, 8); // Middle: 0x00FF00
		}

		/**
		 * Returns the value of the provided component as the green channel in a packed color integer.
		 * <p>
		 * The other channels are assumed to have a value of {@literal 0}.
		 * 
		 * @param value - The 0 - 1 value of the desired green channel
		 * @return A packed integer color with the green channel set to the desired value.
		 */
		public static int packGreen(final float value) {
			return Colors.packWithShift(value, 8); // To middle: 0x00FF00
		}

		/**
		 * Unpacks the blue component of the provided packed color, returning it as a {@code float}.
		 *
		 * @param packedColor - The color to unpack
		 * @return The blue component of the provided packed color.
		 */
		public static float unpackBlue(final int packedColor) {
			return Colors.unpackWithShift(packedColor, 0); // End: 0x0000FF
		}

		/**
		 * Returns the value of the provided component as the blue channel in a packed color integer.
		 * <p>
		 * The other channels are assumed to have a value of {@literal 0}.
		 * 
		 * @param value - The 0 - 1 value of the desired blue channel
		 * @return A packed integer color with the blue channel set to the desired value.
		 */
		public static int packBlue(final float value) {
			return Colors.packWithShift(value, 0); // To end: 0x0000FF
		}
		
		/**
		 * Returns a packed-color representation of the provided channel values.
		 * 
		 * @param red - The 0 - 1 value of the red channel
		 * @param green - The 0 - 1 value of the green channel
		 * @param blue - The 0 - 1 value of the blue channel
		 * @return A packed-color representation of the provided values.
		 */
		public static int packColor(final float red, final float green, final float blue) {
			return Colors.packRed(red) & Colors.packGreen(green) & Colors.packBlue(blue);
		}

		/* Internal Methods */
		
		protected static int packWithShift(final float channel, int shift) {
			return (Mth.floor(255.0F * channel) & 0xFF) << shift;
		}

		protected static float unpackWithShift(final int packedColor, int shift) {
			return ((packedColor >> shift) & 0xFF) / 255.0F;
		}

	}

	/**
	 * Container class for render-related utility methods.
	 *
	 * @author MalcolmRiley
	 */
	public static class Render {
		
		/**
		 * Utility to obtain the frame delta time from the {@link Minecraft} instance.
		 * 
		 * @return The current frame delta time.
		 */
		public static float getDeltaTime() {
			return Minecraft.getInstance().getDeltaFrameTime();
		}
		
		/**
		 * Utility method to fetch the current time in nanoseconds.
		 * 
		 * @return The current time.
		 */
		public static long getNanoTime() {
			return Util.getNanos();
		}
		
		/**
		 * Utility method to fetch the current time in milliseconds.
		 * 
		 * @return The current time.
		 */
		public static long getMilliTime() {
			return Util.getMillis();
		}

		/**
		 * Performs the common setup operations for blitting a texture: setting the shader to {@link GameRenderer#getPositionTexShader()} and
		 * resetting the shader color to white.
		 */
		public static void setupForBlit() {
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			Render.resetShaderColor();
		}

		/**
		 * Convenience method to call {@link RenderSystem#setShaderColor(float, float, float, float)} with {@literal 1, 1, 1, 1}.
		 */
		public static void resetShaderColor() {
			RenderSystem.setShaderColor(1, 1, 1, 1);
		}

		/**
		 * Convenience method to unpack the provided color and pass the result to {@link #setShaderColor(int, float)} with an alpha of {@literal 1}.
		 *
		 * @param packedColor - The packed color to use for setting the shader color.
		 */
		public static void setShaderColor(final int packedColor) {
			Render.setShaderColor(packedColor, 1.0F);
		}

		/**
		 * Convenience method to unpack the provided color and pass the result to {@link RenderSystem#setShaderColor(float, float, float, float)} with an alpha of {@literal 1}.
		 *
		 * @param packedColor - The packed color to use for setting the shader color.
		 */
		public static void setShaderColor(final int packedColor, final float alpha) {
			RenderSystem.setShaderColor(Colors.unpackRed(packedColor), Colors.unpackGreen(packedColor), Colors.unpackBlue(packedColor), alpha);
		}
		
		/**
		 * Draws a "ninepatch" of quads to the provided {@link BufferBuilder} using the provided parameters, and assuming some convenient default parameters.
		 * This is a convenience method that calls {@link #drawNinepatch(BufferBuilder, Matrix4f, float, float, float, float, float, float, float, float, float, float)} with a {@code patchSize} of 8, a {@code patchPadding} of 1, and a {@code textureSize} of 256.
		 * 
		 * @param buffer - The {@link BufferBuilder} to use
		 * @param matrix - The {@link Matrix4f} to use for view transforms
		 * @param z - The Z depth of the quad to draw, in {@link Screen} derivatives this is typically {@link Screen#getBlitOffset()}.
		 * @param xPos - The upper-left X coordinate to begin drawing
		 * @param yPos - The upper-left Y coordinate to begin drawing
		 * @param width - The final width of the desired ninepatch
		 * @param height - The final height of the desired ninepatch
		 * @param minU - The upper-left U texel coordinate in the source texture
		 * @param minV - The upper-right V texel coordinate in the source texture
		 * @see #drawNinepatch(BufferBuilder, Matrix4f, float, float, float, float, float, float, float, float, float, float)
		 */
		public static void drawNinepatch(final BufferBuilder buffer, final Matrix4f matrix, final float z, final float xPos, final float yPos, final float width, final float height, final float minU, final float minV) {
			Render.drawNinepatch(buffer, matrix, z, xPos, yPos, width, height, minU, minV, 8, 1, 256);
		}
		
		/**
		 * Draws a "ninepatch" of quads to the provided {@link BufferBuilder} using the provided parameters.
		 * <p>
		 * This method assumes a 3x3 source grid for the ninepatch with the same relative positionings of each component.
		 * <p>
		 * <b>Important:</b> This is a convenience method that calls {@link #drawQuadDirect(BufferBuilder, Matrix4f, float, float, float, float, float, float, float, float, float)} after
		 * performing some minor calculations using the provided parameters. This method does not perform any kind of setup or cleanup whatsoever. 
		 * It does not set any {@link RenderSystem} state, nor does it begin or end the buffer build process.
		 * 
		 * @param buffer - The {@link BufferBuilder} to use
		 * @param matrix - The {@link Matrix4f} to use for view transforms
		 * @param z - The Z depth of the quad to draw, in {@link Screen} derivatives this is typically {@link Screen#getBlitOffset()}.
		 * @param xPos - The upper-left X coordinate to begin drawing
		 * @param yPos - The upper-left Y coordinate to begin drawing
		 * @param width - The final width of the desired ninepatch
		 * @param height - The final height of the desired ninepatch
		 * @param minU - The upper-left U texel coordinate in the source texture
		 * @param minV - The upper-right V texel coordinate in the source texture
		 * @param patchSize - The UV texel dimensions of each ninepatch grid square in the source texture
		 * @param patchPadding - The number of UV texels padding each grid square in the source texture. For "flush" ninepatches in the source texture, this value should be {@literal 0}.
		 * @param textureSize - The original texel texture dimensions of the source texture.
		 * @see #drawNinepatch(BufferBuilder, Matrix4f, float, float, float, float, float, float, float)
		 */
		public static void drawNinepatch(final BufferBuilder buffer, final Matrix4f matrix, final float z, final float xPos, final float yPos, final float width, final float height, final float minU, final float minV, final float patchSize, final float patchPadding, final float textureSize) {
			final float patchOffset = patchSize + patchPadding;
			final float doublePatchSize = 2.0F * patchSize;
			final float doublePatchOffset = 2.0F * patchOffset;
			
			final float midWidth = width - doublePatchSize; // Width of middle minus two corners
			final float midHeight = height - doublePatchSize; // Width of middle minus two corners
			
			final float midX = xPos + patchSize; // X of middle patches
			final float maxX = midX + midWidth; // X of rightmost patches
			
			final float midY = yPos + patchSize; // Y of middle patches
			final float maxY = midY + midHeight; // Y of bottom-most patches
			
			final float midU = minU + patchOffset; // U of middle patches
			final float maxU = minU + doublePatchOffset; // U of rightmost patches
			
			final float midV = minV + patchOffset; // V of middle patches
			final float maxV = minV + doublePatchOffset; // V of bottom-most patches
			
			// Always draw corners
			ClientUtilities.Render.drawQuad(buffer, matrix, z, xPos, yPos, patchSize, patchSize, minU, minV, patchSize, patchSize, textureSize, textureSize); // Upper-Left
			ClientUtilities.Render.drawQuad(buffer, matrix, z, maxX, yPos, patchSize, patchSize, maxU, minV, patchSize, patchSize, textureSize, textureSize); // Upper-Right
			ClientUtilities.Render.drawQuad(buffer, matrix, z, xPos, maxY, patchSize, patchSize, minU, maxV, patchSize, patchSize, textureSize, textureSize); // Bottom-Left
			ClientUtilities.Render.drawQuad(buffer, matrix, z, maxX, maxY, patchSize, patchSize, maxU, maxV, patchSize, patchSize, textureSize, textureSize); // Bottom-Right
			
			// Conditional upon dimensions
			final boolean positiveWidth = midWidth > 0;
			final boolean positiveHeight = midHeight > 0;
			
			if (positiveWidth) {
				ClientUtilities.Render.drawQuad(buffer, matrix, z, midX, yPos, midWidth, patchSize, midU, minV, patchSize, patchSize, textureSize, textureSize); // Upper-Mid
				ClientUtilities.Render.drawQuad(buffer, matrix, z, midX, maxY, midWidth, patchSize, midU, maxV, patchSize, patchSize, textureSize, textureSize); // Bottom-Mid
			}
			if (positiveHeight) {
				ClientUtilities.Render.drawQuad(buffer, matrix, z, xPos, midY, patchSize, midHeight, minU, midV, patchSize, patchSize, textureSize, textureSize); // Mid-Left
				ClientUtilities.Render.drawQuad(buffer, matrix, z, maxX, midY, patchSize, midHeight, maxU, midV, patchSize, patchSize, textureSize, textureSize); // Mid-Right
			}
			if (positiveWidth && positiveHeight) {
				ClientUtilities.Render.drawQuad(buffer, matrix, z, midX, midY, midWidth, midHeight, midU, midV, patchSize, patchSize, textureSize, textureSize); // Mid-Mid
			}
			
		}
		
		/**
		 * Draws a quad into the provided {@link BufferBuilder} using the provided parameters and assuming a source texture size of 256x256 (standard for UI textures).
		 * <p>
		 * <b>Important:</b> This is a convenience method that calls {@link #drawQuadDirect(BufferBuilder, Matrix4f, float, float, float, float, float, float, float, float, float)} after
		 * performing some minor calculations using the provided parameters. This method does not perform any kind of setup or cleanup whatsoever. 
		 * It does not set any {@link RenderSystem} state, nor does it begin or end the buffer build process.
		 * 
		 * @param buffer - The {@link BufferBuilder} to use
		 * @param matrix - The {@link Matrix4f} to use for view transforms
		 * @param z - The Z depth of the quad to draw, in {@link Screen} derivatives this is typically {@link Screen#getBlitOffset()}.
		 * @param xPos - The X screen coordinate to draw to
		 * @param yPos - The Y screen coordinate to draw to
		 * @param width - The total width of the desired quad
		 * @param height - The total height of the desired quad
		 * @param uPos - The origin U position (upper-left X coordinate in the source texture)
		 * @param vPos - The origin V position (upper-left Y coordinate in the source texture)
		 * @see #drawQuad(BufferBuilder, Matrix4f, float, float, float, float, float, float, float, float, float)
		 */
		public static void drawQuad(@Nonnull final BufferBuilder buffer, @Nonnull final Matrix4f matrix, final float z, final float xPos, final float yPos, final float width, final float height, final float uPos, final float vPos) {
			Render.drawQuad(buffer, matrix, z, xPos, yPos, width, height, uPos, vPos, 256.0F, 256.0F);
		}
		
		/**
		 * Draws a quad into the provided {@link BufferBuilder} using the provided parameters.
		 * <p>
		 * <b>Important:</b> This is a convenience method that calls {@link #drawQuadDirect(BufferBuilder, Matrix4f, float, float, float, float, float, float, float, float, float)} after
		 * performing some minor calculations using the provided parameters. This method does not perform any kind of setup or cleanup whatsoever. 
		 * It does not set any {@link RenderSystem} state, nor does it begin or end the buffer build process.
		 * <p>
		 * This method assumes that the texel draw dimensions match the source UV texel dimensions. For a variation that does not, see {@link #drawQuad(BufferBuilder, Matrix4f, float, float, float, float, float, float, float, float, float, float, float)}.
		 * 
		 * @param buffer - The {@link BufferBuilder} to use
		 * @param matrix - The {@link Matrix4f} to use for view transforms
		 * @param z - The Z depth of the quad to draw, in {@link Screen} derivatives this is typically {@link Screen#getBlitOffset()}.
		 * @param xPos - The X screen coordinate to draw to
		 * @param yPos - The Y screen coordinate to draw to
		 * @param width - The total width of the desired quad
		 * @param height - The total height of the desired quad
		 * @param uPos - The origin U position (upper-left X coordinate in the source texture)
		 * @param vPos - The origin V position (upper-left Y coordinate in the source texture)
		 * @param textureWidth - The total width of the source texture
		 * @param textureHeight - The total height of the source texture
		 * @see #drawQuad(BufferBuilder, Matrix4f, float, float, float, float, float, float, float, float, float, float, float)
		 * @see #drawQuadDirect(BufferBuilder, Matrix4f, float, float, float, float, float, float, float, float, float)
		 */
		public static void drawQuad(@Nonnull final BufferBuilder buffer, @Nonnull final Matrix4f matrix, final float z, final float xPos, final float yPos, final float width, final float height, final float uPos, final float vPos, final float textureWidth, final float textureHeight) {
			Render.drawQuad(buffer, matrix, z, xPos, yPos, width, height, uPos, vPos, width, height, textureWidth, textureHeight);
		}
		
		/**
		 * Draws a quad into the provided {@link BufferBuilder} using the provided parameters.
		 * <p>
		 * <b>Important:</b> This is a convenience method that calls {@link #drawQuadDirect(BufferBuilder, Matrix4f, float, float, float, float, float, float, float, float, float)} after
		 * performing some minor calculations using the provided parameters. This method does not perform any kind of setup or cleanup whatsoever. 
		 * It does not set any {@link RenderSystem} state, nor does it begin or end the buffer build process.
		 * <p>
		 * This method assumes different texel dimensions for the desired quad than the source texture UV. For a version that does not, see {@link #drawQuad(BufferBuilder, Matrix4f, float, float, float, float, float, float, float, float, float)}.
		 * 
		 * @param buffer - The {@link BufferBuilder} to use
		 * @param matrix - The {@link Matrix4f} to use for view transforms
		 * @param z - The Z depth of the quad to draw, in {@link Screen} derivatives this is typically {@link Screen#getBlitOffset()}.
		 * @param xPos - The X screen coordinate to draw to
		 * @param yPos - The Y screen coordinate to draw to
		 * @param width - The total width of the desired quad
		 * @param height - The total height of the desired quad
		 * @param uPos - The origin U position (upper-left X coordinate in the source texture)
		 * @param vPos - The origin V position (upper-left Y coordinate in the source texture)
		 * @param uvWidth - The UV width in the source texture
		 * @param uvHeight - The UV height in the source texture
		 * @param textureWidth - The total width of the source texture
		 * @param textureHeight - The total height of the source texture
		 * @see #drawQuad(BufferBuilder, Matrix4f, float, float, float, float, float, float, float, float, float)
		 * @see #drawQuadDirect(BufferBuilder, Matrix4f, float, float, float, float, float, float, float, float, float)
		 */
		public static void drawQuad(@Nonnull final BufferBuilder buffer, @Nonnull final Matrix4f matrix, final float z, final float xPos, final float yPos, final float width, final float height, final float uPos, final float vPos, final float uvWidth, final float uvHeight, final float textureWidth, final float textureHeight) {
			Render.drawQuadDirect(buffer, matrix, z, xPos, xPos + width, yPos, yPos + height, uPos / textureWidth, (uPos + uvWidth) / textureWidth, vPos / textureHeight, (vPos + uvHeight) / textureHeight);
		}
		
		/**
		 * Directly draws a quad into the provided {@link BufferBuilder} using the provided parameters.
		 * <p>
		 * <b>Important:</b> This is purely a convenience method for calling a quad-drawing procedure on the provided {@link BufferBuilder}.
		 * This method does not perform any kind of setup or cleanup whatsoever. It does not set any {@link RenderSystem} state, nor does it begin or end the buffer build process.
		 * <p>
		 * Notes:
		 * <li> The X and Y coordinate parameters will form the on-screen positions of the four vertices of the quad. </li>
		 * <li> The U and V coordinate parameters are the texture coordinates in the input texture and should be a value from 0 to 1. </li>
		 * <li> To obtain reasonable U and V coordinates from known pixel coordinates in the input image, just divide the desired start or end pixel by the total pixel width of the image. </li>
		 * <p>
		 * For less granular methods that perform some of the above reccommended calculations, see {@link #drawQuad(BufferBuilder, Matrix4f, float, float, float, float, float, float, float)} and
		 * {@link #drawQuad(BufferBuilder, Matrix4f, float, float, float, float, float, float, float, float, float)}.
		 * 
		 * @param buffer - The {@link BufferBuilder} to use
		 * @param matrix - The {@link Matrix4f} to use for view transforms
		 * @param z - The Z depth of the quad to draw, in {@link Screen} derivatives this is typically {@link Screen#getBlitOffset()}.
		 * @param minX - The min X vertex coordinate
		 * @param maxX - The max X vertex coordinate
		 * @param minY - The min Y vertex coordinate
		 * @param maxY - The max Y vertex coordinate
		 * @param minU - The min U texel coordinate (range 0 - 1)
		 * @param maxU - The max U texel coordinate (range 0 - 1)
		 * @param minV - The min V texel coordinate (range 0 - 1)
		 * @param maxV - The max V texel coordinate (range 0 - 1)
		 * @see #drawQuad(BufferBuilder, Matrix4f, float, float, float, float, float, float, float)
		 * @see #drawQuad(BufferBuilder, Matrix4f, float, float, float, float, float, float, float, float, float)
		 */
		public static void drawQuadDirect(@Nonnull final BufferBuilder buffer, @Nonnull final Matrix4f matrix, final float z, final float minX, final float maxX, final float minY, final float maxY, final float minU, final float maxU, final float minV, final float maxV) {
			buffer.vertex(matrix, minX, maxY, z).uv(minU, maxV).endVertex();
			buffer.vertex(matrix, maxX, maxY, z).uv(maxU, maxV).endVertex();
			buffer.vertex(matrix, maxX, minY, z).uv(maxU, minV).endVertex();
			buffer.vertex(matrix, minX, minY, z).uv(minU, minV).endVertex();
		}

	}

	/**
	 * Container class for UI-related utility methods.
	 *
	 * @author Malcolm Riley
	 */
	public static class UI {

		/**
		 * Utility method to open a client-only {@link Screen} derivative.
		 * <p>
		 * This method should ONLY be called from the client, and furthermore, should ONLY be used for {@link Screen} implementations that do not require ANY client-server synchronization!
		 *
		 * @param initializer - A {@link Supplier} providing the {@link Screen} to be opened.
		 * @return The instance provided by the {@link Supplier}, or {@code null} if the supplier returned {@code null}.
		 */
		public static <T extends Screen> @Nullable T openClientOnlyUI(final Supplier<T> initializer) {
			return Utilities.Misc.acceptIfNonNull(initializer, UI::openUIOnClient);
		}

		/**
		 * Utility method to open a client-only {@link Screen} derivative.
		 * <p>
		 * This method should ONLY be called from the client, and furthermore, should ONLY be used for {@link Screen} implementations that do not require ANY client-server synchronization!
		 *
		 * @param initializer - A {@link Supplier} providing the {@link Screen} to be opened.
		 * @param transformer - A method to modify the initialized {@link Screen}, if possible.
		 * @return The instance provided by the {@link Supplier}, or {@code null} if the supplier returned {@code null}.
		 */
		public static <T extends Screen> @Nullable T openClientOnlyUI(final Supplier<T> initializer, Consumer<T> transformer) {
			return Utilities.Misc.acceptIfNonNull(initializer, transformer.andThen(UI::openUIOnClient));
		}

		/**
		 * Calls {@link #openClientOnlyUI(Supplier)}, wrapping the result in an {@link Optional}.
		 *
		 * @param initializer - A {@link Supplier} providing the {@link Screen} to be opened.
		 * @return An {@link Optional} containing the reference (or {@code null}) provided by the {@link Supplier}.
		 */
		public static <T extends Screen> Optional<T> tryOpenClientOnlyUI(final Supplier<T> initializer) {
			return Optional.ofNullable(UI.openClientOnlyUI(initializer));
		}

		/**
		 * Calls {@link #openClientOnlyUI(Supplier)}, wrapping the result in an {@link Optional}.
		 *
		 * @param initializer - A {@link Supplier} providing the {@link Screen} to be opened.
		 * @param transformer - A method to modify the initialized {@link Screen}, if possible.
		 * @return An {@link Optional} containing the reference (or {@code null}) provided by the {@link Supplier}.
		 */
		public static <T extends Screen> Optional<T> tryOpenClientOnlyUI(final Supplier<T> initializer, Consumer<T> transformer) {
			return Optional.ofNullable(UI.openClientOnlyUI(initializer, transformer));
		}
		
		/**
		 * Returns the total UI-scaled client window width assuming the provided margin on both sides.
		 * 
		 * @param margin - The margin to apply to both sides
		 * @return The UI-scaled width, minus double the provided margin.
		 */
		public static int getWindowWidthWithMargin(final int margin) {
			return UI.getSizeWithMargin(UI.getWindow().getGuiScaledWidth(), margin);
		}
		
		/**
		 * Returns the total UI-scaled client window height assuming the provided margin on both sides.
		 * 
		 * @param margin - The margin to apply to both sides
		 * @return The UI-scaled height, minus double the provided margin.
		 */
		public static int getWindowHeightWithMargin(final int margin) {
			return UI.getSizeWithMargin(UI.getWindow().getGuiScaledHeight(), margin);
		}
		
		/**
		 * Shortcut for accessing the minecraft client {@link Window} object.
		 * <p>
		 * Route calls to this method in case things change.
		 * 
		 * @return The {@link Window} instance.
		 */
		public static Window getWindow() {
			return Minecraft.getInstance().getWindow();
		}
		
		/**
		 * Simple convenience method to reduce the original value by double the margin value.
		 * <p>
		 * Good for calculating the dimensions of an inset UI element.
		 * 
		 * @param original - The original value
		 * @param margin - The value of the margin
		 * @return The original value minus double the margin.
		 */
		public static int getSizeWithMargin(final int original, final int margin) {
			return original - (2 * margin);
		}
		
		/**
		 * Simple convenience method that returns half of the difference between the first and second parameters.
		 * <p>
		 * Good for calculating the top-left coordinate of an inner element so as to be placed within the outer element.
		 * @param outerwidth - The width of the outer element
		 * @param innerwidth - The width of the inner element
		 * @return The top-left coordinate of the inner element.
		 */
		public static int getCenteredWithin(final int outerwidth, final int innerwidth) {
			return (outerwidth - innerwidth) / 2;
		}
		
		/**
		 * Method to close the active screens.
		 */
		public static void closeScreen() {
			UI.openUIOnClient(null);
		}

		/* Internal Methods */

		protected static void openUIOnClient(Screen screen) {
			Minecraft.getInstance().setScreen(screen);
		}

	}

}
