package nl.dke.pursuitevasion.game.agents.impl.MCTS;

import nl.dke.pursuitevasion.game.Engine;
import nl.dke.pursuitevasion.game.Vector2D;
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

    HistoryNode historyNode;


    public State(Engine e, Map map, TurnOrder turnOrder, ArrayList<AbstractAgent> evaders, ArrayList<AbstractAgent> pursuers) {
        this.e = e;
        this.map = map;
        this.turnOrder = turnOrder;
        this.evaders = evaders;
        this.pursuers = pursuers;

        prepareMaprepresentation(map);
    }

    private void prepareMaprepresentation(Map map) {
        historyNode = new HistoryNode(600);
        HistoryNode.getDistance(HistoryNode,HistoryNode);
    }


    private State() {

    }


    public State clone(){
        State s = new State();
        s.map = map;
        s.e = e;

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

    public double[] evaluate(TurnOrder turn) {

        //sum of square distances... the smaller the better
        if (turn.isEvader()){
            Vector2D distance1
            Vector2D distance1
            Vector2D distance1
        }
        return 3.4;
        return new double[0];
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

    private class HistoryNode {
        //an t->x,y memory
        ArrayList<String> history = new ArrayList<>(600);
        ArrayList<Vector2D> history2 = new ArrayList<>(600);
        ArrayList<Vector2D> history3 = new ArrayList<>(600);
        ArrayList<Vector2D> history4 = new ArrayList<>(600);


        public HistoryNode(int width, int height) {

        }

        public HistoryNode(int expectedSeconds) {

        }

        public void update(AbstractAgent a){

        }
    }
}
