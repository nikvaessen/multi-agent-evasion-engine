package nl.dke.pursuitevasion.game.agents.tasks;

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

    private Point.Double walkToLocation;
    private Collection<Point> pathFindingPoints;
    private boolean pathFind;

    public WalkToTask(Point.Double walkToLocation, boolean pathFind)
    {
        this.pathFind = pathFind;
        this.walkToLocation = walkToLocation;
    }

    public WalkToTask(Point.Double walkToLocation)
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
            if(logger.isTraceEnabled()) {
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
                Point.Double location = agent.getLocation();

                Point.Double movement = new Point.Double(walkToLocation.x - location.x,
                        walkToLocation.y - location.y);

                movement = setToLength(movement, maxDistance);

                location.setLocation(
                        location.x + movement.x,
                         location.y + movement.y
                );

                logger.trace("{} can move to location {} in the allowed movable distance", this,
                        location);
                return new AgentCommand(agent, location);
            }
        }
    }

    private Point.Double setToLength(Point.Double v, double length)
    {
        double scale = length / Math.sqrt(Math.pow(v.x, 2) + Math.pow(v.y, 2));
        return new Point2D.Double(v.x * scale, v.y * scale);
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
