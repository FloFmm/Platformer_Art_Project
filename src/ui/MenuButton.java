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
    private BufferedImage pressedEmptyButton, emptyButton;
    private int buttonState = GLFW.GLFW_RELEASE, prevButtonState = GLFW.GLFW_RELEASE;
    private Rectangle bounds;
    private boolean mouseOver;
    private String buttonText;

    public MenuButton(int xPos, int yPos, String buttonText, int rowIndex, Gamestate state, int controllerButtonId, Game game) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.buttonText = buttonText;
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
        pressedEmptyButton = LoadSave.GetSpriteAtlas(LoadSave.PRESSED_EMPTY_BUTTON);
        emptyButton = LoadSave.GetSpriteAtlas(LoadSave.EMPTY_BUTTON);
    }

    public void draw(Graphics g, int xDrawOffset) {
        BufferedImage img = emptyButton;
        if (buttonState == GLFW.GLFW_PRESS)
            img = pressedEmptyButton;


        g.drawImage(img, xPos - xOffsetCenter + xDrawOffset, yPos, B_WIDTH, B_HEIGHT, null);

        // draw text on button
        Graphics2D g2d = (Graphics2D) g;
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 100));
        g2d.setColor(Color.WHITE);
        //g2d.drawString("PLAY [A]", xPos - xOffsetCenter + xDrawOffset, (int)(yPos + B_HEIGHT*0.75 + pressedYPushDown));
        drawCenteredString(g2d, buttonText, xDrawOffset);
    }

    public void drawCenteredString(Graphics g2d, String text, int xDrawOffset) {
        Rectangle b = new Rectangle(bounds.x, bounds.y, bounds.width, (int)(0.8*bounds.height));
        if (buttonState == GLFW.GLFW_PRESS)
            b = new Rectangle(bounds.x, bounds.y + (int)(0.2*bounds.height), bounds.width, (int)(0.8*bounds.height));

        int fontSize = 100; // Start with a large font size
        Font font = g2d.getFont().deriveFont((float) fontSize);
        FontMetrics metrics = g2d.getFontMetrics(font);

        // Reduce the font size until the text fits the width of the rectangle
        while (metrics.stringWidth(text) > b.width) {
            fontSize--;
            font = g2d.getFont().deriveFont((float) fontSize);
            metrics = g2d.getFontMetrics(font);
        }
        fontSize -= 5;
        font = g2d.getFont().deriveFont((float) fontSize);
        metrics = g2d.getFontMetrics(font);

        // Find the X coordinate for the text
        int x = b.x + (b.width - metrics.stringWidth(text)) / 2;
        // Find the Y coordinate for the text (centered vertically within the rectangle)
        int y = b.y + ((b.height - metrics.getHeight()) / 2) + metrics.getAscent();

        g2d.setFont(font);
        g2d.drawString(text, x + xDrawOffset, y);
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
