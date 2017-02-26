package nl.dke.pursuitevasion.game.agents;

import java.awt.*;
import java.util.Vector;

/**
 * Created by nik on 2/8/17.
 */
public abstract class AbstractAgent
{
    private int radius;

    protected Point location;

    protected Direction facing;

    protected boolean moving;

    public AbstractAgent(Point startLocation, Direction startsFacing, int radius)
    {
        this.location = startLocation;
        this.radius = radius;
        this.facing = startsFacing;
    }

    public synchronized Point getLocation()
    {
        return location;
    }

    public int getRadius()
    {
        return radius;
    }

    public Direction getFacing()
    {
        return facing;
    }

    public boolean isMoving()
    {
        return moving;
    }

    /**
     * Make the agent decide where to go.
     */
    public void decide()
    {

    }
}
