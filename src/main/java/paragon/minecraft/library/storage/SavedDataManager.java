package paragon.minecraft.library.storage;

import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;

import org.apache.logging.log4j.LogManager;

import com.mojang.serialization.Codec;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

/**
 * Helper class for interacting with {@link SavedData} subtypes via a level provider mechanism.
 * <p>
 * Contains some boilerplate-reducing methods for fetching the data from {@link DimensionDataStorage} via a {@link Codec}.
 * <p>
 * Because {@link SavedData} is only really held by the logical server, this class should be used for server-sided data or be used in conjunction with a network synchronization mechanism.
 *
 * @author Malcolm Riley
 * @param <T> The {@link SavedData} type.
 */
public class SavedDataManager<T extends SavedData> {

	/* Internal Fields */
	protected final Codec<T> CODEC;
	protected final SavedData.Factory<T> FACTORY;
	protected final ILevelDataStorage PROVIDER;
	protected final String NAME;

	public SavedDataManager(Codec<T> codec, SavedData.Factory<T> factory, String name) {
		this(codec, factory, name, LevelDataStorage.GLOBAL);
	}

	public SavedDataManager(Codec<T> codec, SavedData.Factory<T> factory, String name, ILevelDataStorage provider) {
		this.CODEC = Objects.requireNonNull(codec, "Cannot instantiate SavedDataManager with null Codec!");
		this.FACTORY = Objects.requireNonNull(factory, "Cannot instantiate SavedDataManager with null data factory!");
		this.PROVIDER = Objects.requireNonNull(provider, "Cannot instantiate SavedDataManager with null storage provider!");
		this.NAME = name;
	}

	/* Public Methods */

	/**
	 * Helper method for saving to a {@link CompoundTag} using the provided {@link Codec}, instance, and {@link CompoundTag}.
	 *
	 * @param <D> The type to save
	 * @param codec - The {@link Codec} for the subject type
	 * @param instance - The instance to save
	 * @param fallback - The {@link CompoundTag} to be returned if the operation fails
	 * @return A {@link CompoundTag} with the provided instance written to it.
	 */
	public static <D> CompoundTag saveUsingCodec(Codec<D> codec, D instance, CompoundTag fallback) {
		return codec.encodeStart(NbtOps.INSTANCE, instance)
			.get()
			.left()
			.map(element -> (element instanceof CompoundTag compound) ? compound : null)
			.orElse(fallback);
	}

	/**
	 * Tries to get the desired {@link SavedData} derivative from the {@link DimensionDataStorage} location.
	 *
	 * @return An {@link Optional} potentially containing the {@link SavedData}.
	 */
	public Optional<T> tryGet() {
		return this.PROVIDER.tryGet().map(this::from);
	}

	/**
	 * Tries to mark the underlying {@link SavedData} dirty, hence signaling that there are changes to be persisted to the save file at the next opportunity.
	 */
	public void markDirty() {
		this.tryGet().ifPresent(SavedData::setDirty);
	}

	/* Internal Methods */

	protected T load(CompoundTag tag) {
		return this.CODEC.decode(NbtOps.INSTANCE, tag).getOrThrow(false, error -> {
			LogManager.getLogger().error("Error while attempting to decode persistent data \"{}\": {}", this.NAME, error);
		}).getFirst();
	}

	protected T from(DimensionDataStorage storage) {
		return storage.computeIfAbsent(this.FACTORY, this.NAME);
	}

	/* Storage Provider Implementations */

	/**
	 * Interface representing a method for obtaining {@link DimensionDataStorage} instances.
	 *
	 * @author Malcolm Riley
	 */
	@FunctionalInterface
	public static interface ILevelDataStorage {

		/**
		 * This method should return a {@link DimensionDataStorage} if it is available, or {@link Optional#empty()} if it is not.
		 *
		 * @return An {@link Optional}, possibly containing a {@link DimensionDataStorage} of some kind.
		 */
		public @Nonnull Optional<DimensionDataStorage> tryGet();

	}

	/**
	 * Holder and factory class for providing {@link ILevelDataStorage} instances.
	 *
	 * @author Malcolm Riley
	 */
	public static class LevelDataStorage {

		/* Shared Fields */

		/** Storage method for global data available across levels, including overworld. */
		public static final ILevelDataStorage GLOBAL = LevelDataStorage.forLevel(Level.OVERWORLD);
		/** Storage method for data available only in the nether. */
		public static final ILevelDataStorage NETHER = LevelDataStorage.forLevel(Level.NETHER);
		/** Storage method for data available only in the end. */
		public static final ILevelDataStorage END = LevelDataStorage.forLevel(Level.END);

		private LevelDataStorage() {}

		/**
		 * Generates an access method for {@link DimensionDataStorage} available to the {@link Level} indicated by the provided {@link ResourceKey}.
		 * <p>
		 * Note that {@link DimensionDataStorage} becomes unavailable when the holding {@link Level} is unloaded, except for {@link Level#OVERWORLD}, which is never fully unloaded.
		 * For this reason, global data needed across all dimensions should be accessed via {@link Level#OVERWORLD}.
		 * <p>
		 * Several {@link ILevelDataStorage} instances corresponding to each base-game dimension are available statically as {@link LevelDataStorage#GLOBAL}, {@link LevelDataStorage#NETHER},
		 * and {@link LevelDataStorage#END}.
		 *
		 * @param key - The {@link ResourceKey} corresponding to the desired {@link Level}
		 * @return A {@link ILevelDataStorage} for that {@link Level}.
		 * @see LevelDataStorage#GLOBAL
		 * @see LevelDataStorage#NETHER
		 * @see LevelDataStorage#END
		 */
		public static ILevelDataStorage forLevel(final ResourceKey<Level> key) {
			return () -> LevelDataStorage.getServer().map(server -> server.getLevel(key).getDataStorage());
		}

		/* Internal Methods */

		protected static Optional<MinecraftServer> getServer() {
			return Optional.ofNullable(ServerLifecycleHooks.getCurrentServer());
		}

	}

}
