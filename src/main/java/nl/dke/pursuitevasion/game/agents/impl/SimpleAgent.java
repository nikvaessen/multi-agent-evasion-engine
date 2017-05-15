package nl.dke.pursuitevasion.game.agents.impl;

import nl.dke.pursuitevasion.game.EngineConstants;
import nl.dke.pursuitevasion.game.Vector2D;
import nl.dke.pursuitevasion.game.agents.AbstractAgent;
import nl.dke.pursuitevasion.game.agents.AgentRequest;
import nl.dke.pursuitevasion.game.agents.Direction;
import nl.dke.pursuitevasion.game.agents.tasks.RotateTask;
import nl.dke.pursuitevasion.game.agents.tasks.WalkToTask;
import nl.dke.pursuitevasion.map.impl.Floor;
import nl.dke.pursuitevasion.map.impl.Map;
import nl.dke.pursuitevasion.map.impl.Obstacle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.Collection;

/**
 * Created by nik on 26/02/17.
 */
public class SimpleAgent
    extends AbstractAgent
{
    private static Logger logger = LoggerFactory.getLogger(SimpleAgent.class);

    Vector2D goal = new Vector2D(15, 100);
    private boolean hasRequest;

    public SimpleAgent(Map map, Floor startingFloor, Vector2D startLocation, Direction startsFacing, int radius,
                       double visionRange, double visionAngle)
    {
        super(map, startingFloor, startLocation, startsFacing, radius, visionRange, visionAngle);
        hasRequest = true;
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
        Collection<Obstacle> obs = super.getFloor().getObstacles();
        if(super.location.getX() != goal.getX() && !contains(obs, super.location))
        {
            if(super.location.getX() > goal.getX())
            {
                Direction direction = Direction.getDirection(false, false, false, true);
                request.add(new WalkToTask(goal));
                request.add(new RotateTask(Direction.getAngle(direction)));
                hasRequest = false;
                return;
            }
            else if(super.location.getX() < goal.getX())
            {
                Direction direction = Direction.getDirection(false, false, true, false);
                request.add(new WalkToTask(goal));
                request.add(new RotateTask(Direction.getAngle(direction)));
                hasRequest = false;
                return;
            }
        }

        if(super.location.getY() != goal.getY())
        {
            if(super.location.getY() > goal.getY())
            {
                Direction direction = Direction.getDirection(true, false, false, false);
                request.add(new WalkToTask(goal));
                request.add(new RotateTask(Direction.getAngle(direction)));
                hasRequest = false;
                return;
            }
            else if(super.location.getY() < goal.getY())
            {
                Direction direction = Direction.getDirection(false, true, false, false);
                request.add(new WalkToTask(goal));
                request.add(new RotateTask(Direction.getAngle(direction)));
                hasRequest = false;
                return;
            }
        }
    }

    private boolean contains(Collection<Obstacle> obstacles, Vector2D p)
    {
        Ellipse2D.Double circle = new Ellipse2D.Double(
            p.getX() - super.getRadius(),
            p.getY() - super.getRadius(),
            super.getRadius() * 2,
            super.getRadius() * 2);
        for(Obstacle obs : obstacles)
        {
            Point north = new Point((int) (circle.getX() + (circle.getHeight() / 2)), (int) (circle.getY()));
            Point south = new Point((int) (circle.getX() + (circle.getHeight() / 2)),
                                    (int) (circle.getY() + circle.getHeight()));
            Point east = new Point((int) (circle.getX() + (circle.getHeight())),
                                   (int) (circle.getY() + circle.getHeight() / 2));
            Point west = new Point((int) (circle.getX()), (int) (circle.getY() + circle.getHeight() / 2));
            Point southeast = new Point((int) ((
                                                   circle.getX() + (circle.getHeight() / 2)) +
                                               ((circle.getHeight() / 2) * Math.cos(0.25 * Math.PI))),
                                        (int) ((circle.getY() + (circle.getHeight() / 2)) +
                                               ((circle.getHeight() / 2) * Math.sin(0.25 * Math.PI))));
            Point southwest = new Point(
                (int) ((circle.getX() + (circle.getHeight() / 2)) + ((circle.getHeight() / 2) * Math.cos(
                    0.75 * Math.PI))),
                (int) ((circle.getY() + (circle.getHeight() / 2)) + ((circle.getHeight() / 2) * Math.sin(
                    0.75 * Math.PI))));
            Point northwest = new Point(
                (int) ((circle.getX() + (circle.getHeight() / 2)) + ((circle.getHeight() / 2) * Math.cos(
                    1.25 * Math.PI))),
                (int) ((circle.getY() + (circle.getHeight() / 2)) + ((circle.getHeight() / 2) * Math.sin(
                    1.25 * Math.PI))));
            Point northeast = new Point(
                (int) ((circle.getX() + (circle.getHeight() / 2)) + ((circle.getHeight() / 2) * Math.cos(
                    1.75 * Math.PI))),
                (int) ((circle.getY() + (circle.getHeight() / 2)) + ((circle.getHeight() / 2) * Math.sin(
                    1.75 * Math.PI))));
            if(obs.getPolygon().contains(north) || obs.getPolygon().contains(south) || obs.getPolygon().contains(
                east) || obs.getPolygon().contains(west) || obs.getPolygon().contains(southeast) ||
               obs.getPolygon().contains(southwest) || obs.getPolygon().contains(northeast) ||
               obs.getPolygon().contains(northwest))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Calculate whether a new Request is to be determined
     *
     * @return whether a new Request should be created
     */
    @Override
    protected boolean hasNewRequest()
    {
        return hasRequest;
    }

    @Override
    public boolean isEvader()
    {
        return false;
    }
}
