package com.codingame.game.engine;


import com.codingame.game.Player;
import com.codingame.gameengine.core.MultiplayerGameManager;
import com.codingame.gameengine.module.entities.*;
import com.codingame.gameengine.module.toggle.ToggleModule;
import com.codingame.gameengine.module.tooltip.TooltipModule;

import java.util.*;

import static com.codingame.game.engine.Constants.*;

public class Viewer
{
  private MazeManager[] mms;
  private Random rng;

  private GraphicEntityModule graphic;
  private MultiplayerGameManager<Player> manager;
  private TooltipModule tooltip;
  private ToggleModule toggle;


  private String PRELOAD_FOG;
  private String[] PRELOAD_MAZE;
  private String[] PRELOAD_ITEMS;


  //////////////////////////
  // SETTINGS
  //////////////////////////

  public int Z_FLOOR = 00;
  public int Z_OBSTACLE = 10;
  public int Z_WALL = 20;
  public int Z_CORNER = 30;

  public int Z_COORDINATES = 40;
  public int Z_ITEM = 50;


  public static int Z_CHAR_Y0 = 100;

  public static int Z_EVENT_NOTIF = 1100;
  public static int Z_FOG = 200;

  public static int Z_ENDGAME_BOT = 20010;
  public static int Z_ENDGAME_TOP = 20020;

  public int Z_MISSION_BG = 4008;
  public int Z_MISSION_PANEL = 4010;
  public int Z_MISSION_TEXT = 4012;


  public int[] maze_offset_x;
  public int[] maze_offset_y;

  private int avatarSize = 130;

  public static int CELL_SIZE = PLAYERS==4 ? 34 : 72;
  private static double FOG_ALPHA = 0.7;

  private static int EVENT_OFFSET_Y = -15;
  private static int EVENT_FLOAT_Y = -40;


  private Group coordinateTooltips;
  public Group[] monsterTooltipsGroup = new Group[5];

  private Sprite centergfx;
  private Group fogGroup;
  private Group floorGroup;
  private Group missionGroup;
  private Group[] itemsGroup = new Group[7];
  private ArrayList<Sprite>[] itemsList = new ArrayList[] {new ArrayList<Sprite>(), new ArrayList<Sprite>(), new ArrayList<Sprite>(), new ArrayList<Sprite>()};
  private Group hudGroup;

  private static int[][] itemsmap = new int[][] {{0, 4}, {}, {10,13}, {14,7}, {11, 16}, {1, 8}, {9, 6}};


  private BitmapText turntext;

  private Sprite[][][] fog = new Sprite[4][Constants.MAZE_WIDTH][Constants.MAZE_HEIGHT];
  private Rectangle[][] monsterTooltipsText = new Rectangle[Constants.MAZE_WIDTH][Constants.MAZE_HEIGHT];


  private BitmapText[][][] eventNotifs = new BitmapText[4][Constants.MAZE_WIDTH][Constants.MAZE_HEIGHT];
  private HashMap<Integer, Sprite>[] items = new HashMap[] {new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>()};

  private BitmapText[][] info = new BitmapText[4][7];

  public Viewer(GraphicEntityModule graphic, MultiplayerGameManager<Player> manager, TooltipModule tooltip, ToggleModule toggle, int players)//, int offset_x, int offset_y)
  {
    this.graphic = graphic;
    this.manager = manager;
    this.tooltip = tooltip;
    this.toggle = toggle;
    this.maze_offset_x =new int[] {1920 - CELL_SIZE * (MAZE_WIDTH+1) };
    this.maze_offset_y = new int[] {CELL_SIZE*2};
    if (players==2)
    {
      this.maze_offset_x = new int[] {1920/2 - CELL_SIZE * (MAZE_WIDTH+1), 1920/2 + 2*CELL_SIZE};
      this.maze_offset_y = new int[] {CELL_SIZE*2, CELL_SIZE*2};
    }
    if (players==4)
    {
      this.maze_offset_x = new int[] {1920/2 - CELL_SIZE * (MAZE_WIDTH+1), 1920/2 + 2*CELL_SIZE,
                                      1920/2 - CELL_SIZE * (MAZE_WIDTH+1), 1920/2 + 2*CELL_SIZE};
      int ymargin = CELL_SIZE/2;
      this.maze_offset_y = new int[] {1080/2 - CELL_SIZE * (MAZE_HEIGHT+1)-ymargin, 1080/2 - CELL_SIZE * (MAZE_HEIGHT+1)-ymargin,
                                      1080/2 + 2*CELL_SIZE+ymargin, 1080/2 + 2*CELL_SIZE+ymargin};
    }
  }


