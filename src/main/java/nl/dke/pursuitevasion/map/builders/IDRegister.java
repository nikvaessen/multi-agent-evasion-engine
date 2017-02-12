package nl.dke.pursuitevasion.map.builders;

import nl.dke.pursuitevasion.map.AbstractObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A class which is used to get unique ID's for every Object being loaded into memory
 *
 * A IDregister should be unique for every map
 *
 * Created by nik on 2/8/17.
 */
public class IDRegister
{
    /**
     * Counts the number if registered objects. The count + 1 is the unique id of every newly registred object
     */
    private int count;

    /**
     * Construct an empty IDRegister
     */
    public IDRegister()
    {
        count = 0;
    }

    /**
     * Get a unique ID which has been returned yet
     *
     * @return an integer value not returned previously
     */
    public synchronized int getUniqueID()
    {
        int id = count;
        count++;
        if(count == 0)
        {
            count = -1;
            throw new IllegalStateException("IDRegister ran out of unique ID's");
        }
        return id;
    }

}
