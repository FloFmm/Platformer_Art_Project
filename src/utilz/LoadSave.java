package utilz;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

public class LoadSave {
	public static final String LOADING = "displays/loading.png";
	public static final String LEVEL_ATLAS = "outside_sprites.png";
	public static final String MENU_BUTTONS = "displays/button_atlas.png";
	public static final String MENU_BACKGROUND = "displays/controller_labeled.png";
	public static final String PAUSE_BACKGROUND = "displays/pause_menu.png";
	public static final String SOUND_BUTTONS = "displays/sound_button.png";
	public static final String URM_BUTTONS = "displays/urm_buttons.png";
	public static final String MENU_BACKGROUND_IMG = "displays/menu_background.png";
	public static final String PLAYING_BG_IMG = "playing_bg_img.png";
	public static final String PLAYING_FG_IMG = "playing_fg_img.png";
	
	public static final String TUMBLE_WEED_SPRITE = "animation/tumbleweed/tumble_weed_sprite.png";
	
	public static final String STATUS_BAR = "displays/health_power_bar.png";
	public static final String TEMP_SCALE = "displays/middle_seperator.png";
	public static final String WINDSOCK1 = "displays/flag1.png";
	public static final String WINDSOCK2 = "displays/flag2.png";
	public static final String WINDSOCK3 = "displays/flag3.png";
	
	
	public static final String OPTIONS_MENU = "displays/options_background.png";
	public static final String CREDITS = "displays/credits.png";
	public static final String CONTROLLER_ONLINE = "displays/controller_online.png";
	public static final String CONTROLLER_OFFLINE = "displays/controller_offline.png";
	public static final String PAUSED = "displays/paused.png";
	public static final String VICTORY_IMG = "displays/victory.png";
	public static final String DEFEAT_IMG = "displays/defeat.png";
	public static final String VOLUME_BUTTONS = "displays/volume_buttons.png";

	public static BufferedImage GetSpriteAtlas(String fileName) {
		BufferedImage img = null;
		InputStream is = LoadSave.class.getResourceAsStream("/" + fileName);
		try {
			img = ImageIO.read(is);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (Exception e) {
				System.out.println("failed to load " + fileName);
				e.printStackTrace();
			}
		}
		return img;
	}
	
    public static boolean SaveImage(BufferedImage img, String formatName, String path) {
        File outputFile = new File(path);
        boolean result = false;
        try {
            result = ImageIO.write(img, formatName, outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
    public static BufferedImage[] GetAllLevels() {
        List<BufferedImage> imageList = new ArrayList<>();
        
        // Get the resources as input streams
        for (int i = 1; ; i++) {
            String resourcePath = "/lvls/" + i + ".png";
            InputStream inputStream = LoadSave.class.getResourceAsStream(resourcePath);
            if (inputStream == null) {
                break; // No more levels found
            }
            try {
                BufferedImage image = ImageIO.read(inputStream);
                imageList.add(image);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // Convert list to array
        BufferedImage[] imgs = new BufferedImage[imageList.size()];
        imageList.toArray(imgs);
        return imgs;
    }
	public static BufferedImage[] GetAllLevelsOld() {
		URL url = LoadSave.class.getResource("/lvls");
		File file = null;

		try {
			file = new File(url.toURI());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		File[] files = file.listFiles();
		File[] filesSorted = new File[files.length];

		for (int i = 0; i < filesSorted.length; i++)
			for (int j = 0; j < files.length; j++) {
				if (files[j].getName().equals((i + 1) + ".png"))
					filesSorted[i] = files[j];

			}

		BufferedImage[] imgs = new BufferedImage[filesSorted.length];

		for (int i = 0; i < imgs.length; i++)
			try {
				imgs[i] = ImageIO.read(filesSorted[i]);
			} catch (IOException e) {
				e.printStackTrace();
			}

		return imgs;
	}

}