  public void init(MazeManager[] mazes, long seed)
  {
    this.mms = mazes;
    //this.rng = new Random(seed);
    this.rng = new Random(seed);

    PRELOAD_MAZE = graphic.createSpriteSheetSplitter().setSourceImage("maze.png").setImageCount(29).setWidth(256).setHeight(256).setOrigRow(0).setOrigCol(0).setImagesPerRow(5).setName("").split();

    if (mms.length==4) initFloorFourPlayers();


    centergfx = graphic.createSprite().setImage("logo.png").setX(1920/2).setY(1080/2).setZIndex(Z_MISSION_PANEL).setAnchor(0.5);
    initInfoPanelMultiplayer();

    turntext = graphic.createBitmapText()
            .setFont("s")
            .setFontSize(30)
            .setText("")
            .setAnchor(0.5)
            .setTint(0x000000)
            .setX(1920/2)
            .setY(1080/2-20)
            .setZIndex(Z_MISSION_BG);
  }

  public void initMoreMultiplayer(int turn)
  {
    switch (turn)
    {
      case 1:
        System.out.println("Initializing Walls");
        for (int mmid = 0; mmid< mms.length; mmid++) initWalls(mmid);
        return;
      case 2:
        System.out.println("Initializing Obstacles");
        for (int mmid = 0; mmid< mms.length; mmid++) initObstacles(mmid);
        return;
      case 3:
        System.out.println("Preinitializing Monster Layers 1/4");
        for (Monster m:mms[0].monsters.values()) m.animation.preinitLayers(m.x, m.y);
        return;
      case 4:
        System.out.println("Preinitializing Monster Layers 2/4");
        for (Monster m:mms[1].monsters.values()) m.animation.preinitLayers(m.x, m.y);
        return;
      case 5:
        System.out.println("Preinitializing Monster Layers 3/4");
        for (Monster m:mms[2].monsters.values()) m.animation.preinitLayers(m.x, m.y);
        return;
      case 6:
        System.out.println("Preinitializing Monster Layers 4/4");
        for (Monster m:mms[3].monsters.values()) m.animation.preinitLayers(m.x, m.y);
        return;
      case 7:
        System.out.println("Initializing Monsters 1/4");
        for (Monster m:mms[0].monsters.values()) m.animation.setInitImages();
        return;
      case 8:
        System.out.println("Initializing Monsters 2/4");
        for (Monster m:mms[1].monsters.values()) m.animation.setInitImages();
        return;
      case 9:
        System.out.println("Initializing Monsters 3/4");
        for (Monster m:mms[2].monsters.values()) m.animation.setInitImages();
        return;
      case 10:
        System.out.println("Initializing Monsters 4/4");
        for (Monster m:mms[3].monsters.values()) m.animation.setInitImages();
        return;
      case 11:
        System.out.println("Initializing Items and Monster Tooltips");
        initMonsterTooltips();
        initItems();
        return;
      case 12:
        System.out.println("Initializing Coordinate Tooltips 1/2");
        initCoordinateTooltips(0);
        initCoordinateTooltips(1);
        return;
      case 13:
        System.out.println("Initializing Coordinate Tooltips 2/2");
        initCoordinateTooltips(2);
        initCoordinateTooltips(3);
        return;
      case 14:
        System.out.println("Initializing Fog 1/2");
        initFog(0);
        initFog(1);
        return;
      case 15:
        System.out.println("Initializing Fog 2/2");
        initFog(2);
        initFog(3);
        return;
      case 16:
        System.out.println("Initializing Event Notifications 1/2");
        initEventNotifications(0);
        initEventNotifications(1);
        return;
      case 17:
        System.out.println("Initializing Event Notifications 2/2");
        initEventNotifications(2);
        initEventNotifications(3);
        return;
      case 18:
        floorGroup.setZIndex(Z_FLOOR);
        centergfx.setVisible(false);
        //initInfoPanelMultiplayer();
        for (int mmid = 0; mmid< mms.length; mmid++) updateMonsterTooltips(mmid);
        for (int mmid = 0; mmid< mms.length; mmid++) updateHeroTooltip(mmid);
        //for (int mmid = 0; mmid< mms.length; mmid++) updateInfo(mmid,0);
        updateFog();
        for (int mmid = 0; mmid< mms.length; mmid++) mms[mmid].hero.animation.setInitImages();
        updateInfo(0, turn); // for updating turn string
        return;
      default:
        return;
    }

  }

