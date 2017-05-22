package nl.dke.pursuitevasion.game.agents.impl.MCTS;

import nl.dke.pursuitevasion.game.Engine;
import nl.dke.pursuitevasion.game.agents.AbstractAgent;
import nl.dke.pursuitevasion.map.impl.Map;

import java.util.ArrayList;

/**
 * Created by Nibbla on 20.05.2017.
 */
public class State {
    ArrayList<AbstractAgent> pursuers = new ArrayList<>(3);
    ArrayList<AbstractAgent> evaders = new ArrayList<>(1);
    TurnOrder turnOrder;

    Map map;
    Engine e;


    public State clone(){
        State s = new State();
        s.pursuers = new ArrayList<>(3);
        for (AbstractAgent p: pursuers){
            s.pursuers.add(p.clone());
        }
        for (AbstractAgent e: evaders){
            s.pursuers.add(e.clone());
        }
        s.turnOrder = turnOrder.clone();
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


    }

    public void evaluate() {

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
}
