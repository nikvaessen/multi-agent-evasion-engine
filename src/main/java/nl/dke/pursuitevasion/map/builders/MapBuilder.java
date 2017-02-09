package nl.dke.pursuitevasion.map.builders;

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
     * The iterator of the FloorBuilders. This iteration needs to be exhausted before gates can be
     * added and before all floors can be build
     */
    private Iterator<FloorBuilder> floorBuilderIterator;

    /**
     * Stores the amount of floors getting created
     */
    private int amountOfFloors;

    /**
     * Is used to get a unique ID for every object being created on a single map
     */
    private IDRegister register;

    private MapBuilder(int amountOfFloors)
    {
        //check if amount of floors is negative
        validAmountOfFloors(amountOfFloors);

        //instantiate member variables
        this.amountOfFloors = amountOfFloors;
        this.floorBuilders = new ArrayList<FloorBuilder>(amountOfFloors);
        this.register = new IDRegister();

        //create the FloorBuilders for the necessary amount of floors
        for(int i = 0; i < amountOfFloors; i++)
        {
            floorBuilders.add(new FloorBuilder(this, register));
        }

        //and create the iterator of the list of floors, which must be exhausted before
        //building can be completed
        this.floorBuilderIterator = floorBuilders.iterator();
    }

    /**
     * Create an instance of a MapBuilder. This MapBuilder goes through the creation of the
     * specified amount of floors
     *
     * @param amountOfFloors the amount of floors which are going to be created by the MapBuilder
     * @return The MapBuilder object to create the map with the specified amount of floors
     */
    public static MapBuilder create(int amountOfFloors)
    {
        return new MapBuilder(amountOfFloors);
    }

    /**
     * Get a FloorBuilder to create a floor on this map
     *
     * @return the FloorBuilder object to make the floor with
     * @throws IllegalStateException when there are no floors left to be made
     */
    public FloorBuilder makeFloor()
        throws IllegalStateException
    {
        checkFloorCanBeMade();
        return floorBuilderIterator.next();
    }


    /**
     * make a gate between two floors. The number of the floor should be between
     * 1 and the given amount of floors in the constructor
     * @param floorNumber1 the number of the first floor
     * @param floorNumber2 the number of the second floor
     * @param gate1 the polygon which is the gate placed on the first floor
     * @param gate2 the polygon which is the gate placed on the second floor
     * @return this mapbuilder
     * @throws IllegalStateException when gates cannot be added because not all floors are constructed
     * @throws IllegalArgumentException when the given floorNumbers are invalid
     */
    public MapBuilder makeGate(int floorNumber1, int floorNumber2, Polygon gate1, Polygon gate2)
        throws IllegalStateException
    {
        checkGateCanBeMade(floorNumber1, floorNumber2);
        //todo create the gates
        return this;
    }

    /**
     * Verify that all polygons are correct
     */
    public void verify()
    {
        //todo do the verification
    }

    /**
     * Builds the map given the created floors
     * @return
     */
    public Map build()
    {
        checkCanBuild();
        return new Map(constructFloors());
    }

    /**
     * Construct the floors made by the FloorBuilders
     * @return the list of floors which were made
     */
    private Collection<Floor> constructFloors()
    {
        //todo create the floors
        return new ArrayList<Floor>();
    }

    /**
     * check if the specified number of floors is a valid number, (not 0 or negative)
     * @param amountOfFloors the integer to check
     * @return whether it's a natural number or not (excluding 0 from N)
     */
    private boolean validAmountOfFloors(int amountOfFloors)
    {
        return amountOfFloors > 0;
    }

    /**
     * checks if Map can be built
     * @throws IllegalStateException when floors still need to be constructed
     */
    private void checkCanBuild()
            throws IllegalStateException
    {
        if(floorBuilderIterator.hasNext())
        {
            throw new IllegalStateException("all floors need to be made before Map can be built");
        }
    }

    /**
     * Checks if there are floors left to be constructed
     * @throws IllegalStateException when there are floors left to be constructed
     */
    private void checkFloorCanBeMade()
            throws IllegalStateException
    {
        if(!floorBuilderIterator.hasNext())
        {
            throw new IllegalStateException("FloorBuilder iterator has run out. No more floors" +
                    " can be made");
        }
    }

    /**
     * checks if all floors are constructed
     * @throws IllegalStateException when not all floors are constructed
     * @throws IllegalArgumentException when the given floor numbers are not valid
     */
    private void checkGateCanBeMade(int floor1, int floor2)
            throws IllegalStateException
    {
        if(floorBuilderIterator.hasNext())
        {
            throw new IllegalStateException("All floors need to be made before " +
                    "a gate can be constructed");
        }
        if(!insideFloorConstraints(floor1) || !insideFloorConstraints(floor2))
        {
            throw new IllegalArgumentException(
                    String.format("One of the given floors %d and %d do not exist", floor1,floor2));
        }
    }

    /**
     * check if the given number is between 0 and the amount of floor to be created
     * @param n the number to check
     * @return if the number corresponds to a floor number
     */
    private boolean insideFloorConstraints(int n)
    {
        return n > 0 && n <= amountOfFloors;
    }

}