  private void initInfoPanelMultiplayer()
  {
    hudGroup = graphic.createGroup().setZIndex(Z_MISSION_TEXT);
    int[] xss = {165, 1920-165, 165, 1920-165};
    int[] yss = {CELL_SIZE+CELL_SIZE/2, CELL_SIZE+CELL_SIZE/2, CELL_SIZE*(MAZE_HEIGHT+5)+CELL_SIZE/2, CELL_SIZE*(MAZE_HEIGHT+5)+CELL_SIZE/2};
    Sprite s;
    BitmapText t;
    for (int mmid = 0; mmid< mms.length; mmid++)
    {
      s = graphic.createSprite().setImage("infopanel.png").setX(xss[mmid]).setY(yss[mmid]-CELL_SIZE).setZIndex(Z_MISSION_PANEL).setAnchorX(0.5);
      hudGroup.add(s);

      s = graphic.createSprite().setImage(manager.getPlayer(mmid).getAvatarToken()).setX(xss[mmid]).setY(yss[mmid]).setBaseHeight(avatarSize).setBaseWidth(avatarSize).setZIndex(Z_MISSION_TEXT).setAnchorX(0.5);
      hudGroup.add(s);
      t = graphic.createBitmapText().setFont("s").setFontSize(36) // gfs_jackson_regular_32
              .setText(manager.getPlayer(mmid).getNicknameToken())
              .setAnchorX(0.5).setAnchorY(0.0).setTint(manager.getPlayer(mmid).getColorToken()).setX(xss[mmid]).setZIndex(Z_MISSION_TEXT)
              .setY(yss[mmid]+90);
      hudGroup.add(t);

      int size = CELL_SIZE;
      int margin = 10;
      int[] ys = new int[]{224, 264, 304, 344, 384, 424};
      String[] spriteOnmap = new String[]{"i14", "i10", null, "i11", "i1", "i9"};
      String[] spriteSymbol = new String[]{"i3", "i12", "i17", "i15", "i5", "i2"};
      int[] item = new int[]{3, 2, 7, 4, 5, 6};

      int[] col = new int[]{TEXT_COLORS[2], TEXT_COLORS[4], TEXT_COLORS[5], TEXT_COLORS[5], TEXT_COLORS[5], TEXT_COLORS[5]};


      for (int i = 0; i < ys.length; i++)
      {
        if (spriteSymbol[i] != null)
        {
          s = graphic.createSprite().setImage(spriteSymbol[i]).setX(xss[mmid] - 2*size - margin).setY(yss[mmid]+ys[i]).setZIndex(Z_MISSION_TEXT).setBaseWidth(size).setBaseHeight(size).setAnchor(0.5);
          if (item[i] != 3)
            tooltip.setTooltipText(s, String.format("%s\n%s", ITEM_DESCRIPTIONS[item[i]][0], ITEM_DESCRIPTIONS[item[i]][1]));
          else
            tooltip.setTooltipText(s, HEALTH_DESCRIPTION);
          hudGroup.add(s);
        }
        if (spriteOnmap[i] != null)
        {
          s = graphic.createSprite().setImage(spriteOnmap[i]).setX(xss[mmid] - size ).setY(yss[mmid]+ys[i]).setZIndex(Z_MISSION_TEXT).setBaseWidth(size).setBaseHeight(size).setAnchor(0.5);
          tooltip.setTooltipText(s, String.format("%s\n%s", ITEM_DESCRIPTIONS[item[i]][0], ITEM_DESCRIPTIONS[item[i]][1]));
          hudGroup.add(s);
        }

        t = graphic.createBitmapText()
                .setFont("s")
                .setFontSize(28)
                .setText("")
                //.setMaxWidth(300)
                .setAnchorX(0.0) // .setAnchorX(0.5)
                .setAnchorY(1.0)
                .setTint(col[i])
                //.setX(xss[mmid])
                .setX(xss[mmid] - margin)
                .setY(yss[mmid]+ys[i] + 10)
                .setZIndex(Z_MISSION_TEXT);
        hudGroup.add(t);
        if (item[i] != 3)
          tooltip.setTooltipText(t, String.format("%s\n%s", ITEM_DESCRIPTIONS[item[i]][0], ITEM_DESCRIPTIONS[item[i]][1]));
        else
          tooltip.setTooltipText(t, HEALTH_DESCRIPTION);
        info[mmid][i] = t;


      }

      t = graphic.createBitmapText()
              .setFont("s")
              .setFontSize(20)
              .setText("")
              .setMaxWidth(540)
              .setAnchorX(0.5)
              .setAnchorY(0.5)
              .setTint(0xffffff)//(TEXT_COLORS[0])
              .setX(maze_offset_x[mmid] + (MAZE_WIDTH/2)*CELL_SIZE-CELL_SIZE/2)
              .setY(yss[mmid]+422)
              .setZIndex(Z_MISSION_TEXT);
      hudGroup.add(t);
      info[mmid][6] = t;
    }
  }

