package nl.dke.pursuitevasion.game.agents.impl;

import nl.dke.pursuitevasion.game.EngineConstants;
import nl.dke.pursuitevasion.game.agents.AbstractAgent;
import nl.dke.pursuitevasion.game.agents.AgentRequest;
import nl.dke.pursuitevasion.game.agents.Direction;
import nl.dke.pursuitevasion.game.agents.tasks.RotateTask;
import nl.dke.pursuitevasion.game.agents.tasks.WalkToTask;
import nl.dke.pursuitevasion.map.impl.Floor;
import nl.dke.pursuitevasion.map.impl.Map;
import nl.dke.pursuitevasion.map.impl.Obstacle;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.Collection;
import java.util.Random;

/**
 * Created by nik on 26/02/17.
 */
public class SimpleAgent
    extends AbstractAgent
{
    Point goal = new Point(300,500);

    public SimpleAgent(Map map, Floor startingFloor, Point startLocation, Direction startsFacing, int radius,
                       double visionRange, double visionAngle)
    {
        super(map, startingFloor, startLocation, startsFacing, radius, visionRange, visionRange);
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
        Random random = new Random();
        Collection<Obstacle> obs = super.getFloor().getObstacles();
        if(super.location.x != goal.x && !contains(obs, super.location)){
            if (super.location.x > goal.x){
                Direction direction = Direction.getDirection(false, false, false, true);
                request.add(new RotateTask(Direction.getAngle(direction)));
                request.add(new WalkToTask(goal));
                return;
            }
            else if (super.location.x < goal.x) {
                Direction direction = Direction.getDirection(false, false, true, false);
                request.add(new RotateTask(Direction.getAngle(direction)));
                request.add(new WalkToTask(goal));
                return;
            }
        } else if(super.location.x != goal.x) {
            request.add(new RotateTask(Direction.getAngle(Direction.getRandomDirection())));
            request.add(new WalkToTask(new Point
                    (super.location.x+(random.nextInt(40)-20), super.location.y+(random.nextInt(40)-20))));
            return;
        }
        if(super.location.y != goal.y && !contains(obs, super.location)){
            if (super.location.y > goal.y){
                Direction direction = Direction.getDirection(true, false, false, false);
                request.add(new RotateTask(Direction.getAngle(direction)));
                request.add(new WalkToTask(goal));
                return;
            }
            else if (super.location.y < goal.y){
                Direction direction = Direction.getDirection(false, true, false, false);
                request.add(new RotateTask(Direction.getAngle(direction)));
                request.add(new WalkToTask(goal));
                return;
            }
        } else if(super.location.y != goal.y) {
            request.add(new RotateTask(Direction.getAngle(Direction.getRandomDirection())));
            request.add(new WalkToTask(new Point
                    (super.location.x+(random.nextInt(40)-20), super.location.y+(random.nextInt(40)-20))));
            return;
        }
    }

    private boolean contains(Collection<Obstacle> obstacles, Point p){
        Ellipse2D.Double circle = new Ellipse2D.Double
                (p.x-super.getRadius(), p.y-super.getRadius(), super.getRadius()*2, super.getRadius()*2);
        for (Obstacle obs: obstacles) {
            Point north = new Point((int)(circle.getX()+(circle.getHeight()/2)), (int) (circle.getY()));
            Point south = new Point((int)(circle.getX()+(circle.getHeight()/2)), (int) (circle.getY()+circle.getHeight()));
            Point east = new Point((int)(circle.getX()+(circle.getHeight())), (int) (circle.getY()+circle.getHeight()/2));
            Point west = new Point((int)(circle.getX()), (int) (circle.getY()+circle.getHeight()/2));
            Point southeast = new Point((int)( (circle.getX()+(circle.getHeight()/2))+((circle.getHeight()/2)*Math.cos(0.25*Math.PI)) ),
                    (int) ( (circle.getY()+(circle.getHeight()/2))+((circle.getHeight()/2)*Math.sin(0.25*Math.PI)) ) );
            Point southwest = new Point((int)( (circle.getX()+(circle.getHeight()/2))+((circle.getHeight()/2)*Math.cos(0.75*Math.PI)) ),
                    (int) ( (circle.getY()+(circle.getHeight()/2))+((circle.getHeight()/2)*Math.sin(0.75*Math.PI)) ) );
            Point northwest = new Point((int)( (circle.getX()+(circle.getHeight()/2))+((circle.getHeight()/2)*Math.cos(1.25*Math.PI)) ),
                    (int) ( (circle.getY()+(circle.getHeight()/2))+((circle.getHeight()/2)*Math.sin(1.25*Math.PI)) ) );
            Point northeast = new Point((int)( (circle.getX()+(circle.getHeight()/2))+((circle.getHeight()/2)*Math.cos(1.75*Math.PI)) ),
                    (int) ( (circle.getY()+(circle.getHeight()/2))+((circle.getHeight()/2)*Math.sin(1.75*Math.PI)) ) );
            if ( obs.getPolygon().contains(north) || obs.getPolygon().contains(south) || obs.getPolygon().contains(east)
                    || obs.getPolygon().contains(west) || obs.getPolygon().contains(southeast)
                    || obs.getPolygon().contains(southwest) || obs.getPolygon().contains(northeast)
                    || obs.getPolygon().contains(northwest) ){
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
        return true;
    }

    @Override
    public boolean isEvader() {
        return false;
    }
}
