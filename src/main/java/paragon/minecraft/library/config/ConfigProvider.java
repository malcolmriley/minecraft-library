package paragon.minecraft.library.config;

import java.util.Objects;
import java.util.function.Consumer;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import paragon.minecraft.library.registration.IEventBusListener;

/**
 * Container and initializer class for interacting with {@link ModConfigSpec}.
 * 
 * @author Malcolm Riley
 */
public abstract class ConfigProvider implements IEventBusListener {

	/* Constants */
	protected static final String EXCEPTION_NULL_FACTORY = "ModConfigSpec factory cannoy be null!";

	public ConfigProvider() {
		// TODO: Potentially move to threaded mod work queue?
		this.initialize();
	}

	/* IModBusListener Compliance Methods */

	@Override
	public void registerTo(IEventBus bus) {
		bus.addListener(this::onLoad);
		bus.addListener(this::onReload);
	}

	/* Internal Methods */

	/**
	 * Automatically and registers a new configuration object using the provided builder.
	 *
	 * @param type - The configuration {@link ModConfig.Type} corresponding to the created object
	 * @param factory - A {@link Consumer} which creates a config spec using the provided {@link ModConfigSpec.Builder}.
	 */
	protected void addConfig(final ModConfig.Type type, final Consumer<ModConfigSpec.Builder> factory) {
		final ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
		Objects.requireNonNull(factory, EXCEPTION_NULL_FACTORY);
		factory.accept(builder);
		ModLoadingContext.get().registerConfig(type, builder.build());
	}

	/**
	 * Event receiver for {@link ModConfigEvent.Loading} events.
	 *
	 * @param event - The received event.
	 */
	protected void onLoad(final ModConfigEvent.Loading event) {

	}

	/**
	 * Event receiver for {@link ModConfigEvent.Reloading} events.
	 *
	 * @param event - The received event.
	 */
	protected void onReload(final ModConfigEvent.Reloading event) {

	}

	/* Abstract Methods */

	/**
	 * Called on class construction, with the intent to initialize the contained configuration values.
	 * <p>
	 * Use {@link #addConfig(net.neoforged.fml.config.ModConfig.Type, Consumer)} within to build individual configs.
	 */
	protected abstract void initialize();

}
