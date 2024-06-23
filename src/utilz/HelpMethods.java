package utilz;

import java.awt.geom.Rectangle2D;
//import java.util.Arrays;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import javax.imageio.ImageIO;

import java.io.File;
import java.io.IOException;

import java.util.Map;
import java.util.Set;
import main.Game;
import objects.Projectile;

public class HelpMethods {

	public static boolean CanMoveHere(float x, float y, float width, float height, int[][] lvlData) {
//		if (!IsSolid(x, y, lvlData))
//			if (!IsSolid(x + width, y + height, lvlData))
//				if (!IsSolid(x + width, y, lvlData))
//					if (!IsSolid(x, y + height, lvlData))
//						return true;
//		return false;
		return !IsSolid(x, y, width, height, lvlData);
	}

	private static boolean IsSolid(float x, float y, int[][] lvlData) {
		int maxWidth = lvlData[0].length * Game.TILES_SIZE;
		int maxHeight = lvlData.length * Game.TILES_SIZE;
		if (x < 0 || x >= maxWidth)
			return true;
		if (y < 0 || y >= maxHeight)
			return true;
		float xIndex = x / Game.TILES_SIZE;
		float yIndex = y / Game.TILES_SIZE;
		
		if (IsTileSolid((int) xIndex, (int) yIndex, lvlData)) {
			int tileValue = lvlData[(int) yIndex][(int) xIndex];
			if (tileValue >= 111 && tileValue <= 989) {
				
				int[][] triangleCoordinates = TriangleCoordinatesBaseLongShort((int) xIndex,  (int) yIndex, lvlData);
				
				// System.out.println(IsInsideTriangle(triangleCoordinates, new int[] {(int) x, (int) y}, 0.00001));
				return IsInsideTriangle(triangleCoordinates, new float[] {x, y}, 0.00001);

			}
			else 
				return true;
		}
		else
			return false;
	}
	
	private static boolean IsSolid(float x, float y, float width, float height, int[][] lvlData) {
		int maxWidth = lvlData[0].length * Game.TILES_SIZE;
		int maxHeight = lvlData.length * Game.TILES_SIZE;
		if (x < 0 || x+width-1 >= maxWidth)
			return true;
		if (y < 0 || y+height-1 >= maxHeight)
			return true;
		float[] xCoordinates = {x, (x+width-1), x, (x+width-1)};
		float[] yCoordinates = {y, y, (y+height-1), (y+height-1)};
		float xIndex, yIndex;
		for (int i = 0; i < 4; i++) {
			xIndex = xCoordinates[i]/Game.TILES_SIZE;
			yIndex = yCoordinates[i]/Game.TILES_SIZE;
			if (IsTileSolid((int) xIndex, (int) yIndex, lvlData)) {
				int tileValue = lvlData[(int) yIndex][(int) xIndex];

				if (tileValue >= 111 && tileValue <= 989) {
					
					int[][] triangleCoordinates = TriangleCoordinatesBaseLongShort((int) xIndex,  (int) yIndex, lvlData);
					
					if (IsRectIntersectingTriangle(x, y, width, height, triangleCoordinates)) {
						return true;
					}
				}
				else 
					return true;
			}
		}
		return false;
	}
	
	
	public static boolean IsRectIntersectingTriangle(float x, float y, float width, float height, int[][] triangleCoordinatesBaseLongShort) {
		if (IsInsideTriangle(triangleCoordinatesBaseLongShort, new float[] {x, y}, 1.0)
		 || IsInsideTriangle(triangleCoordinatesBaseLongShort, new float[] {x+width, y}, 1.0)
		 || IsInsideTriangle(triangleCoordinatesBaseLongShort, new float[] {x, y+height}, 1.0)
		 || IsInsideTriangle(triangleCoordinatesBaseLongShort, new float[] {x+width, y+height}, 1.0))
			return true;
		
		int triangleBaseX = triangleCoordinatesBaseLongShort[0][0];
		int triangleBaseY = triangleCoordinatesBaseLongShort[0][1];
		if ((y <= triangleBaseY && y+height >= triangleBaseY) || (x <= triangleBaseX && x+width >= triangleBaseX))
			return true;
		return false;
	}

