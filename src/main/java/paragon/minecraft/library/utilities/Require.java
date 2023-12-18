package paragon.minecraft.library.utilities;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
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

	private Require() {}

	protected static final String DEFAULT_MESSAGE = "Check failed for value \"%s\"";

	/**
	 * Checks the value inside the provided {@link Optional} using the provided {@link Predicate} if empty {@link Optional} are not allowed.
	 * <p>
	 * Throws an {@link IllegalArgumentException} under the following circumstances:
	 * <li>The provided {@link Optional} is {@code null}</li>
	 * <li>The provided {@link Optional} is empty and empty {@link Optional} are not allowed</li>
	 * <li>The provided {@link Optional} is not empty and the value contained therein failed the provided {@link Predicate} test.</li>
	 *
	 * @param <T> The type contained within the provided {@link Optional}.
	 * @param instance - The {@link Optional} to check
	 * @param check - The test to perform if the {@link Optional} is not empty
	 * @param allowEmpty - Whether an exception should be thrown if the {@link Optional} is empty
	 * @return The provided {@link Optional}.
	 */
	public static final <T> Optional<T> check(Optional<T> instance, Predicate<T> check, boolean allowEmpty) {
		return Require.check(instance, check, allowEmpty, Require.getDefaultMessage(instance));
	}

	/**
	 * Checks the value inside the provided {@link Optional} using the provided {@link Predicate} if empty {@link Optional} are not allowed.
	 * <p>
	 * Throws an {@link IllegalArgumentException} under the following circumstances:
	 * <li>The provided {@link Optional} is {@code null}</li>
	 * <li>The provided {@link Optional} is empty and empty {@link Optional} are not allowed</li>
	 * <li>The provided {@link Optional} is not empty and the value contained therein failed the provided {@link Predicate} test.</li>
	 *
	 * @param <T> The type contained within the provided {@link Optional}.
	 * @param instance - The {@link Optional} to check
	 * @param check - The test to perform if the {@link Optional} is not empty
	 * @param allowEmpty - Whether an exception should be thrown if the {@link Optional} is empty
	 * @param message - The message for the thrown exception
	 * @return The provided {@link Optional}.
	 */
	public static final <T> Optional<T> check(Optional<T> instance, Predicate<T> check, boolean allowEmpty, String message) {
		return Require.throwIfNot(instance, Require.checkInsideOptional(instance, check, allowEmpty), message);
	}

	/**
	 * Checks the value inside the provided {@link Optional} using the provided {@link Predicate} if empty {@link Optional} are not allowed.
	 * <p>
	 * Throws an {@link IllegalArgumentException} under the following circumstances:
	 * <li>The provided {@link Optional} is {@code null}</li>
	 * <li>The provided {@link Optional} is empty and empty {@link Optional} are not allowed</li>
	 * <li>The provided {@link Optional} is not empty and the value contained therein failed the provided {@link Predicate} test.</li>
	 *
	 * @param <T> The type contained within the provided {@link Optional}.
	 * @param instance - The {@link Optional} to check
	 * @param check - The test to perform if the {@link Optional} is not empty
	 * @param allowEmpty - Whether an exception should be thrown if the {@link Optional} is empty
	 * @param messageProvider - A {@link Supplier} for the message for the thrown exception
	 * @return The provided {@link Optional}.
	 */
	public static final <T> Optional<T> check(Optional<T> instance, Predicate<T> check, boolean allowEmpty, Supplier<String> messageProvider) {
		return Require.throwIfNot(instance, Require.checkInsideOptional(instance, check, allowEmpty), messageProvider);
	}

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
		return Require.check(instance, check, Require.getDefaultMessage(instance));
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
		return Require.throwIfNot(instance, check.test(instance), message);
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
		return Require.throwIfNot(instance, check.test(instance), messageProvider);
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
		return Require.throwIf(collection, Utilities.Misc.isNullOrEmpty(collection), message);
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
	public static float positive(float value, String message) {
		return Require.throwIfNot(value, value > 0, message);
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
	public static float nonPositive(float value, String message) {
		return Require.throwIfNot(value, value <= 0, message);
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
	public static float negative(float value, String message) {
		return Require.throwIfNot(value, value < 0, message);
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
	public static float nonNegative(float value, String message) {
		return Require.throwIfNot(value, value >= 0, message);
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
	public static float nonZero(float value, String message) {
		return Require.throwIf(value, value == 0, message);
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
	public static int positive(int value, String message) {
		return Require.throwIfNot(value, value > 0, message);
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
	public static int nonPositive(int value, String message) {
		return Require.throwIfNot(value, value <= 0, message);
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
	public static int negative(int value, String message) {
		return Require.throwIfNot(value, value < 0, message);
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
	public static int nonNegative(int value, String message) {
		return Require.throwIfNot(value, value >= 0, message);
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
	public static int nonZero(int value, String message) {
		return Require.throwIf(value, value == 0, message);
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
	public static double positive(double value, String message) {
		return Require.throwIfNot(value, value > 0, message);
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
	public static double nonPositive(double value, String message) {
		return Require.throwIfNot(value, value <= 0, message);
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
	public static double negative(double value, String message) {
		return Require.throwIfNot(value, value < 0, message);
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
	public static double nonNegative(double value, String message) {
		return Require.throwIfNot(value, value >= 0, message);
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
	public static double nonZero(double value, String message) {
		return Require.throwIf(value, value == 0, message);
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
	public static long positive(long value, String message) {
		return Require.throwIfNot(value, value > 0, message);
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
	public static long nonPositive(long value, String message) {
		return Require.throwIfNot(value, value <= 0, message);
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
	public static long negative(long value, String message) {
		return Require.throwIfNot(value, value < 0, message);
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
	public static long nonNegative(long value, String message) {
		return Require.throwIfNot(value, value >= 0, message);
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
	public static long nonZero(long value, String message) {
		return Require.throwIf(value, value == 0, message);
	}

	/* Internal Methods */

	protected static final String getDefaultMessage(Object argument) {
		return String.format(Require.DEFAULT_MESSAGE, String.valueOf(argument));
	}

	protected static <T> boolean checkInsideOptional(Optional<T> optional, Predicate<T> predicate, boolean allowEmpty) {
		return (Objects.nonNull(optional) && (optional.isEmpty() && allowEmpty)) || predicate.test(optional.get());
	}

	protected static final <Z, T extends Comparable<? super Z>> boolean isNegative(T value, Z zero) {
		return Objects.nonNull(value) && (value.compareTo(zero) < 0);
	}

	protected static final <Z, T extends Comparable<? super Z>> boolean isNonNegative(T value, Z zero) {
		return Objects.nonNull(value) && (value.compareTo(zero) >= 0);
	}

	protected static final <Z, T extends Comparable<? super Z>> boolean isPositive(T value, Z zero) {
		return Objects.nonNull(value) && (value.compareTo(zero) > 0);
	}

	protected static final <Z, T extends Comparable<? super Z>> boolean isNonPositive(T value, Z zero) {
		return Objects.nonNull(value) && (value.compareTo(zero) <= 0);
	}

	protected static final <Z, T extends Comparable<? super Z>> boolean isZero(T value, Z zero) {
		return Objects.nonNull(value) && (value.compareTo(zero) == 0);
	}

	protected static final <Z, T extends Comparable<? super Z>> boolean isNonZero(T value, Z zero) {
		return Objects.nonNull(value) && (value.compareTo(zero) != 0);
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
