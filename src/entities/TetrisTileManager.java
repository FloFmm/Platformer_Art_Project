package entities;

import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import gamestates.Playing;
import levels.Level;
import utilz.LoadSave;
import static utilz.HelpMethods.*;
import static utilz.Constants.TetrisTileConstants.*;

public class TetrisTileManager {

	private Playing playing;
	private BufferedImage[][] tetrisTileArr = new BufferedImage[NUM_TETRIS_TILES][4];
	private Level currentLevel;

	public TetrisTileManager(Playing playing) {
		this.playing = playing;
		loadTetrisTileImgs();
	}
	
	public void loadTetrisTiles(Level level) {
		this.currentLevel = level;
	}

	public void update() {
		for (TetrisTile c : currentLevel.getTetrisTiles())
			c.update(playing);
	}

	public void draw(Graphics g, int xLvlOffset, int yLvlOffset) {
		drawTetrisTiles(g, xLvlOffset, yLvlOffset);
	}


	private void drawTetrisTiles(Graphics g, int xLvlOffset, int yLvlOffset) {
		for (TetrisTile c : currentLevel.getTetrisTiles()) {
			g.drawImage(tetrisTileArr[c.getTileIndex()][c.getRotation()], (int) c.getHitbox().x - xLvlOffset,
					(int) c.getHitbox().y - yLvlOffset, TETRIS_TILE_WIDTH, TETRIS_TILE_HEIGHT, null);
			c.drawHitbox(g, xLvlOffset, yLvlOffset);
		}
	}

	public void checkEnemyHit(Rectangle2D.Float attackBox) {
		for (Crabby c : currentLevel.getCrabs())
			if (c.isActive())
				if (attackBox.intersects(c.getHitbox())) {
					c.hurt(20);
					return;
				}
	}

	private void loadTetrisTileImgs() {
        for (int tile = 0; tile < NUM_TETRIS_TILES; tile++) {
        	tetrisTileArr[tile][0] = LoadSave.GetSpriteAtlas("tetris_tiles/" + tile + ".png");
        	tetrisTileArr[tile][1] = rotateImage(tetrisTileArr[tile][0], 90);
        	tetrisTileArr[tile][2] = rotateImage(tetrisTileArr[tile][0], 180);
        	tetrisTileArr[tile][3] = rotateImage(tetrisTileArr[tile][0], 270);
        }
	}
	
	public void resetAllTetrisTiles() {
		for (TetrisTile c : currentLevel.getTetrisTiles())
			c.resetTetrisTile();
	}
}
