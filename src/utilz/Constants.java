package utilz;

import main.Game;
import static utilz.HelpMethods.*;

import java.awt.Color;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
public class Constants {
	public static final int FPS_SET = 120;
	public static final int UPS_SET = 200;
	public static final float GRAVITY = 0.04f * Game.SCALE;
	public static final int ANI_SPEED = 13;//25;

	public static class Dialogue {
		public static final int QUESTION = 0;
		public static final int EXCLAMATION = 1;

		public static final int DIALOGUE_WIDTH = (int) (14 * Game.SCALE);
		public static final int DIALOGUE_HEIGHT = (int) (12 * Game.SCALE);

		public static int GetSpriteAmount(int type) {
			switch (type) {
			case QUESTION, EXCLAMATION:
				return 5;
			}

			return 0;
		}
	}

	public static class Projectiles {
		public static final int CANNON_BALL_DEFAULT_WIDTH = 15;
		public static final int CANNON_BALL_DEFAULT_HEIGHT = 15;

		public static final int CANNON_BALL_WIDTH = (int) (Game.SCALE * CANNON_BALL_DEFAULT_WIDTH);
		public static final int CANNON_BALL_HEIGHT = (int) (Game.SCALE * CANNON_BALL_DEFAULT_HEIGHT);
		public static final float SPEED = 0.75f * Game.SCALE;
	}

	public static class ObjectConstants {

		public static final int RED_POTION = 0;
		public static final int BLUE_POTION = 1;
		public static final int BARREL = 2;
		public static final int BOX = 3;
		public static final int SPIKE = 4;
		public static final int CANNON_LEFT = 5;
		public static final int CANNON_RIGHT = 6;
		public static final int TREE_ONE = 7;
		public static final int TREE_TWO = 8;
		public static final int TREE_THREE = 9;

		public static final int RED_POTION_VALUE = 15;
		public static final int BLUE_POTION_VALUE = 10;

		public static final int CONTAINER_WIDTH_DEFAULT = 40;
		public static final int CONTAINER_HEIGHT_DEFAULT = 30;
		public static final int CONTAINER_WIDTH = (int) (Game.SCALE * CONTAINER_WIDTH_DEFAULT);
		public static final int CONTAINER_HEIGHT = (int) (Game.SCALE * CONTAINER_HEIGHT_DEFAULT);

		public static final int POTION_WIDTH_DEFAULT = 12;
		public static final int POTION_HEIGHT_DEFAULT = 16;
		public static final int POTION_WIDTH = (int) (Game.SCALE * POTION_WIDTH_DEFAULT);
		public static final int POTION_HEIGHT = (int) (Game.SCALE * POTION_HEIGHT_DEFAULT);

		public static final int SPIKE_WIDTH_DEFAULT = 32;
		public static final int SPIKE_HEIGHT_DEFAULT = 32;
		public static final int SPIKE_WIDTH = (int) (Game.SCALE * SPIKE_WIDTH_DEFAULT);
		public static final int SPIKE_HEIGHT = (int) (Game.SCALE * SPIKE_HEIGHT_DEFAULT);

		public static final int CANNON_WIDTH_DEFAULT = 40;
		public static final int CANNON_HEIGHT_DEFAULT = 26;
		public static final int CANNON_WIDTH = (int) (CANNON_WIDTH_DEFAULT * Game.SCALE);
		public static final int CANNON_HEIGHT = (int) (CANNON_HEIGHT_DEFAULT * Game.SCALE);

		public static int GetSpriteAmount(int object_type) {
			switch (object_type) {
			case RED_POTION, BLUE_POTION:
				return 7;
			case BARREL, BOX:
				return 8;
			case CANNON_LEFT, CANNON_RIGHT:
				return 7;
			}
			return 1;
		}

		public static int GetTreeOffsetX(int treeType) {
			switch (treeType) {
			case TREE_ONE:
				return (Game.TILES_SIZE / 2) - (GetTreeWidth(treeType) / 2);
			case TREE_TWO:
				return (int) (Game.TILES_SIZE / 2.5f);
			case TREE_THREE:
				return (int) (Game.TILES_SIZE / 1.65f);
			}

			return 0;
		}

