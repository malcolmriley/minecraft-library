package paragon.minecraft.library.client.ui.elements;

import com.mojang.blaze3d.vertex.PoseStack;

import paragon.minecraft.library.client.ClientUtilities;

public class WindowMonitor implements IGuiElement {
	
	/* Shared Fields */
	public static WindowMonitor INSTANCE = new WindowMonitor();
	
	private WindowMonitor() { }
	
	/* IGuiElement Compliance Methods */

	@Override
	public int getX() {
		return 0;
	}

	@Override
	public int getY() {
		return 0;
	}

	@Override
	public int getWidth() {
		return ClientUtilities.UI.getWindow().getGuiScaledWidth();
	}

	@Override
	public int getHeight() {
		return ClientUtilities.UI.getWindow().getGuiScaledHeight();
	}

	@Override
	public void render(PoseStack pose, int cursorX, int cursorY, float delta) {
		// No-op
	}

}
