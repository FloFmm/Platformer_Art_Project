package entities;

import static utilz.HelpMethods.*;
import gamestates.Playing;
import main.Game;

public class BuildingZone extends Entity {
	private int[][] lvlData;
	int[][] matrix;
	int buildingZoneIndex;

	public BuildingZone(int x, int y, int width, int height, int buildingZoneIndex) {
		super(x, y, width, height);
		this.buildingZoneIndex = buildingZoneIndex;
	}

	public void update(Playing playing) {
	}
	
	public void loadLvlData(int[][] lvlData) {
		this.lvlData = lvlData;
		if (!IsEntityOnFloor(hitbox, lvlData))
			inAir = true;
	}
	
	public int getBuildingZoneIndex() {
		return buildingZoneIndex;
	}
}