package levels;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

import entities.Crabby;
import entities.TetrisTile;
import main.Game;
import objects.BackgroundTree;
import objects.Cannon;
import objects.GameContainer;
import objects.Grass;
import objects.Potion;
import objects.Spike;
import zones.BuildingZone;

import static utilz.Constants.EnemyConstants.*;
import static utilz.Constants.ObjectConstants.*;
import static utilz.Constants.TetrisTileConstants.*;
import static utilz.Constants.PlayerConstants.*;
import static utilz.HelpMethods.*;

public class Level {

	private BufferedImage img;
	private int[][] lvlData;

	private ArrayList<Crabby> crabs = new ArrayList<>();
	private ArrayList<TetrisTile> tetrisTiles = new ArrayList<>();
	private ArrayList<BuildingZone> buildingZones = new ArrayList<>();
	private ArrayList<Potion> potions = new ArrayList<>();
	private ArrayList<Spike> spikes = new ArrayList<>();
	private ArrayList<GameContainer> containers = new ArrayList<>();
	private ArrayList<Cannon> cannons = new ArrayList<>();
	private ArrayList<BackgroundTree> trees = new ArrayList<>();
	private ArrayList<Grass> grass = new ArrayList<>();

	private int lvlTilesWide;
	private int lvlTilesHigh;
	//private int maxTilesOffset;
	private int maxLvlOffsetX;
	private int maxLvlOffsetY;
	private Point playerSpawn;
	private int buildingZoneIndex = 0;

	public Level(BufferedImage img) {
		this.img = img;
		lvlData = new int[img.getHeight()][img.getWidth()];
		loadLevel();
		calcLvlOffsets();
	}

	private void loadLevel() {

		// Looping through the image colors just once. Instead of one per
		// object/enemy/etc..
		// Removed many methods in HelpMethods class.

		for (int y = 0; y < img.getHeight(); y++)
			for (int x = 0; x < img.getWidth(); x++) {
				Color c = new Color(img.getRGB(x, y));
				int red = c.getRed();
				int green = c.getGreen();
				int blue = c.getBlue();
				
				if (red >= 251 && red <= 254)
					red = calculateTriangleLengthOrientationPosition(red,x,y,img);
				loadLevelData(red, x, y);
				loadEntities(green, x, y);
				loadObjects(blue, x, y);
			}
	}

	private int calculateTriangleLengthOrientationPosition(int red, int x, int y, BufferedImage img) {
		int[] leftRightUpperLower = calculateAreaCoveredByEquivalentTiles(red, x, y, img);
		int leftBound = leftRightUpperLower[0];
		int rightBound = leftRightUpperLower[1];
		int upperBound = leftRightUpperLower[2];
		int lowerBound = leftRightUpperLower[3];
		int xLength = rightBound - leftBound + 1;
		int yLength = upperBound - lowerBound + 1;
		if (xLength == 1) // vertical
			return yLength*100 + (red-250+4)*10 + (y-lowerBound+1);
		if (yLength == 1) //horizontal
			return xLength*100 + (red-250)*10 + (x-leftBound+1);
		
		return -1;
	}

	private void loadLevelData(int redValue, int x, int y) {
		if (redValue >= 50 && !(redValue >= 111 && redValue <= 989))
			lvlData[y][x] = 0;
		else
			lvlData[y][x] = redValue;
		switch (redValue) {
			case 0, 1, 2, 30, 31, 33, 34, 35, 36, 37, 38, 39 -> grass.add(new Grass((int) (x * Game.TILES_SIZE), 
						(int) (y * Game.TILES_SIZE) - Game.TILES_SIZE, getRndGrassType(x)));
			case 3 -> {
				int[] leftRightUpperLower = calculateAreaCoveredByEquivalentTiles(redValue, x, y, img);
				int leftBound = leftRightUpperLower[0];
				int rightBound = leftRightUpperLower[1];
				int upperBound = leftRightUpperLower[2];
				int lowerBound = leftRightUpperLower[3];
				if (x == leftBound && y == lowerBound) {
					int[][] goalMatrix = WINDMILL_MATRIX;
					String type = "windmill";
					if (buildingZoneIndex == 0) {
						goalMatrix = ROCKET_MATRIX;
						type = "rocket";
					}
	
					buildingZones.add(new BuildingZone((int) (x * Game.TILES_SIZE), (int) (y * Game.TILES_SIZE), 
							(rightBound-leftBound+1) * Game.TILES_SIZE, 
							(upperBound-lowerBound+1) * Game.TILES_SIZE, 
							buildingZoneIndex,
							goalMatrix,
							type));
					buildingZoneIndex += 1;
				}
			}
		}
	}