		public static int GetTreeOffsetY(int treeType) {

			switch (treeType) {
			case TREE_ONE:
				return -GetTreeHeight(treeType) + Game.TILES_SIZE * 2;
			case TREE_TWO, TREE_THREE:
				return -GetTreeHeight(treeType) + (int) (Game.TILES_SIZE / 1.25f);
			}
			return 0;

		}

		public static int GetTreeWidth(int treeType) {
			switch (treeType) {
			case TREE_ONE:
				return (int) (39 * Game.SCALE);
			case TREE_TWO:
				return (int) (62 * Game.SCALE);
			case TREE_THREE:
				return -(int) (62 * Game.SCALE);

			}
			return 0;
		}

		public static int GetTreeHeight(int treeType) {
			switch (treeType) {
			case TREE_ONE:
				return (int) (int) (92 * Game.SCALE);
			case TREE_TWO, TREE_THREE:
				return (int) (54 * Game.SCALE);

			}
			return 0;
		}
	}

	public static class EnemyConstants {
		public static final int TUMBLE_WEED = 0;
		
		public static final int IDLE = 0;
		public static final int RUNNING = 1;
		public static final int HIT = 2;
		public static final int DEAD = 3;
		public static final int NUM_ENEMY_STATES = 4;

		public static final int TUMBLE_WEED_WIDTH_DEFAULT = 30;
		public static final int TUMBLE_WEED_HEIGHT_DEFAULT = 30;
		public static final int TUMBLE_WEED_WIDTH = (int) (TUMBLE_WEED_WIDTH_DEFAULT * Game.SCALE);
		public static final int TUMBLE_WEED_HEIGHT = (int) (TUMBLE_WEED_HEIGHT_DEFAULT * Game.SCALE);
		public static final int TUMBLE_WEED_HITBOX_WIDTH_DEFAULT = 24;
		public static final int TUMBLE_WEED_HITBOX_HEIGHT_DEFAULT = 24;
		public static final int TUMBLE_WEED_HITBOX_WIDTH = (int) (TUMBLE_WEED_HITBOX_WIDTH_DEFAULT * Game.SCALE);
		public static final int TUMBLE_WEED_HITBOX_HEIGHT = (int) (TUMBLE_WEED_HITBOX_HEIGHT_DEFAULT * Game.SCALE);
		
		public static final int TUMBLE_WEED_DRAWOFFSET_X = (int) ((TUMBLE_WEED_WIDTH-TUMBLE_WEED_HITBOX_WIDTH)/2);
		public static final int TUMBLE_WEED_DRAWOFFSET_Y = (int) ((TUMBLE_WEED_HEIGHT-TUMBLE_WEED_HITBOX_HEIGHT)/2);
		public static final int TUMBLE_WEED_NUM_ANIMATIONS = 5;
		public static final int TUMBLE_WEED_MAX_ANIMATION_LENGTH = 10;
		public static final float TUMBLE_WEED_MAX_SPEED = 1.0f*Game.SCALE;
		public static final float TUMBLE_WEED_TIME_TO_REACH_WIND_SPEED = 5.0f;
		public static final int TUMBLE_WEED_MAX_ANI_SPEED = (int) (0.5*ANI_SPEED);
		public static final int TUMBLE_WEED_MIN_ANI_SPEED = (int) (3*ANI_SPEED);

		public static int GetSpriteAmount(int enemy_type, int enemy_state) {
			switch (enemy_state) {

			case IDLE: {
				if (enemy_type == TUMBLE_WEED)
					return 1;
			}
			case RUNNING:
				return 10;
			case HIT:
				return 10;
			case DEAD:
				return 4;
			}

			return 0;

		}

		public static int GetMaxHealth(int enemy_type) {
			switch (enemy_type) {
			case TUMBLE_WEED:
				return 20;
			default:
				return 1;
			}
		}

		public static int GetEnemyDmg(int enemy_type) {
			switch (enemy_type) {
			case TUMBLE_WEED:
				return 10;
			default:
				return 0;
			}
		}
	}

	public static class Environment {
		// temperature
		public static float MAX_TEMP = 100;
		public static float TIME_TO_REACH_MAX_TEMP = 10 * 60;
		
