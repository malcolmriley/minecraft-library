package paragon.minecraft.library.client.ui.elements;

import net.minecraft.util.Mth;

/**
 * Holder class for utility methods pertaining to {@link IPlacementMethod} operation.
 * <p>
 * Some of the methods defined here are themselves compliant with {@link IPlacementMethod}. For those that are not, suitable factory methods
 * are provided via {@link PlacementMethods.Factory} to generate {@link IPlacementMethod} based on unvarying parameters.
 * <p>
 * For {@link IPlacementMethod} employing varying parameters of placement, it may still be useful to call the methods here.
 *
 * @author Malcolm Riley
 */
public class PlacementMethods {

	/* Shared Fields */
	/** Placement Method representing static placement, that is to say, not relative to any other. */
	public static IPlacementMethod ABSOLUTE = (self, other) -> { /* No op, placement is not relative to another. */ };

	private PlacementMethods() {}
	
	/**
	 * Interface specifying a method of placing a dynamic UI element relative to another UI element.
	 * <p>
	 * Such a placement method may alter the position and dimensions of the dynamic element depending on the properties of the non-dynamic element.
	 *
	 * @author Malcolm Riley
	 */
	@FunctionalInterface
	public static interface IPlacementMethod {

		/**
		 * This method should reposition the provided {@link IDynamicGuiElement} relative to the other {@link IGuiElement}.
		 * <p>
		 * This method may or may not alter the position and/or dimensions of the {@link IDynamicGuiElement}, but should not attempt to modify the other {@link IGuiElement}.
		 *
		 * @param self - The element to modify
		 * @param other - The reference element
		 */
		public void reposition(final IDynamicGuiElement self, final IGuiElement other);

	}

	/**
	 * Set the X position of the {@link IDynamicGuiElement} such that it is vertically aligned with a relative position on the Y axis of the reference {@link IGuiElement}.
	 * <p>
	 * For a value of 0.3, the {@link IDynamicGuiElement} would be placed a third of the way rightwards from the reference {@link IGuiElement}.
	 * <p>
	 * For exact centering, see {@link #centerHorizontal(IDynamicGuiElement, IGuiElement)}.
	 *
	 * @param self - The element to modify
	 * @param other - the reference element
	 * @param value - The relative axial position to use
	 */
	public static void alignHorizontal(final IDynamicGuiElement self, final IGuiElement other, float value) {
		final int position = PlacementMethods.alignTo(self.getWidth(), other.getWidth(), other.getX(), value);
		self.setX(position);
	}

	/**
	 * Set the Y position of the {@link IDynamicGuiElement} such that it is vertically aligned with a relative position on the Y axis of the reference {@link IGuiElement}.
	 * <p>
	 * For a value of 0.3, the {@link IDynamicGuiElement} would be placed a third of the way down from the reference {@link IGuiElement}.
	 * <p>
	 * For exact centering, see {@link #centerHorizontal(IDynamicGuiElement, IGuiElement)}.
	 *
	 * @param self - The element to modify
	 * @param other - the reference element
	 * @param value - The relative axial position to use
	 */
	public static void alignVertical(final IDynamicGuiElement self, final IGuiElement other, float value) {
		final int position = PlacementMethods.alignTo(self.getHeight(), other.getHeight(), other.getY(), value);
		self.setY(position);
	}

	/**
	 * Set the X position of the {@link IDynamicGuiElement} such that it is horizontally centered within the reference {@link IGuiElement}.
	 *
	 * @param self - The element to modify
	 * @param other - the reference element
	 */
	public static void centerHorizontal(final IDynamicGuiElement self, final IGuiElement other) {
		self.setX(PlacementMethods.centerDimension(self.getWidth(), other.getWidth(), other.getX()));
	}

	/**
	 * Set the X position of the {@link IDynamicGuiElement} such that it is vertically centered within the reference {@link IGuiElement}.
	 *
	 * @param self - The element to modify
	 * @param other - the reference element
	 */
	public static void centerVertical(final IDynamicGuiElement self, final IGuiElement other) {
		self.setY(PlacementMethods.centerDimension(self.getHeight(), other.getHeight(), other.getY()));
	}

	/**
	 * Set the X and Y position of the {@link IDynamicGuiElement} such that it is horizontally and vertically centered within the reference {@link IGuiElement}.
	 *
	 * @param self - The element to modify
	 * @param other - the reference element
	 */
	public static void center(final IDynamicGuiElement self, final IGuiElement other) {
		PlacementMethods.centerHorizontal(self, other);
		PlacementMethods.centerVertical(self, other);
	}

