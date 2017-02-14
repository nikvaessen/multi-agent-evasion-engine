package nl.dke.pursuitevasion.map.builders;

import nl.dke.pursuitevasion.map.impl.Floor;
import nl.dke.pursuitevasion.map.impl.Gate;
import nl.dke.pursuitevasion.map.impl.Obstacle;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by nik on 2/9/17.
 */
public class FloorBuilder
{
    /**
     * Collection of obstacles which will be placed on the floor
     */
    private Collection<Obstacle> obstacles;

    /**
     * Collection of gates which will be placed on the floor
     */
    private Collection<Gate> gates;

    /**
     * The MapBuilder which is using this FloorBuilder to create a floor
     */
    private MapBuilder builder;

    /**
     * The ID register which is used to give this floor and every object being placed on the floor
     * an unique id
     */
    private IDRegister register;

    /**
     * The polygon of this floor
     */
    private Polygon polygon;

    /**
     * Boolean flag to make sure this floor is only built once
     */
    private boolean constructed;

    /**
     * The id of the floor being constructed
     */
    private int floorID;

    /**
     * Create a FloorBuilder object
     *
     * @param builder the MapBuilder which the floor being built is placed on
     * @param register the IDRegister to give every object an unique ID
     */
    protected FloorBuilder(Polygon polygon, MapBuilder builder, IDRegister register)
    {
        this.obstacles = new ArrayList<Obstacle>();
        this.gates = new ArrayList<Gate>();
        this.builder = builder;
        this.register = register;
        this.polygon = polygon;
        constructed = false;
        floorID = register.getUniqueID();
    }

    /**
     * Add a Obstacle object with the given polygon to this floor

     * @param polygon the shape of the Obstacle to be addeed to this floor
     * @return the same FloorBuilder object
     * @throws IllegalArgumentException when the given polygon is outside the floor
     */
    public FloorBuilder addObstacle(Polygon polygon)
    {
        checkPolygonIsInside(polygon);
        obstacles.add(new Obstacle(polygon, register.getUniqueID(), floorID));
        return this;
    }

    /**
     * Gives the MapBuilder object which uses this FloorBuilder object to make a Floor on its map
     *
     * @return the MapBuilder object this floor is being built in
     */
    public MapBuilder finish()
    {
        return builder;
    }

    /**
     * Add a Gate to this floor
     *
     * @param gate the gate to be added to this floor
     * @throws IllegalArgumentException when the polygon of the given gate is outside the floor
     */
    protected void addGate(Gate gate)
        throws IllegalArgumentException
    {
        checkPolygonIsInside(gate.getPolygon());
        gates.add(gate);
    }

    /**
     * Get the floor object which has been constructed by this builder. This can only be done once
     *
     * @return the floor object
     * @throws IllegalStateException when the floor has already been constructed once
     */
    protected synchronized Floor constructFloor()
    {
        if(constructed)
        {
            throw new IllegalStateException("Floor has already been constructed once");
        }
        constructed = true;
        return new Floor(polygon, floorID, obstacles, gates);
    }

    /**
     * Check if a given polygon p is inside the polygon of the floor object being created
     * @param p the polygon to checl
     * @throws IllegalArgumentException when the given Polygon is not inside the polygon of the floor
     */
    private void checkPolygonIsInside(Polygon p)
    {
        if(!this.polygon.contains(p.getBounds2D()))
        {
            throw new IllegalArgumentException(String.format("The given polygon %s is not totally inside the polygon" +
                    " of this floor's polygon %s", polygonToString(p), polygonToString(this.polygon)));
        }
    }

    /**
     * Give a string representation of a polygon which includes a list of the coordinates of the vertexes
     * @param p the polygon to get the string from
     * @return the string representation including coordinates of vertexes of the given polygon
     */
    public static String polygonToString(Polygon p)
    {
        int[] xpoints = p.xpoints;
        int[] ypoints = p.ypoints;
        if(xpoints.length != ypoints.length)
        {
            throw new IllegalStateException("given polygon does not have as much x points as y points");
        }

        String stringOfCoords = "";
        int i;
        for(i = 0; i < xpoints.length - 1; i++)
        {
            stringOfCoords += String.format("(%d, %d),", xpoints[i], ypoints[i]);
        }
        stringOfCoords += String.format("(%d, %d)", xpoints[i], ypoints[i]);

        return p.toString() + String.format("[%s]", stringOfCoords);
    }

}
