package entities;

import static utilz.Constants.EnemyConstants.*;
import static utilz.Constants.TetrisTileConstants.GetTetrisTileShape;
import static utilz.Constants.TetrisTileConstants.TETRIS_GRID_SIZE;
import static utilz.Constants.TetrisTileConstants.TETRIS_TILE_TIME_TO_EXPLODE;
import static utilz.Constants.TetrisTileConstants.TETRIS_TILE_TIME_TO_REACH_WINDSPEED;
import static utilz.Constants.TetrisTileConstants.TETRIS_TILE_TIME_TO_STOP_WHEN_IS_ON_FLOOR;
import static utilz.HelpMethods.CanMoveHere;
import static utilz.HelpMethods.GetEntityXPosNextToWall;
import static utilz.HelpMethods.IsEntityOnFloor;
import static utilz.HelpMethods.IsFloor;
import static utilz.Constants.GRAVITY;
import static utilz.Constants.UPS_SET;
import static utilz.Constants.Dialogue.*;

import gamestates.Playing;
import main.Game;
import zones.BuildingZone;

public class Tumbleweed extends Enemy {
	private float fallSpeedAfterCollision = 0.5f * Game.SCALE;
	private boolean moving = false;
	private int[][] lvlData;
	
	public Tumbleweed(float x, float y, int[][] lvlData) {
		super(x, y, TUMBLE_WEED_WIDTH, TUMBLE_WEED_HEIGHT, TUMBLE_WEED);
		this.lvlData = lvlData;
		initHitbox(TUMBLE_WEED_WIDTH, TUMBLE_WEED_HEIGHT);
		initAttackBox(TUMBLE_WEED_WIDTH, TUMBLE_WEED_HEIGHT, 0);
		
	}

	public void update(int[][] lvlData, Playing playing) {
		updateBehavior(lvlData, playing);
		updateAnimationTick();
		updatePos(playing.getWindSpeed());
		updateAttackBox();
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

}