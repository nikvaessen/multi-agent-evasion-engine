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

    private Point.Double location;
    private Angle angle;
    private boolean locationChanged;
    private boolean angleChanged;

    public AgentCommand(AbstractAgent agent, Point.Double newLocation, Angle newAngle)
    {
        this.agent = agent;
        this.location = newLocation;
        this.angle = newAngle;
        locationChanged = true;
        angleChanged = true;
    }

    public AgentCommand(AbstractAgent agent, Point.Double location)
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

    public Point.Double getLocation()
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

    public double getMovedDistance() {
        if (isLocationChanged())
        {
            return location.distance(agent.getLocation());
        }
        return 0;
    }

    public double getRotatedDistance() {
        if (isAngleChanged()) {

            return angle.distance(agent.getFacingAngle());
        }
        return 0;
    }

    @Override
    public String toString() {
        return String.format("AgentCommand[location{%b, %s},angle{%b, %s}]",
                locationChanged, location,
                angleChanged, angle);
    }
}
