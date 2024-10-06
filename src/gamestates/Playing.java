package gamestates;

import audio.AudioPlayer;
import entities.EnemyManager;
import entities.Entity;
import entities.Player;
import entities.TetrisTileManager;
import levels.LevelManager;
import main.Game;
import objects.ObjectManager;
import ui.GameOverOverlay;
import ui.MenuButton;
import ui.PauseOverlay;
import ui.UserInterface;
import utilz.LoadSave;
import zones.BuildingZoneManager;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Random;

import static utilz.Constants.Environment.*;
import static utilz.Constants.PlayerConstants.*;
import static utilz.Constants.UPS_SET;
import static utilz.HelpMethods.linear;

public class Playing extends State {

    private final Random random;
    private final int leftBorder = (int) ((1 - CLOSE_TO_BORDER_HORIZONTAL) * Game.GAME_WIDTH / 2);
    private final int rightBorder = (int) (CLOSE_TO_BORDER_HORIZONTAL * Game.GAME_WIDTH / 2);
    private final int upperBorder = (int) ((1 - CLOSE_TO_BORDER_VERTICAL) * Game.GAME_HEIGHT / 2);
    private final int lowerBorder = (int) (CLOSE_TO_BORDER_VERTICAL * Game.GAME_HEIGHT / 2);
    public boolean useController = true;
    private Player player1, player2;
    private LevelManager levelManager;
    private EnemyManager enemyManager;
    private ObjectManager objectManager;
    private TetrisTileManager tetrisTileManager;
    private BuildingZoneManager buildingZoneManager;
    private UserInterface uiGameOver;

    // timed events
    private boolean loading = true;
    private int imgID = 0;
    private int gameUpdates = 0;
    private float gameTimeInSeconds = 0;
    private float timeOfLastWindChange = 0;
    private float temperature = 0, tempFromTime = 0, tempFromExplosion = 0, tempFromWindmills = 0;
    private boolean paused = false;
    private float currentCloudYPos, currentWaterYPos, currentDarknessAlpha;
    private int maxLvlOffsetX, maxLvlOffsetY;
    private BufferedImage backgroundImg1, backgroundImg2, foregroundImg, skyImg, cloudImg1, cloudImg2, waterImg, loadingImg;
    private boolean gameOver = false, player1Won = false, player2Won = false;
    private float windSpeed = 0.0f;


    public Playing(Game game) {
        super(game);
        random = new Random();
        initClasses();
        calcLvlOffset();
        loadLevel(levelManager.getLevelIndex(), false);
        player1.resetLvlOffsets();
        player2.resetLvlOffsets();
        currentWaterYPos = WATER_START_OFFSET_FACTOR * levelManager.getCurrentLevel().getLvlHeight();
        currentCloudYPos = CLOUD_START_OFFSET_FACTOR * levelManager.getCurrentLevel().getLvlHeight();
        currentDarknessAlpha = 0;
        loadImages();

    }

    private void becomeOP(Player player) {
        player.powerAttack();
        player.changePower(200);
    }

