package paragon.minecraft.library.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;

import org.apache.logging.log4j.LogManager;

import com.mojang.serialization.Codec;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import paragon.minecraft.library.registration.IEventBusListener;
import paragon.minecraft.library.utilities.Require;
import paragon.minecraft.library.utilities.Utilities;

/**
 * This class enables {@link PlayerDataManager} to persist data to disk.
 * <p>
 * Handles serialization via a provided {@link Codec}, but does not perform client-server synchronization. For that, see {@link SyncHelper}.
 *
 * @author Malcolm Riley
 * @param <T> The type of data.
 */
public class PersistenceHelper<T> implements IEventBusListener.Simple {

	/* Internal Fields */
	protected final PlayerDataManager<T> MANAGER;
	protected final Codec<T> CODEC;
	protected final String NAME;

	/* Shared Constants */
	protected static final String DEFAULT_SUFFIX = ".gznbt";

	public PersistenceHelper(@Nonnull final PlayerDataManager<T> manager, @Nonnull final Codec<T> codec, @Nonnull final String name) {
		this.MANAGER = manager;
		this.CODEC = Objects.requireNonNull(codec, "Cannot initialize PersistenceHelper with null codec!");
		this.NAME = Require.notNullOrEmpty(name, "Cannot initialize PersistenceHelper with null or empty filename!");
	}

	/* Event Receiver Methods */

	/**
	 * Fired when player data is loaded from individual files. This occurs before the player is fully "logged in", and before registry, tags, and datapack data is transmitted.
	 *
	 * @param event - The event parameters
	 */
	@SubscribeEvent
	public void onPlayerLoad(final PlayerEvent.LoadFromFile event) {
		// Clear held data
		this.MANAGER.clearFor(event.getEntity());

		// Load data from additional file
		final File file = this.getFileFor(event.getPlayerDirectory(), event.getPlayerUUID());
		if (file.exists()) {
			try (InputStream stream = new FileInputStream(file)) {
				final CompoundTag tag = NbtIo.readCompressed(stream);
				final T instance = this.CODEC.decode(NbtOps.INSTANCE, tag).getOrThrow(false, this::printError).getFirst();
				this.MANAGER.put(Utilities.Players.getUUID(event.getEntity()), instance);
			}
			catch (IOException exception) {
				this.printException(exception);
			}
		}
	}

	/**
	 * Fires when player data is being saved to individual files.
	 *
	 * @param event - The event parameters
	 */
	@SubscribeEvent
	public void onPlayerSave(final PlayerEvent.SaveToFile event) {
		// Persist data to additional file
		final Optional<T> data = this.MANAGER.tryGetFor(event.getEntity());
		if (data.isPresent()) {
			final File file = this.getFileFor(event.getPlayerDirectory(), event.getPlayerUUID());
			try (OutputStream stream = new FileOutputStream(file)) {
				final CompoundTag tag = (CompoundTag) this.CODEC.encodeStart(NbtOps.INSTANCE, data.get()).getOrThrow(false, this::printError);
				NbtIo.writeCompressed(tag, stream);
			}
			catch (Exception exception) {
				this.printException(exception);
			}
		}
	}

	/* Internal Methods */

	protected File getFileFor(File directory, String playerID) {
		return directory.toPath().resolve(this.getFilename(playerID)).toFile();
	}

	protected String getFilename(String playerID) {
		return new StringBuilder()
			.append(playerID.toString())
			.append(".")
			.append(this.NAME)
			.append(PersistenceHelper.DEFAULT_SUFFIX)
			.toString();
	}

	protected void printException(String operation, Exception exception) {
		this.printError(operation);
		this.printException(exception);
	}

	protected void printError(String operation) {
		LogManager.getLogger().error(this.buildErrorMessage(operation));
	}

	protected void printException(Exception exception) {
		LogManager.getLogger().error(exception);
	}

	protected String buildErrorMessage(String operation) {
		return new StringBuilder()
			.append("Exception occurred while attempting to ")
			.append(operation)
			.append(" persistent player data for type ")
			.append("\"")
			.append(this.NAME)
			.append("\": ")
			.toString();
	}

}
