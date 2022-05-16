package com.codingame.game.engine;

import java.util.*;

import static com.codingame.game.engine.CharacterAnimation.*;
import static com.codingame.game.engine.Constants.*;

public class MazeManager
{
  private final Viewer viewer;

  public int mmid;
  private Random rng;
  public Hero hero;
  public HashMap<Integer, Entity> obstacles = new HashMap<>();
  public HashMap<Integer, Entity> items = new HashMap<>();
  public HashMap<Integer, Monster> monsters = new HashMap<>();


  public MazeManager(Viewer viewer, int mmid, long seed)
  {
    this.mmid = mmid;
    rng = new Random(seed);
    this.viewer = viewer;
    String[] maze = new MazeGenerator().generateMaze(rng);

    for (int y = 0; y < MAZE_HEIGHT; y ++)
    {
      for (int x = 0; x < MAZE_WIDTH; x ++)
      {
        addEntity(maze[y].charAt(x)+"", x, y);
      }
    }
    hero.computeFov();
  }

  public void addEntity(String c, int x, int y)
  {
    addEntity(Constants.MAZE_CHARS_TYPES.get(c), x, y);
  }

  public void addEntity(int type, int x, int y)
  {
    if (type==Constants.TYPE_FLOOR) return;
    if (type==Constants.TYPE_HERO) hero = new Hero(x, y, mmid);
    if (type==Constants.TYPE_OBSTACLE) obstacles.put(x*100+y, new Entity(type, x, y));
    if (type== TYPE_EXIT) items.put(x*100+y, new Entity(type, x, y));
    if (type >= TYPE_TREASURE && type <= TYPE_CHARGES3) items.put(x*100+y, new Entity(type, x, y));
    if (type >= TYPE_MONSTER0 && type <= TYPE_MONSTER4) monsters.put(x*100+y, new Monster(type, x, y, mmid));
  }


  public boolean turnHero()
  {
    if (hero.action.actiontype == Action.ActionType.IDLE)
    {
      hero.animation.showSitIdleHurt(hero.action.dir, idleSprites, 0, FRAMEDURATION_FRAC1);
    }
    else if (hero.action.actiontype == Action.ActionType.MOVE)
    {
      hero.x = hero.action.x;
      hero.y = hero.action.y;
      hero.animation.showMove(hero.action.dir, hero.x, hero.y, 0, FRAMEDURATION_FRAC1);
      hero.computeFov();

      if (items.containsKey(hero.x*100+hero.y))
      {
        Entity item = items.get(hero.x*100+hero.y);
        heroPickupItem(item);
        viewer.updateEventNotificationsItem(item, 0, FRAMEDURATION_FRAC1, mmid);
        if (item.type==TYPE_EXIT) return true;
      }
    }
    else // ActionType.ATTACK
    {
      hero.charges[hero.action.weapon]--;
      hero.animation.showAttack(hero.action.dir, hero.action.weapon, hero.x, hero.y, 0, FRAMEDURATION_FRAC1);
      viewer.updateEventNotificationsDamage(hero.action.targets, -HERO_DMG[hero.action.weapon], 0, FRAMEDURATION_FRAC1, mmid);
    }

    viewer.updateFog();

    ArrayList<Integer> toremove = new ArrayList<>();
    for (Monster m:monsters.values())
    {
      if (hero.action.targets.contains(m.x*100+m.y))
      {
        m.hp -= HERO_DMG[hero.action.weapon];
        if (m.hp <= 0) // vanish
        {
          m.animation.showVanish(0, FRAMEDURATION_FRAC1);
          hero.score += m.score;
          toremove.add(m.x*100+m.y);
        }
        else // hurt
        {
          m.status = Monster.Status.HURT;
          m.dir = REV_DIR[hero.action.dir];
          m.animation.showSitIdleHurt(m.dir, hurtSprites, 0, FRAMEDURATION_FRAC1);
        }
        continue;
      }

      if (m.canSee(hero))
      {
        int dir = dirTo(m, hero);
        if (m.status == Monster.Status.AWARE && dir == m.dir) continue; // no need to change
        m.status = Monster.Status.AWARE;
        m.dir = dir;
        m.animation.showSitIdleHurt(m.dir, idleSprites, 0, FRAMEDURATION_FRAC1);
      }
      else
      {
        if (m.status == Monster.Status.PEACE) continue; // no need to change
        m.status = Monster.Status.PEACE;
        m.animation.showSitIdleHurt(m.dir, sitSprites, 0, FRAMEDURATION_FRAC1);
      }
    }
    for (Integer xy:toremove) monsters.remove(xy);

    return false;
  }