    public void keyPressed(int key) {
        if (gameOver)
            uiGameOver.keyPressed(key);
        else if (paused)
            ui.keyPressed(key);
        else {
            if (!player1.getControllerIsPresent()) {
                switch (key) {
                    case KeyEvent.VK_W -> player1.setJumpRequest(true);
                    case KeyEvent.VK_A -> {
                        player1.setLeft(true);
                        if (loading) {
                            loading = false;
                        }
                    }
                    case KeyEvent.VK_S -> player1.fastFall(true);
                    case KeyEvent.VK_D -> player1.setRight(true);
                    case KeyEvent.VK_E -> player1.grabOrThrow();
                    case KeyEvent.VK_R -> player1.rotateTile(true);
                    case KeyEvent.VK_T ->
                            this.objectManager.addExplosion((int) player1.getX(), (int) player1.getY(), 10, 10);
                    case KeyEvent.VK_J -> player1.changeThrowDirectionKeyboardLeft();
                    case KeyEvent.VK_L -> player1.changeThrowDirectionKeyboardRight();
                    case KeyEvent.VK_K -> player1.changeThrowDirectionKeyboardDown();
                    case KeyEvent.VK_I -> player1.changeThrowDirectionKeyboardUp();
                    case KeyEvent.VK_CIRCUMFLEX -> becomeOP(player1);
                    case KeyEvent.VK_SPACE -> this.setLoading(false);
                    case KeyEvent.VK_ESCAPE -> {
                        paused = !paused;
                        if (paused) game.getAudioPlayer().stopSong();
                    }
                    case KeyEvent.VK_ENTER -> {
                        if (loading) {
                            loading = false;
                        }
                    }
                }
            }
            if (!player2.getControllerIsPresent()) {
                switch (key) {
                    case KeyEvent.VK_UP -> player2.setJumpRequest(true);
                    case KeyEvent.VK_LEFT -> player2.setLeft(true);
                    case KeyEvent.VK_RIGHT -> player2.setRight(true);
                    case KeyEvent.VK_DOWN -> player2.fastFall(true);
                    case KeyEvent.VK_ENTER -> {
                        player2.grabOrThrow();
                        if (loading) {
                            loading = false;
                        }
                    }
                    case KeyEvent.VK_NUMPAD4 -> player2.changeThrowDirectionKeyboardLeft();
                    case KeyEvent.VK_NUMPAD5 -> player2.changeThrowDirectionKeyboardDown();
                    case KeyEvent.VK_NUMPAD6 -> player2.changeThrowDirectionKeyboardRight();
                    case KeyEvent.VK_NUMPAD8 -> player2.changeThrowDirectionKeyboardUp();
                    case KeyEvent.VK_SHIFT -> player2.rotateTile(true);
                    case KeyEvent.VK_NUMPAD0 -> becomeOP(player2);
                    case KeyEvent.VK_SPACE -> loading = false;
                    case KeyEvent.VK_ESCAPE -> {
                        paused = !paused;
                        if (paused) game.getAudioPlayer().stopSong();
                    }
                    case KeyEvent.VK_A -> {
                        if (loading) {
                            loading = false;
                        }
                    }
                }
            }
        }
    }

    public void keyReleased(int key) {
        if (gameOver)
            uiGameOver.keyReleased(key);
        else if (paused)
            ui.keyReleased(key);
        else {
            if (!player1.getControllerIsPresent()) {
                switch (key) {
                    case KeyEvent.VK_W -> player1.setJumpRequest(false);
                    case KeyEvent.VK_A -> player1.setLeft(false);
                    case KeyEvent.VK_S -> player1.fastFall(false);
                    case KeyEvent.VK_D -> player1.setRight(false);
                    case KeyEvent.VK_E -> player1.setGrabOrThrow(false);
                    case KeyEvent.VK_R -> player1.rotateTile(false);
                }
            }
            if (!player2.getControllerIsPresent()) {
                switch (key) {
                    case KeyEvent.VK_SHIFT -> player2.rotateTile(false);
                    case KeyEvent.VK_UP -> player2.setJumpRequest(false);
                    case KeyEvent.VK_LEFT -> player2.setLeft(false);
                    case KeyEvent.VK_RIGHT -> player2.setRight(false);
                    case KeyEvent.VK_ENTER -> player2.setGrabOrThrow(false);
                    case KeyEvent.VK_DOWN -> player2.fastFall(false);
                }
            }
        }
    }

    public void loadLevel(int lvlIndex, boolean resetAll) {
        levelManager.setLevelIndex(lvlIndex);
        if (resetAll) resetAll();
        enemyManager.loadEnemies(levelManager.getCurrentLevel());
        objectManager.loadObjects(levelManager.getCurrentLevel());
        tetrisTileManager.loadTetrisTiles(levelManager.getCurrentLevel());
        buildingZoneManager.loadBuildingZones(levelManager.getCurrentLevel());
        loadLvlImgs();
        calcLvlOffset();
        player1.loadLvlData(levelManager.getCurrentLevel().getLevelData());
        player2.loadLvlData(levelManager.getCurrentLevel().getLevelData());
        player1.setSpawn(levelManager.getCurrentLevel().getPlayerSpawn());
        player2.setSpawn(levelManager.getCurrentLevel().getPlayerSpawn());
        player1.resetAll();
        player2.resetAll();
    }

