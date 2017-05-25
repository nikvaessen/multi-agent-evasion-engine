package nl.dke.pursuitevasion.game.agents.impl;

import nl.dke.pursuitevasion.game.agents.AbstractAgent;
import nl.dke.pursuitevasion.game.agents.AgentRequest;

/**
 * Created by Carla on 25/05/2017.
 */
public class DistanceAgent extends AbstractAgent
{
    @Override
    protected void completeRequest(AgentRequest request)
    {

    }

    @Override
    protected boolean hasNewRequest()
    {
        return false;
    }

    @Override
    public boolean isEvader()
    {
        return true;
    }
}