  private void initFloorFourPlayers()
  {
    floorGroup = graphic.createGroup().setZIndex(Z_MISSION_BG);
    for (int x = -12; x < MAZE_WIDTH+30; x+=2)
    {
      int cx = x * CELL_SIZE + maze_offset_x[0] + CELL_SIZE / 2; //here floor always on zero
      for (int y = -2; y < MAZE_HEIGHT+18; y+=2)
      {
        int cy = y * CELL_SIZE + maze_offset_y[0] + CELL_SIZE / 2;

        int type = rng.nextInt(8);
        if (type>2) type=0;

        String img = PRELOAD_MAZE[type];
        Sprite cellSprite = graphic.createSprite().setImage(img).setBaseWidth(2*CELL_SIZE).setBaseHeight(2*CELL_SIZE).setX(cx).setY(cy).setZIndex(Z_FLOOR).setAnchor(0.5);

        if (rng.nextInt(2)==0) cellSprite.setRotation(Math.PI/2);
        if (rng.nextInt(2)==0) cellSprite.setScaleX(-1);
        if (rng.nextInt(2)==0) cellSprite.setScaleY(-1);
        floorGroup.add(cellSprite);
      }
    }
  }

  private void initWalls(int mmid)
  {
    for (int x = 0; x < MAZE_WIDTH; x+=2)
    {
      int cx = x * CELL_SIZE + maze_offset_x[mmid] + CELL_SIZE / 2;
      graphic.createSprite().setImage(PRELOAD_MAZE[18+rng.nextInt(6)]).setBaseWidth(2*CELL_SIZE).setBaseHeight(2*CELL_SIZE).setX(cx).setY(maze_offset_y[mmid] - 3*CELL_SIZE / 2).setZIndex(Z_WALL).setAnchor(0.5);
      graphic.createSprite().setImage(PRELOAD_MAZE[7+rng.nextInt(6)]).setBaseWidth(2*CELL_SIZE).setBaseHeight(2*CELL_SIZE).setX(cx).setY(MAZE_HEIGHT * CELL_SIZE + maze_offset_y[mmid] + CELL_SIZE / 2).setZIndex(Z_WALL).setAnchor(0.5);
    }
    for (int y = 0; y < MAZE_HEIGHT; y+=2)
    {
      int cy = y * CELL_SIZE + maze_offset_y[mmid] + CELL_SIZE / 2;
      graphic.createSprite().setImage(PRELOAD_MAZE[13+rng.nextInt(5)]).setBaseWidth(2*CELL_SIZE).setBaseHeight(2*CELL_SIZE).setX(maze_offset_x[mmid] - 3*CELL_SIZE / 2).setY(cy).setZIndex(Z_WALL).setAnchor(0.5);
      graphic.createSprite().setImage(PRELOAD_MAZE[24+rng.nextInt(5)]).setBaseWidth(2*CELL_SIZE).setBaseHeight(2*CELL_SIZE).setX(MAZE_WIDTH * CELL_SIZE+ maze_offset_x[mmid] + CELL_SIZE / 2).setY(cy).setZIndex(Z_WALL).setAnchor(0.5);
    }
    graphic.createSprite().setImage(PRELOAD_MAZE[6]).setBaseWidth(2*CELL_SIZE).setBaseHeight(2*CELL_SIZE).setX(maze_offset_x[mmid] - 3*CELL_SIZE / 2).setY(maze_offset_y[mmid] - 3*CELL_SIZE / 2).setZIndex(Z_CORNER).setAnchor(0.5);
    graphic.createSprite().setImage(PRELOAD_MAZE[4]).setBaseWidth(2*CELL_SIZE).setBaseHeight(2*CELL_SIZE).setX(MAZE_WIDTH * CELL_SIZE+ maze_offset_x[mmid] + CELL_SIZE / 2).setY(maze_offset_y[mmid] - 3*CELL_SIZE / 2).setZIndex(Z_CORNER).setAnchor(0.5);
    graphic.createSprite().setImage(PRELOAD_MAZE[5]).setBaseWidth(2*CELL_SIZE).setBaseHeight(2*CELL_SIZE).setX(maze_offset_x[mmid] - 3*CELL_SIZE / 2).setY(MAZE_HEIGHT * CELL_SIZE + maze_offset_y[mmid] + CELL_SIZE / 2).setZIndex(Z_CORNER).setAnchor(0.5);
    graphic.createSprite().setImage(PRELOAD_MAZE[3]).setBaseWidth(2*CELL_SIZE).setBaseHeight(2*CELL_SIZE).setX(MAZE_WIDTH * CELL_SIZE + maze_offset_x[mmid] + CELL_SIZE / 2).setY(MAZE_HEIGHT * CELL_SIZE + maze_offset_y[mmid] + CELL_SIZE / 2).setZIndex(Z_CORNER).setAnchor(0.5);
  }

