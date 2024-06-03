package main;

import java.awt.Graphics;

import org.lwjgl.glfw.GLFW;

import audio.AudioPlayer;
import gamestates.Credits;
import gamestates.Gamestate;
import gamestates.Menu;
import gamestates.Playing;

import static utilz.Constants.*;


public class Game implements Runnable {

	private GamePanel gamePanel1;
	private GamePanel gamePanel2;
	private Thread gameThread;
	

	private Playing playing;
	private Menu menu;
	private Credits credits;
	private AudioPlayer audioPlayer;

	public final static int TILES_DEFAULT_SIZE = 32;
	public final static float SCALE = 2f;
	//public final static int TILES_IN_WIDTH = 15;//30;//26;
	//public final static int TILES_IN_HEIGHT = 16;//16;//14;
	public final static int TILES_SIZE = (int) (TILES_DEFAULT_SIZE * SCALE);
	public final static int GAME_WIDTH = 1920;//TILES_SIZE * TILES_IN_WIDTH;
	public final static int GAME_HEIGHT = 1080;//TILES_SIZE * TILES_IN_HEIGHT;

	private final boolean SHOW_FPS_UPS = false;

	public Game() {
	
		System.out.println("size: " + GAME_WIDTH + " : " + GAME_HEIGHT);
		initClasses();
		gamePanel1 = new GamePanel(this, true);
		gamePanel2 = new GamePanel(this, false);
		new GameWindow(gamePanel1, gamePanel2);
		gamePanel1.requestFocusInWindow();
		gamePanel2.requestFocusInWindow();
		startGameLoop();
	}

	private void initClasses() {
		audioPlayer = new AudioPlayer();
		menu = new Menu(this);
		playing = new Playing(this);
		credits = new Credits(this);
		
		// joystick listener
		if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }
	}

	private void startGameLoop() {
		gameThread = new Thread(this);
		gameThread.start();
	}

	public void update() {
		switch (Gamestate.state) {
		case MENU -> menu.update();
		case PLAYING -> playing.update();
		case CREDITS -> credits.update();
		case QUIT -> System.exit(0);
		}
	}

	@SuppressWarnings("incomplete-switch")
	public void render(Graphics g, boolean isPlayer1) {
		switch (Gamestate.state) {
		case MENU -> menu.draw(g, isPlayer1);
		case PLAYING -> playing.draw(g, isPlayer1);
		case CREDITS -> credits.draw(g, isPlayer1);
		}
	}

	@Override
	public void run() {
		double timePerFrame = 1000000000.0 / FPS_SET;
		double timePerUpdate = 1000000000.0 / UPS_SET;

		long previousTime = System.nanoTime();

		int frames = 0;
		int updates = 0;
		long lastCheck = System.currentTimeMillis();

		double deltaU = 0;
		double deltaF = 0;

		while (true) {

			long currentTime = System.nanoTime();

			deltaU += (currentTime - previousTime) / timePerUpdate;
			deltaF += (currentTime - previousTime) / timePerFrame;
			previousTime = currentTime;

			if (deltaU >= 1) {

				update();
				updates++;
				deltaU--;

			}

			if (deltaF >= 1) {

				gamePanel1.repaint();
				gamePanel2.repaint();
				frames++;
				deltaF--;

			}

			if (SHOW_FPS_UPS)
				if (System.currentTimeMillis() - lastCheck >= 1000) {

					lastCheck = System.currentTimeMillis();
					System.out.println("FPS: " + frames + " | UPS: " + updates);
					frames = 0;
					updates = 0;

				}

		}
	}

	public void windowFocusLost() {
		if (Gamestate.state == Gamestate.PLAYING) {
			playing.getPlayer1().resetDirBooleans();
			playing.getPlayer2().resetDirBooleans();
			
		}
	}

	public Menu getMenu() {
		return menu;
	}

	public Playing getPlaying() {
		return playing;
	}

	public Credits getCredits() {
		return credits;
	}


	public AudioPlayer getAudioPlayer() {
		return audioPlayer;
	}
}