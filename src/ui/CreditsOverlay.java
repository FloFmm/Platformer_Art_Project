package ui;

import audio.AudioPlayer;
import gamestates.Gamestate;
import main.Game;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

import static utilz.Constants.ControllerConstants.*;
import static utilz.Constants.ControllerConstants.CONTROLLER_B_BUTTON_ID;
import static utilz.Constants.UI.VolumeButtons.SLIDER_WIDTH;
import static utilz.Constants.UI.VolumeButtons.VOLUME_HEIGHT;

public class CreditsOverlay extends UserInterface{
    private int selectedButtonIndex = 0;

    public CreditsOverlay(Game game) {
        super(game);
    }

    public void loadImages() {
    }

    public void loadButtons() {
        buttons = new MenuButton[1];
        buttons[0] = new MenuButton(Game.GAME_WIDTH / 8, (int) (Game.GAME_HEIGHT * 0.85), 3, Gamestate.MENU, CONTROLLER_B_BUTTON_ID, game);
    }

    public void update() {
        updateButtons();
    }

    public void draw(Graphics g, boolean isPlayer1) {
        drawButtons(g, isPlayer1);
    }

    public void activateButton(MenuButton mb) {
        mb.applyGamestate();
        resetButtons();
    }
}