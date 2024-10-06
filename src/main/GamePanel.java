package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import gamestates.Gamestate;
import static main.Game.GAME_HEIGHT;
import static main.Game.GAME_WIDTH;

public class GamePanel extends JPanel implements KeyListener, MouseMotionListener {
    private final Game game;
    private final boolean isPlayer1;


    public GamePanel(Game game, boolean isPlayer1) {
        this.game = game;
        this.isPlayer1 = isPlayer1;
        setPanelSize();
        setFocusable(true);
        addKeyListener(this);
        this.addMouseListener(game.getMenu().getUI());
        this.addMouseMotionListener(game.getMenu().getUI());
    }

    private void setPanelSize() {
        Dimension size = new Dimension(GAME_WIDTH / 2, GAME_HEIGHT);
        setPreferredSize(size);
    }

    public void updateGame() {

    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        game.render(g, isPlayer1);
    }

    public Game getGame() {
        return game;
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        game.keyPressed(e.getKeyCode());
    }

    @Override
    public void keyReleased(KeyEvent e) {
        game.keyReleased(e.getKeyCode());
    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    public void removeMouseListeners() {
        for (MouseListener listener : getMouseListeners()) {
            removeMouseListener(listener);
        }
        for (MouseMotionListener listener : getMouseMotionListeners()) {
            removeMouseMotionListener(listener);
        }
    }

    public void addMouseListeners(Gamestate state) {
        switch (state) {
            case MENU:
                addMouseListener(game.getMenu().getUI());
                addMouseMotionListener(game.getMenu().getUI());
                break;
            case PLAYING:
                addMouseListener(game.getPlaying().getUI());
                addMouseMotionListener(game.getPlaying().getUI());
                addMouseListener(game.getPlaying().getUIGameOver());
                addMouseMotionListener(game.getPlaying().getUIGameOver());
                break;
            case CREDITS:
                addMouseListener(game.getCredits().getUI());
                addMouseMotionListener(game.getCredits().getUI());
                break;
            case LEVEL_SELECTION:
                addMouseListener(game.getLevelSelection().getUI());
                addMouseMotionListener(game.getLevelSelection().getUI());
                break;
        }
    }


}