  public boolean turnMonsters()
  {
    int[] heroHurt = new int[] {0, 0, 0, 0};
    //for (Monster m:monsters.values())
    List<Monster> vs = new ArrayList<Monster>(monsters.values());
    Collections.shuffle(vs, rng);
    for (Monster m: vs)
    {
      if (m.canAttack(hero))
      {
        m.status= Monster.Status.ATTACK;
        m.dir = dirTo(m, hero);
        m.animation.showAttack(m.dir, m.weapon, m.x, m.y, FRAMEDURATION_FRAC0, 1);
        heroHurt[REV_DIR[m.dir]] += m.dmg;
      } else if (m.canSee(hero))
      {
        // if can see but cannot attack it means that it should move
        m.status = Monster.Status.MOVE;
        int move = monsterPathfinding(m);
        if (move == m.x*100+m.y)
        {
          m.dir = dirTo(m, hero);
        }
        else
        {
          m.dir = dirTo(m.x, m.y, move/100, move%100);
        }

        System.out.println("monster move from "+(m.x*100+m.y) + " to " + move);
        if (move==1107 && (m.x*100+m.y)==1007)
          monsterPathfinding(m);
        monsters.remove(m.x*100+m.y);
        monsters.put(move, m);
        m.x = move/100;
        m.y = move%100;
        m.animation.showMove(m.dir, m.x, m.y, FRAMEDURATION_FRAC0, 1);
      }
      else
      {
        if (m.status == Monster.Status.PEACE) continue; // no need to change
        m.status = Monster.Status.PEACE;
        m.animation.showSitIdleHurt(m.dir, sitSprites, FRAMEDURATION_FRAC0, 1);
      }

    }

    int dmg = heroHurt[0]+heroHurt[1]+heroHurt[2]+heroHurt[3];
    if (dmg > 0) // hero got hurt
    {
      int dir = 0;
      if (heroHurt[1] > heroHurt[dir]) dir = 1;
      if (heroHurt[2] > heroHurt[dir]) dir = 2;
      if (heroHurt[3] > heroHurt[dir]) dir = 3;


      hero.hp -= dmg;
      hero.score -= dmg;
      viewer.updateEventNotificationsDamage(new HashSet<Integer>(){{add(hero.x*100+hero.y);}}, -dmg, FRAMEDURATION_FRAC0, 1, mmid);

      if (hero.hp <= 0)
      {
        hero.animation.showVanish(FRAMEDURATION_FRAC0, 1);
        hero.score += DIED_SCORE;
        return true;
      }
      else
      {
        hero.animation.showSitIdleHurt(dir, hurtSprites, FRAMEDURATION_FRAC0, 1);
      }
    }
    else if (hero.action.actiontype == Action.ActionType.ATTACK) // if was attack then have to idle
    {
      hero.animation.showSitIdleHurt(hero.action.dir, idleSprites, FRAMEDURATION_FRAC0, 1);
    }
    return false;
  }

  private void heroPickupItem(Entity item)
  {
    items.remove(item.x*100+item.y);

    if (item.type == TYPE_EXIT) hero.score += EXIT_SCORE;
    if (item.type == TYPE_TREASURE) hero.score += TREASURE_SCORE;
    if (item.type == TYPE_POTION) hero.hp = Math.min(hero.hp + POTION_HEAL, HERO_MAX_HP);
    if (item.type >= TYPE_CHARGES1) hero.charges[item.type-TYPE_CHARGES1+1]++;

    viewer.pickupItem(item, mmid);
  }

  public ArrayList<String> toPlayerInputString()
  {
    ArrayList<String> input = new ArrayList<>();
    input.add(String.format("%d %d %d %d %d %d %d", hero.x, hero.y, hero.hp, hero.score, hero.charges[1], hero.charges[2], hero.charges[3]));

    ArrayList<String> entities = new ArrayList<>();
    for (Integer xy: hero.fov)
    {
      if (obstacles.containsKey(xy)) entities.add(obstacles.get(xy).toString());
      if (items.containsKey(xy)) entities.add(items.get(xy).toString());
      if (monsters.containsKey(xy)) entities.add(monsters.get(xy).toString());
    }
    input.add(entities.size()+"");
    input.addAll(entities);

    return input;
  }

