package nl.dke.pursuitevasion.game.agents.impl.MCTS;

import nl.dke.pursuitevasion.game.Engine;
import nl.dke.pursuitevasion.game.EngineConstants;
import nl.dke.pursuitevasion.game.Vector2D;
import nl.dke.pursuitevasion.game.agents.AbstractAgent;
import nl.dke.pursuitevasion.game.agents.Angle;
import nl.dke.pursuitevasion.game.agents.Direction;
import nl.dke.pursuitevasion.game.agents.tasks.AbstractAgentTask;
import nl.dke.pursuitevasion.game.agents.tasks.RotateTask;
import nl.dke.pursuitevasion.game.agents.tasks.WalkToTask;
import nl.dke.pursuitevasion.gui.simulator.MapViewPanel;
import nl.dke.pursuitevasion.map.impl.Floor;
import nl.dke.pursuitevasion.map.impl.Map;

import java.util.ArrayList;
import java.util.HashMap;



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
public class CoordinatorEvaderKillKillKillEmAll{
    private final Map map;
    private final Floor floor;
    private final Engine engine;
    private final ArrayList<AbstractAgent> agents;
    private final PreCalcMap preCalcMap;
    private boolean visualOutput;
    ArrayList<AbstractAgent>  evador  = new ArrayList<>(1);
    ArrayList<AbstractAgent> pursuers = new ArrayList<>(3);
    private MCTS_2 m;
    private MapViewPanel viewport;
    private long lastTime =0;
    private long lastTimeViewUpdate = 0;
    private java.util.Map<Integer,AbstractAgentTask> lastAbstractAgentTask = new HashMap<>(4);
    private java.util.Map<Integer,Boolean> hasRequest = new HashMap<>(4);
    private boolean hasNewRequest = false;


    public CoordinatorEvaderKillKillKillEmAll(Engine e, Map map, Floor startingFloor, Vector2D startLocation, Direction startsFacing, int radius,
                                              double visionRange, double visionAngle, ArrayList<AbstractAgent> agents, boolean visualOUtput) {
        this.map = map;
        this.floor = startingFloor;
        this.engine = e;
        this.agents = agents;
        this.visualOutput = visualOUtput;
        EvaderKillKillKillEmAll e1 = new EvaderKillKillKillEmAll( map,  startingFloor,  startLocation,  startsFacing,  radius,
                visionRange,  visionAngle,0);


        this.evador.add(e1);

        this.pursuers.add(agents.get(0));this.pursuers.add(agents.get(1));this.pursuers.add(agents.get(2));
        EvaderKillKillKillEmAll.setCoordinatorPursuer(this);
        System.out.println(e1);
        TurnOrder t = new TurnOrder(e1,agents);
       // t.previousPlayer();

            this.preCalcMap = new PreCalcMap(map);

        State s = new State(engine, map, t, evador, pursuers);
        this.agents.add(0,e1);
        Thread calctrhead = new Thread(new ThinkThread(e1,s,t,this, EngineConstants.CALCULATION_TIME));

        calctrhead.start();



       // try {
        //    Thread.sleep(1000);
       // } catch (InterruptedException ee) {
        //    ee.printStackTrace();
        //}
    }

    public void setViewPort(MapViewPanel panel){
        viewport = panel;
    }

    public AbstractAgentTask getNextMove(EvaderKillKillKillEmAll p, long calculationTime){


        TurnOrder t = new TurnOrder(p,agents);
        State s = new State(engine, map, t, evador, pursuers);
        hasNewRequest = false;
        hasRequest.put(p.getId(),false);
        Thread calctrhead = new Thread(new ThinkThread(p,s,t,this,calculationTime));
        calctrhead.start();

        AbstractAgentTask aa = lastAbstractAgentTask.get(p.getId());

        return aa;

        //  Engine start = engine.copy();
    }

    public boolean hasNewRequest(EvaderKillKillKillEmAll pursuerKillKillKillEmAll) {
        Boolean b =  hasRequest.get(pursuerKillKillKillEmAll.getId());
        if (b==null) return false;
        return b;
    }

    public AbstractAgentTask getRotationTaks(AbstractAgent pursuerKillKillKillEmAll) {
        Angle a = new Angle(Math.abs(evador.get(0).getFacingAngle() - pursuerKillKillKillEmAll.getFacingAngle()));
        RotateTask rt = new RotateTask(a.getAngle());
        return rt;
    }

    private class ThinkThread
            implements Runnable
    {

        private final EvaderKillKillKillEmAll p;
        private final State s;
        private final TurnOrder t;
        private final CoordinatorEvaderKillKillKillEmAll Coordinator;
        private final long calculationTime;

        public ThinkThread(EvaderKillKillKillEmAll p, State s, TurnOrder t, CoordinatorEvaderKillKillKillEmAll coordinatorPursuerKillKillKillEmAll, long calculationTime) {
            this.p = p;
            this.s = s;
            this.t = t;
            this.Coordinator = coordinatorPursuerKillKillKillEmAll;
            this.calculationTime = calculationTime;
        }

        @Override
        public void run() {
            long start = System.currentTimeMillis();
          //  System.out.println("Started one MCTS at: " + start + "ms");
            if (MCTS_2.getLastMCTS() == null) m = new MCTS_2(s,t,calculationTime, 10,false,preCalcMap);
            else m = MCTS_2.getLastMCTS();
            if (viewport!=null){
                if ((  start - lastTimeViewUpdate)>2000)  {
                    viewport.setMCTSPreview(m,visualOutput);
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
            //System.out.println(m.toString());
           // System.out.println("Finished one MCTS in: " + duration + "ms");
        }
    }




}
