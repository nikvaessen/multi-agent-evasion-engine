package nl.dke.pursuitevasion.game.agents.impl.MCTS;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

/** This class is supposed to suggest a randomized move out of a board.
 * This it does by prefaring certain moves over others.
 * For example a move 3 away from the enemy could be more usefull then
 * just 1 away.
 * Created by Nibbla on 17.01.2017.
 */
public class ExtensionStrategy {
    State board;
    int[][] weights;
    int weightSum = 0;
    int n=0;
    Random random = new Random();

    public Move getSuggestedMove(ArrayList<Move> freeMoves){
        int currentLimit = random.nextInt(weightSum+1);
        System.out.println("Weightsum" + weightSum);
        int currentSum = 0;
        Move m = null;
        outerloop: for (int x = 0; x < n; x++) {
            for (int y = 0; y < n; y++) {

                currentSum += weights[x][y];
                if (currentSum >= currentLimit) {

                    System.out.println("Suggestet" + x + " " + y);
                    m = new Move(x, y);
                    break outerloop;
                }
            }
        }

        if (m == null){
            m = freeMoves.remove((int)(Math.random()*freeMoves.size()));
            System.out.println("SpecialRemoval: " + m.toString());
        }
        if (weightSum == 0){
            m = freeMoves.remove((int)(Math.random()*freeMoves.size()));
            System.out.println("SpecialRemoval" + m.toString());
        }

        int count = 0;
        outerloop: for (int x = 0; x < n; x++) {
            for (int y = 0; y < n; y++) {
                if (weights[x][y] == 0) {
                    count++;
                }
            }
        }
        System.out.println("Weightcounter: " +  (n*n-count));
        System.out.println("Freemovearraysize" + freeMoves.size());

        for (int i = 0; i < freeMoves.size(); i++) {
            if (freeMoves.get(i).getX()==m.getX() && freeMoves.get(i).getY()==m.getY()){
                m = freeMoves.remove(i);
                System.out.println("Freemovesize" + freeMoves.size());
                break;
            }
        }

        return m;
    }


    public ExtensionStrategy(ArrayList<Move> freeMoves, State b, TurnOrder player){
        this.board = b;
        Cell[][] cells = b.getGrid();
        n = b.getSize();

        boolean[][] freeMove = new boolean[n][n];
        weights = new int[n][n];
        //distance to player
        int[][] disttancePlayer = new int[n][n];
        boolean[][] visitedPlayer = new boolean[n][n];
        StatusCell playerInitiative = player;

        int initivativeValue=0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                initivativeValue += cells[i][j].getStatus().getInt();
            }
        }
        if (initivativeValue>0) playerInitiative = StatusCell.Blue;
        if (initivativeValue<0) playerInitiative = StatusCell.Red;
        if (initivativeValue==0) playerInitiative = player;

        int[][] disttanceEnemy = new int[n][n];
        boolean[][] visitedEnemy = new boolean[n][n];

        LinkedList<Cell> playerQueue = new LinkedList<Cell>();
        LinkedList<Cell> enemyQueue = new LinkedList<Cell>();

        settingUpDistanceMaps(cells,disttancePlayer,disttanceEnemy,n,player,playerQueue,enemyQueue,visitedPlayer,visitedEnemy);

        calcDistanceMap(disttancePlayer, visitedPlayer, playerQueue);
        calcDistanceMap(disttanceEnemy, visitedEnemy, enemyQueue);
