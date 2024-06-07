package gamestates;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.ArrayList;

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
import entities.Entity;

import static utilz.Constants.Environment.*;
import static utilz.Constants.Dialogue.*;
import static utilz.Constants.PlayerConstants.*;

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

	private boolean paused = false;

	private int leftBorder = (int) ((1-CLOSE_TO_BORDER_HORIZONTAL) * Game.GAME_WIDTH/2);
	private int rightBorder = (int) (CLOSE_TO_BORDER_HORIZONTAL * Game.GAME_WIDTH/2);
	private int upperBorder = (int) ((1-CLOSE_TO_BORDER_VERTICAL) * Game.GAME_HEIGHT/2);
	private int lowerBorder = (int) (CLOSE_TO_BORDER_VERTICAL * Game.GAME_HEIGHT/2);
	private int maxLvlOffsetX, maxLvlOffsetY;

	private BufferedImage backgroundImg, foregroundImg, bigCloud, smallCloud, shipImgs[];
	private BufferedImage[] questionImgs, exclamationImgs;
	private ArrayList<DialogueEffect> dialogEffects = new ArrayList<>();

	private int[] smallCloudsPos;
	private Random rnd = new Random();

	private boolean gameOver;
	private boolean lvlCompleted;
	private boolean gameCompleted;
	private boolean playerDying;

	private float windSpeed = 2.0f;//5.0f;
	public boolean useController = true;
	
	
	public Playing(Game game) {
		super(game);
		initClasses();

		
		bigCloud = LoadSave.GetSpriteAtlas(LoadSave.BIG_CLOUDS);
		smallCloud = LoadSave.GetSpriteAtlas(LoadSave.SMALL_CLOUDS);
		smallCloudsPos = new int[8];
		for (int i = 0; i < smallCloudsPos.length; i++)
			smallCloudsPos[i] = (int) (90 * Game.SCALE) + rnd.nextInt((int) (100 * Game.SCALE));


		loadDialogue();
		calcLvlOffset();
		loadStartLevel();
		player1.resetLvlOffsets();
		player2.resetLvlOffsets();
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

		player1.loadLvlData(levelManager.getCurrentLevel().getLevelData());
		player2.loadLvlData(levelManager.getCurrentLevel().getLevelData());
		player1.setSpawn(levelManager.getCurrentLevel().getPlayerSpawn());
		player2.setSpawn(levelManager.getCurrentLevel().getPlayerSpawn());
	}
	
	public void loadNextLevel() {
		levelManager.setLevelIndex(levelManager.getLevelIndex() + 1);
		levelManager.loadNextLevel();
		player1.setSpawn(levelManager.getCurrentLevel().getPlayerSpawn());
		player2.setSpawn(levelManager.getCurrentLevel().getPlayerSpawn());
		resetAll();
		loadLvlImgs();
		
		player1.loadLvlData(levelManager.getCurrentLevel().getLevelData());
		player2.loadLvlData(levelManager.getCurrentLevel().getLevelData());
		player1.setSpawn(levelManager.getCurrentLevel().getPlayerSpawn());
		player2.setSpawn(levelManager.getCurrentLevel().getPlayerSpawn());
	}

	private void loadStartLevel() {
		enemyManager.loadEnemies(levelManager.getCurrentLevel());
		objectManager.loadObjects(levelManager.getCurrentLevel());
		tetrisTileManager.loadTetrisTiles(levelManager.getCurrentLevel());
		buildingZoneManager.loadBuildingZones(levelManager.getCurrentLevel());
		loadLvlImgs();
		
		player1.loadLvlData(levelManager.getCurrentLevel().getLevelData());
		player2.loadLvlData(levelManager.getCurrentLevel().getLevelData());
		player1.setSpawn(levelManager.getCurrentLevel().getPlayerSpawn());
		player2.setSpawn(levelManager.getCurrentLevel().getPlayerSpawn());
	}
	
	private void loadLvlImgs() {
		if (Files.exists(Paths.get("res/backgrounds/" + (levelManager.getLevelIndex() + 1) + ".png"))) {
			backgroundImg = LoadSave.GetSpriteAtlas("backgrounds/" + (levelManager.getLevelIndex() + 1) + ".png");
		}
		else {
			System.out.println("file does not exist: " + "res/backgrounds/" + (levelManager.getLevelIndex() + 1) + ".png");
			backgroundImg = LoadSave.GetSpriteAtlas("backgrounds/1.png");
		}
		
		if (Files.exists(Paths.get("res/foregrounds/" + (levelManager.getLevelIndex() + 1) + ".png"))) {
			foregroundImg = LoadSave.GetSpriteAtlas("foregrounds/" + (levelManager.getLevelIndex() + 1) + ".png");
		}
		else {
			System.out.println("file does not exist: " + "res/foregrounds/" + (levelManager.getLevelIndex() + 1) + ".png");
			foregroundImg = LoadSave.GetSpriteAtlas("foregrounds/1.png");
		}
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
		
		g.drawImage(backgroundImg, 0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT, null);
		int xLvlOffset, yLvlOffset;
		if (isPlayer1) {
			xLvlOffset = player1.getXLvlOffset();
			yLvlOffset = player1.getYLvlOffset();
		}
		else {
			xLvlOffset = player2.getXLvlOffset();
			yLvlOffset = player2.getYLvlOffset();
		}
		
			
		drawClouds(g, xLvlOffset);
		//if (drawRain)
		//	rain.draw(g, xLvlOffset, yLvlOffset);
		
		if (levelManager.getCurrentLevel().getDrawPolygons())
			levelManager.draw(g, xLvlOffset, yLvlOffset);
		if (levelManager.getCurrentLevel().getDrawForeground())
			g.drawImage(foregroundImg, -xLvlOffset, -yLvlOffset, foregroundImg.getWidth(), foregroundImg.getHeight(), null);

		
		objectManager.draw(g, xLvlOffset, yLvlOffset);
		enemyManager.draw(g, xLvlOffset, yLvlOffset);
		buildingZoneManager.draw(g, xLvlOffset, yLvlOffset);
		tetrisTileManager.draw(g, xLvlOffset, yLvlOffset);
		player1.drawPlayer(g, xLvlOffset, yLvlOffset);
		player2.drawPlayer(g, xLvlOffset, yLvlOffset);
		int xDrawOffset = 0;
		if (isPlayer1)
			player1.drawUI(g);
		else {
			player2.drawUI(g);
			xDrawOffset = -Game.GAME_WIDTH/2;
		}

		if (paused) {
			g.setColor(new Color(0, 0, 0, 150));
			g.fillRect(0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT);
			pauseOverlay.draw(g, isPlayer1);
		} else if (gameOver)
			gameOverOverlay.draw(g, xDrawOffset);
		else if (lvlCompleted)
			levelCompletedOverlay.draw(g, xDrawOffset);
		else if (gameCompleted)
			gameCompletedOverlay.draw(g);
		
		
	}

	private void drawClouds(Graphics g, int xLvlOffset) {
		for (int i = 0; i < 4; i++)
			g.drawImage(bigCloud, i * BIG_CLOUD_WIDTH - (int) (xLvlOffset * 0.3), (int) (204 * Game.SCALE), BIG_CLOUD_WIDTH, BIG_CLOUD_HEIGHT, null);

		for (int i = 0; i < smallCloudsPos.length; i++)
			g.drawImage(smallCloud, SMALL_CLOUD_WIDTH * 4 * i - (int) (xLvlOffset * 0.7), smallCloudsPos[i], SMALL_CLOUD_WIDTH, SMALL_CLOUD_HEIGHT, null);
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
		playerDying = false;


		player1.resetAll();
		player2.resetAll();
		enemyManager.resetAllEnemies();
		objectManager.resetAllObjects();
		tetrisTileManager.resetAllTetrisTiles();
		dialogEffects.clear();
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

	public void setPlayerDying(boolean playerDying) {
		this.playerDying = playerDying;
	}

	public boolean getPaused() {
		return paused;
	}

	public void setPaused(boolean paused) {
		this.paused = paused;
	}
}