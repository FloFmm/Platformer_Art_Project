package zones;

import static utilz.HelpMethods.*;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;

import entities.Entity;
import entities.TetrisTile;
import gamestates.Playing;
import main.Game;

public class BuildingZone {
	private int[][] lvlData;
	int[][] matrix;
	int buildingZoneIndex;
	protected Rectangle2D.Float hitbox;


	public BuildingZone(int x, int y, int width, int height, int buildingZoneIndex) {
		this.buildingZoneIndex = buildingZoneIndex;
		hitbox = new Rectangle2D.Float(x, y, (int) (width), (int) (height));

	}

	public boolean isTetrisTileColliding(TetrisTile tetrisTile) {
		//matrixAdd(int[][] matrixA, int[][] matrixB , int xIndex, int yIndex)
		
		return false;
	}
	
	public void update(Playing playing) {
	}
	
	public void loadLvlData(int[][] lvlData) {
		this.lvlData = lvlData;
	}
	
	public int getBuildingZoneIndex() {
		return buildingZoneIndex;
	}
	
	public Rectangle2D.Float getHitbox() {
		return hitbox;
	}
	
	protected void drawHitbox(Graphics g, int xLvlOffset, int yLvlOffset) {
		g.setColor(Color.GREEN);
		g.drawRect((int) hitbox.x - xLvlOffset, (int) hitbox.y - yLvlOffset, (int) hitbox.width, (int) hitbox.height);
	}
	}