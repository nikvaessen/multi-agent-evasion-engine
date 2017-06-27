package nl.dke.pursuitevasion.game.agents.impl.MCTS;


import nl.dke.pursuitevasion.game.Vector2D;
import nl.dke.pursuitevasion.game.agents.AbstractAgent;
import nl.dke.pursuitevasion.game.agents.Angle;
import nl.dke.pursuitevasion.gui.simulator.MapViewPanel;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

/**
 * Created by giogio,Nibbla on 1/21/17.
 *
 */
public class MCTS_2 implements Strategy{
    private final static Logger log = Logger.getLogger( MCTS_2.class.getName() );
    private final int depthLevel;
    private static PreCalcMap preCalcMap;

    private boolean extensionStrategy;
    private TurnOrder ally;

    private State realState;
    private int maxtTime, n_expansion;



    private NodeTree_2 root;

    //public static NodeTree_2 getLastFinalSelectedMove() {
    //    return lastFinalSelectedMove;
   // }

   // private static NodeTree_2 lastFinalSelectedMove;

    int[][] blueWinboard;
    int[][] bluePlayboard;

    int[][] redWinboard;
    int[][] redPlayboard;

    public static MCTS_2 getLastMCTS() {
        return lastMCTS;
    }

    private static MCTS_2 lastMCTS;


    public MCTS_2(State realState, TurnOrder firstPlayer, long maxCalcTimeMilliSeconds, int depthLevel, boolean ExtensionStrategy, PreCalcMap preCalcMap){
        this.depthLevel = depthLevel;
        this.realState = realState;
        this.ally = firstPlayer;

        this.maxtTime = (int) maxCalcTimeMilliSeconds;
        this.extensionStrategy = ExtensionStrategy;
        this.preCalcMap =preCalcMap;




        /*int s = realState.getSize();

        this.blueWinboard = new int[s][s];
        this.bluePlayboard = new int[s][s];
        this.redWinboard = new int[s][s];
        this.redPlayboard = new int[s][s];
        */


    }



    public NodeTree_2 getRoot() {
        return root;
    }
    public Move start(){

       setNewRoot(realState.turnOrder);

        double startTime = System.currentTimeMillis();
        n_expansion = 0;
        if (depthLevel==1){
            monteCarloSearch(root,startTime);
        }else {
            while (System.currentTimeMillis() - startTime < maxtTime) {
                expansion(selection(root));
            }
        }
        //printTree(root);
       System.out.println("Expansions: "+n_expansion);
        NodeTree_2 m = null;
        if (depthLevel==10){ m = getBestValue();}else
        {m = getBestMove();}

        lastMCTS = this;
        return m.getMove();

    }

    private void printTree(NodeTree_2 root) {

        System.out.println(" WINS/TOTAL: "+root.getWins()+"/"+root.getGames());
        printChild(root);
        System.out.println("Root: "+root.getGames());
    }

    private void printChild(NodeTree_2 leaf) {
        for(NodeTree_2 nodeTree: leaf.getChildren()) {
            for (int i = 0; i < nodeTree.getDepth(); i++) {
                System.out.print("-- ");
            }
            System.out.println(" WINS/TOTAL: "+nodeTree.getWins()+"/"+nodeTree.getGames());
            if(nodeTree.getChildren().size()>0){
                printChild(nodeTree);
            }
        }
    }

    private void monteCarloSearch(NodeTree_2 root, double startTime) {


        ArrayList<Move> moves = root.getFreeMoves();
        for (int i = 0; i < moves.size(); i++) {
            NodeTree_2 newNode = new NodeTree_2(root);
            newNode.setMove(moves.get(i));
            n_expansion++;
        }

        int n = root.getChildren().size();
        while (System.currentTimeMillis() - startTime < maxtTime) {
            for (int i = 0; i < n; i++) {
                n_expansion++;
                simulateQuick(root.getChildren().get(i));
                root.getChildren().get(i).incrementGame();
            }
        }

    }


    @Deprecated
    private Move startQuick() {
        //setNewRoot(new TurnOrder());
        double startTime = System.currentTimeMillis();
        n_expansion = 0;
        while (System.currentTimeMillis() - startTime <maxtTime){
            expansion(selection(root));

        }
        System.out.println("Expansions: "+n_expansion);

        NodeTree_2 m = getMostSimulations();
        NodeTree_2 m2 = getBestValue();

        System.out.println("Moves to choose:");
        System.out.println(m);
        System.out.println(m2);

        while (m == null){
            expansion(selection(root));
            m = getMostSimulations();
        }

        return m.getMove();

    }


