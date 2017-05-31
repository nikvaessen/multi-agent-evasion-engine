package nl.dke.pursuitevasion.game.agents.impl.MCTS;

import java.util.*;

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

        double diagonal = 2000;

        double valuePursuer = diagonal - ((0 * simpleDistance[(int) simpleDistance[5]] + 1 * simpleDistance[3] + 0.0 * (simpleDistance[0] + simpleDistance[1] + simpleDistance[2])));
         valuePursuer = valuePursuer/diagonal;
        double t = valuePursuer;
        double valueEvador = (1 * simpleDistance[3] + 0.0 * (simpleDistance[0] + simpleDistance[1] + simpleDistance[2]));
        valueEvador /= diagonal;
         //valuePursuer = valueEvador;
        //valueEvador = t;
        State.StatePreCalcValue spcv = new State.StatePreCalcValue(valuePursuer,valueEvador,1,1);
        return spcv;


    }
}
