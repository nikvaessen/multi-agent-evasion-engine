package nl.dke.pursuitevasion.map.impl;

import nl.dke.pursuitevasion.map.AbstractObject;
import nl.dke.pursuitevasion.map.ObjectType;

import java.awt.*;

/**
 * Created by nik on 2/8/17.
 */
public class Gate extends AbstractObject
{
    public Gate(Polygon polygon)
    {
        super(polygon);
    }

    @Override
    public ObjectType getType()
    {
        return ObjectType.GATE;
    }
}
