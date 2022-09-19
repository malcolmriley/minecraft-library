package paragon.minecraft.library.client.ui.elements;

/**
 * Interface specifying a method of placing a dynamic UI element relative to another UI element.
 * <p>
 * Such a placement method may alter the position and dimensions of the dynamic element depending on the properties of the non-dynamic element.
 * 
 * @author Malcolm Riley
 */
@FunctionalInterface
public interface IPlacementMethod {
	
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
