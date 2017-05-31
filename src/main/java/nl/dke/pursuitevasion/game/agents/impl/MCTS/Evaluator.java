package nl.dke.pursuitevasion.game.agents.impl.MCTS;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Nibbla on 29.05.2017.
 */
public class Evaluator {
    Map<State.StateHandler,State.StatePreCalcValue> t = new HashMap<>(100000);

    public static State.StatePreCalcValue calcValues(State s, State.StateHandler sh) {
        double[] simpleDistance = s.getDistances();
       /* distances[0]=distance0;
        distances[1]=distance1;
        distances[2]=distance2;
        distances[3]=shortest;
        distances[4]=skaleA;
        distances[5]=i; */

        State.StatePreCalcValue spcv = new State.StatePreCalcValue(Math.abs(1000-simpleDistance[3])/1000,simpleDistance[3]);
        return spcv;


    }
}
