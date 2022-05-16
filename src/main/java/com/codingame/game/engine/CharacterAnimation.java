package com.codingame.game.engine;


import com.codingame.gameengine.module.entities.GraphicEntityModule;
import com.codingame.gameengine.module.entities.SpriteAnimation;

import java.util.Arrays;

import static com.codingame.game.engine.Constants.*;
import static com.codingame.game.engine.Viewer.*;

public class CharacterAnimation
{
  private static int CHAR_OFFSET_X = 0;
  private static int CHAR_OFFSET_Y = -10;
  private static double CHAR_SCALE = 0.387;

  private static int[][][] WEAP_OFFSET_X;
  private static int[][][] WEAP_OFFSET_Y;

  private static GraphicEntityModule graphic;
  private static Viewer viewer;

  public static String[][][] sitSprites = new String[6][3][]; // character -> dir -> string array
  public static String[][][] idleSprites = new String[6][3][]; // character -> dir -> string array
  public static String[][][] moveSprites = new String[6][3][]; // character -> dir -> string array
  public static String[][][] hurtSprites = new String[6][3][]; // character -> dir -> string array
  public static String[][] vanishSprites = new String[6][]; // character -> string array
  public static String[][][][] attackSpritesBot = new String[6][4][3][]; // character -> weapon -> dir -> string array
  public static String[][][][] attackSpritesTop = new String[6][4][3][]; // character -> weapon -> dir -> string array

  public static String[][][] weaponSprites = new String[4][3][]; // weapon -> dir -> string array

  public int mmid;

  private int chartype;
  private int py;
  public boolean changed = false;
  public SpriteAnimation layerTop;
  public SpriteAnimation layerMid;
  public SpriteAnimation layerBot;

  public static void initCharacterAnimations(GraphicEntityModule graphicmod, Viewer viewerobj)
  {
    graphic = graphicmod;
    viewer = viewerobj;

    initOffsets();

    initAnimationsBoxSitVanish();

    initAnimationsSitIdle(CHARACTER_SKELETON, "duck", sitSprites);
    initAnimationsSitIdle(CHARACTER_SKELETON, "idle", idleSprites);
    initAnimationsHurt(CHARACTER_SKELETON);
    initAnimationsVanish(CHARACTER_SKELETON);
    initAnimationsAttackSword(CHARACTER_SKELETON);

    initAnimationsSitIdle(CHARACTER_GARGOYLE, "duck", sitSprites);
    initAnimationsSitIdle(CHARACTER_GARGOYLE, "idle", idleSprites);
    initAnimationsMove(CHARACTER_GARGOYLE);
    initAnimationsHurt(CHARACTER_GARGOYLE);
    initAnimationsVanish(CHARACTER_GARGOYLE);
    initAnimationsAttackSword(CHARACTER_GARGOYLE);

    initAnimationsSitIdle(CHARACTER_ORC, "duck", sitSprites);
    initAnimationsSitIdle(CHARACTER_ORC, "idle", idleSprites);
    initAnimationsHurt(CHARACTER_ORC);
    initAnimationsVanish(CHARACTER_ORC);
    initAnimationsAttackBow(CHARACTER_ORC);

    initAnimationsSitIdle(CHARACTER_VAMPIRE, "duck", sitSprites);
    initAnimationsSitIdle(CHARACTER_VAMPIRE, "idle", idleSprites);
    initAnimationsMove(CHARACTER_VAMPIRE);
    initAnimationsHurt(CHARACTER_VAMPIRE);
    initAnimationsVanish(CHARACTER_VAMPIRE);
    initAnimationsAttackSword(CHARACTER_VAMPIRE);

    initAnimationsSitIdle(CHARACTER_HERO, "idle", idleSprites);
    initAnimationsMove(CHARACTER_HERO);
    initAnimationsHurt(CHARACTER_HERO);
    initAnimationsVanish(CHARACTER_HERO);
    initAnimationsAttackSword(CHARACTER_HERO);
    initAnimationsAttackHammer(CHARACTER_HERO);
    initAnimationsAttackScythe(CHARACTER_HERO);
    initAnimationsAttackBow(CHARACTER_HERO);

    initAnimationsWeapons();
  }

