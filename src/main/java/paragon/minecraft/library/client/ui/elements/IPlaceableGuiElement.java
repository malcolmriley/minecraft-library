package paragon.minecraft.library.client.ui.elements;

import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;

import paragon.minecraft.library.client.ui.elements.PlacementMethods.IPlacementMethod;

public interface IPlaceableGuiElement extends IDynamicGuiElement {

	public Optional<IPlacementMethod> getPlacementMethod();

	public default void updatePlacement(@Nullable IGuiElement other) {
		final Optional<IPlacementMethod> method = this.getPlacementMethod();
		if (method.isPresent() && Objects.nonNull(other)) {
			method.get().reposition(this, other);
		}
	}

}
