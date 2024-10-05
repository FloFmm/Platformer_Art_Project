package gamestates;

import main.Game;
import ui.CreditsOverlay;
import utilz.LoadSave;

import java.awt.*;
import java.awt.image.BufferedImage;


public class Credits extends State {
    private BufferedImage backgroundImg, creditsImg;

    public Credits(Game game) {
        super(game);
        loadImages();
        ui = new CreditsOverlay(game);
    }

    public void loadImages() {
        backgroundImg = LoadSave.GetSpriteAtlas(LoadSave.MENU_BACKGROUND_IMG);
        creditsImg = LoadSave.GetSpriteAtlas(LoadSave.CREDITS);
    }

    public void update() {
        ui.update();
    }

    public void draw(Graphics g, boolean isPlayer1) {
        int xDrawOffset = 0;
        if (!isPlayer1)
            xDrawOffset = -Game.GAME_WIDTH / 2;
        g.drawImage(backgroundImg, xDrawOffset, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT, null);
        g.drawImage(creditsImg, xDrawOffset, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT, null);

        ui.draw(g, isPlayer1);
    }
}
