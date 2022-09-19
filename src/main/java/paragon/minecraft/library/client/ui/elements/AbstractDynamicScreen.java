package paragon.minecraft.library.client.ui.elements;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public abstract class AbstractDynamicScreen extends Screen implements IDynamicGuiElement {
	
	/* Internal Fields */
	protected int x, y;
	protected boolean initialized = false;

	protected AbstractDynamicScreen(Component title) {
		super(title);
	}
	
	/* IDynamicGuiElement Compliance Methods */

	@Override
	public int getX() {
		return this.x;
	}

	@Override
	public int getY() {
		return this.y;
	}

	@Override
	public int getWidth() {
		return this.width;
	}

	@Override
	public int getHeight() {
		return this.height;
	}

	@Override
	public void setX(int xPos) {
		this.x = xPos;
	}

	@Override
	public void setY(int yPos) {
		this.y = yPos;
	}

	@Override
	public void setWidth(int width) {
		this.width = width;
	}

	@Override
	public void setHeight(int height) {
		this.height = height;
	}
	
	/* Supertype Override Methods */
	
	public void init() {
		super.init();
		if (!this.initialized) {
			this.initializeComponents();
		}
		this.updatePlacement();
	}
	
	@Override
	public void render(PoseStack poseStack, int cursorX, int cursorY, float delta) {
		this.renderBackground(poseStack);
		this.doRender(poseStack, cursorX, cursorY, delta);
		super.render(poseStack, cursorX, cursorY, delta);
	}
	
	/* Abstract Methods */
	
	protected abstract void doRender(PoseStack poseStack, int cursorX, int cursorY, float delta);
	
	protected abstract void initializeComponents();
	
	/* Internal Methods */
	
	protected void updatePlacement() {
		
	}

}