    private void loadLvlImgs() {
        waterImg = levelManager.getCurrentLevel().getWaterImg();
        skyImg = levelManager.getCurrentLevel().getSkyImg();
        cloudImg1 = levelManager.getCurrentLevel().getCloudImg1();
        cloudImg2 = levelManager.getCurrentLevel().getCloudImg2();
        backgroundImg1 = levelManager.getCurrentLevel().getBackgroundImg1();
        backgroundImg2 = levelManager.getCurrentLevel().getBackgroundImg2();
        foregroundImg = levelManager.getCurrentLevel().getForegroundImg();
    }

    private void calcLvlOffset() {
        maxLvlOffsetX = levelManager.getCurrentLevel().getMaxLvlOffsetX();
        maxLvlOffsetY = levelManager.getCurrentLevel().getMaxLvlOffsetY();
    }

    private void initClasses() {
        levelManager = new LevelManager(game);
        enemyManager = new EnemyManager(this);
        objectManager = new ObjectManager(this);
        tetrisTileManager = new TetrisTileManager(this);
        tetrisTileManager.loadTetrisTiles(levelManager.getCurrentLevel());
        buildingZoneManager = new BuildingZoneManager(this);
        player1 = new Player(1, 1, (int) (PLAYER_BASE_WIDTH * Game.SCALE), (int) (PLAYER_BASE_HEIGHT * Game.SCALE), this, true);
        player2 = new Player(1, 1, (int) (PLAYER_BASE_WIDTH * Game.SCALE), (int) (PLAYER_BASE_HEIGHT * Game.SCALE), this, false);
        player1.loadLvlData(levelManager.getCurrentLevel().getLevelData());
        player2.loadLvlData(levelManager.getCurrentLevel().getLevelData());
        player1.setSpawn(levelManager.getCurrentLevel().getPlayerSpawn());
        player2.setSpawn(levelManager.getCurrentLevel().getPlayerSpawn());


        ui = new PauseOverlay(game);
        uiGameOver = new GameOverOverlay(game);

    }

    public void loadImages() {
        loadingImg = LoadSave.GetSpriteAtlas(LoadSave.LOADING);
    }

    public void update() {
        if (paused) ui.update();
        else if (gameOver && !levelManager.getCurrentLevel().getIsTutorial()) uiGameOver.update();
        else {
            player1.update();
            player2.update();
            if (loading) return;
            updateTimedEvents();
            levelManager.update();
            objectManager.update(levelManager.getCurrentLevel().getLevelData(), player1, player2);
            tetrisTileManager.update();
            buildingZoneManager.update();
            enemyManager.update(levelManager.getCurrentLevel().getLevelData());
            checkCloseToBorder(player1);
            checkCloseToBorder(player2);
        }
    }


    private void updateTimedEvents() {
        gameUpdates += 1;
        gameTimeInSeconds = (float) gameUpdates / UPS_SET;
        tempFromTime = gameTimeInSeconds * MAX_TEMP / TIME_TO_REACH_MAX_TEMP;
        temperature = Math.min(tempFromTime + tempFromExplosion + tempFromWindmills, MAX_TEMP);
        if (temperature < 0) {
            tempFromWindmills -= temperature;
            temperature = 0;
        }

        float currentTimeBetweenWindChange = linear(temperature, 0, MAX_TEMP, TIME_BETWEEN_WIND_CHANGE_START, TIME_BETWEEN_WIND_CHANGE_END);
        float currentMaxWindSpeed = linear(temperature, 0, MAX_TEMP, MAX_WIND_SPEED_START, MAX_WIND_SPEED_END);
        if (gameTimeInSeconds - timeOfLastWindChange > currentTimeBetweenWindChange) {
            timeOfLastWindChange = gameTimeInSeconds;
            windSpeed = random.nextFloat(2 * currentMaxWindSpeed) - currentMaxWindSpeed;
            AudioPlayer ap = game.getAudioPlayer();
            ap.windFactor = linear(Math.abs(windSpeed), 0.0f, MAX_WIND_SPEED_END, AudioPlayer.MIN_WIND_VOLUME_FACTOR, AudioPlayer.MAX_WIND_VOLUME_FACTOR);
            ap.updateSongVolume();
        }

        if (temperature >= MAX_TEMP - 0.1) {
            gameOver = true;
        }
    }

