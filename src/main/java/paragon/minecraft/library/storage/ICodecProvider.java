package paragon.minecraft.library.storage;

import com.mojang.serialization.Codec;

/**
 * Interface specifying that this class provides a {@link Codec} with the same type as the interface type parameter.
 *
 * @author Malcolm Riley
 *
 * @param <T> The type interpreted by the returned {@link Codec}.
 */
@FunctionalInterface
public interface ICodecProvider<T> {
	
	public Codec<T> getCodec();

}
