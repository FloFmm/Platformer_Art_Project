package main;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class GameWindow {
	private JFrame jframe;

	public GameWindow(GamePanel gamePanel1, GamePanel gamePanel2) {

		jframe = new JFrame();
		//jframe.setUndecorated(true);
		jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// Set Layout for the content pane of the frame
		jframe.getContentPane().setLayout(new GridLayout(1, 2));
        
        // Add the panels to the frame
		jframe.getContentPane().add(gamePanel1);
		jframe.getContentPane().add(gamePanel2);
		
		jframe.setResizable(false);
		jframe.pack();
		jframe.setLocationRelativeTo(null);
		jframe.setVisible(true);
		jframe.addWindowFocusListener(new WindowFocusListener() {

			@Override
			public void windowLostFocus(WindowEvent e) {
				gamePanel1.getGame().windowFocusLost();
				gamePanel2.getGame().windowFocusLost();
			}

			@Override
			public void windowGainedFocus(WindowEvent e) {
				// TODO Auto-generated method stub

			}
		});

	}

}
