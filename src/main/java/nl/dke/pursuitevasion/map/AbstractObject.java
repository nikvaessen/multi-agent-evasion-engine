package nl.dke.pursuitevasion.map;

import nl.dke.pursuitevasion.game.Vector2D;
import org.jgrapht.alg.NeighborIndex;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Vector;

/**
 * A general object, which can be placed in the map.
 *
 * A object is made of a polygon, and has a unique ID.
 *
 * Created by nik on 2/8/17.
 */
public abstract class AbstractObject implements Serializable
{
    /**
     * The unique id of this object
     */
    private final int id;

    /**
     * The polygon of this object
     */
    private final MapPolygon polygon;

    /**
     * The collection of lines connecting the vertexes of the polygon
     */
    private Collection<Line2D> connectionLines;

    /**
     * The graph which represent the polygon. Vertexes of the vertexes of the polygon
     * and the edges are the lines between the vertexes which create the polygon
     */
    private SimpleGraph<Vector2D, DefaultEdge> graph;

    /**
     * The list of the neighbours of the vertexes of the graph representing the
     * polygon
     */
    private NeighborIndex<Vector2D, DefaultEdge> neigbourList;

    /**
     * General constructor which registers the object and gets a unique idea
     */
    public AbstractObject(MapPolygon polygon, int id)
    {
        this.id = id;
        this.polygon = polygon;
        this.connectionLines = Collections.unmodifiableCollection(computeConnectingLines());
        this.graph = constructGraph(this.polygon);
        this.neigbourList = new NeighborIndex<>(graph);
    }

    /**
     * Get which kind of object it is
     * @return the ObjectType of the object
     */
    public abstract ObjectType getType();

    /**
     * Get the unique ID of the object
     * @return the unique ID of the object
     */
    public int getID()
    {
        return id;
    }

    /**
     * The polygon which this object is made of
     * @return the polygon of this object
     */
    public MapPolygon getPolygon()
    {
        return polygon;
    }

    /**
     * Get the graph representing the polygon
     * @return the graph representing the polygon
     */
    public SimpleGraph<Vector2D, DefaultEdge> getPolygonGraph()
    {
        return graph;
    }

    /**
     * The neighbour list of the graph of the polygon
     * @return the neighbour list
     */
    public NeighborIndex<Vector2D, DefaultEdge> getNeigbourList()
    {
        return neigbourList;
    }

    @Override
    public int hashCode()
    {
        return id;
    }

    @Override
    public boolean equals(Object o)
    {
        return (o instanceof AbstractObject) && ((AbstractObject) o).getID() == this.id;
    }

    public boolean contains(Point p) {
        if (polygon.contains(p)) return true;
        for (int i = 0; i < polygon.npoints; i++) {
            if (polygon.xpoints[i] == p.x &&polygon.ypoints[i] == p.y) return true;

        }
        return false;
    }

    /**
     * Compute the lines between all vertexes of the polygon of this object
     */
    private Collection<Line2D> computeConnectingLines()
    {
       return getLines(this.polygon);
    }

    /**
     * Get the lines between all vertexes of the given polygon
     *
     * @param polygon the polygon
     * @return the lines between all vertexes of the polygon
     */
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

    private SimpleGraph<Vector2D, DefaultEdge> constructGraph(Polygon p)
    {
        SimpleGraph<Vector2D, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);

        //first add all vertexes
        for (int i = 0; i < p.npoints; i++)
        {
            g.addVertex(new Vector2D(p.xpoints[i], p.ypoints[i]));
        }

        //create edges along polygon lines. Each vertex has 2 neighbours
        Vector2D leftNeighbour, rightNeighbour;
        for (int i = 0; i < p.npoints; i++)
        {
            //select left neighbour
            if(i == 0)
            {
                leftNeighbour = new Vector2D(p.xpoints[p.npoints -1], p.ypoints[p.npoints - 1]);
            }
            else
            {
                leftNeighbour = new Vector2D(p.xpoints[i-1], p.ypoints[i-1]);
            }

            //select right neighbour
            if(i == p.npoints - 1)
            {
                rightNeighbour = new Vector2D(p.xpoints[0], p.ypoints[0]);
            }
            else
            {
                rightNeighbour = new Vector2D(p.xpoints[i + 1], p.ypoints[i + 1]);
            }

            //add the edges
            Vector2D v = new Vector2D(p.xpoints[i], p.ypoints[i]);
            g.addEdge(v, leftNeighbour);
            g.addEdge(v, rightNeighbour);
        }

        return g;
    }

    /**
     * Get the lines between all vertexes of the object
     * @return A collection of lines between each vertex of the object
     */
    public Collection<Line2D> getConnectionLines() {
        return connectionLines;
    }
}
