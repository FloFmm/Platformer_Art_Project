package entities;

import static utilz.Constants.EnemyConstants.*;
import static utilz.HelpMethods.CanMoveHere;
import static utilz.HelpMethods.GetEntityXPosNextToWall;
import static utilz.HelpMethods.IsEntityOnFloor;

import audio.AudioPlayer;

import static utilz.Constants.ANI_SPEED;
import static utilz.Constants.GRAVITY;
import static utilz.Constants.UPS_SET;

import gamestates.Playing;
import main.Game;

public class Tumbleweed extends Enemy {
	private float fallSpeedAfterCollision = 0.5f * Game.SCALE;
	private boolean moving = false;
	private int[][] lvlData;
	
	public Tumbleweed(float x, float y, int[][] lvlData) {
		super(x, y, TUMBLE_WEED_WIDTH, TUMBLE_WEED_HEIGHT, TUMBLE_WEED);
		this.lvlData = lvlData;
		initHitbox(TUMBLE_WEED_HITBOX_WIDTH_DEFAULT, TUMBLE_WEED_HITBOX_HEIGHT_DEFAULT);
		initAttackBox(TUMBLE_WEED_HITBOX_WIDTH_DEFAULT, TUMBLE_WEED_HITBOX_HEIGHT_DEFAULT, 0);
		
	}

	public void update(int[][] lvlData, Playing playing) {
		updateBehavior(lvlData, playing);
		updateAnimationTick();
		updatePos(playing.getWindSpeed());
		updateAttackBox();
		
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
			} else if (aniIndex == GetSpriteAmount(TUMBLE_WEED, DEAD) - 1 && aniTick >= ANI_SPEED - 1) {
				kill();
			}
		}
	}

	private void updateBehavior(int[][] lvlData, Playing playing) {
		if (firstUpdate)
			firstUpdateCheck(lvlData);

		if (inAir) {
			inAirChecks(lvlData, playing);
		} else {
			switch (state) {
			case IDLE:
				if (Math.abs(xSpeed) > 0)
					newState(RUNNING);
				break;
			case RUNNING:
				if (!playing.getPlayer1().getPowerAttackActive()) {
					checkPlayerHit(attackBox, playing.getPlayer1());
				}
				if (!playing.getPlayer1().getPowerAttackActive()) {
					checkPlayerHit(attackBox, playing.getPlayer2());
				}
				break;
			case HIT:
				if (aniIndex <= GetSpriteAmount(enemyType, state) - 2)
					pushBack(pushBackDir, lvlData, 2f);
				updatePushBackDrawOffset();
				break;
			}
		}
	}
	
	private void updatePos(float windSpeed) {
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
		moving = true;
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
	
	public void kill() {
		resetTumbleWeed();
	}	
}