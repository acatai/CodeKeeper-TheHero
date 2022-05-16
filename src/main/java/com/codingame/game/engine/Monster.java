package com.codingame.game.engine;

import static com.codingame.game.engine.Constants.DIR_DOWN;

public class Monster extends Entity
{
  public enum Status {PEACE, AWARE, ATTACK, HURT, MOVE};


  public int chartype;
  public int fov;
  public int range;
  public int hp;
  public int maxhp;
  public int dmg;
  public int score;
  public int weapon;
  public Status status = Status.PEACE;
  public int dir = DIR_DOWN;

  public CharacterAnimation animation;

  public Monster(int type, int xpos, int ypos, int mmid)
  {
    super(type, xpos, ypos);

    chartype = type - Constants.TYPE_MONSTER0;
    fov =   Constants.MONSTER_STATS[chartype][0];
    range = Constants.MONSTER_STATS[chartype][1];
    hp =    Constants.MONSTER_STATS[chartype][2];
    maxhp = hp;
    dmg =   Constants.MONSTER_STATS[chartype][3];
    score = hp;

    weapon = range==2 ? 3: 0;
    animation = new CharacterAnimation(chartype, x, y, mmid);
  }

  public boolean canSee(Entity e)
  {
    return MazeManager.oneDimDistance(x, y, e.x, e.y) <= fov;
  }

  public boolean canAttack(Entity e)
  {
    return MazeManager.oneDimDistance(x, y, e.x, e.y) <= range;
  }

  public String toString()
  {
    return String.format("%d %d %d %d", x, y, type, hp);
  }

  public String toTooltipString(int score)
  {
    return String.format("Entity %d\nX: %d, Y: %d", type, x, y);
  }

}