		// wind
		public static float WEAK_WIND_TH = 0.25f*Game.SCALE;
		public static float STRONG_WIND_TH = 0.75f*Game.SCALE;
		public static float MAX_WIND_SPEED_START = 0.5f*Game.SCALE;
		public static float MAX_WIND_SPEED_END = 1.5f*Game.SCALE;
		public static float TIME_BETWEEN_WIND_CHANGE_START = 5;
		public static float TIME_BETWEEN_WIND_CHANGE_END = 10;
		
		public static final Color FLOOR_TILE_COLOR = new Color(40, 40, 45, 150);
		
		// darkness
		public static final float DARKNESS_START_ALPHA = 0f;//-0.15f;
		public static final float DARKNESS_END_ALPHA = 150f;//0.10f;
		public static final float DARKNESS_CHANGE_SPEED = 0.05f*Game.SCALE;
		
		// clouds
		public static final float CLOUD_START_OFFSET_FACTOR = 0.05f;//-0.15f;
		public static final float CLOUD_END_OFFSET_FACTOR = 0.05f;//0.10f;
		public static final float CLOUD_MOVE_SPEED = 0.05f*Game.SCALE;
		
		// water
		public static final float WATER_START_OFFSET_FACTOR = 1.0f;
		public static final float WATER_END_OFFSET_FACTOR = 0.8f;
		public static final float WATER_MOVE_SPEED = 0.05f*Game.SCALE;
		public static final float WATER_DMG_PER_SECOND = 10.0f;
		public static final float WATER_PLAYER_SLOW_FACTOR = 0.5f;
		public static final float WATER_PLAYER_JUMP_SLOW_FACTOR = 0.75f;
		
		// layer speed 
		public static final float SKY_SPEED = 0.5f;
		public static final float BG1_SPEED = 0.6f;
		public static final float C1_SPEED = 0.7f;
		public static final float C2_SPEED = 0.8f;
		public static final float BG2_SPEED = 0.9f;
	}

	public static class UI {
		public static class Buttons {
			public static final int B_WIDTH_DEFAULT = 768;
			public static final int B_HEIGHT_DEFAULT = 256;
			public static final int B_WIDTH = (int) (180 * Game.SCALE);
			public static final int B_HEIGHT = (int) (60 * Game.SCALE);
		}

		public static class PauseButtons {
			public static final int SOUND_SIZE_DEFAULT = 42;
			public static final int SOUND_SIZE = (int) (SOUND_SIZE_DEFAULT * Game.SCALE);
		}

		public static class URMButtons {
			public static final int URM_DEFAULT_SIZE = 56;
			public static final int URM_SIZE = (int) (URM_DEFAULT_SIZE * Game.SCALE);

		}

		public static class VolumeButtons {
			public static final int VOLUME_DEFAULT_WIDTH = 28;
			public static final int VOLUME_DEFAULT_HEIGHT = 44;
			public static final int SLIDER_DEFAULT_WIDTH = 215;

			public static final int VOLUME_WIDTH = (int) (VOLUME_DEFAULT_WIDTH * Game.SCALE);
			public static final int VOLUME_HEIGHT = (int) (VOLUME_DEFAULT_HEIGHT * Game.SCALE);
			public static final int SLIDER_WIDTH = (int) (SLIDER_DEFAULT_WIDTH * Game.SCALE);
		}
	}

	public static class Directions {
		public static final int LEFT = 0;
		public static final int UP = 1;
		public static final int RIGHT = 2;
		public static final int DOWN = 3;
	}

	public static class PlayerConstants {
		public static final float PLAYER_WALKSPEED = Game.SCALE * 1.0f;
		public static final float PLAYER_JUMP_SPEED = -2.25f * Game.SCALE;
		public static final float TIME_TO_JUMP_WHEN_ALREADY_IN_AIR = 0.25f;
		
		
		public static final float CLOSE_TO_BORDER_HORIZONTAL = 0.6f;
		public static final float CLOSE_TO_BORDER_VERTICAL = 0.6f;
		public static final float MAX_X_LVL_OFFSET_STEP_HORIZONTAL = 0.002f*Game.GAME_WIDTH;
		public static final float MAX_X_LVL_OFFSET_STEP_VERTICAL = 0.02f*Game.GAME_HEIGHT;
		
