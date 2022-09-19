package paragon.minecraft.library.datageneration;

import java.util.function.Supplier;

import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;
import paragon.minecraft.library.Utilities;

/**
 * Data-Generator class for generating blockstate files and block models.
 * <p>
 * @author Malcolm Riley
 */
public abstract class BlockStateHelper extends BlockStateProvider {
	
	public BlockStateHelper(DataGenerator generator, String modid, ExistingFileHelper helper) {
		super(generator, modid, helper);
		// TODO Auto-generated constructor stub
	}
	
	/* Internal Methods */

	/**
	 * Generates {@code quantity} texture-varying-only cube model files for the provided {@link Block}.
	 * <p>
	 * Calling this method expects the existence of textures of the format {@code path_N} where {@code path} is the value of {@link ResourceLocation#getPath()} on the provided {@link Block}'s registry name,
	 * and {@code N} is the variant ordinal ranging from 1 to the passed value (inclusive).
	 * <p>
	 * 
	 * @param baseBlock - The {@link Block} for which to add the texture variation
	 * @param quantity - The quantity of texture variations
	 * @return An array of cube {@link ConfiguredModel} with 
	 */
	protected ConfiguredModel[] cubesWithTextureVariants(Block baseBlock, int quantity) {
		ConfiguredModel[] variants = new ConfiguredModel[quantity];
		ResourceLocation name = baseBlock.getRegistryName();
		for (int index = 1; index <= quantity; index += 1) {
			final String variantName = this.blockFolderTexture(name.getPath() + "_" + index);
			ConfiguredModel model = ConfiguredModel.builder().modelFile(this.models().cubeAll(variantName, new ResourceLocation(name.getNamespace(), variantName))).buildLast();
			variants[index - 1] = model;
		}
		return variants;
	}
	
	/**
	 * This method merely returns the passed {@link String} prefixed with the name of the {@link Block} texture directory and the path delimiter.
	 * <p>
	 * The formula is: {@value ModelProvider#BLOCK_FOLDER} + {@value Utilities.Strings#DELIMITER_PATH} + {@code path}.
	 * @param path - The rest of the texture path to append
	 * @return A {@link ResourceLocation}-friendly path within the {@link Block} textures subdirectory.
	 */
	protected String blockFolderTexture(String path) {
		return ModelProvider.BLOCK_FOLDER + Utilities.Strings.DELIMITER_PATH + path;
	}
	
	/**
	 * Convenience method that simply invokes {@link Supplier#get()}, and then {@link #simpleBlock(Block)} using the result.
	 * <p>
	 * Intended for use with {@link RegistryObject} references.
	 * 
	 * @param blockSupplier - The {@link Supplier} to query
	 */
	protected void simpleBlock(Supplier<Block> blockSupplier) {
		this.simpleBlock(blockSupplier.get());
	}

}
