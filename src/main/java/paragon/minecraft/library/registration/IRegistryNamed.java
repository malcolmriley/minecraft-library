package paragon.minecraft.library.registration;

import net.minecraft.resources.ResourceLocation;

/**
 * Base interface type for objects that can be identified by a {@link ResourceLocation}.
 * 
 * @author Malcolm Riley
 */
public interface IRegistryNamed {
	
	/**
	 * This method should return the ID of this {@link IRegistryNamed}.
	 * 
	 * @return The {@link ResourceLocation} ID of this {@link IRegistryNamed}.
	 */
	public ResourceLocation getRegistryName();

}
