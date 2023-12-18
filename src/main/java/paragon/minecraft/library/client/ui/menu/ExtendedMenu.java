package paragon.minecraft.library.client.ui.menu;

import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;

/**
 * Abstract class extension for a {@link AbstractContainerMenu} with {@link ContainerLevelAccess}.
 * 
 * @author Malcolm Riley
 */
public abstract class ExtendedMenu extends AbstractContainerMenu {

	/* Internal Fields */
	protected final IMenuValidator VALIDATOR;
	
	protected ExtendedMenu(MenuType<?> type, int id, @Nullable IMenuValidator validator) {
		super(type, id);
		this.VALIDATOR = Objects.requireNonNullElse(validator, IMenuValidator.DEFAULT);
	}
	
	/* Public Methods */
	
	public IMenuValidator getValidator() {
		return this.VALIDATOR;
	}
	
	public static abstract class LevelAccess extends ExtendedMenu {
		
		/* Internal Fields */
		protected final ContainerLevelAccess ACCESS;

		protected LevelAccess(MenuType<?> type, int id, @Nullable IMenuValidator validator, @Nullable ContainerLevelAccess access) {
			super(type, id, validator);
			this.ACCESS = Objects.requireNonNullElse(access, ContainerLevelAccess.NULL);
		}
		
		/* Supertype Override Methods */

		@Override
		public boolean stillValid(Player player) {
			return this.ACCESS.evaluate((level, pos) -> this.VALIDATOR.isValid(level, player, pos), true);
		}
		
	}
	
	public static abstract class PlayerAccess extends ExtendedMenu {

		/* Internal Fields */
		protected final Optional<Player> PLAYER;
		
		protected PlayerAccess(MenuType<?> type, int id, IMenuValidator validator, @Nullable Player player) {
			super(type, id, validator);
			this.PLAYER = Optional.ofNullable(player);
		}
		
		/* Public Methods */
		
		public boolean hasPlayerReference() {
			return this.PLAYER.isPresent();
		}
		
		public Optional<Player> tryGetPlayer() {
			return this.PLAYER;
		}
		
		@Nullable
		public Player getPlayer() {
			return this.PLAYER.orElse(null);
		}
		
	}

}
