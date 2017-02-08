package nl.dke.pursuitevasion.map.impl;

import nl.dke.pursuitevasion.map.AbstractObject;
import nl.dke.pursuitevasion.map.ObjectType;

import java.awt.*;
import java.util.List;

/**
 * A floor
 *
 * Created by nik on 2/8/17.
 */
public class Floor extends AbstractObject
{

    /**
     *
     */
    private List<Obstacle> obstacles;

    /**
     *
     */
    private List<Gate> gates;

    public Floor(Polygon polygon)
    {
        super(polygon);
    }

    @Override
    public ObjectType getType()
    {
        return ObjectType.FLOOR;
    }
}
