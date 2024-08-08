package objects;

import entities.Enemy;
import entities.Entity;
import entities.Player;
import gamestates.Playing;
import levels.Level;
import utilz.LoadSave;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import static utilz.Constants.ObjectConstants.EXPLOSION;
import static utilz.Constants.ObjectConstants.GetSpriteAmount;

public class ObjectManager {

    private Playing playing;
    private BufferedImage[] explosionImgs;
    private ArrayList<Explosion> explosions;

    private Level currentLevel;

    public ObjectManager(Playing playing) {
        this.playing = playing;
        currentLevel = playing.getLevelManager().getCurrentLevel();
        loadImgs();
    }

    private void loadImgs() {
        //explosions
        explosionImgs = new BufferedImage[GetSpriteAmount(EXPLOSION)];
        for (int i = 0; i < explosionImgs.length; i++) {
            explosionImgs[i] = LoadSave.GetSpriteAtlas("animation/explosion/explosion" + i + ".png");
        }
    }

    public void checkSpikesTouched(Entity p) {
        for (Spike s : currentLevel.getSpikes())
            if (s.getHitbox().intersects(p.getHitbox()))
                p.kill();
    }

    public void checkSpikesTouched(Enemy e) {
        for (Spike s : currentLevel.getSpikes())
            if (s.getHitbox().intersects(e.getHitbox()))
                e.hurt(200);
    }

    public void loadObjects(Level newLevel) {
        currentLevel = newLevel;
    }

    public void update(int[][] lvlData, Player player1, Player player2) {
        for (Explosion p : explosions)
            p.update();
    }

    public void draw(Graphics g, int xLvlOffset, int yLvlOffset) {
        drawExplosions(g, xLvlOffset, yLvlOffset);
    }

    private void drawExplosions(Graphics g, int xLvlOffset, int yLvlOffset) {
        for (Explosion p : explosions)
            if (p.isActive()) {
                g.drawImage(explosionImgs[p.getAniIndex()], (int) (p.x - p.getWidth() / 2 - xLvlOffset), (int) (p.y - p.getHeight() / 2 - yLvlOffset), p.getWidth(), p.getHeight(),
                        null);
            }
    }

    public void addExplosion(int x, int y, int width, int height) {
        for (Explosion ex : explosions) {
            if (!ex.isActive()) {
                ex.reset();
                ex.setX(x);
                ex.setY(y);
                ex.setWidth(width);
                ex.setHeight(height);
                return;
            }
        }
        explosions.add(new Explosion(x, y, width, height, EXPLOSION));
    }

    public void resetAllObjects() {
        explosions = new ArrayList<Explosion>();
    }
}
