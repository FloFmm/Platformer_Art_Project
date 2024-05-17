package entities;

import static utilz.Constants.TetrisTileConstants.*;
import static utilz.Constants.UPS_SET;
import static utilz.HelpMethods.*;
import static utilz.Constants.*;
import static utilz.Constants.PlayerConstants.HITBOX_BASE_HEIGHT;
import static utilz.Constants.PlayerConstants.HITBOX_BASE_WIDTH;

import java.util.Random;

import gamestates.Playing;
import levels.Level;
import main.Game;
import zones.BuildingZone;
import java.awt.geom.Rectangle2D;
import java.util.Random;
public class TetrisTile extends Entity {
	
	private int[][] lvlData;
	private float fallSpeedAfterCollision = 0.5f * Game.SCALE;
	private int rotation = 0;
	private int tileY = 0;
	private int tileIndex;
	private BuildingZone lockedInBuildingZone = null;
	private TetrisTileManager tetrisTileManager;
	private float xSpeed = 0;
	private float xDrawOffset=0;
	private float yDrawOffset=0;
	private Player isCarriedBy;
	int[][] matrix;
	boolean movingInRaster = false;

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
	
	private boolean tetrisTileCanMoveHere(float x, float y, BuildingZone buildingZone) {
		double xGridIndex = (double) Math.round(x)/TETRIS_GRID_SIZE;
		double yGridIndex = (double) Math.round(y)/TETRIS_GRID_SIZE;
		int xGridIndexFloor = (int) Math.floor(xGridIndex)*TETRIS_GRID_SIZE;
		int yGridIndexFloor = (int) Math.floor(yGridIndex)*TETRIS_GRID_SIZE;
		int xGridIndexCeil = (int) Math.ceil(xGridIndex)*TETRIS_GRID_SIZE;
		int yGridIndexCeil = (int) Math.ceil(yGridIndex)*TETRIS_GRID_SIZE;

		if (matrixContainsValue(buildingZone.addTetrisTileMatrix(xGridIndexCeil, yGridIndexCeil, 
				matrix, xDrawOffset, yDrawOffset), 2)) {
			return false;
		}
		
		if (matrixContainsValue(buildingZone.addTetrisTileMatrix(xGridIndexCeil, yGridIndexFloor, 
				matrix, xDrawOffset, yDrawOffset), 2)) {
			return false;
		}
		
		if (matrixContainsValue(buildingZone.addTetrisTileMatrix(xGridIndexFloor, yGridIndexCeil, 
				matrix, xDrawOffset, yDrawOffset), 2)) {
			return false;
		}
			
		if (matrixContainsValue(buildingZone.addTetrisTileMatrix(xGridIndexFloor, yGridIndexFloor, 
				matrix, xDrawOffset, yDrawOffset), 2)) {
			return false;
		}
		
		return true;
	}
	
	private int[] closestLockingXY(float x, float y, BuildingZone buildingZone) {
		double xGridIndex = (double) Math.round(x)/TETRIS_GRID_SIZE;
		double yGridIndex = (double) Math.round(y)/TETRIS_GRID_SIZE;
		int xGridIndexFloor = (int) Math.floor(xGridIndex)*TETRIS_GRID_SIZE;
		int yGridIndexFloor = (int) Math.floor(yGridIndex)*TETRIS_GRID_SIZE;
		int xGridIndexCeil = (int) Math.ceil(xGridIndex)*TETRIS_GRID_SIZE;
		int yGridIndexCeil = (int) Math.ceil(yGridIndex)*TETRIS_GRID_SIZE;
		int xGridIndexRound = (int) Math.round(xGridIndex)*TETRIS_GRID_SIZE;
		int yGridIndexRound = (int) Math.round(yGridIndex)*TETRIS_GRID_SIZE;
		int xGridIndexNotRound = xGridIndexFloor, yGridIndexNotRound = yGridIndexFloor;
		if (xGridIndexNotRound == xGridIndexRound)
			xGridIndexNotRound = xGridIndexCeil;
		if (yGridIndexNotRound == yGridIndexRound)
			yGridIndexNotRound = yGridIndexCeil;
		

		if (!matrixContainsValue(buildingZone.addTetrisTileMatrix(xGridIndexRound, yGridIndexRound, 
				matrix, xDrawOffset, yDrawOffset), 2)) {
			return new int[] {xGridIndexRound, yGridIndexRound};
		}
//		printArray(buildingZone.addTetrisTileMatrix(xGridIndexRound, yGridIndexRound, 
//				matrix, xDrawOffset, yDrawOffset));
//		System.out.println("==========================");
//		
		if (!matrixContainsValue(buildingZone.addTetrisTileMatrix(xGridIndexRound, yGridIndexNotRound, 
				matrix, xDrawOffset, yDrawOffset), 2)) {
			return new int[] {xGridIndexRound, yGridIndexNotRound};
		}
//		printArray(buildingZone.addTetrisTileMatrix(xGridIndexRound, yGridIndexNotRound, 
//				matrix, xDrawOffset, yDrawOffset));
//		System.out.println("==========================");
//		
		if (!matrixContainsValue(buildingZone.addTetrisTileMatrix(xGridIndexNotRound, yGridIndexRound, 
				matrix, xDrawOffset, yDrawOffset), 2)) {
			return new int[] {xGridIndexNotRound, yGridIndexRound};
		}
//		printArray(buildingZone.addTetrisTileMatrix(xGridIndexNotRound, yGridIndexRound, 
//				matrix, xDrawOffset, yDrawOffset));
//		System.out.println("==========================");
//		
		if (!matrixContainsValue(buildingZone.addTetrisTileMatrix(xGridIndexNotRound, yGridIndexNotRound, 
				matrix, xDrawOffset, yDrawOffset), 2)) {
			return new int[] {xGridIndexNotRound, yGridIndexNotRound};
		}
//		printArray(buildingZone.addTetrisTileMatrix(xGridIndexNotRound, yGridIndexNotRound, 
//				matrix, xDrawOffset, yDrawOffset));
//		System.out.println("==========================");
//		
		return null;
	}
	
