package com.codingame.game.engine;

import java.util.HashSet;

import static com.codingame.game.engine.Constants.*;

public class Hero extends Entity
{
  public int hp;
  public int score;
  public int[] charges;
  public HashSet<Integer> fov = new HashSet<>();
  public Action action = null;
  public CharacterAnimation animation;

  public Hero(int xpos, int ypos, int mmid)
  {
    super(TYPE_HERO, xpos, ypos);

    hp = Constants.HERO_MAX_HP;
    charges = new int[] {HERO_INITCHARGES[0], HERO_INITCHARGES[1], HERO_INITCHARGES[2], HERO_INITCHARGES[3]};
    score = 0;
    animation = new CharacterAnimation(CHARACTER_HERO, x, y, mmid);
  }



  public void computeFov()
  {
    fov.clear();

    for (int x1 = 0; x1 < Constants.MAZE_WIDTH; x1++)
    {
      for (int y1 = 0; y1 < Constants.MAZE_HEIGHT; y1++)
      {
        if (MazeManager.oneDimDistance(x, y, x1, y1) <= Constants.HERO_FOV) fov.add(x1*100+y1);
      }
    }
  }

  public boolean validateWeaponRange(int weapon, int tx, int ty)
  {
    if (tx==x && ty==y) return false;
    if (weapon==WEAP_SWORD)
    {
      return MazeManager.manhattanDistance(tx, ty, x, y) <= 1;
    }
    if (weapon==WEAP_HAMMER)
    {
      return MazeManager.oneDimDistance(tx, ty, x, y) <= 1;
    }
    if (weapon==WEAP_SCYTHE)
    {
      return MazeManager.oneDimDistance(tx, ty, x, y) <= 2 && MazeManager.manhattanDistance(tx, ty, x, y) != 3;
    }
    if (weapon==WEAP_BOW)
    {
      return MazeManager.oneDimDistance(tx, ty, x, y) <= 3;
    }
    return false;
  }

  public HashSet<Integer> computeWeaponTargets(int weapon, int tx, int ty)
  {
    HashSet<Integer> targets = new HashSet<>();
    targets.add(tx*100+ty);
    if (weapon==WEAP_SWORD || weapon==WEAP_BOW)
      return targets;

    int dx = tx - x;
    int dy = ty - y;

    if (weapon==WEAP_HAMMER)
    {
      if (dx==0)
      {
        if (tx < MAZE_WIDTH-1) targets.add((tx+1)*100+(ty));
        if(tx > 0) targets.add((tx-1)*100+(ty));
      }
      else if (dy==0)
      {
        if (ty < MAZE_HEIGHT-1) targets.add((tx)*100+(ty+1));
        if (ty > 0) targets.add((tx)*100+(ty-1));
      }
      else
      {
        targets.add((tx)*100+(y));
        targets.add((x)*100+(ty));
      }
    }

    if (weapon==WEAP_SCYTHE)
    {
      if (MazeManager.oneDimDistance(x, y, tx, ty)==1 && MazeManager.inMaze(tx+dx, ty+dy))
        targets.add((tx+dx)*100+(ty+dy));
      else if (MazeManager.inMaze(x+dx/2, y+dy/2))
        targets.add((x+dx/2)*100+(y+dy/2));
    }

    return targets;
  }

  public String toString()
  {
    return String.format("hp:%d (%d,%d)", hp, x, y);
  }

  public String toTooltipString(int score)
  {
    return String.format("Nameless Hero\nX: %d, Y: %d\nHP: %d\nSCORE: %d", x, y, hp, score);
  }

  public String toInfoString()
  {
    return String.format("Nameless Hero\nX: %d, Y: %d\nHP: %d\nSCORE: %d\nsword ∞\nhammer %d\nscythe %d\nbow %d\n\nSAY:\n%s",
            x, y, hp, score, charges[1], charges[2], charges[3], action==null?"":action.message);
  }

}
