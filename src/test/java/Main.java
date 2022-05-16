import com.codingame.game.Player;
import com.codingame.gameengine.runner.SoloGameRunner;
import com.codingame.gameengine.runner.MultiplayerGameRunner;

public class Main
{
  public static void main(String[] args)
  {
    MultiplayerGameRunner gameRunner = new MultiplayerGameRunner();

    //Choose league level
    //gameRunner.setLeagueLevel(1);

    //gameRunner.setTestCase("test1.json");

    //Add players
    //gameRunner.addAgent(PlayerRandom.class);
    gameRunner.addAgent(SimpleAI.class);
    gameRunner.addAgent(SimpleAI.class);
    gameRunner.addAgent(PlayerRandom.class);
    gameRunner.addAgent(PlayerRandom.class);

    //Set game seed
    gameRunner.setSeed(21L); // 0L // 4666L

    //Run game and start viewer on 'http://localhost:8888/'
    gameRunner.start(8888);
  }
}
