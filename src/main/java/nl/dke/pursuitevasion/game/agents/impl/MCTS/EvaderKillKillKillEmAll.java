package nl.dke.pursuitevasion.game.agents.impl.MCTS;

import nl.dke.pursuitevasion.game.EngineConstants;
import nl.dke.pursuitevasion.game.Vector2D;
import nl.dke.pursuitevasion.game.agents.AbstractAgent;
import nl.dke.pursuitevasion.game.agents.AgentRequest;
import nl.dke.pursuitevasion.game.agents.Direction;
import nl.dke.pursuitevasion.game.agents.tasks.AbstractAgentTask;
import nl.dke.pursuitevasion.map.impl.Floor;
import nl.dke.pursuitevasion.map.impl.Map;

/**
 * Created by Nibbla on 19.05.2017.
 */
public class EvaderKillKillKillEmAll extends AbstractAgent{
    public static void setCoordinatorPursuer(CoordinatorEvaderKillKillKillEmAll coordinatorEvader2) {
        coordinatorEvader = coordinatorEvader2;
    }

    private static CoordinatorEvaderKillKillKillEmAll coordinatorEvader;
    private int agentnumber;
    /**
     * Create an agent in the given Map
     *
     * @param map           the map where the agent is going to be interacting in
     * @param startingFloor the Floor (which is in the Map) the agent will be placed on
     * @param startLocation the location on the given floor the agent will be put on
     * @param startsFacing  the direction the agent will start facing in
     * @param radius        the radius of the agent
     * @param visionRange
     * @param visionAngle
     */
    public EvaderKillKillKillEmAll(Map map, Floor startingFloor, Vector2D startLocation, Direction startsFacing, int radius, double visionRange, double visionAngle, int agentNumber) {
        super(map, startingFloor, startLocation, startsFacing, radius, visionRange, visionAngle);
        this.agentnumber = agentNumber;
    }

    private EvaderKillKillKillEmAll(int id) {
        super(id);
    }

    @Override
    public EvaderKillKillKillEmAll clone()  {
        EvaderKillKillKillEmAll b = new EvaderKillKillKillEmAll(this.getId());

        b.map = map;
        b.floor = this.floor;
        b.location = this.location.copy();

        b.facing = this.facing.clone();
        b.radius = this.radius;
        b.visionRange = this.visionRange;
        b.visionAngle = this.visionAngle;
        b.visionArc = this.getVisionArc().clone();
        b.agentnumber = this.agentnumber;
        return b;
    }

    @Override
    protected void completeRequest(AgentRequest request) {
       // if (CoordinatorPursuer == null) CoordinatorPursuer = new CoordinatorPursuerKillKillKillEmAll();
        System.out.println("Ask for move");
        AbstractAgentTask a =coordinatorEvader.getNextMove(this, EngineConstants.CALCULATION_TIME);
        AbstractAgentTask b = coordinatorEvader.getRotationTaks(this);
        System.out.println("recieved as solution: "+ a);
        request.add(a);


    }

    @Override
    protected boolean hasNewRequest() {
        return coordinatorEvader.hasNewRequest(this);
    }

    @Override
    public boolean isEvader() {
        return false;
    }
}
