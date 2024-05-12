package zones;

import static utilz.HelpMethods.*;
import static utilz.Constants.TetrisTileConstants.*;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;

import entities.Entity;
import entities.TetrisTile;
import gamestates.Playing;
import main.Game;

public class BuildingZone {
	private int[][] lvlData;
	private int gridWidth, gridHeight;
	int[][] matrix;
	int buildingZoneIndex;
	protected Rectangle2D.Float hitbox;


	public BuildingZone(int x, int y, int width, int height, int buildingZoneIndex) {
		this.buildingZoneIndex = buildingZoneIndex;
		hitbox = new Rectangle2D.Float(x, y, (int) (width), (int) (height));
		gridWidth = (int) width/Game.TILES_SIZE*4;
		gridHeight = (int) height/Game.TILES_SIZE*4;
		matrix = initMatrix(gridHeight, gridWidth);
	}
	
	private int[][] initMatrix(int rows, int cols) {
		int[][] m = new int[rows][cols];

        // Fill matrix with zeros
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                m[i][j] = 0;
            }
        }
        return m;
	}

	public boolean isTetrisTileColliding(TetrisTile tetrisTile) {
		//matrixAdd(int[][] matrixA, int[][] matrixB , int xIndex, int yIndex)
		
		return false;
	}
	
	
	public void addTetrisTile(TetrisTile tetrisTile) {
		matrix = addTetrisTileMatrix(tetrisTile.getHitbox().x, tetrisTile.getHitbox().y, tetrisTile.getMatrix(), 
				tetrisTile.getXDrawOffset(), tetrisTile.getYDrawOffset());
	}
	
	public int[][] addTetrisTileMatrix(float x, float y, int[][] tileMatrix, float xDrawOffset, float yDrawOffset) {
		int[][] m = new int[gridHeight][gridWidth];
		int xIndexShift = Math.round(-hitbox.x + x - xDrawOffset) / TETRIS_GRID_SIZE;
		int yIndexShift = Math.round(-hitbox.y + y - yDrawOffset) / TETRIS_GRID_SIZE;
		printArray(m);
		System.out.println("=============");
		m = matrixAdd(matrix, tileMatrix, xIndexShift, yIndexShift);
		printArray(m);
		return m;
	}
	
	public void update(Playing playing) {
	}
	
	public void loadLvlData(int[][] lvlData) {
		this.lvlData = lvlData;
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
	}