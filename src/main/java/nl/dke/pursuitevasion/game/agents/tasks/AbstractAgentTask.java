package nl.dke.pursuitevasion.game.agents.tasks;

import nl.dke.pursuitevasion.game.agents.AbstractAgent;
import nl.dke.pursuitevasion.game.agents.AgentCommand;

/**
 * Created by nik on 03/03/17.
 */
public abstract class AbstractAgentTask
{
    /**
     * boolean flag for the "completed" state
     */
    private boolean handled = false;

    /**
     * Create an AgentCommand based on the task. It does not necessarily have to complete the task. This is based on how
     * far is is allowed to move. If the task gets completed by the the AgentCommand, new calls to handle will result
     * in an IllegalStateException.
     *
     * @return an AgentCommand which can (partially) accomplish the task.
     * @throws IllegalStateException when the task has already been handled in previous calls
     */
    public final AgentCommand handle(AbstractAgent agent, double maxDistance, double maxRotation)
    {
        if(handled)
        {
            throw new IllegalStateException("This task had already been handled in previous calls");
        }

        AgentCommand command = computeAgentCommand(agent, maxDistance, maxRotation);
        handled = completesTask(command);
        return command;
    }

    /**
     * Get if this task has been completed

     * @return whether AgentCommands have been given which complete this task
     */
    public boolean completed()
    {
       return handled;
    }

    /**
     * Compute the AgentCommand based on the allowed distance and rotation travel

     * @param agent The agent on which the AgentCommand will be applied
     * @param maxDistance the distance allowed to travel
     * @param maxRotation the rotation allowed to spin
     * @return the AgentCommand which will be able to (partially) complete this task
     */
    protected abstract AgentCommand computeAgentCommand(AbstractAgent agent, double maxDistance, double maxRotation);

    /**
     * Checks whether the given command will manage to complete this task
     *
     * @param command the command to check
     * @return whether the given command completed this task
     */
    protected abstract boolean completesTask(AgentCommand command);

}
