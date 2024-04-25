package main;

import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JPanel;
import inputs.KeyboardInputs;
import inputs.MouseInputs;
import static main.Game.GAME_HEIGHT;
import static main.Game.GAME_WIDTH;

public class GamePanel extends JPanel {

	private MouseInputs mouseInputs;
	private Game game;
	private final boolean isPlayer1;

	public GamePanel(Game game, boolean isPlayer1) {
		mouseInputs = new MouseInputs(this);
		this.game = game;
		this.isPlayer1 = isPlayer1;
		setPanelSize();
		addKeyListener(new KeyboardInputs(this));
		addMouseListener(mouseInputs);
		addMouseMotionListener(mouseInputs);
		
		
	}

	private void setPanelSize() {
		Dimension size = new Dimension(GAME_WIDTH/2, GAME_HEIGHT);
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

}