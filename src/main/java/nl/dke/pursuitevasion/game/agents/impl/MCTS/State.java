package nl.dke.pursuitevasion.game.agents.impl.MCTS;

import nl.dke.pursuitevasion.game.Engine;
import nl.dke.pursuitevasion.game.EngineConstants;
import nl.dke.pursuitevasion.game.Vector2D;
import nl.dke.pursuitevasion.game.agents.AbstractAgent;
import nl.dke.pursuitevasion.map.impl.Map;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Nibbla on 20.05.2017.
 */
public class State {
    ArrayList<AbstractAgent> pursuers = new ArrayList<>(3);
    ArrayList<AbstractAgent> evaders = new ArrayList<>(1);
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

    private static StatePreCalcValue calcValues(State s, StateHandler sh) {
        return new StatePreCalcValue(s,sh);
    }

    public State(Engine e, Map map, TurnOrder turnOrder, ArrayList<AbstractAgent> evaders, ArrayList<AbstractAgent> pursuers) {
        this.e = e;
        this.map = map;
        this.turnOrder = turnOrder;
        this.evaders = evaders;
        this.pursuers = pursuers;

        this.stateHandler = new StateHandler(this);

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
        for (AbstractAgent p: pursuers){
            s.pursuers.add(p.clone());
        }
        for (AbstractAgent e: evaders){
            s.evaders.add(e.clone());
        }
        s.turnOrder = turnOrder.clone();
        s.stateHandler = stateHandler;
        return s;
    }

    public void executeMove(Move move) {
        AbstractAgent toMove = null;
        int id = move.getAgent().getId();

        for (AbstractAgent p: pursuers){
            if (p.getId()== id) toMove = p;
        }
        for (AbstractAgent e: evaders){
            if (e.getId()== id) toMove = e;
        }
        if (toMove == null) return;
        toMove.getLocation().add(move.getDeltaX(),move.getDeltaY());
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
        public StatePreCalcValue(State s, StateHandler sh) {

        }
    }

    public static class StateHandler {
        public StateHandler(State s) {
            super();
        }


        public State getExampleState() {
            return null;//new State(StateHandler);
        }
    }



}
