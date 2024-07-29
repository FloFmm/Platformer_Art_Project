package gamestates;


import java.awt.Graphics;
import java.awt.image.BufferedImage;

import org.lwjgl.glfw.GLFW;

import main.Game;
import ui.MenuButton;
import utilz.LoadSave;

import static org.lwjgl.glfw.GLFW.*;
import static utilz.Constants.ControllerConstants.*;

public class Credits extends State implements Statemethods {
	private MenuButton[] buttons = new MenuButton[1];
	private BufferedImage backgroundImg, creditsImg;
	public Credits(Game game) {
		super(game);
		backgroundImg = LoadSave.GetSpriteAtlas(LoadSave.MENU_BACKGROUND_IMG);
		creditsImg = LoadSave.GetSpriteAtlas(LoadSave.CREDITS);
		loadButtons();
	}
	
	private void loadButtons() {
		buttons[0] = new MenuButton(Game.GAME_WIDTH / 8, (int) (Game.GAME_HEIGHT*0.85), 3, Gamestate.MENU, CONTROLLER_B_BUTTON_ID, game);
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

	public void keyPressed(int key) {
		int a = 1;
		int b = 1+2 + a;
	}
}
