package entities;

import gamestates.Playing;
import main.Game;
import zones.BuildingZone;

import java.util.List;
import java.util.Random;

import static utilz.Constants.EnemyConstants.TUMBLE_WEED_MAX_SPEED;
import static utilz.Constants.Environment.TEMP_FROM_ROCKET_EXPLOSION;
import static utilz.Constants.Environment.TEMP_FROM_WINDMILL_EXPLOSION;
import static utilz.Constants.GRAVITY;
import static utilz.Constants.ObjectConstants.*;
import static utilz.Constants.TetrisTileConstants.*;
import static utilz.Constants.UPS_SET;
import static utilz.HelpMethods.*;

public class TetrisTile extends Entity {

    private int[][] lvlData;
    private float fallSpeedAfterCollision = 0.5f * Game.SCALE;
    private int rotation = 0;
    private int tileY = 0;
    private int tileIndex;
    private BuildingZone lockedInBuildingZone = null;
    private TetrisTileManager tetrisTileManager;
    private float xDrawOffset = 0;
    private float yDrawOffset = 0;
    private Player isCarriedBy;
    int[][] matrix;
    boolean movingInGrid = false;
    boolean moving = false;
    private float explosionStartTime = -1;
    private String explosionType = "small";
    private BuildingZone currentBZ;

    private boolean isPredictionTile = false;


    public TetrisTile(float x, float y, int width, int height, int tileIndex, int[][] lvlData, boolean isPredictionTile) {
        super(x, y, width, height);
        this.tileIndex = tileIndex;
        this.lvlData = lvlData;
        this.isPredictionTile = isPredictionTile;
        Random random = new Random();
        this.rotation = random.nextInt(4);
        matrix = GetTetrisTileShape(tileIndex, rotation);
        initHitbox(TETRIS_TILE_WIDTH_DEFAULT, TETRIS_TILE_HEIGHT_DEFAULT);
    }

    public void update(Playing playing) {
        if (explosionStartTime != -1 && (tetrisTileManager.getPlaying().getGameTimeInSeconds() - explosionStartTime) > TETRIS_TILE_TIME_TO_EXPLODE)
            explosion();
        updatePos(playing.getWindSpeed());
        updateHitBox();
        if (moving && !movingInGrid && isCarriedBy == null)
            checkSpikesTouched(playing);

    }

    private boolean tetrisTileCanMoveHere(float x, float y, BuildingZone buildingZone) {
        double xGridIndex = (double) Math.round(x) / TETRIS_GRID_SIZE;
        double yGridIndex = (double) Math.round(y) / TETRIS_GRID_SIZE;
        int xGridIndexFloor = (int) Math.floor(xGridIndex) * TETRIS_GRID_SIZE;
        int yGridIndexFloor = (int) Math.floor(yGridIndex) * TETRIS_GRID_SIZE;
        int xGridIndexCeil = (int) Math.ceil(xGridIndex) * TETRIS_GRID_SIZE;
        int yGridIndexCeil = (int) Math.ceil(yGridIndex) * TETRIS_GRID_SIZE;

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
        double xGridIndex = (double) Math.round(x) / TETRIS_GRID_SIZE;
        double yGridIndex = (double) Math.round(y) / TETRIS_GRID_SIZE;
        int xGridIndexFloor = (int) Math.floor(xGridIndex) * TETRIS_GRID_SIZE;
        int yGridIndexFloor = (int) Math.floor(yGridIndex) * TETRIS_GRID_SIZE;
        int xGridIndexCeil = (int) Math.ceil(xGridIndex) * TETRIS_GRID_SIZE;
        int yGridIndexCeil = (int) Math.ceil(yGridIndex) * TETRIS_GRID_SIZE;
        int xGridIndexRound = (int) Math.round(xGridIndex) * TETRIS_GRID_SIZE;
        int yGridIndexRound = (int) Math.round(yGridIndex) * TETRIS_GRID_SIZE;
        int xGridIndexNotRound = xGridIndexFloor, yGridIndexNotRound = yGridIndexFloor;
        if (xGridIndexNotRound == xGridIndexRound)
            xGridIndexNotRound = xGridIndexCeil;
        if (yGridIndexNotRound == yGridIndexRound)
            yGridIndexNotRound = yGridIndexCeil;

        int[][] positions = new int[][]{
                {xGridIndexRound, yGridIndexRound},
                {xGridIndexRound, yGridIndexNotRound},
                {xGridIndexNotRound, yGridIndexRound},
                {xGridIndexNotRound, yGridIndexNotRound},
        };

        int[] returnV = null;
        // check for not overlapping with other tetris tiles AND not outside of goal
        for (int[] pos : positions) {
            int[][] resultMatrixAfterAddingTT = buildingZone.addTetrisTileMatrix(xGridIndexRound, yGridIndexRound, matrix, xDrawOffset, yDrawOffset);
            boolean noOverlapBetweenTetrisTiles = !matrixContainsValue(resultMatrixAfterAddingTT, 2);
            boolean isCompletable = buildingZone.isCompletable(resultMatrixAfterAddingTT);
            if (noOverlapBetweenTetrisTiles && isCompletable) {
                return pos;
            }

            if (noOverlapBetweenTetrisTiles && returnV == null) {
                returnV = pos;
                //printMatrix(resultMatrixAfterAddingTT);
            }
        }
        return returnV;
    }

