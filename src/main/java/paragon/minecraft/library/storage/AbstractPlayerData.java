package paragon.minecraft.library.storage;

import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.authlib.GameProfile;

import net.minecraft.world.entity.player.Player;
import paragon.minecraft.library.utilities.Utilities;

/**
 * Simple base class for holding per-player data.
 * <p>
 * Holds a reference to the {@link UUID} of the desired player.
 * 
 * @author Malcolm Riley
 */
public abstract class AbstractPlayerData implements IDataHolder {
	
	/* Internal Fields */
	private final UUID PLAYER_ID;
	
	public AbstractPlayerData(@Nonnull final UUID id) {
		this.PLAYER_ID = Objects.requireNonNull(id, "Cannot initialize player data with null UUID!");
	}
	
	/**
	 * Returns the stable {@link UUID} associated with the {@link GameProfile} of the intended player.
	 * 
	 * @return The player's {@link UUID}.
	 */
	public UUID getID() {
		return this.PLAYER_ID;
	}
	
	/**
	 * Returns whether this {@link AbstractPlayerData} is intended for the passed {@link Player}. If the passed {@link Player} is {@code non-null} and the {@link GameProfile} {@link UUID}
	 * matches, returns {@code true}, otherwise returns {@code false}.
	 * 
	 * @param player - The {@link Player} to check
	 * @return Whether the provided {@link Player} is non-{@code null} and has the same {@link UUID} as {@link #getID()}.
	 */
	public boolean isFor(@Nullable final Player player) {
		return Objects.nonNull(player) && Objects.equals(Utilities.Players.getUUID(player), this.getID());
	}

}
