package paragon.minecraft.library.datageneration;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import paragon.minecraft.library.registration.IRegistryNamed;

/**
 * Partial implementation of a {@link IDataProvdier} for {@link IRegistryNamed} instances. Handles some boilerplate associated
 * with generating data.
 * 
 * @author Malcolm Riley
 *
 * @param <T> The {@link IRegistryNamed} type.
 */
public abstract class AbstractDataGenerator<T extends IRegistryNamed> implements DataProvider {
	
	/* Internal Fields */
	protected final Gson GSON;
	protected final Path OUTPUT;
	protected final ResourceKey<Registry<T>> REGISTRY_KEY;
	private final Codec<T> CODEC;
	private final RegistryOps<JsonElement> OPERATION = RegistryOps.create(JsonOps.INSTANCE, RegistryAccess.BUILTIN.get());
	
	/* Constants */
	private static final String OUTPUT_SUFFIX = ".json";
	
	protected AbstractDataGenerator(DataGenerator generator, Gson gson, ResourceKey<Registry<T>> registryKey, Codec<T> codec) {
		this.OUTPUT = generator.getOutputFolder();
		this.GSON = gson;
		this.REGISTRY_KEY = Objects.requireNonNull(registryKey, "RegistryKey cannot be null!");
		this.CODEC = Objects.requireNonNull(codec, "Codec cannot be null!");
	}
	
	/* IDataProvider Compliance Methods */

	@Override
	public void run(HashCache cache) throws IOException {
		this.generate(instance -> this.createAndAdd(instance, cache));
	}
	
	/* Abstract Methods */
	
	/**
	 * This method should be overridden to serialize all known content. Feed individual instances to the provided {@link Consumer}
	 * in order to have them generated.
	 * 
	 * @param generator - The generator to use.
	 */
	protected abstract void generate(Consumer<T> generator);
	
	/* Internal Methods */
	
	protected Path resolveOutputPath(ResourceLocation name) {
		final ResourceLocation registry = this.REGISTRY_KEY.location();
		return this.OUTPUT.resolve(Paths.get(PackType.SERVER_DATA.getDirectory(), name.getNamespace(), registry.getNamespace(), registry.getPath(), name.getPath() + OUTPUT_SUFFIX));
	}
	
	private final void createAndAdd(final T instance, HashCache cache) {
		final Path path = this.resolveOutputPath(instance.getRegistryName());
		this.CODEC.encodeStart(this.OPERATION, instance)
			.resultOrPartial(error -> this.logSerializerError(error, path))
			.ifPresent(result -> {
				try {
					DataProvider.save(this.GSON, cache, result, path);
				}
				catch (IOException exception) {
					this.logSerializerError(exception.getMessage(), path);
				}
			});
	}
	
	private final void logSerializerError(String error, Path path) {
		LogManager.getLogger().error("Error while serializing data {} for path {}: {}", this.REGISTRY_KEY.location(), path, error);
	}
	
}
