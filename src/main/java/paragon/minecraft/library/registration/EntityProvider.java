package paragon.minecraft.library.registration;

import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Specialized {@link ContentProvider} subtype for providing {@link EntityType}.
 *
 * @author Malcolm Riley
 */
public class EntityProvider extends ContentProvider<EntityType<?>> {

	public EntityProvider(String modID) {
		super(modID);
	}
	
	/* Supertype Override Methods */

	@Override
	protected DeferredRegister<EntityType<?>> initializeRegistry(String modID) {
		return DeferredRegister.create(ForgeRegistries.ENTITIES, modID);
	}

}
