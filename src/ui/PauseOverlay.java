package ui;

import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import org.lwjgl.glfw.GLFW;

import gamestates.Gamestate;
import gamestates.Playing;
import main.Game;
import utilz.LoadSave;
import static utilz.Constants.UI.URMButtons.*;
import static utilz.Constants.ControllerConstants.*;
public class PauseOverlay {

	private Playing playing;
	private BufferedImage backgroundImg;
	private int bgX, bgY, bgW, bgH;
	private UrmButton menuB, unpauseB;

	public PauseOverlay(Playing playing) {
		this.playing = playing;
		loadBackground();
		createUrmButtons();
	}

	private void createUrmButtons() {
		int menuX = (int) (Game.GAME_WIDTH/2 - 1.5*URM_SIZE);
		int unpauseX = (int) (Game.GAME_WIDTH/2 + 0.5*URM_SIZE);
		int bY = (int) (Game.GAME_HEIGHT/2);

		menuB = new UrmButton(menuX, bY, URM_SIZE, URM_SIZE, 2, CONTROLLER_B_BUTTON_ID);
		unpauseB = new UrmButton(unpauseX, bY, URM_SIZE, URM_SIZE, 0, CONTROLLER_H_BUTTON_ID);
	}

	private void loadBackground() {
		backgroundImg = LoadSave.GetSpriteAtlas(LoadSave.PAUSE_BACKGROUND);
		bgW = (int) (backgroundImg.getWidth() * Game.SCALE);
		bgH = (int) (backgroundImg.getHeight() * Game.SCALE);
		bgX = Game.GAME_WIDTH / 2 - bgW / 2;
		bgY = (int) (25 * Game.SCALE);
	}

	public void update() {
		menuB.update();
		unpauseB.update();
		
		if (menuB.getButtonState() == GLFW.GLFW_RELEASE && menuB.getPrevButtonState() == GLFW.GLFW_PRESS) {
			playing.resetAll();
			playing.setGamestate(Gamestate.MENU);
			playing.unpauseGame();
			menuB.resetBools();
			unpauseB.resetBools();
		}
		
		if (unpauseB.getButtonState() == GLFW.GLFW_RELEASE && unpauseB.getPrevButtonState() == GLFW.GLFW_PRESS) {
			playing.unpauseGame();
			menuB.resetBools();
			unpauseB.resetBools();
		}	
	}

	public void draw(Graphics g, int xDrawOffset) {
		// Background
		g.drawImage(backgroundImg, bgX + xDrawOffset, bgY, bgW, bgH, null);

		// UrmButtons
		menuB.draw(g,xDrawOffset);
		unpauseB.draw(g, xDrawOffset);
	}
}
