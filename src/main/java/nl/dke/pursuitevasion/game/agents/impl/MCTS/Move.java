package nl.dke.pursuitevasion.game.agents.impl.MCTS;

import nl.dke.pursuitevasion.game.EngineConstants;
import nl.dke.pursuitevasion.game.Vector2D;
import nl.dke.pursuitevasion.game.agents.AbstractAgent;
import nl.dke.pursuitevasion.game.agents.Angle;
import nl.dke.pursuitevasion.game.agents.Direction;
import nl.dke.pursuitevasion.map.impl.Map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by giogio on 1/13/17.
 */
public class Move {
    private final int id;
    private int shortestDistance; //special storing of save file
    private double distance1;
    private double distance2;
    private double distance0;
    private  ArrayList<Move> moves;
    private ArrayList<Move> freeMoves;// = new ArrayList<>(16);
    private  AbstractAgent agent;
    private double skale = 1;
    private Angle angle;
    private double deltaX;
    private double deltaY;
    private double finalX;
    private double finalY;


    static HashMap<Move,double[]> moveHashMap = new HashMap<>(2000000,2);
    private State afterState;


    public double getDeltaX() {
        return deltaX;
    }

    public double getDeltaY() {
        return deltaY;
    }

    public Move(AbstractAgent agent, Angle angle, double skale,State previousState) {
        this.angle = angle.clone();
        this.skale = skale;
        //this.skale = 10;
        this.agent = agent.copy();
         id =   agent.getId();
        double[] val = null;
       // val = moveHashMap.get(this);
        if (val ==null){
            val = new double[2];
            val[0] =  Math.cos(angle.getRadians())* this.skale ;
            val[1] =  Math.sin(angle.getRadians())* this.skale ;
            //moveHashMap.put(this,val);
        }
        deltaX = val[0];
        deltaY = val[1];
        finalX = deltaX + agent.getLocation().getX();
        finalY = deltaY + agent.getLocation().getY();
        State after = previousState.clone();
        after.executeMove(this);
        afterState = after;
    }


   /* public Move(AbstractAgent agent,Byte b){
        double skale = b%16+1 ;
        double angle = b/16*360;

        this.agent = agent;
        this.skale = skale;
        this.angle = new Angle(angle);
        double[] val = null;
       // val = moveHashMap.get(this);
        if (val ==null){
            val = new double[2];
            val[0] =  Math.acos(this.angle.getRadians())* skale * EngineConstants.WALKING_SPEED * EngineConstants.TIMECONSTANT;
            val[1] =  Math.asin(this.angle.getRadians())* skale * EngineConstants.WALKING_SPEED * EngineConstants.TIMECONSTANT;
           // moveHashMap.put(this,val);
        }
        deltaX = val[0];
        deltaY = val[1];
    }*/

    public double getSkale() {
        return skale;
    }

    public Angle getAngle() {
        return angle;
    }

    @Override
    public String toString() {
        return agent.getLocation() + " " + finalX + " " + finalY + "Angle: " + angle.toString() + " Skale: " + skale;
    }



    private ArrayList<Move> getMoves(AbstractAgent player, State state) {
       if (moves != null) return moves;
         //prepare Moves and Store Them state.getStateHandler();
        double skale = 0;

        ArrayList<AbstractAgent> pursuer = state.pursuers;
        AbstractAgent evader = state.evaders.get(0);


        double[] distances = state.getDistances();

        int count =0;
        ArrayList<Move> moves= new ArrayList<Move>();

        boolean c = false;
        if (player.isEvader()) {
            //add 16 moves, one for every direction. The length is a fourth of the distance to the closest pursuer
            c = !c;
            Angle a = new Angle(evader,pursuer.get((int)distances[5]));
            double OverSixteen = 1./16.*360;
            for (int j = 0; j < 16; j++) {
                count++;
                Angle b = a.clone(); a.rotate((int) OverSixteen);


                Move n = new Move(evader,b,distances[4],state);
               // System.out.println(moves);
                moves.add(n);
               // System.out.println("count"+count +" "+ moves.size());
            }
        }
        if (!player.isEvader()){
            c = !c;

            //add 16 moves, one for every direction. The length is a fourth of the distance of evader to closest pursuer,
            //with an minimum distance of speed*timeframe and direction 0 is the direct angle closing in to the evader
            Angle a = new Angle(evader,pursuer.get((int)distances[5]));
            double OverSixteen = 1./16.*360;

            for (int j = 0; j < 16; j++) {
                count++;
                Angle b = a.clone(); a.rotate((int) OverSixteen);
                Move n = new Move(player,b,distances[4],state);

                moves.add(n);
               // System.out.println("count"+count + " "+moves.size());
            }

        }
        if (c==false) System.out.println("hopala");
         //System.out.println("checkinglegality of moves" + moves.size() );
        int start = moves.size()-1;
        for (int i = start; i >= 0 ; i--) {
           // System.out.println(moves.size() + " "  + i);
            Move m = moves.get(i);
            if (!m.isLegal(state.map)) {
                moves.remove(m);
            }
        }
        this.moves = moves;
        return moves;
    }

    public ArrayList<Move> getFreeMoves(NodeTree_2 node) {
        if (freeMoves!=null) return freeMoves;

        State s = node.getState();

        ArrayList<Move> freeMoves2 = new ArrayList<>(16);

        //freeMoves = new ArrayList<>();
        try {
            ArrayList<Move> allMoves = (ArrayList<Move>) getMoves(node.getPlayer(),s).clone();
            for (int i = 0; i < allMoves.size(); i++) {
                freeMoves2.add(allMoves.get(i));
            }
        }catch (NullPointerException nl) {
            nl.printStackTrace();
        }

        freeMoves = freeMoves2;
        return freeMoves;
    }

    /**
     *  bad legality check
     * @param map
     * @return
     */
    private boolean isLegal(Map map) {
        Vector2D v = new Vector2D(finalX,finalY);
        Vector2D v3 = new Vector2D(finalX-deltaX*7./8.,finalY-deltaY*7./8.);
        Vector2D v2 = new Vector2D(finalX-deltaX *2./4.,finalY-deltaY*2./4.);
        if (map.isInsideAndLegal(v)){
            if (map.isInsideAndLegal(v2)){
                if (map.isInsideAndLegal(v3)){
                    return true;
                }

            }
        }

        return false;
    }


    public Vector2D getStartLocation() {
        return agent.getLocation();
    }

    public int getId() {
        return  id;
    }

    public Vector2D getEndLocation() {
        return new Vector2D(finalX,finalY);
    }

    public State getAfterState() {

        return afterState;
    }
}
