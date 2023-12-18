package paragon.minecraft.library.utilities;

import java.util.Locale;

import javax.annotation.Nullable;

import net.minecraft.util.Mth;

/**
 * Interface specifying a rounding method - i.e. what to do when converting the value 0.5 to an integer.
 */
@FunctionalInterface
public interface IRoundingMethod {
	
	/**
	 * Perform the rounding operation on the provided value.
	 * 
	 * @param value - The value to round
	 * @return A suitably-rounded integer value.
	 */
	public int round(double value);
	
	/**
	 * Collection of common rounding methods.
	 */
	public static enum RoundingMethod implements IRoundingMethod {
		
		FLOOR(Mth::floor),
		CEILING(Mth::ceil),
		ROUND_UP(value -> Math.round((float)value)),
		ROUND_DOWN(value -> value > 0.5 ? 1 : 0);
		;
		
		private IRoundingMethod operator;
		
		private RoundingMethod(IRoundingMethod function) {
			this.operator = function;
		}
		
		@Override
		public int round(double value) {
			return this.operator.round(value);
		}
		
		/**
		 * Performs a reverse by-name lookup to a {@link RoundingMethod} using the provided {@link String}.
		 * 
		 * @param name - The name of the {@link RoundingMethod} to look up
		 * @return The corresponding {@link RoundingMethod} if any, else {@code null}.
		 */
		public static @Nullable IRoundingMethod byName(String name) {
			switch(name.toLowerCase(Locale.US)) {
				case "floor" : return RoundingMethod.FLOOR;
				case "ceiling": return RoundingMethod.CEILING;
				case "round_up": return RoundingMethod.ROUND_UP;
				case "round_down": return RoundingMethod.ROUND_DOWN;
			}
			return null;
		}
		
	}
	
}