  public static void initOffsets()
  {
    if (CELL_SIZE==34)
    {
      CHAR_OFFSET_X = 0;
      CHAR_OFFSET_Y = -10;
      CHAR_SCALE = 0.387;
      WEAP_OFFSET_X = new int[][][] { // character -> weapon -> dir
              {null, null, null, null}, // ??
              {{2, -1, -2, 0}, null, null, null}, // SKELETON
              {{2, -1, -2, 0}, null, null, null}, // GARGOYLE
              {null, null, null, {4, 3, 0, 4}}, // ORC
              {{2, -1, -2, 0}, null, null, null}, // VAMPIRE
              {{2, -1, -2, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}} // HERO
      };
      WEAP_OFFSET_Y = new int[][][] { // character -> weapon -> dir
              {null, null, null, null}, // ??
              {{7, 7, 6, 6}, null, null, null}, // SKELETON
              {{7, 7, 6, 6}, null, null, null}, // GARGOYLE
              {null, null, null, {6, 2, 0, 4}}, // ORC
              {{7, 7, 6, 6 }, null, null, null}, // VAMPIRE
              {{7, 7, 6, 6}, {4, 4, 0, 4}, {4, 0, 4, 4}, {4, 3, 0, 4}} // HERO
      };
    }
    if (CELL_SIZE==72)
    {
      CHAR_OFFSET_X = 0;
      CHAR_OFFSET_Y = -20;
      CHAR_SCALE = 0.82;
      WEAP_OFFSET_X = new int[][][] { // character -> weapon -> dir
              {null, null, null, null}, // ??
              {{0, 0, 0, 0}, null, null, null}, // SKELETON
              {{0, 4, -2, 0}, null, null, null}, // GARGOYLE
              {null, null, null, {6, 0, 0, 0}}, // ORC
              {{6, -1, -2, 4}, null, null, null}, // VAMPIRE
              {{0, -0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}} // HERO
      };
      WEAP_OFFSET_Y = new int[][][] { // character -> weapon -> dir
              {null, null, null, null}, // ??
              {{12, 6, 14, 12}, null, null, null}, // SKELETON
              {{12, 10, 14, 12}, null, null, null}, // GARGOYLE
              {null, null, null, {12, 0, 0, 0}}, // ORC
              {{12, 10, 14, 12}, null, null, null}, // VAMPIRE
              {{12, 6, 14, 12}, {11, 10, 10, 11}, {11, 10, 10, 9}, {0, 11, 8, 11}} // HERO
      };
    }
  }

  private static void initAnimationsWeapons()
  {
    String[] sprites;
    sprites = graphic.createSpriteSheetSplitter()
            .setSourceImage("wo.png")
            .setImageCount(36)
            .setWidth(224)
            .setHeight(216)
            .setOrigRow(0)
            .setOrigCol(0)
            .setImagesPerRow(9)
            .setName("wo")
            .split();
    weaponSprites[WEAP_SWORD][DIR_LEFT] = Arrays.copyOfRange(sprites, 0, 12);
    weaponSprites[WEAP_SWORD][DIR_DOWN] = Arrays.copyOfRange(sprites, 12, 24);
    weaponSprites[WEAP_SWORD][DIR_UP] = Arrays.copyOfRange(sprites, 24, 36);
    sprites = graphic.createSpriteSheetSplitter()
            .setSourceImage("wm.png")
            .setImageCount(69)
            .setWidth(224)
            .setHeight(216)
            .setOrigRow(0)
            .setOrigCol(0)
            .setImagesPerRow(9)
            .setName("wm")
            .split();
    weaponSprites[WEAP_HAMMER][DIR_LEFT] = Arrays.copyOfRange(sprites, 0, 23);
    weaponSprites[WEAP_HAMMER][DIR_DOWN] = Arrays.copyOfRange(sprites, 23, 46);
    weaponSprites[WEAP_HAMMER][DIR_UP] = Arrays.copyOfRange(sprites, 46, 69);
    sprites = graphic.createSpriteSheetSplitter()
            .setSourceImage("ws.png")
            .setImageCount(42)
            .setWidth(224)
            .setHeight(216)
            .setOrigRow(0)
            .setOrigCol(0)
            .setImagesPerRow(9)
            .setName("ws")
            .split();
    weaponSprites[WEAP_SCYTHE][DIR_LEFT] = Arrays.copyOfRange(sprites, 0, 14);
    weaponSprites[WEAP_SCYTHE][DIR_DOWN] = Arrays.copyOfRange(sprites, 14, 28);
    weaponSprites[WEAP_SCYTHE][DIR_UP] = Arrays.copyOfRange(sprites, 28, 42);
    sprites = graphic.createSpriteSheetSplitter()
            .setSourceImage("wb.png")
            .setImageCount(21)
            .setWidth(224)
            .setHeight(216)
            .setOrigRow(0)
            .setOrigCol(0)
            .setImagesPerRow(9)
            .setName("wb")
            .split();
    weaponSprites[WEAP_BOW][DIR_LEFT] = Arrays.copyOfRange(sprites, 0, 7);
    weaponSprites[WEAP_BOW][DIR_DOWN] = Arrays.copyOfRange(sprites, 7, 14);
    weaponSprites[WEAP_BOW][DIR_UP] = Arrays.copyOfRange(sprites, 14, 21);
  }

