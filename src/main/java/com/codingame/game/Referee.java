package com.codingame.game;

import com.codingame.game.engine.*;
import com.codingame.gameengine.core.AbstractPlayer.TimeoutException;
import com.codingame.gameengine.core.AbstractReferee;
import com.codingame.gameengine.core.SoloGameManager;
import com.codingame.gameengine.core.MultiplayerGameManager;
import com.codingame.gameengine.module.endscreen.EndScreenModule;
import com.codingame.gameengine.module.tooltip.TooltipModule;
import com.codingame.gameengine.module.entities.*;
import com.google.inject.Inject;
import com.codingame.gameengine.module.toggle.ToggleModule;


public class Referee extends AbstractReferee
{
  @Inject private MultiplayerGameManager<Player> manager;
  @Inject private GraphicEntityModule graphic;
  @Inject private TooltipModule tooltip;
  @Inject private ToggleModule toggle;
  @Inject private EndScreenModule ends;

  private Viewer viewer;

  private MazeManager[] mazes;

  @Override
  public void init()
  {
    viewer = new Viewer(graphic, manager, tooltip, toggle, Constants.PLAYERS);

    manager.setFirstTurnMaxTime(Constants.TIMELIMIT_INIT);
    manager.setTurnMaxTime(Constants.TIMELIMIT_TURN);
    manager.setMaxTurns(Constants.TURNLIMIT_INITIAL + 2*Constants.PLAYER_TURNS_MAX + 10);
    manager.setFrameDuration(1);

    CharacterAnimation.initCharacterAnimations(graphic, viewer);

    MazeManager mm0 = new MazeManager(viewer, 0, manager.getSeed());
    MazeManager mm1 = new MazeManager(viewer, 1, manager.getSeed());
    MazeManager mm2 = new MazeManager(viewer, 2, manager.getSeed());
    MazeManager mm3 = new MazeManager(viewer, 3, manager.getSeed());

    mazes = new MazeManager[] {mm0, mm1, mm2, mm3};

    viewer.init(mazes, manager.getSeed());
    viewer.updateFog();
  }

  @Override
  public void gameTurn(int turn)
  {
    if (turn <= Constants.TURNLIMIT_INITIAL)
    {
      manager.setFrameDuration(1);
      if (Constants.PLAYERS==4) viewer.initMoreMultiplayer(turn);
      return;
    }

    System.out.println(Constants.CurrentTurn(turn));
    if (turn==Constants.TURNLIMIT_INITIAL+1)
    {
      manager.addTooltip(manager.getPlayer(0), "The game begins!");
      return;
    }

    manager.setFrameDuration(Constants.FRAMEDURATION);


    if (turn == Constants.TURNLIMIT_INITIAL + (Constants.PLAYERS==4?2:1)*Constants.PLAYER_TURNS_MAX+1)
    {
      for (int mmid = 0; mmid < 4; mmid++)
      {
        if (!manager.getPlayer(mmid).isActive()) continue;
        manager.getPlayer(mmid).setScore(mazes[mmid].hero.score);
        manager.getPlayer(mmid).deactivate(manager.getPlayer(mmid).getNicknameToken() + " exceeded turn limit");
      }
    }

    if (!manager.getPlayer(0).isActive() && !manager.getPlayer(1).isActive() && !manager.getPlayer(2).isActive() && !manager.getPlayer(3).isActive())
    {
      manager.endGame();
      return;
    }


    if (turn%2==0) // 16+
    {
      for (int mmid = 0; mmid < 4; mmid++)
      {
        if (!manager.getPlayer(mmid).isActive()) continue;
        for (String line : mazes[mmid].toPlayerInputString())
          manager.getPlayer(mmid).sendInputLine(line);
        manager.getPlayer(mmid).execute();
      }

      for (int mmid = 0; mmid < 4; mmid++)
      {
        Player player = manager.getPlayer(mmid);
        if (!player.isActive()) continue;
        try
        {
          String output = player.getOutputs().get(0);
          mazes[mmid].hero.action = Action.parse(output, mazes[mmid], manager);
        } catch (InvalidAction e)
        {
          mazes[mmid].hero.score = Constants.ERROR_SCORE;
          manager.getPlayer(mmid).setScore(mazes[mmid].hero.score);
          viewer.endgame(mmid, "err", 1.0);
          viewer.updateInfo(mmid, turn);
          manager.getPlayer(mmid).deactivate(manager.getPlayer(mmid).getNicknameToken() + " made invalid action");
          continue;
        } catch (TimeoutException e)
        {
          mazes[mmid].hero.score = Constants.ERROR_SCORE;
          manager.getPlayer(mmid).setScore(mazes[mmid].hero.score);
          viewer.endgame(mmid, "err", 1.0);
          viewer.updateInfo(mmid, turn);
          manager.getPlayer(mmid).deactivate(player.getNicknameToken() + " timed out");
          continue;
        }
      }

      for (int mmid = 0; mmid < 4; mmid++)
      {
        if (!manager.getPlayer(mmid).isActive()) continue;

        boolean won = mazes[mmid].turnHero();
        viewer.updateMonsterTooltips(mmid);
        viewer.updateHeroTooltip(mmid);
        viewer.updateInfo(mmid, turn);

        if (won)
        {
          viewer.endgame(mmid, "win", 1.0);
          manager.getPlayer(mmid).setScore(mazes[mmid].hero.score);
          manager.getPlayer(mmid).deactivate(manager.getPlayer(mmid).getNicknameToken() + " reached exit");
        }
      }
    }
    else
    {
      for (int mmid = 0; mmid < 4; mmid++)
      {
        if (!manager.getPlayer(mmid).isActive()) continue;

        boolean lost = mazes[mmid].turnMonsters();
        viewer.updateHeroTooltip(mmid);
        viewer.updateInfo(mmid, turn);

        if (lost)
        {
          viewer.endgame(mmid, "lost", 1);
          manager.getPlayer(mmid).setScore(mazes[mmid].hero.score);
          manager.getPlayer(mmid).deactivate(manager.getPlayer(mmid).getNicknameToken() + " was destroyed");
        }
      }
    }
  }

  @Override
  public void onEnd()
  {
    String s = " points";

    int[] scores = { mazes[0].hero.score};
    String[] text = { scores[0] + s};
    if (Constants.PLAYERS==4)
    {
      scores = new int[] { mazes[0].hero.score, mazes[1].hero.score, mazes[2].hero.score, mazes[3].hero.score };
      text = new String[] { scores[0] + s, scores[1] + s, scores[2] + s, scores[3] + s };
    }

    ends.setTitleRankingsSprite("logoend.png");
    ends.setScores(scores, text);
  }

}