	public static boolean IsProjectileHittingLevel(Projectile p, int[][] lvlData) {
		return IsSolid(p.getHitbox().x + p.getHitbox().width / 2, p.getHitbox().y + p.getHitbox().height / 2, lvlData);
	}
	
	public static int[][] TriangleCoordinatesBaseLongShort(int xTile, int yTile, int[][] lvlData) {
		int tileValue = lvlData[yTile][xTile];
		int x = Game.TILES_SIZE * xTile;
		int y = Game.TILES_SIZE * yTile;
		
		int orientation = (tileValue%100)/10; // 1 to 8
		int simple_orient = orientation; // 1 to 4
		int length = tileValue/100;
		int position = tileValue % 10;
		int width=0, height=0, baseX=0, baseY=0, shortX=0, shortY=0, longX=0, longY=0;
		
		if (orientation <= 4) { //horizontal
			width = length * Game.TILES_SIZE;
			height = Game.TILES_SIZE;
			x -= Game.TILES_SIZE * (position - 1);
		}
		else { //vertical
			height = length * Game.TILES_SIZE;
			width = Game.TILES_SIZE;
			simple_orient -= 4;
			y -= Game.TILES_SIZE * (position - 1);
		}
		
		switch (simple_orient) {
		case 1 -> {
			baseX = x+width-1; 
			baseY = y+height-1;
			shortX = x+width-1;
			shortY = y;
			longX = x;
			longY = y+height-1;
		}
		case 2 -> {
			baseX = x+width-1; 
			baseY = y;
			shortX = x+width-1;
			shortY = y+height-1;
			longX = x;
			longY = y;
		}
		case 3 -> {
			baseX = x; 
			baseY = y+height-1;
			shortX = x;
			shortY = y;
			longX = x+width-1;
			longY = y+height-1;
		}
		case 4 -> {
			baseX = x; 
			baseY = y;
			shortX = x;
			shortY = y+height-1;
			longX = x+width-1;
			longY = y;
			}
		}

		return new int[][] {{baseX, baseY}, {longX, longY}, {shortX, shortY}};
	}

	public static double triangleArea(float[][] triangleCoordinates) {
		float x1 = triangleCoordinates[0][0];
		float y1 = triangleCoordinates[0][1];
		float x2 = triangleCoordinates[1][0];
		float y2 = triangleCoordinates[1][1];
		float x3 = triangleCoordinates[2][0];
		float y3 = triangleCoordinates[2][1];
		return Math.abs((x1*(y2-y3) + x2*(y3-y1)+x3*(y1-y2))/2.0);
	}
	
	public static boolean IsInsideTriangle(int[][] triangleCoordinates, float[] coordinate, double treshhold) {
		int x1 = triangleCoordinates[0][0];
		int y1 = triangleCoordinates[0][1];
		int x2 = triangleCoordinates[1][0];
		int y2 = triangleCoordinates[1][1];
		int x3 = triangleCoordinates[2][0];
		int y3 = triangleCoordinates[2][1];
		float x = coordinate[0];
		float y = coordinate[1];
		/* Calculate area of triangle ABC */
		double A = triangleArea(new float[][] {{x1, y1}, {x2, y2}, {x3, y3}});
		
		/* Calculate area of triangle PBC */ 
		double A1 = triangleArea(new float[][] {{x, y}, {x2, y2}, {x3, y3}});
		
		/* Calculate area of triangle PAC */ 
		double A2 = triangleArea(new float[][] {{x1, y1}, {x, y}, {x3, y3}});
		
		/* Calculate area of triangle PAB */  
		double A3 = triangleArea(new float[][] {{x1, y1}, {x2, y2}, {x, y}});
		
		/* Check if sum of A1, A2 and A3 is same as A */
		return (Math.abs(A - (A1 + A2 + A3)) < treshhold);
	}
	
