package paragon.minecraft.library.client.ui.elements;

import java.util.Optional;

import javax.annotation.Nullable;

public abstract class AbstractPlaceableGuiElement extends AbstractGuiElement implements IPlaceableGuiElement {

	/* Internal Fields */
	protected Optional<IPlacementMethod> placementMethod;
	
	protected AbstractPlaceableGuiElement(@Nullable IPlacementMethod placementMethod) {
		this.placementMethod = Optional.ofNullable(placementMethod);
	}

	/* Public Methods */

	@Override
	public Optional<IPlacementMethod> getPlacementMethod() {
		return this.placementMethod;
	}

	/* IDynamicGuiElement Compliance Methods */

	@Override
	public void setX(int xPos) {
		this.xPos = xPos;
	}

	@Override
	public void setY(int yPos) {
		this.yPos = yPos;
	}

	@Override
	public void setWidth(int width) {
		this.width = width;
	}

	@Override
	public void setHeight(int height) {
		this.height = height;
	}

	/* Internal Methods */

	protected void setPlacementMethod(@Nullable IPlacementMethod method) {
		this.placementMethod = Optional.ofNullable(method);
	}

}