	/**
	 * Set the position and dimensions of the {@link IDynamicGuiElement} such that it is inset within the reference {@link IGuiElement} with a fixed pixel margin on each side.
	 *
	 * @param self - The element to modify
	 * @param other - the reference element
	 * @param margin - The pixel margin on each side
	 */
	public static void insetFixed(final IDynamicGuiElement self, final IGuiElement other, final int margin) {
		PlacementMethods.insetFixed(self, other, margin, margin);
	}

	/**
	 * Set the position and dimensions of the {@link IDynamicGuiElement} such that it is inset within the reference {@link IGuiElement} with a fixed pixel margin on each side.
	 *
	 * @param self - The element to modify
	 * @param other - the reference element
	 * @param xMargin - The pixel margin on each horizontal side
	 * @param yMargin - The pixel margin on each vertical side
	 */
	public static void insetFixed(final IDynamicGuiElement self, final IGuiElement other, final int xMargin, final int yMargin) {
		PlacementMethods.offsetFixed(self, other, xMargin, yMargin);
		self.setDimensions(PlacementMethods.floorInset(other.getWidth(), xMargin), PlacementMethods.floorInset(other.getHeight(), yMargin));
	}

	/**
	 * Set the position and dimensions of the {@link IDynamicGuiElement} such that it is inset within the reference {@link IGuiElement} with margin on each side relative to the dimensions of the
	 * reference element's sides.
	 *
	 * @param self - The element to modify
	 * @param other - the reference element
	 * @param margin - The relative margin on each side
	 */
	public static void insetRelative(final IDynamicGuiElement self, final IGuiElement other, final float margin) {
		PlacementMethods.insetRelative(self, other, margin, margin);
	}

	/**
	 * Set the position and dimensions of the {@link IDynamicGuiElement} such that it is inset within the reference {@link IGuiElement} with margin on each side relative to the dimensions of the
	 * reference element's sides.
	 *
	 * @param self - The element to modify
	 * @param other - the reference element
	 * @param xMargin - The relative margin on each horizontal side
	 * @param yMargin - The relative margin on each vertical side
	 */
	public static void insetRelative(final IDynamicGuiElement self, final IGuiElement other, final float xMargin, final float yMargin) {
		PlacementMethods.insetFixed(self, other, PlacementMethods.floorMult(other.getWidth(), xMargin), PlacementMethods.floorMult(other.getHeight(), yMargin));
	}

	/**
	 * Copies the X and Y position of the reference {@link IGuiElement} to the provided {@link IDynamicGuiElement}.
	 *
	 * @param self - The element to modify
	 * @param other - the reference element
	 */
	public static void copyPosition(final IDynamicGuiElement self, final IGuiElement other) {
		PlacementMethods.copyX(self, other);
		PlacementMethods.copyY(self, other);
	}

	/**
	 * Copies the X position of the reference {@link IGuiElement} to the provided {@link IDynamicGuiElement}.
	 *
	 * @param self - The element to modify
	 * @param other - the reference element
	 */
	public static void copyX(final IDynamicGuiElement self, final IGuiElement other) {
		self.setX(other.getX());
	}

	/**
	 * Copies the Y position of the reference {@link IGuiElement} to the provided {@link IDynamicGuiElement}.
	 *
	 * @param self - The element to modify
	 * @param other - the reference element
	 */
	public static void copyY(final IDynamicGuiElement self, final IGuiElement other) {
		self.setY(other.getY());
	}

	/**
	 * Copies the dimensions (width and height) of the reference {@link IGuiElement} to the provided {@link IDynamicGuiElement}.
	 *
	 * @param self - The element to modify
	 * @param other - the reference element
	 */
	public static void copyDimensions(final IDynamicGuiElement self, final IGuiElement other) {
		self.setDimensions(other.getWidth(), other.getHeight());
	}

	/**
	 * Copies the position and dimensions of the reference {@link IGuiElement} to the provided {@link IDynamicGuiElement}.
	 *
	 * @param self - The element to modify
	 * @param other - the reference element
	 */
	public static void matching(final IDynamicGuiElement self, final IGuiElement other) {
		PlacementMethods.copyPosition(self, other);
		PlacementMethods.copyDimensions(self, other);
	}

