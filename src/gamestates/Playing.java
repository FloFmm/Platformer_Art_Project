package gamestates;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;
import java.nio.file.Files;
import java.nio.file.Paths;

import entities.EnemyManager;
import entities.Entity;
import entities.Player;
import entities.TetrisTileManager;
import levels.LevelManager;
import main.Game;
import objects.ObjectManager;
import ui.GameCompletedOverlay;
import ui.GameOverOverlay;
import ui.LevelCompletedOverlay;
import ui.PauseOverlay;
import utilz.LoadSave;
import zones.BuildingZoneManager;
import effects.DialogueEffect;
import effects.Rain;

import static utilz.Constants.Dialogue.*;
import static utilz.Constants.PlayerConstants.*;
import static utilz.Constants.Environment.*;
import static utilz.Constants.UPS_SET;
import static utilz.HelpMethods.*;

public class Playing extends State implements Statemethods {

	private Player player1, player2;
	private LevelManager levelManager;
	private EnemyManager enemyManager;
	private ObjectManager objectManager;
	private TetrisTileManager tetrisTileManager;
	private BuildingZoneManager buildingZoneManager;
	private PauseOverlay pauseOverlay;
	private GameOverOverlay gameOverOverlay;
	private GameCompletedOverlay gameCompletedOverlay;
	private LevelCompletedOverlay levelCompletedOverlay;
	private Rain rain;
	
	// timed events
	private Random random;
	private int gameUpdates = 0;
	private float gameTimeInSeconds = 0;
	private float timeOfLastWindChange = 0;
	private float temperature = 0, tempFromTime = 0, tempFromExplosion = 0, tempFromWindmills = 0;
	private boolean paused = false;
	private float currentCloudYPos, currentWaterYPos, currentDarknessAlpha;

	private int leftBorder = (int) ((1-CLOSE_TO_BORDER_HORIZONTAL) * Game.GAME_WIDTH/2);
	private int rightBorder = (int) (CLOSE_TO_BORDER_HORIZONTAL * Game.GAME_WIDTH/2);
	private int upperBorder = (int) ((1-CLOSE_TO_BORDER_VERTICAL) * Game.GAME_HEIGHT/2);
	private int lowerBorder = (int) (CLOSE_TO_BORDER_VERTICAL * Game.GAME_HEIGHT/2);
	private int maxLvlOffsetX, maxLvlOffsetY;

	private BufferedImage backgroundImg1, backgroundImg2, foregroundImg, skyImg, cloudImg1, cloudImg2, waterImg;
	private BufferedImage[] questionImgs, exclamationImgs;
	private ArrayList<DialogueEffect> dialogEffects = new ArrayList<>();

	private boolean gameOver=false, player1Won=false, player2Won=false;
	private boolean lvlCompleted;
	private boolean gameCompleted;

	private float windSpeed = 2.0f;//5.0f;
	public boolean useController = true;
	
	
	public Playing(Game game) {
		super(game);
		random = new Random();
		initClasses();
		loadDialogue();
		calcLvlOffset();
		loadStartLevel();
		player1.resetLvlOffsets();
		player2.resetLvlOffsets();
		currentWaterYPos = WATER_START_OFFSET_FACTOR*levelManager.getCurrentLevel().getLvlHeight();
		currentCloudYPos = CLOUD_START_OFFSET_FACTOR*levelManager.getCurrentLevel().getLvlHeight();
		currentDarknessAlpha = 0;
	}

	private void loadDialogue() {
		loadDialogueImgs();

		// Load dialogue array with premade objects, that gets activated when needed.
		// This is a simple
		// way of avoiding ConcurrentModificationException error. (Adding to a list that
		// is being looped through.

		for (int i = 0; i < 10; i++)
			dialogEffects.add(new DialogueEffect(0, 0, EXCLAMATION));
		for (int i = 0; i < 10; i++)
			dialogEffects.add(new DialogueEffect(0, 0, QUESTION));

		for (DialogueEffect de : dialogEffects)
			de.deactive();
	}

	private void loadDialogueImgs() {
		BufferedImage qtemp = LoadSave.GetSpriteAtlas(LoadSave.QUESTION_ATLAS);
		questionImgs = new BufferedImage[5];
		for (int i = 0; i < questionImgs.length; i++)
			questionImgs[i] = qtemp.getSubimage(i * 14, 0, 14, 12);

		BufferedImage etemp = LoadSave.GetSpriteAtlas(LoadSave.EXCLAMATION_ATLAS);
		exclamationImgs = new BufferedImage[5];
		for (int i = 0; i < exclamationImgs.length; i++)
			exclamationImgs[i] = etemp.getSubimage(i * 14, 0, 14, 12);
	}

