package paragon.minecraft.library.network;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.PacketDistributor.PacketTarget;
import net.minecraftforge.network.simple.SimpleChannel;
import paragon.minecraft.library.Require;

/**
 * Abstract class to ease in the implementation of {@link SimpleChannel}-based networking.
 * 
 * @author Malcolm Riley
 */
public abstract class AbstractNetworkHandler {
	
	/* Internal Constants */
	protected static final String FORGE_MOD_ABSENT = "ABSENT";
	protected static final String FORGE_VANILLA_SERVER = "ACCEPTVANILLA";
	protected static final String EXCEPTION_NULL_VERSION = "Cannot initialize NetworkHandler with null or empty protocol version String!";
	protected static final String EXCEPTION_NULL_CHANNEL = "Cannot initialize NetworkHandler with null channel!";
	
	/* Internal Fields */
	protected final String PROTOCOL_VERSION;
	protected final boolean CAN_CONNECT_TO_VANILLA;
	protected final boolean CAN_CONNECT_IF_MOD_ABSENT;
	private final SimpleChannel CHANNEL;
	

	protected AbstractNetworkHandler(final ResourceLocation name, final String version) {
		this(name, version, false, false);
	}
	
	protected AbstractNetworkHandler(final ResourceLocation name, final String version, boolean canConnectIfAbsent, boolean canConnectToVanilla) {
		this.CHANNEL = Objects.requireNonNull(this.createChannel(name), AbstractNetworkHandler.EXCEPTION_NULL_CHANNEL);
		this.PROTOCOL_VERSION = Require.notNullOrEmpty(version, AbstractNetworkHandler.EXCEPTION_NULL_VERSION);
		this.CAN_CONNECT_IF_MOD_ABSENT = canConnectIfAbsent;
		this.CAN_CONNECT_TO_VANILLA = canConnectToVanilla;
	}
	
	/* Shared Methods */
	
	protected SimpleChannel createChannel(final ResourceLocation name) {
		return NetworkRegistry.newSimpleChannel(name, () -> this.PROTOCOL_VERSION, this::canConnect, this::canConnect);
	}
	
	/* Public Methods */
	
	/**
	 * Sends the provided {@link INetworkMessage} to the server from the client.
	 * <p>
	 * <b> Important! </b> This method should only be called from client-side!
	 * 
	 * @param instance - The {@link INetworkMessage} to send.
	 */
	public void sendToServer(@Nonnull INetworkMessage instance) {
		this.CHANNEL.sendToServer(instance);
	}
	
	/**
	 * Sends the provided {@link INetworkMessage} from the server to the client represented by the provided {@link ServerPlayer}.
	 * 
	 * @param player - The {@link ServerPlayer} to be messaged
	 * @param instance - The {@link INetworkMessage} to send.
	 */
	public void sendToPlayer(@Nonnull ServerPlayer player, @Nonnull INetworkMessage instance) {
		this.sendTo(PacketDistributor.PLAYER.with(() -> player), instance);
	}
	
	/**
	 * Sends the provided {@link INetworkMessage} from the server to the targets determined by the provided {@link PacketTarget}.
	 * Callers may use {@link PacketDistributor} to conveniently obtain a {@link PacketTarget}.
	 * <p>
	 * <b> Important! </b> This method should only be called from server-side!
	 * 
	 * @param target - The target selector to use
	 * @param instance - The {@link INetworkMessage} to send to identified targets.
	 */
	public void sendTo(PacketTarget target, @Nonnull INetworkMessage instance) {
		this.CHANNEL.send(target, instance);
	}
	
	/**
	 * Sends the provided {@link INetworkMessage} to all connected clients.
	 * <p>
	 * <b> Important! </b> This method should only be called from server-side, and as a measure of last resort. In most circumstances, callers should prefer {@link #sendTo(PacketTarget, INetworkMessage)}.
	 * @param instance - The {@link INetworkMessage} to send to all connected clients.
	 * @see #sendTo(PacketTarget, INetworkMessage)
	 */
	public void sendToAll(@Nonnull INetworkMessage instance) {
		this.CHANNEL.send(PacketDistributor.ALL.noArg(), instance);
	}
	
	public void initialize() {
		this.registerMessages(new MessageHelper(this.CHANNEL));
	}
	
	/* Internal Methods */
	