    private void checkCloseToBorder(Player player) {
        int xLvlOffset = player.getXLvlOffset();
        int yLvlOffset = player.getYLvlOffset();
        int playerX = (int) player.getHitbox().x;
        int playerY = (int) player.getHitbox().y;
        int xDiff = playerX - xLvlOffset;
        int yDiff = playerY - yLvlOffset;


        if (xDiff > rightBorder) xLvlOffset += Math.min(xDiff - rightBorder, MAX_X_LVL_OFFSET_STEP_HORIZONTAL);
        else if (xDiff < leftBorder) xLvlOffset += Math.max(xDiff - leftBorder, -MAX_X_LVL_OFFSET_STEP_HORIZONTAL);

        if (yDiff > lowerBorder) yLvlOffset += Math.min(yDiff - lowerBorder, MAX_X_LVL_OFFSET_STEP_HORIZONTAL);
        else if (yDiff < upperBorder) yLvlOffset += Math.max(yDiff - upperBorder, -MAX_X_LVL_OFFSET_STEP_HORIZONTAL);

        xLvlOffset = Math.max(Math.min(xLvlOffset, maxLvlOffsetX), 0);
        yLvlOffset = Math.max(Math.min(yLvlOffset, maxLvlOffsetY), 0);
        player.setXLvlOffset(xLvlOffset);
        player.setYLvlOffset(yLvlOffset);

    }

