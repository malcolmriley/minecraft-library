package paragon.minecraft.library.content;

import java.util.function.Supplier;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;

/**
 * Extension of the simple {@link BlockItem} that takes a {@link Supplier} instead of a direct block reference.
 * 
 * @author Malcolm Riley
 */
public class LazyBlockItem extends BlockItem {

	public LazyBlockItem(Supplier<Block> blockSupplier, Properties builder) {
		super(blockSupplier.get(), builder);
	}

}