		public static final int PLAYER_GREEN_VALUE = 100;
		public static final int HITBOX_BASE_WIDTH = 16;
		public static final int GRABBOX_BASE_WIDTH = (int) (HITBOX_BASE_WIDTH * 1.75f);
		public static final int HITBOX_BASE_HEIGHT = 27;
		public static final int GRABBOX_BASE_HEIGHT = (int) (HITBOX_BASE_HEIGHT * 1.25f);
		
		public static final int PLAYER_BASE_HEIGHT = 64;
		public static final int PLAYER_BASE_WIDTH = 64;
		
		public static final int IDLE = 0;
		public static final int RUNNING = 1;
		public static final int JUMP = 2;
		public static final int FALLING = 3;
		public static final int ATTACK = 4;
		public static final int HIT = 5;
		public static final int DEAD = 6;
		public static final int THROW = 7;
		
		public static final int NUM_ANIMATIONS = 8;
		public static final int MAX_ANIMATION_LENGTH = 12;
		
		public static int GetSpriteAmount(int player_action) {
			switch (player_action) {
			case DEAD:
				return 4;
			case RUNNING:
				return 12;
			case IDLE:
				return 1;
			case HIT:
				return 6;
			case JUMP:
				return 5;
			case ATTACK:
				return 5;
			case FALLING:
				return 4;
			case THROW:
				return 4;
			default:
				return 1;
			}
		}
		
		public static final Color PLAYER_DEFAULT_COLOR = new Color(21, 19, 26); // BLACK
		public static final int PLAYER_COLOR_TOLERANCE = 8; // BLACK
		
		public static final Map<Color, Color> COLOR_MAP;
	    static {
	        Map<Color, Color> map = new HashMap<>();
	        map.put(new Color(233, 38, 28), new Color(252, 191, 43)); // A
	        map.put(new Color(171, 40, 28), new Color(215, 150, 33)); // B
	        map.put(new Color(97, 39, 24), new Color(153, 104, 20)); // C
	        map.put(new Color(225, 85, 39), new Color(248, 214, 126)); // D
	        map.put(new Color(22, 22, 28), new Color(22, 22, 28)); // BLACK
	        map.put(new Color(14, 0, 12), new Color(14, 0, 12)); // BLACK

	        COLOR_MAP = Collections.unmodifiableMap(map);
	    }
	}
	
	public static class TetrisTileConstants {
		public static final int TETRIS_TILE_GREEN_VALUE = 255;
		
		public static final int TETRIS_TILE_WIDTH_DEFAULT = 32;
		public static final int TETRIS_TILE_HEIGHT_DEFAULT = 32;
		public static final int TETRIS_TILE_WIDTH = (int) (TETRIS_TILE_WIDTH_DEFAULT * Game.SCALE);
		public static final int TETRIS_TILE_HEIGHT = (int) (TETRIS_TILE_HEIGHT_DEFAULT * Game.SCALE);
		public static final int NUM_TETRIS_TILES = 15;
		
		public static final int T_TILE = 0;
		public static final int L_TILE = 1;
		public static final int J_TILE = 2;
		public static final int Z_TILE = 3;
		public static final int S_TILE = 4;
		public static final int O_TILE = 5;
		public static final int Q_TILE = 6;
		public static final int CROSS_TILE = 7;
		public static final int LONG_T_TILE = 8;
		public static final int LONG_Z_TILE = 9;
		public static final int LONG_S_TILE = 10;
		public static final int I_TILE = 11;
		public static final int LONG_I_TILE = 12;
		public static final int SINGLE_TILE = 13;
		public static final int DOUBLE_TILE = 14;
		
		public static final Color THROW_ARC_COLOR = new Color(100,100,100);
		public static final float TETRIS_TILE_MAX_THROW_HEIGHT = 32*Game.SCALE*3.0f;
		public static final float TETRIS_TILE_MAX_THROW_SPEED = (float) Math.sqrt(TETRIS_TILE_MAX_THROW_HEIGHT*2*GRAVITY);
		public static final float TETRIS_TILE_TIME_FOR_MAX_THROW_SPEED = 3.0f;
		public static final float THROW_ANGLE_STEP = 5.0f;
		public static final float MAX_THROW_ANGLE = 60.0f;
				
