package nl.dke.pursuitevasion.game.agents.impl;

import nl.dke.pursuitevasion.game.agents.AbstractAgent;
import nl.dke.pursuitevasion.game.agents.AgentRequest;
import nl.dke.pursuitevasion.game.agents.Direction;
import nl.dke.pursuitevasion.map.impl.Floor;
import nl.dke.pursuitevasion.map.impl.Map;

import java.awt.*;

/**
 * Created by nik on 26/02/17.
 */
public class SimpleAgent
    extends AbstractAgent
{
    public SimpleAgent(Map map, Floor startingFloor, Point startLocation, Direction startsFacing, int radius,
                       double visionRange, double visionAngle)
    {
        super(map, startingFloor, startLocation, startsFacing, radius, visionRange, visionRange);
    }

    /**
     * This method will add tasks to a request until the desired state of the agent
     * will be reached by the request.
     *
     * @param request The request to complete with tasks
     */
    @Override
    protected void completeRequest(AgentRequest request)
    {

    }

    /**
     * Calculate whether a new Request is to be determined
     *
     * @return whether a new Request should be created
     */
    @Override
    protected boolean hasNewRequest()
    {
        return false;
    }

    @Override
    public boolean isEvader() {
        return false;
    }
}