    public void draw(Graphics g, boolean isPlayer1) {

        int curLvlWidth = levelManager.getCurrentLevel().getLvlWidth();
        int curLvlHeight = levelManager.getCurrentLevel().getLvlHeight();
        int xLvlOffset = player2.getXLvlOffset();
        int yLvlOffset = player2.getYLvlOffset();
        int xDrawOffset = -Game.GAME_WIDTH / 2;
        if (isPlayer1) {
            xLvlOffset = player1.getXLvlOffset();
            yLvlOffset = player1.getYLvlOffset();
            xDrawOffset = 0;
        }

        imgID += 1;
        // loading
        if (loading) {
            g.drawImage(loadingImg, xDrawOffset, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT, null);
            if (imgID == 1 || imgID == 2) {
                return;
            }
        }

        // sky
        if (levelManager.getCurrentLevel().getDrawSky())
            g.drawImage(skyImg, -(int) (xLvlOffset * SKY_SPEED), -yLvlOffset, curLvlWidth, curLvlHeight, null);
        // background 1
        if (levelManager.getCurrentLevel().getDrawBackground())
            g.drawImage(backgroundImg1, -(int) (xLvlOffset * BG1_SPEED), -yLvlOffset, curLvlWidth, curLvlHeight, null);

        // cloud 1 and 2
        if (levelManager.getCurrentLevel().getDrawClouds()) {
            int cloudYPos = (int) linear(temperature, 0, MAX_TEMP, CLOUD_START_OFFSET_FACTOR * levelManager.getCurrentLevel().getLvlHeight(), CLOUD_END_OFFSET_FACTOR * levelManager.getCurrentLevel().getLvlHeight());

            if (((int) currentCloudYPos) != cloudYPos) {
                if (currentCloudYPos > cloudYPos) currentCloudYPos -= CLOUD_MOVE_SPEED;
                else currentCloudYPos += CLOUD_MOVE_SPEED;
            }
            g.drawImage(cloudImg1, -(int) (xLvlOffset * C1_SPEED), (int) (-yLvlOffset + currentCloudYPos), curLvlWidth, curLvlHeight, null);
            g.drawImage(cloudImg2, -(int) (xLvlOffset * C2_SPEED), -yLvlOffset + cloudYPos, curLvlWidth, curLvlHeight, null);
        }

        // background 2
        if (levelManager.getCurrentLevel().getDrawBackground())
            g.drawImage(backgroundImg2, -(int) (xLvlOffset * BG2_SPEED), -yLvlOffset, curLvlWidth, curLvlHeight, null);

        // darkness 1
        if (levelManager.getCurrentLevel().getDrawDarkness()) {
            int darknessAlpha = (int) linear(temperature, 0, MAX_TEMP, DARKNESS_START_ALPHA, DARKNESS_END_ALPHA) / 2;
            if (((int) currentDarknessAlpha) != darknessAlpha) {
                if (currentDarknessAlpha > darknessAlpha) currentDarknessAlpha -= DARKNESS_CHANGE_SPEED;
                else currentDarknessAlpha += DARKNESS_CHANGE_SPEED;
            }
            if (currentDarknessAlpha > 255) currentDarknessAlpha = 255;
            if (currentDarknessAlpha < 0) currentDarknessAlpha = 0;
            Color black = new Color(40, 0, 0, (int) currentDarknessAlpha);
            g.setColor(black);
            g.fillRect(0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT);
        }

        // building zones
        buildingZoneManager.draw(g, xLvlOffset, yLvlOffset);

        // foreground
        if (levelManager.getCurrentLevel().getDrawForeground())
            g.drawImage(foregroundImg, -xLvlOffset, -yLvlOffset, curLvlWidth, curLvlHeight, null);

        // polygons
        if (levelManager.getCurrentLevel().getDrawPolygons()) levelManager.draw(g, xLvlOffset, yLvlOffset);

        // objects
        objectManager.draw(g, xLvlOffset, yLvlOffset);

        //entities
        tetrisTileManager.draw(g, xLvlOffset, yLvlOffset);
        enemyManager.draw(g, xLvlOffset, yLvlOffset);
        player1.drawPlayer(g, xLvlOffset, yLvlOffset);
        player2.drawPlayer(g, xLvlOffset, yLvlOffset);

        // water
        if (levelManager.getCurrentLevel().getDrawWater()) {
            int waterYPos = (int) linear(temperature, 0, MAX_TEMP, WATER_START_OFFSET_FACTOR * levelManager.getCurrentLevel().getLvlHeight(), WATER_END_OFFSET_FACTOR * levelManager.getCurrentLevel().getLvlHeight());
            if (((int) currentWaterYPos) != waterYPos) {
                if (currentWaterYPos > waterYPos) currentWaterYPos -= WATER_MOVE_SPEED;
                else currentWaterYPos += WATER_MOVE_SPEED;
            }
            g.drawImage(waterImg, -xLvlOffset, (int) (-yLvlOffset + currentWaterYPos), curLvlWidth, WATER_HEIGHT, null);
        }

        // darkness 2
        if (levelManager.getCurrentLevel().getDrawDarkness()) {
            Color black2 = new Color(40, 0, 0, (int) (currentDarknessAlpha));
            g.setColor(black2);
            g.fillRect(0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT);
        }

        // ui
        if (isPlayer1) player1.drawUI(g);
        else {
            player2.drawUI(g);
        }

        // overlay
        if (paused) {
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT);
            ui.draw(g, isPlayer1);
        } else if (gameOver && !levelManager.getCurrentLevel().getIsTutorial())
            uiGameOver.draw(g, isPlayer1);