		public static final float TETRIS_TILE_MIN_EXPLOSION_X_SPEED = -TETRIS_TILE_MAX_THROW_SPEED*0.5f;
		public static final float TETRIS_TILE_MAX_EXPLOSION_X_SPEED = TETRIS_TILE_MAX_THROW_SPEED*0.5f;
		public static final float TETRIS_TILE_MIN_EXPLOSION_Y_SPEED = TETRIS_TILE_MAX_THROW_SPEED*0.75f;
		public static final float TETRIS_TILE_MAX_EXPLOSION_Y_SPEED = TETRIS_TILE_MAX_THROW_SPEED*1.5f;
		public static final float TETRIS_TILE_TIME_TO_EXPLODE = 1f;
		
		public static final float TETRIS_TILE_TIME_TO_REACH_WINDSPEED = 1.0f;
		public static final float TETRIS_TILE_TIME_TO_STOP_WHEN_IS_ON_FLOOR = 0.2f;
		public static final int TETRIS_GRID_SIZE = Game.TILES_SIZE/4;
		
		public static final int[][] ROCKET_GOAL_MATRIX = new int[][] {
			{0,0,0,0, 0,0,0,0, 0,0,0,0},
			{0,0,0,0, 0,1,0,0, 0,0,0,0},
			{0,0,0,0, 0,1,0,0, 0,0,0,0},
			{0,0,0,0, 0,1,0,0, 0,0,0,0},
			
			{0,0,0,0, 1,1,1,0, 0,0,0,0},
			{0,0,0,0, 1,1,1,0, 0,0,0,0},
			{0,0,0,0, 1,1,1,0, 0,0,0,0},
			{0,0,0,0, 1,1,1,0, 0,0,0,0},
			
			{0,0,0,0, 1,1,1,0, 0,0,0,0},
			{0,0,0,1, 1,1,1,1, 0,0,0,0},
			{0,0,0,1, 1,1,1,1, 0,0,0,0},
			{0,0,0,1, 1,1,1,1, 0,0,0,0},
			
			{0,0,0,1, 1,1,1,1, 0,0,0,0},
			{0,0,0,1, 1,1,1,1, 0,0,0,0},
			{0,0,0,1, 1,1,1,1, 0,0,0,0},
			{0,0,0,1, 1,1,1,1, 0,0,0,0},
			
			{0,0,0,1, 1,1,1,1, 0,0,0,0},
			{0,0,0,1, 1,1,1,1, 0,0,0,0},
			{0,0,0,1, 0,1,0,1, 0,0,0,0},
			{0,0,0,1, 0,1,0,1, 0,0,0,0},
			
			{0,0,0,0, 0,0,0,0, 0,0,0,0},
			{0,0,0,0, 0,0,0,0, 0,0,0,0},
			{0,0,0,0, 0,0,0,0, 0,0,0,0},
			{0,0,0,0, 0,0,0,0, 0,0,0,0},
		};
		
		public static final int[][] ROCKET_TUTORIAL_PRE_MATRIX = new int[][] {
			{0,0,0,0, 0,0,0,0, 0,0,0,0},
			{0,0,0,0, 0,1,0,0, 0,0,0,0},
			{0,0,0,0, 0,1,0,0, 0,0,0,0},
			{0,0,0,0, 0,1,0,0, 0,0,0,0},
			
			{0,0,0,0, 1,1,1,0, 0,0,0,0},
			{0,0,0,0, 1,1,1,0, 0,0,0,0},
			{0,0,0,0, 1,1,1,0, 0,0,0,0},
			{0,0,0,0, 1,1,1,0, 0,0,0,0},
			
			{0,0,0,0, 1,1,1,0, 0,0,0,0},
			{0,0,0,1, 1,1,1,1, 0,0,0,0},
			{0,0,0,1, 1,1,1,1, 0,0,0,0},
			{0,0,0,1, 1,1,1,1, 0,0,0,0},
			
			{0,0,0,1, 1,0,1,1, 0,0,0,0},
			{0,0,0,1, 0,0,0,1, 0,0,0,0},
			{0,0,0,1, 1,1,1,1, 0,0,0,0},
			{0,0,0,1, 1,1,1,1, 0,0,0,0},
			
			{0,0,0,1, 1,1,1,1, 0,0,0,0},
			{0,0,0,1, 1,1,1,1, 0,0,0,0},
			{0,0,0,1, 0,1,0,1, 0,0,0,0},
			{0,0,0,1, 0,1,0,1, 0,0,0,0},
			
			{1,1,1,1, 1,1,1,1, 1,1,1,1},
			{1,1,1,1, 1,1,1,1, 1,1,1,1},
			{1,1,1,1, 1,1,1,1, 1,1,1,1},
			{1,1,1,1, 1,1,1,1, 1,1,1,1},
		};
		