    public void updateHitBox() {
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

        xDrawOffset = minColIndex * Game.TILES_SIZE / 4;
        yDrawOffset = minRowIndex * Game.TILES_SIZE / 4;
        hitbox.width = (maxColIndex - minColIndex + 1) * Game.TILES_SIZE / 4;
        hitbox.height = (maxRowIndex - minRowIndex + 1) * Game.TILES_SIZE / 4;
    }

    public void updatePos(float windSpeed) {
        float oldXPos = hitbox.x;
        float oldYPos = hitbox.y;

        // if already build in a building zone
        if (lockedInBuildingZone != null) {
            return;
        }

        // if carried by player
        if (isCarriedBy != null) {
            hitbox.x = isCarriedBy.hitbox.x + isCarriedBy.hitbox.width / 2 - hitbox.width / 2;
            hitbox.y = isCarriedBy.hitbox.y - hitbox.height;
            xSpeed = 0;
            return;
        }

        // acceleration in air and deceleration on floor
        if (IsEntityOnFloor(hitbox, lvlData)) {
            if (Math.abs(xSpeed) > 0) {
                float abs_deceleration_on_floor = Math.abs(windSpeed / (UPS_SET * TETRIS_TILE_TIME_TO_STOP_WHEN_IS_ON_FLOOR));
                if (Math.abs(xSpeed) > abs_deceleration_on_floor)
                    xSpeed -= Math.signum(xSpeed) * abs_deceleration_on_floor;
                else
                    xSpeed = 0;
            }
        } else {
            inAir = true;
            if ((Math.signum(xSpeed) == Math.signum(windSpeed) && Math.abs(xSpeed) < Math.abs(TUMBLE_WEED_MAX_SPEED)) || Math.signum(xSpeed) != Math.signum(windSpeed))
                xSpeed += windSpeed / (UPS_SET * TETRIS_TILE_TIME_TO_REACH_WINDSPEED);
        }


        if (inAir) {
            // in air
            if (CanMoveHere(hitbox.x, hitbox.y + airSpeed, hitbox.width, hitbox.height, lvlData)) {
                // can move in y direction
                if (isInBuildingZone() && airSpeed > 0) {
                    BuildingZone currentBuildingZone = tetrisTileManager.getPlaying().getBuildingZoneManager().checkInBuildingZone(hitbox);
                    if (!tetrisTileCanMoveHere(hitbox.x, hitbox.y + airSpeed, currentBuildingZone)) {
                        int[] xy = closestLockingXY(hitbox.x, hitbox.y + airSpeed, currentBuildingZone);
                        if (xy != null) {
                            hitbox.x = xy[0];
                            hitbox.y = xy[1];
                            xSpeed = 0;
                            if (movingInGrid) {
                                currentBuildingZone.addTetrisTile(this);
                                movingInGrid = false;
                            } else {
                                airSpeed = 0f;
                                movingInGrid = true;
                            }
                        }
                    }
                }

                hitbox.y += airSpeed;
                airSpeed += GRAVITY;
                if (!movingInGrid)
                    updateXPos(xSpeed, lvlData);
            } else {
                // cannot move in y direction
                if (isInBuildingZone() && airSpeed > 0) {
                    BuildingZone currentBZ = tetrisTileManager.getPlaying().getBuildingZoneManager().checkInBuildingZone(hitbox);
                    airSpeed = 0;
                    inAir = false;

                    int[] xy = closestLockingXY(hitbox.x, hitbox.y + airSpeed, currentBZ);
                    if (xy != null) {
                        hitbox.x = xy[0];
                        hitbox.y = xy[1];//(int) Math.floor((double) Math.round(hitbox.y + airSpeed)/TETRIS_GRID_SIZE)*TETRIS_GRID_SIZE;
                        currentBZ.addTetrisTile(this);
                    }
                } else {
                    if (airSpeed > 0) {
                        inAir = false;
                        airSpeed = 0;
                    } else
                        airSpeed = fallSpeedAfterCollision;
                    // TODO
                    if (!movingInGrid)
                        updateXPos(xSpeed, lvlData);
                }
            }

        } else {
            // not in air
            if (!movingInGrid)
                updateXPos(xSpeed, lvlData);
        }

        // check whether moving
        if (Math.abs(hitbox.x - oldXPos) > 0.1f || Math.abs(hitbox.y - oldYPos) > 0.1f)
            moving = true;
        else
            moving = false;
    }