  private static void initAnimationsBoxSitVanish()
  {
    String[] sprites = graphic.createSpriteSheetSplitter().setSourceImage("c0.png").setImageCount(3).setWidth(128).setHeight(128).setOrigRow(0).setOrigCol(0).setImagesPerRow(2).setName("c0").split();
    sitSprites[CHARACTER_BOX][DIR_DOWN] = new String[] {sprites[0]};
    sitSprites[CHARACTER_BOX][DIR_LEFT] = new String[] {sprites[0]};
    sitSprites[CHARACTER_BOX][DIR_UP] = new String[] {sprites[0]};
    vanishSprites[CHARACTER_BOX] = sprites;
  }

  private static void initAnimationsSitIdle(int type, String behavior, String[][][] container)
  {
    String[] sprites = graphic.createSpriteSheetSplitter()
            .setSourceImage("c"+type+behavior.charAt(0)+".png")
            .setImageCount(63)
            .setWidth(180)
            .setHeight(150)
            .setOrigRow(0)
            .setOrigCol(0)
            .setImagesPerRow(11)
            .setName("c"+type+behavior.charAt(0))
            .split();
    container[type][DIR_DOWN] = Arrays.copyOfRange(sprites, 0, 21);
    container[type][DIR_LEFT] = Arrays.copyOfRange(sprites, 21, 42);
    container[type][DIR_UP] = Arrays.copyOfRange(sprites, 42, 63);
  }

  private static void initAnimationsMove(int type)
  {
    String[] sprites = graphic.createSpriteSheetSplitter()
            .setSourceImage("c"+type+"w.png")
            .setImageCount(42)
            .setWidth(180)
            .setHeight(150)
            .setOrigRow(0)
            .setOrigCol(0)
            .setImagesPerRow(11)
            .setName("c"+type+"w")
            .split();
    moveSprites[type][DIR_DOWN] = Arrays.copyOfRange(sprites, 0, 14);
    moveSprites[type][DIR_LEFT] = Arrays.copyOfRange(sprites, 14, 28);
    moveSprites[type][DIR_UP] = Arrays.copyOfRange(sprites, 28, 42);
  }

  private static void initAnimationsHurt(int type)
  {
    String[] sprites = graphic.createSpriteSheetSplitter()
            .setSourceImage("c"+type+"h.png")
            .setImageCount(39)
            .setWidth(180)
            .setHeight(150)
            .setOrigRow(0)
            .setOrigCol(0)
            .setImagesPerRow(11)
            .setName("c"+type+"h")
            .split();
    hurtSprites[type][DIR_DOWN] = Arrays.copyOfRange(sprites, 0, 13);
    hurtSprites[type][DIR_LEFT] = Arrays.copyOfRange(sprites, 13, 26);
    hurtSprites[type][DIR_UP] = Arrays.copyOfRange(sprites, 26, 39);
  }