		public static final int[][] ROCKET_PRE_MATRIX = new int[][] {
			{0,0,0,0, 0,0,0,0, 0,0,0,0},
			{0,0,0,0, 0,0,0,0, 0,0,0,0},
			{0,0,0,0, 0,0,0,0, 0,0,0,0},
			{0,0,0,0, 0,0,0,0, 0,0,0,0},
			
			{0,0,0,0, 0,0,0,0, 0,0,0,0},
			{0,0,0,0, 0,0,0,0, 0,0,0,0},
			{0,0,0,0, 0,0,0,0, 0,0,0,0},
			{0,0,0,0, 0,0,0,0, 0,0,0,0},
			
			{0,0,0,0, 0,0,0,0, 0,0,0,0},
			{0,0,0,0, 0,0,0,0, 0,0,0,0},
			{0,0,0,0, 0,0,0,0, 0,0,0,0},
			{0,0,0,0, 0,0,0,0, 0,0,0,0},
			
			{0,0,0,0, 0,0,0,0, 0,0,0,0},
			{0,0,0,0, 0,0,0,0, 0,0,0,0},
			{0,0,0,0, 0,0,0,0, 0,0,0,0},
			{0,0,0,0, 0,0,0,0, 0,0,0,0},
			
			{0,0,0,0, 0,0,0,0, 0,0,0,0},
			{0,0,0,0, 0,0,0,0, 0,0,0,0},
			{0,0,0,0, 0,0,0,0, 0,0,0,0},
			{0,0,0,0, 0,0,0,0, 0,0,0,0},
			
			{1,1,1,1, 1,1,1,1, 1,1,1,1},
			{1,1,1,1, 1,1,1,1, 1,1,1,1},
			{1,1,1,1, 1,1,1,1, 1,1,1,1},
			{1,1,1,1, 1,1,1,1, 1,1,1,1},
		};
		
		public static final int[][] WINDMILL_GOAL_MATRIX = new int[][] {
			{0,0,0,0, 0,0,0,0, 0,0,0,0},
			{0,0,0,0, 0,0,0,0, 0,0,0,0},
			{0,0,0,0, 0,0,0,0, 0,0,0,0},
			{0,0,0,0, 0,0,0,0, 0,0,0,0},
			
			{0,0,0,0, 0,0,0,0, 0,0,0,0},
			{0,0,0,0, 0,0,0,0, 0,0,0,0},
			{0,0,0,0, 0,0,0,0, 0,0,0,0},
			{0,0,0,0, 0,1,0,0, 0,0,0,0},
			
			{0,0,0,0, 0,1,0,0, 0,0,0,0},
			{0,0,0,0, 0,1,0,0, 0,0,0,0},
			{0,0,0,0, 1,1,1,0, 0,0,0,0},
			{0,0,0,0, 1,1,1,1, 0,0,0,0},
			
			{0,0,0,1, 1,1,0,1, 1,0,0,0},
			{0,0,1,1, 0,1,0,0, 1,1,0,0},
			{0,1,1,0, 0,1,0,0, 0,0,0,0},
			{0,0,0,0, 0,1,0,0, 0,0,0,0},
			
			{0,0,0,0, 0,1,0,0, 0,0,0,0},
			{0,0,0,0, 0,1,0,0, 0,0,0,0},
			{0,0,0,0, 0,1,0,0, 0,0,0,0},
			{0,0,0,0, 1,1,1,0, 0,0,0,0},
			
			{0,0,0,0, 0,0,0,0, 0,0,0,0},
			{0,0,0,0, 0,0,0,0, 0,0,0,0},
			{0,0,0,0, 0,0,0,0, 0,0,0,0},
			{0,0,0,0, 0,0,0,0, 0,0,0,0},
		};
		
