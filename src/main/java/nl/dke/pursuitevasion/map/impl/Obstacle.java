package nl.dke.pursuitevasion.map.impl;

import nl.dke.pursuitevasion.map.AbstractObject;
import nl.dke.pursuitevasion.map.ObjectType;

import java.awt.*;

/**
 * Created by nik on 2/8/17.
 */
public class Obstacle extends AbstractObject
{
    /**
     * The id of the floor this obstacle is placed on
     */
    public int floorID;


    public Obstacle(Polygon polygon, int floorID)
    {
        super(polygon);
        if(!super.getIdRegister().contains(floorID))
        {
            throw new IllegalArgumentException(
                    String.format("Cannot construct obstacle as there is no floor with id %d", floorID));
        }
        getIdRegister().getObject(floorID);
    }

    @Override
    public ObjectType getType()
    {
        return ObjectType.OBSTACLE;
    }
}
