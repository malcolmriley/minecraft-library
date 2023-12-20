package paragon.minecraft.library.blockentity;

import java.util.Optional;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import paragon.minecraft.library.utilities.Utilities;

public class BlockEntityName implements IBlockEntityFeature {
	
	/* Internal Fields */
	protected Optional<Component> name = Optional.empty();
	
	/* Constants */
	protected static final String FIELD_NAME = "CustomName";
	
	/* Public Methods */
	
	public void clear() {
		this.name = Optional.empty();
	}
	
	public void setName(Component component) {
		this.name = Optional.ofNullable(component);
	}
	
	public boolean hasCustomName() {
		return this.name.isPresent();
	}
	
	public Component getName(Component otherwise) {
		return this.name.orElse(otherwise);
	}
	
	public Component getName() {
		return this.getName(null);
	}
	
	/* IBlockEntityFeature Compliance Methods */
	
	@Override
	public void loadFrom(final ItemStack stack) {
		if (stack.hasCustomHoverName()) {
			this.setName(stack.getHoverName());
		}
	}
	
	@Override
	public void saveTo(final ItemStack stack) {
		if (this.hasCustomName()) {
			stack.setHoverName(this.getName());
		}
	}

	@Override
	public void loadFrom(CompoundTag tag) {
		this.name = Utilities.NBT.tryGetComponent(FIELD_NAME, tag);
	}

	@Override
	public void saveTo(CompoundTag tag) {
		Utilities.NBT.tryPutComponent(tag, FIELD_NAME, this.name);
	}

}
