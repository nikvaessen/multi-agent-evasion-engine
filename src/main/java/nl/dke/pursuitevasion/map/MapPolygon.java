package nl.dke.pursuitevasion.map;

import nl.dke.pursuitevasion.game.Vector2D;
import nl.dke.pursuitevasion.map.impl.Map;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;

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

    public void addPoint(Vector2D point){
        addPoint((int)Math.round(point.getX()), (int)Math.round(point.getY()));
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


    /**
     * Check if the given vector location is inside or on the boundaries of this object
     *
     * @param p the location
     * @return true is it's inside or on the boundary of this object, false otherwise
     */
    @Override
    public boolean contains(Point2D p)
    {
        boolean vInsideP = super.contains(p);

        if(!vInsideP)
        {
            for (Line2D line : this.getLines())
            {
                if (line.ptSegDist(p) < 0.001)
                {
                    vInsideP = true;
                }
            }
        }

        return vInsideP;
    }

    /**
     * Check if the given vector location is inside or on the boundaries of this object
     *
     * @param v the location
     * @return true is it's inside or on the boundary of this object, false otherwise
     */
    public boolean contains(Vector2D v)
    {
        return this.contains(v.toPoint());
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
        builder.append("]");
        return builder.toString();
    }

    public double getArea()
    {

        List<Vector2D> polygon = new ArrayList<Vector2D>(getPoints());
        int N = polygon.size();
        int i,j;
        double area = 0;
        for (i=0;i < N;i++)
        {
            j = (i + 1) % N;
            area += polygon.get(i).getX() * polygon.get(j).getY();
            area -= polygon.get(i).getY() * polygon.get(j).getX();
        }
        area /= 2;
        return(area < 0 ? -area : area);
    }
}
