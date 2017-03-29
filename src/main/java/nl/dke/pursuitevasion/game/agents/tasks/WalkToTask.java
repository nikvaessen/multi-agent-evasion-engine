package nl.dke.pursuitevasion.game.agents.tasks;

import nl.dke.pursuitevasion.game.agents.AbstractAgent;
import nl.dke.pursuitevasion.game.agents.AgentCommand;

import java.awt.*;
import java.util.Collection;

/**
 * Created by nik on 03/03/17.
 */
public class WalkToTask
    extends AbstractAgentTask
{

    private Point walkToLocation;
    private Collection<Point> pathFindingPoints;
    private boolean pathFind;

    public WalkToTask(Point walkToLocation, boolean pathFind)
    {
        this.pathFind = pathFind;
        this.walkToLocation = walkToLocation;
    }

    public WalkToTask(Point walkToLocation)
    {
        this.walkToLocation = walkToLocation;
        this.pathFind = false;
    }

    /**
     * Compute the AgentCommand based on the allowed distance and rotation travel
     *
     * @param agent       The agent on which the AgentCommand will be applied
     * @param maxDistance the distance allowed to travel
     * @param maxRotation the rotation allowed to spin
     * @return the AgentCommand which will be able to (partially) complete this task
     */
    @Override
    protected AgentCommand computeAgentCommand(AbstractAgent agent, double maxDistance, double maxRotation)
    {
        if(pathFind)
        {
            throw new IllegalStateException("Pathfinding walking is not implemented");
        }
        else
        {
            if(agent.getLocation().distance(walkToLocation) > maxDistance)
            {
                return new AgentCommand(agent, walkToLocation);
            }
            else
            {
                Point location = new Point(agent.getLocation());

                location.move(((int) (location.x < walkToLocation.x ? -maxDistance : maxDistance)),
                        ((int) (location.y < walkToLocation.y ? -maxDistance : maxDistance)));
                return new AgentCommand(agent, location);
            }
        }
    }

    /**
     * Checks whether the given command will manage to complete this task
     *
     * @param command the command to check
     * @return whether the given command completed this task
     */
    @Override
    protected boolean completesTask(AgentCommand command)
    {
        return command.getLocation().equals(walkToLocation);
    }

    @Override
    public String toString() {
        return String.format("WalkToTask[point:%s,pathfind:%b]", walkToLocation, pathFind);
    }
}