	private static double linearFuncOfTwoPoints(int[] point1, int[] point2, double xOrY, boolean useAsX) {
		if (useAsX) {
			if (point2[0] > point1[0]) 
				return point1[1] + ((double)point2[1] - (double)point1[1]) * (xOrY-(double)point1[0])/((double)point2[0]-(double)point1[0]);
			else
				return point2[1] + ((double)point1[1] - (double)point2[1]) * (xOrY-(double)point2[0])/((double)point1[0]-(double)point2[0]);
		}
		else {
			if (point2[1] > point1[1]) 
				return point1[0] + ((double)point2[0] - (double)point1[0]) * (xOrY-(double)point1[1])/((double)point2[1]-(double)point1[1]);
			else
				return point2[0] + ((double)point1[0] - (double)point2[0]) * (xOrY-(double)point2[1])/((double)point1[1]-(double)point2[1]);
		}
	}
	
//	public static double[] lowerAndUpperXOrYTriangleTouchingPoint(int[][] triangleCoordinatesBaseLongShort, double[] xOrYs, boolean useAsX, int simple_orient) {
//		double lowerBound = -1.0, upperBound = -1.0;
//		double x1 = xOrYs[0]; 
//		double x2 = xOrYs[1];
//		if (x1 > x2) {
//			x1 = xOrYs[1]; 
//			x2 = xOrYs[0];
//		}
//
//		int[] xCoordinates = new int[] {triangleCoordinatesBaseLongShort[0][0], 
//				triangleCoordinatesBaseLongShort[1][0], triangleCoordinatesBaseLongShort[2][0]}; 
//		int[] yCoordinates = new int[] {triangleCoordinatesBaseLongShort[0][1], 
//				triangleCoordinatesBaseLongShort[1][1], triangleCoordinatesBaseLongShort[2][1]}; 
//		int triangleMaxX = Arrays.stream(xCoordinates).max().getAsInt();
//		int triangleMaxY = Arrays.stream(yCoordinates).max().getAsInt();
//		int triangleMinX = Arrays.stream(xCoordinates).min().getAsInt();
//		int triangleMinY = Arrays.stream(yCoordinates).min().getAsInt();
//		
//		if (useAsX) {
//			switch (simple_orient) {
//				case 1 -> {
//					lowerBound = triangleMinY;
//					if (x2 >= triangleMaxX)
//						upperBound = triangleMaxY;
//					else {
//						upperBound = linearFuncOfTwoPoints(triangleCoordinatesBaseLongShort[1], 
//								triangleCoordinatesBaseLongShort[2], x2, true);
//					}
//				}
//				case 2 -> {
//					upperBound = triangleMaxY;
//					if (x2 >= triangleMaxX)
//						lowerBound = triangleMinY;
//					else {
//						lowerBound = linearFuncOfTwoPoints(triangleCoordinatesBaseLongShort[1], 
//								triangleCoordinatesBaseLongShort[2], x2, true);
//					}
//				}
//				case 3 -> {
//					lowerBound = triangleMinY;
//					if (x1 <= triangleMinX)
//						upperBound = triangleMaxY;
//					else {
//						upperBound = linearFuncOfTwoPoints(triangleCoordinatesBaseLongShort[1], 
//								triangleCoordinatesBaseLongShort[2], x1, true);
//					}
//				}
//				case 4 -> {
//					upperBound = triangleMaxY;
//					if (x1 <= triangleMinX)
//						lowerBound = triangleMinY;
//					else {
//						lowerBound = linearFuncOfTwoPoints(triangleCoordinatesBaseLongShort[1], 
//								triangleCoordinatesBaseLongShort[2], x1, true);
//					}
//				}
//			}
//		}
//		else {
//			switch (simple_orient) {
//				case 1 -> {
//					upperBound = triangleMaxX;
//					if (x2 >= triangleMaxY)
//						lowerBound = triangleMinX;
//					else {
//						lowerBound = linearFuncOfTwoPoints(triangleCoordinatesBaseLongShort[1], 
//								triangleCoordinatesBaseLongShort[2], x2, true);
//					}
//				}
//				case 2 -> {
//					upperBound = triangleMaxX;
//					if (x1 <= triangleMinY)
//						lowerBound = triangleMinX;
//					else {
//						lowerBound = linearFuncOfTwoPoints(triangleCoordinatesBaseLongShort[1], 
//								triangleCoordinatesBaseLongShort[2], x1, true);
//					}
//				}
//				case 3 -> {
//					lowerBound = triangleMinX;
//					if (x1 >= triangleMaxY)
//						upperBound = triangleMaxX;
//					else {
//						upperBound = linearFuncOfTwoPoints(triangleCoordinatesBaseLongShort[1], 
//								triangleCoordinatesBaseLongShort[2], x2, true);
//					}
//				}
//				case 4 -> {
//					lowerBound = triangleMinX;
//					if (x1 <= triangleMinY)
//						upperBound = triangleMaxX;
//					else {
//						upperBound = linearFuncOfTwoPoints(triangleCoordinatesBaseLongShort[1], 
//								triangleCoordinatesBaseLongShort[2], x1, true);
//					}
//				}
//			}
//		}
//		
//		return (new double[] {lowerBound, upperBound});
//	}
	
