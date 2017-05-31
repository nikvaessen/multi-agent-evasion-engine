package nl.dke.pursuitevasion.game.agents;

import nl.dke.pursuitevasion.game.EngineConstants;
import nl.dke.pursuitevasion.game.Vector2D;
import nl.dke.pursuitevasion.map.AbstractObject;
import nl.dke.pursuitevasion.map.impl.EntryEvader;
import nl.dke.pursuitevasion.map.impl.Floor;
import nl.dke.pursuitevasion.map.impl.Map;
import nl.dke.pursuitevasion.map.impl.Obstacle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

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
    protected Vector2D location;

    /**
     * The direction the Agent is facing
     */
    protected Angle facing;

    /**
     * The amount of distance the agent can see something in
     */
    protected double visionRange;

    /**
     * The view angle of this agent
     */
    protected double visionAngle;

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
    public AbstractAgent(Map map, Floor startingFloor, Vector2D startLocation, Direction startsFacing,
                         int radius, double visionRange, double visionAngle) {
        this.map = map;
        this.floor = startingFloor;
        setStartLocation(startLocation);

        this.facing = new Angle(startsFacing);
        this.radius = radius;
        this.visionRange = visionRange;
        this.visionAngle = visionAngle;
        this.visionArc = new VisionArc();


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
            this.location = command.getNewLocation();
        }
        if(command.isAngleChanged())
        {
            this.facing = command.getNewAngle();
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
    public synchronized Vector2D getLocation()
    {
        return location;
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

    /**
     * Get the vision range of the agent
     * @return the vision range of the agent
     */
    public double getVisionRange()
    {
        return visionRange;
    }

    /**
     * Set the vision range of the agent
     * @param visionRange the vision range
     */
    public void setVisionRange(double visionRange)
    {
        this.visionRange = visionRange;
    }

    /**
     * Get the visoon angle of the agent
     * @return the vision angle in degrees
     */
    public double getVisionAngle()
    {
        return visionAngle;
    }

    /**
     * Set the vision angle of the agent
     * @param visionAngle the vision angle in degrees
     */
    public void setVisionAngle(double visionAngle)
    {
        this.visionAngle = visionAngle;
    }

    /**
     * Get whether this agent can act as an evader or as an pursuer
     *
     * @return true is agent acts as an evader, false if agent acts as pursuer
     */
    public abstract boolean isEvader();

    @Override
    public String toString()
    {
        return String.format("Agent[location:%s, facing: %f", location.toString(), facing.getAngle());
    }

    public VisionArc getVisionArc()
    {
        return visionArc;
    }

    private VisionArc visionArc;

    private Collection<AbstractAgent> visibleAgents = new ArrayList<>();

    public Collection<AbstractAgent> getVisibleAgents(){
        return visibleAgents;
    }

    /** sets the start location inside the map boundaries
     * @param startLocation
     */
    private void setStartLocation(Vector2D startLocation) {
        this.location = startLocation; if (true) return;
        ArrayList<AbstractObject> startObject = new ArrayList<>(3);
        if (isEvader()){
            startObject.addAll(floor.getEntryEvader());
        }else startObject.addAll(floor.getEntryPursuer());

        if (startObject.size()==0) return;
        Random r = EngineConstants.getRandom();
        Polygon startArea = startObject.get(r.nextInt(startObject.size())).getPolygon();

        Vector2D point = new Vector2D(-6,-6); //first one has to be outside
        Rectangle2D bound = startArea.getBounds2D();
        while (!startArea.contains(point.getX(),point.getY())){
            point.setX(bound.getX() + r.nextDouble()*bound.getWidth());
            point.setY(bound.getY() + r.nextDouble()*bound.getHeight());
        }
        this.location = point;

    }

    public class VisionArc {

        Angle lowerAngle;
        Angle upperAngle;
        private Collection<AbstractAgent> agents;


        private VisionArc(){
            updateAngles();
        }

        private void updateAngles(){
            double facingAngle = getFacingAngle();
            upperAngle = new Angle( (facingAngle + visionAngle/2) % 360);
            lowerAngle = new Angle( (360 + facingAngle - visionAngle/2) % 360 );
        }

        public void update(Collection<AbstractAgent> agents){
            updateAngles();
            visibleAgents = getVisibleAgents(agents);
        }

        private Collection<AbstractAgent> getVisibleAgents(Collection<AbstractAgent> agents){
            // TODO: filter out own agent.

            // TODO: filter out agent on other floors
            Floor f = getFloor();
            // pre-calculate all lines created by obstacles in the floor
            Collection<Line2D> lines = f.getLines();
            // check which agents are within the area.
            ArrayList<AbstractAgent> visibleAgents = new ArrayList<AbstractAgent>();
            for(AbstractAgent agent : agents){
                Vector2D location = agent.getLocation();
                if(agent.getFloor() == f && inArc(location) && !isObstructed(location, lines)){
                    visibleAgents.add(agent);
                }
            }
            return visibleAgents;
        }

        private boolean isObstructed(Vector2D p, Collection<Line2D> lines){
            // check for obstacles that may obstruct line of sight to an agent
            // Check if a line from the agent to the other agent intersects an obstacle line

            Line2D ray = new Line2D.Double(location.getX(), location.getY(), p.getX(), p.getY());
            for(Line2D line : lines){
                if(ray.intersectsLine(line)){
                    return true;
                }
            }
            return false;
        }



        private boolean inArc(Vector2D p){
            if(p.distance(location) > visionRange){
                return false;
            }
            // calculate angle between the origin and the point
            double opposite = location.getY() - p.getY();
            double adjacent = p.getX() - location.getX();
            double angle = Math.toDegrees(Math.atan2(opposite, adjacent));
            if(angle < 0){angle += 360;}
            if(angle >= lowerAngle.getAngle() && angle <= upperAngle.getAngle()){
                return true;
            }
            return false;

        }

        private Vector2D calculateBase()
        {
            int dx = new Long(Math.round(Math.cos(facing.getRadians()) * radius)).intValue();
            //change sign of y because y works reversed from normal cartesian plane
            int dy = - new Long(Math.round(Math.sin(facing.getRadians()) * radius)).intValue();

            if(logger.isTraceEnabled())
            {
                logger.trace("changing base with dx {} and dy {} facing {}, {}",
                        dx, dy, facing.getAngle(), facing.getRadians());
            }

            return new Vector2D(location.getX() + Math.round(dx), location.getY() + Math.round(dy));
        }

        public Vector2D getBasePoint() {
            return calculateBase();
        }
    }


}
