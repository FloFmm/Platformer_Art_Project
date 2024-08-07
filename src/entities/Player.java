package entities;

import static utilz.Constants.PlayerConstants.*;
import static utilz.HelpMethods.*;
import static utilz.Constants.UPS_SET;
import static utilz.Constants.GRAVITY;
import static utilz.Constants.ANI_SPEED;
import static utilz.Constants.Directions.*;
import static utilz.Constants.TetrisTileConstants.*;
import static utilz.Constants.ControllerConstants.*;
import static utilz.Constants.UI.*;
import static utilz.Constants.Environment.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import gamestates.Playing;
import main.Game;
import utilz.Constants;
import utilz.LoadSave;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import org.lwjgl.glfw.GLFW;


public class Player extends Entity {
	
	private BufferedImage[][] animations;
	private boolean moving = false, attacking = false;
	private boolean left, right, jump, grabOrThrow = false;
	protected Rectangle2D.Float grabBox;
	private TetrisTile isCarrying;
	private float startTimeInAir;
	private int[][] lvlData;
	private float xDrawOffset = (width-HITBOX_BASE_WIDTH*Game.SCALE)/2;//21 * Game.SCALE;
	private float yDrawOffset = (height-HITBOX_BASE_HEIGHT*Game.SCALE)/2;//4 * Game.SCALE;
	private int xLvlOffset, yLvlOffset;
	
	// Jumping / Gravity
	private float jumpSpeed = PLAYER_JUMP_SPEED;

    // StatusBarUI
	private BufferedImage statusBarImg, windsockImg1, windsockImg2, windsockImg3, tempScaleImg;
	
	private final Color tempScaleBackgroundColor = BACKGROUND_GREY;

    private final int statusBarWidth = (int) (128 * Game.SCALE);

    private final Color healthBarBackgroundColor = BACKGROUND_GREY;
	private final Color healthBarColor = BASE_GREY;
	private final int healthBarWidth = statusBarWidth*800/1024;
    private int healthWidth = healthBarWidth;

	private final Color powerBarBackgroundColor = BACKGROUND_GREY;
	private final Color powerBarColor = BASE_GREY;
	private final int powerBarWidth = statusBarWidth*800/1024;
    private int powerWidth = powerBarWidth;
	private final int powerMaxValue = 200;
	private int powerValue = powerMaxValue;

	private int flipX = 0;
	private int flipW = 1;
	private boolean attackChecked;
	private final Playing playing;
	private int tileY = 0;

	private boolean powerAttackActive=false, selfHurt = false;
	private int powerAttackTick;
    private int powerGrowTick;
	
	// grab and throw
	private float throwHeightInSmallTiles = (float) TETRIS_TILE_MAX_THROW_HEIGHT_IN_SMALL_TILES /2, throwWidthInSmallTiles = (float) TETRIS_TILE_MAX_THROW_WIDTH_IN_SMALL_TILES /2;
	private boolean throwActive=false;
	private boolean drawThrowArc = false;
	//controller
	private final int[] buttonStates = new int[NUM_BUTTONS];
	private final int[] prevButtonStates = new int[NUM_BUTTONS];
	private final float[] pushDownStartTimes = new float[NUM_BUTTONS];
	private final float[] buttonLastPressed = new float[NUM_BUTTONS];
	private final int controllerID;
    private int grabOrThrowControllerState = GLFW.GLFW_RELEASE;
    private int rotateControllerState = GLFW.GLFW_RELEASE;
    private int pauseControllerState = GLFW.GLFW_RELEASE;
    private int dashControllerState = GLFW.GLFW_RELEASE;
	private boolean keyboardRotatedTile = false;

	private int jumpsDone = 0;
	private boolean resetJump = true;
	private boolean fasterFall = false;
	private Direction lastDirection = Direction.LEFT;
	private DashState dashState = DashState.NOTHING;
	private long dashStartTime;

	private final boolean isPlayer1;

	public Player(float x, float y, int width, int height, Playing playing, boolean isPlayer1) {
		super(x, y, width, height);
		this.isPlayer1 = isPlayer1;
		if (isPlayer1) 
			controllerID = GLFW.GLFW_JOYSTICK_1;
		else
			controllerID = GLFW.GLFW_JOYSTICK_2;
		this.playing = playing;
		this.state = IDLE;
		this.maxHealth = 100;
		this.currentHealth = maxHealth;
		this.walkSpeed = PLAYER_WALKSPEED;
		loadAnimations();
		initHitbox(HITBOX_BASE_WIDTH, HITBOX_BASE_HEIGHT);
		initGrabBox(GRABBOX_BASE_WIDTH, GRABBOX_BASE_HEIGHT);
		initAttackBox(ATTACKBOX_BASE_WIDTH, ATTACKBOX_BASE_HEIGHT);
		
		for (int i=0; i<NUM_BUTTONS; i+=1) {
			buttonStates[i] = GLFW.GLFW_RELEASE;
			prevButtonStates[i] = GLFW.GLFW_RELEASE;
		}
	}

	public void setSpawn(Point spawn) {
		this.x = spawn.x;
		this.y = spawn.y;
		hitbox.x = x;
		hitbox.y = y;
	}

	public float getX(){
		return this.x;
	}

	public float getY(){
		return this.y;
	}


