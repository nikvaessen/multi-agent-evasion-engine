package nl.dke.pursuitevasion.game.agents.impl.MCTS;


import nl.dke.pursuitevasion.game.agents.AbstractAgent;

import java.util.*;

/**
 * Created by giogio on 1/21/17.
 * and Nibbla
 */
public class NodeTree_2 {
    private State state;
    private Move move;
    private NodeTree_2 parent;
    private ArrayList<NodeTree_2> children  = new ArrayList<>();;
    private TurnOrder currentplayer;
    private double wins=0,games=0,maxPoints=1;
    private boolean winningMove = false, losingMove= false, deadCell;



    public NodeTree_2(NodeTree_2 parent){
        if(parent!=null){
            this.parent = parent;

            parent.addChildren(this);
            this.currentplayer = parent.currentplayer.clone();
            this.currentplayer.nextPlayer();
        }
        children = new ArrayList<>();

    }

    public void setMove(Move move){

        this.move = move;
        state=move.getAfterState();


       // state.evaluate(currentplayer); should be done later or every expanded node gets one evaluation.


    }


    public void incrementWin(double scoreEvador, double scorePursuer, double EvadorPossible, double PursuerPossible, TurnOrder winningPlayer){
        if (this.currentplayer.isSameTeam(winningPlayer.getIDCurrent())){
           // this.wins+=scoreEvador;
            //this.maxPoints+=WinningPossible;
        }
        else {
           // this.wins-=scoreLoosing;
           // this.maxPoints-=LoosingPossible;
        }
        if (this.currentplayer.isEvader()){
            this.wins+=scoreEvador;
            this.maxPoints+=EvadorPossible;
        }else {
            this.wins+=scorePursuer;
            this.maxPoints+=PursuerPossible;
        }
        if(parent!=null)
            parent.incrementWin(scoreEvador, scorePursuer, EvadorPossible, PursuerPossible, winningPlayer);
    }
    public void incrementGame(){
        this.games+=1;//1;
        if(parent!=null)
            parent.incrementGame();
    }

    public boolean isLosingMove(){
        return losingMove;
    }

    public void setState(State state) {
        this.state = state;
    }

    public void setTurn(TurnOrder currentplayer) {
        this.currentplayer = currentplayer;
    }

    public void addChildren(NodeTree_2 child){
      //  System.out.println(this);
        children.add(child);
    }

   // public void setChildren(ArrayList<NodeTree_2> children) {
       // this.children = children;
    //}

    public void setWins(int wins) {
        this.wins = wins;
    }

    public void setGames(int games) {
        this.games = games;
    }

    public void setWinningMove(boolean winningMove) {
        this.winningMove = winningMove;
    }

    public void setLosingMove(boolean losingMove) {
        this.losingMove = losingMove;
    }

   /* public void setDeadCell(boolean deadCell) {
        this.deadCell = deadCell;
    }
    */

    public State getState() {
        return state;
    }

    public Move getMove() {
        return move;
    }

    public NodeTree_2 getParent() {
        return parent;
    }

    public ArrayList<NodeTree_2> getChildren() {
        return children;
    }



    public double getWins() {
        return wins;
    }

    public double getGames() {
        return games;
    }

    public boolean isWinningMove() {
        return winningMove;
    }

    public void setParent(NodeTree_2 parent) {
        this.parent = parent;
    }

    public String toString(){

        String s =  "Current ID player" + currentplayer.getIDCurrent()+ " " ;
        if (move != null) s+= move.toString();
        s += " " + "Depth: " + getDepth() + " Height: " + getHeight() + " Value: " + wins + " Games: " + games + " Parent: ";
        if (parent != null) if (parent.move!=null) s += parent.move.toString();
        return s;
    }

    public int getDepth(){
        NodeTree_2 dad = parent;
        int count = 0;
        while (dad!=null) {
            count++;
            dad = dad.parent;
        }
        return count;
    }

    public int getHeight(){
        if (isLeaf()) return 0;
        int maxHeight = -1;
        for (int i = 0; i < children.size(); i++) {
            int childHeight = children.get(i).getHeight();
            if (childHeight>maxHeight) maxHeight = childHeight;
        }

        maxHeight+=1;
        return maxHeight;
    }



    public Enumeration children() {
        Enumeration c = Collections.enumeration(children);
        return c;
    }


    public boolean isLeaf() {
        if (children.size()==0)return true;
        return false;
    }


    public String getName() {
        if (move == null) return "root";
        return move.toString();
    }

    public  ArrayList<NodeTree_2> breadthFirst(){
        ArrayList<NodeTree_2> bfsearch = new ArrayList<>((int)games);
        Queue<NodeTree_2> quey = new LinkedList<NodeTree_2>();

        quey.add(this);
        while (!quey.isEmpty()){
            NodeTree_2 n = quey.remove();
            bfsearch.add(n);
            for (int i = 0; i < n.children.size(); i++) {
                quey.add(n.children.get(i));
            }

        }

        return bfsearch;

    }

    public AbstractAgent getPlayer() {
        return state.getAgent(currentplayer.current);
    }

    public ArrayList<Move> getFreeMoves() {

        //if (depth == 1)
            return move.getFreeMoves(this);
       // ArrayList<Move> free = move.getFreeMoves(this);
       // for (Move m: free){
       //     free.addAll(m.getFreeMoves());
       // }

    }


    public TurnOrder getTurn() {
        return currentplayer;
    }

    public double getMetricalDistanceIfPlayerToRoot(TurnOrder t) {
        if (this.getParent() == null) return 0;
        if (t == null) t = this.getTurn().clone();
        double distanceToParent = 0;
        if (t.current == getTurn().current) distanceToParent = this.move.getSkale();
        t.previousPlayer();
        return distanceToParent + this.getParent().getMetricalDistanceIfPlayerToRoot(t);
    }

    public void overwriteMove(Move move) {
        this.move = move;
    }

    public double getMaximalPossiblePoints() {
        return maxPoints;
    }
}