  private static void initAnimationsVanish(int type)
  {
    String[] sprites = graphic.createSpriteSheetSplitter()
            .setSourceImage("c"+type+"v.png")
            .setImageCount(33)
            .setWidth(180)
            .setHeight(150)
            .setOrigRow(0)
            .setOrigCol(0)
            .setImagesPerRow(11)
            .setName("c"+type+"v")
            .split();
    vanishSprites[type] = Arrays.copyOfRange(sprites, 0, 33);
  }

  private static void initAnimationsAttackSword(int type)
  {
    String[] sprites = graphic.createSpriteSheetSplitter()
            .setSourceImage("c"+type+"o.png")
            .setImageCount(60)
            .setWidth(180)
            .setHeight(150)
            .setOrigRow(0)
            .setOrigCol(0)
            .setImagesPerRow(11)
            .setName("c"+type+"o")
            .split();
    attackSpritesBot[type][WEAP_SWORD][DIR_LEFT] = Arrays.copyOfRange(sprites, 0, 12);
    attackSpritesTop[type][WEAP_SWORD][DIR_LEFT] = Arrays.copyOfRange(sprites, 12, 24);
    attackSpritesBot[type][WEAP_SWORD][DIR_DOWN] = Arrays.copyOfRange(sprites, 24, 36);
    attackSpritesTop[type][WEAP_SWORD][DIR_DOWN] = Arrays.copyOfRange(sprites, 36, 48);
    attackSpritesTop[type][WEAP_SWORD][DIR_UP]   = Arrays.copyOfRange(sprites, 48, 60);
  }

  private static void initAnimationsAttackHammer(int type)
  {
    String[] sprites = graphic.createSpriteSheetSplitter()
            .setSourceImage("c"+type+"m.png")
            .setImageCount(115)
            .setWidth(180)
            .setHeight(150)
            .setOrigRow(0)
            .setOrigCol(0)
            .setImagesPerRow(11)
            .setName("c"+type+"m")
            .split();
    attackSpritesBot[type][WEAP_HAMMER][DIR_LEFT] = Arrays.copyOfRange(sprites, 0, 23);
    attackSpritesTop[type][WEAP_HAMMER][DIR_LEFT] = Arrays.copyOfRange(sprites, 23, 46);
    attackSpritesBot[type][WEAP_HAMMER][DIR_DOWN] = Arrays.copyOfRange(sprites, 46, 69);
    attackSpritesTop[type][WEAP_HAMMER][DIR_DOWN] = Arrays.copyOfRange(sprites, 69, 92);
    attackSpritesTop[type][WEAP_HAMMER][DIR_UP]   = Arrays.copyOfRange(sprites, 92, 115);
  }

  private static void initAnimationsAttackScythe(int type)
  {
    String[] sprites = graphic.createSpriteSheetSplitter()
            .setSourceImage("c"+type+"s.png")
            .setImageCount(70)
            .setWidth(180)
            .setHeight(150)
            .setOrigRow(0)
            .setOrigCol(0)
            .setImagesPerRow(11)
            .setName("c"+type+"s")
            .split();
    attackSpritesBot[type][WEAP_SCYTHE][DIR_LEFT] = Arrays.copyOfRange(sprites, 0, 14);
    attackSpritesTop[type][WEAP_SCYTHE][DIR_LEFT] = Arrays.copyOfRange(sprites, 14, 28);
    attackSpritesBot[type][WEAP_SCYTHE][DIR_DOWN] = Arrays.copyOfRange(sprites, 28, 42);
    attackSpritesTop[type][WEAP_SCYTHE][DIR_DOWN] = Arrays.copyOfRange(sprites, 42, 56);
    attackSpritesTop[type][WEAP_SCYTHE][DIR_UP]   = Arrays.copyOfRange(sprites, 56, 70);
  }