  private void initObstacles(int mmid)
  {
    for (int x = 0; x < Constants.MAZE_WIDTH; x++)
    {
      for (int y = 0; y < Constants.MAZE_HEIGHT; y++)
      {
        if (!mms[mmid].obstacles.containsKey(x * 100 + y)) continue;

        int cx = x * CELL_SIZE + maze_offset_x[mmid];
        int cy = y * CELL_SIZE + maze_offset_y[mmid];
        int l = mms[mmid].obstacles.containsKey((x - 1) * 100 + y) ? 1 : 0;
        int r = mms[mmid].obstacles.containsKey((x + 1) * 100 + y) ? 1 : 0;
        int u = mms[mmid].obstacles.containsKey(x * 100 + (y - 1)) ? 1 : 0;
        int d = mms[mmid].obstacles.containsKey(x * 100 + (y + 1)) ? 1 : 0;

        graphic.createSprite().setImage(String.format("%d%d%d%d.png", l, r, u, d)).setBaseWidth(2 * CELL_SIZE).setBaseHeight(2 * CELL_SIZE).setX(cx).setY(cy).setZIndex(Z_OBSTACLE).setAnchor(0.5);
      }
    }
  }

  private void initFog(int mmid)
  {
    PRELOAD_FOG = graphic.createSpriteSheetSplitter().setSourceImage("fog.png").setImageCount(1).setWidth(128).setHeight(128).setOrigRow(0).setOrigCol(0).setImagesPerRow(1).setName("f").split()[0];

    fogGroup = graphic.createGroup().setZIndex(Z_FOG);
    for (int x = 0; x < Constants.MAZE_WIDTH; x++)
    {
      for (int y = 0; y < Constants.MAZE_HEIGHT; y++)
      {
        int cx = x * CELL_SIZE + maze_offset_x[mmid] ;
        int cy = y * CELL_SIZE + maze_offset_y[mmid];

        fog[mmid][x][y] =  graphic.createSprite().setImage(PRELOAD_FOG).setBaseWidth(CELL_SIZE).setBaseHeight(CELL_SIZE).setX(cx).setY(cy).setZIndex(Z_FOG).setAnchor(0.5).setAlpha(FOG_ALPHA);
        fogGroup.add(fog[mmid][x][y]);
      }
    }
    toggle.displayOnToggleState(fogGroup, "toggleFog", true);
  }

