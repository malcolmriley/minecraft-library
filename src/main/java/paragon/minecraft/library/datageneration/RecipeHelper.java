package paragon.minecraft.library.datageneration;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.InventoryChangeTrigger.TriggerInstance;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryObject;
import paragon.minecraft.library.Utilities;

/**
 * Data-Generator class for generating {@link FinishedRecipe}.
 * <p>
 * Contains some useful utility methods that the base class does not.
 *
 * @author Malcolm Riley
 */
public abstract class RecipeHelper extends RecipeProvider {

	/* Internal Fields */
	protected static final String CRITERION_PREFIX = "has_";

	public RecipeHelper(DataGenerator generator) {
		super(generator);
	}

	@Override
	public abstract void buildCraftingRecipes(final Consumer<FinishedRecipe> registrar);

	/* Utility Methods */
	
	/**
	 * Convenience method to create a {@link TriggerInstance} that requires the player to have all of the provided {@link ItemLike} in their inventory at once.
	 * <p>
	 * For a similar method that requires the player to have at least one {@link ItemLike} from a list of such, see {@link #hasAny(ItemLike...)}.
	 * 
	 * @param ingredients - The items required
	 * @return A suitable {@link TriggerInstance}.
	 * @see #hasAny(ItemLike...)
	 */
	protected static TriggerInstance hasAll(ItemLike ... ingredients) {
		final ItemPredicate[] predicates = Stream.of(ingredients).map(item -> ItemPredicate.Builder.item().of(item).build()).toArray(ItemPredicate[]::new);
		return RecipeHelper.inventoryTrigger(predicates);
	}
	
	/**
	 * Convenience method to create a {@link TriggerInstance} that requires the player to have at least one of the provided {@link ItemLike} in their inventory at once.
	 * <p>
	 * For a similar method that requires the player to have all {@link ItemLike} in a list of such, see {@link #hasAll(ItemLike...)}.
	 * 
	 * @param ingredients - The items required
	 * @return A suitable {@link TriggerInstance}.
	 * @see #hasAll(ItemLike...)
	 */
	protected static TriggerInstance hasAny(ItemLike ... ingredients) {
		return RecipeHelper.inventoryTrigger(ItemPredicate.Builder.item().of(ingredients).build());
	}

	/**
	 * Convenience method to create an {@link InventoryChangeTrigger.Instance} for the possesion of the {@link Item} contained in the provided {@link RegistryObject}.
	 * <p>
	 * Simply calls {@link RegistryObject#get()} and provides the result to {@link RecipeHelper#hasItem(ItemLike)}.
	 * 
	 * @param registryObject - The {@link RegistryObject} containing the desired {@link Item}
	 * @return A suitable {@link InventoryChangeTrigger.Instance}
	 */
	protected static TriggerInstance hasItem(RegistryObject<Item> registryObject) {
		return RecipeHelper.has(registryObject.get());
	}
	
	/**
	 * Convenience method to create an {@link InventoryChangeTrigger.Instance} for the possesion of the {@link Block} contained in the provided {@link RegistryObject}.
	 * <p>
	 * Simply calls {@link RegistryObject#get()} and provides the result to {@link RecipeHelper#hasItem(ItemLike)}.
	 * 
	 * @param registryObject - The {@link RegistryObject} containing the desired {@link Block}
	 * @return A suitable {@link InventoryChangeTrigger.Instance}
	 */
	protected static TriggerInstance hasBlock(RegistryObject<Block> registryObject) {
		return RecipeHelper.has(registryObject.get());
	}

	/**
	 * Helper method for creating a recipe name automatically from the passed two components.
	 * <p>
	 * The path of the resulting {@link ResourceLocation} will be the result of concatenating the registry path of the output {@link ItemLike}'s {@link Item}, then the {@link String} literal {@code "_from_"}, and then
	 * the registry path of the input {@link ItemLike}'s {@link Item}.
	 * <p>
	 * The first parameter, the {@link Function} must be a means of transforming a {@link String} registry path into a domain-qualified {@link ResourceLocation}.
	 *
	 * @param transformer - A path-to-qualified-domain transformer
	 * @param output - An "output" {@link ItemLike}
	 * @param input - An "input" {@link ItemLike}
	 * @return A {@link ResourceLocation} suitable for use as the name of an {@link FinishedRecipe}.
	 */
	protected static ResourceLocation nameFromIngredients(Function<String, ResourceLocation> transformer, ItemLike output, ItemLike input) {
		return RecipeHelper.nameFrom(transformer, output, RecipeHelper.registryPathOf(input));
	}
	
