package paragon.minecraft.library.registration;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import paragon.minecraft.library.Utilities;

/**
 * Simple holder and decoder class for data-driven objects. This class when registered to the Forge event bus, receives {@link AddReloadListenerEvent} for files in the provided datapack directory.
 * <p>
 * For most use cases, a traditional Forge registry should be preferred.
 * 
 * @author MalcolmRiley
 * @param <T> The type contained within this {@link ContentProvider}.
 */
public class CustomDataProvider<T> extends SimpleJsonResourceReloadListener implements IEventBusListener {
	
	/* Shared Static Fields */
	protected static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
	
	/* Internal Fields */
	protected final Map<ResourceLocation, T> ENTRIES;
	protected final Codec<T> CODEC;
	
	public CustomDataProvider(Codec<T> codec, ResourceLocation registryName) {
		this(codec, Utilities.Strings.path(registryName.getNamespace(), registryName.getPath()));
	}

	public CustomDataProvider(Codec<T> codec, String directory) {
		super(CustomDataProvider.GSON, directory);
		this.ENTRIES = new HashMap<>();
		this.CODEC = Objects.requireNonNull(codec, "Codec for CustomDataProvider cannot be null!");
	}
	
	/* Public Methods */
	
	/**
	 * Returns the content corresponding to the provided {@link ResourceLocation} id.
	 * 
	 * @param id - The {@link ResourceLocation} id
	 * @return The element registered to the provided id, else {@code null}.
	 */
	public @Nullable T get(final ResourceLocation id) {
		return this.ENTRIES.get(id);
	}
	
	/**
	 * Returns an {@link Optional} containing the instance corresponding to the provided {@link ResourceLocation}, or an empty {@link Optional} if no such instance exists.
	 * 
	 * @param id - The {@link ResourceLocation} id
	 * @return An {@link Optional} wrapping the instance corresponding to the provided id, else an empty {@link Optional}.
	 */
	public Optional<T> tryGet(final ResourceLocation id) {
		return Optional.ofNullable(this.get(id));
	}
	
	/**
	 * Returns a {@link Stream} over all instances contained within this {@link CustomDataProvider}.
	 * 
	 * @return All held instances.
	 */
	public Stream<T> streamAll() {
		return this.ENTRIES.values().stream();
	}
	
	/**
	 * Returns a {@link Stream} over all known {@link ResourceLocation} IDs held by this {@link CustomDataProvider}.
	 * 
	 * @return All held {@link ResourceLocation}.
	 */
	public Stream<ResourceLocation> streamIDs() {
		return this.ENTRIES.keySet().stream();
	}
	
	/* Event Handler Methods */
	
	@SubscribeEvent
	public void onAddReloadListener(AddReloadListenerEvent event) {
		event.addListener(this);
	}
	
	/* Supertype Override Methods  */

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> elements, ResourceManager resources, ProfilerFiller profiler) {
		this.clear();
		elements.forEach((location, element) -> {
			if (this.ENTRIES.containsKey(location)) {
				LogManager.getLogger().error("Error while reading datapack: An entry is already registered to ID {}.", location);
			}
			else {
				this.CODEC.decode(JsonOps.INSTANCE, element)
					.resultOrPartial(error -> {
						LogManager.getLogger().error("Error while decoding {}:", location, error);
					})
					.ifPresent(pair -> {
						final T decoded = pair.getFirst();
						this.add(location, decoded);
					});
			}
		});
	}
	
	/* IEventBusListener Compliance Methods */

	@Override
	public void registerTo(IEventBus bus) {
		bus.register(this);
	}
	
	/* Internal Methods */
	
	/**
	 * Clears the content held by this {@link CustomDataProvider}.
	 */
	protected void clear() {
		this.ENTRIES.clear();
	}
	
	/**
	 * Adds the provided instance to this {@link CustomDataProvider} using the provided {@link ResourceLocation}.
	 * <p>
	 * Throws a {@link NullPointerException} if either of the provided {@link ResourceLocation} ID or instance are null.
	 * 
	 * @param id - The ID to use
	 * @param instance - The instance to add
	 * @return The added instance.
	 */
	protected T add(@Nonnull ResourceLocation id, @Nonnull T instance) {
		return this.ENTRIES.put(Objects.requireNonNull(id, "IDs for content added to custom data providers may not be null!"), Objects.requireNonNull(instance, "Instances added to custom data providers may not be null!"));
	}

}
