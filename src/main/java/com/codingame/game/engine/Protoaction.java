package com.codingame.game.engine;


public class Protoaction
{
  public Action.ActionType actiontype;
  public int x;
  public int y;
  public int weapon;
  public String message;

  public Protoaction()
  {

  }

  public Protoaction(int weapon, int x, int y)
  {
    this.actiontype= Action.ActionType.ATTACK;
    this.weapon = weapon;
    this.x=x;
    this.y=y;
  }

  public Protoaction(int x, int y)
  {
    this.actiontype= Action.ActionType.MOVE;
    this.x=x;
    this.y=y;
  }

}
