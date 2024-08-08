package gamestates;

import audio.AudioPlayer;
import main.Game;
import org.lwjgl.glfw.GLFW;
import ui.MenuButton;
import ui.VolumeButton;
import utilz.LoadSave;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

import static utilz.Constants.ControllerConstants.*;
import static utilz.Constants.UI.VolumeButtons.SLIDER_WIDTH;
import static utilz.Constants.UI.VolumeButtons.VOLUME_HEIGHT;

public class Menu extends State implements Statemethods, MouseListener, MouseMotionListener {
    private final MenuButton[] buttons = new MenuButton[4];
    private final boolean useVolumeButton = false;
    private BufferedImage backgroundImg, backgroundImgPink, controllerOnlineImg, controllerOfflineImg;
    private int menuX, menuY, menuWidth, menuHeight;
    private VolumeButton volumeButton;
    private int selectedButtonIndex = 0;

    public Menu(Game game) {
        super(game);
        loadButtons();
        loadImages();
    }

    private void loadImages() {
        backgroundImgPink = LoadSave.GetSpriteAtlas(LoadSave.MENU_BACKGROUND_IMG);
        backgroundImg = LoadSave.GetSpriteAtlas(LoadSave.MENU_BACKGROUND);
        controllerOnlineImg = LoadSave.GetSpriteAtlas(LoadSave.CONTROLLER_ONLINE);
        controllerOfflineImg = LoadSave.GetSpriteAtlas(LoadSave.CONTROLLER_OFFLINE);
        float heightFactor = 1.0f, widthFactor = 1.0f;
        menuWidth = (int) (Game.GAME_WIDTH * widthFactor);
        menuHeight = (int) (Game.GAME_HEIGHT * heightFactor);
        menuX = Game.GAME_WIDTH / 20;
        menuY = 0; //Game.GAME_HEIGHT / 2 - menuHeight / 2 - (int) ((1-heightFactor)/3*Game.GAME_HEIGHT);
    }

    private void loadButtons() {
        buttons[0] = new MenuButton(Game.GAME_WIDTH / 6, (int) (130 * Game.SCALE), 0, Gamestate.PLAYING, CONTROLLER_A_BUTTON_ID, game);
        buttons[1] = new MenuButton(Game.GAME_WIDTH / 6, (int) (200 * Game.SCALE), 1, Gamestate.PLAYING, CONTROLLER_X_BUTTON_ID, game);
        buttons[2] = new MenuButton(Game.GAME_WIDTH / 6, (int) (270 * Game.SCALE), 2, Gamestate.CREDITS, CONTROLLER_Y_BUTTON_ID, game);
        buttons[3] = new MenuButton(Game.GAME_WIDTH / 6, (int) (340 * Game.SCALE), 3, Gamestate.MENU, CONTROLLER_B_BUTTON_ID, game);
        if (useVolumeButton)
            volumeButton = new VolumeButton((Game.GAME_WIDTH / 6 - SLIDER_WIDTH / 2), (int) (410 * Game.SCALE), SLIDER_WIDTH, VOLUME_HEIGHT, game);
    }

    @Override
    public void update() {
        for (MenuButton mb : buttons) {
            mb.update();

            if (mb.getButtonState() == GLFW.GLFW_RELEASE && mb.getPrevButtonState() == GLFW.GLFW_PRESS) {
                activateButton(mb.getRowIndex());
                mb.applyGamestate();
                if (mb.getState() == Gamestate.PLAYING) {
                    int rowId = mb.getRowIndex();
                    game.getPlaying().loadLevel(rowId, true);
                    game.getAudioPlayer().playSong(AudioPlayer.WIND);
                }
                selectButton(mb.getRowIndex());
            }

        }
        updateButtonSelection();
        if (useVolumeButton)
            volumeButton.update();
    }

