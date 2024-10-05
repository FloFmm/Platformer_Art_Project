package ui;

import main.Game;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public abstract class UserInterface implements MouseListener, MouseMotionListener {
    protected MenuButton[] buttons;
    protected int selectedButtonIndex = 0;
    protected Game game;

    public UserInterface(Game game) {
        this.game = game;
        loadButtons();
        loadImages();
    }

    public void selectNextButton() {
        selectedButtonIndex = (selectedButtonIndex + 1) % buttons.length;
        updateButtonSelection();
    }

    public void selectPreviousButton() {
        selectedButtonIndex = (selectedButtonIndex - 1 + buttons.length) % buttons.length;
        updateButtonSelection();
    }

    public void updateButtonSelection() {
        for (int i = 0; i < buttons.length; i++) {
            buttons[i].setMouseOver(i == selectedButtonIndex);
        }
    }

    public int getSelectedButton() {
        for (int i = 0; i < buttons.length; i++) {
            if (buttons[i].isMouseOver())
                return i;
        }
        return -1;
    }

    private int getSelectedButtonIndex() {
        return selectedButtonIndex;
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
                int button_id = getSelectedButton();
                if (button_id != -1) activateButton(buttons[button_id]);
            }
        }
    }

    public void keyReleased(int key) {
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
    public void mouseReleased(MouseEvent e) {

        // This method is called when a mouse button is released
        for (MenuButton mb : buttons) {

            if (mb.getBounds().contains(e.getX(), e.getY())) {
                activateButton(mb);
                return;
            }
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        // This method is called when the mouse is moved
        for (int i = 0; i < buttons.length; i++) {
            MenuButton mb = buttons[i];
            mb.setMouseOver(false);
            if (mb.getBounds().contains(e.getX(), e.getY())) {
                mb.setMouseOver(true);
                selectedButtonIndex = i;
                break;
            }
        }
    }

    public void updateButtons() {
        for (MenuButton mb : buttons) {
            mb.update();
            if (mb.getButtonState() == GLFW.GLFW_RELEASE && mb.getPrevButtonState() == GLFW.GLFW_PRESS) {
                activateButton(mb);
            }
        }
        updateButtonSelection();
    }

    public void resetButtons() {
        for (MenuButton mb : buttons)
            mb.resetBools();
    }

    public void drawButtons(Graphics g, boolean isPlayer1) {
        int xDrawOffset = 0;
        if (!isPlayer1)
            xDrawOffset = -Game.GAME_WIDTH / 2;
        for (int i = 0; i < buttons.length; i++) {
            buttons[i].draw(g, xDrawOffset);
            if (i == selectedButtonIndex) {
                // Draw a highlight around the selected button
                Graphics2D g2d = (Graphics2D) g;
                g2d.setColor(Color.YELLOW);
                g2d.setStroke(new BasicStroke(5));
                Rectangle bounds = buttons[i].getBounds();
                g2d.drawRect(bounds.x + xDrawOffset - 2, bounds.y - 2, bounds.width + 4, bounds.height + 4);

            }
        }
    }

    public abstract void update();

    public abstract void draw(Graphics g, boolean isPlayer1);

    public abstract void loadButtons();

    public abstract void loadImages();

    public abstract void activateButton(MenuButton mb);
}
