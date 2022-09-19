package paragon.minecraft.library;

import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

/**
 * Why deliberately make something both lazy and weak? To hold a weak reference to a lazily-instantiated object, of course!
 * <p>
 * Also contains some {@link Optional}-like convenience methods.
 * 
 * @author Malcolm Riley
 * @param <T> The type of the contained reference
 */
public class LazyWeakReference<T> implements Supplier<T> {
	
	/* Internal References */
	protected final Supplier<T> INITIALIZER;
	protected WeakReference<T> REFERENCE;
	
	/**
	 * The provided {@link Supplier} should initialize or provide a non-{@code null} {@link T} instance.
	 * 
	 * @param initializer - An initializer or provider for the type in question.
	 */
	public LazyWeakReference(@Nonnull Supplier<T> initializer) {
		this.INITIALIZER = Objects.requireNonNull(initializer, "Initializer for LazyWeakSuppliers cannot be null!");
		this.REFERENCE = new WeakReference<>(null);
	}
	
	/* Public Methods */
	
	/**
	 * Deliberately nullifies the internal reference, meaning the initializer will be invoked the next time {@link #get()} is called.
	 */
	public void purge() {
		this.REFERENCE = new WeakReference<>(null);
	}

	/**
	 * Returns {@code TRUE} if this {@link LazyWeakReference} holds a {@code null} reference, and {@code FALSE} otherwise.
	 * 
	 * @return Whether this {@link LazyWeakReference} holds a {@code null} reference.
	 * @see #nonNull()
	 */
	public boolean isNull() {
		return Objects.isNull(this.REFERENCE.get());
	}
	
	/**
	 * Returns {@code TRUE} if this {@link LazyWeakReference} holds a non-{@code null} reference, and {@code FALSE} otherwise.
	 * 
	 * @return Whether this {@link LazyWeakReference} holds a non-{@code null} reference.
	 * @see #isNull()
	 */
	public boolean nonNull() {
		return Objects.nonNull(this.REFERENCE.get());
	}
	
	/**
	 * If this {@link LazyWeakReference} currently holds a non-null reference, returns it. Otherwise, returns {@code null}.
	 * <p>
	 * This method does not perform any instantiations if no reference is currently held.
	 * 
	 * @return The currently held reference if non-{@code null}, or else {@code null}.
	 * @see #orElse(Object)
	 */
	public T orElseNull() {
		return this.orElse(null);
	}
	
	/**
	 * If this {@link LazyWeakReference} currently holds a non-{@code null} reference, returns it. Otherwise, returns the provided reference.
	 * <p>
	 * This method does not perform any instantiations if no reference is currently held.
	 * 
	 * @param element - The element to return if no non-{@code null} reference is currently held.
	 * @return The currently held reference if non-{@code null}, or else the provided reference.
	 * @see #orElseNull()
	 */
	public T orElse(final T element) {
		return this.nonNull() ? this.get() : element;
	}

	/* Supplier compliance methods */
	
	/**
	 * If the reference held by this {@link LazyWeakReference}
	 */
	@Override
	public T get() {
		T value = this.REFERENCE.get();
		if (Objects.isNull(value)) {
			value = this.INITIALIZER.get();
			this.REFERENCE = new WeakReference<>(value);
		}
		return value;
	}

}
