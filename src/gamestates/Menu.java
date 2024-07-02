package gamestates;

import java.awt.Graphics;
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
	private BufferedImage backgroundImg, backgroundImgPink, controllerOnlineImg, controllerOfflineImg;
	private int menuX, menuY, menuWidth, menuHeight;
	private VolumeButton volumeButton;
	
	public Menu(Game game) {
		super(game);
		loadButtons();
		loadImages();
	}

	private void loadImages() {
		backgroundImgPink = LoadSave.GetSpriteAtlas(LoadSave.MENU_BACKGROUND_IMG);
		backgroundImg = LoadSave.GetSpriteAtlas(LoadSave.MENU_BACKGROUND);
		controllerOnlineImg = LoadSave.GetSpriteAtlas(LoadSave.CONTROLLER_ONLINE);
		controllerOfflineImg = LoadSave.GetSpriteAtlas(LoadSave.CONTROLLER_OFFLINE);
		float heightFactor = 1.0f, widthFactor = 1.0f;
		menuWidth = (int) (Game.GAME_WIDTH*widthFactor);
		menuHeight = (int) (Game.GAME_HEIGHT*heightFactor);
		menuX = Game.GAME_WIDTH / 20;
		menuY = 0; //Game.GAME_HEIGHT / 2 - menuHeight / 2 - (int) ((1-heightFactor)/3*Game.GAME_HEIGHT);
	}

	private void loadButtons() {
		buttons[0] = new MenuButton(Game.GAME_WIDTH / 6, (int) (130 * Game.SCALE), 0, Gamestate.PLAYING, CONTROLLER_A_BUTTON_ID);
		buttons[1] = new MenuButton(Game.GAME_WIDTH / 6, (int) (200 * Game.SCALE), 1, Gamestate.PLAYING, CONTROLLER_X_BUTTON_ID);
		buttons[2] = new MenuButton(Game.GAME_WIDTH / 6, (int) (270 * Game.SCALE), 2, Gamestate.CREDITS, CONTROLLER_Y_BUTTON_ID);
		buttons[3] = new MenuButton(Game.GAME_WIDTH / 6, (int) (340 * Game.SCALE), 3, Gamestate.QUIT, CONTROLLER_B_BUTTON_ID);
		volumeButton = new VolumeButton((Game.GAME_WIDTH/6 - SLIDER_WIDTH/2), (int) (410 * Game.SCALE), SLIDER_WIDTH, VOLUME_HEIGHT, game);
	}

	@Override
	public void update() {
		for (MenuButton mb : buttons) {
			mb.update();
			if (mb.getButtonState() == GLFW.GLFW_RELEASE && mb.getPrevButtonState() == GLFW.GLFW_PRESS) {
				mb.applyGamestate();
				if (mb.getState() == Gamestate.PLAYING) {
					int rowId = mb.getRowIndex();
					game.getPlaying().loadLevel(rowId, true);
					game.getAudioPlayer().setLevelSong(game.getPlaying().getLevelManager().getLevelIndex());
				}
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

		if (GLFW.glfwJoystickPresent(GLFW.GLFW_JOYSTICK_1))
			g.drawImage(controllerOnlineImg, (int) (Game.GAME_WIDTH*0.8 + xDrawOffset), 
					(int) (Game.GAME_WIDTH*0.01), (int) (Game.GAME_WIDTH*0.1), (int) (Game.GAME_HEIGHT*0.1), null);
		else
			g.drawImage(controllerOfflineImg, (int) (Game.GAME_WIDTH*0.8 + xDrawOffset), 
					(int) (Game.GAME_WIDTH*0.01), (int) (Game.GAME_WIDTH*0.1), (int) (Game.GAME_HEIGHT*0.1), null);
		
		if (GLFW.glfwJoystickPresent(GLFW.GLFW_JOYSTICK_2))
			g.drawImage(controllerOnlineImg, (int) (Game.GAME_WIDTH*0.9 + xDrawOffset), 
					(int) (Game.GAME_WIDTH*0.01), (int) (Game.GAME_WIDTH*0.1), (int) (Game.GAME_HEIGHT*0.1), null);
		else
			g.drawImage(controllerOfflineImg, (int) (Game.GAME_WIDTH*0.9 + xDrawOffset), 
					(int) (Game.GAME_WIDTH*0.01), (int) (Game.GAME_WIDTH*0.1), (int) (Game.GAME_HEIGHT*0.1), null);
	
		for (MenuButton mb : buttons)
			mb.draw(g, xDrawOffset);

		volumeButton.draw(g, xDrawOffset);
	}

	private void resetButtons() {
		for (MenuButton mb : buttons)
			mb.resetBools();

	}
}