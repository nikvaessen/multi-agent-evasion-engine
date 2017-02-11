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
    private Collection<Obstacle> obstacles;
    private Collection<Gate> gates;
    private MapBuilder builder;
    private IDRegister register;
    private int floorID;
    private Polygon polygon;

    protected FloorBuilder(MapBuilder builder, IDRegister register)
    {
        this.obstacles = new ArrayList<Obstacle>();
        this.gates = new ArrayList<Gate>();
        this.builder = builder;
        this.register = register;
        this.floorID = register.getUniqueID();
    }

    public FloorBuilder addObstacle(Polygon p)
    {
        obstacles.add(new Obstacle(p, register.getUniqueID(), floorID));
        return this;
    }

    public MapBuilder  finish(Polygon polygon)
    {
        this.polygon = polygon;
        return builder;
    }

    public void addGate(Gate gate)
    {
        gates.add(gate);
    }

    protected Floor constructFloor()
    {
        return new Floor(polygon, floorID, obstacles, gates);
    }




}
