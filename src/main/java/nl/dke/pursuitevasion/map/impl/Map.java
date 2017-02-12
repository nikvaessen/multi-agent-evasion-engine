package nl.dke.pursuitevasion.map.impl;

import nl.dke.pursuitevasion.map.AbstractObject;
import nl.dke.pursuitevasion.map.builders.MapBuilder;
import nl.dke.pursuitevasion.map.impl.Floor;
import nl.dke.pursuitevasion.map.impl.Gate;
import nl.dke.pursuitevasion.map.impl.Obstacle;

import java.io.Serializable;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * A map defines polygon objects on which to "play" the pursuit-evasion game
 *
 * A map consists of floors.
 *
 * Each floor can contain obstacles
 *
 * Created by nik on 2/8/17.
 */
public class Map implements Serializable
{
    /**
     * Every map needs a name
     */
    private String name = "";

    /**
     * Floors in the map
     */
    private Collection<Floor> floors;

    /**
     * Construct the map created by one or more floors
     *
     * @param floors the floors which make up the map
     */
    public Map(Collection<Floor> floors)
    {
        this.floors = Collections.unmodifiableCollection(floors);
    }

    /**
     * Get a read-only Collection of the floors which make up this map
     *
     * @return the collection of floors on this map
     */
    public Collection<Floor> getFloors()
    {
        return floors;
    }

    public static Map loadFile() {
        //todo implement loadFile and file chooser
        return null;
    }

    public static void saveToFile(Map map) {
        //todo implementlo saveFile and file chooser
    }

    /**
     * Get the collection of polygons which make up all objects of this map
     *
     * @return An ArrayList of Polygons making up this map
     */
    public Collection<Polygon> getPolygons()
    {
        ArrayList<Polygon> polygons = new ArrayList<>();
        fillPolygonList(polygons);
        polygons.trimToSize();
        return polygons;
    }

    /**
     * Fill a Collection of polygons with all Polygons of this map
     *
     * @param polygons the list of polygons to fill
     */
    private void fillPolygonList(Collection<Polygon> polygons)
    {
        fillObjectList(floors, polygons);
        for(Floor floor : floors)
        {
            fillObjectList(floor.getObstacles(), polygons);
            fillObjectList(floor.getGates(), polygons);
        }
    }

    /**
     * Fill a collection of polygons with the polygons of a collection of AbstractObjects
     *
     * @param collection the collection of AbstractObjects
     * @param polygons the collection of polygons to fill
     */
    private void fillObjectList(Collection<? extends AbstractObject> collection, Collection<Polygon> polygons)
    {
        for(AbstractObject object : collection)
        {
            polygons.add(object.getPolygon());
        }
    }

    /**
     * Get a simple Map which is a rectangle floor with a box in the middle

     * @return A map object containing a simple map
     */
    public static Map getSimpleMap()
    {
        Polygon mainFloor = new Polygon(
                new int[] {   0, 100, 100,   0},
                new int[] {   0,   0, 100, 100},
                4
        );

        Polygon obstacle = new Polygon(
                new int[] {40, 60, 60, 40},
                new int[] {40, 40, 60, 60},
                4
        );

        return MapBuilder.create()
                .makeFloor(mainFloor)
                .addObstacle(obstacle)
                .finish()
                .build();
    }

}
