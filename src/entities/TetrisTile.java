package entities;

import static utilz.Constants.TetrisTileConstants.*;
import static utilz.HelpMethods.*;
import static utilz.Constants.*;

import java.util.Random;

import gamestates.Playing;
import main.Game;

public class TetrisTile extends Entity {
	
	private boolean moving = false;
	private int[][] lvlData;
	private float fallSpeedAfterCollision = 0.5f * Game.SCALE;
	private int rotation = 0;
	private int tileY = 0;
	private int tileIndex;
	private int xSpeed = 0;
	int[][] matrix;

	public TetrisTile(float x, float y, int width, int height, int tileIndex, int[][] lvlData) {
		super(x, y, width, height);
		this.tileIndex = tileIndex;
		this.lvlData = lvlData;
		Random random = new Random();
		this.rotation = random.nextInt(4);
		matrix = GetTetrisTileShape (tileIndex, rotation);
		initHitbox(TETRIS_TILE_WIDTH_DEFAULT, TETRIS_TILE_HEIGHT_DEFAULT);
	}

	public void update(Playing playing) {
		updatePos(playing.getWindSpeed());
		updateHitBox();
	}

	private void updateHitBox() {
		//TODO
		matrix = GetTetrisTileShape(tileIndex, rotation);
		int maxRowIndex = -1;
        int minRowIndex = matrix.length;
        int maxColIndex = -1;
        int minColIndex = matrix[0].length;
        
        // Iterate through the matrix to find the max and min indices
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                if (matrix[i][j] == 1) {
                    // Update max and min indices for rows
                    maxRowIndex = Math.max(maxRowIndex, i);
                    minRowIndex = Math.min(minRowIndex, i);
                    
                    // Update max and min indices for columns
                    maxColIndex = Math.max(maxColIndex, j);
                    minColIndex = Math.min(minColIndex, j);
                }
            }
        }
        
        //hitbox.x = x + minColIndex * Game.TILES_SIZE/4;
        //hitbox.y = y + minRowIndex * Game.TILES_SIZE/4;
		hitbox.width = (maxColIndex - minColIndex + 1) * Game.TILES_SIZE/4;
		hitbox.height =  (maxRowIndex - minRowIndex + 1) * Game.TILES_SIZE/4;
	}

	private void updatePos(float windSpeed) {
		moving = false;
		
		if (Math.abs(windSpeed) < Math.abs(windSpeed))
			xSpeed += windSpeed/(200*4);


		if (!inAir)
			if (!IsEntityOnFloor(hitbox, lvlData))
				inAir = true;
 
		if (inAir) {
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
				
		}
	}

	public void loadLvlData(int[][] lvlData) {
		this.lvlData = lvlData;
		if (!IsEntityOnFloor(hitbox, lvlData))
			inAir = true;
	}
	
	public void resetTetrisTile() {
		xSpeed = 0;
		airSpeed = 0;

	}


	public int getTileY() {
		return tileY;
	}

	public int getTileIndex() {
		return tileIndex;
	}

	public int getRotation() {
		return rotation;
	}
}