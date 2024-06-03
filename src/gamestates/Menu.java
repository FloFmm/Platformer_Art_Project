package gamestates;

import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import static utilz.Constants.ControllerConstants.*;
import static utilz.Constants.UI.VolumeButtons.SLIDER_WIDTH;
import static utilz.Constants.UI.VolumeButtons.VOLUME_HEIGHT;

import org.lwjgl.glfw.GLFW;

import main.Game;
import ui.MenuButton;
import ui.VolumeButton;
import utilz.LoadSave;

public class Menu extends State implements Statemethods {

	private MenuButton[] buttons = new MenuButton[4];
	private BufferedImage backgroundImg, backgroundImgPink;
	private int menuX, menuY, menuWidth, menuHeight;
	private VolumeButton volumeButton;
	
	public Menu(Game game) {
		super(game);
		loadButtons();
		loadBackground();
		backgroundImgPink = LoadSave.GetSpriteAtlas(LoadSave.MENU_BACKGROUND_IMG);

	}

	private void loadBackground() {
		backgroundImg = LoadSave.GetSpriteAtlas(LoadSave.MENU_BACKGROUND);
		float heightFactor = 0.8f, widthFactor = 0.4f;
		menuWidth = (int) (Game.GAME_WIDTH*widthFactor);
		menuHeight = (int) (Game.GAME_HEIGHT*heightFactor);
		menuX = Game.GAME_WIDTH / 2 - menuWidth / 2;
		menuY = Game.GAME_HEIGHT / 2 - menuHeight / 2 - (int) ((1-heightFactor)/3*Game.GAME_HEIGHT);
	}

	private void loadButtons() {
		buttons[0] = new MenuButton(Game.GAME_WIDTH / 2, (int) (130 * Game.SCALE), 0, Gamestate.PLAYING, CONTROLLER_A_BUTTON_ID);
		buttons[1] = new MenuButton(Game.GAME_WIDTH / 2, (int) (200 * Game.SCALE), 1, Gamestate.OPTIONS, CONTROLLER_X_BUTTON_ID);
		buttons[2] = new MenuButton(Game.GAME_WIDTH / 2, (int) (270 * Game.SCALE), 3, Gamestate.CREDITS, CONTROLLER_Y_BUTTON_ID);
		buttons[3] = new MenuButton(Game.GAME_WIDTH / 2, (int) (340 * Game.SCALE), 2, Gamestate.QUIT, CONTROLLER_B_BUTTON_ID);
		volumeButton = new VolumeButton((Game.GAME_WIDTH/2 - SLIDER_WIDTH/2), (int) (410 * Game.SCALE), SLIDER_WIDTH, VOLUME_HEIGHT, game);
	}

	@Override
	public void update() {
		for (MenuButton mb : buttons) {
			mb.update();
			if (mb.getButtonState() == GLFW.GLFW_RELEASE && mb.getPrevButtonState() == GLFW.GLFW_PRESS) {
				mb.applyGamestate();
				if (mb.getState() == Gamestate.PLAYING)
					game.getAudioPlayer().setLevelSong(game.getPlaying().getLevelManager().getLevelIndex());
				resetButtons();
			}
		}
		
		volumeButton.update();
	}

	@Override
	public void draw(Graphics g, boolean isPlayer1) {
		int xDrawOffset = 0;
		if (!isPlayer1)
			xDrawOffset = -Game.GAME_WIDTH/2;
		g.drawImage(backgroundImgPink, xDrawOffset, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT, null);
		g.drawImage(backgroundImg, menuX + xDrawOffset, menuY, menuWidth, menuHeight, null);

		for (MenuButton mb : buttons)
			mb.draw(g, xDrawOffset);

		volumeButton.draw(g, xDrawOffset);
	}

	private void resetButtons() {
		for (MenuButton mb : buttons)
			mb.resetBools();

	}

	@Override
	public void keyPressed(KeyEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}
	
	@Override
	public void keyTyped(KeyEvent e) {
	}

}