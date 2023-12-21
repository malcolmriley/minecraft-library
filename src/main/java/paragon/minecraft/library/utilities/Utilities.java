package paragon.minecraft.library.utilities;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalLong;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import com.machinezoo.noexception.optional.OptionalBoolean;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;

import net.minecraft.ResourceLocationException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.ToFloatFunction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.NetworkHooks;

/**
 * Container class for various static utility functions.
 *
 * @author Malcolm Riley
 */
public final class Utilities {

	private Utilities() {}

	/**
	 * Container class for {@link Codec} related static utility functions.
	 */
	public static final class Codecs {

		private Codecs() {}

		/**
		 * Attempts to decode the provided {@link CompoundTag} using {@link NbtOps#INSTANCE} without accepting partial results and using the provided {@link Consumer} to accept any errors.
		 * <p>
		 * If the aggregate decode operation succeeds, the result will be placed in the returned {@link Optional}. If the aggregate operation fails or if the provided data was {@code null},
		 * an empty {@link Optional} will be returned instead.
		 *
		 * @param <T> The result type
		 * @param codec - The {@link Codec} to use
		 * @param tag - The {@link CompoundTag} to decode
		 * @param onError - An action to perform per {@link String} error thrown
		 * @return An {@link Optional} containing the successfully decoded instance, or an empty {@link Optional}.
		 */
		public static <T> Optional<T> decodeNBT(@Nonnull final Codec<T> codec, @Nullable final CompoundTag tag, @Nonnull final Consumer<String> onError) {
			return Codecs.decodeUsing(codec, tag, NbtOps.INSTANCE, onError);
		}

		/**
		 * Attepts to decode the provided data type using the provided {@link DynamicOps} without partial results and using the provided {@link Consumer} to accept any errors.
		 * <p>
		 * If the aggregate decode operation succeeds, the result will be placed in the returned {@link Optional}. If the aggregate operation fails or if the provided data was {@code null},
		 * an empty {@link Optional} will be returned instead.
		 *
		 * @param <T> The result type
		 * @param <D> The data type
		 * @param codec - The {@link Codec} to use
		 * @param data - The data to decode
		 * @param ops - The {@link DynamicOps} instance to use
		 * @param onError - An action to perform per {@link String} error thrown
		 * @return An {@link Optional} containing the successfully decoded instance, or an empty {@link Optional}.
		 */
		public static <T, D> Optional<T> decodeUsing(@Nonnull final Codec<T> codec, @Nullable final D data, @Nonnull final DynamicOps<D> ops, @Nonnull final Consumer<String> onError) {
			if (Objects.isNull(data)) {
				return Optional.empty();
			}
			return codec.decode(ops, data)
				.getOrThrow(false, onError)
				.mapFirst(Optional::ofNullable)
				.getFirst();
		}

		/**
		 * Creates a {@link Codec} for a {@link Set} of the provided inner {@link Codec} type.
		 *
		 * @param <T> The inner type
		 * @param innerCodec - The {@link Codec} to use for the inner type
		 * @return A {@link Codec} for a {@link Set} of the inner type.
		 */
		public static <T> Codec<Set<T>> setOf(@Nonnull final Codec<T> innerCodec) {
			return Codec.list(innerCodec).xmap(Utilities.Misc::intoSet, List::copyOf);
		}

		/* Internal Methods */

		protected static final <T, K> Codec<Either<TagKey<T>, K>> tagOrOther(@Nonnull final ResourceKey<? extends Registry<T>> key, Codec<K> other, final String prefix, final String otherSuffix) {
			return Codec.mapEither(TagKey.codec(key).fieldOf(optionalPrefix(prefix, "tag")), other.fieldOf(optionalPrefix(prefix, otherSuffix))).codec();
		}

		protected static final String optionalPrefix(@Nullable String prefix, @Nonnull String suffix) {
			if (Objects.isNull(prefix)) {
				return suffix;
			}
			return Strings.name(prefix, suffix);
		}

	}

	/**
	 * Container class for {@link Player} related static utility functions.
	 */
	public static final class Players {

		private Players() {}

		/**
		 * Tries to get the {@link UUID} from the {@link GameProfile} of the provided {@link Player}, returning it as an {@link Optional}
		 * if it exists and the provied {@link Player} reference is not {@code null}.
		 *
		 * @param player - The {@link Player} in question
		 * @return An {@link Optional} containing the stable {@link UUID} of the provided {@link Player}, or an empty one.
		 */
		public static Optional<UUID> tryGetUUID(@Nullable final Player player) {
			return (Objects.nonNull(player)) ? Optional.ofNullable(Players.getUUID(player)) : Optional.empty();
		}

		/**
		 * Returns the {@link UUID} of associated with the {@link GameProfile} of the provided {@link Player}.
		 *
		 * @param player - The {@link Player} in question
		 * @return The stable {@link UUID} of that {@link Player}.
		 */
		public static UUID getUUID(@Nonnull final Player player) {
			return player.getGameProfile().getId();
		}

	}

	/**
	 * Container class for time-related static utility functions.
	 */
	public static final class Time {

		private Time() {}

		/* Public Fields */

		/** The burn time it takes to smelt a single item in an ordinary furnace. */
		public static final int BURN_TICKS_SINGLE_ITEM = 200;
		/** The (ideal) number of in-game ticks per real-world second. */
		public static final int TICKS_PER_SECOND = 20;

		/**
		 * Returns the burn time, in ticks, required to smelt the passed whole quantity of items in an ordinary furnace.
		 *
		 * @param quantityItems - The quantity of single items to smelt
		 * @return The burn time, in ticks, required to smelt those items.
		 */
		public static final int burnTimeFor(int quantityItems) {
			return Time.BURN_TICKS_SINGLE_ITEM * quantityItems;
		}

		/**
		 * Converts the quantity of real-world seconds into an (ideally) equivalent quantity of in-game ticks.
		 *
		 * @param seconds - The quantity of seconds to convert
		 * @return An equivalent number of ingame ticks.
		 */
		public static final int secondsToTicks(double seconds) {
			return Mth.floor(Time.TICKS_PER_SECOND * seconds);
		}

		/**
		 * Converts the quantity of real-world minutes into an (ideally) equivalent quantity of in-game ticks.
		 *
		 * @param minutes - The quantity of minutes to convert
		 * @return An equivalent number of ingame ticks.
		 */
		public static final int minutesToTicks(double minutes) {
			return Mth.floor(Time.TICKS_PER_SECOND * 60 * minutes);
		}

	}

	/**
	 * Container for {@link BlockState}-related static helper methods.
	 */
	public static final class States {

		private States() {}

		/**
		 * Convenience method that returns whether the {@link BlockState} below the provided {@link BlockPos} has a solid upward surface, via {@link BlockState#isSolidSide(LevelReader, BlockPos, Direction)}.
		 *
		 * @param world - The {@link LevelReader} to use for {@link BlockState} access
		 * @param position - The position above the queried block
		 * @return Whether the {@link BlockState} below the provided {@link BlockPos} has a solid upward surface.
		 */
		public static boolean isBlockBelowSolidAbove(LevelReader world, BlockPos position) {
			BlockPos below = position.below();
			return world.getBlockState(below).isFaceSturdy(world, below, Direction.UP);
		}

		/**
		 * Deduces the horizontal-only facing from the provided {@link BlockPlaceContext}.
		 * <p>
		 * If the facing is {@value Direction#UP} or {@value Direction#DOWN}, returns the opposite of {@link BlockPlaceContext#getPlacementHorizontalFacing()}; otherwise returns {@link BlockPlaceContext#getFace()}.
		 *
		 * @param context - The context to deduce the facing from
		 * @return The block placement facing direction.
		 */
		public static Direction deduceHorizontalFacing(BlockPlaceContext context) {
			Direction face = context.getClickedFace();
			return (face == Direction.UP) || (face == Direction.DOWN) ? context.getNearestLookingDirection() : face.getOpposite();
		}

	}

	public static class Misc {

		private Misc() {}

		/**
		 * Returns the sum of the values yielded by applying the provided {@link ToIntFunction} to each element of the provided {@link Collection}.
		 *
		 * @param <T> The type contained in the {@link Collection}
		 * @param collection - The {@link Collection} to examine
		 * @param function - A {@link ToIntFunction} to apply
		 * @return The simple sum of the values provided as above.
		 */
		public static <T> int intSum(Collection<T> collection, ToIntFunction<? super T> function) {
			int sum = 0;
			for (T instance : collection) {
				sum += function.applyAsInt(instance);
			}
			return sum;
		}

		/**
		 * Returns the sum of the values yielded by applying the provided {@link ToFloatFunction} to each element of the provided {@link Collection}.
		 *
		 * @param <T> The type contained in the {@link Collection}
		 * @param collection - The {@link Collection} to examine
		 * @param function - A {@link ToFloatFunction} to apply
		 * @return The simple sum of the values provided as above.
		 */
		public static <T> float floatSum(Collection<T> collection, ToFloatFunction<T> function) {
			float sum = 0;
			for (T instance : collection) {
				sum += function.apply(instance);
			}
			return sum;
		}

		/**
		 * Wraps the provided {@link Collection} subtype into an {@link Optional}. If the provided {@link Collection} is {@code null} or empty,
		 * the returned {@link Optional} will also be empty.
		 *
		 * @param <T> - The type contained in the {@link Collection}
		 * @param <C> - The {@link Collection} type
		 * @param collection - The {@link Collection} instance
		 * @return An {@link Optional} containing the {@link Collection}, or an empty one if the {@link Collection} was {@code null} or empty.
		 */
		public static <T, C extends Collection<T>> Optional<C> wrap(@Nullable final C collection) {
			return Misc.isNullOrEmpty(collection) ? Optional.empty() : Optional.of(collection);
		}

