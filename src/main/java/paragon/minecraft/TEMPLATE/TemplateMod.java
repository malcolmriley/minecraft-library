package paragon.minecraft.TEMPLATE;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;

@Mod(TemplateMod.MOD_ID)
public class TemplateMod {
	
	public static final String MOD_ID = "template";

	public TemplateMod() { }
	
	/**
	 * Creates a {@link ResourceLocation} in the {@value #MOD_ID} domain with
	 * the provided suffix as a path.
	 * 
	 * @param suffix - The {@link ResourceLocation} path to use
	 * @return A suitable {@link ResourceLocation}
	 */
	public static ResourceLocation resource(String suffix) {
		return new ResourceLocation(TemplateMod.MOD_ID, suffix);
	}
	
}
