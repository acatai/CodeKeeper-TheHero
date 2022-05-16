import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;


public class PlayerRandom
{
  private static Random RNG = new Random(36);

  public static void main(String[] args)
  {
    Scanner in = new Scanner(System.in);

    int tx = -1;
    int ty = -1;
    int lastx=-1;
    int lasty=-1;
    int step=0;

    // game loop
    while (true)
    {
      int x = in.nextInt();
      int y = in.nextInt();
      int hp = in.nextInt();
      int score = in.nextInt();
      int att1 = in.nextInt();
      int att2 = in.nextInt();
      int att3 = in.nextInt();
      int visibleEntities = in.nextInt();
      for (int i = 0; i < visibleEntities; i++)
      {
        int ex = in.nextInt(); // x position of the unit
        int ey = in.nextInt(); // y position of the unit
        int etype = in.nextInt(); // type of the unit: 0-4
        int evalue = in.nextInt();
      }
      System.err.println(String.format("%d %d %d %d %d %d %d", x, y, hp, score, att1, att2, att3));

      if ((tx < 0) || (lastx==x && lasty==y))
      {
        tx = RNG.nextInt(16);
        ty = RNG.nextInt(12);
        step = 0;
      }
      lastx = x;
      lasty = y;
      step++;

      double ppb = 0.05;
      if (RNG.nextDouble()<ppb && x < 15)
        System.out.println(String.format("ATTACK 0 %d %d", x+1, y));
      else if (RNG.nextDouble()<ppb && x > 0)
        System.out.println(String.format("ATTACK 0 %d %d", x-1, y));
      else if (RNG.nextDouble()<ppb && y < 11)
        System.out.println(String.format("ATTACK 0 %d %d", x, y+1));
      else if (RNG.nextDouble()<ppb && y > 0)
        System.out.println(String.format("ATTACK 0 %d %d", x, y-1));
      else
        System.out.println(String.format("MOVE %d %d step %d", tx, ty, step));



    }
  }
}