  private static void initAnimationsAttackBow(int type)
  {
    String[] sprites = graphic.createSpriteSheetSplitter()
            .setSourceImage("c"+type+"b.png")
            .setImageCount(5)
            .setWidth(180)
            .setHeight(150)
            .setOrigRow(0)
            .setOrigCol(0)
            .setImagesPerRow(5)
            .setName("c"+type+"b")
            .split();
    attackSpritesBot[type][WEAP_BOW][DIR_LEFT] = Arrays.copyOfRange(sprites, 0, 1);
    attackSpritesTop[type][WEAP_BOW][DIR_LEFT] = Arrays.copyOfRange(sprites, 1, 2);
    attackSpritesBot[type][WEAP_BOW][DIR_DOWN] = Arrays.copyOfRange(sprites, 2, 3);
    attackSpritesTop[type][WEAP_BOW][DIR_DOWN] = Arrays.copyOfRange(sprites, 3, 4);
    attackSpritesTop[type][WEAP_BOW][DIR_UP]   = Arrays.copyOfRange(sprites, 4, 5);
  }

  public CharacterAnimation(int chartype, int x, int y, int mmid)
  {
    this.chartype = chartype;
    this.mmid = mmid;
    if (chartype==CHARACTER_HERO) preinitLayers(x, y);
  }

  public void preinitLayers(int x, int y)
  {
    layerBot = graphic.createSpriteAnimation().setLoop(true).setPlaying(true).setDuration(PLAYERS==4?FRAMEDURATION:FRAMEDURATION_HERO).setZIndex(Z_CHAR_Y0+y*6);
    layerMid = graphic.createSpriteAnimation().setLoop(true).setPlaying(true).setDuration(PLAYERS==4?FRAMEDURATION:FRAMEDURATION_HERO).setZIndex(Z_CHAR_Y0+1+y*6);
    layerTop = graphic.createSpriteAnimation().setLoop(true).setPlaying(true).setDuration(PLAYERS==4?FRAMEDURATION:FRAMEDURATION_HERO).setZIndex(Z_CHAR_Y0+2+y*6);


    layerTop.setX(x * CELL_SIZE + viewer.maze_offset_x[mmid]+CHAR_OFFSET_X).setY(y * CELL_SIZE + viewer.maze_offset_y[mmid]+CHAR_OFFSET_Y).setScale(CHAR_SCALE).setAnchor(0.5);
    layerMid.setAnchor(0.5);
    layerBot.setAnchor(0.5);
    py = y;
  }

  public void setInitImages()
  {
    layerTop.setImages(chartype==CHARACTER_HERO? CharacterAnimation.idleSprites[chartype][0] : CharacterAnimation.sitSprites[chartype][0]);
  }

  public void showSitIdleHurt(int dir, String[][][] container, double tstart, double tend)
  {
    if (container==hurtSprites) changed = true;
    layerBot.setVisible(false);
    graphic.commitEntityState(tstart, layerBot);
    layerMid.setVisible(false);
    graphic.commitEntityState(tstart, layerMid);

    layerTop.setZIndex(Z_CHAR_Y0+2+py*6);
    graphic.commitEntityState(tstart, layerTop); // PREVENTS FLIPPING
    setAnim(layerTop, dir, container[chartype][dir%3]);
    graphic.commitEntityState(tstart>0?tstart+FRAMEDURATION_EPSILON:tstart, layerTop); // PREVENTS FLIPPING
  }

  public void showVanish(double tstart, double tend)
  {
    layerBot.setVisible(false);
    graphic.commitEntityState(tstart, layerBot);
    layerMid.setVisible(false);
    graphic.commitEntityState(tstart, layerMid);

    layerTop.setZIndex(Z_CHAR_Y0+2+py*6);
    if (PLAYERS!=4) graphic.commitEntityState(tstart, layerTop); // PREVENTS FLIPPING
    setAnim(layerTop, DIR_DOWN, vanishSprites[chartype]);
    if (PLAYERS==4) graphic.commitEntityState(tstart, layerTop); else graphic.commitEntityState(tstart>0?tstart+FRAMEDURATION_EPSILON:tstart, layerTop); // PREVENTS FLIPPING
    layerTop.setVisible(false);
    if (PLAYERS!=4) graphic.commitEntityState(tend, layerTop); // PREVENTS FLIPPING
  }