    public void startExplosionTimer(String explosionType, BuildingZone currentBZ, float offset) {
        if ((tetrisTileManager.getPlaying().getGameTimeInSeconds() - explosionStartTime) < TETRIS_TILE_TIME_TO_EXPLODE)
            explosion();
        explosionStartTime = tetrisTileManager.getPlaying().getGameTimeInSeconds() - offset;
        this.explosionType = explosionType;
        this.currentBZ = currentBZ;
    }

    public void explosion() {
        float temp_increase = TEMP_FROM_WINDMILL_EXPLOSION;
        int width = WINDMILL_EXPLOSION_WIDTH, height = WINDMILL_EXPLOSION_HEIGHT;
        if (explosionType == "large") {
            temp_increase = TEMP_FROM_ROCKET_EXPLOSION;
            width = ROCKET_EXPLOSION_WIDTH;
            height = ROCKET_EXPLOSION_HEIGHT;

            List<TetrisTile> buildInTiles = currentBZ.getTetrisTiles();
            TetrisTile closestTile = null;
            float closestDistance = 1000_000_000;
            for (TetrisTile tt : buildInTiles) {
                float xD = tt.getHitbox().x - hitbox.x, yD = tt.getHitbox().y - hitbox.y;
                float d = (float) Math.sqrt(xD * xD + yD * yD);
                if (d < closestDistance) {
                    closestDistance = d;
                    closestTile = tt;
                }
            }
            if (closestTile != null && closestDistance < MAX_DISTANCE_FOR_FOLOWUP_EXPLOSION) {
                closestTile.startExplosionTimer("small", currentBZ, 0.5f * TETRIS_TILE_TIME_TO_EXPLODE);
            }
        }
        tetrisTileManager.getPlaying().getObjectManager().addExplosion((int) (hitbox.x + hitbox.width / 2), (int) (hitbox.y + hitbox.height / 2), width, height);
        explosionStartTime = -1;
        isCarriedBy = null;
        movingInGrid = false;
        inAir = true;
        if (lockedInBuildingZone != null && lockedInBuildingZone.getTetrisTiles().contains(this))
            lockedInBuildingZone.removeTetrisTile(this);
        lockedInBuildingZone = null;
        Random random = new Random();
        hitbox.y -= 1.0f;
        xSpeed = random.nextFloat() *
                (2 * TETRIS_TILE_MAX_EXPLOSION_X_SPEED) +
                -TETRIS_TILE_MAX_EXPLOSION_X_SPEED;
        airSpeed = -(random.nextFloat() *
                (TETRIS_TILE_MAX_EXPLOSION_Y_SPEED - TETRIS_TILE_MIN_EXPLOSION_Y_SPEED) +
                TETRIS_TILE_MIN_EXPLOSION_Y_SPEED);
        if (explosionType == "small") {
            xSpeed = xSpeed / 2;
            airSpeed = airSpeed / 2;
            tetrisTileManager.getPlaying().getGame().getAudioPlayer().playSmallExplosion();
        } else {
            tetrisTileManager.getPlaying().getGame().getAudioPlayer().playBigExplosion();
        }
        if (Math.abs(xSpeed) < TETRIS_TILE_MIN_EXPLOSION_X_SPEED)
            xSpeed = TETRIS_TILE_MIN_EXPLOSION_X_SPEED * Math.signum(xSpeed);
        tetrisTileManager.getPlaying().setTempFromExplosion(tetrisTileManager.getPlaying().getTempFromExplosion() + temp_increase);
    }

