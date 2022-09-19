package paragon.minecraft.library.client.ui.elements;

public abstract class AbstractGuiElement implements IGuiElement {
	
	/* Internal Fields */
	protected int xPos;
	protected int yPos;
	protected int width;
	protected int height;
	
	/* IGuiElement Compliance Methods */

	@Override
	public int getX() {
		return this.xPos;
	}

	@Override
	public int getY() {
		return this.yPos;
	}

	@Override
	public int getWidth() {
		return this.width;
	}

	@Override
	public int getHeight() {
		return this.height;
	}

}
