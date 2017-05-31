package nl.dke.pursuitevasion.game.agents.impl.MCTS;

import nl.dke.pursuitevasion.game.Engine;
import nl.dke.pursuitevasion.game.Vector2D;
import nl.dke.pursuitevasion.game.agents.AbstractAgent;
import nl.dke.pursuitevasion.game.agents.AgentCommand;
import nl.dke.pursuitevasion.game.agents.Direction;
import nl.dke.pursuitevasion.game.agents.tasks.AbstractAgentTask;
import nl.dke.pursuitevasion.game.agents.tasks.WalkToTask;
import nl.dke.pursuitevasion.gui.KeyboardInputListener;
import nl.dke.pursuitevasion.gui.editor.ModelView;
import nl.dke.pursuitevasion.gui.simulator.MapViewPanel;
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
    private final ArrayList<AbstractAgent> agents;
    ArrayList<AbstractAgent>  evador  = new ArrayList<>(1);
    ArrayList<AbstractAgent> pursuers = new ArrayList<>(3);
    private MCTS_2 m;
    private MapViewPanel viewport;
    private long lastTime =0;
    private long lastTimeViewUpdate = 0;


    public CoordinatorPursuerKillKillKillEmAll(Engine e, Map map, Floor startingFloor, Vector2D startLocation, Direction startsFacing, int radius,
                                               double visionRange, double visionAngle, ArrayList<AbstractAgent> agents  ) {
        this.map = map;
        this.floor = startingFloor;
        this.engine = e;
        this.agents = agents;
        PursuerKillKillKillEmAll p1 = new PursuerKillKillKillEmAll( map,  startingFloor,  startLocation,  startsFacing,  radius,
                visionRange,  visionAngle,0);
        PursuerKillKillKillEmAll p2 = new PursuerKillKillKillEmAll( map,  startingFloor,  startLocation,  startsFacing,  radius,
                visionRange,  visionAngle,1);
        PursuerKillKillKillEmAll p3 = new PursuerKillKillKillEmAll( map,  startingFloor,  startLocation,  startsFacing,  radius,
                visionRange,  visionAngle,2);
        agents.add(p1);agents.add(p2);agents.add(p3);
        this.evador.add(agents.get(0));
        this.pursuers.add(p1);this.pursuers.add(p2);this.pursuers.add(p3);
        PursuerKillKillKillEmAll.setCoordinatorPursuer(this);


    }

    public void setViewPort(MapViewPanel panel){
        viewport = panel;
    }

    public AbstractAgentTask getNextMove(PursuerKillKillKillEmAll p, long calculationTime){
        long start = System.currentTimeMillis();
        System.out.println("Started one MCTS at: " + start + "ms");

        TurnOrder t = new TurnOrder(p,agents);
        State s = new State(engine, map, t, evador, pursuers);


        if (MCTS_2.getLastMCTS() == null) m = new MCTS_2(s,t,calculationTime, 10,false);
        else m = MCTS_2.getLastMCTS();
        if (viewport!=null){
            if ((  start - lastTimeViewUpdate)>2000)  {
                viewport.setMCTSPreview(m);
                lastTimeViewUpdate = start;
            }

        }
        m.updateState(s);
        Move move = m.start();

        AbstractAgentTask abstractAgentTask = new WalkToTask(move.getEndLocation(),false);
        //if (!m.hasCalculated) m.calculate(durationInMS);


        long finish = System.currentTimeMillis();
        long duration = finish-start;
        lastTime = finish;
        System.out.println(m.toString());
        System.out.println("Finished one MCTS in: " + duration + "ms");
        return abstractAgentTask;

      //  Engine start = engine.copy();
    }




}