		public static final int[][] WINDMILL_TUTORIAL_PRE_MATRIX = new int[][] {
			{0,0,0,0, 0,0,0,0, 0,0,0,0},
			{0,0,0,0, 0,0,0,0, 0,0,0,0},
			{0,0,0,0, 0,0,0,0, 0,0,0,0},
			{0,0,0,0, 0,0,0,0, 0,0,0,0},
			
			{0,0,0,0, 0,0,0,0, 0,0,0,0},
			{0,0,0,0, 0,0,0,0, 0,0,0,0},
			{0,0,0,0, 0,0,0,0, 0,0,0,0},
			{0,0,0,0, 0,0,0,0, 0,0,0,0},
			
			{0,0,0,0, 0,0,0,0, 0,0,0,0},
			{0,0,0,0, 0,0,0,0, 0,0,0,0},
			{0,0,0,0, 1,0,1,0, 0,0,0,0},
			{0,0,0,0, 1,1,1,1, 0,0,0,0},
			
			{0,0,0,1, 1,1,0,1, 1,0,0,0},
			{0,0,1,1, 0,1,0,0, 1,1,0,0},
			{0,1,1,0, 0,1,0,0, 1,1,0,0},
			{0,1,1,0, 0,1,0,0, 1,1,0,0},
		
			{0,1,1,0, 0,1,0,0, 1,1,0,0},
			{0,1,1,0, 0,1,0,0, 1,1,0,0},
			{0,1,1,0, 0,1,0,0, 1,1,0,0},
			{0,1,1,0, 1,1,1,0, 1,1,0,0},
			
			{1,1,1,1, 1,1,1,1, 1,1,1,1},
			{1,1,1,1, 1,1,1,1, 1,1,1,1},
			{1,1,1,1, 1,1,1,1, 1,1,1,1},
			{1,1,1,1, 1,1,1,1, 1,1,1,1},
		};
		
