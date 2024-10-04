package audio;

import java.io.IOException;
import java.net.URL;
import java.util.Random;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.UnsupportedAudioFileException;

public class AudioPlayer {


    public static int WIND = 0;
    public static int MENU = 1;
    public float windFactor = MIN_WIND_VOLUME_FACTOR;
    public static final float MIN_WIND_VOLUME_FACTOR = 0.6f;
    public static final float MAX_WIND_VOLUME_FACTOR = 1.0f;

    public static final int SMALL_EXPLOSION = 0;
    public static final int BIG_EXPLOSION = 1;
    public static final float SMALL_EXPLOSION_VOLUME = 0.7f;
    public static final float BIG_EXPLOSION_VOLUME = 0.8f;

    private Clip[] songs, effects;
    private int currentSongId;
    private float volume = 1.0f;

    public AudioPlayer() {
        loadSongs();
        loadEffects();
        playSong(MENU);
    }

    private void loadSongs() {
        String[] names = {"wind"};
        songs = new Clip[names.length];
        for (int i = 0; i < songs.length; i++)
            songs[i] = getClip(names[i]);
    }

    private void loadEffects() {
        String[] effectNames = {"small_explosion", "big_explosion"};
        effects = new Clip[effectNames.length];
        for (int i = 0; i < effects.length; i++)
            effects[i] = getClip(effectNames[i]);

        updateEffectsVolume();

    }

    private Clip getClip(String name) {

        URL url = getClass().getResource("/audio/" + name + ".wav");
        System.out.println(url);
        AudioInputStream audio;

        try {
            audio = AudioSystem.getAudioInputStream(url);
            Clip c = AudioSystem.getClip();
            c.open(audio);
            return c;

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {

            e.printStackTrace();
        }

        return null;

    }

    public void setVolume(float volume) {
        this.volume = volume;
        updateSongVolume();
        updateEffectsVolume();
    }

    public void stopSong() {
        if (currentSongId >= songs.length || currentSongId < 0 || songs[currentSongId] == null)
            return;

        if (songs[currentSongId].isActive())
            songs[currentSongId].stop();
    }


    public void playSmallExplosion() {
        playEffect(SMALL_EXPLOSION);
    }

    public void playBigExplosion() {
        playEffect(BIG_EXPLOSION);
    }

    public void playEffect(int effect) {
        if (effects[effect].getMicrosecondPosition() > 0)
            effects[effect].setMicrosecondPosition(0);
        effects[effect].start();
    }

    public void playSong(int song) {
        stopSong();

        currentSongId = song;
        updateSongVolume();
        if (currentSongId >= songs.length || currentSongId < 0 || songs[currentSongId] == null)
            return;

        songs[currentSongId].setMicrosecondPosition(0);
        songs[currentSongId].loop(Clip.LOOP_CONTINUOUSLY);
    }


    public void updateSongVolume() {
        if (currentSongId >= songs.length || currentSongId < 0 || songs[currentSongId] == null)
            return;
        FloatControl gainControl = (FloatControl) songs[currentSongId].getControl(FloatControl.Type.MASTER_GAIN);
        float range = gainControl.getMaximum() - gainControl.getMinimum();
        float gain = (range * volume * windFactor) + gainControl.getMinimum();
        gainControl.setValue(gain);
        //System.out.println(volume * windFactor);
    }

    private void updateEffectsVolume() {

        for (int i = 0; i < effects.length; i += 1) {
            float factor = 1.0f;
            switch (i) {
                case (SMALL_EXPLOSION):
                    factor = SMALL_EXPLOSION_VOLUME;
                    break;
                case (BIG_EXPLOSION):
                    factor = BIG_EXPLOSION_VOLUME;
                    break;
            }
            Clip c = effects[i];
            FloatControl gainControl = (FloatControl) c.getControl(FloatControl.Type.MASTER_GAIN);
            float range = gainControl.getMaximum() - gainControl.getMinimum();
            float gain = (range * volume * factor) + gainControl.getMinimum();
            gainControl.setValue(gain);
        }
    }


}
