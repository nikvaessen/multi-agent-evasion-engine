package nl.dke.pursuitevasion.game.agents.impl.MCTS;

import nl.dke.pursuitevasion.game.agents.AbstractAgent;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Nibbla on 21.05.2017.
 *
 * sets the order in wich player take their turns, even though their simultanious, their calculation is not.
 */
public class TurnOrder {
    LinkedList<Integer> turnOrder = new LinkedList<>();
    int current = 0;

    public TurnOrder(List<AbstractAgent> pursuers, List<AbstractAgent> evaders){
        turnOrder.add(evaders.get(0).getId());
        for (int i = 0; i < pursuers.size(); i++) {
            turnOrder.add(pursuers.get(i).getId());
        }

    }

    private TurnOrder() {

    }

    public int getIDCurrent(){
        return turnOrder.get(current);
    }
    public int getIDNext(){
        return turnOrder.get(current+1%turnOrder.size());
    }
    public int getIDLast(){
        int i = current-1;
        if (i<0) i = turnOrder.size()-1;
        return turnOrder.get(i);
    }

    public void nextPlayer(){
        current = current+1%turnOrder.size();
    }
    public void previousPlayer(){
        current = current-1;
        if (current<0) current = turnOrder.size()-1;
    }

    public TurnOrder clone(){
        TurnOrder t = new TurnOrder();
        t.current = current;
        for (int i = 0; i < turnOrder.size(); i++) {
            t.turnOrder.add(turnOrder.get(i));
        }
        return t;
    }
}
