package nl.dke.pursuitevasion.map.impl;

import nl.dke.pursuitevasion.map.ObjectType;
import nl.dke.pursuitevasion.map.MapPolygon;

/**
 * Created by nik on 2/8/17.
 */
public class Gate extends Obstacle
{
    /**
     * The id of the gate this gate connects to
     */
    private final int otherGateID;

    /**
     * Create a gate object
     *
     * @param polygon the polygon of this object
     * @param id the id of this object
     * @param floorID the floorID of the floor this object is on
     * @param otherGateID the id of the gate this object connects to
     */
    public Gate(MapPolygon polygon, int id, int floorID, int otherGateID)
    {
        super(polygon, id, floorID);
        this.otherGateID = otherGateID;
    }

    /**
     * Get the id of the gate this gate connects to
     *
     * @return the id of the other gate
     */
    public int getOtherGateID()
    {
        return otherGateID;
    }

    @Override
    public ObjectType getType()
    {
        return ObjectType.GATE;
    }

}
