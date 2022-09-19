package paragon.minecraft.library.registration;

import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class EffectProvider extends ContentProvider<MobEffect> {

	public EffectProvider(String modID) {
		super(modID);
	}
	
	/* Abstract Method Implementation */

	@Override
	protected DeferredRegister<MobEffect> initializeRegistry(String modID) {
		return DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, modID);
	}

}
