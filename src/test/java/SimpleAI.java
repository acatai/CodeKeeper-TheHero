
import java.util.*;


public class SimpleAI
{
  private static Random RNG = new Random(36);

  //////////////////////////////////////////////
  //////////////////////////////////////////////

  public static HashSet<Integer> computeFov(int x, int y)
  {
    HashSet<Integer> fov = new HashSet<>();
    for (int x1 = 0; x1 < 16; x1++)
    {
      for (int y1 = 0; y1 < 12; y1++)
      {
        if (oneDimDistance(x, y, x1, y1) <= 3) fov.add(x1*100+y1);
      }
    }
    return fov;
  }

  public static boolean inMaze(int xy)
  {
    return inMaze(xy/100, xy%100);
  }

  public static boolean inMaze(int x, int y)
  {
    return !(x<0 || x> 16 || y < 0 || y > 12);
  }

  public static ArrayList<Integer> neighboursAll(int xy)
  {
    ArrayList<Integer> ns = new ArrayList<>();
    if (xy/100 > 0 ) ns.add( (xy/100-1)*100 + (xy%100));
    if (inMaze(xy/100+1, xy%100)) ns.add( (xy/100+1)*100 + (xy%100));
    if (xy%100 > 0 ) ns.add( xy - 1);
    if (inMaze(xy/100, (xy%100) +1)) ns.add( xy + 1);
    return ns;
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

  //////////////////////////////////////////////
  //////////////////////////////////////////////

  public static boolean useBow(int x, int y, int chargesBow, HashMap<Integer, Integer> monsters)
  {
    if (chargesBow<1) return false;
    for (Map.Entry<Integer, Integer> entry : monsters.entrySet())
    {
      if (entry.getValue()==10 && manhattanDistance(x, y, entry.getKey()/100, entry.getKey()%100)>=3)
      {
        System.out.println(String.format("ATTACK 3 %d %d Bow vs Orc", entry.getKey()/100, entry.getKey()%100));
        return true;
      }
    }
    return false;
  }

  public static boolean useHammer(int x, int y, int chargesHammer, HashMap<Integer, Integer> monsters)
  {
    if (chargesHammer<1) return false;
    int[] round = new int[]{(x)*100+(y+1), (x-1)*100+(y+1), (x-1)*100+(y), (x-1)*100+(y-1), (x)*100+(y-1), (x+1)*100+(y-1), (x+1)*100+(y), (x+1)*100+(y+1)};
    int[] mon = new int[8];
    for (int i=0; i <8; i++)
      mon[i] = monsters.containsKey(round[i])?1:0;
    //System.err.println(Arrays.toString(mon));

    for (int i=0; i <8; i++)
    {
      if (mon[(((i-1)%8)+8)%8]+mon[i]+mon[(i+1)%8]==3)
      {
        System.out.println(String.format("ATTACK 1 %d %d triple hammer", round[i]/100, round[i]%100));
        return true;
      }
    }

    for (int i=0; i <8; i++)
    {
      if (mon[(((i-1)%8)+8)%8]+mon[i]+mon[(i+1)%8]==2)
      {
        System.out.println(String.format("ATTACK 1 %d %d double hammer", round[i]/100, round[i]%100));
        return true;
      }
    }

    return false;
  }

  public static boolean useScythe(int x, int y, int chargesScythe, HashMap<Integer, Integer> monsters)
  {
    if (chargesScythe<1) return false;

    if (monsters.containsKey((x+1)*100+(y)) && monsters.containsKey((x+2)*100+(y)))
    {
      System.out.println(String.format("ATTACK 2 %d %d double scythe R", x+1, y));
      return true;
    }
    if (monsters.containsKey((x-1)*100+(y)) && monsters.containsKey((x-2)*100+(y)))
    {
      System.out.println(String.format("ATTACK 2 %d %d double scythe L", x-1, y));
      return true;
    }
    if (monsters.containsKey((x)*100+(y+1)) && monsters.containsKey((x)*100+(y+2)))
    {
      System.out.println(String.format("ATTACK 2 %d %d double scythe D", x, y+1));
      return true;
    }
    if (monsters.containsKey((x)*100+(y-1)) && monsters.containsKey((x)*100+(y-2)))
    {
      System.out.println(String.format("ATTACK 2 %d %d double scythe U", x, y-1));
      return true;
    }

    if (monsters.containsKey((x+1)*100+(y+1)) && monsters.containsKey((x+2)*100+(y+2)))
    {
      System.out.println(String.format("ATTACK 2 %d %d double scythe DR", x+1, y+1));
      return true;
    }
    if (monsters.containsKey((x-1)*100+(y+1)) && monsters.containsKey((x-2)*100+(y+2)))
    {
      System.out.println(String.format("ATTACK 2 %d %d double scythe DL", x-1, y+1));
      return true;
    }
    if (monsters.containsKey((x+1)*100+(y-1)) && monsters.containsKey((x+2)*100+(y-2)))
    {
      System.out.println(String.format("ATTACK 2 %d %d double scythe UR", x+1, y-1));
      return true;
    }
    if (monsters.containsKey((x-1)*100+(y-1)) && monsters.containsKey((x-2)*100+(y-2)))
    {
      System.out.println(String.format("ATTACK 2 %d %d double scythe UL", x-1, y-1));
      return true;
    }

    if (monsters.containsKey((x+2)*100+(y+2)) && monsters.get((x+2)*100+(y+2))==10)
    {
      System.out.println(String.format("ATTACK 2 %d %d double scythe DR orc", x+2, y+2));
      return true;
    }
    if (monsters.containsKey((x-2)*100+(y+2)) && monsters.get((x-2)*100+(y+2))==10)
    {
      System.out.println(String.format("ATTACK 2 %d %d double scythe DL orc", x-2, y+2));
      return true;
    }
    if (monsters.containsKey((x+2)*100+(y-2)) && monsters.get((x+2)*100+(y-2))==10)
    {
      System.out.println(String.format("ATTACK 2 %d %d double scythe UR orc", x+2, y-2));
      return true;
    }
    if (monsters.containsKey((x-2)*100+(y-2)) && monsters.get((x-2)*100+(y-2))==10)
    {
      System.out.println(String.format("ATTACK 2 %d %d double scythe UL orc", x-2, y-2));
      return true;
    }
    return false;
  }

  public static boolean useSword(int x, int y, HashMap<Integer, Integer> monsters)
  {
    if (monsters.containsKey((x+1)*100+(y)))
    {
      System.out.println(String.format("ATTACK 0 %d %d sword R", x+1, y));
      return true;
    }
    if (monsters.containsKey((x-1)*100+(y)))
    {
      System.out.println(String.format("ATTACK 0 %d %d sword L", x-1, y));
      return true;
    }
    if (monsters.containsKey((x)*100+(y+1)))
    {
      System.out.println(String.format("ATTACK 0 %d %d sword D", x, y+1));
      return true;
    }
    if (monsters.containsKey((x)*100+(y-1)))
    {
      System.out.println(String.format("ATTACK 0 %d %d sword U", x, y-1));
      return true;
    }

    return false;
  }

  //////////////////////////////////////////////
  //////////////////////////////////////////////

  //public static boolean oneStepExit(int x, int y, HashSet<Integer> charges)

  public static boolean oneStepItemtype(String itemtype, int x, int y, HashSet<Integer> items, HashMap<Integer,Integer> monsters)
  {
    for(int xy: neighboursAll(x*100+y))
    {
      if (items.contains(xy) && !monsters.containsKey(xy))
      {
        System.out.println(String.format("MOVE %d %d one step %s", xy/100, xy%100, itemtype));
        return true;
      }
    }
    return false;
  }

  public static boolean searchItemType(String itemtype, int x, int y, HashSet<Integer> items)
  {
    int mindist = 9999;
    int bestxy = -1;
    for (Integer xy: items)
    {
      int d = manhattanDistance(x*100+y, xy);
      if (mindist > d)
      {
        mindist = d;
        bestxy = xy;
      }
    }
    if (bestxy < 0) return false;

    System.out.println(String.format("MOVE %d %d go to %s (%d steps)", bestxy/100, bestxy%100, itemtype, mindist));
    return true;
  }

  //////////////////////////////////////////////
  //////////////////////////////////////////////

  public static void main(String[] args)
  {
    Scanner in = new Scanner(System.in);

    HashSet<Integer> charges = new HashSet<>();
    HashSet<Integer> potions = new HashSet<>();
    HashSet<Integer> treasures = new HashSet<>();
    HashSet<Integer> exit = new HashSet<>();

    HashSet<Integer> unseen = new HashSet<>();
    for (int x1 = 0; x1 < 16; x1++) for (int y1 = 0; y1 < 12; y1++) unseen.add(x1*100+y1);


    //while (true) System.out.println(String.format("MOVE 11 9"));
    // game loop
    while (true)
    {
      HashMap<Integer, Integer> monsters = new HashMap<>();

      int x = in.nextInt();
      int y = in.nextInt();
      int hp = in.nextInt();
      int score = in.nextInt();
      int chargesHammer = in.nextInt();
      int chargesScythe = in.nextInt();
      int chargesBow = in.nextInt();
      int visibleEntities = in.nextInt();
      for (int i = 0; i < visibleEntities; i++)
      {
        int ex = in.nextInt(); // x position of the unit
        int ey = in.nextInt(); // y position of the unit
        int etype = in.nextInt(); // type of the unit: 0-4
        int evalue = in.nextInt();
        if (etype >= 7) monsters.put(ex*100+ey, etype);
        else if (etype>=4) charges.add(ex*100+ey);
        else if (etype==3) potions.add(ex*100+ey);
        else if (etype==2) treasures.add(ex*100+ey);
        else if (etype==0) exit.add(ex*100+ey);
      }
      System.err.println(String.format("%d %d %d %d %d %d %d", x, y, hp, score, chargesHammer, chargesScythe, chargesBow));

      charges.remove(x*100+y); // remove our current position
      potions.remove(x*100+y); // remove our current position
      treasures.remove(x*100+y); // remove our current position
      unseen.removeAll(computeFov(x, y));


      if (oneStepItemtype("exit", x, y, exit, monsters))  continue;
      if (hp <= 10 && oneStepItemtype("potion", x, y, potions, monsters))  continue;

      if (useHammer(x, y, chargesHammer, monsters)) continue;
      if (useScythe(x, y, chargesScythe, monsters)) continue;
      if (useSword(x, y, monsters)) continue;
      if (useBow(x, y, chargesBow, monsters)) continue;

      if (oneStepItemtype("charge", x, y, charges, monsters))  continue;
      if (oneStepItemtype("treasure", x, y, treasures, monsters))  continue;

      if (hp <= 10 && searchItemType("potion", x, y, potions))  continue;
      if (searchItemType("charge", x, y, charges)) continue;
      if (searchItemType("exit", x, y, exit)) continue;
      if (searchItemType("treasure", x, y, treasures)) continue;

      if (searchItemType("unseen", x, y, unseen)) continue; // always true



      double ppb = 0.0; // todo random square random attack
      if (RNG.nextDouble()<ppb && x < 15)
        System.out.println(String.format("ATTACK 1 %d %d", x+1, y));
      else if (RNG.nextDouble()<ppb && x > 0)
        System.out.println(String.format("ATTACK 1 %d %d", x-1, y));
      else if (RNG.nextDouble()<ppb && y < 11)
        System.out.println(String.format("ATTACK 1 %d %d", x, y+1));
      else if (RNG.nextDouble()<ppb && y > 0)
        System.out.println(String.format("ATTACK 1 %d %d", x, y-1));
      else
        System.out.println(String.format("MOVE %d %d stay in place", x, y));



    }
  }
}