	private void initAttackBox(int width, int height) {
		attackBox = new Rectangle2D.Float(x, y, (int) (width * Game.SCALE), (int) (height * Game.SCALE));
	}
	
	protected void initGrabBox(int width, int height) {
		grabBox = new Rectangle2D.Float(x, y, (int) (width * Game.SCALE), (int) (height * Game.SCALE));
	}

	private boolean handleDeadBody(){
		if (currentHealth <= 0) {
			if (state != DEAD) {
				state = DEAD;
				aniTick = 0;
				aniIndex = 0;

				// Check if player died in air
				if (!IsEntityOnFloor(hitbox, lvlData)) {
					inAir = true;
					//airSpeed = 0;
				}
			} else if (aniIndex == GetSpriteAmount(DEAD) - 1 && aniTick >= ANI_SPEED - 1) {
				//playing.getGame().getAudioPlayer().stopSong();
				resetAtDeath();
			} else {
				updateAnimationTick();

				// Fall if in air
				if (inAir)
					if (CanMoveHere(hitbox.x, hitbox.y + airSpeed, hitbox.width, hitbox.height, lvlData)) {
						hitbox.y += airSpeed;
						airSpeed += GRAVITY;
					} else
						inAir = false;
			}
			return true;
		}
		return false;
	}

	public void update() {
		if(playing.getLoading()){return;}
		updateHealthBar();
		if (handleDeadBody()){return;};

		boolean startInAir = inAir;
		updateControllerInputs();

		updatePowerBar();
		updateAttackBox();
		updateGrabBox();

		if (state == HIT) {
			if (aniIndex <= GetSpriteAmount(state) - 3)
				pushBack(pushBackDir, lvlData, 1.25f);
			updatePushBackDrawOffset();
		} else{
			updatePos();
		}

		checkSpikesTouched();
		checkInsideWater();
		if (moving) {
			tileY = (int) (hitbox.y / Game.TILES_SIZE);
			if (powerAttackActive) {
				powerAttackTick++;
				if (powerAttackTick >= 35) {
					powerAttackTick = 0;
					powerAttackActive = false;
				}
			}
		}

		if (attacking || powerAttackActive){
			checkAttack();
		}

		updateAnimationTick();
		setAnimation();
		
		if (!startInAir && inAir){
			startTimeInAir = playing.getGameTimeInSeconds(); // if air status changed in this update loop then start timer
		}
	}
	
	private void updateControllerInputs() {
		boolean controllerIsPresent = GLFW.glfwJoystickPresent(controllerID);
		if (controllerIsPresent) {
			ByteBuffer buttons = GLFW.glfwGetJoystickButtons(controllerID);
			for (int i = 0; i<NUM_BUTTONS; i+=1) {
				controllerIsPresent = GLFW.glfwJoystickPresent(controllerID);
				prevButtonStates[i] = buttonStates[i];
				if (!controllerIsPresent)
					return;
				buttonStates[i] = buttons.get(i);
				if (buttonStates[i] == GLFW.GLFW_PRESS && prevButtonStates[i] == GLFW.GLFW_RELEASE) {
		        	pushDownStartTimes[i] = playing.getGameTimeInSeconds();
		        }
				if (buttonStates[i] == GLFW.GLFW_PRESS)
					buttonLastPressed[i] = playing.getGameTimeInSeconds();
			}
			// continue on loading screen
			if (playing.getLoading() ) {
				if (prevButtonStates[CONTROLLER_A_BUTTON_ID] == GLFW.GLFW_PRESS && buttonStates[CONTROLLER_A_BUTTON_ID] == GLFW.GLFW_RELEASE)
					playing.setLoading(false);
				return;
			}
			
			// jump
	        if (buttonStates[CONTROLLER_A_BUTTON_ID] == GLFW.GLFW_PRESS) {
	        	jump = true;
	        }
	        if (buttonStates[CONTROLLER_A_BUTTON_ID] == GLFW.GLFW_RELEASE) {
	        	jump = false;
	        	
	        }
	        
	        // dash
            int prevDashControllerState = dashControllerState;
	        dashControllerState = buttons.get(CONTROLLER_L_BUTTON_ID);
	        if (dashControllerState == GLFW.GLFW_RELEASE && prevDashControllerState == GLFW.GLFW_PRESS) {
	        	powerAttack();
	        }
	        
	        // grab or throw
            int prevGrabOrThrowControllerState = grabOrThrowControllerState;
	        grabOrThrowControllerState = buttons.get(CONTROLLER_X_BUTTON_ID);
	        if (grabOrThrowControllerState == GLFW.GLFW_RELEASE && prevGrabOrThrowControllerState == GLFW.GLFW_PRESS) {
				grabOrThrow = false;
				grabOrThrow();
	        }
	        
	        // throw direction
	        int[] directions = {CONTROLLER_LEFT_BUTTON_ID, CONTROLLER_RIGHT_BUTTON_ID, CONTROLLER_UP_BUTTON_ID, CONTROLLER_DOWN_BUTTON_ID};
	        for (int i : directions) {
		        if (buttonStates[i] == GLFW.GLFW_RELEASE && prevButtonStates[i] == GLFW.GLFW_PRESS) {
		        	changeThrowDirection(i);
		        } 
		        else if (buttonStates[i] == GLFW.GLFW_PRESS) {
		        	if (playing.getGameTimeInSeconds() - pushDownStartTimes[i] > TIME_FOR_FIRST_THROW_ARC_CHANGE + TIME_BETWEEN_THROW_CHANGES) {
		        		pushDownStartTimes[i] += TIME_BETWEEN_THROW_CHANGES;
		        		changeThrowDirection(i);
		        	}
		        }
	        }
	        
	        // rotate tetris tile
            int prevRotateControllerState = rotateControllerState;
	        rotateControllerState = buttons.get(CONTROLLER_Y_BUTTON_ID);
	        if (rotateControllerState == GLFW.GLFW_RELEASE && prevRotateControllerState == GLFW.GLFW_PRESS) {
	        	if (isCarrying != null) {
					int old_rotation_player1 = isCarrying.getRotation();
					isCarrying.setRotation((old_rotation_player1 + 1) % 4);
				}
	        }
	        
	        // joysticks
	        FloatBuffer axes = GLFW.glfwGetJoystickAxes(controllerID);
	        float left_js_x = axes.get(0);
	        // left joystick for running
			if (left_js_x > JOYSTICK_DEAD_ZONE) {
				setRight(true);
				setLeft(false);
			}
			else if (left_js_x < -JOYSTICK_DEAD_ZONE) {
				setRight(false);
				setLeft(true);
			}
			else {
				setRight(false);
				setLeft(false);
			}
			
			// pause menu
            int prevPauseControllerState = pauseControllerState;
			pauseControllerState = buttons.get(CONTROLLER_H_BUTTON_ID);
	        if (pauseControllerState == GLFW.GLFW_RELEASE && prevPauseControllerState == GLFW.GLFW_PRESS) {
	        	playing.setPaused(!playing.getPaused());
	        	if (playing.getPaused())
	        		playing.getGame().getAudioPlayer().stopSong();
	        }
		}
	}

