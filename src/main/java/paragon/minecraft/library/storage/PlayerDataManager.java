package paragon.minecraft.library.storage;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.SerializableUUID;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.loading.FMLEnvironment;
import paragon.minecraft.library.network.AbstractNetworkHandler;
import paragon.minecraft.library.utilities.Utilities;

/**
 * Base class for managing instances of per-player data. Has helper methods for generating appropriate {@link Codec} as well as
 * auto-initializing data.
 * <p>
 * Internally, backed by a map keyed to the player {@link GameProfile} {@link UUID}.
 * <p>
 * <b> Important! </b> This class does not, by itself, enable data persistence or network synchronization. For that, this class should be paired with
 * {@link PersistenceHelper} and {@link SyncHelper}.
 * 
 * @author Malcolm Riley
 * @param <T> The type of per-player data.
 */
public class PlayerDataManager<T> {

	/* Internal Fields */
	protected final Map<UUID, T> PER_PLAYER_DATA;
	protected final Function<UUID, T> INITIALIZER;
	
	/* Shared Constants */
	protected static final String FIELD_PER_PLAYER = "per_player_data";
	
	public PlayerDataManager(@Nonnull final Function<UUID, T> initializer) {
		this(initializer, null);
	}
	
	public PlayerDataManager(@Nonnull final Function<UUID, T> initializer, @Nullable final Map<UUID, T> data) {
		this.PER_PLAYER_DATA = Objects.requireNonNullElseGet(data, HashMap::new);
		this.INITIALIZER = Objects.requireNonNull(initializer, "Cannot initialize PerPlayerDataManager with null instance initializer!");
	}
	
	/* Public Methods */
	
	public <D extends IDataHolder.Syncable> SyncHelper<D> createSyncHelper(@Nonnull final PlayerDataManager<D> manager, @Nonnull final AbstractNetworkHandler handler) {
		return new SyncHelper<D>(manager, handler);
	}
	
	public PersistenceHelper<T> createPersistenceHelper(@Nonnull final Codec<T> codec, @Nonnull final String name) {
		return new PersistenceHelper<>(this, codec, name);
	}

	/**
	 * If this is the client environment, tries to fetch the data object associated with the {@link GameProfile} of the client player. If this is not the client environment
	 * or if there is no data associated with the client player, returns an empty {@link Optional}.
	 * 
	 * @return An {@link Optional} potentially containing data associated with the client player.
	 */
	public Optional<T> tryGetClientData() {
		if (FMLEnvironment.dist.isClient()) {
			@SuppressWarnings("resource")
			final LocalPlayer player = Minecraft.getInstance().player;
			if (Objects.nonNull(player)) {
				return this.tryGetFor(Utilities.Players.getUUID(player));
			}
		}
		return Optional.empty();
	}
	
	/**
	 * Helper method to generate a codec for a {@link PlayerDataManager} subtype.
	 * 
	 * @param <P> The per-player data type
	 * @param <M> The subtype type
	 * @param playerDataCodec - A {@link Codec} for the per-player data type
	 * @param initializer - An initializer for the per-player data type
	 * @return A suitable maplike {@link Codec} for the desired {@link PlayerDataManager} subtype.
	 */
	public static <P, M extends PlayerDataManager<P>> Codec<M> generateCodec(final Codec<P> playerDataCodec, final Function<? super Map<UUID, P>, M> initializer) {
		return Codec.unboundedMap(SerializableUUID.CODEC, playerDataCodec).xmap(initializer, manager -> manager.PER_PLAYER_DATA);
	}
	
	/**
	 * Returns whether data is present for the provided {@link Player}.
	 * <p>
	 * Simply delegates to {@link #hasDataFor(UUID)} after fetching the {@link GameProfile} {@link UUID}.
	 * 
	 * @param player - The {@link Player} to check for
	 * @return Whether data is present for the provided {@link Player}.
	 */
	public boolean hasDataFor(@Nonnull final Player player) {
		return this.hasDataFor(Utilities.Players.getUUID(player));
	}
	
	/**
	 * Returns whether data is present for the {@link Player} corresponding to the provided {@link UUID}.
	 * 
	 * @param player - The {@link Player} to check for
	 * @return Whether data is present for the {@link Player} corresponding to the provided {@link UUID}.
	 */
	public boolean hasDataFor(@Nonnull UUID id) {
		return this.PER_PLAYER_DATA.containsKey(id);
	}
	
	/**
	 * Tries to get the data associated with the provided {@link Player}, if any.
	 * <p>
	 * Returns an empty {@link Optional} if no data is present.
	 * <p>
	 * Simply delegates to {@link #tryGetFor(UUID)} after fetching the {@link GameProfile} {@link UUID}.
	 * 
	 * @param player - The {@link Player} to check for
	 * @return An {@link Optional} potentially containing the requested data.
	 */
	public Optional<T> tryGetFor(@Nonnull final Player player) {
		return this.tryGetFor(Utilities.Players.getUUID(player));
	}
	
