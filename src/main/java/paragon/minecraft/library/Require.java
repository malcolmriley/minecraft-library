package paragon.minecraft.library;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Collection of check methods for various parameters. These methods throw {@link IllegalArgumentException} with an optionally-customizable message
 * if their requisite check is not passed. These exceptions are not declared with the method signature; the intention is that a failure of the provided
 * checks be considered terminal. 
 * 
 * @author Malcolm Riley
 */
public final class Require {
	
	private Require() { }
	
	/**
	 * Checks the provided instance against the provided {@link Predicate}. if {@link Predicate#test(Object)} returns {@code FALSE},
	 * an {@link IllegalArgumentException} is thrown.
	 * 
	 * @param <T>
	 * @param instance - The instance to check
	 * @param check - The check to perform
	 * @return The instance if the check passed, else an {@link IllegalArgumentException} is thrown.
	 */
	public static final <T> T check(T instance, Predicate<T> check) {
		return Require.check(instance, check, String.format("Check failed for value \"%s\"", String.valueOf(instance)));
	}

	/**
	 * Checks the provided instance against the provided {@link Predicate}. if {@link Predicate#test(Object)} returns {@code FALSE},
	 * an {@link IllegalArgumentException} is thrown.
	 * 
	 * @param <T>
	 * @param instance - The instance to check
	 * @param check - The check to perform
	 * @param message - The message to supply to the {@link IllegalArgumentException}.
	 * @return The instance if the check passed, else an {@link IllegalArgumentException} is thrown.
	 */
	public static final <T> T check(T instance, Predicate<T> check, String message) {
		return Require.throwIf(instance, !check.test(instance), message);
	}

	/**
	 * Checks the provided instance against the provided {@link Predicate}. if {@link Predicate#test(Object)} returns {@code FALSE},
	 * an {@link IllegalArgumentException} is thrown.
	 * <p>
	 * This variation accepts a {@link Supplier} for the exception message and should be preferred over {@link Require#check(Object, Predicate, String)}
	 * if the composition of the message is particularly heavy performance-wise.
	 * 
	 * @param <T>
	 * @param instance - The instance to check
	 * @param check - The check to perform
	 * @param message - The supplier of the message for the {@link IllegalArgumentException}
	 * @return The instance if the check passed, else an {@link IllegalArgumentException} is thrown.
	 */
	public static final <T> T check(T instance, Predicate<T> check, Supplier<String> messageProvider) {
		return Require.throwIf(instance, !check.test(instance), messageProvider);
	}
	
	/**
	 * Checks whether the provided {@link String} is {@code null} or empty, throwing an {@link IllegalArgumentException} using the provided
	 * message if that is the case.
	 * 
	 * @param instance - The {@link String} to check
	 * @param message - The message to use for the {@link IllegalArgumentException}
	 * @return The provided {@link String} if it is not {@code null} or empty, else an {@link IllegalArgumentException} is thrown.
	 */
	public static final String notNullOrEmpty(final String instance, final String message) {
		return Require.throwIf(instance, Utilities.Strings.isNullOrEmpty(instance), message);
	}
	
	/**
	 * Checks whether the provided {@link Collection} is {@code null} or empty, throwing an {@link IllegalArgumntException} using the provided
	 * message if that is the case.
	 * 
	 * @param <T> The {@link Collection} type
	 * @param collection - The {@link Collection} to examine
	 * @param message - The message to use for the {@link IllegalArgumentException}
	 * @return The provided {@link Collection} if it is not {@code null} or empty, else an {@link IllegalArgumentException} is thrown.
	 */
	public static <T extends Collection<?>> T notNullOrEmpty(T collection, String message) {
		return Require.throwIf(collection, Objects.isNull(collection) || collection.isEmpty(), message);
	}
	
