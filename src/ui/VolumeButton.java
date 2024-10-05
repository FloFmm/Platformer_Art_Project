package ui;

import main.Game;
import org.lwjgl.glfw.GLFW;
import utilz.LoadSave;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.FloatBuffer;

import static utilz.Constants.ControllerConstants.JOYSTICK_DEAD_ZONE;
import static utilz.Constants.UI.VolumeButtons.*;

public class VolumeButton extends PauseButton {

    private final int minX;
    private final int maxX;
    private final Game game;
    private BufferedImage[] imgs;
    private BufferedImage slider;
    private int index = 0;
    private int buttonX;
    private float floatValue = 0f;

    public VolumeButton(int x, int y, int width, int height, Game game) {
        super(x + width / 2, y, VOLUME_WIDTH, height);
        this.game = game;
        bounds.x -= VOLUME_WIDTH / 2;
        buttonX = x + width / 2;
        this.x = x;
        this.width = width;
        minX = x + VOLUME_WIDTH / 2;
        maxX = x + width - VOLUME_WIDTH / 2;
        loadImgs();
    }

    private void loadImgs() {
        BufferedImage temp = LoadSave.GetSpriteAtlas(LoadSave.VOLUME_BUTTONS);
        imgs = new BufferedImage[3];
        for (int i = 0; i < imgs.length; i++)
            imgs[i] = temp.getSubimage(i * VOLUME_DEFAULT_WIDTH, 0, VOLUME_DEFAULT_WIDTH, VOLUME_DEFAULT_HEIGHT);

        slider = temp.getSubimage(3 * VOLUME_DEFAULT_WIDTH, 0, SLIDER_DEFAULT_WIDTH, VOLUME_DEFAULT_HEIGHT);

    }

    public void update() {
        index = 0;
        int change = 0;
        if (GLFW.glfwJoystickPresent(GLFW.GLFW_JOYSTICK_1)) {
            FloatBuffer axes1 = GLFW.glfwGetJoystickAxes(GLFW.GLFW_JOYSTICK_1);
            if (axes1.get(0) > JOYSTICK_DEAD_ZONE) {
                change += 1;
            } else if (axes1.get(0) < -JOYSTICK_DEAD_ZONE) {
                change -= 1;
            }
        }
        if (GLFW.glfwJoystickPresent(GLFW.GLFW_JOYSTICK_2)) {
            FloatBuffer axes2 = GLFW.glfwGetJoystickAxes(GLFW.GLFW_JOYSTICK_2);
            if (axes2.get(0) > JOYSTICK_DEAD_ZONE) {
                change += 1;
            } else if (axes2.get(0) < -JOYSTICK_DEAD_ZONE) {
                change -= 1;
            }
        }

        if (change != 0) {
            changeX(buttonX + change);
            game.getAudioPlayer().setVolume(floatValue);
            index = 2;
        }
    }

    public void draw(Graphics g, int xDrawOffset) {
        g.drawImage(slider, x + xDrawOffset, y, width, height, null);
        g.drawImage(imgs[index], buttonX + xDrawOffset - VOLUME_WIDTH / 2, y, VOLUME_WIDTH, height, null);
    }

    public void changeX(int x) {
        if (x < minX)
            buttonX = minX;
        else if (x > maxX)
            buttonX = maxX;
        else
            buttonX = x;
        updateFloatValue();
        bounds.x = buttonX - VOLUME_WIDTH / 2;

    }

    private void updateFloatValue() {
        float range = maxX - minX;
        float value = buttonX - minX;
        floatValue = value / range;
    }


    public float getFloatValue() {
        return floatValue;
    }
}
