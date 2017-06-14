package nl.dke.pursuitevasion.map;

import nl.dke.pursuitevasion.game.Vector2D;
import nl.dke.pursuitevasion.map.impl.Map;

import java.awt.*;
import java.awt.geom.Line2D;
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

    public MapPolygon()
    {
        super();
    }

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
            int type = it.currentSegment(current);
            if(type == PathIterator.SEG_MOVETO || type == PathIterator.SEG_LINETO){
                points.add(Vector2D.fromDoubleArray(current));
            }
            it.next();
        }
        return points;
    }

    /**
     * Get the lines between all vertexes of this polygon
     *
     * @return the lines between all vertexes of the polygon
     */
    public ArrayList<Line2D> getLines(){
        ArrayList<java.awt.geom.Line2D> lines = new ArrayList<>();
        Point2D start = null;
        Point2D last = null;
        for (PathIterator iter = this.getPathIterator(null); !iter.isDone(); iter.next()) {
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

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder("MapFloor[");
        for(int i = 0; i < npoints; i++)
        {
            builder.append(String.format("(%d,%d),", xpoints[i], ypoints[i]));
        }
        builder.deleteCharAt(builder.lastIndexOf(","));
        return builder.toString();
    }
}