	/**
	 * Throws an exception if the provided value is not positive; that is to say, if it is less than or equal to zero.
	 * <p>
	 * If the provided value is {@code null}, throws an exception.
	 * 
	 * @param <V> The type of value to test
	 * @param value - The value to test
	 * @param message - The message for the {@link IllegalArgumentException} if the value is not positive
	 * @return The provided value.
	 */
	public static <V extends Comparable<Number>> V positive(V value, String message) {
		return Require.throwIfNot(value, Require.isPositive(value, 0), message);
	}
	/**
	 * Throws an exception if the provided value is not non-positive; that is to say, if it is less zero but not if it is equal to zero.
	 * <p>
	 * If the provided value is {@code null}, throws an exception.
	 * 
	 * @param <V> The type of value to test
	 * @param value - The value to test
	 * @param message - The message for the {@link IllegalArgumentException} if the value is not positive
	 * @return The provided value.
	 */
	public static <V extends Comparable<Number>> V nonPositive(V value, String message) {
		return Require.throwIfNot(value, Require.isNonPositive(value, 0), message);
	}
	
	/**
	 * Throws an exception if the provided value is not negative; that is to say, if it is greater than or equal to zero.
	 * <p>
	 * If the provided value is {@code null}, throws an exception.
	 * 
	 * @param <V> The type of value to test
	 * @param value - The value to test
	 * @param message - The message for the {@link IllegalArgumentException} if the value is not positive
	 * @return The provided value.
	 */
	public static <V extends Comparable<Number>> V negative(V value, String message) {
		return Require.throwIfNot(value, Require.isNegative(value, 0), message);
	}
	
	/**
	 * Throws an exception if the provided value is not non-negative; that is to say, if it is less than zero but not equal to zero.
	 * <p>
	 * If the provided value is {@code null}, throws an exception.
	 * 
	 * @param <V> The type of value to test
	 * @param value - The value to test
	 * @param message - The message for the {@link IllegalArgumentException} if the value is not positive
	 * @return The provided value.
	 */
	public static <V extends Comparable<Number>> V nonNegative(V value, String message) {
		return Require.throwIfNot(value, Require.isNonNegative(value, 0), message);
	}
	
	/**
	 * Throws an exception if the provided value is equal to zero.
	 * <p>
	 * If the provided value is {@code null}, throws an exception.
	 * 
	 * @param <V> The type of value to test
	 * @param value - The value to test
	 * @param message - The message for the {@link IllegalArgumentException} if the value is not positive
	 * @return The provided value.
	 */
	public static <V extends Comparable<Number>> V nonZero(V value, String message) {
		return Require.throwIfNot(value, Require.isNonZero(value, 0), message);
	}
	
	/* Internal Methods */
	
	protected static final <Z, T extends Comparable<Z>> boolean isNegative(T value, Z zero) {
		return Objects.nonNull(value) && value.compareTo(zero) < 0;
	}
	
	protected static final <Z, T extends Comparable<Z>> boolean isNonNegative(T value, Z zero) {
		return Objects.nonNull(value) && value.compareTo(zero) >= 0;
	}
	
	protected static final <Z, T extends Comparable<Z>> boolean isPositive(T value, Z zero) {
		return Objects.nonNull(value) && value.compareTo(zero) > 0;
	}
	
	protected static final <Z, T extends Comparable<Z>> boolean isNonPositive(T value, Z zero) {
		return Objects.nonNull(value) && value.compareTo(zero) <= 0;
	}
	
	protected static final <Z, T extends Comparable<Z>> boolean isZero(T value, Z zero) {
		return Objects.nonNull(value) && value.compareTo(zero) == 0;
	}
	
	protected static final <Z, T extends Comparable<Z>> boolean isNonZero(T value, Z zero) {
		return Objects.nonNull(value) && value.compareTo(zero) != 0;
	}
	
	protected static final <T> T throwIfNot(T instance, boolean shouldThrow, Supplier<String> messageProvider) {
		return Require.throwIf(instance, !shouldThrow, messageProvider);
	}
	
	protected static final <T> T throwIf(T instance, boolean shouldThrow, Supplier<String> messageProvider) {
		if (shouldThrow) {
			throw new IllegalArgumentException(messageProvider.get());
		}
		return instance;
	}
	
	protected static final <T> T throwIfNot(T instance, boolean shouldThrow, String message) {
		return Require.throwIf(instance, !shouldThrow, message);
	}
	
	protected static final <T> T throwIf(T instance, boolean shouldThrow, String message) {
		if (shouldThrow) {
			throw new IllegalArgumentException(message);
		}
		return instance;
	}

}