	public void rotateTile(boolean is_pressed){
		if (!keyboardRotatedTile && is_pressed && isCarrying != null) {
				int old_rotation_player1 = isCarrying.getRotation();
				isCarrying.setRotation((old_rotation_player1 + 1) % 4);
		}

		// should be activated on each release -> reset rotating on
		keyboardRotatedTile = is_pressed;
		System.out.println("krt: " + keyboardRotatedTile + " is pressed: " + is_pressed);
	}

	private void changeThrowDirection(int buttonId) {
		switch(buttonId) {
			case CONTROLLER_LEFT_BUTTON_ID:
        		throwWidthInSmallTiles = Math.max(throwWidthInSmallTiles-1, -TETRIS_TILE_MAX_THROW_WIDTH_IN_SMALL_TILES);
				break;
			case CONTROLLER_RIGHT_BUTTON_ID:
        		throwWidthInSmallTiles = Math.min(throwWidthInSmallTiles+1, TETRIS_TILE_MAX_THROW_WIDTH_IN_SMALL_TILES);
				break;
			case CONTROLLER_UP_BUTTON_ID:
        		throwHeightInSmallTiles = Math.min(throwHeightInSmallTiles+1, TETRIS_TILE_MAX_THROW_HEIGHT_IN_SMALL_TILES);
				break;
			case CONTROLLER_DOWN_BUTTON_ID:
        		throwHeightInSmallTiles = Math.max(throwHeightInSmallTiles-1, 1);
				break;
		}
	}

	public void changeThrowDirectionKeyboardLeft(){
		throwWidthInSmallTiles = Math.max(throwWidthInSmallTiles-1, -TETRIS_TILE_MAX_THROW_WIDTH_IN_SMALL_TILES);
	}

	public void changeThrowDirectionKeyboardRight(){
		throwWidthInSmallTiles = Math.min(throwWidthInSmallTiles+1, TETRIS_TILE_MAX_THROW_WIDTH_IN_SMALL_TILES);
	}
	public void changeThrowDirectionKeyboardDown(){
		throwHeightInSmallTiles = Math.max(throwHeightInSmallTiles-1, 1);
	}

	public void changeThrowDirectionKeyboardUp(){
		throwHeightInSmallTiles = Math.min(throwHeightInSmallTiles+1, TETRIS_TILE_MAX_THROW_HEIGHT_IN_SMALL_TILES);
	}
	
	private void checkInsideWater() {
		if (hitbox.y > playing.getCurrentWaterYPos() + WATER_HEIGHT*0.1f) {
			currentHealth -= WATER_DMG_PER_SECOND / UPS_SET;
			currentHealth = Math.max(currentHealth, 0);
			walkSpeed = PLAYER_WALKSPEED*WATER_PLAYER_SLOW_FACTOR;
			jumpSpeed = PLAYER_JUMP_SPEED*WATER_PLAYER_JUMP_SLOW_FACTOR;
		}
		else {
			walkSpeed = PLAYER_WALKSPEED;
			jumpSpeed = PLAYER_JUMP_SPEED;
		}
	}

	private void checkSpikesTouched() {
		playing.checkSpikesTouched(this);
	}

	private void checkAttack() {
		if (attackChecked || aniIndex != 1)
			return;

        attackChecked = !powerAttackActive;

		playing.checkEnemyPlayerHit(isPlayer1);
		playing.checkEnemyHit(attackBox);
	}
	