    public NodeTree_2 getMostSimulations(){
        NodeTree_2 bestNode = null;
        double bestValue = -999999999;
        for(NodeTree_2 child: root.getChildren()){

            double value = child.getGames();
            if(value>bestValue) {
                bestNode = child;
                bestValue = value;
            }
        }
       // lastFinalSelectedMove = bestNode;
        return bestNode;
    }

    public NodeTree_2 getBestValue() {
        NodeTree_2 bestNode = null;
        double bestValue = -999999999;
       // System.out.println("gettingthe best for evader?" + ally.isEvader());

        for(NodeTree_2 child: root.getChildren()){

            double value = child.getWins()/(child.getMaximalPossiblePoints()*1.0);
           // System.out.println(child.getMove());
           // System.out.println("   " + value );
            if(value>bestValue) {
                bestNode = child;
                bestValue = value;
            }
        }
        //lastFinalSelectedMove = bestNode;
        return bestNode;
    }

    /**
     * checked
     * @return
     */
    public NodeTree_2 getBestMove(){
        NodeTree_2 bestNode = null;
        double bestValue = -999999999;
        ArrayList<NodeTree_2> ddd = root.getChildren();
        System.out.println("info");
        int i =0;
        for(NodeTree_2 child: ddd){
            System.out.println(i++);
          //  if(child.isWinningMove() || child.isLosingMove()){
          //      lastFinalSelectedMove = child;
          //      return child ;
          //  }
            if (child == null){
                System.out.print("eeer");
            }
            double value = child.getGames();
            if(value>bestValue) {
                bestNode = child;
                bestValue = value;
            }
        }
        System.out.println("infoout");
      //  lastFinalSelectedMove = bestNode;
        return bestNode;
    }

    public NodeTree_2 selection(NodeTree_2 node){


        if( node.getFreeMoves().size()>0) //TODO check that all moves are looked at.
            return node;

        NodeTree_2 bestNode = node;
        double bestValue = -999999999;
        for(NodeTree_2 child: node.getChildren()){

            double value;
           // if (depthLevel==2) value = rave(child);
            value = UCB1(child);
            //log.info(child.toString() + " UCB1: " + value);
            if(value>bestValue) {
                bestNode = child;
                bestValue = value;
            }
        }
        return selection(bestNode);
    }
/*
    private double rave(NodeTree_2 node) {
        Move m = node.getMove();
        int y = m.getY();
        int x = m.getX();

        int[][] wins= redWinboard; //= new int[0][];
        int[][] plays= redPlayboard;// = new int[0][];
        StatusCell color = node.isEvader();

        if (color==StatusCell.Blue) {
            wins = blueWinboard;
            plays = bluePlayboard;
        } else if (color==StatusCell.Red) {
            wins = redWinboard;
            plays = redPlayboard;
        } else if (color==StatusCell.Empty) return 0;
        if (ally ==StatusCell.Empty) return 0;

        float vi = (float) node.getWins() / node.getGames();
        float vi2 = (float) wins[y][x] / plays[y][x];


        double beta = betaFunction(node.getGames(),plays[y][x]);
        int np = node.getGames();
        int ni = node.getParent().getGames();
        float a = 0.85f;
        double C = Math.sqrt(0.04);
       // if(vi>a)
        //    C = 0;
        //System.out.println("1-beta * :" + vi + " beta*: " + vi2 + " val: " +  C * Math.sqrt(Math.log(ni)/np));

        return (1-beta)*vi+beta*vi2 + C * Math.sqrt(Math.log(ni)/np);


    }
    */

    private double betaFunction(int games, int raveGames) {
        double nominator = raveGames;
        double denominator = games+raveGames+4*0.12*games*raveGames;
        double val = nominator/denominator;
        System.out.println("games:" + games + " raveGames: " + raveGames + " val: " + val);

        return val;
    }

