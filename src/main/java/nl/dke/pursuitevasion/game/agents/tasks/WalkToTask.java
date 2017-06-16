package nl.dke.pursuitevasion.game.agents.tasks;

import nl.dke.pursuitevasion.game.Vector2D;
import nl.dke.pursuitevasion.game.agents.AbstractAgent;
import nl.dke.pursuitevasion.game.agents.AgentCommand;
import org.jgrapht.GraphPath;
import org.jgrapht.WeightedGraph;
import org.jgrapht.alg.shortestpath.KShortestPaths;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.Line;
import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;

/**
 * Created by nik on 03/03/17.
 */
public class WalkToTask
    extends AbstractAgentTask
{

    private static Logger logger = LoggerFactory.getLogger(WalkToTask.class);

    private Vector2D walkToLocation;
    private boolean pathFind;
    private LinkedList<Line2D> pathLines;

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
        Vector2D moveTowards = null;
        if(pathFind)
        {
            if(pathLines == null)
            {
                ArrayList<Vector2D> extraPoints = new ArrayList<>();
                extraPoints.add(agent.getLocation());
                extraPoints.add(walkToLocation);
                WeightedGraph<Vector2D, DefaultWeightedEdge> g = agent.getFloor().getVisibilityGraph(extraPoints);
                KShortestPaths<Vector2D, DefaultWeightedEdge> shortestPathAlgo =
                        new KShortestPaths<>(g, 1);

                List<GraphPath<Vector2D, DefaultWeightedEdge>> paths =
                        shortestPathAlgo.getPaths(agent.getLocation(), walkToLocation);
                if(!paths.isEmpty())
                {
                    GraphPath<Vector2D, DefaultWeightedEdge> path = paths.get(0);
                    this.pathLines = new LinkedList<>();
                    List<Vector2D> vertexList = path.getVertexList();
                    for(int i = 0; i < vertexList.size() - 1; i++)
                    {
                        Vector2D start = vertexList.get(i);
                        Vector2D end = vertexList.get(i+1);
                        pathLines.add(new Line2D.Double(start.toPoint(), end.toPoint()));
                    }
                }
                else
                {
                    throw new IllegalArgumentException("Cannot compute path");
                }
            }

            // just move on the first line as the pathfind task will be overwritten immiadetely because
            // the evader moves
            // FIXME: 6/16/17 actually make it select the line the agent is currently on and discard already used lines
            Line2D line = pathLines.peekFirst();
            moveTowards = new Vector2D(line.getP2().getX(), line.getP2().getY());
        }

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
            Vector2D movement;
            if(moveTowards == null)
            {
                movement = walkToLocation.subtract(oldLocation);
            }
            else
            {
                movement = moveTowards.subtract(oldLocation);
            }

            // scale the movement vector to the allowed moveable distance
            double scalar = 1 / (movement.length() / maxDistance);
            movement = movement.scale(scalar);

            // get the new location by adding the allowed movement to the new location
            // inverse the y axis because of how the coordinate system works
            Vector2D newLocation = oldLocation.add(movement);

            if(logger.isTraceEnabled())
            {
                logger.trace("{} can move to location {} in the allowed movable distance", this, newLocation);
                logger.trace("oldLocation: {} movement: {} scalar: {}, newLocation: {}, goal: {}",
                             oldLocation, movement, scalar, newLocation, walkToLocation);
            }

            return new AgentCommand(agent, newLocation);
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
        return Math.abs((command.getNewLocation().subtract(command.getAgent().getLocation())).length()) < 0.0001;
    }

    @Override
    public String toString()
    {
        return String.format("WalkToTask[point:%s,pathfind:%b]", walkToLocation, pathFind);
    }
}