	public float[] calcThrowSpeed() {
		float tileAirSpeed = (float) Math.sqrt(2.0f * GRAVITY * throwHeightInSmallTiles*Game.TILES_SIZE/4.0f); 
		float tileXSpeed = (float) ((throwWidthInSmallTiles*Game.TILES_SIZE/4.0f)/(2.0f*tileAirSpeed/GRAVITY));
		return new float[] {tileXSpeed, tileAirSpeed};
	}
	
	public void grabOrThrow() {
		if (isCarrying != null) {
			float[] throwSpeed = calcThrowSpeed();
			isCarrying.xSpeed = throwSpeed[0];
			isCarrying.airSpeed = -throwSpeed[1];
			isCarrying.setIsCarriedBy(null);
			isCarrying = null;	
			throwActive = true;
		}
		else {
			playing.checkTetrisTileGrabbed(grabBox, this);
		}
	}

	private void updateAttackBox() {
		attackBox.x = hitbox.x - (attackBox.width-hitbox.width)/2;
		attackBox.y = hitbox.y - (attackBox.height-hitbox.height)/2;
	}
	

	private void updateGrabBox() {
		grabBox.x = hitbox.x - (grabBox.width-hitbox.width)/2;
		grabBox.y = hitbox.y - (grabBox.height-hitbox.height)/2;
		
	}

	private void updateHealthBar() {
		healthWidth = (int) ((currentHealth / (float) maxHealth) * healthBarWidth);
	}

	private void updatePowerBar() {
		powerWidth = (int) ((powerValue / (float) powerMaxValue) * powerBarWidth);

		powerGrowTick++;
        int powerGrowSpeed = 15;
        if (powerGrowTick >= powerGrowSpeed) {
			powerGrowTick = 0;
			changePower(1);
		}
	}

	public void drawPlayer(Graphics g, int xLvlOffset, int yLvlOffset) {
		
		int aniStateOffset = 0;
		if (isCarrying != null)
			aniStateOffset = NUM_ANIMATIONS;		
		g.drawImage(animations[state + aniStateOffset][aniIndex], (int) (hitbox.x - xDrawOffset) - xLvlOffset + flipX, 
				(int) (hitbox.y - yDrawOffset - yLvlOffset + (int) (pushDrawOffset)), width * flipW, height, null);
//		drawHitbox(g, xLvlOffset, yLvlOffset);
//		drawGrabBox(g, xLvlOffset, yLvlOffset);
//		drawAttackBox(g, xLvlOffset, yLvlOffset);
		/*if ((playing.getGameTimeInSeconds() - buttonLastPressed[CONTROLLER_X_BUTTON_ID] < THROW_ARC_SHOW_TIME ||
			playing.getGameTimeInSeconds() - buttonLastPressed[CONTROLLER_RIGHT_BUTTON_ID] < THROW_ARC_SHOW_TIME ||
			playing.getGameTimeInSeconds() - buttonLastPressed[CONTROLLER_LEFT_BUTTON_ID] < THROW_ARC_SHOW_TIME ||
			playing.getGameTimeInSeconds() - buttonLastPressed[CONTROLLER_UP_BUTTON_ID] < THROW_ARC_SHOW_TIME ||
			playing.getGameTimeInSeconds() - buttonLastPressed[CONTROLLER_DOWN_BUTTON_ID] < THROW_ARC_SHOW_TIME)
			&& (isCarrying != null) && throwHeightInSmallTiles > 0) {

		}*/
		if (isCarrying != null){
			drawThrowArc(g, xLvlOffset, yLvlOffset);
			drawGrabBox(g, 0,0);
			drawThrowArc = true;
		}

			
	}

	public void drawGrabBox(Graphics g, int xLvlOffset, int yLvlOffset) {
		g.setColor(Color.BLACK);
		g.drawRect((int) grabBox.x - xLvlOffset, (int) grabBox.y - yLvlOffset, (int) grabBox.width, (int) grabBox.height);
	}

	protected void drawThrowArcOld(Graphics g, int xLvlOffset, int yLvlOffset, int numArcPoints) {
		Graphics2D g2 = (Graphics2D) g;
		float[] throwSpeed = calcThrowSpeed();
		float tileXSpeed = throwSpeed[0];
		float tileAirSpeed = throwSpeed[1];
		if (isPlayer1)
			g.setColor(THROW_ARC_COLOR_PLAYER1);
		else
			g.setColor(THROW_ARC_COLOR_PLAYER2);
		int circle_x , circle_y, lastX=0, lastY=0;
		int radius, maxRadius = 15, minRadius = 6;
		float maxThrowTime = tileAirSpeed / GRAVITY * 2;
		float xDistanceTraveled, xDistanceDueStartSpeed, xDistanceDueWind, time;
		boolean lastPointExists=false;
		for (int i=0; i < numArcPoints+1; i++) {
			time = i/(numArcPoints-1.0f)*maxThrowTime;
			xDistanceDueStartSpeed = time * tileXSpeed;
			if (time <= TETRIS_TILE_TIME_TO_REACH_WINDSPEED*UPS_SET)
				xDistanceDueWind = playing.getWindSpeed()/(TETRIS_TILE_TIME_TO_REACH_WINDSPEED*UPS_SET) * 0.5f * time * time;
			else
				xDistanceDueWind = playing.getWindSpeed()*(TETRIS_TILE_TIME_TO_REACH_WINDSPEED*UPS_SET) * 0.5f + 
						playing.getWindSpeed() * (time - TETRIS_TILE_TIME_TO_REACH_WINDSPEED*UPS_SET);
			xDistanceTraveled = xDistanceDueStartSpeed + xDistanceDueWind;
			
			radius = (int)(minRadius + i/((float) numArcPoints) * (maxRadius-minRadius));
			circle_x = (int) (hitbox.x + hitbox.width/2 - xLvlOffset - (float) radius /2 + xDistanceTraveled);
			
			if (isCarrying != null) {
				circle_y = (int) (hitbox.y - yLvlOffset - (float) radius /2 - isCarrying.hitbox.height/2 -
						calculateYOfThrowArc(time, playing.getWindSpeed(), tileAirSpeed, GRAVITY));
				if (lastPointExists) {
	                g2.setStroke(new BasicStroke((int) radius));
					g2.drawLine(lastX, lastY, circle_x, circle_y);
				}
				lastX = circle_x;
				lastY = circle_y;
				lastPointExists = true;
			}
		}
	}
	
