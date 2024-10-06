package ui;

import audio.AudioPlayer;
import gamestates.Gamestate;
import gamestates.Playing;
import main.Game;
import org.lwjgl.glfw.GLFW;
import utilz.LoadSave;

import java.awt.*;
import java.awt.image.BufferedImage;

import static utilz.Constants.ControllerConstants.CONTROLLER_B_BUTTON_ID;

public class GameOverOverlay extends UserInterface{
    private BufferedImage victoryImg, defeatImg;

    public GameOverOverlay(Game game) {
        super(game);
        loadButtons();
        loadImages();
    }

    public void loadImages() {
        victoryImg = LoadSave.GetSpriteAtlas(LoadSave.VICTORY_IMG);
        defeatImg = LoadSave.GetSpriteAtlas(LoadSave.DEFEAT_IMG);
    }

    public void loadButtons() {
        buttons = new MenuButton[1];
        buttons[0] = new MenuButton(Game.GAME_WIDTH / 8, (int) (Game.GAME_HEIGHT * 0.85), 3, Gamestate.MENU, CONTROLLER_B_BUTTON_ID, game);
    }

    public void update() {
        updateButtons();
    }

    public void draw(Graphics g, boolean isPlayer1) {
        int xDrawOffset = 0;
        if (!isPlayer1)
            xDrawOffset = -Game.GAME_WIDTH / 2;

        if (isPlayer1) {
            if (game.getPlaying().getPlayer1Won())
                g.drawImage(victoryImg, 0, 0, Game.GAME_WIDTH / 2, Game.GAME_HEIGHT, null);
            else
                g.drawImage(defeatImg, 0, 0, Game.GAME_WIDTH / 2, Game.GAME_HEIGHT, null);
        } else {
            if (game.getPlaying().getPlayer2Won())
                g.drawImage(victoryImg, 0, 0, Game.GAME_WIDTH / 2, Game.GAME_HEIGHT, null);
            else
                g.drawImage(defeatImg, 0, 0, Game.GAME_WIDTH / 2, Game.GAME_HEIGHT, null);
        }

        drawButtons(g, isPlayer1);
    }

    public void activateButton(MenuButton mb) {
        if (!game.getPlaying().getGameOver())
            return;
        mb.applyGamestate();
        if (mb.getState() == Gamestate.MENU) {
            game.getPlaying().resetAll();
            game.getPlaying().unpauseGame();
        }
        resetButtons();
    }
}
