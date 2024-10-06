package gamestates;
import main.Game;
import ui.LevelSelectionOverlay;
import utilz.LoadSave;
import java.awt.*;
import java.awt.image.BufferedImage;

public class LevelSelection extends State {
    private BufferedImage backgroundImg;

    public LevelSelection(Game game) {
        super(game);
        ui = new LevelSelectionOverlay(game);
        loadImages();
    }

    public void loadImages() {
        backgroundImg = LoadSave.GetSpriteAtlas(LoadSave.MENU_BACKGROUND_IMG);
    }

    public void update() {
        ui.update();
    }

    public void draw(Graphics g, boolean isPlayer1) {
        int xDrawOffset = 0;
        if (!isPlayer1)
            xDrawOffset = -Game.GAME_WIDTH / 2;
        g.drawImage(backgroundImg, xDrawOffset, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT, null);

        ui.draw(g, isPlayer1);
    }
}