		/**
		 * Converts the provided {@link Collection} into an unmodifiable {@link List}.
		 * <p>
		 * If the provided {@link Collection} is {@code null} or empty, the returned {@link List} will simply be empty.
		 * <p>
		 * <b> Important! </b> While the provided {@link Collection} may itself be {@code null}, it may not contain any {@code null} elements.
		 *
		 * @param <T> The type contained in the {@link Collection}
		 * @param collection - The {@link Collection} to convert
		 * @return An unmodifiable {@link List} of the elements in the provided {@link Collection}.
		 */
		public static <T> List<T> intoUnmodifiableList(@Nullable final Collection<T> collection) {
			return Misc.isNullOrEmpty(collection) ? List.of() : List.copyOf(collection);
		}

		/**
		 * Converts the provided {@link Collection} of values into a new {@link Set}, using the provided {@link Supplier} as a provider of the set.
		 * <p>
		 * If the provided {@link Collection} is {@code null}, an empty {@link Set} will be returned.
		 * <p>
		 * By default, this method creates a new {@link HashSet} to use. To specify the type of {@link Set} or use an existing one, see {@link #intoSet(Collection, Set)}.
		 *
		 * @param <T> The type contained in the {@link Collection}
		 * @param collection - The {@link Collection} to convert
		 * @return A {@link Set}, by definition containing all unique elements of the provided {@link Collection}.
		 * @see #intoSet(Collection, Set)
		 */
		public static <T> Set<T> intoSet(@Nullable final Collection<T> collection) {
			return Misc.intoSet(collection, null);
		}

		/**
		 * Converts the provided {@link Collection} of values into a new {@link Set}, using the provided {@link Supplier} as a provider of the set.
		 * <p>
		 * If the provided {@link Collection} is {@code null}, an empty {@link Set} will be returned. If the provided {@link Set} is {@code null},
		 * a new {@link HashSet} will be created and filled.
		 * <p>
		 * If no existing {@link Set} exists or if no or specific {@link Set} implementation is required, consider {@link #intoSet(Collection)} for simplicity.
		 *
		 * @param <T> The type contained in the {@link Collection}
		 * @param collection - The {@link Collection} to convert
		 * @param existingSet - An existing {@link Set} to use, or {@code null}.
		 * @return A {@link Set}, by definition containing all unique elements of the provided {@link Collection}.
		 * @see #intoSet(Collection)
		 */
		public static <T> Set<T> intoSet(@Nullable final Collection<T> collection, @Nullable final Set<T> existingSet) {
			final Set<T> set = Objects.requireNonNullElseGet(existingSet, HashSet::new);
			if (!Misc.isNullOrEmpty(collection)) {
				collection.forEach(set::add);
			}
			return set;
		}

		/**
		 * Returns {@code true} if the provided {@link Collection} is {@code null} or returns true for {@link Collection#isEmpty()}.
		 *
		 * @param collection - The {@link Collection} to examine
		 * @return Whether the provided reference is {@code null} or is an empty {@link Collection}.
		 */
		public static boolean isNullOrEmpty(@Nullable final Collection<?> collection) {
			return Objects.isNull(collection) || collection.isEmpty();
		}

		/**
		 * Invokes the provided {@link Supplier} and applies the provided {@link Consumer} if the object returned by the provided {@link Supplier} is non-{@code null}.
		 *
		 * @param <T> The instance type
		 * @param initializer - The {@link Supplier} to invoke
		 * @param transformer - The {@link Consumer} to invoke if the instance returned by the {@link Supplier} is non-null
		 * @return The instance returned by the provided {@link Supplier}, possibly transformed by the provided {@link Consumer}.
		 */
		@Nullable
		public static <T> T acceptIfNonNull(final Supplier<T> initializer, Consumer<T> transformer) {
			final T instance = initializer.get();
			if (Objects.nonNull(instance)) {
				transformer.accept(instance);
			}
			return instance;
		}

		/**
		 * Tries to return a random element from the provided {@link List}.
		 * <p>
		 * <li>If the {@link List} is empty or {@code null}, returns {@link Optional#empty()}.</li>
		 * <li>If the {@link List} contains exactly one item, simply returns it.</li>
		 * <li>Otherwise, selects a random element from the {@link List} using the provided {@link Random} instance.
		 *
		 * @param <T> The type of elements in the provided {@link List}.
		 * @param RNG - The {@link Random} instance to use for random selection (if applicable)
		 * @param elements - A possibly-{@code null} list of elements.
		 * @return An {@link Optional} containing a random element from the {@link List}, the only element, or {@link Optional#empty()} as described above.
		 */
		public static <T> Optional<T> trySelectFrom(@Nonnull final Random RNG, @Nullable final List<T> elements) {
			if (Objects.isNull(elements)) {
				return Optional.empty();
			}
			final int size = elements.size();
			return switch (size) {
				case 0 -> Optional.empty();
				case 1 -> Optional.ofNullable(elements.get(0));
				default -> Optional.ofNullable(elements.get(RNG.nextInt(size)));
			};
		}

		/**
		 * Returns whether the two {@link Optional} contain equal items, via {@link Objects#equals(Object)} on
		 * each of their {@link Optional#orElse(Object)} methods.
		 *
		 * @param <T> The type parameter of the {@link Optional}s
		 * @param first - The first {@link Optional} to examine
		 * @param second - The second {@link Optional} to examine
		 * @return Whether both {@link Optional} contain equivalent objects.
		 */
		public static <T> boolean sameContents(Optional<T> first, Optional<T> second) {
			return Objects.equals(first.orElse(null), second.orElse(null));
		}

		/**
		 * Returns a {@link Stream} over the non-null references held by the provided {@link Holders}.
		 *
		 * @param <T> The type held by the provided {@link Holder}
		 * @param holders - The {@link Holder} holders to use
		 * @return A stream over the non-null references held by those {@link Holder}.
		 */
		@SafeVarargs // Iterate and map
		public static <T> Stream<T> streamPresent(Holder<T>... holders) {
			return Misc.filterHolderStream(Stream.of(holders));
		}

		/**
		 * Returns a {@link Stream} over the non-null references held by the provided {@link Collection}.
		 *
		 * @param <T> The type held by the provided {@link Holder}
		 * @param holders - The {@link Collection} of {@link Holder} holders to use
		 * @return A stream over the non-null references held by those {@link Holder}.
		 */
		public static <T> Stream<T> streamPresent(Collection<Holder<T>> holders) {
			return Misc.filterHolderStream(holders.stream());
		}

		/* Internal Methods */

		protected static <T> Stream<T> filterHolderStream(Stream<Holder<T>> stream) {
			return stream.filter(Holder::isBound).map(Holder::value);
		}

	}

	/**
	 * Container class for NBT ({@link CompoundTag})-related static helper methods.
	 */
	public static final class NBT {

		public static final INBTReader<Byte> GET_BYTE = (source, key) -> source.getByte(key);
		public static final INBTReader<Short> GET_SHORT = (source, key) -> source.getShort(key);
		public static final INBTReader<Integer> GET_INT = (source, key) -> source.getInt(key);
		public static final INBTReader<Long> GET_LONG = (source, key) -> source.getLong(key);
		public static final INBTReader<Float> GET_FLOAT = (source, key) -> source.getFloat(key);
		public static final INBTReader<Double> GET_DOUBLE = (source, key) -> source.getDouble(key);
		public static final INBTReader<byte[]> GET_BYTE_ARRAY = (source, key) -> source.getByteArray(key);
		public static final INBTReader<String> GET_STRING = (source, key) -> source.getString(key);
		public static final INBTReader<CompoundTag> GET_TAG = (source, key) -> source.getCompound(key);
		public static final INBTReader<int[]> GET_INT_ARRAY = (source, key) -> source.getIntArray(key);
		public static final INBTReader<long[]> GET_LONG_ARRAY = (source, key) -> source.getLongArray(key);
		public static final INBTReader<Boolean> GET_BOOLEAN = (source, key) -> source.getBoolean(key);
		public static final INBTReader<ResourceLocation> GET_RESOURCE = NBT.makeStringReader(ResourceLocation::new);
		public static final INBTReader<Component> GET_COMPONENT = NBT.makeStringReader(Component.Serializer::fromJson);

		public static final INBTWriter<Byte> PUT_BYTE = (destination, key, value) -> destination.putByte(key, value);
		public static final INBTWriter<Short> PUT_SHORT = (destination, key, value) -> destination.putShort(key, value);
		public static final INBTWriter<Integer> PUT_INT = (destination, key, value) -> destination.putInt(key, value);
		public static final INBTWriter<Long> PUT_LONG = (destination, key, value) -> destination.putLong(key, value);
		public static final INBTWriter<Float> PUT_FLOAT = (destination, key, value) -> destination.putFloat(key, value);
		public static final INBTWriter<Double> PUT_DOUBLE = (destination, key, value) -> destination.putDouble(key, value);
		public static final INBTWriter<byte[]> PUT_BYTE_ARRAY = (destination, key, value) -> destination.putByteArray(key, value);
		public static final INBTWriter<String> PUT_STRING = (destination, key, value) -> destination.putString(key, value);
		public static final INBTWriter<CompoundTag> PUT_TAG = (destination, key, value) -> destination.put(key, value);
		public static final INBTWriter<int[]> PUT_INT_ARRAY = (destination, key, value) -> destination.putIntArray(key, value);
		public static final INBTWriter<long[]> PUT_LONG_ARRAY = (destination, key, value) -> destination.putLongArray(key, value);
		public static final INBTWriter<Boolean> PUT_BOOLEAN = (destination, key, value) -> destination.putBoolean(key, value);
		public static final INBTWriter<ResourceLocation> PUT_RESOURCE = NBT.makeStringWriter(ResourceLocation::toString);
		public static final INBTWriter<Component> PUT_COMPONENT = NBT.makeStringWriter(Component.Serializer::toJson);

