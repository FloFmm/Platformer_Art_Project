package ui;

import audio.AudioPlayer;
import gamestates.Gamestate;
import gamestates.Playing;
import main.Game;
import org.lwjgl.glfw.GLFW;
import utilz.LoadSave;

import java.awt.*;
import java.awt.image.BufferedImage;

import static utilz.Constants.ControllerConstants.CONTROLLER_A_BUTTON_ID;
import static utilz.Constants.ControllerConstants.CONTROLLER_B_BUTTON_ID;
import static utilz.Constants.UI.Buttons.B_WIDTH;
import static utilz.Constants.UI.VolumeButtons.SLIDER_WIDTH;
import static utilz.Constants.UI.VolumeButtons.VOLUME_HEIGHT;

public class PauseOverlay extends UserInterface {
    private final boolean useVolumeButton = false;
    private BufferedImage backgroundImg, pausedImg, controllerOnlineImg, controllerOfflineImg;
    private int menuX, menuY, menuWidth, menuHeight;
    private VolumeButton volumeButton;

    public PauseOverlay(Game game) {
        super(game);
        loadButtons();
        loadImages();
    }

    public void loadImages() {
        backgroundImg = LoadSave.GetSpriteAtlas(LoadSave.MENU_BACKGROUND);
        controllerOnlineImg = LoadSave.GetSpriteAtlas(LoadSave.CONTROLLER_ONLINE);
        controllerOfflineImg = LoadSave.GetSpriteAtlas(LoadSave.CONTROLLER_OFFLINE);
        pausedImg = LoadSave.GetSpriteAtlas(LoadSave.PAUSED);
        float heightFactor = 1.0f, widthFactor = 1.0f;
        menuWidth = (int) (Game.GAME_WIDTH * widthFactor);
        menuHeight = (int) (Game.GAME_HEIGHT * heightFactor);
        menuX = Game.GAME_WIDTH / 20;
        menuY = 0;
    }

    public void loadButtons() {
        buttons = new MenuButton[2];
        buttons[0] = new MenuButton(Game.GAME_WIDTH / 6, (int) (130 * Game.SCALE), 0, Gamestate.PLAYING, CONTROLLER_A_BUTTON_ID, game);
        buttons[1] = new MenuButton(Game.GAME_WIDTH / 6, (int) (340 * Game.SCALE), 3, Gamestate.MENU, CONTROLLER_B_BUTTON_ID, game);
        if (useVolumeButton)
            volumeButton = new VolumeButton((Game.GAME_WIDTH / 6 - SLIDER_WIDTH / 2), (int) (410 * Game.SCALE), SLIDER_WIDTH, VOLUME_HEIGHT, game);
    }

    public void update() {
        for (MenuButton mb : buttons) {
            mb.update();
            if (mb.getButtonState() == GLFW.GLFW_RELEASE && mb.getPrevButtonState() == GLFW.GLFW_PRESS) {
                activateButton(mb);
            }
        }
        if (useVolumeButton)
            volumeButton.update();
    }

    public void draw(Graphics g, boolean isPlayer1) {
        int xDrawOffset = 0;
        if (!isPlayer1)
            xDrawOffset = -Game.GAME_WIDTH / 2;
        g.drawImage(backgroundImg, menuX + xDrawOffset, menuY, menuWidth, menuHeight, null);
        g.drawImage(pausedImg, Game.GAME_WIDTH / 6 + xDrawOffset - (int) (100 * Game.SCALE), (int) (193 * Game.SCALE), B_WIDTH, B_WIDTH, null);

        if (GLFW.glfwJoystickPresent(GLFW.GLFW_JOYSTICK_1))
            g.drawImage(controllerOnlineImg, (int) (Game.GAME_WIDTH * 0.8 + xDrawOffset),
                    (int) (Game.GAME_WIDTH * 0.01), (int) (Game.GAME_WIDTH * 0.1), (int) (Game.GAME_HEIGHT * 0.1), null);
        else
            g.drawImage(controllerOfflineImg, (int) (Game.GAME_WIDTH * 0.8 + xDrawOffset),
                    (int) (Game.GAME_WIDTH * 0.01), (int) (Game.GAME_WIDTH * 0.1), (int) (Game.GAME_HEIGHT * 0.1), null);

        if (GLFW.glfwJoystickPresent(GLFW.GLFW_JOYSTICK_2))
            g.drawImage(controllerOnlineImg, (int) (Game.GAME_WIDTH * 0.9 + xDrawOffset),
                    (int) (Game.GAME_WIDTH * 0.01), (int) (Game.GAME_WIDTH * 0.1), (int) (Game.GAME_HEIGHT * 0.1), null);
        else
            g.drawImage(controllerOfflineImg, (int) (Game.GAME_WIDTH * 0.9 + xDrawOffset),
                    (int) (Game.GAME_WIDTH * 0.01), (int) (Game.GAME_WIDTH * 0.1), (int) (Game.GAME_HEIGHT * 0.1), null);

        drawButtons(g, isPlayer1);

        if (useVolumeButton)
            volumeButton.draw(g, xDrawOffset);
    }


    public void activateButton(MenuButton mb) {
        if (!game.getPlaying().getPaused())
            return;
        mb.applyGamestate();
        if (mb.getState() == Gamestate.MENU) {
            game.getPlaying().resetAll();
            game.getPlaying().unpauseGame();
        } else if (mb.getState() == Gamestate.PLAYING) {
            game.getPlaying().unpauseGame();
        }
        resetButtons();
    }
}
