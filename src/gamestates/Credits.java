package gamestates;

import static utilz.Constants.ControllerConstants.CONTROLLER_A_BUTTON_ID;
import static utilz.Constants.ControllerConstants.CONTROLLER_B_BUTTON_ID;
import static utilz.Constants.ControllerConstants.CONTROLLER_X_BUTTON_ID;
import static utilz.Constants.ControllerConstants.CONTROLLER_Y_BUTTON_ID;
import static utilz.Constants.UI.URMButtons.URM_SIZE;
import static utilz.Constants.UI.VolumeButtons.SLIDER_WIDTH;
import static utilz.Constants.UI.VolumeButtons.VOLUME_HEIGHT;

import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import org.lwjgl.glfw.GLFW;

import main.Game;
import ui.MenuButton;
import ui.VolumeButton;
import utilz.LoadSave;

public class Credits extends State implements Statemethods {
	private MenuButton[] buttons = new MenuButton[1];
	private BufferedImage backgroundImg, creditsImg;
	private int bgX, bgY, bgW, bgH;
	private int bgYFloat;
	public Credits(Game game) {
		super(game);
		backgroundImg = LoadSave.GetSpriteAtlas(LoadSave.MENU_BACKGROUND_IMG);
		creditsImg = LoadSave.GetSpriteAtlas(LoadSave.CREDITS);
		
		int menuX = (int) (Game.GAME_WIDTH/2 - 0.5*URM_SIZE);
		int bY = (int) (Game.GAME_HEIGHT/2);

		loadButtons();
	}
	
	private void loadButtons() {
		buttons[0] = new MenuButton(Game.GAME_WIDTH / 6, (int) (Game.GAME_HEIGHT*0.85), 3, Gamestate.MENU, CONTROLLER_B_BUTTON_ID);
	}

	@Override
	public void update() {
		for (MenuButton mb : buttons) {
			mb.update();
			if (mb.getButtonState() == GLFW.GLFW_RELEASE && mb.getPrevButtonState() == GLFW.GLFW_PRESS) {
				mb.applyGamestate();
				resetButtons();
			}
		}
	}

	@Override
	public void draw(Graphics g, boolean isPlayer1) {
		int xDrawOffset = 0;
		if (!isPlayer1)
			xDrawOffset = -Game.GAME_WIDTH/2;
		g.drawImage(backgroundImg, xDrawOffset, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT, null);
		g.drawImage(creditsImg, xDrawOffset, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT, null);

		for (MenuButton mb : buttons)
			mb.draw(g, xDrawOffset);
	}
	
	private void resetButtons() {
		for (MenuButton mb : buttons)
			mb.resetBools();
	}
}
