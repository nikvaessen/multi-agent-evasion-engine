package nl.dke.pursuitevasion.game.agents.impl.MCTS;

import nl.dke.pursuitevasion.game.Engine;
import nl.dke.pursuitevasion.game.EngineConstants;
import nl.dke.pursuitevasion.game.Vector2D;
import nl.dke.pursuitevasion.game.agents.AbstractAgent;
import nl.dke.pursuitevasion.game.agents.AgentCommand;
import nl.dke.pursuitevasion.game.agents.Angle;
import nl.dke.pursuitevasion.game.agents.Direction;
import nl.dke.pursuitevasion.game.agents.tasks.AbstractAgentTask;
import nl.dke.pursuitevasion.game.agents.tasks.RotateTask;
import nl.dke.pursuitevasion.game.agents.tasks.WalkToTask;
import nl.dke.pursuitevasion.gui.KeyboardInputListener;
import nl.dke.pursuitevasion.gui.editor.ModelView;
import nl.dke.pursuitevasion.gui.simulator.MapViewPanel;
import nl.dke.pursuitevasion.map.impl.Floor;
import nl.dke.pursuitevasion.map.impl.Map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Nibbla on 19.05.2017.
 */
public class CoordinatorPursuerKillKillKillEmAll{
    private final Map map;
    private final Floor floor;
    private final Engine engine;
    private final ArrayList<AbstractAgent> agents;
    private final PreCalcMap preCalcMap;
    ArrayList<AbstractAgent>  evador  = new ArrayList<>(1);
    ArrayList<AbstractAgent> pursuers = new ArrayList<>(3);
    private MCTS_2 m;
    private MapViewPanel viewport;
    private long lastTime =0;
    private long lastTimeViewUpdate = 0;
    private java.util.Map<Integer,AbstractAgentTask> lastAbstractAgentTask = new HashMap<>(4);
    private java.util.Map<Integer,Boolean> hasRequest = new HashMap<>(4);
    private boolean hasNewRequest = false;


    public CoordinatorPursuerKillKillKillEmAll(Engine e, Map map, Floor startingFloor, Vector2D startLocation, Direction startsFacing, int radius,
                                               double visionRange, double visionAngle, ArrayList<AbstractAgent> agents  ) {
        this.map = map;
        this.floor = startingFloor;
        this.engine = e;
        this.agents = agents;
        this.preCalcMap = new PreCalcMap(map);

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
        TurnOrder t = new TurnOrder(p1,agents);
        State s = new State(engine, map, t, evador, pursuers);

        Thread calctrhead = new Thread(new ThinkThread(p1,s,t,this, EngineConstants.CALCULATION_TIME,preCalcMap));
        calctrhead.start();
        TurnOrder t2 = new TurnOrder(p2,agents);
        State s2 = new State(engine, map, t2, evador, pursuers);

        Thread calctrhead2 = new Thread(new ThinkThread(p2,s2,t2,this,EngineConstants.CALCULATION_TIME,preCalcMap));
        calctrhead2.start();
        TurnOrder t3 = new TurnOrder(p3,agents);

        State s3 = new State(engine, map, t3, evador, pursuers);
        Thread calctrhead3 = new Thread(new ThinkThread(p3,s3,t3,this,EngineConstants.CALCULATION_TIME,preCalcMap));
        calctrhead3.start();


        try {
            Thread.sleep(1000);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
    }

    public void setViewPort(MapViewPanel panel){
        viewport = panel;
    }

    public AbstractAgentTask getNextMove(PursuerKillKillKillEmAll p, long calculationTime){


        TurnOrder t = new TurnOrder(p,agents);
        State s = new State(engine, map, t, evador, pursuers);
        hasNewRequest = false;
        hasRequest.put(p.getId(),false);
        Thread calctrhead = new Thread(new ThinkThread(p,s,t,this,calculationTime,preCalcMap));
        calctrhead.start();

        AbstractAgentTask aa = lastAbstractAgentTask.get(p.getId());

        return aa;

      //  Engine start = engine.copy();
    }

    public boolean hasNewRequest(PursuerKillKillKillEmAll pursuerKillKillKillEmAll) {
       Boolean b =  hasRequest.get(pursuerKillKillKillEmAll.getId());
        if (b==null) return false;
        return b;
    }

    public AbstractAgentTask getRotationTaks(PursuerKillKillKillEmAll pursuerKillKillKillEmAll) {
        Angle a = new Angle(Math.abs(evador.get(0).getFacingAngle() - pursuerKillKillKillEmAll.getFacingAngle()));
        RotateTask rt = new RotateTask(a.getAngle());
        return rt;
    }

    private class ThinkThread
            implements Runnable
    {

        private final PursuerKillKillKillEmAll p;
        private final State s;
        private final TurnOrder t;
        private final CoordinatorPursuerKillKillKillEmAll Coordinator;
        private final long calculationTime;
        private final PreCalcMap precalcmap;

        public ThinkThread(PursuerKillKillKillEmAll p, State s, TurnOrder t, CoordinatorPursuerKillKillKillEmAll coordinatorPursuerKillKillKillEmAll, long calculationTime,PreCalcMap preCalcMap) {
            this.p = p;
            this.s = s;
            this.t = t;
            this.Coordinator = coordinatorPursuerKillKillKillEmAll;
            this.calculationTime = calculationTime;
            this.precalcmap =preCalcMap;
        }

        @Override
        public void run() {
            long start = System.currentTimeMillis();
            System.out.println("Started one MCTS at: " + start + "ms");
            if (MCTS_2.getLastMCTS() == null) m = new MCTS_2(s,t,calculationTime, 10,false,precalcmap);
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
            Coordinator.hasNewRequest = true;
            hasRequest.put(p.getId(),true);
            Coordinator.lastAbstractAgentTask.put(p.getId(), abstractAgentTask);

            long finish = System.currentTimeMillis();
            long duration = finish-start;
            lastTime = finish;
            System.out.println(m.toString());
            System.out.println("Finished one MCTS in: " + duration + "ms");
        }
    }




}
