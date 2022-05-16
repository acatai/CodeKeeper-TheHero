package com.codingame.game.engine;

public class InvalidAction extends Exception
{
  private static final long serialVersionUID = -8185589153224401565L;

  public InvalidAction(String message)
  {
    super(message);
  }
}