    private boolean isInBuildingZone() {
        float[] xCoordinates = {hitbox.x, (hitbox.x + hitbox.width - 1), hitbox.x, (hitbox.x + hitbox.width - 1)};
        float[] yCoordinates = {hitbox.y, hitbox.y, (hitbox.y + hitbox.height - 1), (hitbox.y + hitbox.height - 1)};
        int xIndex, yIndex;
        for (int i = 0; i < 4; i++) {
            xIndex = (int) (Math.max(0, Math.min(lvlData[0].length - 1, xCoordinates[i] / Game.TILES_SIZE)));
            yIndex = (int) (Math.max(0, Math.min(lvlData.length - 1, yCoordinates[i] / Game.TILES_SIZE)));
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
        xSpeed = 0;
        airSpeed = 0;
        isCarriedBy = null;
        movingInGrid = false;
        moving = false;
        lockedInBuildingZone = null;

        hitbox.x = x;
        hitbox.y = y;
        if (!IsEntityOnFloor(hitbox, lvlData))
            inAir = true;

        int oldTileIndex = tileIndex;
        Random random = new Random();
        tileIndex = GetRandomTetrisTileIndex(random);
        if (tileIndex == oldTileIndex)
            tileIndex = (tileIndex + 1) % (NUM_TETRIS_TILES);

    }

    public void grabbed(Player player) {
        if (isCarriedBy != null && isCarriedBy.getIsPlayer1() != player.getIsPlayer1())
            isCarriedBy.setIsCarrying(null);
        isCarriedBy = player;
        player.setIsCarrying(this);
        movingInGrid = false;
    }

    private void checkSpikesTouched(Playing playing) {
        playing.checkSpikesTouched(this);
    }

    public void kill() {
        resetTetrisTile();
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

    public void setAirSpeed(float airSpeed) {
        this.airSpeed = airSpeed;
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

    public void setMovingInGrid(boolean movingInGrid) {
        this.movingInGrid = movingInGrid;
    }

    public void setInAir(boolean inAir) {
        this.inAir = inAir;
    }

    public void setTileIndex(int tileIndex) {
        this.tileIndex = tileIndex;
    }

    public void setMatrix(int[][] matrix) {
        this.matrix = matrix;
    }

    public boolean getMoving() {
        return moving;
    }


    public boolean getMovingInGrid() {
        return movingInGrid;
    }

    public boolean getIsPredictionTile() {
        return isPredictionTile;
    }
}