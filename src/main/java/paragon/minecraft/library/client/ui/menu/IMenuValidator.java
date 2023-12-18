package paragon.minecraft.library.client.ui.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import paragon.minecraft.library.utilities.Require;

/**
 * Interface providing facility for compartmentalized validation of container menus.
 * <p>
 * Intended to be used instead of, or in conjunction with {@link AbstractContainerMenu#stillValid(Player)}
 * 
 * @author Malcolm Riley
 */
@FunctionalInterface
public interface IMenuValidator {
	
	/* Common Instances */
	public static IMenuValidator ALWAYS_TRUE = (level, player, position) -> true;
	public static IMenuValidator DEFAULT = (level, player, position) -> IMenuValidator.validateDistance(player, position, 64D);
	
	/**
	 * Joins two {@link IMenuValidator} into a single instance, performing logical {@code AND} to evaluate.
	 * <p>
	 * Will throw an {@link IllegalArgumentException} if both instances are the same, or one is {@code null}.
	 * 
	 * @param first - One {@link IMenuValidator}
	 * @param second - Another {@link IMenuValidator}
	 * @return A new {@link IMenuValidator} comprised of {@code first && second}.
	 */
	public static IMenuValidator and(final IMenuValidator first, final IMenuValidator second) {
		Require.notEqualAndNonNull(first, second, "Cannot AND IMenuValidator if both are equal, or if either ar null.");
		return (level, player, position) -> first.isValid(level, player, position) && second.isValid(level, player, position);
	}
	
	/**
	 * Joins two {@link IMenuValidator} into a single instance, performing logical {@code OR} to evaluate.
	 * <p>
	 * Will throw an {@link IllegalArgumentException} if both instances are the same, or one is {@code null}.
	 * 
	 * @param first - One {@link IMenuValidator}
	 * @param second - Another {@link IMenuValidator}
	 * @return A new {@link IMenuValidator} comprised of {@code first || second}.
	 */
	public static IMenuValidator or(final IMenuValidator first, final IMenuValidator second) {
		Require.notEqualAndNonNull(first, second, "Cannot OR IMenuValidator if both are equal, or if either ar null.");
		return (level, player, position) -> first.isValid(level, player, position) || second.isValid(level, player, position);
	}
	
	/**
	 * Creates an {@link IMenuValidator} that will check against the provided distance and {@link Holder} of {@link Block}.
	 * 
	 * @param distanceSquared - The maximum valid distance squared a {@link Player} may be
	 * @param block - The expected {@link Block}
	 * @return A suitable {@link IMenuValidator}.
	 */
	public static IMenuValidator create(final double distanceSquared, final Holder<Block> block) {
		return (level, player, position) -> IMenuValidator.validate(level, player, position, distanceSquared, block);
	}
	
	/**
	 * Shared-logic method for validating the provided parameters against the provided maximum distance squared and expected {@link Block}.
	 * 
	 * @param level - A {@link Level} reference
	 * @param player - The {@link Player} that opened the menu
	 * @param position - The {@link BlockPos} of the {@link Block}
	 * @param distanceSquared - The maximum valid distance squared a {@link Player} may be
	 * @param block - The expected {@link Block}
	 * @return Whether or not the {@link Block} at the provided {@link BlockPos} is as expected, and whether the provided {@link Player} is within the minimum distance squared.
	 */
	public static boolean validate(Level level, Player player, BlockPos position, double distanceSquared, Holder<Block> block) {
		return IMenuValidator.validateBlock(level, position, block) && IMenuValidator.validateDistance(player, position, distanceSquared);
	}
	
	/**
	 * Shared-logic method for validating the provided parameters against the provided maximum distance squared.
	 * 
	 * @param player - The {@link Player} that opened the menu
	 * @param position - The {@link BlockPos} of the {@link Block}
	 * @param distanceSquared - The maximum valid distance squared a {@link Player} may be
	 * @return Whether or not the provided {@link Player} is within the minimum distance squared.
	 */
	public static boolean validateDistance(Player player, BlockPos position, double distanceSquared) {
		return player.distanceToSqr(position.getCenter()) < distanceSquared;
	}
	
	/**
	 * Shared-logic method for validating whether the {@link Block} at the provided {@link BlockPos} matches the expected in the provided {@link Holder}.
	 * 
	 * @param level - A {@link Level} reference
	 * @param position - The {@link BlockPos} of the {@link Block}
	 * @param expected - The expected {@link Block}
	 * @return Whether or not the {@link Block} at the provided {@link BlockPos} is as expected.
	 */
	public static boolean validateBlock(Level level, BlockPos position, Holder<Block> expected) {
		return level.getBlockState(position).is(expected);
	}
	
	/**
	 * This method should return {@code TRUE} if the menu is still considered valid based on the provided parameters, and {@code FALSE} otherwise.
	 * 
	 * @param level - A {@link Level} reference
	 * @param player - The {@link Player} that opened the menu
	 * @param position - The {@link BlockPos} where the menu originated
	 * @return Whether the menu should be considered valid.
	 */
	public boolean isValid(Level level, Player player, BlockPos position);
	
}