	private void updateHitBox() {
		//TODO
		matrix = GetTetrisTileShape(tileIndex, rotation);
		int maxRowIndex = -1;
        int minRowIndex = getMatrix().length;
        int maxColIndex = -1;
        int minColIndex = getMatrix()[0].length;
        
        // Iterate through the matrix to find the max and min indices
        for (int i = 0; i < getMatrix().length; i++) {
            for (int j = 0; j < getMatrix()[0].length; j++) {
                if (getMatrix()[i][j] == 1) {
                    // Update max and min indices for rows
                    maxRowIndex = Math.max(maxRowIndex, i);
                    minRowIndex = Math.min(minRowIndex, i);
                    
                    // Update max and min indices for columns
                    maxColIndex = Math.max(maxColIndex, j);
                    minColIndex = Math.min(minColIndex, j);
                }
            }
        }
        
        xDrawOffset = minColIndex * Game.TILES_SIZE/4;
        yDrawOffset = minRowIndex * Game.TILES_SIZE/4;
		hitbox.width = (maxColIndex - minColIndex + 1) * Game.TILES_SIZE/4;
		hitbox.height =  (maxRowIndex - minRowIndex + 1) * Game.TILES_SIZE/4;
	}

	private void updatePos(float windSpeed) {
		if (lockedInBuildingZone != null)
			return;
		
		if (isCarriedBy != null) {
			hitbox.x = isCarriedBy.hitbox.x + isCarriedBy.hitbox.width/2 - hitbox.width/2; 
			hitbox.y = isCarriedBy.hitbox.y - hitbox.height;
			xSpeed = 0;
			return;
		}
		
		
		if (IsEntityOnFloor(hitbox, lvlData)) {
			if (Math.abs(xSpeed) > 0) {
				float abs_deceleration_on_floor = Math.abs(windSpeed/(UPS_SET*TETRIS_TILE_TIME_TO_STOP_WHEN_IS_ON_FLOOR));
				if (Math.abs(xSpeed) > abs_deceleration_on_floor)
					xSpeed -= Math.signum(xSpeed)*abs_deceleration_on_floor;
				else
					xSpeed = 0;
			}
		}
		else {
			inAir = true;
			if (Math.abs(xSpeed) < Math.abs(windSpeed))
				xSpeed += windSpeed/(UPS_SET*TETRIS_TILE_TIME_TO_REACH_WINDSPEED);
		}
			
 
		if (inAir) {
			if (CanMoveHere(hitbox.x, hitbox.y + airSpeed, hitbox.width, hitbox.height, lvlData)) {
				if (isInBuildingZone() && airSpeed > 0) {
					BuildingZone currentBuildingZone = tetrisTileManager.getPlaying().getBuildingZoneManager().checkInBuildingZone(hitbox);
					if (!tetrisTileCanMoveHere(hitbox.x, hitbox.y + airSpeed, currentBuildingZone)) {
						int[] xy = closestLockingXY(hitbox.x, hitbox.y + airSpeed, currentBuildingZone);
						if (xy != null) {
							hitbox.x = xy[0];
							hitbox.y = xy[1];
							xSpeed = 0;
							if (movingInRaster) {
								airSpeed = 0;
								inAir = false;
								lockedInBuildingZone = currentBuildingZone;
								lockedInBuildingZone.addTetrisTile(this);
							}
							else {
								airSpeed = 4.0f;
								System.out.println("208");
								movingInRaster = true;
							}
						}
					}
				}
				
				hitbox.y += airSpeed;
				airSpeed += GRAVITY;
				updateXPos(xSpeed);
			} else {
				if (isInBuildingZone() && airSpeed > 0) {
					lockedInBuildingZone = tetrisTileManager.getPlaying().getBuildingZoneManager().checkInBuildingZone(hitbox);
					airSpeed = 0;
					inAir = false;
					
					int[] xy = closestLockingXY(hitbox.x, hitbox.y + airSpeed, lockedInBuildingZone);
					if (xy != null) {
						hitbox.x = xy[0];
						hitbox.y = (int) Math.floor((double) Math.round(hitbox.y + airSpeed)/TETRIS_GRID_SIZE)*TETRIS_GRID_SIZE;
						xSpeed = 0;
						lockedInBuildingZone.addTetrisTile(this);
					}
				}
				else {
					//TODO
					//hitbox.y = GetEntityYPosUnderRoofOrAboveFloor(hitbox, airSpeed);
					if (airSpeed > 0) {
						inAir = false;
						airSpeed = 0;
					}
					else
						airSpeed = fallSpeedAfterCollision;
					// TODO
					// updateXPos(xSpeed);
				}
			}

		} else {
			updateXPos(xSpeed);
		}
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

	public void explosion() {
		isCarriedBy = null;
		movingInRaster = false;
		inAir = true;
		lockedInBuildingZone = null;
		Random random = new Random();
		hitbox.y -= 100.0f;
        xSpeed = random.nextFloat()*
        		(TETRIS_TILE_MAX_EXPLOSION_X_SPEED - TETRIS_TILE_MIN_EXPLOSION_X_SPEED) + 
        		TETRIS_TILE_MIN_EXPLOSION_X_SPEED;
		airSpeed = -(random.nextFloat()*
        		(TETRIS_TILE_MAX_EXPLOSION_Y_SPEED - TETRIS_TILE_MIN_EXPLOSION_Y_SPEED) + 
        		TETRIS_TILE_MIN_EXPLOSION_Y_SPEED);
	}
	
	private boolean isInBuildingZone() {
		float[] xCoordinates = {hitbox.x, (hitbox.x+hitbox.width-1), hitbox.x, (hitbox.x+hitbox.width-1)};
		float[] yCoordinates = {hitbox.y, hitbox.y, (hitbox.y+hitbox.height-1), (hitbox.y+hitbox.height-1)};
		int xIndex, yIndex;
		for (int i = 0; i < 4; i++) {
			xIndex = (int) (xCoordinates[i]/Game.TILES_SIZE);
			yIndex = (int) (yCoordinates[i]/Game.TILES_SIZE);
			if (lvlData[yIndex][xIndex] == 3)
				return true;
		}
		return false;
	}
	
	public void loadLvlData(int[][] lvlData) {
		this.lvlData = lvlData;
		if (!IsEntityOnFloor(hitbox, lvlData))
			inAir = true;
	}
	
	public void resetTetrisTile() {
		System.out.println("312");
		xSpeed = 0;
		airSpeed = 0;
		isCarriedBy = null;
		movingInRaster = false;

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
	
	public float getXDrawOffset() {
		return xDrawOffset;
	}
	
	public float getYDrawOffset() {
		return yDrawOffset;
	}


	public BuildingZone getLockedInBuildingZone() {
		return lockedInBuildingZone;
	}
	
	public float getXSpeed() {
		return xSpeed;
	}
	public void setXSpeed(float xSpeed) {
		this.xSpeed = xSpeed;
	}
	
	public Player getIsCarriedBy() {
		return isCarriedBy;
	}

	public void setIsCarriedBy(Player isCarriedBy) {
		this.isCarriedBy = isCarriedBy;
	}
	
	public void setRotation(int rotation) {
		this.rotation = rotation;
	}

	public void setTetrisTileManager(TetrisTileManager tetrisTileManager) {
		this.tetrisTileManager = tetrisTileManager;		
	}

	public int[][] getMatrix() {
		return matrix;
	}

	public void setLockedInBuildingZone(BuildingZone lockedInBuildingZone) {
		this.lockedInBuildingZone = lockedInBuildingZone;		
	}
}