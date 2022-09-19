package paragon.minecraft.library.content;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;

/**
 * Simple fuel item.
 *
 * @author Malcolm Riley
 */
public class ItemSimpleFuel extends Item {

	/* Internal Fields */
	private final int BURN_TIME;

	public ItemSimpleFuel(Properties properties, int burnTime) {
		super(properties);
		this.BURN_TIME = burnTime;
	}

	@Override
	public int getBurnTime(ItemStack passedStack, RecipeType<?> type) {
		return this.BURN_TIME;
	}

}
