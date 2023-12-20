package paragon.minecraft.library.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class SimpleBlockEntity extends BlockEntity {

	public SimpleBlockEntity(BlockEntityType<?> type, BlockPos position, BlockState state) {
		super(type, position, state);
	}
	
	/* Supertype Override Methods */
	
	@Override
	public void load(final CompoundTag tag) {
		super.load(tag);
		this.loadData(tag);
	}
	
	@Override
	public void saveAdditional(final CompoundTag tag) {
		super.saveAdditional(tag);
		this.saveData(tag);
	}
	
	/* Abstract Methods */
	
	protected abstract void loadData(final CompoundTag tag);
	protected abstract void saveData(final CompoundTag tag);

}
