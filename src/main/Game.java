package main;

import audio.AudioPlayer;
import entities.TetrisTile;
import gamestates.Credits;
import gamestates.Gamestate;
import gamestates.Menu;
import gamestates.Playing;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWJoystickCallback;

import java.awt.*;
import java.awt.geom.Rectangle2D;

import static utilz.Constants.*;


public class Game implements Runnable {

    public final static int TILES_DEFAULT_SIZE = 32;
    public final static float SCALE = 2f;
    //public final static int TILES_IN_WIDTH = 15;//30;//26;
    //public final static int TILES_IN_HEIGHT = 16;//16;//14;
    public final static int TILES_SIZE = (int) (TILES_DEFAULT_SIZE * SCALE);
    public final static int GAME_WIDTH = 1920;//TILES_SIZE * TILES_IN_WIDTH;
    public final static int GAME_HEIGHT = 1080;//TILES_SIZE * TILES_IN_HEIGHT;
    private static GLFWJoystickCallback joystickCallback;
    private final GamePanel gamePanel1;
    private final GamePanel gamePanel2;
    private final boolean SHOW_FPS_UPS = false;
    /**
     * used to reset double dash counter on turning to the other side
     */
    private final Direction lastDirection = Direction.LEFT;
    private final boolean keyboardRotatedTile = false;
    private final boolean attacking = false;
    /**
     * request to fall down faster
     */
    private final boolean fasterFall = false;
    // player control ++
    private final int jumpsDone = 0;
    /**
     * starting with <b>NOTHING</b> -> ACTIVATE1 (left or right, resets dash counter) <p>
     * <b>RELEASE1</b> (required since holding down a key fires continuous press events) <p>
     * <b>ACTIVATE2</b> (second movement input, reset dash counter if opposite direction) <p>
     * <b>DASHING</b> (is automatically applied if ACTIVATE2 -> start dash timer and give burst of movement)
     */
    private final DashState dashState = DashState.NOTHING;
    public GameWindow gameWindow;
    protected Rectangle2D.Float grabBox;
    private Thread gameThread;
    private Playing playing;
    private Menu menu;
    // player control
    private Credits credits;
    private AudioPlayer audioPlayer;
    /**
     * requesting to go left
     */
    private boolean left;
    /**
     * requesting to go right
     */
    private boolean right;
    /**
     * requesting to jump
     */
    private boolean jumpRequest;
    private TetrisTile isCarrying;
    /**
     * counting time in the air for jumping acceleration
     */
    private float startTimeInAir;
    private boolean attackChecked;
    /**
     * handle keyboard press/release cycle for double jump
     */
    private boolean resetJump = true;
    /**
     * if state is <b>DASHING</b> start timer <p>
     * if timer exceeds maximum reset dashState to <b>NOTHING</b>
     */
    private long dashStartTime;

    public Game() {

        System.out.println("size: " + GAME_WIDTH + " : " + GAME_HEIGHT);
        initClasses();
        initControllerInput();
        gamePanel1 = new GamePanel(this, true);
        gamePanel2 = new GamePanel(this, false);
        gameWindow = new GameWindow(gamePanel1, gamePanel2);
        gamePanel1.requestFocusInWindow();
        gamePanel2.requestFocusInWindow();
        startGameLoop();
    }

    private void initClasses() {
        audioPlayer = new AudioPlayer();
        menu = new Menu(this);
        playing = new Playing(this);
        credits = new Credits(this);


    }

    private void initControllerInput() {
        // joystick listener
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        // Set the joystick callback
        joystickCallback = new GLFWJoystickCallback() {
            @Override
            public void invoke(int jid, int event) {
                if (event == GLFW.GLFW_CONNECTED) {
                    System.out.println("Controller connected: " + jid);
                } else if (event == GLFW.GLFW_DISCONNECTED) {
                    System.out.println("Controller disconnected: " + jid);
                }
            }
        };
        GLFW.glfwSetJoystickCallback(joystickCallback);
    }

    public void keyPressed(int key) {
        switch (Gamestate.state) {
            case MENU -> menu.keyPressed(key);
            case PLAYING -> playing.keyPressed(key);
            case CREDITS -> credits.keyPressed(key);
        }
    }

    public void keyReleased(int key) {
        switch (Gamestate.state) {
            case MENU -> menu.keyReleased(key);
            case PLAYING -> playing.keyReleased(key);
            case CREDITS -> credits.keyReleased(key);
        }
    }

    private void startGameLoop() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    public void update() {
        // joystick listener
        GLFW.glfwPollEvents();
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
            case PLAYING -> {
                playing.draw(g, isPlayer1);
            }
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

    public void setJumpRequest(boolean jumpRequest) {
        //this.jump = jump;
        if (!jumpRequest && jumpsDone < MAX_ALLOWED_JUMPS) {
            resetJump = true;
        } else {
            this.jumpRequest = true;
        }
    }

    public GamePanel getGamePanel1() {
        return gamePanel1;
    }

    public GamePanel getGamePanel2() {
        return gamePanel2;
    }
}