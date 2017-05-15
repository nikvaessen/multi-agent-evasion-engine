package nl.dke.pursuitevasion.game.agents.tasks;

import nl.dke.pursuitevasion.game.Vector2D;
import nl.dke.pursuitevasion.game.agents.AbstractAgent;
import nl.dke.pursuitevasion.game.agents.AgentCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Collection;

/**
 * Created by nik on 03/03/17.
 */
public class WalkToTask
    extends AbstractAgentTask
{

    private static Logger logger = LoggerFactory.getLogger(WalkToTask.class);

    private Vector2D walkToLocation;
    private Collection<Point> pathFindingPoints;
    private boolean pathFind;

    public WalkToTask(Vector2D walkToLocation, boolean pathFind)
    {
        this.pathFind = pathFind;
        this.walkToLocation = walkToLocation;
    }

    public WalkToTask(Vector2D walkToLocation)
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
            if(logger.isTraceEnabled())
            {
                logger.trace("Computing AgentCommand for {}. Agent is currently at {}. Allowewd to move {}",
                             this, agent.getLocation(), maxDistance);
            }

            if(agent.getLocation().distance(walkToLocation) < maxDistance)
            {
                logger.trace("Task {} can be completed in the allowed movable distance", this);
                return new AgentCommand(agent, walkToLocation);
            }
            else
            {
                // the old location
                Vector2D oldLocation = agent.getLocation();

                // calculate the vector describing the movement from old to new location
                Vector2D movement = oldLocation.add(walkToLocation);

                // scale the movement vector to the allowed moveable distance
                double scalar = 1 / (movement.length() / maxDistance);
                movement = movement.scale(scalar);

                // get the new location by adding the allowed movement to the new location
                Vector2D newLocation = oldLocation.add(movement);

                if(logger.isTraceEnabled())
                {
                    logger.trace("{} can move to location {} in the allowed movable distance", this, newLocation);
                    logger.trace("oldLocation: {} movement: {} scalar: {}, newLocation: {}",
                                 oldLocation, movement, scalar, newLocation);
                }

                return new AgentCommand(agent, newLocation);
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
        return command.getNewLocation().equals(walkToLocation);
    }

    @Override
    public String toString()
    {
        return String.format("WalkToTask[point:%s,pathfind:%b]", walkToLocation, pathFind);
    }
}
