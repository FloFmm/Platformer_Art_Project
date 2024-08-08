package gamestates;

import audio.AudioPlayer;
import main.Game;
import ui.MenuButton;

import java.awt.event.MouseEvent;

public abstract class State {

    protected Game game;

    public State(Game game) {
        this.game = game;
    }

    public boolean isIn(MouseEvent e, MenuButton mb) {
        return mb.getBounds().contains(e.getX(), e.getY());
    }

    public Game getGame() {
        return game;
    }

    @SuppressWarnings("incomplete-switch")
    public void setGamestate(Gamestate state) {
        switch (state) {
            case MENU -> game.getAudioPlayer().playSong(AudioPlayer.MENU);
            case PLAYING -> game.getAudioPlayer().playSong(AudioPlayer.WIND);
        }

        Gamestate.state = state;
    }

    public abstract void mouseMoved(MouseEvent e);
}