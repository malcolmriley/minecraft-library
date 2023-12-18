package paragon.minecraft.library.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;
import paragon.minecraft.library.registration.IEventBusListener;

/**
 * Class that provides a means of automatically refreshing values held by {@link ConfigValue} derivatives.
 *
 * @author Malcolm Riley
 */
public abstract class AbstractConfiguration implements IEventBusListener {

	/* Internal Fields */
	private final Collection<ConfigValueWrapper<?>> LISTENERS;
	private final ModConfig.Type TYPE;
	private final String FILENAME;

	protected AbstractConfiguration(ModConfig.Type type, String filename) {
		this.LISTENERS = new ArrayList<>();
		this.TYPE = Objects.requireNonNull(type);
		this.FILENAME = Objects.requireNonNull(filename);
	}

	/* Event Subscriber Methods */

	@SubscribeEvent
	public void onLoad(final ModConfigEvent.Loading event) {
		this.refreshAll();
	}

	@SubscribeEvent
	public void onReload(final ModConfigEvent.Reloading event) {
		this.refreshAll();
	}

	/* IEventBusListener Compliance Methods */

	public void registerTo(IEventBus bus) {
		ModLoadingContext.get().registerConfig(this.TYPE, this.buildSpec(new ModConfigSpec.Builder()), this.FILENAME);
		bus.register(this);
	}

	/* Internal Methods */

	/**
	 * Use this method to register {@link Consumer} instances that will be used to set internal fields when the
	 * paired {@link ConfigValue} is loaded or refreshed.
	 * The recommended use is to call this method from within {@link #initialize(net.minecraftforge.common.ForgeConfigSpec.Builder)},
	 * using the passed {@link ForgeConfigSpec.Builder} instance to create the individual {@link ConfigValue}.
	 *
	 * @param <T> The type of value to set
	 * @param refreshListener - The receiver for the value provided by the {@link ConfigValue}.
	 * @param value - The {@link ConfigValue} providing the value
	 */
	protected final <T> void defineValue(final Consumer<T> refreshListener, final ConfigValue<T> value) {
		this.LISTENERS.add(new ConfigValueWrapper<>(value, refreshListener));
	}

	/**
	 * Method to manually refresh all held {@link ConfigValueWrapper} instances,
	 */
	protected final void refreshAll() {
		this.LISTENERS.forEach(ConfigValueWrapper::refresh);
	}

	/* Abstract Methods */

	/**
	 * Use this method to build the {@link ForgeConfigSpec} associated with this config object.
	 *
	 * @param builder - The builder to use
	 * @return A built {@link ForgeConfigSpec}
	 */
	protected abstract ModConfigSpec buildSpec(ModConfigSpec.Builder builder);

	/* Config Wrapper Implementation */

	/**
	 * Wrapper for {@link ConfigValue} objects, allowing for automatic updating of fields.
	 *
	 * @author Malcolm Riley
	 * @param <T> The ultimate type held by this {@link ConfigValueWrapper}.
	 */
	protected static final class ConfigValueWrapper<T> {

		protected final ConfigValue<T> CONFIG;
		protected final Consumer<T> LISTENER;

		protected ConfigValueWrapper(ConfigValue<T> config, Consumer<T> listener) {
			this.CONFIG = Objects.requireNonNull(config);
			this.LISTENER = Objects.requireNonNull(listener);
		}

		/**
		 * Applies the value of the {@link ConfigValue} contained by this {@link ConfigValueWrapper}
		 * to the contained listener {@link Consumer}.
		 */
		public void refresh() {
			this.LISTENER.accept(this.CONFIG.get());
		}

	}

}
