package nl.dke.pursuitevasion.map.impl;

import nl.dke.pursuitevasion.map.AbstractObject;
import nl.dke.pursuitevasion.map.ObjectType;

import java.awt.*;

/**
 * Created by nik on 2/8/17.
 */
public class Obstacle extends AbstractObject
{

    public Obstacle(Polygon polygon)
    {
        super(polygon);
    }

    @Override
    public ObjectType getType()
    {
        return ObjectType.OBSTACLE;
    }
}
