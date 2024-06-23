package entities;

import static utilz.Constants.EnemyConstants.*;
import static utilz.Constants.Environment.WATER_DMG_PER_SECOND;
import static utilz.Constants.Directions.*;
import static utilz.HelpMethods.CanMoveHere;
import static utilz.HelpMethods.GetEntityXPosNextToWall;
import static utilz.HelpMethods.IsEntityOnFloor;

import audio.AudioPlayer;

import static utilz.Constants.GRAVITY;
import static utilz.Constants.UPS_SET;

import gamestates.Playing;
import main.Game;

public class Tumbleweed extends Enemy {
	private float fallSpeedAfterCollision = 0.5f * Game.SCALE;
	private boolean moving = false;
	private int[][] lvlData;
	private float lastTimeRunning;
	
	public Tumbleweed(float x, float y, int[][] lvlData) {
		super(x, y, TUMBLE_WEED_WIDTH, TUMBLE_WEED_HEIGHT, TUMBLE_WEED);
		this.lvlData = lvlData;
		initHitbox(TUMBLE_WEED_HITBOX_WIDTH_DEFAULT, TUMBLE_WEED_HITBOX_HEIGHT_DEFAULT);
		initAttackBox(TUMBLE_WEED_HITBOX_WIDTH_DEFAULT, TUMBLE_WEED_HITBOX_HEIGHT_DEFAULT, 0);
		
	}

	public void update(int[][] lvlData, Playing playing) {
		if (currentHealth <= 0) {
			if (state != DEAD) {
				state = DEAD;
				aniTick = 0;
				aniIndex = 0;
				playing.getGame().getAudioPlayer().playEffect(AudioPlayer.DIE);

				// Check if died in air
				if (!IsEntityOnFloor(hitbox, lvlData)) {
					inAir = true;
					airSpeed = 0;
				}
			} else if (aniIndex == GetSpriteAmount(TUMBLE_WEED, DEAD) - 1 && aniTick >= aniSpeed - 1) {
				resetTumbleWeed();
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
		
		updatePos(playing.getWindSpeed());
		updateAttackBox();
		
		playing.getObjectManager().checkSpikesTouched(this);
		
		// check inside water
		if (hitbox.y+hitbox.height*0.75f > playing.getCurrentWaterYPos())
			currentHealth -= WATER_DMG_PER_SECOND / UPS_SET;

		updateAnimationTick();
		setAnimation(lvlData, playing);
		
		if (!playing.getPlayer1().getPowerAttackActive()) 
			checkPlayerHit(attackBox, playing.getPlayer1());
		
		if (!playing.getPlayer1().getPowerAttackActive()) 
			checkPlayerHit(attackBox, playing.getPlayer2());
	}

	private void setAnimation(int[][] lvlData, Playing playing) {
		aniSpeed = (int) (TUMBLE_WEED_MIN_ANI_SPEED - Math.abs(xSpeed) / TUMBLE_WEED_MAX_SPEED * (TUMBLE_WEED_MIN_ANI_SPEED - TUMBLE_WEED_MAX_ANI_SPEED));
		int startAni = state;

		if (state == HIT)
			return;

		if (moving) {
			state = RUNNING;
			lastTimeRunning = playing.getGameTimeInSeconds();
		}
		else if ((playing.getGameTimeInSeconds()-lastTimeRunning) > 0.2f)
			state = IDLE;
		if (startAni != state)
			resetAniTick();
	}
	
	private void updatePos(float windSpeed) {
		float oldXPos = hitbox.x;
		float oldYPos = hitbox.y;
		if ((Math.signum(xSpeed) == Math.signum(windSpeed) && Math.abs(xSpeed) < Math.abs(TUMBLE_WEED_MAX_SPEED)) || Math.signum(xSpeed) != Math.signum(windSpeed))
			xSpeed += windSpeed/(UPS_SET*TUMBLE_WEED_TIME_TO_REACH_WIND_SPEED);
		if (!inAir)
			if (!IsEntityOnFloor(hitbox, lvlData))
				inAir = true;
		if (inAir) {
			if (CanMoveHere(hitbox.x, hitbox.y + airSpeed, hitbox.width, hitbox.height, lvlData)) {
				hitbox.y += airSpeed;
				airSpeed += GRAVITY;
				updateXPos(xSpeed);
			} else {
				if (airSpeed > 0)
					resetInAir();
				else
					airSpeed = fallSpeedAfterCollision;
			}

		} else {
			updateXPos(xSpeed);
		}
		float floatError = 0.0001f;
		moving = !(oldXPos - floatError <= hitbox.x && hitbox.x <= oldXPos + floatError &&
				oldYPos - floatError <= hitbox.y && hitbox.y <= oldYPos + floatError);
		if (moving)
			if (hitbox.x > oldXPos)
				walkDir = RIGHT;
			else
				walkDir = LEFT;
	}

	private void updateXPos(float xSpeed) {
		if (CanMoveHere(hitbox.x + xSpeed, hitbox.y, hitbox.width, hitbox.height, lvlData))
			hitbox.x += xSpeed;
		else {
			float[] playerCoord = GetEntityXPosNextToWall(hitbox, xSpeed, lvlData, 0.1f);
			if (CanMoveHere(playerCoord[0], playerCoord[1], hitbox.width, hitbox.height, lvlData)) {
				hitbox.x = playerCoord[0];
				hitbox.y = playerCoord[1];
			}
			else if (CanMoveHere(playerCoord[0], playerCoord[1]-5.0f, hitbox.width, hitbox.height, lvlData)) {
				hitbox.x = playerCoord[0];
				hitbox.y = playerCoord[1]-5.0f;
			}
		}
	}
	
	private void resetInAir() {
		inAir = false;
		airSpeed = 0;
	}
	
	private void resetTumbleWeed() {
		state = IDLE;
		currentHealth = maxHealth;

		if (!IsEntityOnFloor(hitbox, lvlData))
			inAir = true;
		
		xSpeed = 0;
		airSpeed = 0;
		moving = false;
		
		hitbox.x = x;
		hitbox.y = y;
		attackBox.x = x;
		attackBox.y = y;
		if (!IsEntityOnFloor(hitbox, lvlData))
			inAir = true;
	}
	
	private void resetAniTick() {
		aniTick = 0;
		aniIndex = 0;
	}
	
	public boolean getMoving() {
		return moving;
	}
}