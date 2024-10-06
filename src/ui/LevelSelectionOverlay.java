package ui;

import audio.AudioPlayer;
import gamestates.Gamestate;
import main.Game;

import java.awt.*;

import static utilz.Constants.ControllerConstants.*;
import static utilz.Constants.UI.VolumeButtons.SLIDER_WIDTH;
import static utilz.Constants.UI.VolumeButtons.VOLUME_HEIGHT;

public class LevelSelectionOverlay extends UserInterface{
    private final boolean useVolumeButton = false;

    public LevelSelectionOverlay(Game game) {
        super(game);
    }

    public void loadImages() {
    }

    public void loadButtons() {
        buttons = new MenuButton[4];
        buttons[0] = new MenuButton(Game.GAME_WIDTH / 6, (int) (130 * Game.SCALE), "LEVEL 1 [A]", 1, Gamestate.PLAYING, CONTROLLER_A_BUTTON_ID, game);
        buttons[1] = new MenuButton(Game.GAME_WIDTH / 6, (int) (200 * Game.SCALE), "LEVEL 2 [X]", 2, Gamestate.PLAYING, CONTROLLER_X_BUTTON_ID, game);
        buttons[2] = new MenuButton(Game.GAME_WIDTH / 6, (int) (270 * Game.SCALE), "LEVEL 3 [Y]", 3, Gamestate.LEVEL_SELECTION, CONTROLLER_Y_BUTTON_ID, game);
        buttons[3] = new MenuButton(Game.GAME_WIDTH / 6, (int) (340 * Game.SCALE), "BACK [B]", -1, Gamestate.MENU, CONTROLLER_B_BUTTON_ID, game);
    }

    public void update() {
        updateButtons();
    }

    public void draw(Graphics g, boolean isPlayer1) {
        drawButtons(g, isPlayer1);
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
