package nl.dke.pursuitevasion.map.impl;

import nl.dke.pursuitevasion.map.AbstractObject;
import nl.dke.pursuitevasion.map.ObjectType;
import nl.dke.pursuitevasion.map.MapPolygon;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * A floor is a 2d level in which agents can walk
 *
 * Created by nik on 2/8/17.
 */
public class Floor extends AbstractObject
{
    public Collection<Line2D> getLines(){return lines;}

    private Collection<Line2D> constructLines(){
        ArrayList<Polygon> obstructions = new ArrayList<>();
        obstructions.add(this.getPolygon());
        for (Obstacle obstacle : this.getObstacles()) {
            obstructions.add(obstacle.getPolygon());
        }

        ArrayList<Line2D> lines = new ArrayList<>();
        // Get the lines for all objects in the floor (includes the floor).
        for(Polygon polygon : obstructions){
            lines.addAll(getLines(polygon));
        }
        return lines;
    }

    private ArrayList<Line2D> getLines(Polygon polygon){
        ArrayList<java.awt.geom.Line2D> lines = new ArrayList<>();
        Point2D start = null;
        Point2D last = null;
        for (PathIterator iter = polygon.getPathIterator(null); !iter.isDone(); iter.next()) {
            double[] points = new double[6];
            int type = iter.currentSegment(points);
            if (type == PathIterator.SEG_MOVETO) {
                Point2D moveP = new Point2D.Double(points[0], points[1]);
                last = moveP;
                start = moveP;
            } else if (type == PathIterator.SEG_LINETO) {
                Point2D newP = new Point2D.Double(points[0], points[1]);
                java.awt.geom.Line2D line = new java.awt.geom.Line2D.Double(last, newP);
                lines.add(line);
                last = newP;
            } else if (type == PathIterator.SEG_CLOSE){
                java.awt.geom.Line2D line = new java.awt.geom.Line2D.Double(start, last);
                lines.add(line);
            }
        }
        return lines;
    }


    /**
     * Obstacles can be placed on the floor to obstruct vision
     */
    private final Collection<Obstacle> obstacles;

    /**
     * Gates can be placed on the floor to go to another floor
     */
    private final Collection<Gate> gates;
    private final Collection<Exit> exits;
    private final Collection<EntryPursuer> entryPursuer;
    private final Collection<EntryEvader> entryEvader;
    private final Collection<Line2D> lines;


    /**
     * Create a floor object
     *
     * @param polygon the polygon of this floor
     * @param floodID the id of this floor
     * @param obstacles a list of obstacles placed on this floor
     * @param gates a list of gates placed on this floor
     */
    public Floor(MapPolygon polygon, int floodID, Collection<Obstacle> obstacles,
                 Collection<Gate> gates, Collection<Exit> exits,Collection<EntryPursuer> entryPursuer,
                 Collection<EntryEvader> entryEvader)
        throws IllegalArgumentException
    {
        super(polygon, floodID);
        //verifyInsideFloor(obstacles);
        //verifyInsideFloor(gates);
        this.obstacles = Collections.unmodifiableCollection(obstacles);
        this.gates = Collections.unmodifiableCollection(gates);
        this.exits = Collections.unmodifiableCollection(exits);
        this.entryEvader = Collections.unmodifiableCollection(entryEvader);
        this.entryPursuer = Collections.unmodifiableCollection(entryPursuer);
        this.lines = this.constructLines();
    }

    /**
     * Verify that the given polygons are inside the floor
     *
     * @param collection the collections containing the polygons
     */
    private void verifyInsideFloor(Collection<? extends AbstractObject> collection)
        throws IllegalArgumentException
    {
        for(AbstractObject o : collection)
        {
            if(!super.getPolygon().contains(o.getPolygon().getBounds2D()))
            {
                throw new IllegalArgumentException(
                        String.format("given polygon with id %d is not inside the floor", o.getID()));
            }
            if(o instanceof Obstacle && super.getID() != ((Obstacle) o).getFloorID())
            {
                throw new IllegalArgumentException(
                        String.format("given plygon with id %d should not be placed on this floor", o.getID()));
            }
        }
    }

    /**
     * A read-only list of the obstacles on the floor

     * @return the list of the obstacles on the floor
     */
    public Collection<Obstacle> getObstacles()
    {
        return obstacles;
    }

    /**
     * A read-only list of all the gates on the floor
     *
     * @return the list of gates on the floor
     */
    public Collection<Gate> getGates()
    {
        return gates;
    }

    @Override
    public ObjectType getType()
    {
        return ObjectType.FLOOR;
    }
    public Collection<Exit> getExit()
    {
        return exits;
    }
    public Collection<EntryPursuer> getEntryPursuer()
    {
        return entryPursuer;
    }
    public Collection<EntryEvader> getEntryEvader()
    {
        return entryEvader;
    }

}
