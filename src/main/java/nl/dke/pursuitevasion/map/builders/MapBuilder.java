package nl.dke.pursuitevasion.map.builders;

import nl.dke.pursuitevasion.map.MapPolygon;
import nl.dke.pursuitevasion.map.impl.Map;
import nl.dke.pursuitevasion.map.impl.Floor;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Created by nik on 2/9/17.
 */
public class MapBuilder
{

    /**
     * The collections of FloorBuilders which gets used to build the floors of the map
     */
    private Collection<FloorBuilder> floorBuilders;

    /**
     * Is used to get a unique ID for every object being created on a single map
     */
    private IDRegister register;

    /**
     * Create a MapBuilder object
     */
    private MapBuilder()
    {
        //instantiate member variables
        this.floorBuilders = new ArrayList<>();
        this.register = new IDRegister();
    }

    /**
     * Create an instance of a MapBuilder. This MapBuilder goes through the creation of the
     * specified amount of floors
     *
     * @return The MapBuilder object to create the map with the specified amount of floors
     */
    public static MapBuilder create()
    {
        return new MapBuilder();
    }

    /**
     * Get a FloorBuilder to create a floor on this map
     *
     * @param polygon the polygon of the floor being made
     * @return the FloorBuilder object to make the floor with
     */
    public FloorBuilder makeFloor(MapPolygon polygon)
    {
        FloorBuilder floorBuilder = new FloorBuilder(polygon, this, register);
        floorBuilders.add(floorBuilder);
        return floorBuilder;
    }

    /**
     * Get a GateBuilder object which is used to create gates between floors already made
     * currently.
     *
     * @return A GateBuilder object
     */
    public GateBuilder makeGates()
        throws IllegalStateException
    {
        //todo create the GateBuilder object
        throw new UnsupportedOperationException("GateBuilder is not implemented");
    }

    /**
     * Builds the map given the created floors
     *
     * @return a Map object holding every floor made using this builder
     */
    public Map build()
    {
        return new Map(constructFloors());
    }

    /**
     * Construct the floors made by the FloorBuilders
     *
     * @return the list of floors which were made
     */
    private Collection<Floor> constructFloors()
    {
        Collection<Floor> floors = new ArrayList<>();
        for(FloorBuilder builder : floorBuilders)
        {
            floors.add(builder.constructFloor());
        }
        return floors;
    }

}