  private void initEventNotifications(int mmid)
  {
    for (int x = 0; x < Constants.MAZE_WIDTH; x++)
    {
      for (int y = 0; y < Constants.MAZE_HEIGHT; y++)
      {
        eventNotifs[mmid][x][y] = graphic.createBitmapText().setVisible(false)
                .setText("")
                .setFont("s")
                .setX(x * CELL_SIZE + maze_offset_x[mmid])
                .setY(y * CELL_SIZE + maze_offset_y[mmid] + EVENT_OFFSET_Y)
                .setZIndex(Z_EVENT_NOTIF)
                //.setFontWeight(Text.FontWeight.BOLDER)
                .setFontSize(PLAYERS == 4?24:34)
                .setAnchorX(0.5)
                .setAnchorY(1.0);
      }
    }
  }

  private void initItems()
  {
    PRELOAD_ITEMS = graphic.createSpriteSheetSplitter().setSourceImage("items.png").setImageCount(18).setWidth(256).setHeight(256).setOrigRow(0).setOrigCol(0).setImagesPerRow(4).setName("i").split();

    for (int i=0; i <7; i++)
    {
      itemsGroup[i] = graphic.createGroup().setZIndex(Z_ITEM+10);
    }

    for (int mmid = 0; mmid< mms.length; mmid++)
    {
      for (Entity item : mms[mmid].items.values())
      {
        int cx = item.x * CELL_SIZE + maze_offset_x[mmid];
        int cy = item.y * CELL_SIZE + maze_offset_y[mmid];
        Sprite sprite = graphic.createSprite().setImage("i" + itemsmap[item.type][0]).setBaseWidth(CELL_SIZE).setBaseHeight(CELL_SIZE).setX(cx).setY(cy).setZIndex(Z_ITEM).setAnchor(0.5);
        items[mmid].put(item.x * 100 + item.y, sprite);
        //tooltip.setTooltipText(sprite, String.format("%s\n%s", ITEM_DESCRIPTIONS[item.type][0], ITEM_DESCRIPTIONS[item.type][1]));
        itemsGroup[item.type].add(sprite);
        itemsList[mmid].add(sprite);
      }
      for (Entity item : mms[mmid].obstacles.values())
      {
        int cx = item.x * CELL_SIZE + maze_offset_x[mmid] - CELL_SIZE / 2;
        int cy = item.y * CELL_SIZE + maze_offset_y[mmid] - CELL_SIZE / 2;
        Rectangle r = graphic.createRectangle().setFillColor(0xff0000).setAlpha(0.0).setX(cx).setY(cy).setWidth(CELL_SIZE).setHeight(CELL_SIZE).setZIndex(Z_ITEM);
        //tooltip.setTooltipText(r, String.format("%s\n%s", ITEM_DESCRIPTIONS[1][0], ITEM_DESCRIPTIONS[1][1]));
        itemsGroup[TYPE_OBSTACLE].add(r);
      }
    }

    for (int i=0; i < itemsGroup.length; i++) tooltip.setTooltipText(itemsGroup[i], String.format("%s\n%s", ITEM_DESCRIPTIONS[i][0], ITEM_DESCRIPTIONS[i][1]));
  }

