package levels;

import entities.TetrisTile;
import entities.Tumbleweed;
import main.Game;
import objects.Spike;
import utilz.LoadSave;
import zones.BuildingZone;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

import static utilz.Constants.EnemyConstants.TUMBLE_WEED;
import static utilz.Constants.ObjectConstants.SPIKE;
import static utilz.Constants.PlayerConstants.PLAYER_GREEN_VALUE;
import static utilz.Constants.TetrisTileConstants.*;
import static utilz.HelpMethods.calculateAreaCoveredByEquivalentTiles;

public class Level {

    private BufferedImage img;
    private int[][] lvlData;

    private ArrayList<Tumbleweed> crabs = new ArrayList<>();
    private ArrayList<TetrisTile> tetrisTiles = new ArrayList<>();
    private ArrayList<BuildingZone> buildingZones = new ArrayList<>();
    private ArrayList<Spike> spikes = new ArrayList<>();

    private int lvlTilesWide;
    private int lvlTilesHigh;
    private int lvlWidth;
    private int lvlHeight;


    //private int maxTilesOffset;
    private int maxLvlOffsetX;
    private int maxLvlOffsetY;
    private Point playerSpawn;
    private int buildingZoneIndex = 0;
    private boolean drawForeground = true, drawPolygons = false, drawClouds = true, drawBackground = true, drawSky = true, drawWater = true, drawDarkness = true;
    private BufferedImage backgroundImg1, backgroundImg2, foregroundImg, skyImg, cloudImg1, cloudImg2, waterImg;
    private int lvlId;
    private LevelManager levelManager;
    private boolean isTutorial;

