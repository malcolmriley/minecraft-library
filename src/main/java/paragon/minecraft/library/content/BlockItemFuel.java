package paragon.minecraft.library.content;

import java.util.function.Supplier;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;

/**
 * Simple fuel {@link BlockItem}.
 *
 * @author Malcolm Riley
 */
public class BlockItemFuel extends BlockItem {

	/* Internal Fields */
	protected final int BURN_TIME;

	public BlockItemFuel(Block block, Properties properties, int burnTime) {
		super(block, properties);
		this.BURN_TIME = burnTime;
	}

	public BlockItemFuel(Supplier<Block> block, Properties properties, int burnTime) {
		this(block.get(), properties, burnTime);
	}

	@Override
	public int getBurnTime(ItemStack stack, RecipeType<?> recipeType) {
		return this.BURN_TIME;
	}

}
