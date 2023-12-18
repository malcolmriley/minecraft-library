package paragon.minecraft.library.client.ui.slots;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Class for managing a number of {@link Slot} as a conceptually-cohesive group.
 * <p>
 * Useful for grouping related {@link Slot} together, e.g. a player inventory or hotbar, or an input or output cluster within a {@link Container}.
 *
 * @author Malcolm Riley
 */
public class SlotGroup {

	/* Internal Fields */
	protected final Map<Integer, Slot> SLOTS;
	protected final int minX;
	protected final int minY;
	protected final int maxX;
	protected final int maxY;
	protected final int minIndex;
	protected final int maxIndex;
	protected final boolean isContiguous;

	private SlotGroup(Map<Integer, Slot> slotMap, int minX, int minY, int maxX, int maxY, int minIndex, int maxIndex, boolean contiguous) {
		this.SLOTS = Collections.unmodifiableMap(slotMap);
		this.minX = minX;
		this.minY = minY;
		this.maxX = maxX;
		this.maxY = maxY;
		this.minIndex = minIndex;
		this.maxIndex = maxIndex;
		this.isContiguous = contiguous;
	}

	/* Public Methods */

	/**
	 * Returns whether any of the {@link Slot} managed by this {@link SlotGroup} contain any {@link ItemStack}.
	 *
	 * @return Whether any {@link Slot} managed by this {@link SlotGroup} contain any {@link ItemStack}.
	 */
	public boolean hasAnyItemStacks() {
		return this.streamSlots().anyMatch(slot -> slot.hasItem());
	}

	/**
	 * Returns whether the span of indices belonging to {@link Slot} held by this {@link SlotGroup} is contiguous.
	 *
	 * @return {@code TRUE} if all index values between {@link #getMinIndex()} and {@link #getMaxIndex()} (inclusive both) are held by this {@link SlotGroup}, and {@code FALSE} if there exist one or more gaps.
	 */
	public boolean isContiguous() {
		return this.isContiguous;
	}

	/**
	 * Returns the maximum {@link Slot} index held by this {@link SlotGroup}.
	 *
	 * @return The maximum index among {@link Slot} held by this {@link SlotGroup}.
	 */
	public int getMaxIndex() {
		return this.maxIndex;
	}

	/**
	 * Returns the minimum {@link Slot} index held by this {@link SlotGroup}.
	 *
	 * @return The minimum index among {@link Slot} held by this {@link SlotGroup} if any.
	 */
	public int getMinIndex() {
		return this.minIndex;
	}

	/**
	 * Returns the minimum x-position of the {@link Slot}s belonging to this {@link SlotGroup}.
	 *
	 * @return The minimum x-position if any {@link Slot} belong to this {@link SlotGroup}.
	 */
	public int getMinX() {
		return this.minX;
	}

	/**
	 * Returns the maximum x-position of the {@link Slot}s belonging to this {@link SlotGroup}.
	 *
	 * @return The maximum x-position if any {@link Slot} belong to this {@link SlotGroup}.
	 */
	public int getMaxX() {
		return this.getMaxX();
	}

	/**
	 * Returns the minimum y-position of the {@link Slot}s belonging to this {@link SlotGroup}.
	 *
	 * @return The minimum y-position if any {@link Slot} belong to this {@link SlotGroup}.
	 */
	public int getMinY() {
		return this.minY;
	}

	/**
	 * Returns the maximum y-position of the {@link Slot}s belonging to this {@link SlotGroup}.
	 *
	 * @return The maximum y-position if any {@link Slot} belong to this {@link SlotGroup}.
	 */
	public int getMaxY() {
		return this.getMaxY();
	}

	/**
	 * Returns the difference between the minimum and maximum x-position values of {@link Slot} belonging to this {@link SlotGroup}.
	 *
	 * @return The difference between the minimum and maximum x-position if any {@link Slot} belong to this {@link SlotGroup}.
	 */
	public int getWidth() {
		return this.maxX - this.minX;
	}