	private static int GetTileValue(float xPos, float yPos, int[][] lvlData) {
		int xCord = (int) (xPos / Game.TILES_SIZE);
		int yCord = (int) (yPos / Game.TILES_SIZE);
		if (xCord < 0 || xCord >= lvlData.length || yCord < 0 || yCord >= lvlData[0].length)
            return 1;
		return lvlData[yCord][xCord];
	}

	
	public static boolean IsTileSolid(int xTile, int yTile, int[][] lvlData) {
		int tileValue = lvlData[yTile][xTile];
		if (tileValue==11 || tileValue==3 || tileValue==48 || tileValue==49)
			return false;
		else if (tileValue >= 111 && tileValue <= 989)
			return true;
		else
			return true;
	}
	

	public static float GetEntityXPosNextToWall(Rectangle2D.Float hitbox, float xSpeed) {
		int currentTile = (int) (hitbox.x / Game.TILES_SIZE);
		if (xSpeed > 0) {
			// Right
			int tileXPos = currentTile * Game.TILES_SIZE;
			int xOffset = (int) (Game.TILES_SIZE - hitbox.width);
			return tileXPos + xOffset - 1;
		} else
			// Left
			return currentTile * Game.TILES_SIZE;
	}
	
	public static int[] InterpretTriangleTileValue(int tileValue) {
		int orientation = (tileValue%100)/10; // 1 to 8
		int simpleOrient = orientation; // 1 to 4
		if (orientation > 4)
			simpleOrient = orientation-4;
		int length = tileValue/100;
		int position = tileValue % 10;
		return new int[] {length, orientation, position, simpleOrient}; 
	}
	
	public static double GradientOfTriangle(int tileValue) {
		int[] interpretation = InterpretTriangleTileValue(tileValue);
		int length = interpretation[0];
		int orientation = interpretation[1]; // 1 to 8
		switch (orientation) {
			case 1,3 -> {
				return 1.0d/length;
			}
			case 2,4 -> {
				return -1.0d/length;
			}
			case 5,7 -> {
				return length;
			}
			case 6,8 -> {
				return -length;
			}
		}
		return 0;
	}
	
