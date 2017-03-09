package nl.dke.pursuitevasion.game.agents;

import java.awt.*;

/**
 * This class represent data which is passed from the engine to an agent.
 * It tells the agent what it is allowed to change in this iteration of the simulation
 *
 * Created by nik on 03/03/17.
 */
public class AgentCommand
{
    private AbstractAgent agent;

    private Point location;
    private Angle angle;
    private boolean locationChanged;
    private boolean angleChanged;

    public AgentCommand(AbstractAgent agent, Point newLocation, Angle newAngle)
    {
        this.agent = agent;
        this.location = newLocation;
        this.angle = newAngle;
        locationChanged = true;
        angleChanged = true;
    }

    public AgentCommand(AbstractAgent agent, Point location)
    {
        this.agent = agent;
        this.location = location;
        locationChanged = true;
    }

    public AgentCommand(AbstractAgent agent, Angle angle)
    {
        this.agent = agent;
        this.angle = angle;
        angleChanged = true;
    }

    public void apply()
    {
        agent.update(this);
    }

    public Point getLocation()
    {
        return location;
    }

    public Angle getAngle()
    {
        return angle;
    }

    public AbstractAgent getAgent()
    {
        return agent;
    }

    public boolean isLocationChanged()
    {
        return locationChanged;
    }

    public boolean isAngleChanged()
    {
        return angleChanged;
    }

    public double getMovedDistance()
    {
        return location.distance(agent.getLocation());
    }

    public double getRotatedDistance()
    {
        return angle.distance(agent.getFacingAngle());
    }
}