	protected void drawThrowArc(Graphics g, int xLvlOffset, int yLvlOffset) {
		Graphics2D g2 = (Graphics2D) g;
		g.setColor(THROW_ARC_COLOR_PLAYER2);
		int pIndex = 1;
		if (isPlayer1) {
			g.setColor(THROW_ARC_COLOR_PLAYER1);
			pIndex = 0;
		}
			
		int circle_x , circle_y, lastX=0, lastY=0, radius=10;
		for (int i=0; i < NUM_THROW_ARC_PREDICTION_POINTS; i++) {
			circle_x = playing.getTetrisTileManager().getThrowArcPredictionPoints()[pIndex][i][0] - xLvlOffset; 
			circle_y = playing.getTetrisTileManager().getThrowArcPredictionPoints()[pIndex][i][1] - yLvlOffset; 
			
			if (circle_x == FINAL_PREDICTION_POINT || i == NUM_THROW_ARC_PREDICTION_POINTS-1) {
				playing.getTetrisTileManager().drawPredictionTile(g, xLvlOffset, yLvlOffset, i-1, isPlayer1);
				return;
			}
			if (i!=0) {
                g2.setStroke(new BasicStroke((int) radius));
				g2.drawLine(lastX, lastY, circle_x, circle_y);
			}
			lastX = circle_x;
			lastY = circle_y;
		}
	}
	
	public void drawUI(Graphics g) {
		// Background ui
		int xDrawOffset = 0;
        int statusBarX = (int) (10 * Game.SCALE);
        int xStatusBarOffset = statusBarX;
		int xWindsockOffset = (int) (statusBarX + statusBarWidth + 30*Game.SCALE);
		if (!isPlayer1) {
			xDrawOffset = -Game.GAME_WIDTH/2;
			xStatusBarOffset = (int) (-statusBarX + Game.GAME_WIDTH/2 - statusBarWidth);
			xWindsockOffset = (int) ((float) Game.GAME_WIDTH /2 - statusBarX - statusBarWidth - 30*Game.SCALE);
		}
		
		// temperature
		g.setColor(tempScaleBackgroundColor);
        int tempBarWidth = (int) (10 * Game.SCALE);
        int tempBarMaxHeight = (int) (108 * Game.SCALE);
        int tempBarY = (int) (25 * Game.SCALE);
        g.fillRect(Game.GAME_WIDTH/2- tempBarWidth /2 + xDrawOffset,
                tempBarY,
                tempBarWidth,
                tempBarMaxHeight);
		Color tempColor = new Color((int) (playing.getTemperature()*255/MAX_TEMP),0,(int) (255-playing.getTemperature()*255/MAX_TEMP));
		g.setColor(tempColor);
		int tempBarHeight = (int) (tempBarMaxHeight * playing.getTemperature() / MAX_TEMP);
		g.fillRect(Game.GAME_WIDTH/2- tempBarWidth /2 + xDrawOffset,
				tempBarY + tempBarMaxHeight - tempBarHeight,
                tempBarWidth,
				(int) (tempBarHeight+20*Game.SCALE));
        int tempScaleWidth = (int) (20 * Game.SCALE);
        g.drawImage(tempScaleImg, Game.GAME_WIDTH/2- tempScaleWidth /2 + xDrawOffset, 0, tempScaleWidth, Game.GAME_HEIGHT, null);
		
		// Health bar
		g.setColor(healthBarBackgroundColor);
        int statusBarY = 0; //(int) (0 * Game.SCALE);
        int statusBarHeight = (int) (64 * Game.SCALE);
        int healthBarHeight = statusBarHeight * 100 / 512;
        int healthBarXStart = statusBarWidth * 180 / 1024;
        int healthBarYStart = statusBarHeight * 80 / 512;
        g.fillRect(healthBarXStart + xStatusBarOffset, healthBarYStart + statusBarY, healthBarWidth, healthBarHeight);
		g.setColor(healthBarColor);
		g.fillRect(healthBarXStart + xStatusBarOffset, healthBarYStart + statusBarY, healthWidth, healthBarHeight);

		// Power Bar
		g.setColor(powerBarBackgroundColor);
        int powerBarHeight = statusBarHeight * 100 / 512;
        int powerBarXStart = statusBarWidth * 180 / 1024;
        int powerBarYStart = statusBarHeight * 270 / 512;
        g.fillRect(powerBarXStart + xStatusBarOffset, powerBarYStart + statusBarY, powerBarWidth, powerBarHeight);
		g.setColor(powerBarColor);
		g.fillRect(powerBarXStart + xStatusBarOffset, powerBarYStart + statusBarY, powerWidth, powerBarHeight);
		g.drawImage(statusBarImg, xStatusBarOffset, statusBarY, statusBarWidth, statusBarHeight, null);

		// windsock
		BufferedImage wsImg;
		int flip = 1;
		float windSpeed = playing.getWindSpeed();
		if (windSpeed < 0)
			flip = -1;
		
		if (Math.abs(windSpeed) <= WEAK_WIND_TH) 
			wsImg = windsockImg1;
		else if(WEAK_WIND_TH <= Math.abs(windSpeed) && Math.abs(windSpeed) <= STRONG_WIND_TH)
			wsImg = windsockImg2;
		else
			wsImg = windsockImg3;
			
		int windsockX = 50;
        int windsockWidth = Game.GAME_WIDTH / 17;
        int windsockHeight = Game.GAME_WIDTH / 17;
        int windsockY = (int) (3 * Game.SCALE);
        g.drawImage(wsImg, xWindsockOffset-flip* windsockWidth /2, windsockY, flip* windsockWidth, windsockHeight, null);
		
		
	}

