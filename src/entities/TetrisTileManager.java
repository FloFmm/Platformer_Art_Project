package entities;

import gamestates.Playing;
import levels.Level;
import main.Game;
import utilz.LoadSave;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import static utilz.Constants.TetrisTileConstants.*;
import static utilz.Constants.UPS_SET;
import static utilz.HelpMethods.linear;
import static utilz.HelpMethods.rotateImage;

public class TetrisTileManager {

    private final Playing playing;
    private final BufferedImage[][][] tetrisTileArr = new BufferedImage[NUM_TETRIS_TILES][4][3];
    private final TetrisTile[] throwArcPredictionTiles = new TetrisTile[2];
    private Level currentLevel;
    private int[][][] throwArcPredictionPoints;

    public TetrisTileManager(Playing playing) {
        this.playing = playing;
        initThrowArcPrediction();
        loadTetrisTileImgs();
    }

    public static BufferedImage uniformReplaceColors(BufferedImage image, Color goalColor) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Color oldColor = new Color(image.getRGB(x, y), true); // true to consider alpha
                int newA = (int) (linear((oldColor.getRed() + oldColor.getGreen() + oldColor.getBlue()), 0, 3 * 200, 255, 0) * (oldColor.getAlpha() / 255.0f));
                int newRgba = (newA << 24) | (goalColor.getRed() << 16) | (goalColor.getGreen() << 8) | goalColor.getBlue();
                newImage.setRGB(x, y, newRgba);
            }
        }
        return newImage;
    }

    private void predictThrowArc(int[][] lvlData, Player player) {
        TetrisTile carriedTile = player.getIsCarrying();
        if (carriedTile == null) {
            resetThrowArcPrediction(0, player.getIsPlayer1());
            return;
        }

        int pIndex = 1;
        if (player.getIsPlayer1())
            pIndex = 0;

        if (throwArcPredictionTiles[pIndex] == null) {
            throwArcPredictionTiles[pIndex] = new TetrisTile(
                    carriedTile.getHitbox().x,
                    carriedTile.getHitbox().y,
                    TETRIS_TILE_WIDTH, TETRIS_TILE_HEIGHT, carriedTile.getTileIndex(), lvlData, true);
            throwArcPredictionTiles[pIndex].setTetrisTileManager(this);
        } else {
            throwArcPredictionTiles[pIndex].getHitbox().x = carriedTile.getHitbox().x;
            throwArcPredictionTiles[pIndex].getHitbox().y = carriedTile.getHitbox().y;
            throwArcPredictionTiles[pIndex].setTileIndex(carriedTile.getTileIndex());
        }
        throwArcPredictionTiles[pIndex].setRotation(carriedTile.getRotation());
        throwArcPredictionTiles[pIndex].setMatrix(GetTetrisTileShape(carriedTile.getTileIndex(), carriedTile.getRotation()));
        throwArcPredictionTiles[pIndex].movingInGrid = false;
        throwArcPredictionTiles[pIndex].moving = false;
        throwArcPredictionTiles[pIndex].setLockedInBuildingZone(null);
        throwArcPredictionTiles[pIndex].inAir = true;
        throwArcPredictionTiles[pIndex].loadLvlData(currentLevel.getLevelData());


        float[] throwSpeed = player.calcThrowSpeed();
        throwArcPredictionTiles[pIndex].xSpeed = throwSpeed[0];
        throwArcPredictionTiles[pIndex].airSpeed = -throwSpeed[1];

        int stepSize = (int) (THROW_ARC_PREDICTION_TIME * UPS_SET / NUM_THROW_ARC_PREDICTION_POINTS);
        for (int i = 0; i < THROW_ARC_PREDICTION_TIME * UPS_SET; i += 1) {
            if (i / stepSize >= NUM_THROW_ARC_PREDICTION_POINTS)
                break;
            if (i % stepSize == 0) {
                throwArcPredictionPoints[pIndex][i / stepSize][0] = (int) (throwArcPredictionTiles[pIndex].getHitbox().x + throwArcPredictionTiles[pIndex].getHitbox().width / 2);
                throwArcPredictionPoints[pIndex][i / stepSize][1] = (int) (throwArcPredictionTiles[pIndex].getHitbox().y + throwArcPredictionTiles[pIndex].getHitbox().height / 2);
            }
            throwArcPredictionTiles[pIndex].updatePos(playing.getWindSpeed());
            throwArcPredictionTiles[pIndex].updateHitBox();
            if (!throwArcPredictionTiles[pIndex].getMoving() || !throwArcPredictionTiles[pIndex].inAir || throwArcPredictionTiles[pIndex].getLockedInBuildingZone() != null) {
                resetThrowArcPrediction(i + 1, player.getIsPlayer1());
            }
            //	checkSpikesTouched(playing);

        }
    }

    private void initThrowArcPrediction() {
        throwArcPredictionPoints = new int[2][NUM_THROW_ARC_PREDICTION_POINTS][2];
        for (int p = 0; p < throwArcPredictionPoints.length; p++) {
            for (int i = 0; i < throwArcPredictionPoints[0].length; i++) {
                throwArcPredictionPoints[p][i][0] = FINAL_PREDICTION_POINT;
                throwArcPredictionPoints[p][i][1] = FINAL_PREDICTION_POINT;
            }
        }
    }

    private void resetThrowArcPrediction(int startIndex, boolean isPlayer1) {
        int pIndex = 1;
        if (isPlayer1)
            pIndex = 0;

        for (int i = startIndex; i < throwArcPredictionPoints[0].length; i++) {
            if (throwArcPredictionPoints[pIndex][i][0] == FINAL_PREDICTION_POINT)
                return;
            throwArcPredictionPoints[pIndex][i][0] = FINAL_PREDICTION_POINT;
            throwArcPredictionPoints[pIndex][i][1] = FINAL_PREDICTION_POINT;
        }
    }

    public void loadTetrisTiles(Level level) {
        this.currentLevel = level;
        for (TetrisTile c : currentLevel.getTetrisTiles())
            c.setTetrisTileManager(this);
    }

    public void update() {
        for (TetrisTile c : currentLevel.getTetrisTiles())
            c.update(playing);

        predictThrowArc(currentLevel.getLevelData(), playing.getPlayer1());
        predictThrowArc(currentLevel.getLevelData(), playing.getPlayer2());
    }

    public void draw(Graphics g, int xLvlOffset, int yLvlOffset) {
        drawTetrisTiles(g, xLvlOffset, yLvlOffset);
    }

    private void drawTetrisTiles(Graphics g, int xLvlOffset, int yLvlOffset) {
        for (TetrisTile c : currentLevel.getTetrisTiles()) {
            g.drawImage(tetrisTileArr[c.getTileIndex()][c.getRotation()][0],
                    (int) (c.getHitbox().x - xLvlOffset - c.getXDrawOffset() - Game.TILES_SIZE / 2),
                    (int) (c.getHitbox().y - yLvlOffset - c.getYDrawOffset() - Game.TILES_SIZE / 2),
                    TETRIS_TILE_WIDTH * 2, TETRIS_TILE_HEIGHT * 2, null);
            //c.drawHitbox(g, xLvlOffset, yLvlOffset);
        }


    }

    public void drawPredictionTile(Graphics g, int xLvlOffset, int yLvlOffset, int i, boolean isPlayer1) {
        int pIndex = 1;
        if (isPlayer1)
            pIndex = 0;
        TetrisTile predTile = throwArcPredictionTiles[pIndex];
        if (predTile == null || i < 0 || i >= throwArcPredictionPoints[pIndex].length) {
            return;
        }
        int x = (int) (throwArcPredictionPoints[pIndex][i][0] - throwArcPredictionTiles[pIndex].getHitbox().width / 2);
        int y = (int) (throwArcPredictionPoints[pIndex][i][1] - throwArcPredictionTiles[pIndex].getHitbox().height / 2);
        g.drawImage(tetrisTileArr[predTile.getTileIndex()][predTile.getRotation()][pIndex + 1],
                (int) (x - xLvlOffset - predTile.getXDrawOffset() - Game.TILES_SIZE / 2),
                (int) (y - yLvlOffset - predTile.getYDrawOffset() - Game.TILES_SIZE / 2),
                TETRIS_TILE_WIDTH * 2, TETRIS_TILE_HEIGHT * 2, null);
    }

    public void checkTetrisTileGrabbed(Rectangle2D.Float grabBox, Player player) {
        for (TetrisTile c : currentLevel.getTetrisTiles())
            if (c.getLockedInBuildingZone() == null)
                if (grabBox.intersects(c.getHitbox())) {
                    c.grabbed(player);
                    return;
                }
    }

    private void loadTetrisTileImgs() {
        for (int tile = 0; tile < NUM_TETRIS_TILES; tile++) {
            tetrisTileArr[tile][0][0] = LoadSave.GetSpriteAtlas("tetris_tiles/" + tile + ".png");
            tetrisTileArr[tile][1][0] = rotateImage(tetrisTileArr[tile][0][0], 90);
            tetrisTileArr[tile][2][0] = rotateImage(tetrisTileArr[tile][0][0], 180);
            tetrisTileArr[tile][3][0] = rotateImage(tetrisTileArr[tile][0][0], 270);
        }

        for (int tile = 0; tile < NUM_TETRIS_TILES; tile++) {
            tetrisTileArr[tile][0][1] = uniformReplaceColors(tetrisTileArr[tile][0][0], THROW_ARC_COLOR_PLAYER1);
            tetrisTileArr[tile][1][1] = rotateImage(tetrisTileArr[tile][0][1], 90);
            tetrisTileArr[tile][2][1] = rotateImage(tetrisTileArr[tile][0][1], 180);
            tetrisTileArr[tile][3][1] = rotateImage(tetrisTileArr[tile][0][1], 270);
        }

        for (int tile = 0; tile < NUM_TETRIS_TILES; tile++) {
            tetrisTileArr[tile][0][2] = uniformReplaceColors(tetrisTileArr[tile][0][0], THROW_ARC_COLOR_PLAYER2);
            tetrisTileArr[tile][1][2] = rotateImage(tetrisTileArr[tile][0][2], 90);
            tetrisTileArr[tile][2][2] = rotateImage(tetrisTileArr[tile][0][2], 180);
            tetrisTileArr[tile][3][2] = rotateImage(tetrisTileArr[tile][0][2], 270);
        }
    }

    public void resetAllTetrisTiles() {
        for (TetrisTile c : currentLevel.getTetrisTiles())
            c.resetTetrisTile();
    }

    public Playing getPlaying() {
        return playing;
    }

    public int[][][] getThrowArcPredictionPoints() {
        return throwArcPredictionPoints;
    }


}