		private NBT() {}

		@SuppressWarnings("unchecked") // If reader exists, cast is safe.
		public static <T> Optional<INBTReader<T>> tryGetReaderForType(int type) {
			INBTReader<?> discovered = NBT.readerForType(type);
			return Objects.nonNull(discovered) ? Optional.of((INBTReader<T>) discovered) : Optional.empty();
		}

		@SuppressWarnings("unchecked") // If writer exists, cast is safe.
		public static <T> Optional<INBTWriter<T>> tryGetWriterForType(int type) {
			INBTWriter<?> discovered = NBT.writerForType(type);
			return Objects.nonNull(discovered) ? Optional.of((INBTWriter<T>) discovered) : Optional.empty();
		}

		public static @Nullable INBTReader<?> readerForType(int type) {
			return switch (type) {
				case Tag.TAG_BYTE -> GET_BYTE;
				case Tag.TAG_SHORT -> GET_SHORT;
				case Tag.TAG_INT -> GET_INT;
				case Tag.TAG_LONG -> GET_LONG;
				case Tag.TAG_FLOAT -> GET_FLOAT;
				case Tag.TAG_DOUBLE -> GET_DOUBLE;
				case Tag.TAG_BYTE_ARRAY -> GET_BYTE_ARRAY;
				case Tag.TAG_STRING -> GET_STRING;
				case Tag.TAG_COMPOUND -> GET_TAG;
				case Tag.TAG_INT_ARRAY -> GET_INT_ARRAY;
				case Tag.TAG_LONG_ARRAY -> GET_LONG_ARRAY;
				default -> null;
			};
		}

		public static @Nullable INBTWriter<?> writerForType(int type) {
			return switch (type) {
				case Tag.TAG_BYTE -> PUT_BYTE;
				case Tag.TAG_SHORT -> PUT_SHORT;
				case Tag.TAG_INT -> PUT_INT;
				case Tag.TAG_LONG -> PUT_LONG;
				case Tag.TAG_FLOAT -> PUT_FLOAT;
				case Tag.TAG_DOUBLE -> PUT_DOUBLE;
				case Tag.TAG_BYTE_ARRAY -> PUT_BYTE_ARRAY;
				case Tag.TAG_STRING -> PUT_STRING;
				case Tag.TAG_COMPOUND -> PUT_TAG;
				case Tag.TAG_INT_ARRAY -> PUT_INT_ARRAY;
				case Tag.TAG_LONG_ARRAY -> PUT_LONG_ARRAY;
				default -> null;
			};
		}

		/**
		 * Method to automatically copy a number of existing fields from the source {@link CompoundTag} to the destination {@link CompoundTag}. Similar to a deep
		 * copy of a {@link CompoundTag} but allows for optional and selective copying of held fields.
		 * <p>
		 * The copy operation will automatically determine the types of the named fields, transcribing them appropriately.
		 * <p>
		 * If the named fields do not exist on the source {@link CompoundTag} this method does not copy the field and no such field will exist on the
		 * destination {@link CompoundTag}.
		 * <p>
		 * If none of the named fields exist on the source {@link CompoundTag}, this method will not invoke the provided {@link Supplier} so the destination
		 * may remain uninitialized (if wrapping {@link ItemStack#getOrCreateTag()} as the supplier, for instance).
		 *
		 * @param source - The {@link CompoundTag} to copy from
		 * @param destination - A {@link Supplier} of a {@link CompoundTag} to copy to
		 * @param fields - The names of the fields to copy.
		 */
		public static void autoCopyFields(@Nullable final CompoundTag source, Supplier<CompoundTag> destination, String... fields) {
			if (Objects.nonNull(source)) {
				for (String field : fields) {
					NBT.autoCopyField(source, destination, field);
				}
			}
		}

		/**
		 * Attempts to automatically copy a single existing fields from the source {@link CompoundTag} to the destination {@link CompoundTag}. Similar to a deep
		 * copy of a {@link CompoundTag} but allows for optional and selective copying of a field.
		 * <p>
		 * The copy operation will automatically determine the types of the named fields, transcribing them appropriately.
		 * <p>
		 * If the named fields do not exist on the source {@link CompoundTag} this method does not copy the field and no such field will exist on the
		 * destination {@link CompoundTag}.
		 * <p>
		 * If none of the named fields exist on the source {@link CompoundTag}, this method will not invoke the provided {@link Supplier} so the destination
		 * may remain uninitialized (if wrapping {@link ItemStack#getOrCreateTag()} as the supplier, for instance).
		 *
		 * @param source - The {@link CompoundTag} to copy from
		 * @param destination - A {@link Supplier} of a {@link CompoundTag} to copy to
		 * @param field - The name of the field to copy.
		 */
		public static <T> void autoCopyField(@Nonnull final CompoundTag source, Supplier<CompoundTag> optionalDestination, String field) {
			if (source.contains(field)) {
				byte type = source.getTagType(field);
				if (type > 0) { // Belt AND suspenders
					NBT.copyFieldUsing(field, field, source, optionalDestination, type, NBT.tryGetReaderForType(type).get(), NBT.tryGetWriterForType(type).get());
				}
			}
		}

		public static void copyInt(final String field, @Nonnull final CompoundTag source, final Supplier<CompoundTag> destination) {
			NBT.copyOptionalFieldUsing(field, field, source, destination, Tag.TAG_INT, NBT.GET_INT, NBT.PUT_INT);
		}

		public static void copyString(final String field, @Nonnull final CompoundTag source, final Supplier<CompoundTag> destination) {
			NBT.copyOptionalFieldUsing(field, field, source, destination, Tag.TAG_STRING, NBT.GET_STRING, NBT.PUT_STRING);
		}

		public static void copyTag(final String field, @Nonnull final CompoundTag source, final Supplier<CompoundTag> destination) {
			NBT.copyOptionalFieldUsing(field, field, source, destination, Tag.TAG_COMPOUND, NBT.GET_TAG, NBT.PUT_TAG);
		}

		public static void copyList(final String field, final byte listType, @Nonnull final CompoundTag source, final Supplier<CompoundTag> destination) {
			NBT.copyOptionalFieldUsing(field, field, source, destination, Tag.TAG_LIST, (tag, key) -> tag.getList(field, listType), (tag, key, value) -> tag.put(key, value));
		}

		public static void copyList(final String sourceField, final String destinationField, final byte listType, @Nonnull final CompoundTag source, final Supplier<CompoundTag> destination) {
			NBT.copyOptionalFieldUsing(sourceField, destinationField, source, destination, Tag.TAG_LIST, (tag, key) -> tag.getList(sourceField, listType), (tag, key, value) -> tag.put(key, value));
		}

		/**
		 * Safely determines whether the possibly-{@code null} {@link CompoundTag} contains a field of the provided name and type.
		 * <p>
		 * For this method to return {@code TRUE}, all of the following must be true:
		 * <li>The provided {@link CompoundTag} must not be {@code null}</li>
		 * <li>The provided {@link String} field name must not be {@code null} and must also not be empty</li>
		 * <li>The provided {@link CompoundTag} must have a field of the provided name and type.</li>
		 * In all other cases, this method will return {@code FALSE}.
		 *
		 * @param field - The name of the field to check for
		 * @param tag - The {@link CompoundTag} to check
		 * @param type - The type of the field to check for
		 * @return Whether the provided {@link CompoundTag} is non-{@code null} and contains a field of the provided name and type.
		 */
		public static boolean hasField(final String field, final int type, @Nullable final CompoundTag tag) {
			return Objects.nonNull(tag) && !Strings.isNullOrEmpty(field) && tag.contains(field, type);
		}

		public static Optional<String> tryGetString(final String field, @Nullable final CompoundTag tag) {
			return NBT.tryReadUsing(field, tag, Tag.TAG_STRING, NBT.GET_STRING);
		}
		
		public static Optional<ResourceLocation> tryGetResource(final String field, @Nullable final CompoundTag tag) {
			return NBT.tryReadString(field, tag, NBT.GET_RESOURCE);
		}
		
		public static OptionalLong tryGetLong(final String field, @Nullable final CompoundTag tag) {
			return NBT.hasField(field, CompoundTag.TAG_LONG, tag) ? OptionalLong.of(tag.getLong(field)) : OptionalLong.empty();
		}
		
		public static OptionalDouble tryGetDouble(final String field, @Nullable final CompoundTag tag) {
			return NBT.hasField(field, CompoundTag.TAG_DOUBLE, tag) ? OptionalDouble.of(tag.getDouble(field)) : OptionalDouble.empty();
		}
		
		public static OptionalBoolean tryGetBoolean(final String field, @Nullable final CompoundTag tag) {
			return NBT.hasField(field, CompoundTag.TAG_BYTE, tag) ? OptionalBoolean.of(tag.getBoolean(field)) : OptionalBoolean.empty();
		}
		
