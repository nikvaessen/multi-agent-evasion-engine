package nl.dke.pursuitevasion.game.agents.impl.MCTS;

import nl.dke.pursuitevasion.game.EngineConstants;
import nl.dke.pursuitevasion.game.Vector2D;
import nl.dke.pursuitevasion.game.agents.AbstractAgent;
import nl.dke.pursuitevasion.game.agents.Angle;
import nl.dke.pursuitevasion.game.agents.Direction;
import nl.dke.pursuitevasion.map.impl.Map;

import java.util.ArrayList;
import java.util.Collections;

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




    public double getDeltaX() {
        return Math.acos(angle.getRadians())* skale * EngineConstants.WALKING_SPEED;
    }

    public double getDeltaY() {
        return Math.asin(angle.getRadians())* skale * EngineConstants.WALKING_SPEED;
    }

    public Move(AbstractAgent agent, Angle angle, double skale, double distance0, double distance1, double distance2, int shortestDistance) {
        this.angle = angle;
        this.skale = skale;
        this.agent = agent;
        this.distance0 = distance0;
        this.distance1 = distance1;
        this.distance2 = distance2;
        this.shortestDistance = shortestDistance;

    }


    public Move(AbstractAgent agent,Byte b){
        double skale = b%16+1 ;
        double angle = b/16*360;

        this.agent = agent;
        this.skale = skale;
        this.angle = new Angle(angle);
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

    public AbstractAgent getAgent() {
        return agent;
    }

    public ArrayList<Move> getMoves(AbstractAgent player, State state) {
       if (moves != null) return moves;
        double skale = 0;

        ArrayList<AbstractAgent> pursuer = state.pursuers;
        AbstractAgent evader = state.evaders.get(0);
        Vector2D evaderLoc = evader.getLocation();

        double distance0 = pursuer.get(0).getLocation().distance(evaderLoc);
        double distance1 = pursuer.get(1).getLocation().distance(evaderLoc);
        double distance2 = pursuer.get(2).getLocation().distance(evaderLoc);
        double shortest = distance0; int i = 0;
        if (distance1<distance0) {i = 1; shortest = distance1;}
        if (distance2<shortest) {i = 2; shortest = distance2;}
        double skaleA = shortest/4;
        if (skaleA < EngineConstants.shortestMoveLength) skaleA=EngineConstants.shortestMoveLength;

        moves = new ArrayList<Move>();
        if (player.isEvader()) {
            //add 16 moves, one for every direction. The length is a fourth of the distance to the closest pursuer

            Angle a = new Angle(evader,pursuer.get(i));
            double OverSixteen = 1./16.;
            for (int j = 0; j < 16; j++) {

                Angle b = a.clone(); a.rotate((int) OverSixteen);


                Move n = new Move(evader,b,skaleA,distance0,distance1,distance2,i);
                moves.add(n);
            }
        }
        if (!player.isEvader()){
            //add 16 moves, one for every direction. The length is a fourth of the distance of evader to closest pursuer,
            //with an minimum distance of speed*timeframe and direction 0 is the direct angle closing in to the evader
            Angle a = new Angle(player,evader);
            Move n = new Move(player,a,skaleA);
            Move n = new Move(evader,b,skaleA,distance0,distance1,distance2,i);
            moves.add(n);
        }
        for (Move m: moves) {
            if (!m.isLegal(state.map)) {
                moves.remove(m);
            }
        }
        return moves;
    }

    public ArrayList<Move> getFreeMoves(NodeTree_2 node) {

        if (freeMoves != null) return freeMoves;

        freeMoves = new ArrayList<>();
        ArrayList<Move> allMoves = getMoves(node.getPlayer(),node.getState());
        Collections.copy(allMoves,freeMoves);




        return freeMoves;
    }

    /**
     *  bad legality check
     * @param map
     * @return
     */
    private boolean isLegal(Map map) {
        Vector2D v = new Vector2D(this.agent.getLocation());
        v.add(this.getDeltaX(),this.getDeltaY());
        if (map.isInsideAndLegal(v)) return true;
        return false;
    }


}
