package ui;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import org.lwjgl.glfw.GLFW;

import utilz.LoadSave;
import static utilz.Constants.UI.URMButtons.*;

public class UrmButton extends PauseButton {
	private BufferedImage[] imgs;
	private int rowIndex, index;
	private boolean mouseOver, mousePressed;
	private int buttonState = GLFW.GLFW_RELEASE, prevButtonState = GLFW.GLFW_RELEASE;
	private int controllerButtonId;
	
	public UrmButton(int x, int y, int width, int height, int rowIndex, int controllerButtonId) {
		super(x, y, width, height);
		this.rowIndex = rowIndex;
		this.controllerButtonId = controllerButtonId;
		loadImgs();
	}

	private void loadImgs() {
		BufferedImage temp = LoadSave.GetSpriteAtlas(LoadSave.URM_BUTTONS);
		imgs = new BufferedImage[3];
		for (int i = 0; i < imgs.length; i++)
			imgs[i] = temp.getSubimage(i * URM_DEFAULT_SIZE, rowIndex * URM_DEFAULT_SIZE, URM_DEFAULT_SIZE, URM_DEFAULT_SIZE);

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
			index = 2;
	}

	public void draw(Graphics g, int xDrawOffset) {
		g.drawImage(imgs[index], x + xDrawOffset, y, URM_SIZE, URM_SIZE, null);
	}

	public void resetBools() {
		mouseOver = false;
		mousePressed = false;
	}

	public boolean isMouseOver() {
		return mouseOver;
	}

	public void setMouseOver(boolean mouseOver) {
		this.mouseOver = mouseOver;
	}

	public boolean isMousePressed() {
		return mousePressed;
	}

	public void setMousePressed(boolean mousePressed) {
		this.mousePressed = mousePressed;
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