	/**
	 * Sets the X and Y position of the provided {@link IDynamicGuiElement} to the position of the reference {@link IGuiElement} plus an offset.
	 * <p>
	 * This method does not modify the dimensions (width or height) of the provided {@link IDynamicGuiElement}. For that, see {@link #insetFixed(IDynamicGuiElement, IGuiElement, int)}.
	 *
	 * @param self - The element to modify
	 * @param other - the reference element
	 * @param offset - The X and Y offset to apply.
	 * @see #insetFixed(IDynamicGuiElement, IGuiElement, int)
	 */
	public static void offsetFixed(final IDynamicGuiElement self, final IGuiElement other, final int offset) {
		PlacementMethods.offsetFixed(self, other, offset, offset);
	}

	/**
	 * Sets the X and Y position of the provided {@link IDynamicGuiElement} to the position of the reference {@link IGuiElement} plus an offset.
	 * <p>
	 * This method does not modify the dimensions (width or height) of the provided {@link IDynamicGuiElement}. For that, see {@link #insetFixed(IDynamicGuiElement, IGuiElement, int, int)}.
	 *
	 * @param self - The element to modify
	 * @param other - the reference element
	 * @param xOffset - The offset to apply to the X coordinate
	 * @param yOffset - The offset to apply to the Y coordinate
	 * @see #insetFixed(IDynamicGuiElement, IGuiElement, int, int)
	 */
	public static void offsetFixed(final IDynamicGuiElement self, final IGuiElement other, final int xOffset, final int yOffset) {
		PlacementMethods.offsetFixedHorizontal(self, other, xOffset);
		PlacementMethods.offsetFixedVertical(self, other, yOffset);
	}

	/**
	 * Sets the X position of the provided {@link IDynamicGuiElement} to the position of the reference {@link IGuiElement} plus an offset.
	 * <p>
	 * This method does not modify the dimensions (width or height) of the provided {@link IDynamicGuiElement}. For that, see {@link #insetFixed(IDynamicGuiElement, IGuiElement, int, int)}.
	 *
	 * @param self - The element to modify
	 * @param other - the reference element
	 * @param offset - The offset to apply to the X coordinate
	 * @see #insetFixed(IDynamicGuiElement, IGuiElement, int, int)
	 */
	public static void offsetFixedHorizontal(final IDynamicGuiElement self, final IGuiElement other, final int offset) {
		self.setX(other.getX() + offset);
	}

	/**
	 * Sets the Y position of the provided {@link IDynamicGuiElement} to the position of the reference {@link IGuiElement} plus an offset.
	 * <p>
	 * This method does not modify the dimensions (width or height) of the provided {@link IDynamicGuiElement}. For that, see {@link #insetFixed(IDynamicGuiElement, IGuiElement, int, int)}.
	 *
	 * @param self - The element to modify
	 * @param other - the reference element
	 * @param offset - The offset to apply to the X coordinate
	 * @see #insetFixed(IDynamicGuiElement, IGuiElement, int, int)
	 */
	public static void offsetFixedVertical(final IDynamicGuiElement self, final IGuiElement other, final int offset) {
		self.setY(other.getY() + offset);
	}

	/**
	 * Sets the X and Y position of the provided {@link IDynamicGuiElement} to the position of the reference {@link IGuiElement} plus an offset
	 * that is relative to the dimensions of the reference {@link IGuiElement} on each side.
	 * <p>
	 * This method does not modify the dimensions (width or height) of the provided {@link IDynamicGuiElement}. For that, see {@link #insetRelative(IDynamicGuiElement, IGuiElement, float)}.
	 *
	 * @param self - The element to modify
	 * @param other - the reference element
	 * @param offset - The relative offset to apply
	 * @see #insetRelative(IDynamicGuiElement, IGuiElement, float)
	 */
	public static void offsetRelative(final IDynamicGuiElement self, final IGuiElement other, final float offset) {
		PlacementMethods.offsetRelative(self, other, offset, offset);
	}

