package com.codingame.game.engine;

import java.util.HashMap;

public class Constants
{

  public static int PLAYERS = 4;

  //////////////////////////
  // GAME PARAMETERS TO TUNE
  //////////////////////////

  public static int HERO_MAX_HP = 20;
  public static int POTION_HEAL = 10;
  public static int TREASURE_SCORE = 100;
  public static int EXIT_SCORE = 10000;
  public static int ERROR_SCORE = -10000;
  public static int DIED_SCORE = -1000;

  public static Integer[] HERO_DMG = new Integer[]{10, 6, 7, 8};
  public static Integer[] HERO_INITCHARGES = new Integer[]{99999, 5, 5, 5};

  public static int HERO_FOV = 3;

  public static int MAZE_WIDTH = 16;
  public static int MAZE_HEIGHT = 12;


  public static int[][] MONSTER_STATS = {
          // fov, range, hp, dmg,  score?
          {  0,   0,     1,  0},
          {  1,   1,     6,  1}, // SKELETON
          {  2,   1,     14, 2}, // GARGOYLE
          {  2,   2,     8,  2}, // ORC
          {  3,   1,     10,  3},// VAMPIRE
  };


  ///////////////////////////////////////////////////
  // GAME CONSTANTS
  ///////////////////////////////////////////////////

  public static int DIR_LEFT = 0;
  public static int DIR_DOWN = 1;
  public static int DIR_UP = 2;
  public static int DIR_RIGHT = 3;
  public static int[] REV_DIR = {DIR_RIGHT, DIR_UP, DIR_DOWN, DIR_LEFT};

  public static int TYPE_FLOOR = -1;
  public static int TYPE_EXIT = 0;
  public static int TYPE_OBSTACLE = 1;
  public static int TYPE_TREASURE = 2;
  public static int TYPE_POTION = 3;
  public static int TYPE_CHARGES1 = 4;
  public static int TYPE_CHARGES2 = 5;
  public static int TYPE_CHARGES3 = 6;
  public static int TYPE_MONSTER0 = 7;
  public static int TYPE_MONSTER1 = 8;
  public static int TYPE_MONSTER2 = 9;
  public static int TYPE_MONSTER3 = 10;
  public static int TYPE_MONSTER4 = 11;
  public static int TYPE_HERO = 12;

  public static int WEAP_SWORD = 0;
  public static int WEAP_HAMMER = 1;
  public static int WEAP_SCYTHE = 2;
  public static int WEAP_BOW = 3;

  public static int CHARACTER_BOX = 0;
  public static int CHARACTER_SKELETON = 1;
  public static int CHARACTER_GARGOYLE = 2;
  public static int CHARACTER_ORC = 3;
  public static int CHARACTER_VAMPIRE = 4;
  public static int CHARACTER_HERO = 5;

  public static HashMap<String, Integer> MAZE_CHARS_TYPES = new HashMap<String, Integer>() {{
    put(".",  TYPE_FLOOR);
    put("H", TYPE_HERO);
    put("E",  TYPE_EXIT);
    put("X",  TYPE_OBSTACLE);
    put("t",  TYPE_TREASURE);
    put("p",  TYPE_POTION);
    put("a", TYPE_CHARGES1);
    put("b", TYPE_CHARGES2);
    put("c", TYPE_CHARGES3);
    put("0",  TYPE_MONSTER0);
    put("1",  TYPE_MONSTER1);
    put("2",  TYPE_MONSTER2);
    put("3",  TYPE_MONSTER3);
    put("4",  TYPE_MONSTER4);
  }};

  ///////////////////////////////////////////////////
  // GAME CONSTRAINTS, VISUALIZATION
  ///////////////////////////////////////////////////

  public static int TURNLIMIT_INITIAL = PLAYERS==4? 18 : 0;
  public static int PLAYER_TURNS_MAX = 150;
  public static int TIMELIMIT_INIT = 1000;
  public static int TIMELIMIT_TURN = 50;
  public static int FRAMEDURATION = PLAYERS==4? 500: 1000;
  public static int FRAMEDURATION_HERO = 500;
  public static double FRAMEDURATION_FRAC0 = PLAYERS==4? 0 : 0.5;
  public static double FRAMEDURATION_FRAC1 = PLAYERS==4? 1 : 0.5;
  public static double FRAMEDURATION_EPSILON = 0.0001;

  public static int CurrentTurn(int frame)
  {
    if (PLAYERS==4) return Math.max(0,(1+(frame - (TURNLIMIT_INITIAL)-1)/2));
    return (0+frame);
  }

  ///////////////////////////////////////////////////
  // STORY DATA
  ///////////////////////////////////////////////////

  public static String[][] MONSTER_DESCRIPTIONS = {
          {"Syntax Error", "Just sits there\nand prevents further testing"},
          {"Array out of Bounds", " If you can count, count on yourself"},
          {"Stack Overflow", "You will learn a lot"},
          {"Null Pointer Exception", "Just hits you from nowhere"},
          {"Memory Leak", "Silent, persistent, deadly.\nEventually you will notice."}};

  public static String[][] ITEM_DESCRIPTIONS = {
          {"Deadline", "Finally. End of the project\nand some time for well deserved rest."},
          {"void", "If you point at the void,\nthe void will point back at you"},
          {"Commits", "Proof of your engagement,\nthe more the better"},
          {"Mysterious Liquid", "Actually not healthy\nbut increases your productivity."},
          {"Linter", "Deals with multiple problems at once."},
          {"Unit Test ", "Nothing shall pass."},
          {"Breakpoint", "Great if you hunt down a specific problem."},
          {"Console Print", "So inefficient, so widely used..."},
  };

  public static String HERO_DESCRIPTION = "Lonely, tired, underpaid.";
  public static String HEALTH_DESCRIPTION = "Health\nKeep above 0.";

  //                                  0text    1header   2damage   3heal     4gold     5charge
  public static int[] TEXT_COLORS = {0x000000, 0xeb3110, 0xeb3110, 0x52c226, 0xe6af30, 0x2ea1ea};

}