		public static final int[][] WINDMILL_PRE_MATRIX = new int[][] {
			{0,0,0,0, 0,0,0,0, 0,0,0,0},
			{0,0,0,0, 0,0,0,0, 0,0,0,0},
			{0,0,0,0, 0,0,0,0, 0,0,0,0},
			{0,0,0,0, 0,0,0,0, 0,0,0,0},
			
			{0,0,0,0, 0,0,0,0, 0,0,0,0},
			{0,0,0,0, 0,0,0,0, 0,0,0,0},
			{0,0,0,0, 0,0,0,0, 0,0,0,0},
			{0,0,0,0, 0,0,0,0, 0,0,0,0},
			
			{0,0,0,0, 0,0,0,0, 0,0,0,0},
			{0,0,0,0, 0,0,0,0, 0,0,0,0},
			{0,0,0,0, 0,0,0,0, 0,0,0,0},
			{0,0,0,0, 0,0,0,0, 0,0,0,0},
			
			{0,0,0,0, 0,0,0,0, 0,0,0,0},
			{0,0,0,0, 0,0,0,0, 0,0,0,0},
			{0,0,0,0, 0,0,0,0, 1,1,0,0},
			{0,1,1,0, 0,0,0,0, 1,1,0,0},
			
			{0,1,1,0, 0,0,0,0, 1,1,0,0},
			{0,1,1,0, 0,0,0,0, 1,1,0,0},
			{0,1,1,0, 0,0,0,0, 1,1,0,0},
			{0,1,1,0, 0,0,0,0, 1,1,0,0},
			
			{1,1,1,1, 1,1,1,1, 1,1,1,1},
			{1,1,1,1, 1,1,1,1, 1,1,1,1},
			{1,1,1,1, 1,1,1,1, 1,1,1,1},
			{1,1,1,1, 1,1,1,1, 1,1,1,1},
		};
		
		
		public static int[][] GetTetrisTileShape (int tileIndex, int rotation) {
			int[][] matrix;
			switch (tileIndex) {
			case T_TILE:
				matrix = new int[][] {{0,0,0,0}, 
									 {0,1,0,0},
									 {1,1,1,0},
									 {0,0,0,0}};
				break;		
			case L_TILE:
				matrix = new int[][] {{0,1,0,0}, 
									 {0,1,0,0},
									 {0,1,1,0},
									 {0,0,0,0}};
				break;		
			case J_TILE:
				matrix = new int[][] {{0,0,1,0}, 
									{0,0,1,0},
									{0,1,1,0},
									{0,0,0,0}};
				break;		
			case Z_TILE:
				matrix = new int[][] {{0,0,0,0}, 
									{1,1,0,0},
									{0,1,1,0},
									{0,0,0,0}};
				break;		
			case S_TILE:
				matrix = new int[][] {{0,0,0,0}, 
									{0,1,1,0},
									{1,1,0,0},
									{0,0,0,0}};
				break;		
			case O_TILE:
				matrix = new int[][] {{0,0,0,0}, 
									{0,1,1,0},
									{0,1,1,0},
									{0,0,0,0}};
				break;		
			case LONG_T_TILE:
				matrix = new int[][] {{0,1,0,0}, 
									{0,1,0,0},
									{1,1,1,0},
									{0,0,0,0}};
				break;	
			case CROSS_TILE:
				matrix = new int[][] {{0,1,0,0}, 
									{1,1,1,0},
									{0,1,0,0},
									{0,0,0,0}};
				break;	
			case Q_TILE:
				matrix = new int[][] {{0,0,0,0}, 
									{0,1,1,0},
									{1,1,1,0},
									{0,0,0,0}};
				break;	
			case LONG_Z_TILE:
				matrix = new int[][] {{0,0,0,0}, 
									{1,1,0,0},
									{0,1,1,1},
									{0,0,0,0}};
				break;	
			case LONG_S_TILE:
				matrix = new int[][] {{0,0,0,0}, 
									{0,1,1,1},
									{1,1,0,0},
									{0,0,0,0}};
				break;	
			case I_TILE:
				matrix = new int[][] {{0,0,0,0}, 
									{0,1,0,0},
									{0,1,0,0},
									{0,1,0,0}};
				break;
			case LONG_I_TILE:
				matrix = new int[][] {{0,1,0,0}, 
									{0,1,0,0},
									{0,1,0,0},
									{0,1,0,0}};
				break;
			case SINGLE_TILE:
				matrix = new int[][] {{0,0,0,0}, 
									{0,0,0,0},
									{0,1,0,0},
									{0,0,0,0}};
				break;
			case DOUBLE_TILE:
				matrix = new int[][] {{0,0,0,0}, 
									{0,0,0,0},
									{0,1,1,0},
									{0,0,0,0}};
				break;
			default:
				matrix = new int[][] {{1,1,1,1}, 
									{1,1,1,1},
									{1,1,1,1},
									{1,1,1,1}};
				break;
			}
			
			for (int i = 0; i < rotation; i++) {
				matrix = rotateMatrixBy90Degree(matrix);
			}
			return matrix;
		}
	}
	
	public static class ControllerConstants {
		public static final float JOYSTICK_DEAD_ZONE = 0.75f;
		public static final int CONTROLLER_B_BUTTON_ID = 0;
		public static final int CONTROLLER_A_BUTTON_ID = 1;
		public static final int CONTROLLER_X_BUTTON_ID = 2;
		public static final int CONTROLLER_Y_BUTTON_ID = 3;
		public static final int CONTROLLER_O_BUTTON_ID = 4;
		public static final int CONTROLLER_L_BUTTON_ID = 5;
		public static final int CONTROLLER_R_BUTTON_ID = 6;
		public static final int CONTROLLER_ZL_BUTTON_ID = 7;
		public static final int CONTROLLER__BUTTON_ID = 8;
		public static final int CONTROLLER_MINUS_BUTTON_ID = 9;
		public static final int CONTROLLER_PLUS_BUTTON_ID = 10;
		public static final int CONTROLLER_H_BUTTON_ID = 11;
		public static final int CONTROLLER_LJS_BUTTON_ID = 12;
		public static final int CONTROLLER_RJS_BUTTON_ID = 13;
		public static final int CONTROLLER_UP_BUTTON_ID = 14;
		public static final int CONTROLLER_RIGHT_BUTTON_ID = 15;
		public static final int CONTROLLER_DOWN_BUTTON_ID = 16;
		public static final int CONTROLLER_LEFT_BUTTON_ID = 17;
		
		
	}

}