		public static Optional<Component> tryGetComponent(final String field, @Nullable final CompoundTag tag) {
			return NBT.tryReadString(field, tag, NBT.GET_COMPONENT);
		}
		
		public static Optional<CompoundTag> tryGetCompound(final String field, @Nullable final CompoundTag tag) {
			return NBT.tryReadUsing(field, tag, CompoundTag.TAG_COMPOUND, NBT.GET_TAG);
		}
		
		public static void tryPutLong(@Nullable final CompoundTag tag, final String field, OptionalLong value) {
			if (Objects.nonNull(tag) && value.isPresent()) {
				tag.putLong(field, value.getAsLong());
			}
		}
		
		public static void tryPutDouble(@Nullable final CompoundTag tag, final String field, OptionalDouble value) {
			if (Objects.nonNull(tag) && value.isPresent()) {
				tag.putDouble(field, value.getAsDouble());
			}
		}
		
		public static void tryPutBoolean(@Nullable final CompoundTag tag, final String field, OptionalBoolean value) {
			if (Objects.nonNull(tag) && value.isPresent()) {
				tag.putBoolean(field, value.getAsBoolean());
			}
		}
		
		public static void tryPutComponent(@Nullable final CompoundTag tag, final String field, Optional<Component> component) {
			NBT.tryPut(tag, field, component, NBT.PUT_COMPONENT);
		}
		
		public static void tryPutResource(@Nullable final CompoundTag tag, final String field, Optional<ResourceLocation> resource) {
			NBT.tryPut(tag, field, resource, NBT.PUT_RESOURCE);
		}

		public static <T> Optional<T> tryDecode(final String field, @Nullable final CompoundTag tag, Codec<T> decoder, Consumer<String> onError) {
			return NBT.tryGetCompound(field, tag).flatMap(found -> Codecs.decodeNBT(decoder, found, onError));
		}

		public static <T> void tryEncode(final String field, @Nonnull final ItemStack stack, Codec<T> encoder, T data) {
			NBT.tryWrite(encoder, data, dataTag -> stack.getOrCreateTag().put(field, dataTag));
		}

		public static <T> void tryEncode(final String field, @Nonnull final CompoundTag tag, Codec<T> encoder, T data) {
			NBT.tryWrite(encoder, data, dataTag -> tag.put(field, tag));
		}

		public static <T> void tryEncode(@Nonnull final String field, @Nonnull final ItemStack stack, @Nonnull final Codec<T> encoder, @Nonnull final Optional<T> data) {
			data.ifPresent(held -> NBT.tryEncode(field, stack, encoder, held));
		}

		public static <T> void tryEncode(@Nonnull final String field, @Nonnull final CompoundTag tag, @Nonnull final Codec<T> encoder, @Nonnull final Optional<T> data) {
			data.ifPresent(held -> NBT.tryEncode(field, tag, encoder, held));
		}

		/**
		 * Safely Stringifies the provided {@link CompoundTag}, even if it is {@code null}.
		 *
		 * @param tag - The {@link CompoundTag} to Stringifiy
		 * @return A {@link String} representation of the provided {@link CompoundTag} reference.
		 */
		public static String asString(@Nullable CompoundTag tag) {
			return Objects.isNull(tag) ? String.valueOf(tag) : tag.getAsString();
		}

		/* Accessor interface implementations */

		@FunctionalInterface
		public static interface INBTReader<T> {

			public T readFrom(final @Nonnull CompoundTag source, final @Nonnull String key);

		}

		@FunctionalInterface
		public static interface INBTWriter<T> {

			public void writeTo(final @Nonnull CompoundTag destination, final @Nonnull String field, T value);

		}

		public static interface INBTAccessor<T> extends INBTReader<T>, INBTWriter<T> {}

		/* Internal Methods */
		
		protected static <T> INBTReader<T> makeStringReader(Function<? super String, T> mapper) {
			return (source, key) -> mapper.apply(source.getString(key));
		}
		
		protected static <T> INBTWriter<T> makeStringWriter(Function<T, String> mapper) {
			return (destination, key, value) -> destination.putString(key, mapper.apply(value));
		}

		protected static <T> void tryWrite(Codec<T> encoder, T data, Consumer<? super Tag> action) {
			encoder.encodeStart(NbtOps.INSTANCE, data).result().ifPresent(action);
		}
		
		protected static <T> void tryPut(@Nullable CompoundTag destination, final String key, Optional<T> holder, INBTWriter<T> writer) {
			if (Objects.nonNull(destination) && holder.isPresent()) {
				writer.writeTo(destination, key, holder.get());
			}
		}
		
		protected static <T> Optional<T> tryReadString(final String key, @Nullable CompoundTag source, INBTReader<T> reader) {
			return NBT.tryReadUsing(key, source, CompoundTag.TAG_STRING, reader);
		}

		protected static <T> Optional<T> tryReadUsing(final String key, @Nullable CompoundTag source, int type, INBTReader<T> reader) {
			return NBT.hasField(key, type, source) ? Optional.ofNullable(reader.readFrom(source, key)) : Optional.empty();
		}

		protected static <T> void copyOptionalFieldUsing(final String sourceFieldName, final String destinationFieldName, @Nullable CompoundTag source, final Supplier<CompoundTag> optionalDestination, final int fieldType, final INBTReader<T> getter, final INBTWriter<T> setter) {
			if (NBT.hasField(sourceFieldName, fieldType, source)) {
				NBT.copyFieldUsing(sourceFieldName, destinationFieldName, source, optionalDestination, fieldType, getter, setter);
			}
		}

		protected static <T> void copyFieldUsing(final String sourceFieldName, final String destinationFieldName, @Nullable CompoundTag source, final Supplier<CompoundTag> optionalDestination, final int fieldType, final INBTReader<T> getter, final INBTWriter<T> setter) {
			setter.writeTo(optionalDestination.get(), destinationFieldName, getter.readFrom(source, sourceFieldName));
		}

	}

	public static class Game {

		private Game() {}
		
		/**
		 * Attempts to get the {@link ResourceLocation} id from the provided {@link Holder}, returning {@link Optional#empty()} if the {@link Holder} is
		 * not bound.
		 * <p>
		 * If the provided {@link Holder} is {@code null}, will return {@link Optional#empty()}.
		 * 
		 * @param holder - The {@link Holder} to examine
		 * @return The bound {@link ResourceKey}, or {@link Optional#empty()}.
		 */
		public static Optional<ResourceLocation> getID(@Nullable final Holder<?> holder) {
			return Game.getKey(holder).map(ResourceKey::location);
		}
		
		/**
		 * Attempts to get the {@link ResourceKey} id from the provided {@link Holder}, returning {@link Optional#empty()} if the {@link Holder} is
		 * not bound.
		 * <p>
		 * If the provided {@link Holder} is {@code null}, will return {@link Optional#empty()}.
		 * 
		 * @param holder - The {@link Holder} to examine
		 * @return The bound {@link ResourceKey}, or {@link Optional#empty()}.
		 */
		public static <T> Optional<ResourceKey<T>> getKey(@Nullable final Holder<T> holder) {
			return Objects.nonNull(holder) ? holder.unwrapKey() : Optional.empty();
		}

		/**
		 * Returns whether the provided {@link LivingEntity} has the effect contained within the provided {@link Holder}.
		 * <p>
		 * Returns {@code TRUE} if the provided {@link LivingEntity} is non-null, and the provided {@link Holder} contains a valid reference, and
		 * if {@link LivingEntity#hasEffect(MobEffect)} returns {@code TRUE} for the contained effect.
		 * <p>
		 * Good for mod-provided effects since those tend to be held by {@link Holder} anyways.
		 *
		 * @param entity - The {@link LivingEntity} to examine
		 * @param reference - The {@link Holder} holding the effect
		 * @return {@code TRUE} if the {@link LivingEntity} has the effect held by the {@link Holder}, and {@code FALSE} otherwise.
		 */
		public static boolean entityHasEffect(@Nullable final LivingEntity entity, @Nonnull final Holder<MobEffect> reference) {
			return Objects.nonNull(entity) && reference.isBound() && entity.hasEffect(reference.value());
		}

		/**
		 * Returns the opposite of the provided input hand.
		 *
		 * @param in The examined hand
		 * @return The opposite of the examined hand
		 */
		public static InteractionHand getOpposite(InteractionHand in) {
			return switch (in) {
				case OFF_HAND -> InteractionHand.MAIN_HAND;
				default -> InteractionHand.OFF_HAND;
			};
		}

		/**
		 * Returns the provided {@link BlockEntityTicker} if the provided {@link Level} is not client side and the discovered {@link BlockEntityType} is of the expected type.
		 * <p>
		 * Effectively combines a client-side check with the protected method {@link BaseEntityBlock#createTickerHelper()}.
		 *
		 * @param <T> The type of {@link BlockEntity}
		 * @param world - The {@link Level} in which the {@link BlockEntity} resides
		 * @param discovered - The discovered {@link BlockEntity}
		 * @param expected - The expected {@link BlockEntity} type
		 * @param ticker - The actual ticker to return
		 * @return The provided ticker, if the aforementioned checks have passed and {@code null} otherwise.
		 */
		@Nullable
		@SuppressWarnings("unchecked") // Look, if Mojang can get away with it so can I.
		public static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> getTicker(Level world, BlockEntityType<A> discovered, Holder<BlockEntityType<?>> expected, BlockEntityTicker<? super E> ticker) {
			return (!world.isClientSide() && Objects.equals(discovered, expected.value())) ? (BlockEntityTicker<A>) ticker : null;
		}

