package paragon.minecraft.library.registration;

import java.util.Objects;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityType.EntityFactory;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

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
	
	/* Internal Methods */
	
	/**
	 * Adds a new {@link EntityType} to the internal {@link DeferredRegister} using the provided name, factory, category, and optional {@link EntityType.Builder} initializing {@link Consumer}.
	 * If the provided {@link Consumer} is {@code null}, it will simply be ignored.
	 * 
	 * @param <T>
	 * @param name - The name of the {@link Entity}
	 * @param factory - The {@link EntityFactory} to use for this {@link EntityType}
	 * @param category - The {@link MobCategory} of the {@link EntityType}
	 * @param initializer - An optional initializer to use
	 * @return A {@link RegistryObject} containing the registered {@link EntityType}.
	 */
	protected <T extends Entity> RegistryObject<EntityType<T>> addUsing(@Nonnull final String name, @Nonnull final EntityFactory<T> factory, MobCategory category, @Nullable final Consumer<EntityType.Builder<T>> initializer) {
		return this.add(name, () -> this.builder(factory, category, initializer).build(name));
	}
	
	/**
	 * Creates a parameterized {@link EntityType.Builder}, optionally initializing it using the provided {@link Consumer}. If the provided {@link Consumer} is {@code null}, it will simply be
	 * ignored.
	 * <p>
	 * Good for re-using common initialization logic.
	 * 
	 * @param <T> The entity type
	 * @param factory - The entity instance factory
	 * @param category - The {@link MobCategory} of the {@link EntityType}
	 * @param initializer - An optional initializer to use
	 * @return A suitably parameterized and initialized {@link EntityType.Builder}.
	 */
	protected <T extends Entity> EntityType.Builder<T> builder(@Nonnull final EntityFactory<T> factory, MobCategory category, @Nullable final Consumer<EntityType.Builder<T>> initializer) {
		final EntityType.Builder<T> builder = EntityType.Builder.<T>of(factory, category);
		if (Objects.nonNull(initializer)) {
			initializer.accept(builder);
		}
		return builder;
	}

}