	public void loadLevel(int lvlIndex) {
		resetAll();
		levelManager.setLevelIndex(lvlIndex);
		levelManager.loadNextLevel();
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
	
	public void loadNextLevel() {
		levelManager.setLevelIndex(levelManager.getLevelIndex() + 1);
		player1.setSpawn(levelManager.getCurrentLevel().getPlayerSpawn());
		player2.setSpawn(levelManager.getCurrentLevel().getPlayerSpawn());
		resetAll();
		loadLvlImgs();
		calcLvlOffset();
		
		player1.loadLvlData(levelManager.getCurrentLevel().getLevelData());
		player2.loadLvlData(levelManager.getCurrentLevel().getLevelData());
		player1.setSpawn(levelManager.getCurrentLevel().getPlayerSpawn());
		player2.setSpawn(levelManager.getCurrentLevel().getPlayerSpawn());
		player1.resetAll();
		player2.resetAll();
	}

	private void loadStartLevel() {
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
		buildingZoneManager = new BuildingZoneManager(this);
		player1 = new Player(1, 1, (int) (PLAYER_BASE_WIDTH * Game.SCALE), 
				(int) (PLAYER_BASE_HEIGHT * Game.SCALE), this, true);
		player2 = new Player(1, 1, (int) (PLAYER_BASE_WIDTH * Game.SCALE), 
				(int) (PLAYER_BASE_HEIGHT * Game.SCALE), this, false);
		player1.loadLvlData(levelManager.getCurrentLevel().getLevelData());
		player2.loadLvlData(levelManager.getCurrentLevel().getLevelData());
		player1.setSpawn(levelManager.getCurrentLevel().getPlayerSpawn());
		player2.setSpawn(levelManager.getCurrentLevel().getPlayerSpawn());

		pauseOverlay = new PauseOverlay(this);
		gameOverOverlay = new GameOverOverlay(this);
		levelCompletedOverlay = new LevelCompletedOverlay(this);
		gameCompletedOverlay = new GameCompletedOverlay(this);

		rain = new Rain();
	}

	@Override
	public void update() {

		if (paused)
			pauseOverlay.update();
		else if (lvlCompleted)
			levelCompletedOverlay.update();
		else if (gameCompleted)
			gameCompletedOverlay.update();
		else if (gameOver)
			gameOverOverlay.update();
//		else if (playerDying) {
//			player1.update();
//			player2.update();
//		}
		else {
			updateTimedEvents();
			updateDialogue();
			//if (drawRain)
			//	rain.update(xLvlOffset);
			levelManager.update();
			objectManager.update(levelManager.getCurrentLevel().getLevelData(), player1, player2);
			tetrisTileManager.update();
			buildingZoneManager.update();
			player1.update();
			player2.update();
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
		
		float currentTimeBetweenWindChange = Math.max(-gameTimeInSeconds/TIME_TO_REACH_MAX_TEMP*
				(TIME_BETWEEN_WIND_CHANGE_END + TIME_BETWEEN_WIND_CHANGE_START) + TIME_BETWEEN_WIND_CHANGE_START,
				TIME_BETWEEN_WIND_CHANGE_START);
		float currentMaxWindSpeed = Math.min(gameTimeInSeconds/TIME_TO_REACH_MAX_TEMP*
				(MAX_WIND_SPEED_END - MAX_WIND_SPEED_START) + MAX_WIND_SPEED_START,
				MAX_WIND_SPEED_END);;
		if (gameTimeInSeconds - timeOfLastWindChange > currentTimeBetweenWindChange) {
			timeOfLastWindChange = gameTimeInSeconds;
			windSpeed = random.nextFloat(2 * currentMaxWindSpeed) - currentMaxWindSpeed;
		}
		
		// TODO
		//if (temperature >= MAX_TEMP - 0.1) {
		//	gameOver = true;
		//}		
	}

	private void updateDialogue() {
		for (DialogueEffect de : dialogEffects)
			if (de.isActive())
				de.update();
	}
	
	private void checkCloseToBorder(Player player) {
		int xLvlOffset = player.getXLvlOffset();
		int yLvlOffset = player.getYLvlOffset();
		int playerX = (int) player.getHitbox().x;
		int playerY = (int) player.getHitbox().y;
		int xDiff = playerX - xLvlOffset;
		int yDiff = playerY - yLvlOffset;
		

		if (xDiff > rightBorder)
			xLvlOffset += Math.min(xDiff - rightBorder, MAX_X_LVL_OFFSET_STEP_HORIZONTAL);
		else if (xDiff < leftBorder)
			xLvlOffset += Math.max(xDiff - leftBorder, -MAX_X_LVL_OFFSET_STEP_HORIZONTAL);
		
		if (yDiff > lowerBorder)
			yLvlOffset += Math.min(yDiff - lowerBorder, MAX_X_LVL_OFFSET_STEP_HORIZONTAL);
		else if (yDiff < upperBorder)
			yLvlOffset += Math.max(yDiff - upperBorder, -MAX_X_LVL_OFFSET_STEP_HORIZONTAL);

		xLvlOffset = Math.max(Math.min(xLvlOffset, maxLvlOffsetX), 0);
		yLvlOffset = Math.max(Math.min(yLvlOffset, maxLvlOffsetY), 0);
		player.setXLvlOffset(xLvlOffset);
		player.setYLvlOffset(yLvlOffset);
		
	}


	@Override
	public void draw(Graphics g, boolean isPlayer1) {
		int curLvlWidth = levelManager.getCurrentLevel().getLvlWidth();
		int curLvlHeight = levelManager.getCurrentLevel().getLvlHeight();
		int xLvlOffset, yLvlOffset;
		if (isPlayer1) {
			xLvlOffset = player1.getXLvlOffset();
			yLvlOffset = player1.getYLvlOffset();
		}
		else {
			xLvlOffset = player2.getXLvlOffset();
			yLvlOffset = player2.getYLvlOffset();
		}
		
		// sky
		if (levelManager.getCurrentLevel().getDrawSky())
			g.drawImage(skyImg, - (int) (xLvlOffset * SKY_SPEED), -yLvlOffset, curLvlWidth, curLvlHeight, null);
				
		// background 1
		if (levelManager.getCurrentLevel().getDrawBackground())
			g.drawImage(backgroundImg1, - (int) (xLvlOffset * BG1_SPEED), -yLvlOffset, curLvlWidth, curLvlHeight, null);
		
		// cloud 1 and 2
		if (levelManager.getCurrentLevel().getDrawClouds()) {
			int cloudYPos = (int) linear(temperature, 0, MAX_TEMP, 
					CLOUD_START_OFFSET_FACTOR*levelManager.getCurrentLevel().getLvlHeight(), 
					CLOUD_END_OFFSET_FACTOR*levelManager.getCurrentLevel().getLvlHeight());
			
			if (((int) currentCloudYPos) != cloudYPos) {
				if (currentCloudYPos > cloudYPos)
					currentCloudYPos -= CLOUD_MOVE_SPEED;
				else
					currentCloudYPos += CLOUD_MOVE_SPEED;
			}
			g.drawImage(cloudImg1, - (int) (xLvlOffset * C1_SPEED), (int) (-yLvlOffset + currentCloudYPos), curLvlWidth, curLvlHeight, null);
			g.drawImage(cloudImg2, - (int) (xLvlOffset * C2_SPEED), -yLvlOffset + cloudYPos, curLvlWidth, curLvlHeight, null);
		}
		
		// background 2
		if (levelManager.getCurrentLevel().getDrawBackground())
			g.drawImage(backgroundImg2, - (int) (xLvlOffset * BG2_SPEED), -yLvlOffset, curLvlWidth, curLvlHeight, null);
		
		// darkness 1 
		if (levelManager.getCurrentLevel().getDrawDarkness()) {
			int darknessAlpha = (int) linear(temperature, 0, MAX_TEMP, DARKNESS_START_ALPHA, DARKNESS_END_ALPHA)/2;
			if (((int) currentDarknessAlpha) != darknessAlpha) {
				if (currentDarknessAlpha > darknessAlpha)
					currentDarknessAlpha -= DARKNESS_CHANGE_SPEED;
				else
					currentDarknessAlpha += DARKNESS_CHANGE_SPEED;
			}
			Color black = new Color(40,0,0, (int) currentDarknessAlpha);
			g.setColor(black);
			g.fillRect(0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT);
		}
		
		// building zones
		buildingZoneManager.draw(g, xLvlOffset, yLvlOffset);
				
		// foreground 
		if (levelManager.getCurrentLevel().getDrawForeground())
			g.drawImage(foregroundImg, -xLvlOffset, -yLvlOffset, curLvlWidth, curLvlHeight, null);
		
		// polygons
		if (levelManager.getCurrentLevel().getDrawPolygons())
			levelManager.draw(g, xLvlOffset, yLvlOffset);
		
		
		//if (drawRain)
		//	rain.draw(g, xLvlOffset, yLvlOffset);
		
		// objects
		objectManager.draw(g, xLvlOffset, yLvlOffset);
		
		//entities
		tetrisTileManager.draw(g, xLvlOffset, yLvlOffset);
		enemyManager.draw(g, xLvlOffset, yLvlOffset);
		player1.drawPlayer(g, xLvlOffset, yLvlOffset);
		player2.drawPlayer(g, xLvlOffset, yLvlOffset);
		
		// water 
		if (levelManager.getCurrentLevel().getDrawWater()) {
			int waterYPos = (int) linear(temperature, 0, MAX_TEMP, 
					WATER_START_OFFSET_FACTOR*levelManager.getCurrentLevel().getLvlHeight(), 
					WATER_END_OFFSET_FACTOR*levelManager.getCurrentLevel().getLvlHeight());
			if (((int) currentWaterYPos) != waterYPos) {
				if (currentWaterYPos > waterYPos)
					currentWaterYPos -= WATER_MOVE_SPEED;
				else
					currentWaterYPos += WATER_MOVE_SPEED;
			}
			g.drawImage(waterImg, -xLvlOffset, (int) (-yLvlOffset+currentWaterYPos), curLvlWidth, curLvlHeight, null);
		}
		
		// darkness 2
		if (levelManager.getCurrentLevel().getDrawDarkness()) {
			Color black2 = new Color(40,0,0, (int) (currentDarknessAlpha));
			g.setColor(black2);
			g.fillRect(0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT);
		}
		
		// ui
		int xDrawOffset = 0;
		if (isPlayer1)
			player1.drawUI(g);
		else {
			player2.drawUI(g);
			xDrawOffset = -Game.GAME_WIDTH/2;
		}

		// overlay
		if (paused) {
			g.setColor(new Color(0, 0, 0, 150));
			g.fillRect(0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT);
			pauseOverlay.draw(g, isPlayer1);
		} else if (gameOver)
			gameOverOverlay.draw(g, isPlayer1);
		else if (lvlCompleted)
			levelCompletedOverlay.draw(g, xDrawOffset);
		else if (gameCompleted)
			gameCompletedOverlay.draw(g);
		

	}


	public void setGameCompleted() {
		gameCompleted = true;
	}

	public void resetGameCompleted() {
		gameCompleted = false;
	}

	public void resetAll() {
		gameOver = false;
		paused = false;
		lvlCompleted = false;
		gameTimeInSeconds = 0;
		gameUpdates = 0;
		
		player1.resetAll();
		player2.resetAll();
		enemyManager.resetAllEnemies();
		objectManager.resetAllObjects();
		tetrisTileManager.resetAllTetrisTiles();
		dialogEffects.clear();
		currentWaterYPos = WATER_START_OFFSET_FACTOR*levelManager.getCurrentLevel().getLvlHeight();
		currentCloudYPos = CLOUD_START_OFFSET_FACTOR*levelManager.getCurrentLevel().getLvlHeight();
		currentDarknessAlpha = 0;
		temperature = 0;
		tempFromTime = 0; 
		tempFromExplosion = 0; 
		tempFromWindmills = 0;
		
	}

	public void setGameOver(boolean gameOver) {
		this.gameOver = gameOver;
	}

	public void checkObjectHit(Rectangle2D.Float attackBox) {
		objectManager.checkObjectHit(attackBox);
	}

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

	public void checkPotionTouched(Player player) {
		objectManager.checkObjectTouched(player);
	}

	public void checkSpikesTouched(Entity p) {
		objectManager.checkSpikesTouched(p);
	}

	public void setLevelCompleted(boolean levelCompleted) {
		game.getAudioPlayer().lvlCompleted();
		if (levelManager.getLevelIndex() + 1 >= levelManager.getAmountOfLevels()) {
			// No more levels
			gameCompleted = true;
			levelManager.setLevelIndex(0);
			levelManager.loadNextLevel();
			resetAll();
			return;
		}
		this.lvlCompleted = levelCompleted;
	}

	public void setMaxLvlOffsetX(int lvlOffset) {
		this.maxLvlOffsetX = lvlOffset;
	}
	
	public void setMaxLvlOffsetY(int lvlOffset) {
		this.maxLvlOffsetY = lvlOffset;
	}

	public int getMaxLvlOffsetX() {
		return maxLvlOffsetX;
	}
	
	public int getMaxLvlOffsetY() {
		return maxLvlOffsetY;
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
		this.tempFromWindmills= tempFromWindmills;
	}
	

	public float getGameTimeInSeconds() {
		return gameTimeInSeconds;
	}


	public float getCurrentWaterYPos() {
		return currentWaterYPos;
	}

}