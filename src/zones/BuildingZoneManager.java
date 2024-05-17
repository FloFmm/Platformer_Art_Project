package zones;

import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import entities.Player;
import gamestates.Playing;
import levels.Level;
import objects.Spike;
import utilz.LoadSave;
import static utilz.HelpMethods.*;
import static utilz.Constants.TetrisTileConstants.*;

public class BuildingZoneManager {

	private Playing playing;
	private Level currentLevel;
	private BufferedImage rocketImg, windmillImg;
	
	public BuildingZoneManager(Playing playing) {
		this.playing = playing;
		loadBuildingZoneImgs();
	}
	
	public BuildingZone checkInBuildingZone(Rectangle2D.Float hitbox) {
		for (BuildingZone c : currentLevel.getBuildingZones())
			if (c.getHitbox().intersects(hitbox))
				return c;
		return null;
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
			BufferedImage zoneImg = null;
			if (c.getZoneType() == "rocket") 
				zoneImg = rocketImg;
			else if (c.getZoneType() == "windmill")
				zoneImg = windmillImg;
			
			
			g.drawImage(zoneImg, 
					(int) (c.getHitbox().x - xLvlOffset),
					(int) (c.getHitbox().y - yLvlOffset), 
					(int) c.getHitbox().width, (int) c.getHitbox().height, null);
			c.drawHitbox(g, xLvlOffset, yLvlOffset);
				
		}
	}
	

	private void loadBuildingZoneImgs() {
		rocketImg = LoadSave.GetSpriteAtlas("building_zones/rocket.png");
		//BufferedImage windmill = LoadSave.GetSpriteAtlas("tetris_tiles/windmill.png");
	}
}
