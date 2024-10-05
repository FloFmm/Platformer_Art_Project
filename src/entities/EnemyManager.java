package entities;

import gamestates.Playing;
import levels.Level;
import utilz.LoadSave;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import static utilz.Constants.EnemyConstants.*;
import static utilz.HelpMethods.rotateImage;

public class EnemyManager {

    private final Playing playing;
    private BufferedImage[][] tumbleWeedArr;
    private Level currentLevel;

    public EnemyManager(Playing playing) {
        this.playing = playing;
        loadEnemyImgs();
    }

    public void loadEnemies(Level level) {
        this.currentLevel = level;
    }

    public void update(int[][] lvlData) {
        for (Tumbleweed c : currentLevel.getCrabs())
            if (c.isActive()) {
                c.update(lvlData, playing);
            }
    }

    public void draw(Graphics g, int xLvlOffset, int yLvlOffset) {
        drawTumbleWeeds(g, xLvlOffset, yLvlOffset);
    }


    private void drawTumbleWeeds(Graphics g, int xLvlOffset, int yLvlOffset) {
        for (Tumbleweed c : currentLevel.getCrabs()) {
            if (c.isActive()) {

                g.drawImage(tumbleWeedArr[c.getState()][c.getAniIndex()],
                        (int) (c.getHitbox().x - xLvlOffset - c.getXDrawOffset() + c.flipX()),
                        (int) (c.getHitbox().y - yLvlOffset - c.getYDrawOffset()),
                        c.getWidth() * c.flipW(),
                        c.getHeight(), null);

                //c.drawHitbox(g, xLvlOffset, yLvlOffset);
                //c.drawAttackBox(g, xLvlOffset, yLvlOffset);
            }
        }
    }

    public void checkEnemyHit(Rectangle2D.Float attackBox) {
        for (Tumbleweed c : currentLevel.getCrabs())
            if (c.isActive())
                if (c.getState() != DEAD && c.getState() != HIT)
                    if (attackBox.intersects(c.getHitbox())) {
                        c.hurt(20);
                        return;
                    }
    }

    private void loadEnemyImgs() {
        tumbleWeedArr = new BufferedImage[TUMBLE_WEED_NUM_ANIMATIONS][TUMBLE_WEED_MAX_ANIMATION_LENGTH];
        tumbleWeedArr[IDLE][0] = LoadSave.GetSpriteAtlas(LoadSave.TUMBLE_WEED_SPRITE);
        tumbleWeedArr[RUNNING][0] = tumbleWeedArr[IDLE][0];

        for (int i = 1; i < GetSpriteAmount(TUMBLE_WEED, RUNNING); i++) {
            tumbleWeedArr[RUNNING][i] = rotateImage(tumbleWeedArr[RUNNING][i - 1], 360 / GetSpriteAmount(TUMBLE_WEED, RUNNING));
        }

        if (GetSpriteAmount(TUMBLE_WEED, HIT) >= 0)
            System.arraycopy(tumbleWeedArr[RUNNING], 0, tumbleWeedArr[HIT], 0, GetSpriteAmount(TUMBLE_WEED, HIT));

        if (GetSpriteAmount(TUMBLE_WEED, DEAD) >= 0)
            System.arraycopy(tumbleWeedArr[RUNNING], 0, tumbleWeedArr[DEAD], 0, GetSpriteAmount(TUMBLE_WEED, DEAD));
    }

    public void resetAllEnemies() {
        for (Tumbleweed c : currentLevel.getCrabs())
            c.resetEnemy();
    }

}
