package nl.dke.pursuitevasion.game.agents.impl;

import nl.dke.pursuitevasion.game.MapInfo;
import nl.dke.pursuitevasion.game.Vector2D;
import nl.dke.pursuitevasion.game.agents.AbstractAgent;
import nl.dke.pursuitevasion.game.agents.AgentRequest;
import nl.dke.pursuitevasion.game.agents.Direction;
import nl.dke.pursuitevasion.game.agents.tasks.WalkToTask;
import nl.dke.pursuitevasion.map.impl.Floor;
import nl.dke.pursuitevasion.map.impl.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;

/**
 * Created by Carla on 25/05/2017.
 */
public class DistanceAgent extends AbstractAgent
{
    /**
     * The logger of this class
     */
    private final static Logger logger = LoggerFactory.getLogger(DistanceAgent.class);

    /**
     * Create an agent in the given Map
     *
     * @param map           the map where the agent is going to be interacting in
     * @param startingFloor the Floor (which is in the Map) the agent will be placed on
     * @param startLocation the location on the given floor the agent will be put on
     * @param startsFacing  the direction the agent will start facing in
     * @param radius        the radius of the agent
     * @param visionRange
     * @param visionAngle
     */
    public DistanceAgent(Map map, Floor startingFloor, Vector2D startLocation, Direction startsFacing, int radius, double visionRange, double visionAngle)
    {
        super(map, startingFloor, startLocation, startsFacing, radius, visionRange, visionAngle);
    }

    public DistanceAgent(int id) {
        super(id);
    }

    @Override
    protected void completeRequest(AgentRequest request)
    {
        if(logger.isTraceEnabled())
        {
            logger.trace("Completing request for {}", this);
        }

        MapInfo info = super.mapInfo;
        System.out.println(info.getAgentPoints().get(0).getX());
        LinkedList<Vector2D> agentPoint = new LinkedList<>(info.getAgentPoints());
        for (Vector2D point: agentPoint){
            System.out.println("X: " + point.getX());
            System.out.println("Y: " + point.getY());
        }
        Vector2D currentLocation = super.location;
        double shortestDistance = Double.MAX_VALUE;
        int agentIndex=-1;
        for (int i=0; i<agentPoint.size(); i++){
            double distance = Math.abs(agentPoint.get(i).distance(currentLocation));
            System.out.println("Distnace: "+distance);
            if (distance<shortestDistance){
                shortestDistance = distance;
                agentIndex = i;
            }
        }
        System.out.println("AgentIndex: "+agentIndex);

        double pursuerX = agentPoint.get(agentIndex).getX();
        double pursuerY = agentPoint.get(agentIndex).getY();
        double evaderX = currentLocation.getX();
        double evaderY = currentLocation.getY();
        double deltaY = (pursuerY-evaderY);
        double deltaX = (pursuerX-evaderX);
        double ratio = deltaY/deltaX;

        double b = evaderY - (ratio*evaderX);

        boolean moveRight = false;
        if (deltaX<0){
            moveRight=true;
        } else {
            moveRight=false;
        }

        Vector2D newPoint = null;
        if (moveRight){
            double newX = evaderX + 40;
            double newY = (ratio*newX)+b;
            newPoint = new Vector2D(newX, newY);
        } else {
            double newX = evaderX - 40;
            double newY = (ratio*newX)+b;
            newPoint = new Vector2D(newX, newY);
        }

        request.add(new WalkToTask(newPoint));
    }

    @Override
    protected boolean hasNewRequest()
    {
        boolean hasNewRequest = mapInfo != null;
        if(logger.isTraceEnabled())
        {
            logger.trace("mapInfo != null: {}", mapInfo != null);
        }
        super.resetHasNewRequest();
        return hasNewRequest;
    }

    @Override
    public boolean isEvader()
    {
        return true;
    }

    @Override
    public String toString() {
        return "Distance" + super.toString();
    }

    @Override
    protected DistanceAgent clone() throws CloneNotSupportedException {
        DistanceAgent a = new DistanceAgent(this.getId());
        AbstractAgent.setProtectedValues(this, a);
        return a;
    }
}