	/**
	 * Sets the X and Y position of the provided {@link IDynamicGuiElement} to the position of the reference {@link IGuiElement} plus an offset
	 * that is relative to the dimensions of the reference {@link IGuiElement} on each side.
	 * <p>
	 * This method does not modify the dimensions (width or height) of the provided {@link IDynamicGuiElement}. For that, see {@link #insetRelative(IDynamicGuiElement, IGuiElement, float, float)}.
	 *
	 * @param self - The element to modify
	 * @param other - the reference element
	 * @param xOffset - The relative X offset to apply
	 * @param yOffset - The relative Y offset to apply
	 * @see #insetRelative(IDynamicGuiElement, IGuiElement, float, float)
	 */
	public static void offsetRelative(final IDynamicGuiElement self, final IGuiElement other, final float xOffset, final float yOffset) {
		PlacementMethods.offsetFixed(self, other, PlacementMethods.floorMult(other.getWidth(), xOffset), PlacementMethods.floorMult(other.getHeight(), yOffset));
	}

	/**
	 * Sets the Y position of the provided {@link IDynamicGuiElement} such that its bottom is flush with the bottom of the reference {@link IGuiElement}.
	 * <p>
	 * To offset from the bottom by a fixed value, see {@link #offsetFromBottom(IDynamicGuiElement, IGuiElement, int)}.
	 *
	 * @param self - The element to modify
	 * @param other - the reference element
	 * @param margin - The additional fixed margin value to add to the offset.
	 * @see #offsetFromBottom(IDynamicGuiElement, IGuiElement, int)
	 */
	public static void alignToBottom(final IDynamicGuiElement self, IGuiElement other) {
		PlacementMethods.offsetFromBottom(self, self, 0);
	}

	/**
	 * Sets the Y position of the provided {@link IDynamicGuiElement} based on the position of the bottom of the reference {@link IGuiElement} plus a margin value.
	 * <p>
	 * A margin value of 10 would place the provided {@link IDynamicGuiElement} such that its bottom edge is ten pixels above the bottom edge of the reference {@link IGuiOffset}.
	 * <p>
	 * To align with the bottom directly, see {@link #alignToBottom(IDynamicGuiElement, IGuiElement)}.
	 *
	 * @param self - The element to modify
	 * @param other - the reference element
	 * @param margin - The additional fixed margin value to add to the offset.
	 * @see #alignToBottom(IDynamicGuiElement, IGuiElement)
	 */
	public static void offsetFromBottom(final IDynamicGuiElement self, final IGuiElement other, final int margin) {
		final int position = (other.getY() + other.getHeight()) - (self.getHeight() + margin);
		self.setY(position);
	}

	/**
	 * Sets the width of the provided {@link IDynamicGuiElement} to a proportion of the width of the reference {@link IGuiElement} minus a margin.
	 *
	 * @param self - The element to modify
	 * @param other - the reference element
	 * @param proportion - The proportion of the {@link IGuiElement}'s width to copy
	 * @param margin - A margin to subtract from the final width on each horizontal side.
	 */
	public static void horizontallyProportional(final IDynamicGuiElement self, final IGuiElement other, final float proportion, final int margin) {
		self.setWidth(PlacementMethods.calcDimensionWithMargin(proportion, other.getWidth(), margin));
	}

	/**
	 * Sets the height of the provided {@link IDynamicGuiElement} to a proportion of the height of the reference {@link IGuiElement} minus a margin.
	 *
	 * @param self - The element to modify
	 * @param other - the reference element
	 * @param proportion - The proportion of the {@link IGuiElement}'s height to copy
	 * @param margin - A margin to subtract from the final width on each vertical side.
	 */
	public static void verticallyProportional(final IDynamicGuiElement self, final IGuiElement other, final float proportion, final int margin) {
		self.setHeight(PlacementMethods.calcDimensionWithMargin(proportion, other.getHeight(), margin));
	}

	/**
	 * Sets the width and height of the provided {@link IDynamicGuiElement} to a proportion of the height of the reference {@link IGuiElement} minus a margin on all sides.
	 *
	 * @param self - The element to modify
	 * @param other - the reference element
	 * @param proportion - The proportion of the {@link IGuiElement}'s dimensions to copy
	 * @param margin - A margin to subtract from the final width on each side.
	 */
	public static void proportionalTo(final IDynamicGuiElement self, final IGuiElement other, final float proportion, final int margin) {
		PlacementMethods.proportionalTo(self, other, proportion, proportion, margin, margin);
	}

