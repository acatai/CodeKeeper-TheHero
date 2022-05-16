package com.codingame.game.engine;

import static com.codingame.game.engine.Constants.*;

public class Entity
{
  private static int COUNTER = 0;
  public int id;

  public int type;
  public int x;
  public int y;

  public Entity(int etype, int xpos, int ypos)
  {
    id = COUNTER++;

    type = etype;
    x = xpos;
    y = ypos;
  }

  public String toString()
  {
    int val = -1;
    if (type==TYPE_POTION) val = POTION_HEAL;
    if (type==TYPE_TREASURE) val = TREASURE_SCORE;
    if (type==TYPE_EXIT) val = EXIT_SCORE;
    if (type==TYPE_CHARGES1) val = 1;
    if (type==TYPE_CHARGES2) val = 2;
    if (type==TYPE_CHARGES3) val = 3;

    return String.format("%d %d %d %d", x, y, type, val);
  }

  public String toTooltipString(int score)
  {
    return String.format("Entity %d\nX: %d, Y: %d", type, x, y);
  }

}