	protected boolean canConnect(String protocolVersion) {
		return switch(protocolVersion) {
			case AbstractNetworkHandler.FORGE_VANILLA_SERVER -> this.CAN_CONNECT_TO_VANILLA; // Remote version reported by Forge when connecting to a vanilla server
			case AbstractNetworkHandler.FORGE_MOD_ABSENT -> this.CAN_CONNECT_IF_MOD_ABSENT; // Remote version reported by forge when connecting to server with mod absent
			default -> this.remoteVersionAccepted(protocolVersion);
		};
	}
	
	protected boolean remoteVersionAccepted(String protocolVersion) {
		return this.PROTOCOL_VERSION.equals(protocolVersion);
	}
	
	/**
	 * This method may be used to register network messages using the provided helper.
	 * @param helper
	 */
	protected abstract void registerMessages(final MessageHelper helper);
	
	/* Message Interface */
	
	/**
	 * Dummy interface specifying that this object is a network message of some kind.
	 * 
	 * @author Malcolm Riley
	 */
	public static interface INetworkMessage {
		
	}
	
	/**
	 * Interface specifying a network message handler.
	 * 
	 * @author Malcolm Riley
	 *
	 * @param <T> The type of network message
	 */
	public static interface INetworkMessageHandler<T extends INetworkMessage> {
		
		/**
		 * This method should return the class of {@link INetworkMessage} that this {@link INetworkMessageHandler} handles.
		 * 
		 * @return The {@link INetworkMessage} class.
		 */
		public Class<T> getMessageClass();
		
		/**
		 * This method should use the provided {@link FriendlyByteBuf} to encode the provided {@link INetworkMessage} instance.
		 * 
		 * @param message - The message to encode
		 * @param buffer - The buffer to use
		 */
		public void encode(T message, FriendlyByteBuf buffer);
		
		/**
		 * This method should decode the and instantiate a new {@link INetworkMessage} using the provided {@link FriendlyByteBuf}.
		 * 
		 * @param buffer - The buffer to use
		 * @return A new instance of message, decoded.
		 */
		public T decode(FriendlyByteBuf buffer);
		
		/**
		 * This method should provide the actual handling logic for a successfully decoded {@link INetworkMessage}.
		 * <p>
		 * Use the provided {@link NetworkEvent.Context} supplier to enqueue work on the network thread and set whether the message has been handled.
		 * <p>
		 * <b> Important! </b> Don't forget to call {@link NetworkEvent.Context#setPacketHandled(boolean)} at the close of this method!
		 * 
		 * @param message - The decoded message to respond to
		 * @param contextProvider - A provider of {@link NetworkEvent.Context}
		 */
		public void handle(T message, Supplier<NetworkEvent.Context> contextProvider);
		
	}
	
	/* MessageHelper Implementation */
	
	/**
	 * Helper class for initializing new network messages to a particular {@link SimpleChanenl} instance.
	 * <p>
	 * Keeps track of message ID automatically, and allows for simplified registration via {@link INetworkMessageHandler}.
	 * 
	 * @author Malcolm Riley
	 */
	protected static class MessageHelper {
		
		/* Internal Fields */
		protected final SimpleChannel CHANNEL;
		protected int currentID = 0;
		
		protected MessageHelper(SimpleChannel channel) {
			this.CHANNEL = Objects.requireNonNull(channel);
		}
		
		public <T extends INetworkMessage> MessageHelper addMessage(INetworkMessageHandler<T> messageHandler) {
			return this.addMessage(messageHandler, null);
		}
		
		public <T extends INetworkMessage> MessageHelper addMessage(INetworkMessageHandler<T> messageHandler, @Nullable NetworkDirection direction) {
			return this.addMessage(messageHandler.getMessageClass(), messageHandler::encode, messageHandler::decode, messageHandler::handle, direction);
		}
		
		public <T> MessageHelper addMessage(Class<T> messageClass, BiConsumer<T, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, T> decoder, BiConsumer<T, Supplier<NetworkEvent.Context>> handler) {
			return this.addMessage(messageClass, encoder, decoder, handler, null);
		}
		
		public <T> MessageHelper addMessage(Class<T> messageClass, BiConsumer<T, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, T> decoder, BiConsumer<T, Supplier<NetworkEvent.Context>> handler, @Nullable NetworkDirection direction) {
			this.CHANNEL.registerMessage(currentID, messageClass, encoder, decoder, handler, Optional.ofNullable(direction));
			this.currentID += 1;
			return this;
		}
		
	}

}
