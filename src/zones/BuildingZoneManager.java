package zones;

import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import entities.Player;
import entities.Tumbleweed;
import gamestates.Playing;
import levels.Level;
import main.Game;
import objects.Spike;
import utilz.LoadSave;
import static utilz.HelpMethods.*;
import static utilz.Constants.TetrisTileConstants.*;

public class BuildingZoneManager {

	private Playing playing;
	private Level currentLevel;
	private BufferedImage rocketImg, windmillImg, rocketTutorialImg, windmillTutorialImg;
	private boolean player1Finished = false, player2Finished = false;
	
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
		BuildingZone c;
		ArrayList<BuildingZone> bz = currentLevel.getBuildingZones();
		player2Finished = true;
		for (int i=0; i<bz.size(); i++) {
			c = bz.get(i);
			c.setBuildingZoneManager(this);
			c.update(playing);
			if (c.getFinished())
				c.eventOnFinish();
			if (i==0)
				player1Finished = c.getFinished();
			else
				player2Finished = player2Finished && c.getFinished();
		}
		if (player1Finished) {
			playing.setGameOver(true);
			playing.setPlayer1Won(true);
		}
		if (player2Finished) {
			playing.setPlayer2Won(true);
		}
	}

	public void draw(Graphics g, int xLvlOffset, int yLvlOffset) {
		drawBuildingZones(g, xLvlOffset, yLvlOffset);
	}


	private void drawBuildingZones(Graphics g, int xLvlOffset, int yLvlOffset) {
		for (BuildingZone c : currentLevel.getBuildingZones()) {
			BufferedImage zoneImg = null;
			switch(c.getZoneType()) {
				case("rocket"):
					zoneImg = rocketImg;
					break;
				case("rocket_tutorial"):
					zoneImg = rocketTutorialImg;
					break;
				case("windmill"):
					zoneImg = windmillImg;
					break;
				case("windmill_tutorial"):
					zoneImg = windmillTutorialImg;
					break;
			}
			
			g.drawImage(zoneImg, 
					(int) (c.getHitbox().x - xLvlOffset - c.getHitbox().width),
					(int) (c.getHitbox().y - yLvlOffset), 
					(int) (3 * c.getHitbox().width), (int) c.getHitbox().height + Game.TILES_SIZE, null);
			//c.drawHitbox(g, xLvlOffset, yLvlOffset);
				
		}
	}
	

	private void loadBuildingZoneImgs() {
		rocketImg = LoadSave.GetSpriteAtlas("building_zones/rocket.png");
		windmillImg = LoadSave.GetSpriteAtlas("building_zones/windmill.png");
		rocketTutorialImg = LoadSave.GetSpriteAtlas("building_zones/rocket_tutorial.png");
		windmillTutorialImg = LoadSave.GetSpriteAtlas("building_zones/windmill_tutorial.png");
	}
	
	public Playing getPlaying() {
		return playing;
	}

	public void resetAllBuildingZones() {
		for (BuildingZone c : currentLevel.getBuildingZones())
			c.resetBuildingZone();
	}

}