	private void updateAnimationTick() {
		aniTick++;
		if (aniTick >= ANI_SPEED) {
			aniTick = 0;
			aniIndex++;
			if (aniIndex >= GetSpriteAmount(state)) {
				if (state == JUMP || state == FALLING || state == FASTFALLING || state == ATTACK)
					aniIndex--;
				else
					aniIndex = 0;
				if (state == HIT) {
					newState(IDLE);
					//airSpeed = 0f;
					if (!IsFloor(hitbox, 0, lvlData))
						inAir = true;
				}
				attacking = false;
				attackChecked = false;
				throwActive = false;
			}
		}
	}

	private void setAnimation() {
		int startAni = state;

		if (state == HIT)
			return;

		if (moving)
			state = RUNNING;
		else
			state = IDLE;

		if (inAir) {
			if (airSpeed < 0)
				state = JUMP;
			else if ((playing.getGameTimeInSeconds()-startTimeInAir) > 0.2f && !fasterFall){
				state = FALLING;
			} else if (((playing.getGameTimeInSeconds()-startTimeInAir) > 0.2f) && fasterFall) {
				state = FASTFALLING;
			}
		}

		if (powerAttackActive) {
			state = ATTACK;
			aniIndex = 1;
			aniTick = 0;
			return;
		}

		if (throwActive) {
			state = THROW;
		}
		
		if (startAni != state)
			resetAniTick();
	}

	private void resetAniTick() {
		aniTick = 0;
		aniIndex = 0;
	}

	private void updatePos() {
		moving = false;
		if (jump){
			if (resetJump && jumpsDone < MAX_ALLOWED_JUMPS){
				jump();
				jumpsDone++;
				resetJump = false;
			}else {
				jump = false;
			}
		}

		if (!inAir){
			jumpsDone = 0;
			resetJump = true;
			xSpeed = 0;
			dashState = DashState.NOTHING;
			if (!powerAttackActive)
				if ((!left && !right) || (right && left))
					return;
			return;
		}
		if (dashState != DashState.NOTHING)
			System.out.println(dashState);


		if (dashState == DashState.DASHING){
			xSpeed = 0;
			if (left && !right){
				flipX = width;
				flipW = -1;
			}else {
				flipX = 0;
				flipW = 1;
			}
		}else {
			if (left && !right) {
				xSpeed = -walkSpeed;
				flipX = width;
				flipW = -1;
			}
			if (right && !left) {
				xSpeed = walkSpeed;
				flipX = 0;
				flipW = 1;
			}
		}

		if (dashState == DashState.ACTIVATE2){
			dashStartTime = System.currentTimeMillis();
			xSpeed *= 10;
			dashState = DashState.DASHING;
		}else if (dashState == DashState.DASHING){
			if ((System.currentTimeMillis() - dashStartTime) >= 1000){
				dashState = DashState.NOTHING;
				xSpeed = 0;
			}
		}

		if (powerAttackActive) {
			if ((!left && !right) || (left && right)) {
				if (flipW == -1)
					xSpeed = -walkSpeed;
				else
					xSpeed = walkSpeed;
			}
			xSpeed *= 3;
		}

		if (!inAir)
			if (!IsEntityOnFloor(hitbox, lvlData))
				inAir = true;

		// power attack
		if (inAir && !powerAttackActive) {
			if (CanMoveHere(hitbox.x, hitbox.y + airSpeed, hitbox.width, hitbox.height, lvlData)) {
				hitbox.y += airSpeed;
				airSpeed += GRAVITY * (isFastFall() ? 5 : 1);
				updateXPos(xSpeed, lvlData);
			} else {
                float fallSpeedAfterCollision = 0.5f * Game.SCALE;
                if (airSpeed > 0)
					resetInAir();
				else
					airSpeed = fallSpeedAfterCollision;
				// TODO
				updateXPos(xSpeed, lvlData);
			}

		} else {
			// execute x pos update
			updateXPos(xSpeed, lvlData);
			if (powerAttackActive && !CanMoveHere(hitbox.x + xSpeed, hitbox.y, hitbox.width, hitbox.height, lvlData)) {
				powerAttackActive = false;
				powerAttackTick = 0;
			}
		}
		moving = true;
	}

