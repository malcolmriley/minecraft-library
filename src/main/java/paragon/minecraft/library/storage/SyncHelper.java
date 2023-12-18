package paragon.minecraft.library.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import paragon.minecraft.library.network.AbstractNetworkHandler;
import paragon.minecraft.library.network.AbstractNetworkHandler.INetworkMessage;
import paragon.minecraft.library.registration.IEventBusListener;
import paragon.minecraft.library.utilities.Utilities;

public class SyncHelper<T extends IDataHolder.Syncable> implements IEventBusListener.Simple {

	/* Internal References */
	protected final PlayerDataManager<T> MANAGER;
	protected final AbstractNetworkHandler HANDLER;

	public SyncHelper(PlayerDataManager<T> manager, AbstractNetworkHandler channel) {
		this.MANAGER = Objects.requireNonNull(manager, "Cannot initialize SyncHelper with null manager reference!");
		this.HANDLER = Objects.requireNonNull(channel, "Cannot initialize SyncHelper with null network channel!");
	}

	/* Public Methods */

	public void trySyncAll(@Nonnull final Iterable<ServerPlayer> players) {
		players.forEach(this::trySyncPlayer);
	}

	public void trySyncPlayer(@Nullable final ServerPlayer player) {
		if (Objects.nonNull(player)) {
			this.syncToPlayer(player);
		}
	}

	/* Event Receiver Methods */

	@SubscribeEvent
	public void onPlayerLoginEvent(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			this.syncToPlayer(player);
		}
	}

	/* Internal Methods */

	protected void syncToPlayer(ServerPlayer player) {
		final UUID id = Utilities.Players.getUUID(player);
		if (this.MANAGER.hasDataFor(id)) {
			final T instance = this.MANAGER.get(id);
			if (instance.holdsData()) {
				final List<INetworkMessage> messages = new ArrayList<>();
				instance.appendMessages(player, messages);
				messages.forEach(message -> this.HANDLER.sendToPlayer(player, message));
			}
		}
	}

}
