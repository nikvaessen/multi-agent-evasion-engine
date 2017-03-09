package nl.dke.pursuitevasion.game.agents;

import nl.dke.pursuitevasion.game.agents.tasks.AbstractAgentTask;

import java.util.LinkedList;

/**
 * This class represent the desired movement from an agent. A request consists of multiple Tasks which need to be
 * accomplished. If all tasks are accomplished, the request is completed.
 *
 * Tasks can be created until the request is "confirmed". When the request is confirmed, no more tasks can be added.
 * The request will not be "completed" until all Tasks have been fulfilled.
 *
 * The agent who made the request can invalidate a request when a change is required, before the previously made
 * request has been confirmed. This will automatically set the request to "completed"
 *
 * An agent can only have 1 request at the same time.
 *
 * Created by nik on 03/03/17.
 */
public class AgentRequest
{

    /**
     * The agent which requests the tasks
     */
    private AbstractAgent agent;

    /**
     * boolean flag for the "confirmed" state
     */
    private boolean confirmed = false;

    /**
     * boolean flag for the "completed" state
     */
    private boolean completed = false;

    /**
     * Queue for all Tasks to complete
     */
    private LinkedList<AbstractAgentTask> tasks;

    /**
     * Create an AgentRequest
     *
     * @param agent the agent for which the Tasks should be completed
     */
    public AgentRequest(AbstractAgent agent)
    {
        this.agent = agent;
        tasks = new LinkedList<>();
    }

    /**
     * Get if all tasks of this agent are completed
     *
     * @return whether al Tasks have been accomplished
     */
    public boolean isCompleted()
    {
        checkIfCompleted();
        return completed;
    }

    /**
     * Set the state to "confirmed", making it impossible to add other tasks.
     */
    public void confirm()
    {
        confirmed = true;
    }

    /**
     * Get whether this request is confirmed
     *
     * @return whether this request has been confirmed
     */
    public boolean isConfirmed()
    {
        return confirmed;
    }

    /**
     * Get the agent on which to apply all tasks

     * @return The agent to apply all tasks on
     */
    public AbstractAgent getAgent()
    {
        return this.agent;
    }

    /**
     * Invalidate this request, putting it into the "completed" state
     */
    public void invalidate()
    {
        confirmed = true;
        completed = true;
        tasks.clear();
    }

    /**
     * Checks if all tasks have been completed. If they have, the Request will enter the "completed" state
     */
    private void checkIfCompleted()
    {
        AbstractAgentTask task;
        while((task = tasks.peek()) != null)
        {
            if(task.completed())
            {
                tasks.pop();
            }
            else
            {
                break;
            }
        }
        completed = tasks.isEmpty();
    }

    /**
     * Add tasks as long as the request does not get confirmed
     *
     * @param task the task to add to this request
     */
    public void add(AbstractAgentTask task)
    {
        if(!isConfirmed())
        {
            tasks.addLast(task);
        }
    }

    /**
     * Get the first non-completed Task of this request.
     *
     * @return the first task o
     */
    public AbstractAgentTask peek()
    {
        checkConfirmed();
        checkNotCompleted();
        return tasks.peekFirst();
    }

    /**
     * Check if this request is in the confirmed state. If it not, throw an IllegalStateException
     */
    private void checkConfirmed()
    {
        if(!confirmed)
        {
            throw new IllegalStateException("Cannot access Tasks as this request is not confirmed yet");
        }
    }

    /**
     * Check if this Request has been completed. If it is, throw an IllegalStateException
     */
    private void checkNotCompleted()
    {
        checkIfCompleted();
        if(completed)
        {
           throw new IllegalStateException("Cannot access new task as the request has been completed");
        }
    }

}