  public int monsterPathfinding(Monster m)
  {
    LinkedList<Integer> queue = new LinkedList<>();
    int beststep = m.x*100+m.y;
    int bestdist = manhattanDistance(hero.x, hero.y, m.x, m.y);
    HashSet<Integer> visited = new HashSet<>();
    visited.add(beststep);
    HashMap<Integer, Integer> firststep = new HashMap<>();

    for (Integer nxy: neighboursAll(beststep))
    {
      // hero cannot be a neighbour if (nxy == hero.x*100+hero.y) return nxy + (obstacles.containsKey(nxy) ? 100000 : 0);
      if (obstacles.containsKey(nxy) || monsters.containsKey(nxy)) continue;
      queue.add(nxy);
      firststep.put(nxy, nxy);
    }

    while (queue.size() != 0)
    {
      int xy = queue.poll();
      if (xy == hero.x*100+hero.y) return firststep.get(xy);
      visited.add(xy);

      int d = manhattanDistance(xy/100, xy%100, hero.x, hero.y);
      if (d < bestdist)
      {
        beststep = firststep.get(xy);
        bestdist = d;
      }
      for (Integer nxy: neighboursAll(xy))
      {
        if (visited.contains(nxy) || obstacles.containsKey(nxy) ||
        oneDimDistance(nxy, m.x*100+m.y) > m.fov || monsters.containsKey(nxy)) continue;
        queue.add(nxy);
        visited.add(nxy);
        firststep.put(nxy, firststep.get(xy));
      }
    }

    return beststep;

  }

  public int heroPathfinding(int tx, int ty) // returns xy of the first step; plus 100000 if target is unreachable
  {
    LinkedList<Integer> queue = new LinkedList<>();
    int beststep = hero.x*100+hero.y;
    int bestdist = manhattanDistance(hero.x, hero.y, tx, ty);
    HashSet<Integer> visited = new HashSet<>();
    visited.add(beststep);
    HashMap<Integer, Integer> firststep = new HashMap<>();

    for (Integer nxy: neighboursAll(beststep))
    {
      if (nxy == tx*100+ty) return nxy + (obstacles.containsKey(nxy) ? 100000 : 0);
      if (obstacles.containsKey(nxy)) continue;
      queue.add(nxy);
      firststep.put(nxy, nxy);
    }

    while (queue.size() != 0)
    {
      int xy = queue.poll();
      if (xy == tx*100+ty) return firststep.get(xy) + (obstacles.containsKey(xy) ? 100000 : 0);
      visited.add(xy);

      int d = manhattanDistance(xy/100, xy%100, tx, ty);
      if (d < bestdist)
      {
        beststep = firststep.get(xy);
        bestdist = d;
      }
      for (Integer nxy: neighboursAll(xy))
      {
        if (visited.contains(nxy) || obstacles.containsKey(nxy) || !hero.fov.contains(nxy)) continue;
        queue.add(nxy);
        visited.add(nxy);
        firststep.put(nxy, firststep.get(xy));
      }
    }

    return beststep + (bestdist!=0 ? 100000 : 0);
  }

  public static ArrayList<Integer> neighboursAll(int xy)
  {
    ArrayList<Integer> ns = new ArrayList<>();
    if (xy/100 > 0 ) ns.add( (xy/100-1)*100 + (xy%100));
    if (xy/100 < Constants.MAZE_WIDTH - 1 ) ns.add( (xy/100+1)*100 + (xy%100));
    if (xy%100 > 0 ) ns.add( xy - 1);
    if (xy%100 < Constants.MAZE_HEIGHT -1) ns.add( xy + 1);
    return ns;
  }

  public static boolean inMaze(int xy)
  {
    return inMaze(xy/100, xy%100);
  }

  public static boolean inMaze(int x, int y)
  {
    return !(x<0 || x>=Constants.MAZE_WIDTH || y < 0 || y >= Constants.MAZE_HEIGHT);
  }

  public static int manhattanDistance(int xy1, int xy2)
  {
    return manhattanDistance(xy1/100, xy1%100, xy2/100, xy2%100);
  }

  public static int manhattanDistance(int x1, int y1, int x2, int y2)
  {
    return Math.abs(x1- x2) + Math.abs((y1 - y2));
  }

  public static int oneDimDistance(int xy1, int xy2)
  {
    return oneDimDistance(xy1/100, xy1%100, xy2/100, xy2%100);
  }

  public static int oneDimDistance(int x1, int y1, int x2, int y2)
  {
    return Math.max(Math.abs(x1- x2), Math.abs((y1 - y2)));
  }

  public static int dirTo(int fromxy, int toxy)
  {
    return manhattanDistance(fromxy/100, fromxy%100, toxy/100, toxy%100);
  }

  public static int dirTo(Entity from, Entity to)
  {
    return dirTo(from.x, from.y, to.x, to.y);
  }

  public static int dirTo(int fromx, int fromy, int tox, int toy)
  {
    int dx = tox - fromx;
    int dy = toy - fromy;
    if (Math.abs(dx) >= Math.abs(dy))
      return dx > 0 ? DIR_RIGHT : DIR_LEFT;
    else
      return (dy>0) ? DIR_DOWN : DIR_UP;
  }
}
