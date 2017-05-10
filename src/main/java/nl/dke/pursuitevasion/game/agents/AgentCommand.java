package nl.dke.pursuitevasion.game.agents;

import nl.dke.pursuitevasion.game.Vector2D;

/**
 * This class represent data which is passed from the engine to an agent.
 * It tells the agent what it is allowed to change in this iteration of the simulation
 * <p>
 * Created by nik on 03/03/17.
 */
public class AgentCommand
{
    /**
     * The agent this command should be applied to
     */
    private AbstractAgent agent;

    /**
     * The new location on the map of the agent
     */
    private Vector2D newLocation;

    /**
     * The new newAngle the agent is facing
     */
    private Angle newAngle;

    /**
     * True if the location of the agent has changed
     */
    private boolean locationChanged;

    /**
     * True if the rotation of the agent has changed
     */
    private boolean angleChanged;

    /**
     * True is this command has been applied once to the Agent
     */
    private boolean applied = false;

    /**
     * Create an AgentCommand with a new location and newAngle
     *
     * @param agent       the agent this command should be applied on
     * @param newLocation the new location of the agent
     * @param newAngle    the new newAngle of the agent
     */
    public AgentCommand(AbstractAgent agent, Vector2D newLocation, Angle newAngle)
    {
        this.agent = agent;
        this.newLocation = newLocation;
        this.newAngle = newAngle;
        locationChanged = true;
        angleChanged = true;
    }

    /**
     * Create an AgentCommand with a new location
     *
     * @param agent    the agent this command should be applied on
     * @param location the new location of the agent
     */
    public AgentCommand(AbstractAgent agent, Vector2D location)
    {
        this.agent = agent;
        this.newLocation = location;
        locationChanged = true;
        angleChanged = false;
    }

    /**
     * Create an AgentCommand with a new location and newAngle
     *
     * @param agent    the agent this command should be applied on
     * @param newAngle the new newAngle of the agent
     */
    public AgentCommand(AbstractAgent agent, Angle newAngle)
    {
        this.agent = agent;
        this.newAngle = newAngle;
        locationChanged = false;
        angleChanged = true;
    }

    /**
     * Apply this command to the agent
     */
    public void apply()
    {
        if(!applied)
        {
            applied = true;
            agent.update(this);
        }
        else
        {
            throw new IllegalStateException("Command has already been applied once");
        }
    }

    /**
     * Get the new location of the agent
     *
     * @return the new location of the agent
     */
    public Vector2D getNewLocation()
    {
        return newLocation;
    }

    /**
     * Get the new angle of the agent
     *
     * @return the new angle of the agent
     */
    public Angle getNewAngle()
    {
        return newAngle;
    }

    /**
     * Get the agent this command should be applied on
     *
     * @return the agent this command should be applied on
     */
    public AbstractAgent getAgent()
    {
        return agent;
    }

    /**
     * Get whether this command changes the location of the agent
     *
     * @return whether the location is changed
     */
    public boolean isLocationChanged()
    {
        return locationChanged;
    }

    /**
     * Get whether this command changed the rotation of the agent
     *
     * @return whether the angle is changed
     */
    public boolean isAngleChanged()
    {
        return angleChanged;
    }

    /**
     * Calculate the distance between the current location and the new location of the agent
     *
     * @return the distance between the old and new location of the agent
     */
    public double getMovedDistance()
    {
        if(isLocationChanged())
        {
            return newLocation.distance(agent.getLocation());
        }
        return 0;
    }

    /**
     * Get the amount in degrees between the current angle and the new angle of the agent
     *
     * @return the difference in degrees between the old and new angle of the agent
     */
    public double getRotatedDistance()
    {
        if(isAngleChanged())
        {
            return newAngle.distance(agent.getFacingAngle());
        }
        return 0;
    }

    @Override
    public String toString()
    {
        return String.format("AgentCommand[newLocation{%b, %s},newAngle{%b, %s}]",
                             locationChanged, newLocation,
                             angleChanged, newAngle);
    }
}
