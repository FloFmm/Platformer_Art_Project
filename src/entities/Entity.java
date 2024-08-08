package entities;

import main.Game;

import java.awt.*;
import java.awt.geom.Rectangle2D;

import static utilz.Constants.Directions.*;
import static utilz.HelpMethods.CanMoveHere;
import static utilz.HelpMethods.GetEntityXPosNextToWall;

public abstract class Entity {

    protected float x, y;
    protected int width, height;
    protected Rectangle2D.Float hitbox;
    protected int aniTick, aniIndex;
    protected int state;
    protected float airSpeed;
    protected float xSpeed;
    protected boolean inAir = false;
    protected int maxHealth;
    protected float currentHealth;
    protected Rectangle2D.Float attackBox;
    protected float walkSpeed;

    protected int pushBackDir;
    protected float pushDrawOffset;
    protected int pushBackOffsetDir = UP;

    public Entity(float x, float y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    protected void updatePushBackDrawOffset() {
        float speed = 0.95f;
        float limit = -30f;

        if (pushBackOffsetDir == UP) {
            pushDrawOffset -= speed;
            if (pushDrawOffset <= limit)
                pushBackOffsetDir = DOWN;
        } else {
            pushDrawOffset += speed;
            if (pushDrawOffset >= 0)
                pushDrawOffset = 0;
        }
    }

    protected void pushBack(int pushBackDir, int[][] lvlData, float speedMulti) {
        float xSpeed = 0;
        if (pushBackDir == LEFT)
            xSpeed = -walkSpeed;
        else
            xSpeed = walkSpeed;

        if (CanMoveHere(hitbox.x + xSpeed * speedMulti, hitbox.y, hitbox.width, hitbox.height, lvlData))
            hitbox.x += xSpeed * speedMulti;
    }

    protected void drawAttackBox(Graphics g, int xLvlOffset, int yLvlOffset) {
        g.setColor(Color.red);
        g.drawRect((int) (attackBox.x - xLvlOffset), (int) attackBox.y - yLvlOffset, (int) attackBox.width, (int) attackBox.height);
    }

    protected void drawHitbox(Graphics g, int xLvlOffset, int yLvlOffset) {
        g.setColor(Color.PINK);
        g.drawRect((int) hitbox.x - xLvlOffset, (int) hitbox.y - yLvlOffset, (int) hitbox.width, (int) hitbox.height);
    }

    protected void updateXPos(float xSpeed, int[][] lvlData) {
        float upHillHelp = 3.5f * Game.SCALE;
        if (CanMoveHere(hitbox.x + xSpeed, hitbox.y, hitbox.width, hitbox.height, lvlData))
            hitbox.x += xSpeed;
        else {
            //hitbox.x = GetEntityXPosNextToWall(hitbox, xSpeed);
            float[] playerCoord = GetEntityXPosNextToWall(hitbox, xSpeed, lvlData, 0.1f);
            if (CanMoveHere(playerCoord[0], playerCoord[1], hitbox.width, hitbox.height, lvlData)) {
                //System.out.println("went perfectly uphill");
                hitbox.x = playerCoord[0];
                hitbox.y = playerCoord[1];
            } else if (CanMoveHere(playerCoord[0], playerCoord[1] - upHillHelp, hitbox.width, hitbox.height, lvlData)) {
                //System.out.println("need little help to get up the hill");
                hitbox.x = playerCoord[0];
                hitbox.y = playerCoord[1] - upHillHelp;
            } else {
                //System.out.println("failed to move (slope uphill | next to wall) due to !CanMoveHere()");
            }
        }
    }

    protected void initHitbox(int width, int height) {
        hitbox = new Rectangle2D.Float(x, y, (int) (width * Game.SCALE), (int) (height * Game.SCALE));
    }


    public Rectangle2D.Float getHitbox() {
        return hitbox;
    }

    public int getState() {
        return state;
    }

    public int getAniIndex() {
        return aniIndex;
    }

    protected void newState(int state) {
        this.state = state;
        aniTick = 0;
        aniIndex = 0;
    }

    public Rectangle2D.Float getAttackBox() {
        return attackBox;
    }

    public void kill() {
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}