	private int getRndGrassType(int xPos) {
		return xPos % 2;
	}

	private void loadEntities(int greenValue, int x, int y) {
		Random random = new Random();
		switch (greenValue) {
		case CRABBY -> crabs.add(new Crabby(x * Game.TILES_SIZE, y * Game.TILES_SIZE));
		case TETRIS_TILE_GREEN_VALUE -> tetrisTiles.add(new TetrisTile(x * Game.TILES_SIZE, y * Game.TILES_SIZE, 
				TETRIS_TILE_WIDTH, TETRIS_TILE_HEIGHT, random.nextInt(0, NUM_TETRIS_TILES-1), lvlData));
		case PLAYER_GREEN_VALUE -> playerSpawn = new Point(x * Game.TILES_SIZE, y * Game.TILES_SIZE);
		}
	}

	private void loadObjects(int blueValue, int x, int y) {
		switch (blueValue) {
		case RED_POTION, BLUE_POTION -> potions.add(new Potion(x * Game.TILES_SIZE, y * Game.TILES_SIZE, blueValue));
		case BOX, BARREL -> containers.add(new GameContainer(x * Game.TILES_SIZE, y * Game.TILES_SIZE, blueValue));
		case SPIKE -> spikes.add(new Spike(x * Game.TILES_SIZE, y * Game.TILES_SIZE, SPIKE));
		case CANNON_LEFT, CANNON_RIGHT -> cannons.add(new Cannon(x * Game.TILES_SIZE, y * Game.TILES_SIZE, blueValue));
		case TREE_ONE, TREE_TWO, TREE_THREE -> trees.add(new BackgroundTree(x * Game.TILES_SIZE, y * Game.TILES_SIZE, blueValue));
		}
	}

	private void calcLvlOffsets() {
		lvlTilesWide = img.getWidth();
		lvlTilesHigh = img.getHeight();
		
		// maxTilesOffset = lvlTilesWide - Game.TILES_IN_WIDTH;
		// maxLvlOffsetX = Game.TILES_SIZE * maxTilesOffset;
		maxLvlOffsetX = lvlTilesWide*Game.TILES_SIZE - Game.GAME_WIDTH/2 + 1;
		maxLvlOffsetY = lvlTilesHigh*Game.TILES_SIZE - Game.GAME_HEIGHT + 1;
	}

	public int getSpriteIndex(int x, int y) {
		return lvlData[y][x];
	}

	public int[][] getLevelData() {
		return lvlData;
	}

	public int getMaxLvlOffsetX() {
		return maxLvlOffsetX;
	}
	
	public int getMaxLvlOffsetY() {
		return maxLvlOffsetY;
	}

	public Point getPlayerSpawn() {
		return playerSpawn;
	}

	public ArrayList<Crabby> getCrabs() {
		return crabs;
	}
	
	public ArrayList<TetrisTile> getTetrisTiles() {
		return tetrisTiles;
	}

	public ArrayList<BuildingZone> getBuildingZones() {
		return buildingZones;
	}
	
	public ArrayList<Potion> getPotions() {
		return potions;
	}

	public ArrayList<GameContainer> getContainers() {
		return containers;
	}

	public ArrayList<Spike> getSpikes() {
		return spikes;
	}

	public ArrayList<Cannon> getCannons() {
		return cannons;
	}


	public ArrayList<BackgroundTree> getTrees() {
		return trees;
	}

	public ArrayList<Grass> getGrass() {
		return grass;
	}

}
