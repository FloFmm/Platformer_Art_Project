package gamestates;

import main.Game;
import org.lwjgl.glfw.GLFW;
import ui.MenuOverlay;
import utilz.LoadSave;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Menu extends State {
    private BufferedImage backgroundImg, backgroundImgPink, controllerOnlineImg, controllerOfflineImg;
    private int menuX, menuY, menuWidth, menuHeight;

    public Menu(Game game) {
        super(game);
        ui = new MenuOverlay(game);
        loadImages();
    }

    public void loadImages() {
        backgroundImgPink = LoadSave.GetSpriteAtlas(LoadSave.MENU_BACKGROUND_IMG);
        backgroundImg = LoadSave.GetSpriteAtlas(LoadSave.MENU_BACKGROUND);
        controllerOnlineImg = LoadSave.GetSpriteAtlas(LoadSave.CONTROLLER_ONLINE);
        controllerOfflineImg = LoadSave.GetSpriteAtlas(LoadSave.CONTROLLER_OFFLINE);
        float heightFactor = 1.0f, widthFactor = 1.0f;
        menuWidth = (int) (Game.GAME_WIDTH * widthFactor);
        menuHeight = (int) (Game.GAME_HEIGHT * heightFactor);
        menuX = Game.GAME_WIDTH / 20;
        menuY = 0; //Game.GAME_HEIGHT / 2 - menuHeight / 2 - (int) ((1-heightFactor)/3*Game.GAME_HEIGHT);
    }

    public void update() {
        ui.update();
    }

    public void draw(Graphics g, boolean isPlayer1) {
        int xDrawOffset = 0;
        if (!isPlayer1)
            xDrawOffset = -Game.GAME_WIDTH / 2;
        g.drawImage(backgroundImgPink, xDrawOffset, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT, null);
        g.drawImage(backgroundImg, menuX + xDrawOffset, menuY, menuWidth, menuHeight, null);

        if (GLFW.glfwJoystickPresent(GLFW.GLFW_JOYSTICK_1))
            g.drawImage(controllerOnlineImg, (int) (Game.GAME_WIDTH * 0.8 + xDrawOffset),
                    (int) (Game.GAME_WIDTH * 0.01), (int) (Game.GAME_WIDTH * 0.1), (int) (Game.GAME_HEIGHT * 0.1), null);
        else
            g.drawImage(controllerOfflineImg, (int) (Game.GAME_WIDTH * 0.8 + xDrawOffset),
                    (int) (Game.GAME_WIDTH * 0.01), (int) (Game.GAME_WIDTH * 0.1), (int) (Game.GAME_HEIGHT * 0.1), null);

        if (GLFW.glfwJoystickPresent(GLFW.GLFW_JOYSTICK_2))
            g.drawImage(controllerOnlineImg, (int) (Game.GAME_WIDTH * 0.9 + xDrawOffset),
                    (int) (Game.GAME_WIDTH * 0.01), (int) (Game.GAME_WIDTH * 0.1), (int) (Game.GAME_HEIGHT * 0.1), null);
        else
            g.drawImage(controllerOfflineImg, (int) (Game.GAME_WIDTH * 0.9 + xDrawOffset),
                    (int) (Game.GAME_WIDTH * 0.01), (int) (Game.GAME_WIDTH * 0.1), (int) (Game.GAME_HEIGHT * 0.1), null);

        ui.draw(g, isPlayer1);
    }
}