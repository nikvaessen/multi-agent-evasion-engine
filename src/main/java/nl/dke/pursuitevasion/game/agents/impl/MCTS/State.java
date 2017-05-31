package nl.dke.pursuitevasion.game.agents.impl.MCTS;

import nl.dke.pursuitevasion.game.Engine;
import nl.dke.pursuitevasion.game.EngineConstants;
import nl.dke.pursuitevasion.game.Vector2D;
import nl.dke.pursuitevasion.game.agents.AbstractAgent;
import nl.dke.pursuitevasion.map.impl.Map;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Nibbla on 20.05.2017.
 */
public class State {
    ArrayList<AbstractAgent> pursuers; //= new ArrayList<>(3);
    ArrayList<AbstractAgent> evaders;// = new ArrayList<>(1);
    TurnOrder turnOrder;

    Map map;
    Engine e;

  //  StateHandler StateHandler = null;
    static public java.util.Map<StateHandler,StatePreCalcValue> allTheCalculations = new HashMap<>(6000);
    private double[] distances = null;
    boolean distancesCalculated = false;

    static State empty = new State();
    private StateHandler stateHandler;


    public static StatePreCalcValue Values(State s) {
        StateHandler sh = s.getStateHandler();
        StatePreCalcValue precalc = allTheCalculations.get(sh);
        if (precalc == null) precalc = Evaluator.calcValues(s,sh);
        allTheCalculations.put(sh, precalc);


        return precalc;
    }



    public State(Engine e, Map map, TurnOrder turnOrder, List<AbstractAgent> evaders, List<AbstractAgent> pursuers) {
        this.e = e;
        this.map = map;
        this.turnOrder = turnOrder;



        this.stateHandler = new StateHandler(this);
        this.evaders = new ArrayList<>(1);
        this.pursuers = new ArrayList<>(3);
        for (AbstractAgent p: pursuers){
            this.pursuers.add(p.copy());
        }
        for (AbstractAgent f: evaders){
            this.evaders.add(f.copy());
        }

    }

    public State(StateHandler stateHandler) {
        State s = stateHandler.getExampleState();
    }


    private State() {

    }


    public State clone(){
        State s = new State();
        s.map = map;
        s.e = e;
        s.distances = distances;
        s.distancesCalculated = distancesCalculated;
        s.pursuers = new ArrayList<>(3);
        s.evaders = new ArrayList<>(1);
        for (AbstractAgent p: pursuers){
            s.pursuers.add(p.copy());
        }
        for (AbstractAgent e: evaders){
            s.evaders.add(e.copy());
        }
        s.turnOrder = turnOrder.clone();
        s.stateHandler = stateHandler;
        return s;
    }

    public void executeMove(Move move) {
        AbstractAgent toMove = null;
        int id = move.getId();

        for (AbstractAgent p: pursuers){
            if (p.getId()== id) toMove = p;
        }
        for (AbstractAgent e: evaders){
            if (e.getId()== id) toMove = e;
        }
        if (toMove == null) return;
        Vector2D delta = toMove.getLocation().add(move.getDeltaX(),move.getDeltaY());
        toMove.getLocation().setX(delta.getX());
        toMove.getLocation().setY(delta.getY());
        distancesCalculated = false;

    }



    public AbstractAgent getAgent(int id) {
        for (AbstractAgent p: pursuers){
            if (p.getId()== id) return p;
        }
        for (AbstractAgent e: evaders){
            if (e.getId()== id) return e;
        }
        return null;
    }

    public boolean equals(State state){
        //TODO

        return false;
    }

    public double[] getDistances() {
        if (distancesCalculated) return distances;
        Vector2D evaderLoc = evaders.get(0).getLocation();
        double distance0 = pursuers.get(0).getLocation().distance(evaderLoc);
        double distance1 = pursuers.get(1).getLocation().distance(evaderLoc);
        double distance2 = pursuers.get(2).getLocation().distance(evaderLoc);
        double shortest = distance0; int i = 0;
        if (distance1<distance0) {i = 1; shortest = distance1;}
        if (distance2<shortest) {i = 2; shortest = distance2;}
        double skaleA = shortest/4;
        if (skaleA < EngineConstants.shortestMoveLength) skaleA=EngineConstants.shortestMoveLength;

        double[] distances = new double[6];
        distances[0]=distance0;
        distances[1]=distance1;
        distances[2]=distance2;
        distances[3]=shortest;
        distances[4]=skaleA;
        distances[5]=i;
        distancesCalculated = true;
        this.distances = distances;
        return this.distances;
    }

    public StateHandler getStateHandler() {
        if (stateHandler == null) stateHandler = new StateHandler(this);
        return stateHandler;
    }

    public static class StatePreCalcValue {
        private double pursuerScore;
        private double evaderScore;

        public StatePreCalcValue(double pursuerScore,double evaderScore) {
            this.evaderScore = evaderScore;
            this.pursuerScore = pursuerScore;
        }

        public double getPursuerScore() {
            return pursuerScore;
        }

        public double getEvaderScore() {
            return evaderScore;
        }

        public double getEvedorPossible() {
            return 0;
        }

        public double getPursuerPossible() {
            return 0;
        }
    }

    public static class StateHandler {
        private int hashCode=-1;
        //variables must be in range 0-100 for hashcoding //use screencords
        int p1x = 0;
        int p1y = 0;
        int p2x = 0;
        int p2y = 0;
        int p3x = 0;
        int p3y = 0;
        int ex = 0;
        int ey = 0;
        //int tick = 0;

        State s = null;
        private final double hashConstant;

        public int hashCode(){
            if (hashCode != -1) return hashCode;
            hashCode = calculateHashcode();
            return hashCode;
        }

        private int calculateHashcode() throws ArithmeticException{
            long hash= (long) (((long)p1x*p1y*p2x*p2y*p3x*p3y*ex*(long)ey)/hashConstant);
            int hash2 = (int) (hash/2);
            System.out.println("Generated Hash: " + hash2);
            return hash2;
        }

        public StateHandler(State s) {
            super();
            this.s = s;
            //setVariables
            hashConstant = 2500000;
        }


        public State getExampleState() {
            return s;//new State(StateHandler);
        }


    }



}
