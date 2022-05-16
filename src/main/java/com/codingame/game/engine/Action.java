package com.codingame.game.engine;

import com.codingame.gameengine.core.SoloGameManager;
import com.codingame.gameengine.core.MultiplayerGameManager;

import java.util.HashSet;

import static com.codingame.game.engine.Constants.DIR_DOWN;
import static com.codingame.game.engine.MazeManager.dirTo;

public class Action
{
  public enum ActionType {MOVE, ATTACK, IDLE, };
  public ActionType actiontype;
  public int x;
  public int y;
  public int weapon;
  public HashSet<Integer> targets;
  public int dir;
  public String message;

  public int value; // todo remove


  public static Action parse (String data, MazeManager mm, MultiplayerGameManager manager) throws InvalidAction
  {
    String[] str = data.split(" ", 2);
    ActionType atype;

    switch (str[0].trim())
    {
      case "MOVE": atype = ActionType.MOVE; break;
      case "ATTACK": atype = ActionType.ATTACK; break;
      default: throw new InvalidAction("Invalid action name. Should be MOVE/ATTACK, given \""+str[0].trim()+"\"");
    }

    Protoaction pa = new Protoaction();
    pa.actiontype = atype;

    if (atype== ActionType.MOVE)
    {
      try
      {
        String[] args = str[1].split(" ", 3);
        pa.x = Integer.parseInt(args[0]);
        pa.y = Integer.parseInt(args[1]);
        pa.message = args.length < 3 ? "" : args[2].trim().replaceAll("\\\\n", "\n");
      } catch (Exception e)
      {
        throw new InvalidAction("Invalid MOVE arguments. Expected \"MOVE x y [message]\", given \""+data+"\".");
      }
      if (!MazeManager.inMaze(pa.x, pa.y))
        throw new InvalidAction("Invalid MOVE argument (coordinates). Values should not exceed map boundaries.");

      return newMove(pa, mm, manager);
    }

    if (atype== ActionType.ATTACK)
    {
      try
      {
        String[] args = str[1].split(" ", 4);
        pa.weapon = Integer.parseInt(args[0]);
        pa.x = Integer.parseInt(args[1]);
        pa.y = Integer.parseInt(args[2]);
        pa.message = args.length < 4 ? "" : args[3].trim().replaceAll("\\\\n", "\n");
      } catch (Exception e)
      {
        throw new InvalidAction("Invalid ATTACK arguments. Expected \"ATTACK weapon x y [message]\", given \""+data+"\".");
      }
      if (!MazeManager.inMaze(pa.x, pa.y))
        throw new InvalidAction("Invalid ATTACK argument (coordinates). Values should not exceed map boundaries.");
      if (pa.weapon<0 || pa.weapon > 3)
        throw new InvalidAction("Invalid ATTACK argument (weapon). Should be 0/1/2/3, given \""+pa.weapon+"\".");

      return newAttack(pa, mm, manager);
    }


    return null;
  }

  private Action (Protoaction pa)
  {
    this.actiontype = pa.actiontype;
    this.weapon = pa.weapon;
    this.x = pa.x;
    this.y = pa.y;
    this.message = pa.message;
    this.targets = new HashSet<>();
  }

  private static Action newMove(Protoaction pa, MazeManager mm, MultiplayerGameManager manager)
  {
    Action a = new Action(pa);

    int move = mm.heroPathfinding(a.x, a.y);

    a.x = (move % 100000) / 100;
    a.y = (move % 100000) % 100;
    if (a.x==mm.hero.x && a.y==mm.hero.y)
    {
      a.actiontype = ActionType.IDLE;
      a.dir = mm.hero.action==null ? DIR_DOWN : mm.hero.action.dir;
    }
    else
    {
      a.dir = dirTo(mm.hero.x, mm.hero.y, a.x, a.y);
    }

    if (move >= 100000)
      manager.addToGameSummary(manager.getPlayer(mm.mmid).getNicknameToken() +" [Warning] MOVE target unreachable. " + (a.actiontype==ActionType.IDLE?"No step closing distance possible.":"Made step closing distance.") + "\n");

    if (mm.monsters.containsKey(a.x*100+a.y)) // attack instead of move
    {
      a.actiontype = ActionType.ATTACK;
      a.weapon = 0;
      a.targets = new HashSet<Integer>() {{add(a.x*100+a.y);}};
    }

    if (mm.obstacles.containsKey(a.x*100+a.y))
    {
      a.x=mm.hero.x;
      a.y=mm.hero.y;
    }

    return a;
  }

  private static Action newAttack(Protoaction pa, MazeManager mm, MultiplayerGameManager manager)
  {
    Action a = new Action(pa);

    a.dir = dirTo(mm.hero.x, mm.hero.y, a.x, a.y);

    if (mm.hero.charges[a.weapon] < 1)
    {
      a.actiontype = ActionType.IDLE;
      a.dir = mm.hero.action.dir;
      manager.addToGameSummary(manager.getPlayer(mm.mmid).getNicknameToken() +" [Warning] Not enough weapon charges.\n");
      return a;
    }

    if (!mm.hero.validateWeaponRange(a.weapon, a.x, a.y))
    {
      a.actiontype = ActionType.IDLE;
      a.dir = mm.hero.action==null? DIR_DOWN : mm.hero.action.dir;
      manager.addToGameSummary(manager.getPlayer(mm.mmid).getNicknameToken() +" [Warning] Given coordinates are outside weapon range.\n");
      return a;
    }

    a.targets = mm.hero.computeWeaponTargets(a.weapon, a.x, a.y);

    return a;
  }

}
