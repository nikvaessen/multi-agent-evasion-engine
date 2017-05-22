package nl.dke.pursuitevasion.game.agents.impl.MCTS;

import nl.dke.pursuitevasion.game.EngineConstants;
import nl.dke.pursuitevasion.game.agents.AbstractAgent;
import nl.dke.pursuitevasion.game.agents.Angle;

import java.util.ArrayList;

/**
 * Created by giogio on 1/13/17.
 */
public class Move {
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

    private Move(AbstractAgent agent, Angle angle, double skale){
        this.angle = angle;
        this.skale = skale;
        this.agent = agent;
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

    public static ArrayList<Move> getMoves(AbstractAgent player) {

        return null;
    }

    public static ArrayList<Move> getFreeMoves(NodeTree_2 node) {
        return null;
    }
}
