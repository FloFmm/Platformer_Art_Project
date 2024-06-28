package entities;

import static utilz.Constants.PlayerConstants.*;
import static utilz.HelpMethods.*;
import static utilz.Constants.UPS_SET;
import static utilz.Constants.GRAVITY;
import static utilz.Constants.ANI_SPEED;
import static utilz.Constants.Directions.*;
import static utilz.Constants.TetrisTileConstants.*;
import static utilz.Constants.ControllerConstants.*;
import static utilz.Constants.Environment.*;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import audio.AudioPlayer;
import gamestates.Playing;
import main.Game;
import utilz.LoadSave;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.lwjgl.glfw.GLFW;


public class Player extends Entity {
	
	private BufferedImage[][] animations;
	private boolean moving = false, attacking = false;
	private boolean left, right, jump, grabOrThrow = false;
	protected Rectangle2D.Float grabBox;
	private TetrisTile isCarrying;
	private float throwPushDownStartTime, startTimeInAir;
	private int[][] lvlData;
	private float xDrawOffset = (width-HITBOX_BASE_WIDTH*Game.SCALE)/2;//21 * Game.SCALE;
	private float yDrawOffset = (height-HITBOX_BASE_HEIGHT*Game.SCALE)/2;//4 * Game.SCALE;
	private int xLvlOffset, yLvlOffset;
	
	// Jumping / Gravity
	private float jumpSpeed = PLAYER_JUMP_SPEED;
	private float fallSpeedAfterCollision = 0.5f * Game.SCALE;

	// StatusBarUI
	private BufferedImage statusBarImg, middleSeperatorImg, windsockImg1, windsockImg2, windsockImg3;

	private int tempScaleWidth = (int) (Game.GAME_WIDTH/80);
	private int tempScaleMaxHeight = (int) (Game.GAME_HEIGHT/3);
	private int tempScaleY = (int) (Game.GAME_HEIGHT/2);
	
	private int windsockWidth = (int) (Game.GAME_WIDTH/20);
	private int windsockHeight = (int) (Game.GAME_WIDTH/20);
	private int windsockY = (int) (Game.GAME_HEIGHT/20);
	
	private int statusBarWidth = (int) (128 * Game.SCALE);
	private int middleSeperatorWidth = (int) (Game.GAME_WIDTH/5);
	private int statusBarHeight = (int) (64 * Game.SCALE);
	private int statusBarX = (int) (10 * Game.SCALE);
	private int statusBarY = (int) (0 * Game.SCALE);

	private int healthBarWidth = (int) (statusBarWidth*800/1024);
	private int healthBarHeight = (int) (statusBarHeight*100/512);
	private int healthBarXStart = (int) (statusBarWidth*180/1024);
	private int healthBarYStart = (int) (statusBarHeight*80/512);
	private int healthWidth = healthBarWidth;

	private int powerBarWidth = (int) (statusBarWidth*800/1024);
	private int powerBarHeight = (int) (statusBarHeight*100/512);
	private int powerBarXStart = (int) (statusBarWidth*180/1024);
	private int powerBarYStart = (int) (statusBarHeight*270/512);
	private int powerWidth = powerBarWidth;
	private int powerMaxValue = 200;
	private int powerValue = powerMaxValue;

	private int flipX = 0;
	private int flipW = 1;
	private boolean attackChecked;
	private Playing playing;
	private int tileY = 0;

	private boolean powerAttackActive=false, selfHurt = false;
	private int powerAttackTick;
	private int powerGrowSpeed = 15;
	private int powerGrowTick;
	
	// grab and throw
	private float throwAngle = 0;
	private boolean throwActive=false;
	