  public void updateFog()
  {
    if (fog[0][0][0]==null) return;
    for (int mmid = 0; mmid< mms.length; mmid++)
    {
      for (int x = 0; x < Constants.MAZE_WIDTH; x++)
      {
        for (int y = 0; y < Constants.MAZE_HEIGHT; y++)
        {
          fog[mmid][x][y].setAlpha(mms[mmid].hero.fov.contains(x * 100 + y) ? 0 : FOG_ALPHA);
          graphic.commitEntityState(FRAMEDURATION_FRAC1, fog[mmid][x][y]);
        }
      }
    }
  }


  public void updateEventNotificationsItem(Entity item, double tstart, double tend, int mmid)
  {
    if (eventNotifs[0][0]==null) return;

    String val="+";
    int col=0;
    if (item.type == TYPE_EXIT) { val += EXIT_SCORE; col = TEXT_COLORS[4]; }
    if (item.type == TYPE_TREASURE) { val += TREASURE_SCORE; col = TEXT_COLORS[4]; }
    if (item.type == TYPE_POTION) { val += POTION_HEAL; col = TEXT_COLORS[3]; };
    if (item.type >= TYPE_CHARGES1) { val += ITEM_DESCRIPTIONS[item.type][0]; col = TEXT_COLORS[5]; }

    BitmapText t = eventNotifs[mmid][item.x][item.y];
    t.setVisible(true).setText(val).setY(item.y * CELL_SIZE + maze_offset_y[mmid] + EVENT_OFFSET_Y).setTint(col);
    graphic.commitEntityState(tstart, t);
    t.setY(t.getY()+ EVENT_FLOAT_Y);
    t.setVisible(false);
    graphic.commitEntityState(tend-FRAMEDURATION_EPSILON, t);
  }

  public void updateEventNotificationsDamage(HashSet<Integer> targets, int dmg, double tstart, double tend, int mmid)
  {
    if (eventNotifs[0][0]==null) return;
    for (Integer xy: targets)
    {
      BitmapText t = eventNotifs[mmid][xy/100][xy%100];
      t.setVisible(true).setText(dmg+"").setY(xy%100 * CELL_SIZE + maze_offset_y[mmid] + EVENT_OFFSET_Y).setTint(TEXT_COLORS[2]);
      graphic.commitEntityState(tstart, t);
      t.setY(t.getY()+ EVENT_FLOAT_Y);
      t.setVisible(false);
      graphic.commitEntityState(tend, t);
    }
  }


  public void pickupItem(Entity item, int mmid)
  {
    Sprite sprite = items[mmid].get(item.x*100+item.y);

    sprite.setImage("i"+itemsmap[item.type][1]);
    graphic.commitEntityState(0, sprite);

    if (item.type != TYPE_EXIT)
    {
      sprite.setAlpha(0);
      sprite.setVisible(false);
    }
    graphic.commitEntityState(FRAMEDURATION_FRAC1, sprite);


    if (item.type == TYPE_EXIT)
    {
      if (mms.length==1)
      {
        int cx = item.x * CELL_SIZE + maze_offset_x[0];
        int cy = item.y * CELL_SIZE + maze_offset_y[0];
        Circle c = graphic.createCircle().setFillColor(0x000000).setX(cx).setY(cy).setRadius(1).setZIndex(Z_ENDGAME_BOT);
        graphic.commitEntityState(FRAMEDURATION_FRAC1, c);
        c.setRadius(2000);
        graphic.commitEntityState(1, c);
      }
    }

  }

  private void initCoordinateTooltips(int mmid)
  {
    coordinateTooltips = graphic.createGroup().setZIndex(Z_COORDINATES);
    for (int x = 0; x < Constants.MAZE_WIDTH; x++)
    {
      for (int y = 0; y < Constants.MAZE_HEIGHT; y++)
      {
        int cx = x * CELL_SIZE + maze_offset_x[mmid] - CELL_SIZE / 2;
        int cy = y * CELL_SIZE + maze_offset_y[mmid] - CELL_SIZE / 2;
        Rectangle r = graphic.createRectangle().setAlpha(0.0).setX(cx).setY(cy).setWidth(CELL_SIZE).setHeight(CELL_SIZE).setZIndex(Z_COORDINATES);
        coordinateTooltips.add(r);
        tooltip.setTooltipText(r, String.format("X = %d, Y = %d", x, y));
      }
    }
  }