	private void jump() {
		boolean jumping = airSpeed < 0;
		if (!inAir || (!jumping && (playing.getGameTimeInSeconds() - startTimeInAir < COYOTE_TIME) || jumpsDone < MAX_ALLOWED_JUMPS)) {
			inAir = true;
			startTimeInAir = playing.getGameTimeInSeconds();
			if (jumpsDone == 0) {
				airSpeed = jumpSpeed;
			}else {
				airSpeed = 2 * jumpSpeed - airSpeed;
			}
		}
	}

	private void resetInAir() {
		inAir = false;
		airSpeed = 0;
	}

	public void selfHurtFromPowerAttack(int value) {
		if (selfHurt)
			return;
		
		currentHealth += value;
		currentHealth = Math.max(Math.min(currentHealth, maxHealth), 0);
		selfHurt = true;
	}
	
	public void changeHealth(int value) {
		if (value < 0) {
			if (state == HIT)
				return;
			else
				newState(HIT);
		}

		currentHealth += value;
		currentHealth = Math.max(Math.min(currentHealth, maxHealth), 0);
	}

	public void changeHealth(int value, Entity e) {
		if (state == HIT || state == DEAD)
			return;
		changeHealth(value);
		pushBackOffsetDir = UP;
		pushDrawOffset = 0;

		if (e.getHitbox().x < hitbox.x)
			pushBackDir = RIGHT;
		else
			pushBackDir = LEFT;
	}
	
	public void kill() {
		currentHealth = 0;
	}

	public void changePower(int value) {
		powerValue += value;
		powerValue = Math.max(Math.min(powerValue, powerMaxValue), 0);
	}

	private void loadAnimations() {
		String fileName="", baseDir="";
		animations = new BufferedImage[NUM_ANIMATIONS*2][MAX_ANIMATION_LENGTH];
		
		if (isPlayer1)
			baseDir = "animation/player" + 1;
		else
			baseDir = "animation/player" + 2;
		
		for (int j = 0; j < NUM_ANIMATIONS; j++) {
            fileName = switch (j) {
                case IDLE -> "idle";
                case RUNNING -> "running";
                case JUMP -> "jump";
                case FALLING -> "falling";
				case FASTFALLING -> "fastfalling";
                case ATTACK -> "jump";
                case HIT -> "hit";
                case DEAD -> "dead";
                case THROW -> "throw";
                default -> fileName;
            };
			for (int i = 0; i < animations[j].length; i++) {
				if (i<GetSpriteAmount(j)) {
					if (Thread.currentThread().getContextClassLoader().getResource(baseDir + "/" + fileName + i + ".png")!=null) {

						animations[j][i] = LoadSave.GetSpriteAtlas(baseDir + "/" + fileName + i + ".png");
					}
					else {
						if (!isPlayer1) {
				            animations[j][i] = replaceColors(playing.getPlayer1().getAnimations()[j][i], COLOR_MAP, PLAYER_COLOR_TOLERANCE, PLAYER_DEFAULT_COLOR);
				            LoadSave.SaveImage(animations[j][i], "png", "res/" + baseDir + "/" + fileName + i + ".png");
				            System.out.println("file: " + "res/" + baseDir + "/" + fileName + i + ".png" + " was created by repalcing colors");
						} else if (!fileName.contains("hit") && !fileName.contains("dead")) {
							animations[j][i] = LoadSave.GetSpriteAtlas(baseDir + "/idle0.png");
							System.out.println("file does not exist: " + baseDir + "/" + fileName + i + ".png");
						}
						
					}
					
					if (j == IDLE || j == RUNNING || j == JUMP || j == FALLING || j==FASTFALLING || j==ATTACK) {
						// carry animations
						if (Thread.currentThread().getContextClassLoader().getResource(baseDir + "/carry" + fileName + i + ".png")!=null) {
							animations[j+NUM_ANIMATIONS][i] = LoadSave.GetSpriteAtlas(baseDir + "/carry" + fileName + i + ".png");
						}
						else {
							if (!isPlayer1) {
					            animations[j+NUM_ANIMATIONS][i] = replaceColors(playing.getPlayer1().getAnimations()[j+NUM_ANIMATIONS][i], COLOR_MAP, PLAYER_COLOR_TOLERANCE, PLAYER_DEFAULT_COLOR);
					            LoadSave.SaveImage(animations[j+NUM_ANIMATIONS][i], "png", "res/" + baseDir + "/carry" + fileName + i + ".png");
					            System.out.println("file: " + "res/" + baseDir + "/carry" + fileName + i + ".png" + " was created by repalcing colors");
							}
							else {
								animations[j+NUM_ANIMATIONS][i] = LoadSave.GetSpriteAtlas(baseDir + "/idle0.png");
								System.out.println("file does not exist: " + baseDir + "/carry" + fileName + i + ".png");
							}
						}
					}
					else {
						animations[j+NUM_ANIMATIONS][i] = null;
					}
				}
				else {
					animations[j][i] = null;
				}
			}
		}
		statusBarImg = LoadSave.GetSpriteAtlas(LoadSave.STATUS_BAR);
		tempScaleImg = LoadSave.GetSpriteAtlas(LoadSave.TEMP_SCALE);
		windsockImg1 = LoadSave.GetSpriteAtlas(LoadSave.WINDSOCK1);
		windsockImg2 = LoadSave.GetSpriteAtlas(LoadSave.WINDSOCK2);
		windsockImg3 = LoadSave.GetSpriteAtlas(LoadSave.WINDSOCK3);
	}

