package paragon.minecraft.library.network;

import org.apache.logging.log4j.LogManager;

import net.neoforged.neoforge.network.NetworkEvent;
import paragon.minecraft.library.network.AbstractNetworkHandler.INetworkMessage;
import paragon.minecraft.library.network.AbstractNetworkHandler.INetworkMessageHandler;

/**
 * \
 * Simple base class for {@link INetworkMessageHandler} implementors. Performs simple validation and threaded execution of message-associated work.
 *
 * @author Malcolm Riley
 * @param <T> The {@link INetworkMessage} type
 */
public abstract class AbstractNetworkMessageHandler<T extends INetworkMessage> implements INetworkMessageHandler<T> {

	@Override
	public void handle(T message, NetworkEvent.Context context) {
		if (this.validate(message)) {
			context.enqueueWork(() -> this.onReceiveMessage(message));
			context.setPacketHandled(true);
		}
		else {
			LogManager.getLogger().warn("Received invalid packet, it will be ignored: {}", message);
			context.setPacketHandled(false);
		}
	}

	/**
	 * This method should execute the action associated with receiving the provided message. This method will be wrapped and executed on an appropriate thread via {@link NetworkEvent.Context#enqueueWork(Runnable)}.
	 * <p>
	 * The message received here has already passed validation.
	 *
	 * @param message - The received message.
	 */
	protected abstract void onReceiveMessage(T message);

	/**
	 * This method should validate the provided message. If the message fails validation, it will be ignored.
	 * <p>
	 * For instance, a message that requests some kind of level interaction should check here to see if the chunk is loaded.
	 *
	 * @param message - The message to validate
	 * @return Whether or not the provided message should be considered valid.
	 */
	protected abstract boolean validate(T message);

}
