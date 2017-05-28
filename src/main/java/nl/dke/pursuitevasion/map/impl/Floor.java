package nl.dke.pursuitevasion.map.impl;

import nl.dke.pursuitevasion.game.Vector2D;
import nl.dke.pursuitevasion.map.AbstractObject;
import nl.dke.pursuitevasion.map.ObjectType;
import nl.dke.pursuitevasion.map.MapPolygon;
import org.jgrapht.*;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.*;

/**
 * A floor is a 2d level in which agents can walk
 *
 * Created by nik on 2/8/17.
 */
public class Floor extends AbstractObject
{

    /**
     * The logger of this class
     */
    private final static Logger logger = LoggerFactory.getLogger(Floor.class);

    /**
     * Obstacles can be placed on the floor to obstruct vision
     */
    private final Collection<Obstacle> obstacles;

    /**
     * Gates can be placed on the floor to go to another floor
     */
    private final Collection<Gate> gates;

    /**
     * Exits can be used to leave the floor
     */
    private final Collection<Exit> exits;

    /**
     * Pursuers can spawn on these locations
     */
    private final Collection<EntryPursuer> entryPursuer;

    /**
     * Evaders can spawn on these locations
     */
    private final Collection<EntryEvader> entryEvader;

    /**
     * All the lines between all the vertexes of the main floor and the lines between
     * the obstacles
     */
    private final Collection<Line2D> lines;

    /**
     * The visibility graph of this floor. Vertexes are endpoints of the polygon of the floor
     * and of the obstacles. Edges represents line-of-sight visibility between the two endpoints.
     * The weight is the distance between the two vertexes in the floor
     */
    private WeightedGraph<Vector2D, DefaultWeightedEdge> visibilityGraph;

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
        this.visibilityGraph = computeVisibilityGraph();
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

    /**
     * Get the type of this AbstractObstacle (which is floor)
     *
     *  @return that this Class is a floor
     */
    @Override
    public ObjectType getType()
    {
        return ObjectType.FLOOR;
    }

    /**
     * The exit of this floor
     *
     * @return the exit of this floor
     */
    public Collection<Exit> getExit()
    {
        return exits;
    }

    /**
     * Get the location on the floor where pursuers can spawn
     *
     * @return the spawn locations of the pursuer(s)
     */
    public Collection<EntryPursuer> getEntryPursuer()
    {
        return entryPursuer;
    }

    /**
     * Get the location on the floor where evaders can spawn
     *
     * @return the spawn location of the evader(s)
     */
    public Collection<EntryEvader> getEntryEvader()
    {
        return entryEvader;
    }

    /**
     * Get the visibility graph of the floor

     * @return A weighted graph of the visibility graph of the floor
     */
    public WeightedGraph<Vector2D, DefaultWeightedEdge> getVisibilityGraph()
    {
        return this.visibilityGraph;
    }

    /**
     * Compute the visibility graph of this floor

     * @return the visibility graph
     */
    private WeightedGraph<Vector2D, DefaultWeightedEdge> computeVisibilityGraph()
    {
        WeightedGraph<Vector2D, DefaultWeightedEdge> g
                = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);

        // Get all polygons
        ArrayList<MapPolygon> polygons = new ArrayList<>();
        polygons.add(getPolygon());
        getObstacles().forEach(obstacle -> polygons.add(obstacle.getPolygon()));

        //Each endpoint of the polygon is a vertex in the graph
        for (Polygon p : polygons) {
            for (int i = 0; i < p.npoints; i++) {
                g.addVertex(new Vector2D(p.xpoints[i], p.ypoints[i]));
            }
        }

        //add all edges
        Set<Vector2D> vertexes = g.vertexSet();
        for(Vector2D v : vertexes)
        {
            for(Vector2D u : vertexes)
            {
                if(logger.isTraceEnabled())
                {
                    logger.trace("v:{}, u:{}, v==u:{}, not exists: {}, sight: {}",
                                 v, u,
                                 !v.equals(u),
                                 g.getEdge(u, v) == null,
                                 isLineOfSight(v, u));
                }
                if(!v.equals(u) && g.getEdge(u, v) == null && isLineOfSight(v, u))
                {
                    DefaultWeightedEdge e = new DefaultWeightedEdge();
                    g.addEdge(u, v, e);
                    g.setEdgeWeight(e, v.distance(u));
                }
            }
        }

        return g;
    }

    /**
     * Get the lines between all vertexes of the polygons (main polygon and obstacle polygons)
     *
     * @return the lines
     */
    public Collection<Line2D> getLines(){return lines;}

    /**
     * Calculate the lines between all vertexes of the polygons. This does not include lines between
     * different polygons
     *
     * @return the list of all the lines
     */
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

    /**
     * Calculate if there is line-of-sight visibility between two locations according to the floor
     *
     * @param v location 1
     * @param u location 2
     * @return true when there is line-of-sight visibility between the two given points,
     * false otherwise
     */
    private boolean isLineOfSight(Vector2D v, Vector2D u)
    {
        Line2D lineBetweenPoints = new Line2D.Double(
                new Point2D.Double(v.getX(), v.getY()),
                new Point2D.Double(u.getX(), u.getY()));

        ArrayList<Polygon> obstaclePolygons = new ArrayList<>();
        for(AbstractObject object : obstacles)
        {
            obstaclePolygons.add(object.getPolygon());
        }

        for(Polygon p : obstaclePolygons)
        {
            //p.intersects()
        }
        if(logger.isTraceEnabled())
        {
            logger.trace("u = {}, v = {}, line_uv = [{} {}, {} {}]",
                         u,v,
                         lineBetweenPoints.getX1(),
                         lineBetweenPoints.getY1(),
                         lineBetweenPoints.getX2(),
                         lineBetweenPoints.getX2());
        }
        for (Line2D line: lines)
        {
            if(logger.isTraceEnabled())
            {
                logger.trace("intersects with [{} {},{} {}] : {}",
                             line.getX1(),
                             line.getY1(),
                             line.getX2(),
                             line.getX2(),
                             lineBetweenPoints.intersectsLine(line));
            }
            if(line.intersectsLine(lineBetweenPoints))
            {
                return false;
            }
        }

        return true;
    }

    private boolean lineIntersectsPolygon(Polygon p, Line2D line)
    {
        for (int i = 0; i < p.npoints; i++)
        {
            new Vector2D(p.xpoints[i], p.ypoints[i]);
        }
        return false;
    }

}