    @Override
    public void draw(Graphics g, boolean isPlayer1) {
        int xDrawOffset = 0;
        if (!isPlayer1)
            xDrawOffset = -Game.GAME_WIDTH / 2;
        g.drawImage(backgroundImgPink, xDrawOffset, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT, null);
        g.drawImage(backgroundImg, menuX + xDrawOffset, menuY, menuWidth, menuHeight, null);

        if (GLFW.glfwJoystickPresent(GLFW.GLFW_JOYSTICK_1))
            g.drawImage(controllerOnlineImg, (int) (Game.GAME_WIDTH * 0.8 + xDrawOffset),
                    (int) (Game.GAME_WIDTH * 0.01), (int) (Game.GAME_WIDTH * 0.1), (int) (Game.GAME_HEIGHT * 0.1), null);
        else
            g.drawImage(controllerOfflineImg, (int) (Game.GAME_WIDTH * 0.8 + xDrawOffset),
                    (int) (Game.GAME_WIDTH * 0.01), (int) (Game.GAME_WIDTH * 0.1), (int) (Game.GAME_HEIGHT * 0.1), null);

        if (GLFW.glfwJoystickPresent(GLFW.GLFW_JOYSTICK_2))
            g.drawImage(controllerOnlineImg, (int) (Game.GAME_WIDTH * 0.9 + xDrawOffset),
                    (int) (Game.GAME_WIDTH * 0.01), (int) (Game.GAME_WIDTH * 0.1), (int) (Game.GAME_HEIGHT * 0.1), null);
        else
            g.drawImage(controllerOfflineImg, (int) (Game.GAME_WIDTH * 0.9 + xDrawOffset),
                    (int) (Game.GAME_WIDTH * 0.01), (int) (Game.GAME_WIDTH * 0.1), (int) (Game.GAME_HEIGHT * 0.1), null);


        for (int i = 0; i < buttons.length; i++) {
            buttons[i].draw(g, xDrawOffset);
            if (i == selectedButtonIndex) {
                // Draw a highlight around the selected button
                g.setColor(Color.YELLOW);
                Rectangle bounds = buttons[i].getBounds();
                g.drawRect(bounds.x + xDrawOffset - 2, bounds.y - 2, bounds.width + 4, bounds.height + 4);

            }
        }
        if (useVolumeButton)
            volumeButton.draw(g, xDrawOffset);
    }

    private void resetButtons() {
        for (MenuButton mb : buttons)
            mb.resetBools();
    }

    public void keyPressed(int key) {
        switch (key) {
            case KeyEvent.VK_DOWN -> {
                selectNextButton();
            }
            case KeyEvent.VK_UP -> {
                selectPreviousButton();
            }
            case KeyEvent.VK_ENTER -> {
                if (getSelectedButton() != -1) activateButton(getSelectedButton());
            }
            case KeyEvent.VK_ESCAPE -> {
                Gamestate.state = Gamestate.PLAYING;
            }
        }
    }

    public void keyReleased(int key) {
        // You can add specific behavior for key releases if needed
    }

    private void activateButton(int index) {
        if (index >= 0 && index < buttons.length) {
            setGamestate(buttons[index].getState());
            if (buttons[index].getState() == Gamestate.MENU) {
                Gamestate.state = Gamestate.QUIT;
                return;
            } else if (buttons[index].getState() == Gamestate.QUIT) {
                return;
            }
            game.getPlaying().loadLevel(index, true);
            game.getPlaying().setLoading(true);
            game.getPlaying().update();
            game.getAudioPlayer().playSong(AudioPlayer.WIND);
            buttons[index].applyGamestate();
        }
    }

    private void selectNextButton() {
        selectedButtonIndex = (selectedButtonIndex + 1) % buttons.length;
        updateButtonSelection();
    }

    private void selectPreviousButton() {
        selectedButtonIndex = (selectedButtonIndex - 1 + buttons.length) % buttons.length;
        updateButtonSelection();
    }

    private void updateButtonSelection() {
        for (int i = 0; i < buttons.length; i++) {
            buttons[i].setMouseOver(i == selectedButtonIndex);
        }
    }

    private int getSelectedButtonIndex() {
        return selectedButtonIndex;
    }

    private int getSelectedButton() {
        for (int i = 0; i < buttons.length; i++) {
            if (buttons[i].isMouseOver())
                return i;
        }
        return -1;
    }

    private void selectButton(int index) {
        buttons[index].applyGamestate();
        if (buttons[index].getState() == Gamestate.PLAYING) {
            game.getPlaying().loadLevel(buttons[index].getRowIndex(), true);
            game.getAudioPlayer().playSong(AudioPlayer.WIND);
        }
        resetButtons();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // This method is called when a mouse button is clicked
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // This method is called when a mouse button is pressed
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // This method is called when a mouse button is released
        for (MenuButton mb : buttons) {
            if (mb.getBounds().contains(e.getX(), e.getY())) {
                activateButton(mb.getRowIndex());
                return;
            }
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // This method is called when the mouse enters the component
    }

    @Override
    public void mouseExited(MouseEvent e) {
        // This method is called when the mouse exits the component
    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {
        // This method is called when the mouse is moved
        for (MenuButton mb : buttons) {
            mb.setMouseOver(false);
            if (mb.getBounds().contains(e.getX(), e.getY())) {
                mb.setMouseOver(true);
                selectedButtonIndex = mb.getRowIndex();
                break;
            }
        }
    }


}