    public Level(BufferedImage img, boolean tutorial, int lvlId, LevelManager levelManager) {
        this.img = img;
        this.levelManager = levelManager;
        lvlData = new int[img.getHeight()][img.getWidth()];
        if (tutorial) {
            this.drawForeground = true;
            this.drawPolygons = false;
            this.drawSky = true;
            this.drawBackground = false;
            this.drawClouds = false;
            this.drawWater = false;
            this.drawDarkness = false;
        } else {
            this.drawForeground = true;
            this.drawPolygons = false;
            this.drawSky = true;
            this.drawBackground = true;
            this.drawClouds = true;
            this.drawWater = true;
            this.drawDarkness = true;
        }
        this.isTutorial = tutorial;
        this.lvlId = lvlId;
        loadLevel();
        calcLvlOffsets();
        loadLvlImgs();
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
                    red = calculateTriangleLengthOrientationPosition(red, x, y, img);
                loadLevelData(red, x, y);
                loadEntities(green, x, y);
                loadObjects(blue, x, y);
            }
    }

    private void loadLvlImgs() {
        if (Thread.currentThread().getContextClassLoader().getResource("layers/" + (lvlId) + "_water.png") != null) {
            waterImg = LoadSave.GetSpriteAtlas("layers/" + (lvlId) + "_water.png");
        } else if (lvlId == 1) {
            System.out.println("file does not exist: " + "layers/" + (lvlId) + "_water.png");
            waterImg = levelManager.getLevelByIndex(lvlId - 1).getWaterImg();
        }

        if (Thread.currentThread().getContextClassLoader().getResource("layers/" + (lvlId) + "_sky.png") != null) {
            skyImg = LoadSave.GetSpriteAtlas("layers/" + (lvlId) + "_sky.png");
        } else if (lvlId == 1) {
            System.out.println("file does not exist: " + "layers/" + (lvlId) + "_sky.png");
            skyImg = levelManager.getLevelByIndex(lvlId - 1).getSkyImg();
        }

        if (Thread.currentThread().getContextClassLoader().getResource("layers/" + (lvlId) + "_cloud1.png") != null) {
            cloudImg1 = LoadSave.GetSpriteAtlas("layers/" + (lvlId) + "_cloud1.png");
        } else if (lvlId == 1) {
            System.out.println("file does not exist: " + "layers/" + (lvlId) + "_cloud1.png");
            cloudImg1 = levelManager.getLevelByIndex(lvlId - 1).getCloudImg1();
        }

        if (Thread.currentThread().getContextClassLoader().getResource("layers/" + (lvlId) + "_cloud2.png") != null) {
            cloudImg2 = LoadSave.GetSpriteAtlas("layers/" + (lvlId) + "_cloud2.png");
        } else if (lvlId == 1) {
            System.out.println("file does not exist: " + "layers/" + (lvlId) + "_cloud2.png");
            cloudImg2 = levelManager.getLevelByIndex(lvlId - 1).getCloudImg2();
        }

        if (Thread.currentThread().getContextClassLoader().getResource("layers/" + (lvlId) + "_background1.png") != null) {
            backgroundImg1 = LoadSave.GetSpriteAtlas("layers/" + (lvlId) + "_background1.png");
        } else if (lvlId == 1) {
            System.out.println("file does not exist: " + "layers/" + (lvlId) + "_background1.png");
            backgroundImg1 = levelManager.getLevelByIndex(lvlId - 1).getBackgroundImg1();
        }

        if (Thread.currentThread().getContextClassLoader().getResource("layers/" + (lvlId) + "_background2.png") != null) {
            backgroundImg2 = LoadSave.GetSpriteAtlas("layers/" + (lvlId) + "_background2.png");
        } else if (lvlId == 1) {
            System.out.println("file does not exist: " + "layers/" + (lvlId) + "_background2.png");
            backgroundImg2 = levelManager.getLevelByIndex(lvlId - 1).getBackgroundImg2();
        }

        if (Thread.currentThread().getContextClassLoader().getResource("layers/" + (lvlId) + "_foreground.png") != null) {
            foregroundImg = LoadSave.GetSpriteAtlas("layers/" + (lvlId) + "_foreground.png");
        } else {
            System.out.println("file does not exist: " + "layers/" + (lvlId) + "_foreground.png");
            foregroundImg = levelManager.getLevelByIndex(lvlId - 1).getForegroundImg();
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
            return yLength * 100 + (red - 250 + 4) * 10 + (y - lowerBound + 1);
        if (yLength == 1) //horizontal
            return xLength * 100 + (red - 250) * 10 + (x - leftBound + 1);

        return -1;
    }

    private void loadLevelData(int redValue, int x, int y) {

        if (redValue >= 50 && !(redValue >= 111 && redValue <= 989))
            lvlData[y][x] = 0;
        else
            lvlData[y][x] = redValue;
        switch (redValue) {
            case 3 -> {
                int[] leftRightUpperLower = calculateAreaCoveredByEquivalentTiles(redValue, x, y, img);
                int leftBound = leftRightUpperLower[0];
                int rightBound = leftRightUpperLower[1];
                int upperBound = leftRightUpperLower[2];
                int lowerBound = leftRightUpperLower[3];
                if (x == leftBound && y == lowerBound) {
                    String type = "windmill";
                    if (buildingZoneIndex == 0) {
                        type = "rocket";
                    }
                    if (lvlId == 2)
                        type += "_tutorial";

                    buildingZones.add(new BuildingZone((int) (x * Game.TILES_SIZE), (int) (y * Game.TILES_SIZE),
                            (rightBound - leftBound + 1) * Game.TILES_SIZE,
                            (upperBound - lowerBound + 1) * Game.TILES_SIZE,
                            buildingZoneIndex,
                            type));
                    buildingZoneIndex += 1;
                }
            }
        }
    }

    private void loadEntities(int greenValue, int x, int y) {
        Random random = new Random();
        float tumbleWeedSizeFactor = random.nextFloat() / 2.0f + 0.5f;
        switch (greenValue) {
            case TUMBLE_WEED ->
                    crabs.add(new Tumbleweed(x * Game.TILES_SIZE, y * Game.TILES_SIZE, tumbleWeedSizeFactor, lvlData));
            case TETRIS_TILE_GREEN_VALUE -> tetrisTiles.add(new TetrisTile(x * Game.TILES_SIZE, y * Game.TILES_SIZE,
                    TETRIS_TILE_WIDTH, TETRIS_TILE_HEIGHT, GetRandomTetrisTileIndex(random), lvlData, false));
            case PLAYER_GREEN_VALUE -> playerSpawn = new Point(x * Game.TILES_SIZE, y * Game.TILES_SIZE);
        }
    }

    private void loadObjects(int blueValue, int x, int y) {
        switch (blueValue) {
            case SPIKE -> spikes.add(new Spike(x * Game.TILES_SIZE, y * Game.TILES_SIZE, SPIKE));
        }
    }

    private void calcLvlOffsets() {
        lvlTilesWide = img.getWidth();
        lvlTilesHigh = img.getHeight();
        lvlWidth = lvlTilesWide * Game.TILES_SIZE;
        lvlHeight = lvlTilesHigh * Game.TILES_SIZE;


        // maxTilesOffset = lvlTilesWide - Game.TILES_IN_WIDTH;
        // maxLvlOffsetX = Game.TILES_SIZE * maxTilesOffset;
        maxLvlOffsetX = lvlWidth - Game.GAME_WIDTH / 2 + 1;
        maxLvlOffsetY = lvlHeight - Game.GAME_HEIGHT + 1;
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

    public ArrayList<Tumbleweed> getCrabs() {
        return crabs;
    }

    public ArrayList<TetrisTile> getTetrisTiles() {
        return tetrisTiles;
    }

    public ArrayList<BuildingZone> getBuildingZones() {
        return buildingZones;
    }

    public ArrayList<Spike> getSpikes() {
        return spikes;
    }

    public boolean getDrawForeground() {
        return drawForeground;
    }

    public boolean getDrawPolygons() {
        return drawPolygons;
    }


    public boolean getDrawClouds() {
        return drawClouds;
    }


    public boolean getDrawBackground() {
        return drawBackground;
    }

    public boolean getDrawDarkness() {
        return drawDarkness;
    }


    public boolean getDrawSky() {
        return drawSky;
    }

    public boolean getDrawWater() {
        return drawWater;
    }

    public int getLvlWidth() {
        return lvlWidth;
    }


    public int getLvlHeight() {
        return lvlHeight;
    }


    public BufferedImage getBackgroundImg1() {
        return backgroundImg1;
    }

    public BufferedImage getBackgroundImg2() {
        return backgroundImg2;
    }

    public BufferedImage getForegroundImg() {
        return foregroundImg;
    }

    public BufferedImage getSkyImg() {
        return skyImg;
    }

    public BufferedImage getCloudImg1() {
        return cloudImg1;
    }

    public BufferedImage getCloudImg2() {
        return cloudImg2;
    }

    public BufferedImage getWaterImg() {
        return waterImg;
    }


    public int getLvlId() {
        return lvlId;
    }

    public boolean getIsTutorial() {
        return isTutorial;
    }

}