	//controller
	private int controllerID; 
	private int prevGrabOrThrowControllerState = GLFW.GLFW_RELEASE, grabOrThrowControllerState = GLFW.GLFW_RELEASE;
	private int prevRotateControllerState = GLFW.GLFW_RELEASE, rotateControllerState = GLFW.GLFW_RELEASE;
	private int prevPauseControllerState = GLFW.GLFW_RELEASE, pauseControllerState = GLFW.GLFW_RELEASE;
	private int prevDashControllerState = GLFW.GLFW_RELEASE, dashControllerState = GLFW.GLFW_RELEASE;
	private int prevControllerLeftButtonState = GLFW.GLFW_RELEASE, controllerLeftButtonState = GLFW.GLFW_RELEASE;
	private int prevControllerRightButtonState = GLFW.GLFW_RELEASE, controllerRightButtonState = GLFW.GLFW_RELEASE;
	
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
	}

	public void setSpawn(Point spawn) {
		this.x = spawn.x;
		this.y = spawn.y;
		hitbox.x = x;
		hitbox.y = y;
	}


	private void initAttackBox(int width, int height) {
		attackBox = new Rectangle2D.Float(x, y, (int) (width * Game.SCALE), (int) (height * Game.SCALE));
	}
	
	protected void initGrabBox(int width, int height) {
		grabBox = new Rectangle2D.Float(x, y, (int) (width * Game.SCALE), (int) (height * Game.SCALE));
	}

	public void update() {
		boolean startInAir = inAir;
		
		updateControllerInputs();
		updateHealthBar();
		updatePowerBar();

		if (currentHealth <= 0) {
			if (state != DEAD) {
				state = DEAD;
				aniTick = 0;
				aniIndex = 0;
				playing.getGame().getAudioPlayer().playEffect(AudioPlayer.DIE);

				// Check if player died in air
				if (!IsEntityOnFloor(hitbox, lvlData)) {
					inAir = true;
					//airSpeed = 0;
				}
			} else if (aniIndex == GetSpriteAmount(DEAD) - 1 && aniTick >= ANI_SPEED - 1) {
				//playing.getGame().getAudioPlayer().stopSong();
				resetAtDeath();
				playing.getGame().getAudioPlayer().playEffect(AudioPlayer.GAMEOVER);
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

			return;
		}

		updateAttackBox();
		updateGrabBox();

		if (state == HIT) {
			if (aniIndex <= GetSpriteAmount(state) - 3)
				pushBack(pushBackDir, lvlData, 1.25f);
			updatePushBackDrawOffset();
		} else
			updatePos();

		
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

		if (attacking || powerAttackActive)
			checkAttack();

		updateAnimationTick();
		setAnimation();
		
		if (!startInAir && inAir)
			startTimeInAir = playing.getGameTimeInSeconds();
	}
	
	private void updateControllerInputs() {
		boolean controllerIsPresent = GLFW.glfwJoystickPresent(controllerID);
		if (controllerIsPresent) {
			ByteBuffer buttons = GLFW.glfwGetJoystickButtons(controllerID);
			// jump
	        int jumpControllerState = buttons.get(CONTROLLER_A_BUTTON_ID);
	        if (jumpControllerState == GLFW.GLFW_PRESS) {
	        	jump = true;
	        }
	        if (jumpControllerState == GLFW.GLFW_RELEASE) {
	        	jump = false;
	        }
	        
	        // dash
	        prevDashControllerState = dashControllerState;
	        dashControllerState = buttons.get(CONTROLLER_L_BUTTON_ID);
	        if (dashControllerState == GLFW.GLFW_RELEASE && prevDashControllerState == GLFW.GLFW_PRESS) {
	        	powerAttack();
	        }
	        
	        // grab or throw
	        prevGrabOrThrowControllerState = grabOrThrowControllerState;
	        grabOrThrowControllerState = buttons.get(CONTROLLER_X_BUTTON_ID);
	        if (grabOrThrowControllerState == GLFW.GLFW_PRESS) {
	        	
	        	if (!grabOrThrow) {
					grabOrThrow = true;
					throwPushDownStartTime = playing.getGameTimeInSeconds();	
				}
	        }
	        if (grabOrThrowControllerState == GLFW.GLFW_RELEASE && prevGrabOrThrowControllerState == GLFW.GLFW_PRESS) {
				grabOrThrow = false;
				grabOrThrow();
	        }
	        
	        // throw direction
	        prevControllerLeftButtonState = controllerLeftButtonState;
	        controllerLeftButtonState = buttons.get(CONTROLLER_LEFT_BUTTON_ID);
	        if (controllerLeftButtonState == GLFW.GLFW_RELEASE && prevControllerLeftButtonState == GLFW.GLFW_PRESS) {
				if (throwAngle - THROW_ANGLE_STEP >= -MAX_THROW_ANGLE)
					throwAngle -= THROW_ANGLE_STEP;
				else
					throwAngle = -MAX_THROW_ANGLE;
	        }
	        prevControllerRightButtonState = controllerRightButtonState;
	        controllerRightButtonState = buttons.get(CONTROLLER_RIGHT_BUTTON_ID);
	        if (controllerRightButtonState == GLFW.GLFW_RELEASE && prevControllerRightButtonState == GLFW.GLFW_PRESS) {
				if (throwAngle + THROW_ANGLE_STEP <= MAX_THROW_ANGLE)
					throwAngle += THROW_ANGLE_STEP;
				else
					throwAngle = MAX_THROW_ANGLE;
	        }
	        
	        // rotate tetris tile
	        prevRotateControllerState = rotateControllerState;
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
	        float right_js_x = axes.get(2);
	        float right_js_y = axes.get(3);
	        
	        // right joystick for throw direction
	        if (Math.sqrt(right_js_x*right_js_x + right_js_y*right_js_y) > JOYSTICK_DEAD_ZONE) {
				if (right_js_y < 0) {
					throwAngle = (float) Math.toDegrees(Math.atan(right_js_x/Math.abs(right_js_y)));
					if (throwAngle > MAX_THROW_ANGLE)
						throwAngle = MAX_THROW_ANGLE;
					if (throwAngle < -MAX_THROW_ANGLE)
						throwAngle = -MAX_THROW_ANGLE;
				}
				else {
					if (right_js_x > 0)
						throwAngle = MAX_THROW_ANGLE;
					else
						throwAngle = -MAX_THROW_ANGLE;
				}
			}
	        
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
			prevPauseControllerState = pauseControllerState;
			pauseControllerState = buttons.get(CONTROLLER_H_BUTTON_ID);
	        if (pauseControllerState == GLFW.GLFW_RELEASE && prevPauseControllerState == GLFW.GLFW_PRESS) {
	        	playing.setPaused(!playing.getPaused());
	        }
		}
	}

	
	private void checkInsideWater() {
		if (hitbox.y + hitbox.height*0.75 > playing.getCurrentWaterYPos()) {
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
		attackChecked = true;

		if (powerAttackActive)
			attackChecked = false;

		playing.checkEnemyPlayerHit(isPlayer1);
		playing.checkEnemyHit(attackBox);
		playing.checkObjectHit(attackBox);
		playing.getGame().getAudioPlayer().playAttackSound();
	}
	
	public float[] calcThrowSpeed() {
		float now = playing.getGameTimeInSeconds();
		float pushDownDuration = (now-throwPushDownStartTime);
		int increasingOrDecreasing = (int)((now-throwPushDownStartTime)/TETRIS_TILE_TIME_FOR_MAX_THROW_SPEED % 2);
		if (increasingOrDecreasing == 0) {
			pushDownDuration = pushDownDuration % TETRIS_TILE_TIME_FOR_MAX_THROW_SPEED;
		}
		else {
			pushDownDuration = TETRIS_TILE_TIME_FOR_MAX_THROW_SPEED - (pushDownDuration % TETRIS_TILE_TIME_FOR_MAX_THROW_SPEED);
		}
		float throwSpeed = (float) Math.min(TETRIS_TILE_MAX_THROW_SPEED, 
				TETRIS_TILE_MAX_THROW_SPEED*Math.sqrt(pushDownDuration/TETRIS_TILE_TIME_FOR_MAX_THROW_SPEED));
		
		float tileXSpeed = (float) Math.sin(Math.toRadians(throwAngle)) * throwSpeed;
		float tileAirSpeed = (float) Math.cos(Math.toRadians(throwAngle)) * throwSpeed;
		
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
		drawHitbox(g, xLvlOffset, yLvlOffset);
		drawGrabBox(g, xLvlOffset, yLvlOffset);
		drawAttackBox(g, xLvlOffset, yLvlOffset);
		if (grabOrThrow && (isCarrying != null))
			drawThrowArc(g, xLvlOffset, yLvlOffset, 21);
	}
	
	protected void drawGrabBox(Graphics g, int xLvlOffset, int yLvlOffset) {
		g.setColor(Color.BLACK);
		g.drawRect((int) grabBox.x - xLvlOffset, (int) grabBox.y - yLvlOffset, (int) grabBox.width, (int) grabBox.height);
	}

	protected void drawThrowArc(Graphics g, int xLvlOffset, int yLvlOffset, int numArcPoints) {
		float[] throwSpeed = calcThrowSpeed();
		float tileXSpeed = throwSpeed[0];
		float tileAirSpeed = throwSpeed[1];
		
		g.setColor(THROW_ARC_COLOR);
		int circle_x , circle_y;
		int radius, maxRadius = 11, minRadius = 7;
		float maxThrowTime = tileAirSpeed / GRAVITY * 2;
		float xDistanceTraveled, xDistanceDueStartSpeed, xDistanceDueWind, time;
		for (int i=0; i < numArcPoints; i++) {
			time = i/(numArcPoints-1.0f)*maxThrowTime;
			xDistanceDueStartSpeed = time * tileXSpeed;
			if (time <= TETRIS_TILE_TIME_TO_REACH_WINDSPEED*UPS_SET)
				xDistanceDueWind = playing.getWindSpeed()/(TETRIS_TILE_TIME_TO_REACH_WINDSPEED*UPS_SET) * 0.5f * time * time;
			else
				xDistanceDueWind = playing.getWindSpeed()*(TETRIS_TILE_TIME_TO_REACH_WINDSPEED*UPS_SET) * 0.5f + 
						playing.getWindSpeed() * (time - TETRIS_TILE_TIME_TO_REACH_WINDSPEED*UPS_SET);
			xDistanceTraveled = xDistanceDueStartSpeed + xDistanceDueWind;
			
			radius = (int)(minRadius + (numArcPoints/2.0f - Math.abs(numArcPoints/2.0f - i))/(numArcPoints/2.0f) * (maxRadius-minRadius));
			circle_x = (int) (hitbox.x + hitbox.width/2 - xLvlOffset - radius/2 + xDistanceTraveled); 
			
			if (isCarrying != null) {
				circle_y = (int) (hitbox.y - yLvlOffset - isCarrying.hitbox.height/2 - radius/2 - 
						calculateYOfThrowArc(time, playing.getWindSpeed(), tileAirSpeed, GRAVITY));
				g.fillOval(circle_x,circle_y,radius,radius);
			}
		}
		
	}
	
	public void drawUI(Graphics g) {
		// Background ui
		int xDrawOffset = 0;
		int xStatusBarOffset = statusBarX;
		if (!isPlayer1) {
			xDrawOffset = -Game.GAME_WIDTH/2;
			xStatusBarOffset = (int) (-statusBarX + Game.GAME_WIDTH/2 - statusBarWidth);
		}
		g.drawImage(middleSeperatorImg, Game.GAME_WIDTH/2-middleSeperatorWidth/2 + xDrawOffset, 0, middleSeperatorWidth, Game.GAME_HEIGHT, null);
		
		// Health bar
		g.setColor(Color.red);
		g.fillRect(healthBarXStart + xStatusBarOffset, healthBarYStart + statusBarY, healthWidth, healthBarHeight);

		// Power Bar
		g.setColor(Color.yellow);
		g.fillRect(powerBarXStart + xStatusBarOffset, powerBarYStart + statusBarY, powerWidth, powerBarHeight);
		g.drawImage(statusBarImg, xStatusBarOffset, statusBarY, statusBarWidth, statusBarHeight, null);

			
		// temperature
		Color tempColor = new Color((int) (playing.getTemperature()*255/MAX_TEMP),0,(int) (255-playing.getTemperature()*255/MAX_TEMP));
		g.setColor(tempColor);
		int tempScaleHeight = (int) (tempScaleMaxHeight * playing.getTemperature() / MAX_TEMP);
		g.fillRect(Game.GAME_WIDTH/2-tempScaleWidth/2 + xDrawOffset, 
				tempScaleY - tempScaleHeight, 
				tempScaleWidth, 
				tempScaleHeight);
		
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
			
		g.drawImage(wsImg, Game.GAME_WIDTH/2-flip*windsockWidth/2 + xDrawOffset, windsockY, flip*windsockWidth, windsockHeight, null);
		
		
	}

	private void updateAnimationTick() {
		aniTick++;
		if (aniTick >= ANI_SPEED) {
			aniTick = 0;
			aniIndex++;
			if (aniIndex >= GetSpriteAmount(state)) {
				if (state == JUMP || state == FALLING || state == ATTACK)
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
			else if ((playing.getGameTimeInSeconds()-startTimeInAir) > 0.2f)
				state = FALLING;
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
		//System.out.println(jump);
		if (jump)
			jump();

		if (!inAir)
			if (!powerAttackActive)
				if ((!left && !right) || (right && left))
					return;

		float xSpeed = 0;
		if (left && !right) {
			xSpeed -= walkSpeed;
			flipX = width;
			flipW = -1;
		}
		if (right && !left) {
			xSpeed += walkSpeed;
			flipX = 0;
			flipW = 1;
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
 
		if (inAir && !powerAttackActive) {
			if (CanMoveHere(hitbox.x, hitbox.y + airSpeed, hitbox.width, hitbox.height, lvlData)) {
				hitbox.y += airSpeed;
				airSpeed += GRAVITY;
				updateXPos(xSpeed, lvlData);
			} else {
				//TODO
				//hitbox.y = GetEntityYPosUnderRoofOrAboveFloor(hitbox, airSpeed);
				if (airSpeed > 0)
					resetInAir();
				else
					airSpeed = fallSpeedAfterCollision;
				// TODO
				// updateXPos(xSpeed);
			}

		} else {
			updateXPos(xSpeed, lvlData);
			if (powerAttackActive && !CanMoveHere(hitbox.x + xSpeed, hitbox.y, hitbox.width, hitbox.height, lvlData)) {
				powerAttackActive = false;
				powerAttackTick = 0;
			}
		}
		moving = true;
	}

	private void jump() {
		boolean jumping = (airSpeed < 0);
		if (!inAir || (!jumping && (playing.getGameTimeInSeconds() - startTimeInAir < TIME_TO_JUMP_WHEN_ALREADY_IN_AIR))) {
			playing.getGame().getAudioPlayer().playEffect(AudioPlayer.JUMP);
			inAir = true;
			airSpeed = jumpSpeed;
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
			switch(j) {
				case IDLE:
					fileName = "idle";
					break;
				case RUNNING:
					fileName = "running";
					break;
				case JUMP:
					fileName = "jump";
					break;
				case FALLING:
					fileName = "falling";
					break;
				case ATTACK:
					fileName = "jump";
					break;
				case HIT:
					fileName = "hit";
					break;
				case DEAD:
					fileName = "dead";
					break;
				case THROW:
					fileName = "throw";
					break;
			}
			for (int i = 0; i < animations[j].length; i++) {
				if (i<GetSpriteAmount(j)) {
					if (Files.exists(Paths.get("res/" + baseDir + "/" + fileName + i + ".png"))) {
						
						animations[j][i] = LoadSave.GetSpriteAtlas(baseDir + "/" + fileName + i + ".png");
					}
					else {
						if (!isPlayer1) {
				            animations[j][i] = replaceColors(playing.getPlayer1().getAnimations()[j][i], COLOR_MAP, PLAYER_COLOR_TOLERANCE, PLAYER_DEFAULT_COLOR);
				            LoadSave.SaveImage(animations[j][i], "png", "res/" + baseDir + "/" + fileName + i + ".png");
				            System.out.println("file: " + "res/" + baseDir + "/" + fileName + i + ".png" + " was created by repalcing colors");
						}
						else {
							animations[j][i] = LoadSave.GetSpriteAtlas(baseDir + "/idle0.png");
							System.out.println("file does not exist: " + baseDir + "/" + fileName + i + ".png");
						}
						
					}
					
					if (j == IDLE || j == RUNNING || j == JUMP || j == FALLING || j==ATTACK) {
						// carry animations
						if (Files.exists(Paths.get("res/" + baseDir + "/carry" + fileName + i + ".png"))) {
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
		middleSeperatorImg = LoadSave.GetSpriteAtlas(LoadSave.MIDDLE_SEPERATOR);
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
	}

	public boolean isRight() {
		return right;
	}

	public void setRight(boolean right) {
		this.right = right;
	}

	public void setJump(boolean jump) {
		this.jump = jump;
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
	
	
	public void setThrowPushDownStartTime(long throwPushDownStartTime) {
		
		this.throwPushDownStartTime = System.nanoTime();
	}

	public void resetAll() {
		resetAtDeath();
		resetLvlOffsets();		
	}
	
	public void resetLvlOffsets() {
		if (!isPlayer1) {
			xLvlOffset = playing.getMaxLvlOffsetX();//(int)(hitbox.x - Game.GAME_WIDTH/4);
		}
		else {
			xLvlOffset = 0;//- Game.GAME_WIDTH/2 + 1;
		}
		//System.out.println(playing.getMaxLvlOffsetX());
		yLvlOffset = (int)(hitbox.y - Game.GAME_HEIGHT/2); // playing.getMaxLvlOffsetY();
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

	public boolean getPowerAttackActive() {
		return powerAttackActive;
	}
	
	public BufferedImage[][] getAnimations() {
		return animations;
	}

}