    /**
     * resets the root of the monte carlo search tree, after a move has taken.
     * For this to work, the executed move is compared with all child moves of the last selected one.
     * I suppose it works like this, as the active player has always two steps.
     */
    public void setNewRoot(TurnOrder t){
        if(lastMCTS!=null){
//            State.StateHandler sh = lastMCTS.getBestMove().getState().getStateHandler();

        }
       /* if(false &&(lastFinalSelectedMove !=null||ally.getIDCurrent()!=t.getIDCurrent())) {
            //if the mcst has been used before, it now has to be advanced to the new state.
            //the code right now appears to call set New Root again, and by that deletes the factual tree?!?!?!?!
            State copy = lastFinalSelectedMove.getState().clone();

            for (NodeTree_2 child : lastFinalSelectedMove.getChildren()) {
                Move move = child.getMove();
                copy.executeMove(move);
                if (copy.equals(realState)) {
                    root = child;
                    root.setParent(null);
                    root.setLosingMove(false);
                    System.out.println("USED________");
                    return;
                }
                copy = lastFinalSelectedMove.getState().clone();
            }
            lastFinalSelectedMove = null;
            t.nextPlayer();
            setNewRoot(t);
        }else {*/
            //
           // if(root == null) {
                root = new NodeTree_2(null);
                root.setState(realState);
                //root.setTurn(t);
                TurnOrder to = t.clone();to.previousPlayer();
                root.setTurn(to);
                root.overwriteMove(new Move(root.getPlayer(),new Angle(0),0,realState));
           // }

            root.setState(realState.clone());
       // }
    }

    public void expansion(NodeTree_2 node){
        if (node == null) log.severe("Expansion With null node");

        if (extensionStrategy == false) standardExpansion(node);
        else specialExpansion(node);



    }

    private void specialExpansion(NodeTree_2 node) {

        ArrayList<Move> moves =  node.getFreeMoves();
        NodeTree_2 newNode = new NodeTree_2(node);
       // ExtensionStrategy es = new ExtensionStrategy(moves,node.getState(),ally);


       // newNode.setMove(es.getSuggestedMove(moves));
        n_expansion++;
        if (!newNode.isWinningMove() && !newNode.isLosingMove()){
            simulateQuick(newNode);
        }
        newNode.incrementGame();
    }

    private void standardExpansion(NodeTree_2 node) {

        //if(!node.isWinningMove() && !node.isLosingMove()) {
            ArrayList<Move> moves = node.getFreeMoves();
            NodeTree_2 newNode = new NodeTree_2(node);
            Move move = moves.remove((int) (Math.random() * moves.size()));

            newNode.setMove(move);

            n_expansion++;
           // if (!newNode.isWinningMove() && !newNode.isLosingMove() ){
                if (depthLevel==2) simulateQuick(newNode); else
                simulateQuick(newNode);
            //}else {
                   // if(newNode.isEvader() == ally){
              //          if(newNode.isWinningMove())
             //               newNode.incrementWin(1,true);

              //          else
              //              newNode.incrementWin(1, true);
               //     }else {
               //         if (newNode.isWinningMove())
                //            newNode.incrementWin(1, false);
                 //       else
                 //           newNode.incrementWin(1, false);
                 //   }

           // }
            newNode.incrementGame();
        /**}else{
            if(!node.isDeadCell()) {
                if(node.isEvader() == ally){
                    if(node.isWinningMove())
                        node.incrementWin(1, true);
                    else
                        node.incrementWin(1, true);
                }else {
                    if (node.isWinningMove())
                        node.incrementWin(1, false);

                    else
                        node.incrementWin(1, false);

                }
            }
            node.incrementGame();
        }*/
    }



    public void simulateQuick(NodeTree_2 node){
        TurnOrder current;

        //execute a full turn order

        //check if finished
        TurnOrder to = node.getTurn();
        //repeat until 4 seconds are full
        //node.getState().evaluate(to);
        State.StatePreCalcValue value = Evaluator.calcValues(node.getState(),node.getState().getStateHandler());
       // State.StatePreCalcValue value = preCalcMap.getValueOfEvadorValMap(node);
       // System.out.println("Evaluated State: " + node.getPlayer().getLocation().toString() + " with:"+ value.getEvaderScore());
        node.incrementWin(value.getEvaderScore(),value.getPursuerScore(),value.getEvedorPossible(), value.getPursuerPossible(),to);

        /*if(node.isEvader() == StatusCell.Blue)
            current = StatusCell.Red;
        else
            current = StatusCell.Blue;

        State copy = node.getState().clone();
        ArrayList<Move> freeMoves =  copy.getFreeMoves();

        int n_moves = freeMoves.size();
        for(int i=0;i<n_moves/2;i++){
            Move move = freeMoves.remove((int)(Math.random()*freeMoves.size()));
            copy.putStone(move.getX(),move.getY(),current);
        }

        if(current == StatusCell.Blue)
            current = StatusCell.Red;
        else
            current = StatusCell.Blue;

        for(Move move: freeMoves){
            copy.putStone(move.getX(),move.getY(),current);
        }

        if(copy.hasWon(ally)) {

            //rave(copy, ally, true);
            //rave(copy,enemy,false);
        }
        else {
            node.incrementWin(1, node.isEvader()!=ally);
            //rave(copy, ally, false);
            //rave(copy,enemy,true);
        }
        */
    }