	public static float[] GetEntityXPosNextToWall(Rectangle2D.Float hitbox, float xSpeed, int[][] lvlData, float offset) {
		int currentTile = (int) (hitbox.x / Game.TILES_SIZE);
		int yIndex = (int) (hitbox.y+hitbox.height-1)/Game.TILES_SIZE;
		int tileValue, xIndex;
		if (xSpeed > 0) {
			// Right
			xIndex = (int) (hitbox.x+hitbox.width-1+xSpeed)/Game.TILES_SIZE;
			if (xIndex < 0 || xIndex >= lvlData.length || yIndex < 0 || yIndex >= lvlData[0].length)
				tileValue = 1;
			else
				tileValue = lvlData[yIndex][xIndex];
			// System.out.println(tileValue);
			if (tileValue >= 111 && tileValue <= 989) {
				int[] interpretation = InterpretTriangleTileValue(tileValue);
				int simpleOrient = interpretation[3]; // 1 to 4
				switch (simpleOrient) {
					case 1 -> {
						double gradient = GradientOfTriangle(tileValue);
						double factor = 1.0d/Math.sqrt(1+gradient*gradient);
//						System.out.println("===STAT================");
//						System.out.println(hitbox.x);
//						System.out.println((float) (hitbox.x+xSpeed*factor));
//						System.out.println("=======================");
////						System.out.println(hitbox.y);
////						System.out.println((float) (hitbox.y-Math.abs(xSpeed)*gradient*factor));
//						
//						System.out.println(factor);
//						System.out.println(gradient);
						return new float[] {(float) (hitbox.x+xSpeed*factor-offset), 
								(float) (hitbox.y-Math.abs(xSpeed)*gradient*factor-offset)};
	 				}
					case 2 -> {
						return new float[] {hitbox.x, hitbox.y};
	 				}
				}
			}
			int tileXPos = currentTile * Game.TILES_SIZE;
			int xOffset = (int) (Game.TILES_SIZE - hitbox.width);
			return new float[] {tileXPos + xOffset + 1, hitbox.y};
		} else {
			// Left
			xIndex = (int) (hitbox.x+xSpeed)/Game.TILES_SIZE;
			tileValue = lvlData[yIndex][xIndex];
			if (tileValue >= 111 && tileValue <= 989) {
				int[] interpretation = InterpretTriangleTileValue(tileValue);
				int simpleOrient = interpretation[3]; // 1 to 4
				switch (simpleOrient) {
					case 3 -> {
						double gradient = GradientOfTriangle(tileValue);
						double factor = 1.0d/Math.sqrt(1+gradient*gradient);
//						System.out.println("===STAT================");
//						System.out.println(hitbox.x);
//						System.out.println((float) (hitbox.x+xSpeed*factor));
//						System.out.println("=======================");
////						System.out.println(hitbox.y);
////						System.out.println((float) (hitbox.y-Math.abs(xSpeed)*gradient*factor));
////						
//						System.out.println(factor);
//						System.out.println(gradient);
//						System.out.println(hitbox.x+xSpeed*factor+offset);
//						System.out.println(hitbox.y-Math.abs(xSpeed)*gradient*factor-offset);
//						
						return new float[] {(float) (hitbox.x+xSpeed*factor+offset), 
								(float) (hitbox.y-Math.abs(xSpeed)*gradient*factor-offset)};
						
	 				}
					case 4 -> {
						return new float[] {hitbox.x, hitbox.y};
	 				}
				}
			}
			return new float[] {currentTile * Game.TILES_SIZE + 1, hitbox.y};
		}
	}

	public static float GetEntityYPosUnderRoofOrAboveFloor(Rectangle2D.Float hitbox, float airSpeed) {
		int currentTile = (int) (hitbox.y / Game.TILES_SIZE);
		if (airSpeed > 0) {
			// Falling - touching floor
			int tileYPos = currentTile * Game.TILES_SIZE;
			int yOffset = (int) (Game.TILES_SIZE - hitbox.height);
			return tileYPos + yOffset - 1;
		} else
			// Jumping
			return currentTile * Game.TILES_SIZE;

	}

	public static boolean IsEntityOnFloor(Rectangle2D.Float hitbox, int[][] lvlData) {
		if (!IsSolid(hitbox.x, hitbox.y + hitbox.height + 1, lvlData))
			if (!IsSolid(hitbox.x + hitbox.width, hitbox.y + hitbox.height + 1, lvlData))
				return false;
		return true;
	}

	public static boolean IsFloor(Rectangle2D.Float hitbox, float xSpeed, int[][] lvlData) {
		if (xSpeed > 0)
			return IsSolid(hitbox.x + hitbox.width + xSpeed, hitbox.y + hitbox.height + 1, lvlData);
		else
			return IsSolid(hitbox.x + xSpeed, hitbox.y + hitbox.height + 1, lvlData);
	}

	public static boolean IsFloor(Rectangle2D.Float hitbox, int[][] lvlData) {
		if (!IsSolid(hitbox.x + hitbox.width, hitbox.y + hitbox.height + 1, lvlData))
			if (!IsSolid(hitbox.x, hitbox.y + hitbox.height + 1, lvlData))
				return false;
		return true;
	}

