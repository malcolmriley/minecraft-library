package paragon.minecraft.library.datageneration;

import net.minecraft.data.DataGenerator;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.ModelFile.UncheckedModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import paragon.minecraft.library.utilities.Utilities;


/**
 * Data-Generator class for generating item models.
 * <p>
 * Contains some useful utility methods that the base class does not.
 *
 * @author Malcolm Riley
 */
public abstract class ItemModelHelper extends ItemModelProvider {

	/* Internal Fields */
	protected static final String TEXTURE_DEFAULT = "layer0";
	protected static final String ITEM_PREFIX = ITEM_FOLDER + Utilities.Strings.DELIMITER_PATH;
	protected static final String BLOCK_PREFIX = BLOCK_FOLDER + Utilities.Strings.DELIMITER_PATH;

	protected static final ModelFile GENERATED = new UncheckedModelFile(ITEM_PREFIX + "generated");
	
	public ItemModelHelper(DataGenerator generator, String modid, ExistingFileHelper existingFileHelper) {
		super(generator, modid, existingFileHelper);
	}

	/* Internal Methods */

	/**
	 * Convenience method for creating a simple block-parented {@link Item} model.
	 * <p>
	 * The {@link ItemModelHelper} will look in the default location of the mod block model assets folder for
	 * a block model with the same name as the passed {@link String}.
	 * 
	 * @param name - The name of the parent block model.
	 */
	protected void blockItem(String name) {
		this.blockItem(name, name);
	}

	/**
	 * Convenience method for creating a simple block-parented {@link Item} model.
	 * <p>
	 * The {@link ItemModelHelper} will look in the default location of the mod block model assets folder for
	 * a block model with the provided parent name.
	 * 
	 * @param name - The name of the item
	 * @param parentName - The name of the parent block model.
	 */
	protected void blockItem(String name, String parentName) {
		this.getBuilder(name).parent(this.getExistingFile(this.modLoc(BLOCK_PREFIX + parentName)));
	}
	
	/**
	 * Convenience method for creating a simple, single-texture {@link Item}.
	 * <p>
	 * The {@link ItemModelHelper} will look in the default location of the mod item texture assets
	 * for a texture of the same name as the passed {@link String}.
	 * 
	 * @param name - The name of the {@link Item} and texture.
	 */
	protected void simpleItem(String name) {
		this.simpleItem(name, name);
	}
	
	/**
	 * Convenience method for creating a simple, single-texture {@link Item}.
	 * <p>
	 * The {@link ItemModelHelper} will look in the default location of the mod item texture assets
	 * for a texture of the same name as the passed {@link String}.
	 * 
	 * @param name - The name of the {@link Item}
	 * @param texture - The name of the texture.
	 */
	protected void simpleItem(String name, String texture) {
		this.getBuilder(name).parent(GENERATED).texture(TEXTURE_DEFAULT, this.modLoc(ITEM_PREFIX + texture));
	}

	/**
	 * Creates a model for a Stair {@link BlockItem} wherein the parent model location is the concatenation of the provided name suffixed with {@code "1"}.
	 * 
	 * @param parentBlock - The name of the parent model file
	 */
	protected void variantBlockItem(String parentBlock) {
		this.variantBlockItem(parentBlock, "1");
	}

	/**
	 * Creates a model for a Slab {@link BlockItem} wherein the parent model location is the concatenation of the provided name suffixed with {@code "bottom_1"}.
	 * <p>
	 * This will be the suffix of the "normal" slab model if utilizing the standard model generation.
	 * 
	 * @param parentBlock - The name of the parent model file
	 */
	protected void variantSlab(String parentBlock) {
		this.variantBlockItem(parentBlock, "bottom_1");
	}

	/**
	 * Creates a model for a Stair {@link BlockItem} wherein the parent model location is the concatenation of the provided name suffixed with {@code "normal_1"}.
	 * <p>
	 * This will be the suffix of the "normal" stair model if utilizing the standard model generation.
	 * 
	 * @param parentBlock - The name of the parent model file
	 */
	protected void variantStair(String parentBlock) {
		this.variantBlockItem(parentBlock, "normal_1");
	}

	/**
	 * Creates a model for a {@link BlockItem} wherein the parent model location is the concatenation of the provided name with the provided variant {@link String}, with {@value Utilities.Strings#DELIMITER_NAME} betwixt.
	 * 
	 * @param parentBlock - The name of the parent model file
	 * @param variant - The model file "variant" suffix.
	 */
	protected void variantBlockItem(String parentBlock, String variant) {
		this.blockItem(parentBlock, Utilities.Strings.name(parentBlock, variant));
	}

}
