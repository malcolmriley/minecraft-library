package paragon.minecraft.library.registration;

import java.util.Objects;
import java.util.function.Supplier;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import paragon.minecraft.library.content.LazyBlockItem;

/**
 * Specialized {@link ContentProvider} for {@link Block} instances.
 * 
 * @author Malcolm Riley
 */
public class BlockProvider extends ContentProvider<Block> {
	
	/* Internal References */
	protected final ItemProvider BLOCK_ITEMS;
	
	public BlockProvider(String modID, ItemProvider itemProvider) {
		super(modID);
		this.BLOCK_ITEMS = Objects.requireNonNull(itemProvider, "ItemProvider instance cannot be null!");
	}

	public BlockProvider(String modID) {
		this(modID, new ItemProvider(modID));
	}
	
	/* Abstract Method Implementation */

	@Override
	protected DeferredRegister<Block> initializeRegistry(String modID) {
		return DeferredRegister.create(ForgeRegistries.BLOCKS, modID);
	}
	
	/* Internal Methods */
	
	protected RegistryObject<Block> addWithItem(final String name, final Supplier<Block> blockSupplier, final Item.Properties properties) {
		final RegistryObject<Block> registered = this.add(name, blockSupplier);
		this.addBlockItem(name, () -> new LazyBlockItem(registered, properties));
		return registered;
	}
	
	protected RegistryObject<Block> addWithItem(final String name, final Supplier<Block> blockSupplier, final Supplier<Item> blockItemSupplier) {
		this.addBlockItem(name, blockItemSupplier);
		return this.add(name, blockSupplier);
	}
	
	protected RegistryObject<Item> addBlockItem(final String name, final Supplier<Item> blockItemSupplier) {
		return this.BLOCK_ITEMS.add(name, blockItemSupplier);
	}

}
