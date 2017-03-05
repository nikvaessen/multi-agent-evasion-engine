package nl.dke.pursuitevasion.game.agents.tasks;

import nl.dke.pursuitevasion.game.agents.AbstractAgent;
import nl.dke.pursuitevasion.game.agents.AgentCommand;

/**
 * Created by nik on 03/03/17.
 */
public abstract class AbstractAgentTask
{
    /**
     * Create an AgentCommand based on the task
     * @return The AgentCommand of how to handle the request
     */
    public abstract AgentCommand handle(AbstractAgent agent, double maxDistance, double maxRotation);
}
