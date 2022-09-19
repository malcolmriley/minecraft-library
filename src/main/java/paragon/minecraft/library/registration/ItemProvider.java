package paragon.minecraft.library.registration;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Specialized {@link ContentProvider} for {@link Item} instances.
 * 
 * @author Malcolm Riley
 */
public class ItemProvider extends ContentProvider<Item> {

	public ItemProvider(String modID) {
		super(modID);
	}
	
	/* Abstract Method Implementation */

	@Override
	protected DeferredRegister<Item> initializeRegistry(String modID) {
		return DeferredRegister.create(ForgeRegistries.ITEMS, modID);
	}
	
	/* Internal Methods */
	
	/**
	 * Internal convenience method to create a new {@link Item.Properties} instance
	 * with the provided {@link CreativeModeTab} as the creative mode tab.
	 * 
	 * @param group - The {@link CreativeModeTab} to set
	 * @return A thusly-instantiated {@link Item.Properties} instance.
	 */
	protected Item.Properties createProperties(final CreativeModeTab group) {
		return new Item.Properties().tab(group);
	}

}
