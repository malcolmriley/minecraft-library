package paragon.minecraft.library.blockentity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/**
 * Template interface for defining a saveable block-entity feature.
 * <p>
 * Intended to be used where a {@link Capability} would be inappropriate, such as the replication of vanilla featuers
 * such as entity naming and inventory locking (as the NBT structure is saved differently).
 * 
 * @author Malcolm Riley
 */
public interface IBlockEntityFeature {
	
	/**
	 * This method should load the data into this {@link IBlockEntityFeature} from the provided {@link CompoundTag}.
	 * 
	 * @param tag - The tag to load from.
	 */
	public void loadFrom(CompoundTag tag);
	
	/**
	 * This method should save the data of this {@link IBlockEntityFeature} into the provided {@link CompoundTag}.
	 * 
	 * @param tag - The tag instance to save to
	 */
	public void saveTo(CompoundTag tag);
	
	/**
	 * This method should load the data from the provided {@link ItemStack}'s tag.
	 * <p>
	 * By default, this method simply calls {@link #loadFrom(CompoundTag)} using the {@link ItemStack#getTag()} if it has one.
	 * 
	 * @param stack - The {@link ItemStack} holding the relevant {@link CompoundTag} data.
	 */
	public default void loadFrom(final ItemStack stack) {
		if (stack.hasTag()) {
			this.loadFrom(stack.getTag());
		}
	}
	
	/**
	 * This method should save the data from this {@link IBlockEntityFeature} to the provided {@link ItemStack}'s tag.
	 * <p>
	 * By default, this method simply calls {@link #saveTo(CompoundTag)} on {@link ItemStack#getOrCreateTag()}.
	 * 
	 * @param stack - The {@link ItemStack} to save to.
	 */
	public default void saveTo(final ItemStack stack) {
		this.saveTo(stack.getOrCreateTag());
	}

}
