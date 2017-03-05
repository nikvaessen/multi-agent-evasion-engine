package nl.dke.pursuitevasion.game.agents;

import nl.dke.pursuitevasion.game.agents.tasks.AbstractAgentTask;

import java.util.LinkedList;

/**
 * This class represent the desired movement from an agent
 *
 * Created by nik on 03/03/17.
 */
public class AgentRequest
{

    private AbstractAgent agent;

    private boolean confirmed = false;
    private boolean completed = false;

    private LinkedList<AbstractAgentTask> tasks;

    public AgentRequest(AbstractAgent agent)
    {
        this.agent = agent;
    }

    public boolean isCompleted()
    {
        checkIfCompleted();
        return completed;
    }

    public void confirm()
    {
        confirmed = true;
    }

    public boolean isConfirmed()
    {
        return confirmed;
    }

    public AbstractAgent getAgent()
    {
        return this.agent;
    }

    private void checkIfCompleted()
    {

    }

    public void add(AbstractAgentTask task)
    {
        if(!isConfirmed())
        {
            tasks.add(task);
        }
    }

    public AbstractAgentTask peek()
    {
        return tasks.peekFirst();
    }
}