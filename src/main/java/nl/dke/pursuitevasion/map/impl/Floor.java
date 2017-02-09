package nl.dke.pursuitevasion.map.impl;

import nl.dke.pursuitevasion.map.AbstractObject;
import nl.dke.pursuitevasion.map.ObjectType;

import java.awt.*;
import java.util.List;

/**
 * A floor is a 2d level in which agents can walk
 *
 * Created by nik on 2/8/17.
 */
public class Floor extends AbstractObject
{

    /**
     * Obstacles can be placed on the floor to obstruct vision
     */
    private List<Obstacle> obstacles;

    /**
     * Gates can be placed on the floor to go to another floor
     */
    private List<Gate> gates;

    public Floor(Polygon polygon)
    {
        super(polygon);
    }

    public void addObstacle(Obstacle obstacle)
            throws IllegalArgumentException
    {
        if(obstacle.)
        if(obstacles.contains(obstacle))
        {
            throw new IllegalArgumentException("given obstacle is already in the floor");
        }
        obstacles.add(obstacle);
    }

    public void addGate()
    {

    }

    @Override
    public ObjectType getType()
    {
        return ObjectType.FLOOR;
    }
}