  private void initMonsterTooltips()
  {
    for (int i=0; i <5; i++)
    {
      monsterTooltipsGroup[i] = graphic.createGroup().setZIndex(Z_CHAR_Y0+2+11*6);
    }
    monsterTooltipsGroup[CHARACTER_BOX].setZIndex(Z_CHAR_Y0+2+0*6);


    for (int mmid = 0; mmid< mms.length; mmid++)
    {
      for (Monster m : mms[mmid].monsters.values())
      {
        monsterTooltipsGroup[m.chartype].add(m.animation.layerTop);
      }
    }

    for (int i = 0; i < monsterTooltipsGroup.length; i++) tooltip.setTooltipText(monsterTooltipsGroup[i], String.format("%s (%d HP)\n%s", MONSTER_DESCRIPTIONS[i][0], MONSTER_STATS[i][2], MONSTER_DESCRIPTIONS[i][1]));
  }


  public void updateMonsterTooltips(int mmid)
  {
    for (Monster m : mms[mmid].monsters.values())
    {
      if (!m.animation.changed) continue;
      tooltip.setTooltipText(m.animation.layerTop, String.format("%s (%d HP)\n%s", MONSTER_DESCRIPTIONS[m.chartype][0], m.hp, MONSTER_DESCRIPTIONS[m.chartype][1]));
      monsterTooltipsGroup[m.chartype].remove(m.animation.layerTop);

    }
  }

  public void updateHeroTooltip(int mmid)
  {
      tooltip.setTooltipText(mms[mmid].hero.animation.layerTop, String.format("Hero (%d/%d HP)\n%s", mms[mmid].hero.hp, HERO_MAX_HP, HERO_DESCRIPTION));
  }



  public void updateInfo(int mmid, int turn)
  {
      String[] content = new String[]{"" + mms[mmid].hero.hp, "" + mms[mmid].hero.score, "∞", "" + mms[mmid].hero.charges[1], "" + mms[mmid].hero.charges[2], "" + mms[mmid].hero.charges[3], mms[mmid].hero.action == null ? "" : mms[mmid].hero.action.message};
      for (int i = 0; i < content.length; i++)
      {
        info[mmid][i].setText(content[i]);
      }

    turntext.setText("Turn: "+ CurrentTurn(turn) + " / 150"); // todo

      graphic.commitEntityState(1, info[mmid]);
  }


  public void endgame(int mmid, String endtype, double time)
  {
    coordinateTooltips.setVisible(false);

    for(Sprite s: itemsList[mmid]) s.setVisible(false);;

    for(Monster m: mms[mmid].monsters.values())
    {
      m.animation.layerTop.setVisible(false);
      m.animation.layerMid.setVisible(false);
      m.animation.layerBot.setVisible(false);
    }
    mms[mmid].hero.animation.layerTop.setVisible(false);
    for (Sprite[] fg: fog[mmid]) for (Sprite f: fg) f.setVisible(false);

    info[mmid][6].setText("");

    graphic.createSprite().setX(maze_offset_x[mmid] + (MAZE_WIDTH/2)*CELL_SIZE-CELL_SIZE/2).setY(maze_offset_y[mmid] -CELL_SIZE/2+ (MAZE_HEIGHT/2)*CELL_SIZE)
            .setAnchor(0.5).setZIndex(Z_ENDGAME_TOP).setImage(endtype+".png").setScale(0.9);

    graphic.createBitmapText()
            .setFont("s")
            .setFontSize(52)
            .setText(""+mms[mmid].hero.score)
            .setAnchor(0.5)
            .setTint(manager.getPlayer(mmid).getColorToken())
            .setX(maze_offset_x[mmid] + (MAZE_WIDTH/2)*CELL_SIZE-CELL_SIZE/2)
            .setY(maze_offset_y[mmid] -CELL_SIZE/2+ (MAZE_HEIGHT/2)*CELL_SIZE + 120)
            .setZIndex(Z_ENDGAME_TOP);

    graphic.commitWorldState(time);
  }

}

