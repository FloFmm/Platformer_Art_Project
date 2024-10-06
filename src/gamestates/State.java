package gamestates;

import main.Game;
import ui.UserInterface;

import java.awt.*;

public abstract class State {
    protected Game game;
    protected UserInterface ui;

    public State(Game game) {
        this.game = game;
    }

    public Game getGame() {
        return game;
    }

    public UserInterface getUI() {
        return ui;
    }

    public void keyPressed(int key) {
        ui.keyPressed(key);
    }

    public void keyReleased(int key) {
        ui.keyReleased(key);
    }

    public abstract void loadImages();

    public abstract void draw(Graphics g, boolean isPlayer1);

    public abstract void update();
}