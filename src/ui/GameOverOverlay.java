package ui;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import org.lwjgl.glfw.GLFW;

import gamestates.Gamestate;
import gamestates.Playing;
import gamestates.Statemethods;
import main.Game;
import utilz.LoadSave;

import static utilz.Constants.ControllerConstants.*;
import static utilz.Constants.UI.Buttons.*;

public class GameOverOverlay implements Statemethods {
    private Playing playing;
    private MenuButton[] buttons = new MenuButton[1];
    private BufferedImage victoryImg, defeatImg;

    public GameOverOverlay(Playing playing) {
        this.playing = playing;
        loadButtons();
        loadImages();
    }

    private void loadImages() {
        victoryImg = LoadSave.GetSpriteAtlas(LoadSave.VICTORY_IMG);
        defeatImg = LoadSave.GetSpriteAtlas(LoadSave.DEFEAT_IMG);
    }

    private void loadButtons() {
        buttons[0] = new MenuButton(Game.GAME_WIDTH / 8, (int) (Game.GAME_HEIGHT * 0.85), 3, Gamestate.MENU, CONTROLLER_B_BUTTON_ID, playing.getGame());
    }

    @Override
    public void update() {
        for (MenuButton mb : buttons) {
            mb.update();
            if (mb.getButtonState() == GLFW.GLFW_RELEASE && mb.getPrevButtonState() == GLFW.GLFW_PRESS) {
                mb.applyGamestate();
                if (mb.getState() == Gamestate.MENU) {
                    playing.resetAll();
                    playing.unpauseGame();
                }
                resetButtons();
            }
        }
    }

    @Override
    public void draw(Graphics g, boolean isPlayer1) {
        int xDrawOffset = 0;
        if (!isPlayer1)
            xDrawOffset = -Game.GAME_WIDTH / 2;

        if (isPlayer1) {
            if (playing.getPlayer1Won())
                g.drawImage(victoryImg, 0, 0, Game.GAME_WIDTH / 2, Game.GAME_HEIGHT, null);
            else
                g.drawImage(defeatImg, 0, 0, Game.GAME_WIDTH / 2, Game.GAME_HEIGHT, null);
        } else {
            if (playing.getPlayer2Won())
                g.drawImage(victoryImg, 0, 0, Game.GAME_WIDTH / 2, Game.GAME_HEIGHT, null);
            else
                g.drawImage(defeatImg, 0, 0, Game.GAME_WIDTH / 2, Game.GAME_HEIGHT, null);
        }

        for (MenuButton mb : buttons)
            mb.draw(g, xDrawOffset);
    }

    private void resetButtons() {
        for (MenuButton mb : buttons)
            mb.resetBools();
    }
}
