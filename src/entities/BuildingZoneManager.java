package entities;

import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import gamestates.Playing;
import levels.Level;
import utilz.LoadSave;
import static utilz.HelpMethods.*;
import static utilz.Constants.TetrisTileConstants.*;

public class BuildingZoneManager {

	private Playing playing;
	private Level currentLevel;
	private BufferedImage[] buildingZoneImgs;
	
	public BuildingZoneManager(Playing playing) {
		this.playing = playing;
		loadBuildingZoneImgs();
	}
	
	public void loadBuildingZones(Level level) {
		this.currentLevel = level;
	}

	public void update() {
		for (BuildingZone c : currentLevel.getBuildingZones())
			c.update(playing);
	}

	public void draw(Graphics g, int xLvlOffset, int yLvlOffset) {
		drawBuildingZones(g, xLvlOffset, yLvlOffset);
	}


	private void drawBuildingZones(Graphics g, int xLvlOffset, int yLvlOffset) {
		for (BuildingZone c : currentLevel.getBuildingZones()) {
			g.drawImage(buildingZoneImgs[c.getBuildingZoneIndex()], 
					(int) (c.getHitbox().x - xLvlOffset),
					(int) (c.getHitbox().y - yLvlOffset), 
					TETRIS_TILE_WIDTH, TETRIS_TILE_HEIGHT, null);
			c.drawHitbox(g, xLvlOffset, yLvlOffset);
		}
	}
	

	private void loadBuildingZoneImgs() {
	}
}
