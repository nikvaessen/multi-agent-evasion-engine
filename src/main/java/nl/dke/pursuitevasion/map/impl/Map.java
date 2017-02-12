package nl.dke.pursuitevasion.map.impl;

import nl.dke.pursuitevasion.map.builders.MapBuilder;
import nl.dke.pursuitevasion.map.impl.Floor;
import nl.dke.pursuitevasion.map.impl.Gate;
import nl.dke.pursuitevasion.map.impl.Obstacle;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

/**
 * A map defines polygon objects on which to "play" the pursuit-evasion game
 *
 * A map consists of floors.
 *
 * Each floor can contain obstacles and
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
     * @param floors the floors which make up the map
     */
    public Map(Collection<Floor> floors)
    {
        this.floors = Collections.unmodifiableCollection(floors);
    }

    /**
     * Get a read-only list of the floors which make up this map
     * @return the list of floors on this map
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
}