        //loading
        if (loading) {
            g.drawImage(loadingImg, xDrawOffset, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT, null);
        }
    }


    public void resetAll() {
        gameOver = false;
        player1Won = false;
        player2Won = false;
        paused = false;
        gameTimeInSeconds = 0;
        gameUpdates = 0;
        windSpeed = 0.0f;

        player1.resetAll();
        player2.resetAll();
        enemyManager.resetAllEnemies();
        objectManager.resetAllObjects();
        tetrisTileManager.resetAllTetrisTiles();
        buildingZoneManager.resetAllBuildingZones();
        currentWaterYPos = WATER_START_OFFSET_FACTOR * levelManager.getCurrentLevel().getLvlHeight();
        currentCloudYPos = CLOUD_START_OFFSET_FACTOR * levelManager.getCurrentLevel().getLvlHeight();
        currentDarknessAlpha = 0;
        temperature = 0;
        tempFromTime = 0;
        tempFromExplosion = 0;
        tempFromWindmills = 0;
        loading = true;
        imgID = 0;

    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }


    public boolean getGameOver() {return gameOver; }

    public void checkEnemyHit(Rectangle2D.Float attackBox) {
        enemyManager.checkEnemyHit(attackBox);
    }

    public void checkEnemyPlayerHit(boolean isPlayer1Attacking) {
        Player attacker = player2;
        Player defender = player1;
        if (isPlayer1Attacking) {
            attacker = player1;
            defender = player2;
        }
        if (attacker.getAttackBox().intersects(defender.getHitbox())) {
            defender.changeHealth(-20, attacker);
            attacker.selfHurtFromPowerAttack(-20);
            if (defender.getIsCarrying() != null && attacker.getIsCarrying() == null) {
                attacker.setIsCarrying(defender.getIsCarrying());
                attacker.getIsCarrying().setIsCarriedBy(attacker);
                defender.setIsCarrying(null);
            }
        }
    }

    public void checkTetrisTileGrabbed(Rectangle2D.Float grabBox, Player player) {
        tetrisTileManager.checkTetrisTileGrabbed(grabBox, player);
    }

    public void checkSpikesTouched(Entity p) {
        objectManager.checkSpikesTouched(p);
    }

    public int getMaxLvlOffsetX() {
        return maxLvlOffsetX;
    }

    public void setMaxLvlOffsetX(int lvlOffset) {
        this.maxLvlOffsetX = lvlOffset;
    }

    public int getMaxLvlOffsetY() {
        return maxLvlOffsetY;
    }

    public void setMaxLvlOffsetY(int lvlOffset) {
        this.maxLvlOffsetY = lvlOffset;
    }

    public void unpauseGame() {
        paused = false;
    }

    public void windowFocusLost() {
        player1.resetDirBooleans();
        player2.resetDirBooleans();
    }

    public Player getPlayer1() {
        return player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    public EnemyManager getEnemyManager() {
        return enemyManager;
    }

    public ObjectManager getObjectManager() {
        return objectManager;
    }

    public LevelManager getLevelManager() {
        return levelManager;
    }

    public TetrisTileManager getTetrisTileManager() {
        return tetrisTileManager;
    }


    public BuildingZoneManager getBuildingZoneManager() {
        return buildingZoneManager;
    }

    public float getWindSpeed() {
        return windSpeed;
    }

    public boolean getPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public boolean getPlayer1Won() {
        return player1Won;
    }

    public void setPlayer1Won(boolean player1Won) {
        this.player1Won = player1Won;
    }

    public boolean getPlayer2Won() {
        return player2Won;
    }

    public void setPlayer2Won(boolean player2Won) {
        this.player2Won = player2Won;
    }


    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public float getTempFromTime() {
        return tempFromTime;
    }

    public void setTempFromTime(float tempFromTime) {
        this.tempFromTime = tempFromTime;
    }

    public float getTempFromExplosion() {
        return tempFromExplosion;
    }

    public void setTempFromExplosion(float tempFromExplosion) {
        this.tempFromExplosion = tempFromExplosion;
    }

    public float getTempFromWindmills() {
        return tempFromWindmills;
    }

    public void setTempFromWindmills(float tempFromWindmills) {
        this.tempFromWindmills = tempFromWindmills;
    }


    public float getGameTimeInSeconds() {
        return gameTimeInSeconds;
    }


    public float getCurrentWaterYPos() {
        return currentWaterYPos;
    }

    public boolean getLoading() {
        return loading;
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
    }

    public UserInterface getUIGameOver() {
        return uiGameOver;
    }
}