	public void loadLvlData(int[][] lvlData) {
		this.lvlData = lvlData;
		if (!IsEntityOnFloor(hitbox, lvlData))
			inAir = true;
	}

	public void resetDirBooleans() {
		left = false;
		right = false;
	}

	public void setAttacking(boolean attacking) {
		this.attacking = attacking;
	}

	public boolean isLeft() {
		return left;
	}

	public void setLeft(boolean left) {
		this.left = left;

		moveHorizontal(left);
	}

	public boolean isRight() {
		return right;
	}

	public void setRight(boolean right) {
		this.right = right;

		moveHorizontal(right);
	}

	private void moveHorizontal(boolean active) {
		if (this.lastDirection == Direction.LEFT){
			if (this.dashState == DashState.NOTHING && active){
				this.dashState = DashState.ACTIVATE1;
			} else if (this.dashState == DashState.ACTIVATE1 && !active) {
				this.dashState = DashState.RELEASE1;
			} else if (this.dashState == DashState.RELEASE1 && active) {
				// perform dash
				this.dashState = DashState.ACTIVATE2;
			}
		}else {
			this.dashState = DashState.NOTHING;
		}
	}

	public void setJump(boolean jump) {
		//this.jump = jump;
		if (!jump && jumpsDone < MAX_ALLOWED_JUMPS){
			resetJump = true;
		}else {
			this.jump = true;
		}
	}

	public void fastFall(boolean doSpeedup){
		this.fasterFall = doSpeedup;
	}

	public boolean isFastFall(){
		return fasterFall;
	}

	public void stopMovement(){
		this.left = false;
		this.right = false;
		this.jump = false;
	}
	
	public void setGrabOrThrow(boolean grabOrThrow) {
		this.grabOrThrow = grabOrThrow;
	}
	
	public void setIsCarrying(TetrisTile isCarrying) {
		this.isCarrying = isCarrying;
	}
	
	public TetrisTile getIsCarrying() {
		return isCarrying;
	}

	public void resetAll() {
		resetAtDeath();
		resetLvlOffsets();		
	}
	
	public void resetLvlOffsets() {
//		if (!isPlayer1) {
//			xLvlOffset = playing.getMaxLvlOffsetX();//(int)(hitbox.x - Game.GAME_WIDTH/4);
//		}
//		else {
//			xLvlOffset = 0;//- Game.GAME_WIDTH/2 + 1;
//		}
		yLvlOffset = (int)(hitbox.y - (float) Game.GAME_HEIGHT /2);
		xLvlOffset = (int)(hitbox.x - (float) Game.GAME_WIDTH /4);
	}

	public void resetAtDeath() {
		resetDirBooleans();
		if (isCarrying != null) {
			isCarrying.setIsCarriedBy(null);
			isCarrying = null;
		}
		inAir = false;
		attacking = false;
		moving = false;
		airSpeed = 0f;
		state = IDLE;
		currentHealth = maxHealth;
		powerAttackActive = false;
		throwActive = false;
		powerAttackTick = 0;
		powerValue = powerMaxValue;

		hitbox.x = x;
		hitbox.y = y;
		throwHeightInSmallTiles = (float) TETRIS_TILE_MAX_THROW_HEIGHT_IN_SMALL_TILES /2;
		throwWidthInSmallTiles = (float) TETRIS_TILE_MAX_THROW_WIDTH_IN_SMALL_TILES /2;
		if (!IsEntityOnFloor(hitbox, lvlData))
			inAir = true;
	}

	public int getTileY() {
		return tileY;
	}
	

	public void powerAttack() {
		selfHurt = false;
		if (powerAttackActive)
			return;
		if (powerValue >= 60) {
			powerAttackActive = true;
			changePower(-60);
		}

	}
	
	public int getXLvlOffset() {
		return xLvlOffset;
	}
	
	public int getYLvlOffset() {
		return yLvlOffset;
	}
	
	public boolean getGrabOrThrow() {
		return grabOrThrow;
	}
	
	public void setXLvlOffset(int xLvlOffset) {
		this.xLvlOffset = xLvlOffset;
	}
	
	public void setYLvlOffset(int yLvlOffset) {
		this.yLvlOffset = yLvlOffset;
	}

	public boolean getPowerAttackNotActive() {
		return !powerAttackActive;
	}
	
	public BufferedImage[][] getAnimations() {
		return animations;
	}

	public boolean getIsPlayer1() {
		return isPlayer1;
	}

	public boolean getDrawThrowArc() {
		return drawThrowArc;
	}

}