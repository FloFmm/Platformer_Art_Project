package ui;

import audio.AudioPlayer;
import gamestates.Gamestate;
import main.Game;
import main.GamePanel;
import org.lwjgl.glfw.GLFW;
import utilz.LoadSave;

import java.awt.*;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import static utilz.Constants.UI.Buttons.*;

public class MenuButton {
    private final int xPos;
    private final int yPos;
    private final int rowIndex;
    private final int xOffsetCenter = B_WIDTH / 2;
    private final Gamestate state;
    private final int controllerButtonId;
    private final Game game;
    private int index;
    private BufferedImage[] imgs;
    private int buttonState = GLFW.GLFW_RELEASE, prevButtonState = GLFW.GLFW_RELEASE;
    private Rectangle bounds;
    private boolean mouseOver;

    public MenuButton(int xPos, int yPos, int rowIndex, Gamestate state, int controllerButtonId, Game game) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.rowIndex = rowIndex;
        this.state = state;
        this.controllerButtonId = controllerButtonId;
        this.game = game;
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
        GamePanel gamePanel1 = game.getGamePanel1();
        GamePanel gamePanel2 = game.getGamePanel2();
        gamePanel1.removeMouseListeners();
        gamePanel2.removeMouseListeners();
        gamePanel1.addMouseListeners(state);
        gamePanel2.addMouseListeners(state);
        Gamestate.state = state;
    }

    public void resetBools() {
        buttonState = GLFW.GLFW_RELEASE;
        prevButtonState = GLFW.GLFW_RELEASE;
        mouseOver = false;
    }

    public Gamestate getState() {
        switch (state) {
            case MENU -> game.getAudioPlayer().playSong(AudioPlayer.MENU);
            case PLAYING -> game.getAudioPlayer().playSong(AudioPlayer.WIND);
        }
        return state;
    }

    public int getButtonState() {
        return buttonState;
    }

    public void setButtonState(int buttonState) {
        this.buttonState = buttonState;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public int getPrevButtonState() {
        return prevButtonState;
    }

    public void setPrevButtonState(int prevButtonState) {
        this.prevButtonState = prevButtonState;
    }

    public boolean isMouseOver() {
        return mouseOver;
    }

    public void setMouseOver(boolean isOver) {
        this.mouseOver = isOver;
    }

}
