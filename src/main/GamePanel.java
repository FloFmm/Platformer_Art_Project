package main;

import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.*;

import static main.Game.GAME_HEIGHT;
import static main.Game.GAME_WIDTH;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

public class GamePanel extends JPanel implements KeyListener, MouseMotionListener {
    private final Game game;
    private final boolean isPlayer1;
    public JLabel debugOutput;
    public JLabel debugOutput1;
    public JLabel debugOutput2;
    public JLabel debugOutput3;

    public GamePanel(Game game, boolean isPlayer1) {
        this.game = game;
        this.isPlayer1 = isPlayer1;
        setFocusable(true);
        addKeyListener(this);
        setPanelSize();
        this.debugOutput = new JLabel("");
        this.debugOutput1 = new JLabel("");
        this.debugOutput2 = new JLabel("");
        this.debugOutput3 = new JLabel("");
        this.add(debugOutput);
        this.add(debugOutput1);
        this.add(debugOutput2);
        this.add(debugOutput3);
        this.addMouseListener(game.getMenu());
        this.addMouseMotionListener(game.getMenu());
    }

    private void setPanelSize() {
        Dimension size = new Dimension(GAME_WIDTH / 2, GAME_HEIGHT);
        setPreferredSize(size);
    }

    public void setDebugText(String text, int display) {
        switch (display){
            case 0:
                debugOutput.setText(text);
            case 1:
                debugOutput1.setText(text);
            case 2:
                debugOutput2.setText(text);
            case 3:
                debugOutput3.setText(text);
        }

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
}