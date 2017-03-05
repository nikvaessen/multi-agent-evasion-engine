package nl.dke.pursuitevasion.game.agents;

import java.awt.*;

/**
 * Created by nik on 2/8/17.
 */
public abstract class AbstractAgent
{
    private int radius;

    protected Point location;

    protected Angle facing;

    protected boolean moving;

    private AgentRequest request;

    public AbstractAgent(Point startLocation, Direction startsFacing, int radius)
    {
        this.location = startLocation;
        this.radius = radius;
        this.facing = new Angle(startsFacing);
    }

    /**
     *
     */
    public void update(AgentCommand command)
    {

    }

    public AgentRequest getRequest()
        throws IllegalStateException
    {
        if(request == null || request.isCompleted())
        {
            request = new AgentRequest(this);
            fillInRequest(request);
            request.confirm();
            return request;
        }
        throw new IllegalStateException("Cannot get request has previous request is not fulfilled");
    }

    public synchronized Point getLocation()
    {
        return location;
    }

    public double getFacingAngle()
    {
        return facing.getAngle();
    }

    public int getRadius()
    {
        return radius;
    }

    public boolean isMoving()
    {
        return moving;
    }

    protected abstract void fillInRequest(AgentRequest request);

}