	public static boolean CanCannonSeePlayer(int[][] lvlData, Rectangle2D.Float firstHitbox, Rectangle2D.Float secondHitbox, int yTile) {
		int firstXTile = (int) (firstHitbox.x / Game.TILES_SIZE);
		int secondXTile = (int) (secondHitbox.x / Game.TILES_SIZE);

		if (firstXTile > secondXTile)
			return IsAllTilesClear(secondXTile, firstXTile, yTile, lvlData);
		else
			return IsAllTilesClear(firstXTile, secondXTile, yTile, lvlData);
	}

	public static boolean IsAllTilesClear(int xStart, int xEnd, int y, int[][] lvlData) {
		for (int i = 0; i < xEnd - xStart; i++)
			if (IsTileSolid(xStart + i, y, lvlData))
				return false;
		return true;
	}

	public static boolean IsAllTilesWalkable(int xStart, int xEnd, int y, int[][] lvlData) {
		if (IsAllTilesClear(xStart, xEnd, y, lvlData))
			for (int i = 0; i < xEnd - xStart; i++) {
				if (!IsTileSolid(xStart + i, y + 1, lvlData))
					return false;
			}
		return true;
	}

	// Player can sometimes be on an edge and in sight of enemy.
	// The old method would return false because the player x is not on edge.
	// This method checks both player x and player x + width.
	// If tile under playerBox.x is not solid, we switch to playerBox.x +
	// playerBox.width;
	// One of them will be true, because of prior checks.

	public static boolean IsSightClear(int[][] lvlData, Rectangle2D.Float enemyBox, Rectangle2D.Float playerBox, int yTile) {
		int firstXTile = (int) (enemyBox.x / Game.TILES_SIZE);

		int secondXTile;
		if (IsSolid(playerBox.x, playerBox.y + playerBox.height + 1, lvlData))
			secondXTile = (int) (playerBox.x / Game.TILES_SIZE);
		else
			secondXTile = (int) ((playerBox.x + playerBox.width) / Game.TILES_SIZE);

		if (firstXTile > secondXTile)
			return IsAllTilesWalkable(secondXTile, firstXTile, yTile, lvlData);
		else
			return IsAllTilesWalkable(firstXTile, secondXTile, yTile, lvlData);
	}

	public static boolean IsSightClear_OLD(int[][] lvlData, Rectangle2D.Float firstHitbox, Rectangle2D.Float secondHitbox, int yTile) {
		int firstXTile = (int) (firstHitbox.x / Game.TILES_SIZE);
		int secondXTile = (int) (secondHitbox.x / Game.TILES_SIZE);

		if (firstXTile > secondXTile)
			return IsAllTilesWalkable(secondXTile, firstXTile, yTile, lvlData);
		else
			return IsAllTilesWalkable(firstXTile, secondXTile, yTile, lvlData);
	}
	

