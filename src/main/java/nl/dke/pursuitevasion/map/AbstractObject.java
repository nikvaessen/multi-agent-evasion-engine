package nl.dke.pursuitevasion.map;

import java.awt.*;

/**
 * A general object, which can be placed in the map.
 *
 * A object is made of a polygon, and has a unique ID.
 *
 * Created by nik on 2/8/17.
 */
public abstract class AbstractObject
{
    /**
     * The unique id of this object
     */
    private final int id;

    /**
     * The polygon of this object
     */
    private final Polygon polygon;

    /**
     * General constructor which registers the object and gets a unique idea
     */
    public AbstractObject(Polygon polygon, int id)
    {
        this.id = id;
        this.polygon = polygon;
    }

    /**
     * Get which kind of object it is
     * @return the ObjectType of the object
     */
    public abstract ObjectType getType();

    /**
     * Get the unique ID of the object
     * @return the unique ID of the object
     */
    public int getID()
    {
        return id;
    }

    /**
     * The polygon which this object is made of
     * @return the polygon of this object
     */
    public Polygon getPolygon()
    {
        return polygon;
    }

    @Override
    public int hashCode()
    {
        return id;
    }

    @Override
    public boolean equals(Object o)
    {
        return (o instanceof AbstractObject) && ((AbstractObject) o).getID() == this.id;
    }
}
