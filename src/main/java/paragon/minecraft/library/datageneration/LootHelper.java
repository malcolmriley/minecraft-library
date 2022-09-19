package paragon.minecraft.library.datageneration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.mojang.datafixers.util.Pair;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.loot.BlockLoot;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTable.Builder;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.functions.CopyNameFunction;
import net.minecraft.world.level.storage.loot.functions.CopyNbtFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.nbt.ContextNbtProvider;
import net.minecraftforge.registries.RegistryObject;

/**
 * Data-generator class for assisting with generating JSON {@link LootTable}.
 *
 * @author Malcolm Riley
 */
public abstract class LootHelper extends LootTableProvider {

	public LootHelper(DataGenerator generator) {
		super(generator);
	}

	/* Supertype Override Methods */

	@Override
	public final List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> getTables() {
		List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> list = new ArrayList<>();
		this.addLootTables(list::add);
		return Collections.unmodifiableList(list);
	}

	@Override
	protected final void validate(Map<ResourceLocation, LootTable> map, ValidationContext validationtracker) {
		map.forEach((key, value) -> LootTables.validate(validationtracker, key, value));
	}

	/* Abstract Methods */

	/**
	 * Implementors should use this method to add loot tables.
	 * <p>
	 * The general idea is to invoke the provided {@link Consumer} using {@link Pair#of(Object, Object)} using {@link LootContextParamSet} and a supplier to the individual table-providing method or class.
	 * <p>
	 * A suggested implementation for {@link Block} loot tables is to extend {@link BlockLootTables} and then use {@code ExtendingClass::new} to create a {@link Supplier} from it, providing it to this method.
	 *
	 * @param registrar
	 */
	protected abstract void addLootTables(ILootRegistrar registrar);
	
	protected <T extends Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>> void addLootForContext(final ILootRegistrar registrar, LootContextParamSet params, T provider) {
		registrar.accept(Pair.of(provider, params));
	}
	
	protected <T extends Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>> void addBlockLoot(final ILootRegistrar registrar, final T provider) {
		this.addLootForContext(registrar, LootContextParamSets.BLOCK, provider);
	}
	
	/**
	 * Sweep-under-the-carpet interface to shorten the frightening generic parameterization required in {@link LootTableProvider}.
	 * 
	 * @author Malcolm Riley
	 */
	public static interface ILootRegistrar extends Consumer<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> { }
	
	/**
	 * Sweep-under-the-carpet interface to shorten the frightening generic parameterization required in {@link LootTableProvider}.
	 * 
	 * @author Malcolm Riley
	 */
	public static interface ILootProvider extends Consumer<BiConsumer<ResourceLocation, LootTable.Builder>> { };
	
	public static abstract class AbstractLootProvider implements ILootProvider {
		
		/* Internal Fields */
		protected final Map<ResourceLocation, Builder> TABLES = new HashMap<>();
		
		/* ILootProvider Compliance Methods */

		@Override
		public void accept(BiConsumer<ResourceLocation, Builder> registrar) {
			this.addTables();
			this.TABLES.forEach((key, value) -> {
				this.validate(key, value);
				registrar.accept(key, value);
			});
		}
		
		/* Abstract Methods */
		
		protected abstract void addTables();
		
		/* Internal Methods */
		
		protected void validate(ResourceLocation key, Builder value) {
			// No-op but can be overridden in subclass for individual validation
		}
		
		protected void add(ResourceLocation name, Builder builder) {
			this.TABLES.put(Objects.requireNonNull(name, "LootTable ResourceLocation cannot be null!"), Objects.requireNonNull(builder, "LootTable builder instance cannot be null!"));
		}
		
	}

	/**
	 * Data generation helper for {@link Block} loot tables
	 */
	public static abstract class BlockLootTables extends BlockLoot {
		
		/* Abstract Methods */

		/**
		 * This method should be used to register loot tables.
		 */
		@Override
		public abstract void addTables();

		/**
		 * This method should return an {@link Iterable} over blocks for which there should be a loot table registered.
		 */
		@Override
		protected abstract Iterable<Block> getKnownBlocks();
		
		/* Internal Methods */
		
		/**
		 * Convenience method to register a loot table for the {@link Block} within the provided {@link RegistryObject}, if {@link RegistryObject#isPresent()} is {@code TRUE}.
		 * <p>
		 * Merely calls {@link RegistryObject#isPresent()}, and if {@code TRUE}, then calls {@link BlockLootTables#registerLootTable(Block, Builder)} using {@link RegistryObject#get()}.
		 * @param registryObject - The {@link RegistryObject} containing the {@link Block} to add a {@link LootTable} for
		 * @param builder - The builder for the {@link LootTable}
		 */
		protected void addLootFor(RegistryObject<Block> registryObject, LootTable.Builder builder) {
			if (registryObject.isPresent()) {
				this.add(registryObject.get(), builder);
			}
		}

		/**
		 * Convenience method to register a loot table for the {@link Block} within the provided {@link RegistryObject}, if {@link RegistryObject#isPresent()} is {@code TRUE}.
		 * <p>
		 * Merely calls {@link RegistryObject#isPresent()}, and if {@code TRUE}, then calls {@link BlockLootTables#registerLootTable(Block, Function)} using {@link RegistryObject#get()}.
		 * @param registryObject - The {@link RegistryObject} containing the {@link Block} to add a {@link LootTable} for
		 * @param builderFunction - The function providing the {@link LootTable.Builder} for the {@link LootTable}
		 */
		protected void addLootFor(RegistryObject<Block> registryObject, Function<Block, LootTable.Builder> builderFunction) {
			if (registryObject.isPresent()) {
				this.add(registryObject.get(), builderFunction);
			}
		}
		
		/**
		 * Convenience method that simply calls {@link #registerDropSelfLootTable(Block)} on each provided non-empty {@link RegistryObject}.
		 * 
		 * @param registryObjects - A list of {@link RegistryObject} to add self-drop loot tables for
		 */
		@SafeVarargs // Mere iterate and fetch. Should not used in runtime code anyways.
		protected final void dropSelf(RegistryObject<Block> ... registryObjects) {
			Stream.of(registryObjects).filter(RegistryObject::isPresent).forEach(element -> this.dropSelf(element.get()));
		}
		
		/**
		 * Convenience function that returns a simple block-name copying {@link LootItemFunction.Builder}.
		 * 
		 * @return A {@link LootItemFunction.Builder} pre-configured to copy the custom {@link BlockEntity} name.
		 */
		protected static final LootItemFunction.Builder copyBlockName() {
			return CopyNameFunction.copyName(CopyNameFunction.NameSource.BLOCK_ENTITY);
		}
		
		/**
		 * Convenience method that produces a function to copy inventory-related {@link CompoundTag} data such as locks and loot tables.
		 * 
		 * @return A {@link LootItemFunction.Builder} pre-configured to copy relevant inventory metadata.
		 */
		protected static final LootItemFunction.Builder copyInventoryMetadata() {
			return CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY).copy("Lock", "BlockEntityTag.Lock").copy("LootTable", "BlockEntityTag.LootTable").copy("LootTableSeed", "BlockEntityTag.LootTableSeed");
		}
		
	}

}
