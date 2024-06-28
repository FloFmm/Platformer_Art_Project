package zones;

import static utilz.HelpMethods.*;
import static utilz.Constants.TetrisTileConstants.*;
import static utilz.Constants.Environment.*;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;

import entities.Entity;
import entities.TetrisTile;
import gamestates.Playing;
import main.Game;
import java.util.ArrayList;
import java.util.List;


public class BuildingZone {
	private int gridWidth, gridHeight;
	private int[][] matrix, goalMatrix, preMatrix;
	private int buildingZoneIndex;
	protected Rectangle2D.Float hitbox;
	private List<TetrisTile> tetrisTiles = new ArrayList<>();
	private String zoneType;
	private boolean finished = false;
	private BuildingZoneManager buildingZoneManager;
	public BuildingZone(int x, int y, int width, int height, int buildingZoneIndex, String zoneType) {
		this.buildingZoneIndex = buildingZoneIndex;
		hitbox = new Rectangle2D.Float(x, y, (int) (width), (int) (height));
		gridWidth = (int) width/Game.TILES_SIZE*4;
		gridHeight = (int) height/Game.TILES_SIZE*4;
		this.zoneType = zoneType;
		initMatrixes();
	}


	private void initMatrixes() {
		switch (zoneType) {
        case "windmill":
        	goalMatrix = matrixDeepCopy(WINDMILL_GOAL_MATRIX);
        	preMatrix = matrixDeepCopy(WINDMILL_PRE_MATRIX);
        	matrix = matrixDeepCopy(WINDMILL_PRE_MATRIX);
            break;
        case "windmill_tutorial":
        	goalMatrix = matrixDeepCopy(WINDMILL_GOAL_MATRIX);
        	preMatrix = matrixDeepCopy(WINDMILL_TUTORIAL_PRE_MATRIX);
        	matrix = matrixDeepCopy(WINDMILL_TUTORIAL_PRE_MATRIX);
            break;
        case "rocket":
        	goalMatrix = matrixDeepCopy(ROCKET_GOAL_MATRIX);
        	preMatrix = matrixDeepCopy(ROCKET_PRE_MATRIX);
        	matrix = matrixDeepCopy(ROCKET_PRE_MATRIX);
            break;
        case "rocket_tutorial":
        	goalMatrix = matrixDeepCopy(ROCKET_GOAL_MATRIX);
        	preMatrix = matrixDeepCopy(ROCKET_TUTORIAL_PRE_MATRIX);
        	matrix = matrixDeepCopy(ROCKET_TUTORIAL_PRE_MATRIX);
            break;
		}
	}

	public boolean addTetrisTile(TetrisTile tetrisTile) {
		int[][] oldMatrix = matrixDeepCopy(matrix);
		matrix = addTetrisTileMatrix(tetrisTile.getHitbox().x, 
				tetrisTile.getHitbox().y, 
				tetrisTile.getMatrix(), 
				tetrisTile.getXDrawOffset(), 
				tetrisTile.getYDrawOffset());
		if (matrixEquiv(oldMatrix, matrix)) {
			tetrisTile.setLockedInBuildingZone(null);
			return false;
		}
		
		
		if (!isCompletable(matrix)) {
			//TetrisTile lastAddedTile = tetrisTiles.remove(tetrisTiles.size() - 1);
			TetrisTile lastAddedTile = tetrisTile;
			matrix = addTetrisTileMatrix(tetrisTile.getHitbox().x, 
					tetrisTile.getHitbox().y, 
					matrixScalarMul(tetrisTile.getMatrix(),-1), 
					tetrisTile.getXDrawOffset(), 
					tetrisTile.getYDrawOffset());
			
			lastAddedTile.startExplosionTimer();
			return false;
		}
		else {
			// successful
			System.out.println("successfully added");
			if (isFinished()) {
				System.out.println("finished a building zone");
				
				eventOnFinish();
			}
				
			tetrisTiles.add(tetrisTile);
		}
		return true;
	}
	
	public boolean isCompletable(int[][] m) {
		int rows = m.length;
        int cols = m[0].length;
		for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (m[i][j] == 1 && goalMatrix[i][j] == 0 && preMatrix[i][j] == 0)
                	return false;
            }
        }
		return true;
	}
	
	public void eventOnFinish() {
		finished = true;
		if (zoneType=="windmill") {
			buildingZoneManager.getPlaying().setTemperature(buildingZoneManager.getPlaying().getTemperature() + TEMP_FROM_FINISHED_WINDMILL);;
		}
	}
	
	public void setBuildingZoneManager(BuildingZoneManager buildingZoneManager) {
		this.buildingZoneManager = buildingZoneManager;
	}
	
	public boolean isFinished() {
		int rows = matrix.length;
        int cols = matrix[0].length;
		for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (matrix[i][j] == 0 && goalMatrix[i][j] == 1)
                	return false;
            }
        }
		return true;
	}
	
	public int[][] addTetrisTileMatrix(float x, float y, int[][] tileMatrix, float xDrawOffset, float yDrawOffset) {
		int[][] m = new int[gridHeight][gridWidth];
		int xIndexShift = Math.round(-hitbox.x + x - xDrawOffset) / TETRIS_GRID_SIZE;
		int yIndexShift = Math.round(-hitbox.y + y - yDrawOffset) / TETRIS_GRID_SIZE;
		//printArray(m);
		//System.out.println("=============");
		m = matrixAdd(matrix, tileMatrix, xIndexShift, yIndexShift);
		//printArray(m);
		return m;
	}
	
	public void update(Playing playing) {
	}
	
	public boolean getFinished() {
		return finished;
	}
	
	public int getBuildingZoneIndex() {
		return buildingZoneIndex;
	}
	
	public Rectangle2D.Float getHitbox() {
		return hitbox;
	}
	
	protected void drawHitbox(Graphics g, int xLvlOffset, int yLvlOffset) {
		g.setColor(Color.GREEN);
		g.drawRect((int) hitbox.x - xLvlOffset, (int) hitbox.y - yLvlOffset, (int) hitbox.width, (int) hitbox.height);
	}
	
	
	public String getZoneType() {
		return zoneType;
	}
	
	
	public int[][] getMatrix() {
		return matrix;
	}
}