  public void showMove(int dir, int x, int y, double tstart, double tend) // todo init and endtime
  {
    changed = true;
    py = y;
    layerBot.setVisible(false);
    graphic.commitEntityState(tstart, layerBot);
    layerMid.setVisible(false);
    graphic.commitEntityState(tstart, layerMid);

    graphic.commitEntityState(tstart, layerTop);
    setAnim(layerTop, dir, moveSprites[chartype][dir%3]);
    layerTop.setZIndex(Z_CHAR_Y0+2+y*6);
    graphic.commitEntityState(tstart>0?tstart+FRAMEDURATION_EPSILON:tstart, layerTop); // PREVENTS FLIPPING
    setAnimXY(layerTop, x, y);
    graphic.commitEntityState(tend-FRAMEDURATION_EPSILON, layerTop);
  }

  public void showAttack(int dir, int weapon, int x, int y, double tstart, double tend) // todo init and endtime
  {

    py = y;
    if (dir==DIR_UP) layerBot.setVisible(false);
    else
    {

      layerBot.setZIndex(Z_CHAR_Y0+0+y*6);
      graphic.commitEntityState(tstart, layerBot);
      setAnim(layerBot, dir, x, y, attackSpritesBot[chartype][weapon][dir%3]);
    }
    graphic.commitEntityState(tstart, layerBot);

    setAnim(layerMid, dir, x, y, weapon, weaponSprites[weapon][dir%3]);
    layerMid.setZIndex(Z_CHAR_Y0+1+y*6);
    graphic.commitEntityState(tstart, layerMid);

    if (chartype < CHARACTER_HERO) { viewer.monsterTooltipsGroup[chartype].remove(layerTop); changed = true;}
    layerTop.setZIndex(Z_CHAR_Y0+2+y*6);
    graphic.commitEntityState(tstart, layerTop); // PREVENTS FLIPPING

    layerTop.setZIndex(Z_CHAR_Y0+2+y*6);
    setAnim(layerTop, dir, attackSpritesTop[chartype][weapon][dir%3]);
    //layerTop.setZIndex(999);

    graphic.commitEntityState(tstart>0?tstart+FRAMEDURATION_EPSILON:tstart, layerTop); // PREVENTS FLIPPING
  }

  private void setAnimXY(SpriteAnimation animation, int x, int y)
  {
    animation.setX(x * CELL_SIZE + viewer.maze_offset_x[mmid] + CHAR_OFFSET_X).setY(y * CELL_SIZE + viewer.maze_offset_y[mmid] + CHAR_OFFSET_Y);
  }

  private void setAnim(SpriteAnimation animation, int dir, String... images)
  {
    animation.setImages(images);
    animation.setScaleX(CHAR_SCALE * (dir==DIR_RIGHT?-1:1)).setScaleY(CHAR_SCALE);
    animation.setVisible(true);
  }

  private void setAnim(SpriteAnimation animation, int dir, int x, int y, String... images)
  {
    animation.setImages(images);
    animation.setScaleX(CHAR_SCALE * (dir==DIR_RIGHT?-1:1)).setScaleY(CHAR_SCALE);

    if (x >= 0 && y >= 0)
    {
      animation.setX(x * CELL_SIZE + viewer.maze_offset_x[mmid] + CHAR_OFFSET_X).setY(y * CELL_SIZE + viewer.maze_offset_y[mmid] + CHAR_OFFSET_Y);
    }
    animation.setVisible(true);
  }

  private void setAnim(SpriteAnimation animation, int dir, int x, int y, int weapon, String... images)
  {
    animation.setImages(images);
    animation.setScaleX(CHAR_SCALE * (dir==DIR_RIGHT?-1:1)).setScaleY(CHAR_SCALE);
    animation.setX(x * CELL_SIZE + viewer.maze_offset_x[mmid] + CHAR_OFFSET_X + WEAP_OFFSET_X[chartype][weapon][dir]).setY(y * CELL_SIZE + viewer.maze_offset_y[mmid] + CHAR_OFFSET_Y + WEAP_OFFSET_Y[chartype][weapon][dir]);
    animation.setVisible(true);
  }


}