		/**
		 * Tries to rename the provided {@link BlockEntity} using the display name of the provided {@link Stack}.
		 * <p>
		 * Merely checks to see if the provided {@link BlockEntity} is {@link BaseContainerBlockEntity}, and uses the inbuilt mechanism for renaming.
		 *
		 * @param discovered - The {@link BlockEntity} to rename (may be null)
		 * @param namedStack - The {@link ItemStack} whose display name should be used to rename the {@link BlockEntity}
		 */
		public static void tryRenameFrom(@Nullable BlockEntity discovered, @Nonnull ItemStack namedStack) {
			if (namedStack.hasCustomHoverName() && discovered instanceof BaseContainerBlockEntity entity) {
				entity.setCustomName(namedStack.getDisplayName());
			}
		}

		/**
		 * Returns whether the chunk at the indicated {@link BlockPos} is currently loaded.
		 *
		 * @param world - A reference to the world containing the chunk
		 * @param position - The {@link BlockPos} to check
		 * @return Whether the chunk containing {@link BlockPos} is loaded.
		 */
		public static boolean isBlockLoaded(LevelAccessor world, BlockPos position) {
			return world.hasChunk(position.getX() >> 4, position.getZ() >> 4);
		}

		/**
		 * Performs rudimentary check of the provided {@link ItemStack} to see if it has inventory data attached.
		 * <p>
		 * Checks the {@link ItemStack} tag for a tag list named {@literal "Items"}, as this is the mechanism the base
		 * game uses to attach inventories to {@link ItemStack}, such as for Shulker Boxes.
		 *
		 * @param stack - The {@link ItemStack} to examine.
		 * @return Whether inventory data is attached to the provided {@link ItemStack}.
		 */
		public static boolean itemHasInventory(@Nullable ItemStack stack) {
			final String entityDataTag = "BlockEntityTag";
			final String legacyInventoryTag = "Items";
			if (Objects.nonNull(stack) && stack.hasTag()) {
				if (stack.getTag().contains(legacyInventoryTag, Tag.TAG_LIST)) {
					return true;
				}
				else if (stack.getTag().contains(entityDataTag, Tag.TAG_COMPOUND)) {
					return stack.getTagElement(entityDataTag).contains(legacyInventoryTag, Tag.TAG_LIST);
				}
			}
			return false;
		}

		/**
		 * Attempts to parse the received {@link String} as a {@link ResourceLocation}, and attempts to look up that {@link ResourceLocation} in the {@link ForgeRegistries#ITEMS}
		 * registry, returning the result (if any).
		 *
		 * @param input - The input {@link String}, ideally parseable as a {@link ResourceLocation}.
		 * @return The discovered {@link Item}.
		 * @throws ResourceLocationException - If the input {@link String} cannot validly be converted into a {@link ResourceLocation} for registry lookup.
		 */
		public static @Nullable Item tryParseItem(String input) throws ResourceLocationException {
			return BuiltInRegistries.ITEM.get(new ResourceLocation(input.trim()));
		}

		/**
		 * Returns the {@link Vec3i} component corresponding to the provided {@link Axis}.
		 *
		 * @param position - The vector to query
		 * @param axis - The axis to examine
		 * @return The component of the provided {@link Vec3i} corresponding to the provided {@link Axis}.
		 */
		public static int componentFrom(final Vec3i position, final Axis axis) {
			switch (axis) {
				case X:
					return position.getX();
				case Y:
					return position.getY();
				case Z:
					return position.getZ();
			}
			return 0;
		}

		/**
		 * Returns a {@link Stream} over all blocks within the given cubic "radius" centered on the passed origin {@link BlockPos}.
		 *
		 * @param origin - The origin {@link BlockPos}
		 * @param radius - The "radius" to seek
		 * @return A {@link Stream} over all {@link BlockPos} at most {@code radius} blocks away from the origin {@link BlockPos}.
		 */
		public static Stream<BlockPos> streamCubicRadius(BlockPos origin, final int radius) {
			return BlockPos.betweenClosedStream(origin.getX() - radius, origin.getY() - radius, origin.getZ() - radius, origin.getX() + radius, origin.getY() + radius, origin.getZ() + radius);
		}

		/**
		 * Returns a {@link Stream} over all blocks orthogonal to the passed origin {@link BlockPos} in the indicated {@link Direction}.
		 * Merely calls {@link Streams#stream(Iterable)} using {@link #allOrthogonalTo(BlockPos, int, Direction...)}.
		 *
		 * @param origin - The origin {@link BlockPos}
		 * @param distance - The distance to seek
		 * @param direction - The direction(s) to seek in
		 * @return All {@link BlockPos} orthogonal to the origin in each of the given {@link Direction} all within the passed distance.
		 * @see #allOrthogonalTo(BlockPos, int, Direction...)
		 */
		public static Stream<BlockPos> streamOrthogonalTo(BlockPos origin, int distance, Direction... directions) {
			return Streams.stream(Game.allOrthogonalTo(origin, distance, directions));
		}

		/**
		 * Returns an {@link Iterable} over all blocks orthogonal to the passed origin {@link BlockPos} in the indicated {@link Direction}.
		 * If a {@link Direction} is indicated more than once, those blocks will be present in the resulting {@link Iterable} more than once.
		 * The returned {@link Iterable} is immutable.
		 *
		 * @param origin - The origin {@link BlockPos}
		 * @param distance - The distance to seek
		 * @param direction - The direction(s) to seek in
		 * @return All {@link BlockPos} orthogonal to the origin in each of the given {@link Direction} all within the passed distance.
		 */
		public static Iterable<BlockPos> allOrthogonalTo(BlockPos origin, int distance, Direction... directions) {
			ImmutableList.Builder<BlockPos> builder = new ImmutableList.Builder<>();
			for (Direction iterated : directions) {
				Game.appendOrthogonal(builder, origin, iterated, distance);
			}
			return builder.build();
		}

		/**
		 * Returns a {@link Stream} of all {@link BlockPos} orthogonal to the passed origin {@link BlockPos} in the given {@link Direction} within the given distance.
		 * Merely calls {@link Streams#stream(Iterable)} using {@link #allOrthogonalTo(BlockPos, int, Direction)}.
		 *
		 * @param origin - The origin {@link BlockPos}
		 * @param distance - The distance to seek
		 * @param direction - The direction to seek in
		 * @return All {@link BlockPos} orthogonal to the origin in {@link Direction} within the passed distance.
		 * @see #allOrthogonalTo(BlockPos, int, Direction)
		 */
		public static Stream<BlockPos> streamOrthogonalTo(BlockPos origin, int distance, Direction direction) {
			return Streams.stream(Game.allOrthogonalTo(origin, distance, direction));
		}

		/**
		 * Returns an {@link Iterable} over all blocks orthogonal to the passed origin {@link BlockPos} in the given {@link Direction} within the given distance.
		 * The returned {@link Iterable} is immutable.
		 *
		 * @param origin - The origin {@link BlockPos}
		 * @param distance - The distance to seek
		 * @param direction - The direction to seek in
		 * @return All {@link BlockPos} orthogonal to the origin in {@link Direction} within the passed distance.
		 */
		public static Iterable<BlockPos> allOrthogonalTo(BlockPos origin, int distance, Direction direction) {
			ImmutableList.Builder<BlockPos> builder = new ImmutableList.Builder<>();
			Game.appendOrthogonal(builder, origin, direction, distance);
			return builder.build();
		}

		/**
		 * Returns a {@link Stream} of all orthogonal neighbors of the passed {@link BlockPos}.
		 *
		 * @param position - The origin {@link BlockPos}
		 * @return A {@link Stream} of all orthogonal neighbors of the origin.
		 */
		public static Stream<BlockPos> streamOrthogonalNeighbors(BlockPos origin) {
			return Stream.of(origin.above(), origin.below(), origin.north(), origin.south(), origin.east(), origin.west());
		}

		/**
		 * Returns a {@link Stream} of all horizontal neighbors of the passed {@link BlockPos}.
		 * <p>
		 * Iteration order is NORTH, EAST, SOUTH, WEST.
		 *
		 * @param position - The origin {@link BlockPos}
		 * @return All orthogonal neighbors of the origin.
		 */
		public static Stream<BlockPos> streamHorizontalNeighbors(BlockPos position) {
			return Stream.of(position.north(), position.east(), position.south(), position.west());
		}

		/**
		 * Returns a {@link Stream} of all horizontal neighbors of the passed {@link BlockPos}.
		 * <p>
		 * Iteration order is NORTH, EAST, SOUTH, WEST.
		 *
		 * @param position - The origin {@link BlockPos}
		 * @return All orthogonal neighbors of the origin.
		 */
		public static Iterable<BlockPos> horizontalNeighbors(BlockPos position) {
			ImmutableList.Builder<BlockPos> builder = new ImmutableList.Builder<>();
			builder.add(position.north(), position.east(), position.south(), position.west());
			return builder.build();
		}

