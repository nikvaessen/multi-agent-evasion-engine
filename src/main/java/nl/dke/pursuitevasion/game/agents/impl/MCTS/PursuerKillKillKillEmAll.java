package nl.dke.pursuitevasion.game.agents.impl.MCTS;

import nl.dke.pursuitevasion.game.Vector2D;
import nl.dke.pursuitevasion.game.agents.AbstractAgent;
import nl.dke.pursuitevasion.game.agents.AgentRequest;
import nl.dke.pursuitevasion.game.agents.Direction;
import nl.dke.pursuitevasion.game.agents.tasks.WalkForwardTask;
import nl.dke.pursuitevasion.map.impl.Floor;
import nl.dke.pursuitevasion.map.impl.Map;

/**
 * Created by Nibbla on 19.05.2017.
 */
public class PursuerKillKillKillEmAll extends AbstractAgent{
    private static CoordinatorPursuerKillKillKillEmAll CoordinatorPursuer;

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
    public PursuerKillKillKillEmAll(Map map, Floor startingFloor, Vector2D startLocation, Direction startsFacing, int radius, double visionRange, double visionAngle,int agentNumber) {
        super(map, startingFloor, startLocation, startsFacing, radius, visionRange, visionAngle);
    }

    @Override
    protected void completeRequest(AgentRequest request) {
        if (this.CoordinatorPursuer == null) this.CoordinatorPursuer = new CoordinatorPursuerKillKillKillEmAll();

        CoordinatorPursuer.getNextMove(this,);

        request.add(new WalkForwardTask(2));

    }

    @Override
    protected boolean hasNewRequest() {
        return false;
    }

    @Override
    public boolean isEvader() {
        return false;
    }
}
