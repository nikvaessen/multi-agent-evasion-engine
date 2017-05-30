package nl.dke.pursuitevasion.map;

import nl.dke.pursuitevasion.game.Vector2D;

import java.awt.*;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by nik on 26/02/17.
 */
public class MapPolygon extends java.awt.Polygon
{
    private boolean solid;

    public MapPolygon(int[] xPoints, int[] yPoints, int n, boolean solid)
    {
        super(xPoints, yPoints, n);
        this.solid = solid;
    }

    public MapPolygon(Polygon polygon, boolean solid)
    {
        this(polygon.xpoints, polygon.ypoints, polygon.npoints, solid);
    }

    public boolean isSolid()
    {
        return solid;
    }

    public Collection<Vector2D> getPoints(){
        PathIterator it = this.getPathIterator(null);
        Collection<Vector2D> points = new ArrayList<Vector2D>(this.npoints);
        // current contains the points for the next segment.
        // has to be size 6 because of how the currentSegment function is implemented.
        double[] current = new double[6];

        while(!it.isDone()){
            it.currentSegment(current);
            points.add(Vector2D.fromDoubleArray(current));
            it.next();
        }
        return points;

    }
}
