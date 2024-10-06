package ui;

import audio.AudioPlayer;
import gamestates.Gamestate;
import gamestates.Playing;
import gamestates.State;
import main.Game;
import org.lwjgl.glfw.GLFW;
import utilz.LoadSave;

import java.awt.*;
import java.awt.image.BufferedImage;

import static utilz.Constants.ControllerConstants.*;
import static utilz.Constants.ControllerConstants.CONTROLLER_B_BUTTON_ID;
import static utilz.Constants.UI.VolumeButtons.SLIDER_WIDTH;
import static utilz.Constants.UI.VolumeButtons.VOLUME_HEIGHT;

public class MenuOverlay extends UserInterface{
    private final boolean useVolumeButton = false;
    private VolumeButton volumeButton;

    public MenuOverlay(Game game) {
        super(game);
    }

    public void loadImages() {
    }

    public void loadButtons() {
        buttons = new MenuButton[4];
        buttons[0] = new MenuButton(Game.GAME_WIDTH / 6, (int) (130 * Game.SCALE), "PLAY [A]",-1, Gamestate.LEVEL_SELECTION, CONTROLLER_A_BUTTON_ID, game);
        buttons[1] = new MenuButton(Game.GAME_WIDTH / 6, (int) (200 * Game.SCALE), "TUTORIAL [X]", 0, Gamestate.PLAYING, CONTROLLER_X_BUTTON_ID, game);
        buttons[2] = new MenuButton(Game.GAME_WIDTH / 6, (int) (270 * Game.SCALE), "CREDITS [Y]", -1, Gamestate.CREDITS, CONTROLLER_Y_BUTTON_ID, game);
        buttons[3] = new MenuButton(Game.GAME_WIDTH / 6, (int) (340 * Game.SCALE), "EXIT [B]", -1, Gamestate.QUIT, CONTROLLER_B_BUTTON_ID, game);
        if (useVolumeButton)
            volumeButton = new VolumeButton((Game.GAME_WIDTH / 6 - SLIDER_WIDTH / 2), (int) (410 * Game.SCALE), SLIDER_WIDTH, VOLUME_HEIGHT, game);
    }

    public void update() {
        updateButtons();
        if (useVolumeButton)
            volumeButton.update();
    }

    public void draw(Graphics g, boolean isPlayer1) {
        drawButtons(g, isPlayer1);

        int xDrawOffset = 0;
        if (!isPlayer1)
            xDrawOffset = -Game.GAME_WIDTH / 2;
        if (useVolumeButton)
            volumeButton.draw(g, xDrawOffset);
    }

    public void activateButton(MenuButton mb) {
        mb.applyGamestate();
        if (mb.getState() == Gamestate.PLAYING) {
            int rowId = mb.getRowIndex();
            game.getPlaying().loadLevel(rowId, true);
            game.getAudioPlayer().playSong(AudioPlayer.WIND);
        }
        resetButtons();
    }
}