    /*
    public void simulateRave(NodeTree_2 node){
        StatusCell current;






        if(node.isEvader() == StatusCell.Blue)
            current = StatusCell.Red;
        else
            current = StatusCell.Blue;



        Board copy = node.getState().getCopy();
        ArrayList<Move> freeMoves = copy.getFreeMoves();

        if(copy.hasWon(ally)) {
            node.incrementWin(1, node.isEvader()==ally);
            rave(copy, ally, true);
            rave(copy,enemy,false);
            return;
        } else if (copy.hasWon(enemy)){
            node.incrementWin(1, node.isEvader()!=ally);
            rave(copy, ally, false);
            rave(copy,enemy,true);
            return;
        }

        while (true){
            Move move = freeMoves.remove((int)(Math.random()*freeMoves.size()));
            copy.putStone(move.getX(),move.getY(),current);

            if(copy.hasWon(ally)) {
                node.incrementWin(1, node.isEvader()==ally);
                rave(copy, ally, true);
                rave(copy,enemy,false);
                return;
            } else if (copy.hasWon(enemy)){
                node.incrementWin(1, node.isEvader()!=ally);
                rave(copy, ally, false);
                rave(copy,enemy,true);
                return;
            }

        }

    }
*/
/*
    private void rave(Board copy, StatusCell ally, boolean b) {
        Cell[][] cells = copy.getGrid();
        int s = copy.getSize();
        int[][] wins = new int[0][];
        int[][] plays = new int[0][];

        if (ally==StatusCell.Blue) {
            wins = blueWinboard;
            plays = bluePlayboard;
        }
        if (ally==StatusCell.Red) {
            wins = redWinboard;
            plays = redPlayboard;
        }
        if (ally ==StatusCell.Empty) return;


        for (int i = 0; i < s; i++) {
            for (int j = 0; j < s; j++) {
                if (cells[i][j].getStatus() == ally) {
                    if (b) {
                        wins[i][j]++;
                    }
                        plays[i][j]++;
                }
            }
        }
    }
*/

    public double UCB1(NodeTree_2 node){

        float vi = (float) (node.getWins() / node.getMaximalPossiblePoints());
        double np = node.getGames();
        double ni = node.getParent().getGames();
        float a = 0.65f;
        double C = Math.sqrt(0.14);
       // if(vi>a)
          //C = 0;

        return vi + C * Math.sqrt(Math.log(ni)/np);
    }


    @Override
    public Move getMove() {
        return start();
    }

    @Override
    public NodeTree_2 getRootTreeMcts() {
        return root;
    }

    @Override
    public void resetTree() {
        //lastFinalSelectedMove = null;
        System.out.println("DOOONE ___________________________");
    }

    @Override
    public void updateState(State state) {
        realState = state;
    }

    public void paint(Graphics g, MCTSViewSettings mctsViewSettings, MapViewPanel mapViewPanel, Collection<AbstractAgent> agents) {
        PreCalcMap p = this.preCalcMap;
        p.draw(g, mctsViewSettings, mapViewPanel,agents);

        NodeTree_2 root = this.getRoot();
        if (root == null) return;
        int height = root.getHeight();
        LayerView lv = new LayerView(root,this,mapViewPanel.getPreferredSize(),mctsViewSettings);

        //lv.paintComponent(g);
        lv.paintOverView(g,mapViewPanel);

        g.setColor(Color.magenta);




    }

    public void click(MouseEvent e) {
        PreCalcMap p = this.preCalcMap;
        Point pp = p.ScreenPosToXY(e.getPoint());
       // int x = pp[0];
       // int y = pp[1];
       // p.generateSink(pp);

    }




    public static class MCTSViewSettings {
        public MCTSViewSettings(){

        }
    }
}
