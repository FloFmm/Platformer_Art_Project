package entities;

import static utilz.Constants.PlayerConstants.*;
import static utilz.HelpMethods.*;
import static utilz.Constants.UPS_SET;
import static utilz.Constants.GRAVITY;
import static utilz.Constants.ANI_SPEED;
import static utilz.Constants.Directions.*;
import static utilz.Constants.TetrisTileConstants.*;
import static utilz.Constants.ControllerConstants.*;
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
	private boolean moving = false, attacking = false, respawning = false;
	private boolean left, right, jump, grabOrThrow = false;
	protected Rectangle2D.Float grabBox;
	private TetrisTile isCarrying;
	private long throwPushDownStartTime, startTimeInAir;
	private int[][] lvlData;
	private float xDrawOffset = (width-HITBOX_BASE_WIDTH*Game.SCALE)/2;//21 * Game.SCALE;
	private float yDrawOffset = (height-HITBOX_BASE_HEIGHT*Game.SCALE)/2;//4 * Game.SCALE;
	private int xLvlOffset, yLvlOffset;
	
	// Jumping / Gravity
	private float jumpSpeed = -2.25f * Game.SCALE;
	private float fallSpeedAfterCollision = 0.5f * Game.SCALE;

	// StatusBarUI
	private BufferedImage statusBarImg, middleSeperatorImg;

	private int statusBarWidth = (int) (192 * Game.SCALE);
	private int middleSeperatorWidth = (int) (Game.GAME_WIDTH/5);
	private int statusBarHeight = (int) (58 * Game.SCALE);
	private int statusBarX = (int) (10 * Game.SCALE);
	private int statusBarY = (int) (10 * Game.SCALE);

	private int healthBarWidth = (int) (150 * Game.SCALE);
	private int healthBarHeight = (int) (4 * Game.SCALE);
	private int healthBarXStart = (int) (34 * Game.SCALE);
	private int healthBarYStart = (int) (14 * Game.SCALE);
	private int healthWidth = healthBarWidth;

	private int powerBarWidth = (int) (104 * Game.SCALE);
	private int powerBarHeight = (int) (2 * Game.SCALE);
	private int powerBarXStart = (int) (44 * Game.SCALE);
	private int powerBarYStart = (int) (34 * Game.SCALE);
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
		this.walkSpeed = Game.SCALE * 1.0f;
		loadAnimations();
		initHitbox(HITBOX_BASE_WIDTH, HITBOX_BASE_HEIGHT);
		initGrabBox(GRABBOX_BASE_WIDTH, GRABBOX_BASE_HEIGHT);
		initAttackBox();
	}

	public void setSpawn(Point spawn) {
		this.x = spawn.x;
		this.y = spawn.y;
		hitbox.x = x;
		hitbox.y = y;
	}


	private void initAttackBox() {
		attackBox = new Rectangle2D.Float(x, y, (int) (35 * Game.SCALE), (int) (20 * Game.SCALE));
		resetAttackBox();
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
					airSpeed = 0;
				}
			} else if (aniIndex == GetSpriteAmount(DEAD) - 1 && aniTick >= ANI_SPEED - 1) {
				//playing.setGameOver(true);
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

		if (moving) {
			checkPotionTouched();
			checkSpikesTouched();
			checkInsideWater();
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
			startTimeInAir = System.nanoTime();
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
					throwPushDownStartTime = System.nanoTime();	
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
		if (IsEntityInWater(hitbox, playing.getLevelManager().getCurrentLevel().getLevelData()))
			kill();
	}

	private void checkSpikesTouched() {
		playing.checkSpikesTouched(this);
	}

	private void checkPotionTouched() {
		playing.checkPotionTouched(this);
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
		long now = System.nanoTime();
		float pushDownDuration = (now-throwPushDownStartTime)/1000_000_000.0f;
		int increasingOrDecreasing = (int)((now-throwPushDownStartTime)/1000000000.0f/TETRIS_TILE_TIME_FOR_MAX_THROW_SPEED % 2);
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

	private void setAttackBoxOnRightSide() {
		attackBox.x = hitbox.x + hitbox.width - (int) (Game.SCALE * 5);
	}

	private void setAttackBoxOnLeftSide() {
		attackBox.x = hitbox.x - hitbox.width - (int) (Game.SCALE * 10);
	}

	private void updateAttackBox() {
		if (right && left) {
			if (flipW == 1) {
				setAttackBoxOnRightSide();
			} else {
				setAttackBoxOnLeftSide();
			}

		} else if (right || (powerAttackActive && flipW == 1))
			setAttackBoxOnRightSide();
		else if (left || (powerAttackActive && flipW == -1))
			setAttackBoxOnLeftSide();

		attackBox.y = hitbox.y + (Game.SCALE * 10);
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
		g.drawImage(statusBarImg, statusBarX, statusBarY, statusBarWidth, statusBarHeight, null);
		
		int xDrawOffset = 0;
		if (!isPlayer1)
			xDrawOffset = -Game.GAME_WIDTH/2;
		g.drawImage(middleSeperatorImg, Game.GAME_WIDTH/2-middleSeperatorWidth/2 + xDrawOffset, 0, middleSeperatorWidth, Game.GAME_HEIGHT, null);
		
		// Health bar
		g.setColor(Color.red);
		g.fillRect(healthBarXStart + statusBarX, healthBarYStart + statusBarY, healthWidth, healthBarHeight);

		// Power Bar
		g.setColor(Color.yellow);
		g.fillRect(powerBarXStart + statusBarX, powerBarYStart + statusBarY, powerWidth, powerBarHeight);
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
					airSpeed = 0f;
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
			else if ((System.nanoTime()-startTimeInAir)/1000000000.0f > 0.2f)
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
				updateXPos(xSpeed);
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
			updateXPos(xSpeed);
		}
		moving = true;
	}

	private void jump() {
		if (inAir)
			return;
		playing.getGame().getAudioPlayer().playEffect(AudioPlayer.JUMP);
		inAir = true;
		airSpeed = jumpSpeed;
	}

	private void resetInAir() {
		inAir = false;
		airSpeed = 0;
	}

	private void updateXPos(float xSpeed) {
		
		if (CanMoveHere(hitbox.x + xSpeed, hitbox.y, hitbox.width, hitbox.height, lvlData))
			hitbox.x += xSpeed;
		else {
			
			//hitbox.x = GetEntityXPosNextToWall(hitbox, xSpeed);
			float[] playerCoord = GetEntityXPosNextToWall(hitbox, xSpeed, lvlData, 0.1f);
			if (CanMoveHere(playerCoord[0], playerCoord[1], hitbox.width, hitbox.height, lvlData)) {
				hitbox.x = playerCoord[0];
				hitbox.y = playerCoord[1];
			}
			else if (CanMoveHere(playerCoord[0], playerCoord[1]-5.0f, hitbox.width, hitbox.height, lvlData)) {
				hitbox.x = playerCoord[0];
				hitbox.y = playerCoord[1]-5.0f;
				// System.out.println("need little help to get up the hill");
			}
			else {
				// System.out.println("failed to move (slope uphill | next to wall) due to !CanMoveHere()");
			}
				
			if (powerAttackActive) {
				powerAttackActive = false;
				powerAttackTick = 0;
			}
		}
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
		if (state == HIT)
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
		BufferedImage img = LoadSave.GetSpriteAtlas(LoadSave.PLAYER_ATLAS);
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
				//animations[j][i] = img.getSubimage(i * spriteImgWidth, j * spriteImgHeight, spriteImgWidth, spriteImgHeight);
				if (i<GetSpriteAmount(j)) {
					if (Files.exists(Paths.get("res/" + baseDir + "/" + fileName + i + ".png"))) {
						animations[j][i] = LoadSave.GetSpriteAtlas(baseDir + "/" + fileName + i + ".png");
					}
					else {
						animations[j][i] = LoadSave.GetSpriteAtlas(baseDir + "/idle0.png");
						System.out.println("file does not exist: " + baseDir + "/" + fileName + i + ".png");
					}
					
					if (j == IDLE || j == RUNNING || j == JUMP || j == FALLING) {
						// carry animations
						if (Files.exists(Paths.get("res/" + baseDir + "/carry" + fileName + i + ".png"))) {
							animations[j+NUM_ANIMATIONS][i] = LoadSave.GetSpriteAtlas(baseDir + "/carry" + fileName + i + ".png");
						}
						else {
							animations[j+NUM_ANIMATIONS][i] = LoadSave.GetSpriteAtlas(baseDir + "/idle0.png");
							System.out.println("file does not exist: " + baseDir + "/carry" + fileName + i + ".png");
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
		resetAttackBox();

		if (!IsEntityOnFloor(hitbox, lvlData))
			inAir = true;
	}

	private void resetAttackBox() {
		if (flipW == 1)
			setAttackBoxOnRightSide();
		else
			setAttackBoxOnLeftSide();
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

}