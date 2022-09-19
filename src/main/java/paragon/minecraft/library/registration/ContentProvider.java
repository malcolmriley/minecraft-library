package paragon.minecraft.library.registration;

import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryObject;
import paragon.minecraft.library.Utilities;

/**
 * Convenience class for registering content to Forge.
 *
 * @author Malcolm Riley
 * @param <T> The {@link IForgeRegistryEntry} subtype
 */
public abstract class ContentProvider<T extends IForgeRegistryEntry<T>> implements IEventBusListener.FML {

	/* Internal Fields */
	protected final DeferredRegister<T> ALL;

	public ContentProvider(String modID) {
		this.ALL = this.initializeRegistry(modID);
	}
	
	/**
	 * Returns an {@link Iterable} over all non-null content instances held by this {@link ContentProvider}.
	 * <p>
	 * {@link ContentProvider} may hold {@link RegistryObject} that return {@code FALSE} for {@link RegistryObject#isPresent()}; such
	 * instances are already filtered out by this method.
	 * 
	 * @return An {@link Iterable} over all non-null content instances.
	 */
	public Iterable<T> iterateContent() {
		return this.streamContent()::iterator;
	}

	/**
	 * Returns an {@link Stream} over all non-null content instances held by this {@link ContentProvider}.
	 * <p>
	 * {@link ContentProvider} may hold {@link RegistryObject} that return {@code FALSE} for {@link RegistryObject#isPresent()}; such
	 * instances are already filtered out by this method.
	 * 
	 * @return An {@link Stream} over all non-null content instances.
	 */
	public Stream<T> streamContent() {
		return this.streamRegistered(this.ALL.getEntries().stream());
	}
	
	/**
	 * Performs the provided action for each present element in this {@link ContentProvider}.
	 * <p>
	 * Simply a convenience method for calling {@link #streamContent()} and then {@link Stream#forEach(Consumer)}.
	 * 
	 * @param action - The action to perform
	 */
	public void forEach(Consumer<T> action) {
		this.streamContent().forEach(action);
	}
	
	/* Abstract Methods */
	
	/**
	 * This method should initialize and return a {@link DeferredRegister} based on the provided {@link String} as mod ID.
	 * 
	 * @param modID - The {@link String} mod ID to use
	 * @return An initialized {@link DeferredRegister}.
	 */
	protected abstract DeferredRegister<T> initializeRegistry(String modID);

	/* Internal Methods */

	/**
	 * Convenience method to register a bit of content to the internal {@link DeferredRegister} of this {@link ContentProvider}.
	 * 
	 * @param name - The name to use
	 * @param supplier - The {@link Suppllier} for the content
	 * @return A {@link RegistryObject} handle containing the content
	 */
	protected RegistryObject<T> add(String name, Supplier<T> supplier) {
		return this.ALL.register(name, supplier);
	}

	/**
	 * Utility method for filtering out non-present {@link RegistryObject}, and then unwrapping the remainder.
	 * <p>
	 * If {@link RegistryObject#isPresent()} returns {@code TRUE}, then unwraps it with {@link RegistryObject#get()}.
	 * 
	 * @param input - The {@link Stream} of {@link RegistryObject} to use
	 * @return A {@link Stream} over the present elements in the provided {@link RegistryObject} {@link Stream}.
	 */
	protected Stream<T> streamRegistered(Stream<RegistryObject<T>> input) {
		return Utilities.Misc.streamPresent(this.ALL.getEntries());
	}
	
	/* IEventBusListener Compliance Methods */

	@Override
	public void registerTo(IEventBus bus) {
		this.ALL.register(bus);
	}

}