	/**
	 * Returns the difference between the minimum and maximum y-position values of {@link Slot} belonging to this {@link SlotGroup}.
	 *
	 * @return The difference between the minimum and maximum y-position if any {@link Slot} belong to this {@link SlotGroup}.
	 */
	public int getHeight() {
		return this.maxY - this.minY;
	}

	/**
	 * Returns whether this {@link SlotGroup} is managing the provided index.
	 *
	 * @param index - The index of the {@link Slot}
	 * @return Whether that index is managed by this {@link SlotGroup}
	 */
	public boolean holdsSlot(int index) {
		return this.SLOTS.containsKey(index);
	}

	/**
	 * Returns the {@link Slot} keyed to the indicated index.
	 *
	 * @param index - The index of the {@link Slot} to retrieve
	 * @return The {@link Slot} corresponding to that index, or {@code null} if no such {@link Slot} is managed by this {@link SlotGroup}.
	 */
	public Slot getSlot(int index) {
		return this.SLOTS.get(index);
	}

	/**
	 * Convenience method to return the {@link ItemStack} held in the {@link Slot} indicated by the provided index.
	 * <p>
	 * This method does not check whether or not the provided index is actually held by this {@link SlotGroup}, so callers
	 * are recommended to invoke {@link #holdsSlot(int)} beforehand.
	 *
	 * @param index - The index of the {@link Slot} containing the desired {@link ItemStack}
	 * @return The {@link ItemStack} contained by the {@link Slot} at the provided index.
	 */
	public ItemStack getStack(int index) {
		return this.SLOTS.get(index).getItem();
	}

	/**
	 * Returns an {@link Iterable} over the indices of all {@link Slot} managed by this {@link SlotGroup}.
	 *
	 * @return An {@link Iterable} over all {@link Slot} indices.
	 */
	public Iterable<Integer> getIndices() {
		return this.SLOTS.keySet();
	}

	/**
	 * Returns an {@link Iterable} over all {@link Slot} managed by this {@link SlotGroup}.
	 *
	 * @return An {@link Iterable} over all {@link Slot}.
	 */
	public Iterable<Slot> getSlots() {
		return this.SLOTS.values();
	}

	/**
	 * Returns an {@link Stream} over the indices of all {@link Slot} managed by this {@link SlotGroup}.
	 *
	 * @return An {@link Stream} over all {@link Slot} indices.
	 */
	public Stream<Integer> streamIndices() {
		return this.SLOTS.keySet().stream();
	}

	/**
	 * Returns an {@link Stream} over all {@link Slot} managed by this {@link SlotGroup}.
	 *
	 * @return An {@link Stream} over all {@link Slot}.
	 */
	public Stream<Slot> streamSlots() {
		return this.SLOTS.values().stream();
	}

	/**
	 * Performs an action for each slot in this {@link SlotGroup}.
	 *
	 * @param action
	 */
	public void forEach(Consumer<Slot> action) {
		this.SLOTS.values().stream().forEach(action);
	}

	/**
	 * Performs an action upon all elements of this {@link SlotGroup} that have an {@link ItemStack} present.
	 * <p>
	 * Empty {@link ItemStack} are not considered for this action.
	 *
	 * @param action - The action to perform
	 */
	public void forEachStack(Consumer<ItemStack> action) {
		this.SLOTS.values().stream().filter(slot -> slot.hasItem()).forEach(slot -> action.accept(slot.getItem()));
	}

	/* Builder Implementation */

	/**
	 * Builder class for the {@link SlotGroup} object.
	 */
	public static final class Builder {

		/* Internal Fields */
		protected final Map<Integer, Slot> SLOTS;

		/* Constants */
		private static final String EXCEPTION_DUPLICATE_NUMBER = "SlotGroup already contains Slot with provided number.";
		private static final String EXCEPTION_EMPTY_GROUP = "Cannot create an empty SlotGroup.";
		private static final String EXCEPTION_NULL_SLOT = "Cannot add null Slot to SlotGroup.";