/*
        System.out.println("EnemyDistances");
        for (int[] arr : disttanceEnemy) {
            System.out.println(Arrays.toString(arr));
        }

        System.out.println("FriendlyDistances");
        for (int[] arr : disttancePlayer) {
            System.out.println(Arrays.toString(arr));
        }


        System.out.println("Angepasst");
         */
        for (int x = 0; x < n; x++) {
            for (int y = 0; y < n; y++) {
                //if ( disttancePlayer[x][y] == 0) disttancePlayer[x][y] = -1;
                //if ( disttanceEnemy[x][y] == 0) disttanceEnemy[x][y] = -1;

                disttancePlayer[x][y] = Math.abs(disttancePlayer[x][y] - 2);
                disttanceEnemy[x][y] = Math.abs(disttanceEnemy[x][y] - 3);

                //if ( disttancePlayer[x][y] != -1) disttancePlayer[x][y] = Math.abs(disttancePlayer[x][y]-2);
                //if ( disttanceEnemy[x][y] != -1) disttanceEnemy[x][y] = Math.abs(disttanceEnemy[x][y]-3);
            }
        }


        for (int i=0;i<n;i++) {
            for (int j = 0; j < n; j++) {
                freeMove[i][j] = false;
            }
        }
        for (int i = 0; i < freeMoves.size(); i++) {
            Move fm = freeMoves.get(i);
            freeMove[fm.getX()][fm.getY()] = true;
        }
        for (int i=0;i<n;i++) {
            for (int j = 0; j < n; j++) {
                if (freeMove[i][j] == false) {
                    disttancePlayer[i][j] = -1;
                    disttanceEnemy[i][j] = -1;
                }
            }
        }




        int max = -1;
        //calulating sum
        for (int x = 0; x < n; x++) {
            for (int y = 0; y < n; y++) {
                int sum=0;
                if (playerInitiative==player) {  sum= disttancePlayer[x][y];}
                else {sum= disttanceEnemy[x][y];}

                if (sum > max) max = sum;

                weights[x][y] = sum;
            }
        }
        max++;

        for (int x = 0; x < n; x++) {
            for (int y = 0; y < n; y++) {
                weights[x][y] = max - weights[x][y];
                if (freeMove[x][y] == false) weights[x][y] = 0;
                weightSum += weights[x][y];
            }
        }
        /*
        System.out.println("EnemyDistances");
        for (int[] arr : disttanceEnemy) {
            System.out.println(Arrays.toString(arr));
        }

        System.out.println("FriendlyDistances");
        for (int[] arr : disttancePlayer) {
            System.out.println(Arrays.toString(arr));
        }
        System.out.println("PauerToThePeople");

        System.out.println("weights");
        for (int[] w : weights) {
            System.out.println(Arrays.toString(w));
        }*/
        System.out.println("Freemove: " + freeMove.length + " " );
      /*  */
    }

    private void calcDistanceMap(int[][] disttancePlayer, boolean[][] visitedPlayer, LinkedList<Cell> playerQueue) {
        if (playerQueue.isEmpty()){
            for (int x = 0; x < n; x++) {
                for (int y = 0; y < n; y++) {
                    disttancePlayer[x][y] = 1;
                }
            }
            return;
        }
        //destance search player



        while (!playerQueue.isEmpty()){



            Cell c =  playerQueue.poll();
            Cell[] neighbours = c.getNeighbors();
            for (int i = 0; i < neighbours.length; i++) {
                Cell neigh = neighbours[i];
                if (neigh == null) continue;
                if (visitedPlayer[neigh.getCoordXJ()][neigh.getCoordYI()]) continue;
                if (c.getCoordXJ()<0||c.getCoordXJ()==n||c.getCoordYI()<0||c.getCoordYI()==n){
                    disttancePlayer[neigh.getCoordXJ()][neigh.getCoordYI()] = 1;
                }else{
                    disttancePlayer[neigh.getCoordXJ()][neigh.getCoordYI()] = disttancePlayer[c.getCoordXJ()][c.getCoordYI()]+1;
                }
               // if (disttancePlayer[neigh.getCoordYI()][neigh.getCoordXJ()] != -1) continue;

                visitedPlayer[neigh.getCoordXJ()][neigh.getCoordYI()]=true;
                playerQueue.add(neigh);
            }
        }
    }

    private void settingUpDistanceMaps(Cell[][] cells, int[][] disttancePlayer, int[][] disttanceEnemy, int n, StatusCell player, Queue playerQueue, Queue enemyQueue, boolean[][] visitedPlayer, boolean[][] visitedEnemy) {
        for (int x = 0; x < n; x++) {
            for (int y = 0; y < n; y++) {


                if (cells[y][x].getStatus() == player) {
                    disttancePlayer[y][x] = 0;
                    disttanceEnemy[y][x] = -1;
                    playerQueue.add(cells[y][x]);
                   /* if (player == StatusCell.Blue) {
                        if (y!=n-1||y!=0) //checking for x and y that they not included twice
                    }else {
                        if (x!=n-1||x!=0)  playerQueue.add(cells[y][x]);  //checking for x and y that they not included twice

                    }*/

                    visitedEnemy[y][x] = true;
                    visitedPlayer[y][x] = true;
                } else if (cells[y][x].getStatus() == player.opposite()) {
                    disttanceEnemy[y][x] = 0;
                    disttancePlayer[y][x] = -1;
                    enemyQueue.add(cells[y][x]);
                    visitedPlayer[y][x] = true;
                    visitedEnemy[y][x] = true;

                } else {
                    disttanceEnemy[y][x] = -1;
                    disttancePlayer[y][x] = -1;
                    visitedPlayer[y][x] = false;
                    visitedEnemy[y][x] = false;
                }


            }
        }


        if (player == StatusCell.Blue) {
            Cell special = new Cell(-1, -1);
            Cell special2 = new Cell(n, -1);

            Cell[] neighbours = new Cell[n];
            Cell[] neighbours2 = new Cell[n];
            for (int y = 0; y < n; y++) {
                neighbours[y] = cells[y][0];
                neighbours2[y] = cells[y][n - 1];
            }
            special.overWriteNeighbours(neighbours);
            special2.overWriteNeighbours(neighbours2);
            playerQueue.add(special);
            playerQueue.add(special2);
        }

        if (player == StatusCell.Red) {
            Cell special = new Cell(-1, -1);
            Cell special2 = new Cell(-1, n);

            Cell[] neighbours = new Cell[n];
            Cell[] neighbours2 = new Cell[n];
            for (int x = 0; x < n; x++) {


                neighbours[x] = cells[0][x];
                neighbours2[x] = cells[n - 1][x];
            }
            special.overWriteNeighbours(neighbours);
            special2.overWriteNeighbours(neighbours2);
            playerQueue.add(special);
            playerQueue.add(special2);
        }
    }
    }