	/**
	 * Helper method for creating a recipe name automatically from the passed two components.
	 * <p>
	 * The path of the resulting {@link ResourceLocation} will be the result of concatenating the registry path of the output {@link ItemLike}'s {@link Item}, then the {@link String} literal {@code "_from_"}, and then
	 * the value of {@code outputSource}.
	 * <p>
	 * The first parameter, the {@link Function} must be a means of transforming a {@link String} registry path into a domain-qualified {@link ResourceLocation}.
	 *
	 * @param transformer - A path-to-qualified-domain transformer
	 * @param output - An "output" {@link ItemLike}
	 * @param outputSource - The "source" of the output item
	 * @return A {@link ResourceLocation} suitable for use as the name of an {@link FinishedRecipe}.
	 */
	protected static ResourceLocation nameFrom(Function<String, ResourceLocation> transformer, ItemLike output, String outputSource) {
		return transformer.apply(Utilities.Strings.name(RecipeHelper.registryPathOf(output), "from", outputSource));
	}

	/**
	 * Helper method for creating a criterion name from the registry path of the passed {@link ItemLike}.
	 * <p>
	 * Will query {@link ItemLike#asItem()} and then {@link ResourceLocation#getPath()} on the returned {@link ResourceLocation},
	 * prefixing it with {@value #CRITERION_PREFIX}.
	 *
	 * @param item - The {@link IForgeRegistryEntry} to generate a "criterion name" from.
	 * @return A {@link String} suitable for use as an advancement criterion name.
	 */
	protected static String criterionName(ItemLike item) {
		return RecipeHelper.createCriterionName(item.asItem());
	}

	/**
	 * Helper method for creating a criterion name from the registry path of the passed {@link TagKey}.
	 * <p>
	 * Will query {@link TagKey#getName()} and then {@link ResourceLocation#getPath()} on the returned {@link ResourceLocation},
	 * prefixing it with {@value #CRITERION_PREFIX}.
	 *
	 * @param item - The {@link TagKey} to generate a "criterion name" from.
	 * @return A {@link String} suitable for use as an advancement criterion name.
	 */
	protected static String criterionName(TagKey<?> item) {
		return CRITERION_PREFIX + item.location().getPath();
	}

	/**
	 * Helper method for creating a criterion name from the registry path of the passed {@link IForgeRegistryEntry}.
	 * <p>
	 * Will call {@link RegistryObject#get()} and then prefix the path of the returned registry name with {@value #CRITERION_PREFIX}.
	 *
	 * @param item - The {@link IForgeRegistryEntry} to generate a "criterion name" from.
	 * @return A {@link String} suitable for use as an advancement criterion name.
	 */
	protected static <T extends IForgeRegistryEntry<T>> String criterionName(RegistryObject<T> item) {
		return RecipeHelper.createCriterionName(item.get());
	}

	/**
	 * Helper method for creating a criterion name from the registry path of the passed {@link IForgeRegistryEntry}.
	 * <p>
	 * Will prefixing the path of the registry name the passed {@link IForgeRegistryEntry} with {@value #CRITERION_PREFIX}.
	 *
	 * @param item - The {@link IForgeRegistryEntry} to generate a "criterion name" from.
	 * @return A {@link String} suitable for use as an advancement criterion name.
	 */
	protected static <T extends IForgeRegistryEntry<T>> String createCriterionName(T item) {
		return CRITERION_PREFIX + RecipeHelper.getRegistryPath(item);
	}

	/**
	 * Convenience method to return the {@link ResourceLocation#getPath()} value of the {@link Item} obtained from the passed {@link ItemLike}.
	 *
	 * @param item - The {@link ItemLike} to query
	 * @return The {@link ResourceLocation} path of the {@link Item} from the passed {@link ItemLike}.
	 */
	protected static String registryPathOf(ItemLike item) {
		return RecipeHelper.getRegistryPath(item.asItem());
	}

	/**
	 * Convenience method to return the {@link ResourceLocation#getPath()} value of the {@link Item} obtained from the passed {@link RegistryObject}.
	 *
	 * @param item - The {@link RegistryObject} to query
	 * @return The {@link ResourceLocation} path of the {@link Item} from the passed {@link RegistryObject}.
	 */
	protected static <T extends IForgeRegistryEntry<T>> String registryPathOf(RegistryObject<T> entry) {
		return RecipeHelper.getRegistryPath(entry.get());
	}

	/**
	 * Convenience method to return the {@link ResourceLocation#getPath()} value of the {@link Item} obtained from the passed {@link IForgeRegistryEntry}.
	 *
	 * @param item - The {@link IForgeRegistryEntry} to query
	 * @return The {@link ResourceLocation} path of the registry name of the passed {@link IForgeRegistryEntry}.
	 */
	protected static <T extends IForgeRegistryEntry<T>> String getRegistryPath(T element) {
		return element.getRegistryName().getPath();
	}

}