	/**
	 * Tries to get the data associated with the {@link Player} corresponding to the provided {@link UUID}.
	 * <p>
	 * Returns an empty {@link Optional} if no data is present.
	 * 
	 * @param id - The {@link UUID} to check for
	 * @return An {@link Optional} potentially containing the requested data.
	 */
	public Optional<T> tryGetFor(@Nonnull UUID id) {
		if (this.hasDataFor(id)) {
			return Optional.ofNullable(this.get(id));
		}
		return Optional.empty();
	}

	/**
	 * Gets the data corresponding to the provided {@link Player}, or initializes a new instance if no such data is already present. The new instance will be associated with
	 * the provided {@link Player}.
	 * <p>
	 * Simply delegates to {@link #getOrInitializeFor(UUID)} after fetching the {@link GameProfile} {@link UUID}.
	 * 
	 * @param player - The {@link Player} to check for
	 * @return The data associated with that {@link Player} or a new instance.
	 */
	public T getOrInitializeFor(@Nonnull final Player player) {
		return this.getOrInitializeFor(Utilities.Players.getUUID(player));
	}
	
	/**
	 * Gets the data corresponding to the {@link Player} corresponding to the provided {@link UUID} or initializes a new instance if no such data is already present.
	 * The new instance will be associated with the provided {@link UUID}, and thus the associated {@link Player}.
	 * 
	 * @param id - The {@link UUID} to check for
	 * @return The data associated with that {@link UUID}.
	 */
	public T getOrInitializeFor(@Nonnull UUID id) {
		if (!this.hasDataFor(id)) {
			final T instance = this.INITIALIZER.apply(id);
			this.put(id, instance);
			return instance;
		}
		return this.get(id);
	}
	
	/**
	 * Returns the data associated with the provided {@link Player}'s {@link GameProfile} {@link UUID}, or {@code null} if no such data exists or is initialized.
	 * <p>
	 * Internally delegates to {@link #get(UUID)}.
	 * <p>
	 * For {@code null} safe variations on this call, see {@link #getOrInitializeFor(Player)} and {@link #tryGetFor(Player)}.
	 * 
	 * @param player - The {@link Player} to check for
	 * @return The data, if any, else {@code null}.
	 * @see #getOrInitializeFor(Player)
	 * @see #tryGetFor(Player)
	 */
	public T get(@Nonnull final Player player) {
		return this.get(Utilities.Players.getUUID(player));
	}
	
	/**
	 * Returns the data associated with the provided {@link UUID}, or {@code null} if no such data exists or is initialized.
	 * <p>
	 * For {@code null} safe variations on this call, see {@link #getOrInitializeFor(UUID)} and {@link #tryGetFor(UUID)}.
	 * 
	 * @param id - The {@link UUID} to check for
	 * @return The data, if any, else {@code null}.
	 * @see #getOrInitializeFor(UUID)
	 * @see #tryGetFor(UUID)
	 */
	public T get(@Nonnull final UUID id) {
		return this.PER_PLAYER_DATA.get(id);
	}
	
	/**
	 * Returns an {@link Iterable} over all currently-held per-player data.
	 * 
	 * @return An {@link Iterable} over all per-player data.
	 */
	public Iterable<T> iterate() {
		return this.PER_PLAYER_DATA.values();
	}
	
	/**
	 * Returns a {@link Stream} over all currently-held per-player data.
	 * 
	 * @return A {@link Stream} over all per-player data.
	 */
	public Stream<T> stream() {
		return this.PER_PLAYER_DATA.values().stream();
	}
	
	/**
	 * Performs the provided {@link Consumer} action for each currently held per-player data instance.
	 * 
	 * @param action - The action to perform
	 */
	public void forEach(Consumer<T> action) {
		this.iterate().forEach(action);
	}
	
	/**
	 * Performs the provided {@link BiConsumer} action for each currently held pair of {@link UUID} and corresponding player data instance.
	 * 
	 * @param action - The action to perform.
	 */
	public void forEach(BiConsumer<UUID, T> action) {
		this.PER_PLAYER_DATA.forEach(action);
	}
	
	/* Internal Methods */
	
	protected void put(UUID id, T value) {
		this.PER_PLAYER_DATA.putIfAbsent(id, value);
	}
	
	protected void clearFor(@Nonnull final Player player) {
		this.clearFor(Utilities.Players.getUUID(player));
	}
	
	protected void clearFor(@Nonnull final UUID id) {
		this.PER_PLAYER_DATA.remove(id);
	}
	
	protected void clearAll() {
		this.PER_PLAYER_DATA.clear();
	}
	
	
}
