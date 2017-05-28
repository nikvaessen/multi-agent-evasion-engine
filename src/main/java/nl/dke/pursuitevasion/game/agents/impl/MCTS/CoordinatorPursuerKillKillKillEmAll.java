package nl.dke.pursuitevasion.game.agents.impl.MCTS;

import nl.dke.pursuitevasion.game.Engine;
import nl.dke.pursuitevasion.game.Vector2D;
import nl.dke.pursuitevasion.game.agents.AbstractAgent;
import nl.dke.pursuitevasion.game.agents.AgentCommand;
import nl.dke.pursuitevasion.game.agents.Direction;
import nl.dke.pursuitevasion.game.agents.tasks.AbstractAgentTask;
import nl.dke.pursuitevasion.game.agents.tasks.WalkToTask;
import nl.dke.pursuitevasion.gui.KeyboardInputListener;
import nl.dke.pursuitevasion.map.impl.Floor;
import nl.dke.pursuitevasion.map.impl.Map;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nibbla on 19.05.2017.
 */
public class CoordinatorPursuerKillKillKillEmAll{
    private final Map map;
    private final Floor floor;
    private final Engine engine;
    List<PursuerKillKillKillEmAll> pursuers = new ArrayList<>(3);
    private MCTS_2 m;


    public CoordinatorPursuerKillKillKillEmAll(Engine e, Map map, Floor startingFloor, Vector2D startLocation, Direction startsFacing, int radius,
                                               double visionRange, double visionAngle, ArrayList<AbstractAgent> agents  ) {
        this.map = map;
        this.floor = startingFloor;
        this.engine = e;

        PursuerKillKillKillEmAll p1 = new PursuerKillKillKillEmAll( map,  startingFloor,  startLocation,  startsFacing,  radius,
                visionRange,  visionAngle,0);
        PursuerKillKillKillEmAll p2 = new PursuerKillKillKillEmAll( map,  startingFloor,  startLocation,  startsFacing,  radius,
                visionRange,  visionAngle,1);
        PursuerKillKillKillEmAll p3 = new PursuerKillKillKillEmAll( map,  startingFloor,  startLocation,  startsFacing,  radius,
                visionRange,  visionAngle,2);
        agents.add(p1);agents.add(p2);agents.add(p3);
        this.pursuers.add(p1);this.pursuers.add(p2);this.pursuers.add(p3);



    }

    public AbstractAgentTask getNextMove(PursuerKillKillKillEmAll p, List<AbstractAgent> pursuer, List<AbstractAgent> evader, long currentTime){
        State s = new State(currentTime);
        TurnOrder t = new TurnOrder(pursuer,evader);
        if (m = null) m = new MCTS_2(s,p, pursuer, evader, t);
        Move move = m.start();
        AbstractAgentTask abstractAgentTask = new WalkToTask(move.getAgent().getLocation(),false);
        if (!m.hasCalculated) m.calculate(durationInMS);
        return abstractAgentTask;


    }
    public void calculateMCTS(long durationInMS){
        Engine start = engine.copy();

        //do something
        //do more
        //MORE!!!
        //MORE fuckers!!!



    }



}
