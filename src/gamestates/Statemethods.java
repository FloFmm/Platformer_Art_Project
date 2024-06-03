package gamestates;

import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public interface Statemethods {
	public void update();

	public void draw(Graphics g, boolean isPlayer1);

	public void keyPressed(KeyEvent e);

	public void keyReleased(KeyEvent e);
	
	public void keyTyped(KeyEvent e);

}
