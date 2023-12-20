package paragon.minecraft.library.blockentity;

import java.util.Optional;
import java.util.OptionalLong;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import paragon.minecraft.library.utilities.Utilities;

/**
 * Modularized Loot-Table mechanic for custom {@link BlockEntity}.
 */
public class BlockEntityLootTable implements IBlockEntityFeature {
	
	/* Internal Fields */
	protected Optional<ResourceLocation> table = Optional.empty();
	protected OptionalLong seed = OptionalLong.empty();
	
	/* Constants */
	protected static final String FIELD_TABLE = "LootTable";
	protected static final String FIELD_SEED = "LootTableSeed";
	
	/* Public Methods */
	
	public boolean hasLootTable() {
		return this.table.isPresent();
	}
	
	public boolean hasSeed() {
		return this.seed.isPresent();
	}
	
	public void clearTable() {
		this.table = Optional.empty();
	}
	
	public void clearSeed() {
		this.seed = OptionalLong.empty();
	}
	
	public void clear() {
		this.clearTable();
		this.clearSeed();
	}
	
	public void setLootTable(final ResourceLocation location) {
		this.table = Optional.ofNullable(location);
	}
	
	public void setSeed(final long seed) {
		this.seed = OptionalLong.of(seed);
	}
	
	public void setLoot(final ResourceLocation location, final long seed) {
		this.setLootTable(location);
		this.setSeed(seed);
	}
	
	public void populate(final Container container, final Level level, final Player player, final BlockPos origin) {
		if (this.hasLootTable() && level instanceof ServerLevel serverLevel) {
			final ResourceLocation name = this.table.get();
			final LootTable table = level.getServer().getLootData().getLootTable(name);
			LootParams.Builder builder = new LootParams.Builder(serverLevel)
				.withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(origin));
			if (player instanceof ServerPlayer serverPlayer) {
				CriteriaTriggers.GENERATE_LOOT.trigger(serverPlayer, name);
				builder
					.withLuck(serverPlayer.getLuck())
					.withParameter(LootContextParams.THIS_ENTITY, serverPlayer);
			}
			table.fill(container, builder.create(LootContextParamSets.CHEST), this.seed.orElse(0));
			this.clear();
		}
	}
	
	/* Supertype Override Methods */

	@Override
	public void loadFrom(CompoundTag tag) {
		this.table = Utilities.NBT.tryGetResource(FIELD_TABLE, tag);
		this.seed = Utilities.NBT.tryGetLong(FIELD_SEED, tag);
	}

	@Override
	public void saveTo(CompoundTag tag) {
		Utilities.NBT.tryPutResource(tag, FIELD_TABLE, this.table);
		Utilities.NBT.tryPutLong(tag, FIELD_SEED, this.seed);
	}

}