		/**
		 * Returns an {@link Iterable} over all orthogonal neighbors of the passed {@link BlockPos}.
		 * Iteration order is UP, DOWN, NORTH, SOUTH, EAST, WEST.
		 * The returned {@link Iterable} is immutable.
		 *
		 * @param position - The origin {@link BlockPos}
		 * @return All orthogonal neighbors of the origin.
		 */
		public static Iterable<BlockPos> orthogonalNeighborsOf(BlockPos position) {
			ImmutableList.Builder<BlockPos> builder = new ImmutableList.Builder<>();
			builder.add(position.above(), position.below(), position.north(), position.south(), position.east(), position.west());
			return builder.build();
		}

		/**
		 * Streams all {@link BlockPos} in a horizontal cubic radius from the provided origin point.
		 *
		 * @param origin - The origin point
		 * @param radius - The radius, in blocks, within which {@link BlockPos} should be returned
		 * @return A {@link Stream} over all {@link BlockPos} at most {@code radius} from the origin
		 */
		public static Stream<BlockPos> streamHorizontalRadius(BlockPos origin, int radius) {
			return BlockPos.betweenClosedStream(origin.getX() - radius, origin.getY(), origin.getZ() - radius, origin.getX() + radius, origin.getY(), origin.getZ() + radius);
		}

		/**
		 * Calls {@link #spawnItemNear(Level, ItemStack, double, double, double, Random, double)} for each item in the passed {@link Iterable},
		 * starting at the "block center" of the passed position.
		 *
		 * @param world - The world to spawn the {@link ItemEntity} in
		 * @param position - The base coordinate to spawn at
		 * @param items - The items to spawn.
		 * @param RNG - The {@link Random} instance to use
		 * @param spread - The maximum offset from the passed position
		 */
		public static void spawnItemsNear(LevelAccessor world, Vec3i position, Iterable<ItemStack> items, Random RNG, double spread) {
			Game.spawnItemsNear(world, position.getX() + 0.5D, position.getY() + 0.5D, position.getZ() + 0.5D, items, RNG, spread);
		}

		/**
		 * Calls {@link #spawnItem(Level, ItemStack, double, double, double)} at the passed position, offset by 0.5D for each coordinate.
		 * This is useful for spawning {@link ItemEntity} at the precise center of a block.
		 *
		 * @param world - The world to spawn the {@link ItemEntity} in
		 * @param position - The base coordinate to spawn at
		 * @param items - The items to spawn.
		 */
		public static void spawnItems(LevelAccessor world, Vec3i position, Iterable<ItemStack> items) {
			Game.spawnItems(world, position.getX() + 0.5D, position.getY() + 0.5D, position.getZ() + 0.5D, items);
		}

		/**
		 * Calls {@link #spawnItemsNear(Level, double, double, double, Iterable, Random, double)} for each item in the passed {@link Iterable}.
		 *
		 * @param world - The world to spawn the {@link ItemEntity} in
		 * @param item - The {@link ItemStack} that the {@link ItemEntity} should contain
		 * @param xPos - The X coordinate to spawn at
		 * @param yPos - The Y coordinate to spawn at
		 * @param zPos - The Z coordinate to spawn at
		 * @param items - The items to spawn
		 * @param RNG - The {@link Random} instance to use
		 * @param spread - The maximum offset from the passed position
		 */
		public static void spawnItemsNear(LevelAccessor world, double xPos, double yPos, double zPos, Iterable<ItemStack> items, Random RNG, double spread) {
			items.forEach(item -> Game.spawnItemNear(world, item, xPos, yPos, zPos, RNG, spread));
		}

		/**
		 * Calls {@link #spawnItem(Level, ItemStack, double, double, double)} for each item in the passed {@link Iterable}.
		 *
		 * @param world - The world to spawn the {@link ItemEntity} in
		 * @param item - The {@link ItemStack} that the {@link ItemEntity} should contain
		 * @param xPos - The X coordinate to spawn at
		 * @param yPos - The Y coordinate to spawn at
		 * @param zPos - The Z coordinate to spawn at
		 * @param items - The items to spawn
		 * @see #spawnItem(Level, ItemStack, double, double, double)
		 */
		public static void spawnItems(LevelAccessor world, double xPos, double yPos, double zPos, Iterable<ItemStack> items) {
			items.forEach(item -> Game.spawnItem(world, item, xPos, yPos, zPos));
		}

		/**
		 * Calls {@link #spawnItem(Level, ItemStack, double, double, double)} with a slightly-randomized offset for each coordinate.
		 *
		 * @param world - The world to spawn the {@link ItemEntity} in
		 * @param item - The {@link ItemStack} that the {@link ItemEntity} should contain
		 * @param xPos - The X coordinate to spawn at
		 * @param yPos - The Y coordinate to spawn at
		 * @param zPos - The Z coordinate to spawn at
		 * @param item - The items to spawn
		 * @param RNG - The {@link Random} instance to use
		 * @param spread - The maximum offset from the passed position
		 * @return The created {@link ItemEntity} or {@code null}, as described by {@link #spawnItem(Level, ItemStack, double, double, double)}.
		 * @see #spawnItem(Level, ItemStack, double, double, double)
		 */
		public static @Nullable ItemEntity spawnItemNear(LevelAccessor world, @Nullable ItemStack item, double xPos, double yPos, double zPos, Random RNG, double spread) {
			return Game.spawnItem(world, item, xPos + Game.getOffset(RNG, spread), yPos + Game.getOffset(RNG, spread), zPos + Game.getOffset(RNG, spread));
		}

		/**
		 * Calls {@link #spawnItem(Level, ItemStack, double, double, double)} with a slightly-randomized offset for each coordinate.
		 *
		 * @param world - The world to spawn the {@link ItemEntity} in
		 * @param item - The {@link ItemStack} that the {@link ItemEntity} should contain
		 * @param position - The coordinates to spawn at
		 * @param item - The items to spawn
		 * @param RNG - The {@link Random} instance to use
		 * @param spread - The maximum offset from the passed position
		 * @return The created {@link ItemEntity} or {@code null}, as described by {@link #spawnItem(Level, ItemStack, double, double, double)}.
		 * @see #spawnItem(Level, ItemStack, double, double, double)
		 */
		public static @Nullable ItemEntity spawnItemNear(LevelAccessor world, @Nullable ItemStack item, Vec3i position, Random RNG, double spread) {
			return Game.spawnItem(world, item, position.getX() + 0.5D + Game.getOffset(RNG, spread), position.getY() + 0.5D + Game.getOffset(RNG, spread), position.getZ() + 0.5D + Game.getOffset(RNG, spread));
		}

		/**
		 * Spawns the passed {@link ItemStack} in the world as an {@link ItemEntity} at the indicated coordinates and with the default item pickup delay (10 ticks).
		 * If the {@link ItemStack} is {@code null} or returns {@code true} for {@link ItemStack#isEmpty()}, this method will perform no action and return {@code null}.
		 * Otherwise, it will return the created {@link ItemEntity}.
		 *
		 * @param world - The world to spawn the {@link ItemEntity} in
		 * @param item - The {@link ItemStack} that the {@link ItemEntity} should contain
		 * @param xPos - The X coordinate to spawn at
		 * @param yPos - The Y coordinate to spawn at
		 * @param zPos - The Z coordinate to spawn at
		 * @return The created {@link ItemEntity} or {@code null}, as described above.
		 * @see #spawnItem(Level, ItemStack, double, double, double, int)
		 */
		public static @Nullable ItemEntity spawnItem(LevelAccessor world, @Nullable ItemStack item, double xPos, double yPos, double zPos) {
			return Game.spawnItem(world, item, xPos, yPos, zPos, 10);
		}

		/**
		 * Spawns the passed {@link ItemStack} in the world as an {@link ItemEntity} at the indicated coordinates and with the passed pickup delay (in ticks).
		 * If the {@link ItemStack} is {@code null} or returns {@code true} for {@link ItemStack#isEmpty()}, this method will perform no action and return {@code null}.
		 * Otherwise, it will return the created {@link ItemEntity}.
		 *
		 * @param world - The world to spawn the {@link ItemEntity} in
		 * @param item - The {@link ItemStack} that the {@link ItemEntity} should contain
		 * @param xPos - The X coordinate to spawn at
		 * @param yPos - The Y coordinate to spawn at
		 * @param zPos - The Z coordinate to spawn at
		 * @param pickupDelay - The delay before the created {@link ItemEntity} can be picked up
		 * @return The created {@link ItemEntity} or {@code null}, as described above.
		 */
		public static @Nullable ItemEntity spawnItem(LevelAccessor world, @Nullable ItemStack item, double xPos, double yPos, double zPos, int pickupDelay) {
			if (Game.stackValid(item)) {
				ItemEntity instance = new ItemEntity((Level) world, xPos, yPos, zPos, item);
				instance.setPickUpDelay(pickupDelay);
				world.addFreshEntity(instance);
				return instance;
			}
			return null;
		}

		/**
		 * Returns whether the passed {@link ItemStack} reference is valid.
		 * Returns {@code true} if the {@link ItemStack} is non-{@code null} and {@link ItemStack#isEmpty()} returns {@code false}.
		 *
		 * @param stack - The stack to examine
		 * @return Whether the {@link ItemStack} is valid.
		 */
		public static boolean stackValid(@Nullable ItemStack stack) {
			return Objects.nonNull(stack) && !stack.isEmpty();
		}

		/* Internal Methods */

		protected static void appendOrthogonal(ImmutableList.Builder<BlockPos> list, BlockPos origin, Direction direction, int distance) {
			for (int count = 1; count <= distance; count += 1) {
				list.add(origin.relative(direction, count));
			}
		}

		protected static double getOffset(Random RNG, double spread) {
			return spread - (RNG.nextDouble() * 2.0D * spread);
		}

	}

