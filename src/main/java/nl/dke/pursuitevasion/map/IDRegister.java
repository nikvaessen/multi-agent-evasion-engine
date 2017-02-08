package nl.dke.pursuitevasion.map;

import java.util.ArrayList;
import java.util.List;

/**
 * A singleton class which is used to get unique ID's for every Object being loaded into memory
 *
 * Created by nik on 2/8/17.
 */
public class IDRegister
{
    /**
     * The singleton instance of this object
     */
    private static IDRegister ourInstance = new IDRegister();

    /**
     * Get the Singleton object
     * @return the only IDRegister
     */
    public static IDRegister getInstance()
    {
        return ourInstance;
    }

    /**
     * Store all the pairs
     */
    private List<Pair> pairs;

    /**
     * Construct an empty IDRegister
     */
    private IDRegister()
    {
        pairs = new ArrayList<Pair>();
    }

    /**
     * Register a new object to the ID register
     * @param o the object to register
     * @return the id of the object which was registered
     */
    public int register(AbstractObject o)
    {
        int count = pairs.size();
        pairs.add(new Pair(o, count));
        return count;
    }

    /**
     * Get an object by id
     * @param id the id of the object being requested
     * @return the object with that particular id
     * @throws IllegalArgumentException when the given id is not valid
     */
    public AbstractObject getObject(int id)
            throws IllegalArgumentException
    {
        if(id < 0 || id > pairs.size())
        {
            throw new IllegalArgumentException(String.format("given id %d does not exist", id));
        }
        return pairs.get(id).getObject();
    }

    /**
     * A pair links a given ID to the Object
     */
    private class Pair
    {
        AbstractObject o;
        int id;

        Pair(AbstractObject o, int i)
        {
            this.o = o;
            id = i;
        }

        public int getId()
        {
            return id;
        }

        public AbstractObject getObject()
        {
            return o;
        }
    }
}
