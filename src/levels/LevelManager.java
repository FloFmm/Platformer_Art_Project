package levels;

import static utilz.Constants.ObjectConstants.BLUE_POTION;
import static utilz.Constants.ObjectConstants.RED_POTION;
import static utilz.HelpMethods.*;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import main.Game;
import utilz.LoadSave;


public class LevelManager {

	private Game game;
	private BufferedImage[] levelSprite;
	private BufferedImage[] waterSprite;
	private ArrayList<Level> levels;
	private int lvlIndex = 0, aniTick, aniIndex;

	public LevelManager(Game game) {
		this.game = game;
		importOutsideSprites();
		createWater();
		levels = new ArrayList<>();
		buildAllLevels();
	}

	private void createWater() {
		waterSprite = new BufferedImage[5];
		BufferedImage img = LoadSave.GetSpriteAtlas(LoadSave.WATER_TOP);
		for (int i = 0; i < 4; i++)
			waterSprite[i] = img.getSubimage(i * 32, 0, 32, 32);
		waterSprite[4] = LoadSave.GetSpriteAtlas(LoadSave.WATER_BOTTOM);
	}

	public void loadNextLevel() {
		Level newLevel = levels.get(lvlIndex);
		game.getPlaying().getEnemyManager().loadEnemies(newLevel);
		game.getPlaying().getPlayer1().loadLvlData(newLevel.getLevelData());
		game.getPlaying().getPlayer2().loadLvlData(newLevel.getLevelData());
		game.getPlaying().setMaxLvlOffsetX(newLevel.getMaxLvlOffsetX());
		game.getPlaying().setMaxLvlOffsetY(newLevel.getMaxLvlOffsetY());
		game.getPlaying().getObjectManager().loadObjects(newLevel);
	}

	private void buildAllLevels() {
		BufferedImage[] allLevels = LoadSave.GetAllLevels();
		for (BufferedImage img : allLevels)
			levels.add(new Level(img));
	}

	private void importOutsideSprites() {
		BufferedImage img = LoadSave.GetSpriteAtlas(LoadSave.LEVEL_ATLAS);
		levelSprite = new BufferedImage[48];
		for (int j = 0; j < 4; j++)
			for (int i = 0; i < 12; i++) {
				int index = j * 12 + i;
				// TODO
				levelSprite[index] = img.getSubimage(i * 32, j * 32, 32, 32);
			}
	}

	public void draw(Graphics g, int xLvlOffset, int yLvlOffset) {
		int[][] lvlData = levels.get(lvlIndex).getLevelData();
		for (int j = 0; j < lvlData.length; j++)
			for (int i = 0; i < lvlData[0].length; i++) {
				int index = levels.get(lvlIndex).getSpriteIndex(i, j);
				int x = Game.TILES_SIZE * i - xLvlOffset;
				int y = Game.TILES_SIZE * j - yLvlOffset;
				if (111 <= index && index <= 989) { // triangle
					if  (index%10 == 1) {
						int[][] c = TriangleCoordinatesBaseLongShort(i, j, lvlData);
						g.drawPolygon(new int[] {c[0][0]- xLvlOffset, c[1][0]- xLvlOffset, c[2][0]- xLvlOffset}, 
								new int[] {c[0][1] - yLvlOffset, c[1][1] - yLvlOffset, c[2][1] - yLvlOffset}, 3);
						g.fillPolygon(new int[] {c[0][0]- xLvlOffset, c[1][0]- xLvlOffset, c[2][0]- xLvlOffset}, 
								new int[] {c[0][1] - yLvlOffset, c[1][1] - yLvlOffset, c[2][1] - yLvlOffset}, 3);
					}
				}
				else if (index == 48)
					g.drawImage(waterSprite[aniIndex], x, y, Game.TILES_SIZE, Game.TILES_SIZE, null);
				else if (index == 49)
					g.drawImage(waterSprite[4], x, y, Game.TILES_SIZE, Game.TILES_SIZE, null);
				else {
					//System.out.println(i);
					//System.out.println(j);
					// System.out.println(index);
					g.drawImage(levelSprite[index], x, y, Game.TILES_SIZE, Game.TILES_SIZE, null);
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

	public void setLevelIndex(int lvlIndex) {
		this.lvlIndex = lvlIndex;
	}
}
