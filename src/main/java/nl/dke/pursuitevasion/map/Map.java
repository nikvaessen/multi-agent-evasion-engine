package nl.dke.pursuitevasion.map;

import nl.dke.pursuitevasion.map.impl.Floor;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A map defines polygon objects on which to "play" the pursuit-evasion game
 *
 * A map consists of floors
 *
 * Created by nik on 2/8/17.
 */
public class Map
{
    /**
     * Floors in the map
     */
    private Collection<Floor> floors = new ArrayList<Floor>();

    private IDRegister idRegister = new IDRegister();


}
