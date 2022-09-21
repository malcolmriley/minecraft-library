package paragon.minecraft.library.registration;

import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryBuilder;

/**
 * Specialized content provider for handling data-driven content.
 * <p>
 * Intended for content objects that require a custom {@link Registry}, such as datapack registries.
 * <p>
 * Access to the underlying registry is performed dynamically, through a reference to a {@link Level}. This ensures that even if the
 * underlying registry is data-driven, access to the registry's content is maintained regardless of the sidedness of the environment.
 * 
 * @author Malcolm Riley
 *
 * @param <T> The type contained within this {@link RegistryProvider}.
 */
public abstract class RegistryProvider<T extends IForgeRegistryEntry<T>> implements IEventBusListener {
	
	/* Internal Fields */
	protected final DeferredRegister<T> ALL;
	protected final ResourceKey<? extends Registry<T>> KEY;

	public RegistryProvider(String modID, final ResourceKey<? extends Registry<T>> key) {
		this.KEY = Objects.requireNonNull(key, "RegistryKey may not be null!");
		this.ALL = this.initializeRegistry(modID);
	}
	
	/* Public Methods */
	
	/**
	 * Creates a {@link TagKey} using the underlying {@link DeferredRegister}.
	 * <p>
	 * Will use the currently-active modid as a domain.
	 * 
	 * @param path - The {@link ResourceLocation} path to use for the {@link TagKey}
	 * @return A suitable {@link TagKey}
	 */
	public TagKey<T> createTagKey(String path) {
		return this.ALL.createTagKey(path);
	}
	
	/**
	 * Creates a {@link TagKey} using the underlying {@link DeferredRegister}.
	 * 
	 * @param path - The {@link ResourceLocaiton} to use for the {@link TagKey}
	 * @return A suitable {@link TagKey}
	 */
	public TagKey<T> createTagKey(ResourceLocation path) {
		return this.ALL.createTagKey(path);
	}
	
	/**
	 * Tries to get the underlying registry from the provided {@link Level} via {@link Level#registryAccess()}.
	 * <p>
	 * If that operation fails for some reason, will return {@link Optional#empty()}.
	 * 
	 * @param level - A reference to the {@link Level}.
	 * @return An {@link Optional} containing a reference to the underlying active {@link Registry}, or an empty one if no such {@link Registry} exists.
	 * @see #getRegistryOrThrow(Level)
	 */
	public Optional<? extends Registry<T>> tryGetRegistry(@Nonnull final Level level) {
		return level.registryAccess().registry(this.KEY);
	}
	
	/**
	 * Returns the underlying registry from the provided {@link Level} via {@link Level#registryAccess()}, throwing an exception
	 * if the operation fails.
	 * <p>
	 * This method should be preferred wherever the inaccessibility of the registry should be considered a fatal error.
	 * 
	 * @param level - A reference to the {@link Level}.
	 * @return The underlying {@link Registry}, or an exception is thrown.
	 * @see #tryGetRegistry(Level)
	 */
	public Registry<T> getRegistryOrThrow(@Nonnull final Level level) {
		return level.registryAccess().registryOrThrow(this.KEY);
	}
	
	/**
	 * Tries to get the content associated with the provided {@link ResourceLocation} from the provided {@link Level}.
	 * <p>
	 * Two levels of safety are provided:
	 * <li> If the underlying {@link Registry} is inaccessible, this method returns {@link Optional#empty()}. </li>
	 * <li> If the desired object cannot be retrieved from the underlying {@link Registry}, returns {@link Optional#empty()}. </li>
	 * <p>
	 * In all other situations, returns an {@link Optional} containing a reference to the desired content.
	 * 
	 * @param id - The {@link ResourceLocation} corresponding to the desired content
	 * @param level - A reference to the {@link Level} to use as a source of registry access.
	 * @return An {@link Optional} containing the content corresponding to the provided id, or an empty {@link Optional} 
	 */
	public Optional<T> tryGet(@Nonnull final ResourceLocation id, @Nonnull final Level level) {
		final Optional<? extends Registry<T>> registry = this.tryGetRegistry(level);
		return registry.isPresent() ? registry.get().getOptional(id) : Optional.empty();
	}
	
	/**
	 * Returns an {@link Optional} potentially containing the {@link ResourceLocation} ID of the provided instance, first by attempting {@link IForgeRegistryEntry#getRegistryName()} and then by querying the level registry by reverse lookup.
	 * <p>
	 * This is necessary because datapack driven registries do not set the registry name of the instances they contain.
	 * 
	 * @param level - The {@link Level} whose registry is used for lookup as a fallback
	 * @param instance - The instance to query
	 * @return An {@link Optional} containing the {@link ResourceLocation} ID of that instance.
	 */
	public Optional<ResourceLocation> tryGetID(@Nullable final T instance, @Nonnull final Level level) {
		return Optional.ofNullable(this.getID(instance, level));
	}
	
	/**
	 * Returns the {@link ResourceLocation} ID of the provided instance, first by attempting {@link IForgeRegistryEntry#getRegistryName()} and then by querying the level registry by reverse lookup.
	 * <p>
	 * This is necessary because datapack driven registries do not set the registry name of the instances they contain.
	 * 
	 * @param level - The {@link Level} whose registry is used for lookup as a fallback
	 * @param instance - The instance to query
	 * @return The {@link ResourceLocation} ID of that instance.
	 */
	public ResourceLocation getID(@Nullable final T instance, @Nonnull final Level level) {
		return Objects.nonNull(instance.getRegistryName()) ? instance.getRegistryName() : this.getRegistryOrThrow(level).getKey(instance);
	}
	
	/**
	 * Returns an {@link Optional} potentially containing the {@link ResourceLocation} ID of the provided instance, first by attempting {@link IForgeRegistryEntry#getRegistryName()} and then by querying the level registry by reverse lookup.
	 * <p>
	 * This is necessary because datapack driven registries do not set the registry name of the instances they contain.
	 * 
	 * @param level - The {@link Player} whose level registry is used for lookup as a fallback
	 * @param instance - The instance to query
	 * @return An {@link Optional} containing the {@link ResourceLocation} ID of that instance.
	 */
	public Optional<ResourceLocation> tryGetID(@Nullable final T instance, @Nonnull final Player player) {
		return this.tryGetID(instance, player.getLevel());
	}
	
	/**
	 * Returns the {@link ResourceLocation} ID of the provided instance, first by attempting {@link IForgeRegistryEntry#getRegistryName()} and then by querying the level registry by reverse lookup.
	 * <p>
	 * This is necessary because datapack driven registries do not set the registry name of the instances they contain.
	 * 
	 * @param level - The {@link Player} whose level registry is used for lookup as a fallback
	 * @param instance - The instance to query
	 * @return The {@link ResourceLocation} ID of that instance.
	 */
	public ResourceLocation getID(@Nullable final T instance, @Nonnull final Player player) {
		return this.getID(instance, player.getLevel());
	}
	
	/* IEventBusListener Compliance Methods */

	public void registerTo(IEventBus bus) {
		this.ALL.register(bus);
	}
	
	/* Abstract Methods */
	
	protected abstract Class<T> getType();
	protected abstract RegistryBuilder<T> makeRegistry();
	
	/* Internal Methods */
	
	protected DeferredRegister<T> initializeRegistry(String modID) {
		final DeferredRegister<T> registrar = DeferredRegister.create(this.KEY, modID);
		registrar.makeRegistry(this.getType(), this::makeRegistry);
		return registrar;
	}

}