    public static BufferedImage rotateImage(BufferedImage sourceImage, double angle) {
        int width = sourceImage.getWidth();
        int height = sourceImage.getHeight();
        BufferedImage destImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = destImage.createGraphics();

        AffineTransform transform = new AffineTransform();
        transform.rotate(angle / 180 * Math.PI, width / 2 , height / 2);
        g2d.drawRenderedImage(sourceImage, transform);

        g2d.dispose();
        return destImage;
    }
    
    
    public static int[][] rotateMatrixBy90Degree(int[][] matrix) {
        int n = matrix.length;
        int[][] rotated = new int[n][n];
        
        // Transpose the matrix
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                rotated[i][j] = matrix[j][i];
            }
        }
        
        // Reverse each row to rotate 90 degrees
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n / 2; j++) {
                int temp = rotated[i][j];
                rotated[i][j] = rotated[i][n - 1 - j];
                rotated[i][n - 1 - j] = temp;
            }
        }
        
        return rotated;
    }
    
    public static float calculateYOfThrowArc(float t, float xSpeed, float ySpeed, float gravity) {
        return  (ySpeed * t - 0.5f * gravity * t * t); 
    }
    
	public static int[] calculateAreaCoveredByEquivalentTiles(int red, int x, int y, BufferedImage img) {
		int imgHeight = img.getHeight();
		int imgWidth = img.getWidth();
		
		// look up
		int yTest = y;
		while (yTest < imgHeight && (new Color(img.getRGB(x, yTest)).getRed() == red))
			yTest += 1;
		int upperBound = yTest - 1;

		// look down
		yTest = y;
		while (yTest >= 0 && (new Color(img.getRGB(x, yTest)).getRed() == red))
			yTest -= 1;
		int lowerBound = yTest + 1;
		
		// look right
		int xTest = x;
		while (xTest < imgWidth && (new Color(img.getRGB(xTest, y)).getRed() == red))
			xTest += 1;
		int rightBound = xTest - 1;
		
		// look left
		xTest = x;
		while (xTest >= 0 && (new Color(img.getRGB(xTest, y)).getRed() == red))
			xTest -= 1;
		int leftBound = xTest + 1;
		
		return new int[] {leftBound, rightBound, upperBound, lowerBound};
	}
	
	public static int[][] matrixAdd(int[][] matrixA, int[][] matrixB , int xIndex, int yIndex) {
		int rowsA = matrixA.length;
        int colsA = matrixA[0].length;
        int rowsB = matrixB.length;
        int colsB = matrixB[0].length;
        int[][] result = new int[rowsA][colsA];
        
		for (int i = 0; i < rowsA; i++) {
            for (int j = 0; j < colsA; j++) {
                result[i][j] = matrixA[i][j];
                int iB = i - yIndex;
                int jB = j - xIndex;
                if (iB >= 0 && iB < rowsB && jB >= 0 && jB < colsB)
                	result[i][j] += matrixB[iB][jB];
            }
        }
		return result;
	}
	
	public static int[][] matrixAdd(int[][] matrixA, int[][] matrixB) {
		int rowsA = matrixA.length;
        int colsA = matrixA[0].length;
        int[][] result = new int[rowsA][colsA];
        
		for (int i = 0; i < rowsA; i++) {
            for (int j = 0; j < colsA; j++) {
                result[i][j] += matrixA[i][j] + matrixB[i][j];
            }
        }
		return result;
	}
	
	public static boolean matrixEquiv(int[][] matrixA, int[][] matrixB) {
		int rowsA = matrixA.length;
        int colsA = matrixA[0].length;
        int rowsB = matrixB.length;
        int colsB = matrixB[0].length;
        if (rowsA != rowsB || colsA != colsB)
        	return false;
        
		for (int i = 0; i < rowsA; i++) {
            for (int j = 0; j < colsA; j++) {
            	if (matrixA[i][j] != matrixB[i][j])
            		return false;
            }
        }
		return true;
	}
	
	public static int[][] matrixScalarMul(int[][] matrixA, int factor) {
		int rowsA = matrixA.length;
        int colsA = matrixA[0].length;
        int[][] result = new int[rowsA][colsA];
        
		for (int i = 0; i < rowsA; i++) {
            for (int j = 0; j < colsA; j++) {
                result[i][j] = matrixA[i][j]*factor;
            }
        }
		return result;
	}
	
	public static int[][] matrixDeepCopy(int[][] matrixA) {
		int rowsA = matrixA.length;
        int colsA = matrixA[0].length;
        int[][] result = new int[rowsA][colsA];
        
		for (int i = 0; i < rowsA; i++) {
            for (int j = 0; j < colsA; j++) {
                result[i][j] = matrixA[i][j];
            }
        }
		return result;
	}
	
	public static boolean matrixContainsValue(int[][] matrix, int containedValue) {
		int rowsA = matrix.length;
        int colsA = matrix[0].length;
		for (int i = 0; i < rowsA; i++) {
            for (int j = 0; j < colsA; j++) {
                if (matrix[i][j] == containedValue) {
                	return true;
                }
            }
        }
		return false;
	}
	
	public static void printMatrix(int[][] arr) {
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr[i].length; j++) {
                System.out.print(arr[i][j] + "\t");
            }
            System.out.println(); // Move to the next line for the next row
        }
    }
	

	
	public static BufferedImage replaceColors(BufferedImage image, Map<Color, Color> colorMap, int tolerance, Color defaultC) {
		int width = image.getWidth();
        int height = image.getHeight();
		int numMappingFailures = 0;
		// Create a copy of the original image
        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                newImage.setRGB(x, y, image.getRGB(x, y));
            }
        }
        

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int rgba = image.getRGB(x, y);
                Color originalColor = new Color(rgba, true); // true to consider alpha

                if (originalColor.getAlpha() != 0) {
                	Color mappedColor = getNewColor(originalColor, colorMap, tolerance);
	                if (mappedColor == null) {
	                	mappedColor = defaultC;
	                	numMappingFailures += 1;
	                }
	
	                // Preserve the original alpha value
	                int alpha = originalColor.getAlpha();
	                Color newColor = new Color(mappedColor.getRed(), mappedColor.getGreen(), mappedColor.getBlue(), alpha);
	
	                int newRgba = (newColor.getAlpha() << 24) | (newColor.getRed() << 16) | (newColor.getGreen() << 8) | newColor.getBlue();
	                newImage.setRGB(x, y, newRgba);
                }
                
            }
        }
        if (numMappingFailures > 0)
        	System.out.println("failed to map color " + numMappingFailures + " times. replaced those with default color");
        return newImage;
    }
	
	private static Color getNewColor(Color color, Map<Color, Color> colorMap, int tolerance) {
		Set<Color> colorSet = colorMap.keySet();
		
		// try direct conversion
		int smallestError = 255+255+255;
		Color bestC = null;
		for (Color c : colorSet) {
			int error = Math.abs(c.getRed() - color.getRed()) + 
					Math.abs(c.getGreen() - color.getGreen()) + 
					Math.abs(c.getBlue() - color.getBlue());
	        if (error <= 3*tolerance && error < smallestError) {
	        	bestC = c;
	        	smallestError = error;
	        }
	    }
		if (bestC != null)
			return colorMap.get(bestC); 
		
		// try mixing colors
		smallestError = 255+255+255;
		Color bestC1 = null;
		Color bestC2 = null;
		float bestRatio = 1.0f;
	    for (Color c1 : colorSet) {
	    	for (Color c2 : colorSet) {
	    		for (int i = 1; i < 10f; i+=1) {
	    			float ratio = i*0.1f;
	    			Color closestColor = mixColors(c1, c2, ratio); 
	    			int error = Math.abs(closestColor.getRed() - color.getRed()) + 
	    					Math.abs(closestColor.getGreen() - color.getGreen()) + 
	    					Math.abs(closestColor.getBlue() - color.getBlue());
	    			if (error < smallestError) {
	    				bestC1 = c1;
	    				bestC2 = c2;
	    				smallestError = error; 
	    				bestRatio = ratio;
	    			}
//	    			        if (Math.abs(closestColor.getRed() - color.getRed()) <= tolerance &&
//				            Math.abs(closestColor.getGreen() - color.getGreen()) <= tolerance &&
//				            Math.abs(closestColor.getBlue() - color.getBlue()) <= tolerance) {
//				            return mixColors(colorMap.get(c1), colorMap.get(c2), ratio);
//				        }
	    		}
	    	}
	    }
	    if (bestC1 != null)
	    	return  mixColors(colorMap.get(bestC1), colorMap.get(bestC2), bestRatio);
	    return null;
	}
	
	private static Color mixColors(Color c1, Color c2, float ratio) {
		return new Color((int) (c1.getRed()*ratio + c2.getRed()*(1.0f-ratio)), 
				(int) (c1.getGreen()*ratio + c2.getGreen()*(1.0f-ratio)), 
				(int) (c1.getBlue()*ratio + c2.getBlue()*(1.0f-ratio)));
	}
	
	public static float linear(float currentX, float startX, float endX, float startY, float endY) {
		float xRatio = Math.min(1.0f, (currentX - startX)/(endX - startX));
//		System.out.println("(currentX - startX)/(endX - startX)");
//		System.out.println((currentX - startX)/(endX - startX));
//		System.out.println("xRatio");
//		System.out.println(xRatio);
//		System.out.println("endY - startY");
//		System.out.println(endY - startY);
		return startY + xRatio * (endY - startY);
	}
	
}