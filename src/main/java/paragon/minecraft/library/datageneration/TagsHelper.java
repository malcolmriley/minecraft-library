package paragon.minecraft.library.datageneration;

import java.io.IOException;

import javax.annotation.Nullable;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider.TagAppender;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;

/**
 * Combined-duty Item and Block tags provider. Only slightly Frankensteinish!
 * 
 * @author Malcolm Riley
 */
public abstract class TagsHelper implements DataProvider {
	
	/* Internal References */
	protected final ItemTagsHelper ITEM_TAGS;
	protected final BlockTagsHelper BLOCK_TAGS;
	
	public TagsHelper(DataGenerator generator, String modID, @Nullable ExistingFileHelper helper) {
		this.BLOCK_TAGS = new BlockTagsHelper(generator, modID, helper) {
			
			@Override
			protected void addTags() {
				addBlockTags();
			}
			
		};
		this.ITEM_TAGS = new ItemTagsHelper(generator, this.BLOCK_TAGS, modID, helper) {
			
			@Override
			protected void addTags() {
				addItemTags();
			}
			
		};
	}
	
	/* Supertype Override Methods */

	@Override
	public void run(HashCache cache) throws IOException {
		this.BLOCK_TAGS.run(cache);
		this.ITEM_TAGS.run(cache);
	}

	@Override
	public String getName() {
		return "Combined Tags Provider";
	}
	
	/* Abstract Methods */
	
	protected abstract void addItemTags();
	protected abstract void addBlockTags();
	
	/* Internal Methods */
	
	protected TagAppender<Item> itemTag(TagKey<Item> itemTag) {
		return this.ITEM_TAGS.itemTag(itemTag);
	}
	
	protected TagAppender<Block> blockTag(TagKey<Block> blockTag) {
		return this.BLOCK_TAGS.blockTag(blockTag);
	}
	
	/* Helper Implementations */
	
	public static class ItemTagsHelper extends ItemTagsProvider {

		public ItemTagsHelper(DataGenerator generator, BlockTagsProvider blockTags, String modId, @Nullable ExistingFileHelper existingFileHelper) {
			super(generator, blockTags, modId, existingFileHelper);
		}
		
		protected TagAppender<Item> itemTag(TagKey<Item> blockTag) {
			return this.tag(blockTag);
		}
		
	}
	
	public static class BlockTagsHelper extends BlockTagsProvider {

		public BlockTagsHelper(DataGenerator p_126511_, String modId, @Nullable ExistingFileHelper existingFileHelper) {
			super(p_126511_, modId, existingFileHelper);
		}
		
		protected TagAppender<Block> blockTag(TagKey<Block> blockTag) {
			return this.tag(blockTag);
		}
		
	}

}