	public static final class Tags {

		private Tags() {}

		/**
		 * Creates a {@link ResourceLocation} suitable for use as an {@link TagKey} name.
		 *
		 * @param domain - The domain of the tag
		 * @param path - The tag's "path".
		 * @return A suitably-constructed tag string.
		 */
		public static ResourceLocation createName(String domain, String... path) {
			StringBuilder builder = new StringBuilder();
			Strings.concatenateUsing(builder, Strings.DELIMITER_PATH, path);
			return new ResourceLocation(domain, builder.toString());
		}

		/**
		 * Convenience method to create a Forge-domain "ingot" tag.
		 * <p>
		 * The resulting tag will be in {@code data.forge.tags.items.ingot}.
		 *
		 * @param type - The type of ingot
		 * @return A suitable {@link TagKey}.
		 */
		public static TagKey<Item> createIngotTag(String type) {
			return Tags.forgeItemTag("ingot", type);
		}

		/**
		 * Convenience method to create a Forge-domain "nugget" tag.
		 * <p>
		 * The resulting tag will be in {@code data.forge.tags.items.nugget}.
		 *
		 * @param type - The type of ingot
		 * @return A suitable {@link TagKey}.
		 */
		public static TagKey<Item> createNuggetTag(String type) {
			return Tags.forgeItemTag("nugget", type);
		}

		/**
		 * Convenience method to create a Forge-domain "storage_blocks" tag.
		 * <p>
		 * The resulting tag will be in {@code data.forge.tags.items.block}.
		 *
		 * @param type - The type of ingot
		 * @return A suitable {@link TagKey}.
		 */
		public static TagKey<Item> createBlockTag(String type) {
			return Tags.forgeItemTag("storage_blocks", type);
		}

		/**
		 * Convenience method to create a Forge-domain "ore" tag.
		 * <p>
		 * The resulting tag will be in {@code data.forge.tags.items.ore}.
		 *
		 * @param type - The type of ingot
		 * @return A suitable {@link TagKey}.
		 */
		public static TagKey<Item> createOreTag(String type) {
			return Tags.forgeItemTag("ore", type);
		}

		/**
		 * Gets or creates an {@link TagKey} in the Minecraft Forge domain with the passed "path".
		 *
		 * @param path - The {@link Item} tag "path" to use
		 * @return The existing or created {@link TagKey}.
		 */
		public static TagKey<Item> forgeItemTag(String... path) {
			return Tags.itemTag(Strings.DOMAIN_FORGE, path);
		}

		/**
		 * Gets or creates an {@link TagKey} in the Minecraft Forge domain with the passed "path".
		 *
		 * @param path - The {@link Block} tag "path" to use
		 * @return The existing or created {@link TagKey}.
		 */
		public static TagKey<Block> forgeBlockTag(String... path) {
			return Tags.blockTag(Strings.DOMAIN_FORGE, path);
		}

		/**
		 * Gets or creates an {@link TagKey} in the indicated domain, with the passed "path".
		 *
		 * @param domain - The domain to use
		 * @param path - The {@link Item} tag "path" to use
		 * @return The existing or created {@link TagKey}.
		 */
		public static TagKey<Item> itemTag(String domain, String... path) {
			return Tags.createTag(ItemTags::create, domain, path);
		}

		/**
		 * Gets or creates an {@link TagKey} in the indicated domain, with the passed "path".
		 *
		 * @param domain - The domain to use
		 * @param path - The {@link Block} tag "path" to use
		 * @return The existing or created {@link TagKey}.
		 */
		public static TagKey<Block> blockTag(String domain, String... path) {
			return Tags.createTag(BlockTags::create, domain, path);
		}

		/**
		 * Gets or creates an {@link TagKey} in the indicated domain using the passed transformer function and tag "path".
		 *
		 * @param <T> The registry type of the desired {@link TagKey}
		 * @param transformer - The transformer function (See {@link ItemTags#makeWrapperTag(String)} and {@link BlockTags#makeWrapperTag(String)})
		 * @param domain - The domain to use
		 * @param path - The {@link Block} tag "path" to use
		 * @see {@link ItemTags#makeWrapperTag(String)}, {@link BlockTags#makeWrapperTag(String)}
		 * @return
		 */
		public static <T> TagKey<T> createTag(Function<ResourceLocation, TagKey<T>> transformer, String domain, String... path) {
			return transformer.apply(Tags.createName(domain, path));
		}

	}

	public static final class Strings {

		/* Constants */
		public static final String DOMAIN_FORGE = "forge";
		public static final String DOMAIN_MINECRAFT = "minecraft";
		public static final String FILE_SUFFIX_PNG = "png";
		public static final String TOOLTIP = "tooltip";

		public static final String EMPTY = "";
		public static final String[] EMPTY_ARRAY = {};
		public static final String DELIMITER_PATH = "/";
		public static final String DELIMITER_NAME = "_";
		public static final String DELIMITER_TYPE = ".";
		public static final String DELIMITER_RESOURCE = ":";

		private Strings() {}

		/**
		 * Converts the passed {@link String} path into a {@link ResourceLocation} in the {@value #DOMAIN_MINECRAFT} namespace.
		 *
		 * @param path - The resource path to use
		 * @return A {@link ResourceLocation} in the {@value #DOMAIN_MINECRAFT} namespace.
		 */
		public static @Nonnull ResourceLocation minecraftResource(@Nonnull String path) {
			return new ResourceLocation(DOMAIN_MINECRAFT, path);
		}

		/**
		 * Splits the passed {@link String} around occurrences of one or more whitespace characters,
		 * returning the resulting array.
		 * If the {@link String} received by this method is null or empty, an empty array will be returned instead.
		 *
		 * @param input - The {@link String} to split
		 * @return A suitably-split {@link String} array, or an empty one.
		 */
		public static @Nonnull String[] splitByWhitespace(@Nullable String input) {
			return isNullOrEmpty(input) ? EMPTY_ARRAY : input.trim().split("\\s+");
		}

		/**
		 * Returns whether the passed {@link String} is null or empty.
		 *
		 * @param input - The {@link String} to check
		 * @return Whether the {@link String} is null or empty.
		 */
		public static boolean isNullOrEmpty(@Nullable String input) {
			return Objects.isNull(input) ? true : input.isEmpty();
		}

		/**
		 * Concatenates the {@link String} in the passed {@link Iterable} with {@link #DELIMITER_PATH} betwixt.
		 *
		 * @param elements - The {@link String}s to concatenate
		 * @return A suitably-concatenated {@link String}.
		 */
		public static String path(Iterable<String> elements) {
			return Strings.concatenate(DELIMITER_PATH, elements);
		}

		/**
		 * Concatenates the passed {@link String}s with {@link #DELIMITER_PATH} betwixt.
		 *
		 * @param elements - The {@link String}s to concatenate
		 * @return A suitably-concatenated {@link String}.
		 */
		public static String path(String... elements) {
			return Strings.concatenate(DELIMITER_PATH, elements);
		}

		/**
		 * Concatenates the passed {@link String}s with {@link #DELIMITER_PATH} betwixt, and appends the {@value #FILE_SUFFIX_PNG} file suffix to the end.
		 * <p>
		 * Suitable for use as an explicit path to a texture resource.
		 *
		 * @param elements - The {@link String}s to concatenate
		 * @return A suitably-concatenated {@link String}.
		 */
		public static String texturePath(Iterable<String> elements) {
			return Strings.filePath(FILE_SUFFIX_PNG, elements);
		}

		/**
		 * Concatenates the passed {@link String}s with {@link #DELIMITER_PATH} betwixt, and appends the {@value #FILE_SUFFIX_PNG} file suffix to the end.
		 * <p>
		 * Suitable for use as an explicit path to a texture resource.
		 *
		 * @param elements - The {@link String}s to concatenate
		 * @return A suitably-concatenated {@link String}.
		 */
		public static String texturePath(String... elements) {
			return Strings.filePath(FILE_SUFFIX_PNG, elements);
		}

		/**
		 * Concatenates the passed {@link String}s with {@link #DELIMITER_PATH} betwixt, and appends the file suffix to the end.
		 * <p>
		 * Suitable for use as an explicit path to a file asset.
		 *
		 * @param suffix - The file suffix to append
		 * @param elements - The {@link String}s to concatenate
		 * @return A suitably-concatenated {@link String}.
		 */
		public static String filePath(String suffix, Iterable<String> elements) {
			return Strings.concatenateUsing(DELIMITER_PATH, elements)
				.append(DELIMITER_TYPE)
				.append(FILE_SUFFIX_PNG)
				.toString();
		}

		/**
		 * Concatenates the passed {@link String}s with {@link #DELIMITER_PATH} betwixt, and appends the file suffix to the end.
		 * <p>
		 * Suitable for use as an explicit path to a file asset.
		 *
		 * @param suffix - The file suffix to append
		 * @param elements - The {@link String}s to concatenate
		 * @return A suitably-concatenated {@link String}.
		 */
		public static String filePath(String suffix, String... elements) {
			return Strings.concatenateUsing(DELIMITER_PATH, elements)
				.append(DELIMITER_TYPE)
				.append(FILE_SUFFIX_PNG)
				.toString();
		}

		/**
		 * Concatenates the {@link String} in the passed {@link Iterable} with {@link #DELIMITER_NAME} betwixt.
		 *
		 * @param elements - The {@link String}s to concatenate
		 * @return A suitably-concatenated {@link String}.
		 */
		public static String name(Iterable<String> elements) {
			return Strings.concatenate(DELIMITER_NAME, elements);
		}

