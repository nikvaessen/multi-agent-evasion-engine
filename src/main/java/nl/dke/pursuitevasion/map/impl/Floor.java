package nl.dke.pursuitevasion.map.impl;

import nl.dke.pursuitevasion.game.Vector2D;
import nl.dke.pursuitevasion.map.AbstractObject;
import nl.dke.pursuitevasion.map.ObjectType;
import nl.dke.pursuitevasion.map.MapPolygon;
import org.jgrapht.*;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleGraph;
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

        // Get all subgraphs
        ArrayList<SimpleGraph<Vector2D, DefaultEdge>> graphs = new ArrayList<>();
        graphs.add(getPolygonGraph());
        getObstacles().forEach(obstacle -> graphs.add(obstacle.getPolygonGraph()));

        //Each vertex of the graphs of the AbstractObject is a vertex in the visibility-graph
        for(SimpleGraph<Vector2D, DefaultEdge> subgraph : graphs)
        {
            for (Vector2D v : subgraph.vertexSet())
            {
                g.addVertex(v);
            }
        }

        //add the rest of the edges
        Set<Vector2D> vertexes = g.vertexSet();
        for(Vector2D v : vertexes)
        {
            for(Vector2D u : vertexes)
            {
                //check conditions
                boolean vertexesEqual = v.equals(u);
                boolean edgeExists = g.getEdge(u, v) != null;
                boolean vertexesLineOfSight = isLineOfSight(v, u, obstacles);

                //log
                if(logger.isTraceEnabled())
                {
                    logger.trace("v:{}, u:{}, v==u:{}, exists: {}, sight: {}",
                                 v, u,
                                 vertexesEqual,
                                 edgeExists,
                                 vertexesLineOfSight);
                }

                //add edge if vertexes not equal, edge doesn't exist, and there is line of sight between the vertexes
                if(!vertexesEqual && !edgeExists && vertexesLineOfSight)
                {
                    DefaultWeightedEdge e = new DefaultWeightedEdge();
                    g.addEdge(v, u, e);
                    g.setEdgeWeight(e, v.distance(u));
                }
            }
        }

        return g;
    }

    /**
     * Calculate if there is line-of-sight visibility between two locations according to the floor
     *
     * @param v location 1
     * @param u location 2
     * @param obstacles the obstacles of this map
     * @return true when there is line-of-sight visibility between the two given points,
     * false otherwise
     */
    private boolean isLineOfSight(Vector2D v, Vector2D u, Collection<Obstacle> obstacles)
    {
        // Step 1 - Check if given u and v are neighbours on the same polygon.
        //          If they are, there is visibility
        if(neighbourInObject(this, u, v))
        {
            return true;
        }
        for(Obstacle o : obstacles)
        {
            if(neighbourInObject(o, u, v))
            {
                return true;
            }
        }

        // Step 2 - Determine which case applies and solve

        // the line between given points v and u
        Line2D lineBetweenPoints = new Line2D.Double(
                new Point2D.Double(v.getX(), v.getY()),
                new Point2D.Double(u.getX(), u.getY()));

        // Knowing that they are not neighbours:
        // case 1: They are both vertexes on a non-solid polygon (both vertexes of floor)
        //         Check for intersection with solid-polygons inside floor and non-solid
        //         polygon excluding lines with one endpoint being either vertex
        if(bothInObject(this, u, v))
        {
            for(Line2D line: lines)
            {
                if(!endpointInLine(v, line ) && !endpointInLine(u, line))
                {
                    if(line.intersectsLine(lineBetweenPoints))
                    {
                        return false;
                    }
                }
            }
            return true;
        }

        // case 2: They are both end-points of the same solid-polygons.
        //         no visibility if they are not neighbours, but they are not as
        //         that is checked in step 1
        for(Obstacle o : obstacles)
        {
            if(bothInObject(o, u, v))
            {
                return false;
            }
        }

        // case 3: One is vertex on non-solid polygon, one is vertex on solid-polygon
        //         Also check for intersection with solid-polygons, but exclude lines
        //         where one endpoint is the vertex of the solid polygon
        // case 4: both vertexes are part of different solid-polygons
        //         same as case 3
        for(Line2D line: lines)
        {
            if(!endpointInLine(v, line ) && !endpointInLine(u, line))
            {
                if(line.intersectsLine(lineBetweenPoints))
                {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Check if 2 vertexes are neighbours in the same polygon of an AbstractObject
     * @param o the AbstractObject which defines the polygon
     * @param u vertex 1
     * @param v vertex 2
     * @return true if both given vertexes are neighbour in given polygon p, false otherwise
     */
    private boolean neighbourInObject(AbstractObject o, Vector2D u, Vector2D v)
    {
        return o.getPolygonGraph().containsVertex(v) && o.getNeigbourList().neighborListOf(v).contains(u);
    }

    /**
     * Calcuate if 2 vertexes are part of the same polygon of an AbstractObject
     * @param o the AbstractObject which defines the polygon
     * @param u vertex 1
     * @param v vertex 2
     * @return true is both given vertexes are a vertex in the given polygon, false otherwise
     */
    private boolean bothInObject(AbstractObject o, Vector2D u, Vector2D v)
    {
        return o.getPolygonGraph().vertexSet().containsAll(Arrays.asList(u, v));
    }

    /**
     * Check whether the given point is an endpoint of the given line
     *
     * @param v the point
     * @param line the line
     * @return whether the given point is an enpoint on the given line
     */
    public boolean endpointInLine(Vector2D v, Line2D line)
    {
        Vector2D e1 = new Vector2D(line.getX1(), line.getY1());
        Vector2D e2 = new Vector2D(line.getX2(), line.getY2());

        return v.equals(e1) || v.equals(e2);
    }

    private boolean lineIntersectsPolygon(Polygon p, Line2D line)
    {
        // step 1: check if u and v

        for (int i = 0; i < p.npoints; i++)
        {
            new Vector2D(p.xpoints[i], p.ypoints[i]);
        }
        return false;
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


}