	/**
	 * Sets the width and height of the provided {@link IDynamicGuiElement} to a proportion of the height of the reference {@link IGuiElement} minus a margin on all sides.
	 *
	 * @param self - The element to modify
	 * @param other - the reference element
	 * @param horizontalProportion - The proportion of the {@link IGuiElement}'s width to copy
	 * @param verticalProportion - The proportion of the {@link IGuiElement}'s height to copy
	 * @param horizontalMargin - The margin to subtract from each horizontal side
	 * @param verticalMargin - The margin to subtract from each vertical side
	 */
	public static void proportionalTo(final IDynamicGuiElement self, final IGuiElement other, final float horizontalProportion, final float verticalProportion, final int horizontalMargin, final int verticalMargin) {
		PlacementMethods.horizontallyProportional(self, other, horizontalProportion, horizontalMargin);
		PlacementMethods.verticallyProportional(self, other, verticalProportion, verticalMargin);
	}

	/* Internal Methods */

	protected static int calcDimensionWithMargin(float proportion, int dimension, int margin) {
		return PlacementMethods.floorMult(proportion, PlacementMethods.floorInset(dimension, margin));
	}

	protected static int alignTo(final int selfDim, final int otherDim, final int otherPos, final float relative) {
		return (otherPos + PlacementMethods.floorMult(otherDim, relative)) - PlacementMethods.floorHalf(selfDim);
	}

	protected static int centerDimension(final int selfDim, final int otherDim, final int otherPos) {
		return (otherPos + PlacementMethods.floorHalf(otherDim)) - PlacementMethods.floorHalf(selfDim);
	}

	protected static int floorInset(float original, float margin) {
		return Mth.floor(original - (2 * margin));
	}

	protected static int floorHalf(float original) {
		return PlacementMethods.floorMult(original, 0.5F);
	}

	protected static int floorMult(float original, float scalar) {
		return Mth.floor(original * scalar);
	}

	/* Factory */

	/**
	 * Class for initializing {@link IPlacementMethods} based on unvarying parameters.
	 *
	 * @author Malcolm Riley
	 */
	public static class Factory {

		private Factory() {}

		public static IPlacementMethod horizontallyProportional(float proportion, int margin) {
			return (self, other) -> PlacementMethods.horizontallyProportional(self, other, proportion, margin);
		}

		public static IPlacementMethod verticallyProportional(float proportion, int margin) {
			return (self, other) -> PlacementMethods.verticallyProportional(self, other, proportion, margin);
		}

		public static IPlacementMethod proportionalTo(float proportion, int margin) {
			return Factory.proportionalTo(proportion, proportion, margin, margin);
		}

		public static IPlacementMethod proportionalTo(float horizontal, float vertical, int horizontalMargin, int verticalMargin) {
			return (self, other) -> PlacementMethods.proportionalTo(self, other, horizontal, vertical, horizontalMargin, verticalMargin);
		}

		public static IPlacementMethod insetFixed(int margin) {
			return Factory.insetFixed(margin, margin);
		}

		public static IPlacementMethod insetFixed(int xMargin, int yMargin) {
			return (self, other) -> PlacementMethods.insetFixed(self, other, xMargin, yMargin);
		}

		public static IPlacementMethod insetFixed(int left, int right, int top, int bottom) {
			return (self, other) -> {
				PlacementMethods.offsetFixed(self, other, left, top);
				self.setDimensions(other.getWidth() - (left + right), other.getHeight() - (top + bottom));
			};
		}

		public static IPlacementMethod insetRelative(float margin) {
			return Factory.insetRelative(margin, margin);
		}

		public static IPlacementMethod insetRelative(float xMargin, float yMargin) {
			return (self, other) -> PlacementMethods.insetRelative(self, other, xMargin, yMargin);
		}

		public static IPlacementMethod insetRelative(float left, float right, float top, float bottom) {
			return (self, other) -> {
				PlacementMethods.offsetRelative(self, other, left, top);
				self.setDimensions(PlacementMethods.floorMult(other.getWidth(), left + right), PlacementMethods.floorMult(other.getHeight(), top + bottom));
			};
		}

		public static IPlacementMethod offsetFixed(int offset) {
			return Factory.offsetFixed(offset, offset);
		}

		public static IPlacementMethod offsetFixed(int xOffset, int yOffset) {
			return (self, other) -> PlacementMethods.offsetFixed(self, other, xOffset, yOffset);
		}

		public static IPlacementMethod offsetRelative(float offset) {
			return Factory.offsetRelative(offset, offset);
		}

		public static IPlacementMethod offsetRelative(float xOffset, float yOffset) {
			return (self, other) -> PlacementMethods.offsetRelative(self, other, xOffset, yOffset);
		}

	}

}