		/**
		 * Concatenates the passed {@link String}s with {@link #DELIMITER_NAME} betwixt.
		 *
		 * @param elements - The {@link String}s to concatenate
		 * @return A suitably-concatenated {@link String}.
		 */
		public static String name(String... elements) {
			return Strings.concatenate(DELIMITER_NAME, elements);
		}

		/**
		 * Concatenates the {@link String} in the passed {@link Iterable} with {@link #DELIMITER_TYPE} betwixt.
		 *
		 * @param elements - The {@link String}s to concatenate
		 * @return A suitably-concatenated {@link String}.
		 */
		public static String type(Iterable<String> elements) {
			return Strings.concatenate(DELIMITER_TYPE, elements);
		}

		/**
		 * Concatenates the passed {@link String}s with {@link #DELIMITER_TYPE} betwixt.
		 *
		 * @param elements - The {@link String}s to concatenate
		 * @return A suitably-concatenated {@link String}.
		 */
		public static String type(String... elements) {
			return Strings.concatenate(DELIMITER_TYPE, elements);
		}

		/**
		 * Concatenates the passed String array with the passed delimiter placed betwixt each entry.
		 *
		 * @param delimiter - The String to place between each String in {@code elements}
		 * @param elements - The Strings to concatenate
		 * @return A suitably-concatenated String.
		 */
		public static String concatenate(String delimiter, Iterable<String> elements) {
			return Strings.concatenateUsing(delimiter, elements).toString();
		}

		/**
		 * Concatenates the passed String varargs array with the passed delimiter placed betwixt each entry.
		 *
		 * @param delimiter - The String to place between each String in {@code elements}
		 * @param elements - The array of Strings to concatenate
		 * @return A suitably-concatenated String.
		 */
		public static String concatenate(String delimiter, String... elements) {
			return Strings.concatenateUsing(delimiter, elements).toString();
		}

		/**
		 * Appends each {@link String} in the passed {@link Iterable} to the passed {@link StringBuilder}, with the passed delimiter placed betwixt each entry.
		 * <p>
		 * Convenience method which calls {@link #concatenate(String, Iterable)} with a new {@link StringBuilder}.
		 *
		 * @param delimiter - The String to place between each String in {@code elements}
		 * @return The {@link StringBuilder} provided.
		 */
		public static StringBuilder concatenateUsing(String delimiter, Iterable<String> elements) {
			return Strings.concatenateUsing(new StringBuilder(), delimiter, elements);
		}

		/**
		 * Appends the passed {@link String} varargs array to the passed {@link StringBuilder}, with the passed delimiter placed betwixt each entry.
		 * <p>
		 * Convenience method which calls {@link #concatenate(String, String...)} with a new {@link StringBuilder}.
		 *
		 * @param delimiter - The String to place between each String in {@code elements}
		 * @return The {@link StringBuilder} provided.
		 */
		public static StringBuilder concatenateUsing(String delimiter, String... elements) {
			return Strings.concatenateUsing(new StringBuilder(), delimiter, elements);
		}

		/**
		 * Appends each {@link String} in the passed {@link Iterable} to the passed {@link StringBuilder}, with the passed delimiter placed betwixt each entry.
		 *
		 * @param builder - The {@link StringBuilder} instance to use
		 * @param delimiter - The String to place between each String in {@code elements}
		 * @return The {@link StringBuilder} provided.
		 */
		public static StringBuilder concatenateUsing(@Nonnull StringBuilder builder, String delimiter, Iterable<String> elements) {
			Iterator<String> iterator = elements.iterator();
			while (iterator.hasNext()) {
				builder.append(iterator.next());
				if (iterator.hasNext()) {
					builder.append(delimiter);
				}
			}
			return builder;
		}

		/**
		 * Appends the passed {@link String} varargs array to the passed {@link StringBuilder}, with the passed delimiter placed betwixt each entry.
		 *
		 * @param builder - The {@link StringBuilder} instance to use
		 * @param delimiter - The String to place between each String in {@code elements}
		 * @return The {@link StringBuilder} provided.
		 */
		public static StringBuilder concatenateUsing(@Nonnull StringBuilder builder, String delimiter, String... elements) {
			for (int index = 0; index < elements.length; index += 1) {
				builder.append(elements[index]);
				if (index < (elements.length - 1)) {
					builder.append(delimiter);
				}
			}
			return builder;
		}

		/**
		 * Concatentates the passed {@link String}s with {@link #DELIMITER_TYPE} betwixt and {@value #TOOLTIP} as the first element.
		 *
		 * @param elements - The {@link String}s to concatenate
		 * @return A suitably-concatenated {@link String}.
		 */
		public static String tooltip(String... elements) {
			return Strings.concatenateUsing(Strings.createPrependedBuilder(TOOLTIP, DELIMITER_TYPE), DELIMITER_TYPE, elements).toString();
		}

		/**
		 * Concatentates the passed {@link String}s with {@link #DELIMITER_TYPE} betwixt and {@value #TOOLTIP} as the first element.
		 *
		 * @param elements - The {@link String}s to concatenate
		 * @return A suitably-concatenated {@link String}.
		 */
		public static String tooltip(Iterable<String> elements) {
			return Strings.concatenateUsing(Strings.createPrependedBuilder(TOOLTIP, DELIMITER_TYPE), DELIMITER_TYPE, elements).toString();
		}

		/* Internal Methods */

		protected static StringBuilder createPrependedBuilder(String starter, String delimiter) {
			return new StringBuilder().append(starter).append(delimiter);
		}

	}

	/**
	 * Utility class for various UI-related utilities.
	 * <p>
	 * Not all methods in this class are client-only.
	 *
	 * @author Malcolm Riley
	 */
	public static class UI {

		private UI() {}
		
		/**
		 * Generic logic for opening a menu from the block at the indicated position. This method can be safely called from either side
		 * as it checks that the provided {@link Level} and {@link Player} instances are server-sided.
		 * <p>
		 * Intended to be used within {@link Block#use(BlockState, Level, BlockPos, Player, InteractionHand, net.minecraft.world.phys.BlockHitResult)}
		 * as relevant.
		 * 
		 * @param player - The player opening the UI
		 * @param level - A {@link Level} reference
		 * @param position - The {@link BlockPos} of the {@link BlockEntity}
		 * @return A side-suitable and result-suitable {@link InteractionResult}.
		 */
		public static InteractionResult openMenu(Player player, Level level, BlockPos position) {
			return UI.openMenu(player, level, position, null);
		}
		
		/**
		 * Generic logic for opening a menu from the block at the indicated position. This method can be safely called from either side
		 * as it checks that the provided {@link Level} and {@link Player} instances are server-sided.
		 * <p>
		 * Intended to be used within {@link Block#use(BlockState, Level, BlockPos, Player, InteractionHand, net.minecraft.world.phys.BlockHitResult)}
		 * as relevant.
		 * 
		 * @param player - The player opening the UI
		 * @param level - A {@link Level} reference
		 * @param position - The {@link BlockPos} of the {@link BlockEntity}
		 * @param interaction - A {@link ResourceLocation} corresponding to the {@link Stat} award for interacting with the relevant {@link Block}
		 * @return A side-suitable and result-suitable {@link InteractionResult}.
		 */
		public static InteractionResult openMenu(Player player, Level level, BlockPos position, @Nullable ResourceLocation interaction) {
			if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
				final BlockState state = level.getBlockState(position);
				NetworkHooks.openScreen(serverPlayer, state.getMenuProvider(level, position));
				if (Objects.nonNull(interaction)) {
					player.awardStat(interaction);
				}
				return InteractionResult.CONSUME;
			}
			return InteractionResult.sidedSuccess(level.isClientSide());
		}
		
		/**
		 * Creates a {@link MenuProvider} instance from the provided elements. If the {@link BlockEntity} at the indicated position is {@link Nameable}, it will supply the
		 * custom name of that {@link BlockEntity} as per {@link Nameable#getDisplayName()}. If there is no {@link Nameable} at the indicated position,
		 * it will instead supply the name of the {@link Block}.
		 * 
		 * @param constructor - A {@link MenuConstructor} for generating menu instances.
		 * @param level - A {@link Level} reference.
		 * @param position - The {@link BlockPos} of the block
		 * @return A suitable {@link MenuProvider}.
		 */
		public static MenuProvider createNamedMenuProvider(MenuConstructor constructor, Level level, BlockPos position) {
			BlockEntity entity = level.getBlockEntity(position);
			Component title = Objects.nonNull(entity) && entity instanceof Nameable named ? named.getDisplayName() : level.getBlockState(position).getBlock().getName();
			return UI.createMenuProvider(constructor, level, position, title);
		}

		/**
		 * Creates a {@link MenuProvider} instance from the provided elements. If the provided {@link Component} is null, will substitute {@link Component#empty()}.
		 * 
		 * @param constructor - A {@link MenuConstructor} for generating menu instances.
		 * @param level - A {@link Level} reference.
		 * @param position - The {@link BlockPos} of the block
		 * @param title - The {@link Component} to use for the menu title.
		 * @return A suitable {@link MenuProvider}.
		 */
		public static MenuProvider createMenuProvider(MenuConstructor constructor, Level level, BlockPos position, @Nullable Component title) {
			return new SimpleMenuProvider(constructor, Objects.requireNonNullElse(title, Component.empty()));
		}

	}

}
