package nl.dke.pursuitevasion.game.agents;

import nl.dke.pursuitevasion.map.impl.Floor;
import nl.dke.pursuitevasion.map.impl.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;

/**
 * An agent in a Map environment. An agent can move about and rotate in the Map. It has a location (an x and y
 * coordinate),an angle(where 0 degrees = facing east/right and 90 degrees = facing north/up) and a radius
 * (the space occupied in the Map)
 *
 * Created by nik on 2/8/17.
 */
public abstract class AbstractAgent
{
    private final static Logger logger = LoggerFactory.getLogger(AbstractAgent.class);

    /**
     * The radius of the agent.
     */
    private int radius;

    /**
     * The location of the agent in the Map
     */
    protected Point location;

    /**
     * The direction the Agent is facing
     */
    protected Angle facing;

    /**
     * The Map environment the Agent exists in.
     */
    private Map map;

    /**
     * The floor the Agent is currently on.
     */
    private Floor floor;

    /**
     * The current requested movement in the world. Null if there is no desired movement
     */
    private AgentRequest request;

    /**
     * Prevents hasRequest from potentially computing it's value more than once.
     * This flag is set to true when hasRequest() has been called once.
     *
     */
    private volatile boolean hasRequestComputed;

    /**
     * Boolean value determining whether the current Request should be invalidated and a new
     * one should be calculated
     */
    private volatile boolean hasRequest;

    /**
     * Create an agent in the given Map
     *
     * @param map           the map where the agent is going to be interacting in
     * @param startingFloor the Floor (which is in the Map) the agent will be placed on
     * @param startLocation the location on the given floor the agent will be put on
     * @param startsFacing  the direction the agent will start facing in
     * @param radius        the radius of the agent
     */
    public AbstractAgent(Map map, Floor startingFloor, Point startLocation, Direction startsFacing, int radius)
    {
        this.map = map;
        this.floor = startingFloor;
        this.location = startLocation;
        this.facing = new Angle(startsFacing);
        this.radius = radius;
    }

    /**
     * Update the Agent based on a command
     */
    public void update(AgentCommand command)
    {
        if(logger.isTraceEnabled())
        {
            logger.trace("updating agent with command: {}", command);
        }

        if(command.isLocationChanged())
        {
            this.location = command.getLocation();
        }
        if(command.isAngleChanged())
        {
            this.facing = command.getAngle();
        }
    }

    /**
     * Gets a new Request from this agent. If it already gave a request, and that request has not been completed, a call
     * to this method will throw an IllegalStateException.
     *
     * If the method {@link #hasRequest()} returns true, this method will always give a new Request
     *
     * @return a new Request of this Agent
     * @throws IllegalStateException when a Request has already been given and not been completed yet
     */
    public AgentRequest getRequest()
            throws IllegalStateException
    {
        if(!hasRequest())
        {
            throw new IllegalStateException("Cannot get request as previous request is not fulfilled");
        }
        if(request != null && !request.isCompleted())
        {
            request.invalidate();
        }

        request = new AgentRequest(this);
        completeRequest(request);
        request.confirm();
        return request;
    }

    /**
     * Get the current location of this agent
     *
     * @return the current location of this agent
     */
    public synchronized Point getLocation()
    {
        return new Point(location);
    }

    /**
     * Get the angle in which the agent is currently facing
     *
     * @return the angle the agent is facing in
     */
    public synchronized double getFacingAngle()
    {
        return facing.getAngle();
    }

    /**
     * Get the radius of the Agent
     *
     * @return the radius of the Agent
     */
    public int getRadius()
    {
        return radius;
    }

    /**
     * This method will add tasks to a request until the desired state of the agent
     * will be reached by the request.
     *
     * @param request The request to complete with tasks
     */
    protected abstract void completeRequest(AgentRequest request);

    /**
     * Check if this agent has a request. This method returning true will guarantee a successful call to
     * {@link #getRequest()}
     *
     * @return whether this Agent has a new Request
     */
    public boolean hasRequest()
    {
        if(hasRequestComputed)
        {
            return hasRequest;
        }
        else
        {
            hasRequestComputed = true;
            hasRequest = hasNewRequest();
            return hasRequest;
        }
    }

    /**
     * Calculate whether a new Request is to be determined
     *
     * @return whether a new Request should be created
     */
    protected abstract boolean hasNewRequest();

    /**
     * Reset the boolean flag on the request, so it gets calculated again
     */
    public void resetHasNewRequest()
    {
        hasRequest = false;
    }

    /**
     * Get the floor the agent is on
     * @return the floor the agent is currently located on
     */
    public Floor getFloor()
    {
        return floor;
    }
}
