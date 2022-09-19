package paragon.minecraft.library.client.ui.elements;

public interface IDynamicGuiElement extends IGuiElement {
	
	public void setX(int xPos);
	
	public void setY(int yPos);
	
	public void setWidth(int width);
	
	public void setHeight(int height);
	
	public default void setPosition(int xPos, int yPos) {
		this.setX(xPos);
		this.setY(yPos);
	}
	
	public default void setDimensions(int width, int height) {
		this.setWidth(width);
		this.setHeight(height);
	}

}
