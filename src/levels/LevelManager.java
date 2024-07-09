package levels;

import static utilz.HelpMethods.*;
import static utilz.Constants.Environment.*;
import static utilz.Constants.PlayerConstants.*;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import main.Game;
import utilz.LoadSave;


public class LevelManager {

	private Game game;
	private BufferedImage[] levelSprite;
	private ArrayList<Level> levels;
	private int lvlIndex = 0, aniTick, aniIndex;

	public LevelManager(Game game) {
		this.game = game;
		levels = new ArrayList<>();
		buildAllLevels();
	}
	
	public void loadNextLevel() {
		Level newLevel = levels.get(lvlIndex);
		game.getPlaying().getEnemyManager().loadEnemies(newLevel);
		game.getPlaying().getBuildingZoneManager().loadBuildingZones(newLevel);
		game.getPlaying().getTetrisTileManager().loadTetrisTiles(newLevel);
		game.getPlaying().getPlayer1().loadLvlData(newLevel.getLevelData());
		game.getPlaying().getPlayer2().loadLvlData(newLevel.getLevelData());
		game.getPlaying().setMaxLvlOffsetX(newLevel.getMaxLvlOffsetX());
		game.getPlaying().setMaxLvlOffsetY(newLevel.getMaxLvlOffsetY());
		game.getPlaying().getObjectManager().loadObjects(newLevel);
	}

	private void buildAllLevels() {
		BufferedImage[] allLevels = LoadSave.GetAllLevels();
		
		for (int i=0; i<allLevels.length; i++) {
			BufferedImage img = allLevels[i];
			boolean tutorial = i==1;
			levels.add(new Level(img, tutorial, i+1, this));
		}
	}

	public void draw(Graphics g, int xLvlOffset, int yLvlOffset) {
		g.setColor(FLOOR_TILE_COLOR);
		int[][] lvlData = levels.get(lvlIndex).getLevelData();
		for (int j = 0; j < lvlData.length; j++)
			for (int i = 0; i < lvlData[0].length; i++) {
				int index = levels.get(lvlIndex).getSpriteIndex(i, j);
				int x = Game.TILES_SIZE * i - xLvlOffset;
				int y = Game.TILES_SIZE * j - yLvlOffset;
				if (111 <= index && index <= 989) { // triangle
					if  (index%10 == 1) {
						int[][] c = TriangleCoordinatesBaseLongShort(i, j, lvlData);
						
						int xOff = 0;//(int) (-HITBOX_BASE_WIDTH*Game.SCALE*0.75);
						int ori = index/10%10;
						int height = Math.max(Math.abs(c[0][1] - c[1][1]), Math.abs(c[0][1] - c[2][1]));
						if (ori == 1 || ori == 2 || ori == 5 || ori == 6) {
							xOff = -xOff;
						}
						g.drawPolygon(new int[] {c[0][0]- xLvlOffset - xOff, c[1][0]- xLvlOffset - xOff, c[2][0]- xLvlOffset - xOff}, 
								new int[] {c[0][1] - yLvlOffset, c[1][1] - yLvlOffset, c[2][1] - yLvlOffset}, 3);
						g.fillPolygon(new int[] {c[0][0]- xLvlOffset - xOff, c[1][0]- xLvlOffset - xOff, c[2][0]- xLvlOffset - xOff}, 
								new int[] {c[0][1] - yLvlOffset, c[1][1] - yLvlOffset, c[2][1] - yLvlOffset}, 3);
						int m = xOff;
						if (xOff < 0)
							m = 0;
						g.drawRect(c[0][0] - m - xLvlOffset, c[0][1]- yLvlOffset - height, Math.abs(xOff), height);
						g.fillRect(c[0][0] - m - xLvlOffset, c[0][1]- yLvlOffset - height, Math.abs(xOff), height);
					}
				}
				else if (index == 11)
					continue;
				else {
					g.drawRect(x, y, Game.TILES_SIZE, Game.TILES_SIZE);
					g.fillRect(x, y, Game.TILES_SIZE, Game.TILES_SIZE);
				}
					
			}
	}

	public void update() {
		updateWaterAnimation();
	}

	private void updateWaterAnimation() {
		aniTick++;
		if (aniTick >= 40) {
			aniTick = 0;
			aniIndex++;

			if (aniIndex >= 4)
				aniIndex = 0;
		}
	}

	public Level getCurrentLevel() {
		return levels.get(lvlIndex);
	}

	public int getAmountOfLevels() {
		return levels.size();
	}

	public int getLevelIndex() {
		return lvlIndex;
	}
	
	public Level getLevelByIndex(int lvlId) {
		return levels.get(lvlId-1);
	}

	public void setLevelIndex(int lvlIndex) {
		this.lvlIndex = lvlIndex;
	}
	

	public Game getGame() {
		return game;
	}

}