		private Builder(Map<Integer, Slot> slotMap) {
			this.SLOTS = slotMap;
		}

		private Builder() {
			this(new HashMap<>());
		}

		private Builder(int size) {
			this(new HashMap<>(size));
		}

		/**
		 * Creates a new {@link SlotGroup.Builder} with an unknown size.
		 * <p>
		 * The size of the {@link SlotGroup.Builder} is not immutable, but the size of the ultimately-resulting {@link SlotGroup} is.
		 * <p>
		 * Prefer use of {@link #create(int)} if the size is known beforehand.
		 *
		 * @see #create(int)
		 * @return {@code this}, for method chaining.
		 */
		public static Builder create() {
			return new Builder();
		}

		/**
		 * Creates a new {@link SlotGroup.Builder} with the provided initial size.
		 * <p>
		 * The size of the {@link SlotGroup.Builder} is not immutable, but the size of the ultimately-resulting {@link SlotGroup} is.
		 *
		 * @param size - The size, in quantity of {@link Slot}, that the {@link SlotGroup} will ultimately manage.
		 * @return {@code this}, for method chaining.
		 */
		public static Builder create(int size) {
			return new Builder(size);
		}

		/**
		 * This will add the provided {@link Slot} to the {@link SlotGroup} that will ultimately be constructed by this {@link SlotGroup.Builder}.
		 * <p>
		 * Callers should ensure that the {@link Slot} has been added to a container before calling this method, as that is how the {@link Slot} obtains
		 * its index within the inventory it manages.
		 *
		 * @param slot - The {@link Slot} to add
		 * @return {@code this}, for method chaining.
		 */
		public Builder add(Slot slot) {
			final int newIndex = slot.index;
			if (this.SLOTS.containsKey(newIndex)) {
				throw new IllegalArgumentException(EXCEPTION_DUPLICATE_NUMBER);
			}
			this.SLOTS.put(slot.index, Objects.requireNonNull(slot, EXCEPTION_NULL_SLOT));
			return this;
		}

		/**
		 * This will add all provided {@link Slot} to the {@link SlotGroup} that will ultimately be constructed by this {@link SlotGroup.Builder}.
		 * <p>
		 * Callers should ensure that the provided {@link Slot}s have been added to a container before calling this method, as that is how {@link Slot} obtains
		 * their index within the inventory it manages.
		 *
		 * @param slots - The {@link Slot}s to add
		 * @return {@code this}, for method chaining.
		 */
		public Builder add(Slot... slots) {
			for (Slot iterated : slots) {
				this.add(iterated);
			}
			return this;
		}

		/**
		 * Constructs and returns the {@link SlotGroup} being built by this {@link SlotGroup.Builder}.
		 *
		 * @return The {@link SlotGroup} constructed by this {@link SlotGroup.Builder}.
		 */
		public SlotGroup build() {
			if (this.SLOTS.isEmpty()) {
				throw new IllegalStateException(EXCEPTION_EMPTY_GROUP);
			}
			// Determine Min and Max Values
			int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minIndex = Integer.MAX_VALUE;
			int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxIndex = Integer.MIN_VALUE;
			
			for(Slot slot : this.SLOTS.values()) {
				// Positioning
				minX = Math.min(minX, slot.x);
				maxX = Math.max(maxX, slot.x);
				minY = Math.min(minY, slot.y);
				maxY = Math.max(maxY, slot.y);
				
				// Lookup Indices
				minIndex = Math.min(minIndex, slot.index);
				maxIndex = Math.max(maxIndex, slot.index);
			}
			
			// Determine Continuity
			boolean contiguous = true;
			for (int index = minIndex; index <= maxIndex; index += 1) {
				if (!this.SLOTS.containsKey(index)) {
					contiguous = false;
					break;
				}
			}
			
			return new SlotGroup(this.SLOTS, minX, minY, maxX, maxY, minIndex, maxIndex, contiguous);
		}

	}

}
