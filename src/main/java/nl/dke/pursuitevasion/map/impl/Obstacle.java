package nl.dke.pursuitevasion.map.impl;

import nl.dke.pursuitevasion.map.AbstractObject;
import nl.dke.pursuitevasion.map.MapPolygon;
import nl.dke.pursuitevasion.map.ObjectType;

/**
 * Created by nik on 2/8/17.
 */
public class Obstacle extends AbstractObject
{
    /**
     * The id of the floor this obstacle is placed on
     */
    private final int floorID;

    /**
     * Create an obstacle

     * @param polygon the polygon of this obstacle
     * @param id the id of this obstacle
     * @param floorID the id of the floor this obstacle is placed on
     */
    public Obstacle(MapPolygon polygon, int id, int floorID)
    {
        super(polygon, id);
        this.floorID = floorID;
    }

    /**
     * Get the id of the floor this obstacle belongs to

     * @return the id of the floor
     */
    public int getFloorID()
    {
        return floorID;
    }

    @Override
    public ObjectType getType()
    {
        return ObjectType.OBSTACLE;
    }
}
