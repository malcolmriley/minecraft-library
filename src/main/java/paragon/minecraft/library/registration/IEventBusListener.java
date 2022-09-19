package paragon.minecraft.library.registration;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;

/**
 * Interface specification for classes that listen to {@link IEventBus}. Good for classes that themselves contain event-listening instances,
 * for easy grouped registration. For example, a class may contain multiple {@link DeferredRegister} that require registration with the same event bus.
 * <p>
 * For classes that need only register {@link SubscribeEvent}-decorated classes, see {@link Simple}.
 * <p>
 * Additionally, classes may implement {@link IEventBusListener.Forge} and {@link IEventBusListener.FML} and override {@link #registerTo(IEventBus)} in order to
 * perform all appropriate
 *
 * @author Malcolm Riley
 * @see {@link Simple}
 */
public interface IEventBusListener {

	/**
	 * Use this method to register the appropriate listeners to the passed {@link IEventBus}.
	 * This {@link IEventBus} is usually provided by {@link FMLJavaModLoadingContext#getModEventBus()}
	 *
	 * @param bus - The {@link IEventBus} to register to.
	 */
	public void registerTo(IEventBus bus);

	/* Subtype Implementations */

	/**
	 * Simple default implementation of {@link IEventBusListener} that simply registers itself to the provided {@link IEventBus}.
	 * <p>
	 * Good for event handling classes that have {@link SubscribeEvent}-decorated methods.
	 *
	 * @author Malcolm Riley
	 */
	public static interface Simple extends IEventBusListener {

		@Override
		public default void registerTo(IEventBus bus) {
			bus.register(this);
		}

	}

	/**
	 * Interface specifying that this is an {@link IEventBusListener} that is capable of self-registering to an appropriate bus
	 * when {@link #register()} is called.
	 * <p>
	 * Overriding {@link #registerTo(IEventBus)} to perform all necessary sub-registrations (such as in the case of a class holding references to multiple event listeners)
	 * is also recommended, but not necessary as this interface inherits {@link Simple}.
	 * 
	 * @author Malcolm Riley
	 */
	public static interface SelfRegistering extends Simple {

		/**
		 * This method should be used to self-register to the appropriate {@link IEventBus} during mod construction.
		 * <p>
		 * Suggested implementation is to call {@link #registerTo(IEventBus)} with a reference to the appropriate bus.
		 */
		public void register();

	}

	/**
	 * Extension of {@link Simple} that registers itself to the {@link MinecraftForge#EVENT_BUS}.
	 * <p>
	 *
	 * @author Malcolm Riley
	 */
	public static interface Forge extends SelfRegistering {

		@Override
		public default void register() {
			this.registerTo(MinecraftForge.EVENT_BUS);
		}

	}

	/**
	 * Extension of {@link Simple} that registers itself to the mod-context FML {@link IEventBus}.
	 *
	 * @author Malcolm Riley
	 */
	public static interface FML extends SelfRegistering {

		@Override
		public default void register() {
			this.registerTo(FMLJavaModLoadingContext.get().getModEventBus());
		}

	}

}
