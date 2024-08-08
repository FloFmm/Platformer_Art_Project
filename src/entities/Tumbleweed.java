package entities;

import gamestates.Playing;
import main.Game;

import static utilz.Constants.Directions.LEFT;
import static utilz.Constants.Directions.RIGHT;
import static utilz.Constants.EnemyConstants.*;
import static utilz.Constants.Environment.WATER_DMG_PER_SECOND;
import static utilz.Constants.GRAVITY;
import static utilz.Constants.UPS_SET;
import static utilz.HelpMethods.CanMoveHere;
import static utilz.HelpMethods.IsEntityOnFloor;

public class Tumbleweed extends Enemy {
    private float fallSpeedAfterCollision = 0.5f * Game.SCALE;
    private boolean moving = false;
    private int[][] lvlData;
    private float lastTimeRunning;
    private boolean friendly = true;
    private float sizeFactor;
    private int xDrawOffset, yDrawOffset;

    public Tumbleweed(float x, float y, float sizeFactor, int[][] lvlData) {
        super(x, y, (int) (sizeFactor * TUMBLE_WEED_WIDTH), (int) (sizeFactor * TUMBLE_WEED_HEIGHT), TUMBLE_WEED);
        this.sizeFactor = sizeFactor;
        this.lvlData = lvlData;
        initHitbox((int) (sizeFactor * TUMBLE_WEED_HITBOX_WIDTH_DEFAULT), (int) (sizeFactor * TUMBLE_WEED_HITBOX_HEIGHT_DEFAULT));
        initAttackBox((int) (sizeFactor * TUMBLE_WEED_HITBOX_WIDTH_DEFAULT), (int) (sizeFactor * TUMBLE_WEED_HITBOX_HEIGHT_DEFAULT), 0);
        this.xDrawOffset = (int) (TUMBLE_WEED_DRAWOFFSET_X * sizeFactor);
        this.yDrawOffset = (int) (TUMBLE_WEED_DRAWOFFSET_Y * sizeFactor);
    }

    public void update(int[][] lvlData, Playing playing) {
        if (currentHealth <= 0) {
            if (state != DEAD) {
                state = DEAD;
                aniTick = 0;
                aniIndex = 0;

                // Check if died in air
                if (!IsEntityOnFloor(hitbox, lvlData)) {
                    inAir = true;
                    airSpeed = 0;
                }
            } else if (aniIndex == GetSpriteAmount(TUMBLE_WEED, DEAD) - 1 && aniTick >= aniSpeed - 1) {
                resetTumbleWeed();
            } else {
                updateAnimationTick();

                // Fall if in air
                if (inAir)
                    if (CanMoveHere(hitbox.x, hitbox.y + airSpeed, hitbox.width, hitbox.height, lvlData)) {
                        hitbox.y += airSpeed;
                        airSpeed += GRAVITY;
                    } else
                        inAir = false;
            }
            return;
        }

        updatePos(playing.getWindSpeed());
        updateAttackBox();

        playing.getObjectManager().checkSpikesTouched(this);

        // check inside water
        if (hitbox.y + hitbox.height * 0.75f > playing.getCurrentWaterYPos())
            currentHealth -= WATER_DMG_PER_SECOND / UPS_SET;

        updateAnimationTick();
        setAnimation(lvlData, playing);

        if (!friendly && playing.getPlayer1().getPowerAttackNotActive())
            checkPlayerHit(attackBox, playing.getPlayer1());

        if (!friendly && playing.getPlayer1().getPowerAttackNotActive())
            checkPlayerHit(attackBox, playing.getPlayer2());
    }

    private void setAnimation(int[][] lvlData, Playing playing) {
        aniSpeed = (int) (TUMBLE_WEED_MIN_ANI_SPEED - Math.abs(xSpeed) / TUMBLE_WEED_MAX_SPEED * (TUMBLE_WEED_MIN_ANI_SPEED - TUMBLE_WEED_MAX_ANI_SPEED) / sizeFactor);
        int startAni = state;

        if (state == HIT)
            return;

        if (moving) {
            state = RUNNING;
            lastTimeRunning = playing.getGameTimeInSeconds();
        } else if ((playing.getGameTimeInSeconds() - lastTimeRunning) > 0.2f)
            state = IDLE;
        if (startAni != state)
            resetAniTick();
    }

    private void updatePos(float windSpeed) {
        float oldXPos = hitbox.x;
        float oldYPos = hitbox.y;
        if ((Math.signum(xSpeed) == Math.signum(windSpeed) && Math.abs(xSpeed) < Math.abs(TUMBLE_WEED_MAX_SPEED)) || Math.signum(xSpeed) != Math.signum(windSpeed))
            xSpeed += windSpeed / (UPS_SET * TUMBLE_WEED_TIME_TO_REACH_WIND_SPEED) / sizeFactor;
        if (!inAir)
            if (!IsEntityOnFloor(hitbox, lvlData))
                inAir = true;
        if (inAir) {
            if (CanMoveHere(hitbox.x, hitbox.y + airSpeed, hitbox.width, hitbox.height, lvlData)) {
                hitbox.y += airSpeed;
                airSpeed += GRAVITY;
                updateXPos(xSpeed, lvlData);
            } else {
                if (airSpeed > 0)
                    resetInAir();
                else
                    airSpeed = fallSpeedAfterCollision;
            }

        } else {
            updateXPos(xSpeed, lvlData);
        }
        float floatError = 0.0001f;
        moving = !(oldXPos - floatError <= hitbox.x && hitbox.x <= oldXPos + floatError &&
                oldYPos - floatError <= hitbox.y && hitbox.y <= oldYPos + floatError);
        if (moving)
            if (hitbox.x > oldXPos)
                walkDir = RIGHT;
            else
                walkDir = LEFT;
    }

    private void resetInAir() {
        inAir = false;
        airSpeed = 0;
    }

    private void resetTumbleWeed() {
        state = IDLE;
        currentHealth = maxHealth;

        if (!IsEntityOnFloor(hitbox, lvlData))
            inAir = true;

        xSpeed = 0;
        airSpeed = 0;
        moving = false;

        hitbox.x = x;
        hitbox.y = y;
        attackBox.x = x;
        attackBox.y = y;
        if (!IsEntityOnFloor(hitbox, lvlData))
            inAir = true;
    }

    private void resetAniTick() {
        aniTick = 0;
        aniIndex = 0;
    }

    public boolean getMoving() {
        return moving;
    }

    public boolean getFriendly() {
        return friendly;
    }

    public float getSizeFactor() {
        return sizeFactor;
    }

    public int getXDrawOffset() {
        return xDrawOffset;
    }

    public int getYDrawOffset() {
        return yDrawOffset;
    }
}