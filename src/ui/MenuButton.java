package ui;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import org.lwjgl.glfw.GLFW;

import gamestates.Gamestate;
import utilz.LoadSave;
import static utilz.Constants.UI.Buttons.*;

public class MenuButton {
	private int xPos, yPos, rowIndex, index;
	private int xOffsetCenter = B_WIDTH / 2;
	private Gamestate state;
	private BufferedImage[] imgs;
	private int buttonState = GLFW.GLFW_RELEASE, prevButtonState = GLFW.GLFW_RELEASE;
	private Rectangle bounds;
	private int controllerButtonId;

	public MenuButton(int xPos, int yPos, int rowIndex, Gamestate state, int controllerButtonId) {
		this.xPos = xPos;
		this.yPos = yPos;
		this.rowIndex = rowIndex;
		this.state = state;
		this.controllerButtonId = controllerButtonId;
		loadImgs();
		initBounds();
	}

	private void initBounds() {
		bounds = new Rectangle(xPos - xOffsetCenter, yPos, B_WIDTH, B_HEIGHT);
	}

	private void loadImgs() {
		imgs = new BufferedImage[2];
		BufferedImage temp = LoadSave.GetSpriteAtlas(LoadSave.MENU_BUTTONS);
		for (int i = 0; i < imgs.length; i++)
			imgs[i] = temp.getSubimage(i * B_WIDTH_DEFAULT, rowIndex * B_HEIGHT_DEFAULT, B_WIDTH_DEFAULT, B_HEIGHT_DEFAULT);
	}

	public void draw(Graphics g, int xDrawOffset) {
		g.drawImage(imgs[index], xPos - xOffsetCenter + xDrawOffset, yPos, B_WIDTH, B_HEIGHT, null);
	}

	public void update() {
		prevButtonState = buttonState;
		if (GLFW.glfwJoystickPresent(GLFW.GLFW_JOYSTICK_1)) {
			ByteBuffer buttons1 = GLFW.glfwGetJoystickButtons(GLFW.GLFW_JOYSTICK_1);
			buttonState = buttons1.get(controllerButtonId);
		}
		if (GLFW.glfwJoystickPresent(GLFW.GLFW_JOYSTICK_2)) {
			ByteBuffer buttons2 = GLFW.glfwGetJoystickButtons(GLFW.GLFW_JOYSTICK_2);
			if (buttons2.get(controllerButtonId) == GLFW.GLFW_PRESS)
				buttonState = GLFW.GLFW_PRESS;
		}
		index = 0;
		if (buttonState == GLFW.GLFW_PRESS)
			index = 1;
	}

	public Rectangle getBounds() {
		return bounds;
	}
 
	public void applyGamestate() {
		Gamestate.state = state;
	}

	public void resetBools() {
		buttonState = GLFW.GLFW_RELEASE; 
		prevButtonState = GLFW.GLFW_RELEASE;
	}
	
	public Gamestate getState() {
		return state;
	}

	public int getButtonState() {
		return buttonState;
	}

	public void setButtonState(int buttonState) {
		this.buttonState = buttonState;
	}

	public int getPrevButtonState() {
		return prevButtonState;
	}

	public void setPrevButtonState(int prevButtonState) {
		this.prevButtonState = prevButtonState;
	}
	
}
