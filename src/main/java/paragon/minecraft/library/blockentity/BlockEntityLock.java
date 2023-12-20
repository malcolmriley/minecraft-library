package paragon.minecraft.library.blockentity;

import com.google.common.base.Objects;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.LockCode;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;

public class BlockEntityLock implements IBlockEntityFeature {
	
	/* Internal Fields */
	protected LockCode lock = LockCode.NO_LOCK;
	
	/* Public Methods */
	
	public boolean hasLock() {
		return !Objects.equal(this.lock, LockCode.NO_LOCK);
	}
	
	public void clear() {
		this.lock = LockCode.NO_LOCK;
	}
	
	public boolean canBeUnlockedBy(final Player player, final Component message) {
		return BaseContainerBlockEntity.canUnlock(player, this.lock, message);
	}

	/* IBlockEntityFeature Compliance Methods */

	@Override
	public void loadFrom(CompoundTag tag) {
		this.lock = LockCode.fromTag(tag);
	}

	@Override
	public void saveTo(CompoundTag tag) {
		this.lock.addToTag(tag);
	}

}
