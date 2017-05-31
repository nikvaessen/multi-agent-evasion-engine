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
    private int shortestDistance; //special storing of save file
    private double distance1;
    private double distance2;
    private double distance0;
    private  ArrayList<Move> moves;
    private ArrayList<Move> freeMoves;
    private  AbstractAgent agent;
    private double skale = 1;
    private Angle angle;
    private double deltaX;
    private double deltaY;
    private double finalX;
    private double finalY;

    static HashMap<Move,double[]> moveHashMap = new HashMap<>(2000000,2);


    public double getDeltaX() {
        return deltaX;
    }

    public double getDeltaY() {
        return deltaY;
    }

    public Move(AbstractAgent agent, Angle angle, double skale) {
        this.angle = angle;
        this.skale = skale;
        this.agent = agent;

        double[] val;
        val = moveHashMap.get(this);
        if (val ==null){
            val = new double[2];
            val[0] =  Math.cos(angle.getRadians())* skale ;
            val[1] =  Math.sin(angle.getRadians())* skale ;
            moveHashMap.put(this,val);
        }
        deltaX = val[0];
        deltaY = val[1];
        finalX = deltaX + agent.getLocation().getX();
        finalY = deltaY + agent.getLocation().getY();

    }


    public Move(AbstractAgent agent,Byte b){
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
    }

    public double getSkale() {
        return skale;
    }

    public Angle getAngle() {
        return angle;
    }

    @Override
    public String toString() {
        return "Angle: " + angle.toString() + " Skale: " + skale;
    }



    public ArrayList<Move> getMoves(AbstractAgent player, State state) {
       if (moves != null) return moves;
         //prepare Moves and Store Them state.getStateHandler();
        double skale = 0;

        ArrayList<AbstractAgent> pursuer = state.pursuers;
        AbstractAgent evader = state.evaders.get(0);


        double[] distances = state.getDistances();



        moves = new ArrayList<Move>();
        if (player.isEvader()) {
            //add 16 moves, one for every direction. The length is a fourth of the distance to the closest pursuer

            Angle a = new Angle(evader,pursuer.get((int)distances[5]));
            double OverSixteen = 1./16.*360;
            for (int j = 0; j < 16; j++) {

                Angle b = a.clone(); a.rotate((int) OverSixteen);


                Move n = new Move(evader,b,distances[4]);
                moves.add(n);
            }
        }
        if (!player.isEvader()){
            //add 16 moves, one for every direction. The length is a fourth of the distance of evader to closest pursuer,
            //with an minimum distance of speed*timeframe and direction 0 is the direct angle closing in to the evader
            Angle a = new Angle(evader,pursuer.get((int)distances[5]));
            double OverSixteen = 1./16.*360;

            for (int j = 0; j < 16; j++) {
                Angle b = a.clone(); a.rotate((int) OverSixteen);
                Move n = new Move(player,b,distances[4]);

                moves.add(n);
            }

        }
        for (int i = moves.size()-1; i >= 0 ; i--) {
            Move m = moves.get(i);
            if (!m.isLegal(state.map)) {
                moves.remove(m);
            }
        }

        return moves;
    }

    public ArrayList<Move> getFreeMoves(NodeTree_2 node) {
        if (freeMoves != null) return freeMoves;

        State s = node.getState();



        freeMoves = new ArrayList<>();
        ArrayList<Move> allMoves = getMoves(node.getPlayer(),s);
        for (int i = 0; i < allMoves.size(); i++) {
            freeMoves.add(allMoves.get(i));
        }





        return freeMoves;
    }

    /**
     *  bad legality check
     * @param map
     * @return
     */
    private boolean isLegal(Map map) {
        Vector2D v = new Vector2D(this.agent.getLocation());
        v = v.add(this.getDeltaX(),this.getDeltaY());
        if (map.isInsideAndLegal(v)) return true;
        return false;
    }


    public Vector2D getStartLocation() {
        return agent.getLocation();
    }

    public int getId() {
        return agent.getId();
    }

    public Vector2D getEndLocation() {
        return new Vector2D(finalX,finalY);
    }
}
