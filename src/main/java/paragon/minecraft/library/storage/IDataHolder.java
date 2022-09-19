package paragon.minecraft.library.storage;

import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.server.level.ServerPlayer;
import paragon.minecraft.library.network.AbstractNetworkHandler.INetworkMessage;

/**
 * Interface specifying a data-holding object.
 * 
 * @author Malcolm Riley
 */
public interface IDataHolder {
	
	/**
	 * This method should return {@code true} if this {@link AbstractPlayerData} holds any data, and {@code false} otherwise.
	 * <p>
	 * This is to distinguish between uninitialized {@link AbstractPlayerData}, and initialized {@link AbstractPlayerData} with nothing held.
	 * 
	 * @return Whether any data is held by this {@link AbstractPlayerData} instance.
	 */
	public boolean holdsData();
	
	/**
	 * Interface specifying a data-holding object that may be synchronized to a remote receiver.
	 * 
	 * @author Malcolm Riley
	 */
	public static interface Syncable extends IDataHolder {
		
		/**
		 * This method should append all necessary {@link INetworkMessage} for synchronizing to a remote client.
		 * 
		 * @param player - The {@link ServerPlayer} to send the messages to
		 * @param messageList - The list of messages to send.
		 */
		public void appendMessages(@Nonnull final ServerPlayer player, @Nonnull final List<? super INetworkMessage> messageList);
		
	}

}
