package objects;

import static utilz.Constants.ObjectConstants.*;

import main.Game;

public class Explosion extends GameObject {
    private int width, height;

    public Explosion(int x, int y, int width, int height, int objType) {
        super(x, y, objType);
        doAnimation = true;
        this.width = width;
        this.height = height;
    }

    public void update() {
        if (doAnimation)
            updateAnimationTick();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;

    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setX(int x) {
        this.x = x;
    }
}
