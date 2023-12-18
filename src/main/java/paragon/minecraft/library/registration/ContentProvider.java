package paragon.minecraft.library.registration;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.MenuType.MenuSupplier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public abstract class ContentProvider<T, D extends DeferredRegister<T>> implements IEventBusListener {

	/* Internal Methods */
	protected final D REGISTRAR;

	/* Constants */
	private static final String EXCEPTION_NULL_REGISTRAR = "DeferredRegister instance cannot be null.";

	protected ContentProvider(final D deferred) {
		this.REGISTRAR = Objects.requireNonNull(deferred, EXCEPTION_NULL_REGISTRAR);
	}

	/* Public Methods */

	@Override
	public final void registerTo(IEventBus bus) {
		this.REGISTRAR.register(bus);
	}
	
	public final <C extends Collection<T>> C collectAvailable(Function<List<T>, C> transformer) {
		return transformer.apply(this.streamAvailable().toList());
	}

	public final void forEachAvailable(Consumer<T> consumer) {
		this.streamAvailable().forEach(consumer);
	}

	public final Stream<T> streamAvailable() {
		return this.REGISTRAR.getEntries().stream().filter(holder -> holder.isBound()).map(holder -> holder.get());
	}
	
	/* Subtype Implementations */
	
	public static class ItemProvider extends ContentProvider<Item, DeferredRegister.Items> {

		protected ItemProvider(final String modID) {
			super(DeferredRegister.createItems(modID));
		}
		
		protected <V extends Item> DeferredItem<V> add(String name, Supplier<V> initializer) {
			return this.REGISTRAR.register(name, initializer);
		}
		
		protected DeferredItem<Item> add(String name, Item.Properties properties) {
			return this.REGISTRAR.registerSimpleItem(name, properties);
		}
		
		protected DeferredItem<Item> add(String name) {
			return this.REGISTRAR.registerSimpleItem(name);
		}
		
	}
	
	public static class BlockProvider extends ContentProvider<Block, DeferredRegister.Blocks> {
		
		protected final DeferredRegister.Items ITEM_REGISTRAR;
		
		protected BlockProvider(final String modID, final DeferredRegister.Items itemRegistrar) {
			super(DeferredRegister.createBlocks(modID));
			this.ITEM_REGISTRAR = Objects.requireNonNull(itemRegistrar, EXCEPTION_NULL_REGISTRAR);
		}
		
		protected BlockProvider(final String modID, final ItemProvider itemRegistrar) {
			this(modID, itemRegistrar.REGISTRAR);
		}
		
		protected BlockProvider(final String modID) {
			this(modID, DeferredRegister.createItems(modID));
		}
		
		/* Internal Methods */
		
		protected <V extends Block> DeferredBlock<V> add(String name, Supplier<V> initializer) {
			return this.REGISTRAR.register(name, initializer);
		}
		
		protected <V extends Block, I extends BlockItem> DeferredBlock<V> add(String name, Supplier<V> initializer, Supplier<I> itemInitializer) {
			final DeferredBlock<V> holder = this.add(name, initializer);
			this.ITEM_REGISTRAR.register(name, itemInitializer);
			return holder;
		}
		
		protected <V extends Block> DeferredBlock<V> addWithItem(String name, Supplier<V> initializer, Item.Properties properties) {
			final DeferredBlock<V> holder = this.add(name, initializer);
			this.ITEM_REGISTRAR.registerSimpleBlockItem(holder, properties);
			return holder;
		}
		
		protected <V extends Block> DeferredBlock<V> addWithItem(String name, Supplier<V> initializer) {
			final DeferredBlock<V> holder = this.add(name, initializer);
			this.ITEM_REGISTRAR.registerSimpleBlockItem(holder);
			return holder;
		}
		
		protected DeferredBlock<Block> addWithItem(String name, BlockBehaviour.Properties properties) {
			final DeferredBlock<Block> holder = this.REGISTRAR.registerSimpleBlock(name, properties);
			this.ITEM_REGISTRAR.registerSimpleBlockItem(holder);
			return holder;
		}
		
	}
	
	public static class MenuProvider extends ContentProvider<MenuType<?>, DeferredRegister<MenuType<?>>> {

		public MenuProvider(final String modID) {
			super(DeferredRegister.create(Registries.MENU, modID));
		}
		
		protected <V extends AbstractContainerMenu> DeferredHolder<MenuType<?>, MenuType<V>> add(final String id, MenuSupplier<V> supplier, FeatureFlagSet flags) {
			return this.REGISTRAR.register(id, () -> new MenuType<V>(supplier, flags));
		}
		
		protected <V extends AbstractContainerMenu> DeferredHolder<MenuType<?>, MenuType<V>> add(final String id, MenuSupplier<V> supplier) {
			return this.add(id, supplier, FeatureFlagSet.of());
		}
		
		protected <V extends AbstractContainerMenu> DeferredHolder<MenuType<?>, MenuType<V>> addFactory(final String id, IContainerFactory<V> factory) {
			return this.REGISTRAR.register(id, () -> IMenuTypeExtension.create(factory));
		}
		
	}

}
