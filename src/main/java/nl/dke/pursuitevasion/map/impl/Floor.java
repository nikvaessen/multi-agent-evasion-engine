package nl.dke.pursuitevasion.map.impl;

import nl.dke.pursuitevasion.game.Vector2D;
import nl.dke.pursuitevasion.map.AbstractObject;
import nl.dke.pursuitevasion.map.ObjectType;
import nl.dke.pursuitevasion.map.MapPolygon;
import nl.dke.pursuitevasion.map.builders.IDRegister;
import nl.dke.pursuitevasion.map.builders.MapBuilder;
import org.jgrapht.*;
import org.jgrapht.alg.NeighborIndex;
import org.jgrapht.graph.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.*;
import java.util.List;

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
        this.obstacles = Collections.unmodifiableCollection(obstacles == null ? new ArrayList<>(0) : obstacles);
        this.gates = Collections.unmodifiableCollection(gates == null? new ArrayList<>(0) : gates);
        this.exits = Collections.unmodifiableCollection(exits == null ? new ArrayList<>(0) : exits);
        this.entryEvader = Collections.unmodifiableCollection(entryEvader == null ? new ArrayList<>(0) : entryEvader);
        this.entryPursuer = Collections.unmodifiableCollection(entryPursuer == null ? new ArrayList<>(0) : entryPursuer);
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

    /**
     * Calculates the subfloors created by using a path to divide the main polygon up in
     * two subpolygons
     *
     * @param path the path dividing the floor into two subpolygons
     * @return Arraylist of size 2 with the subfloors
     * @throws IllegalArgumentException when the given path cannot divide the floor
     */
    public ArrayList<Floor> getSubFloors(GraphPath<Vector2D, ? extends DefaultEdge> path)
            throws IllegalArgumentException
    {
        // Step 1: Create the 2 walks from u to v to create sP1 and sP2
        MapPolygon mainPolygon = this.getPolygon();

        Vector2D u = path.getStartVertex();
        Vector2D v = path.getEndVertex();

        //check if both u and v are vertexes in main floor and different from each other
        if (!mainPolygon.getPoints().contains(u) || !mainPolygon.getPoints().contains(v) || u.equals(v))
        {
            throw new IllegalArgumentException("The given path cannot divide the floor in two subfloors");
        }

        NeighborIndex<Vector2D, DefaultEdge> mainPolygonGraphNeighbourList = this.getNeigbourList();
        for (Vector2D w : mainPolygon.getPoints())
        {
            List<Vector2D> neighbors = mainPolygonGraphNeighbourList.neighborListOf(w);
            if (neighbors.size() != 2)
            {
                throw new IllegalArgumentException("Cannot divide main polygon");
            }
        }

        ArrayList<Vector2D> walk1 = constructWalkFromUToV(mainPolygonGraphNeighbourList, u, v, new ArrayList<>());
        ArrayList<Vector2D> walk2 = constructWalkFromUToV(mainPolygonGraphNeighbourList, u, v, walk1);

        // Step 2: add the path to both sP1 and sP2
        List<Vector2D> reversedPath = path.getVertexList();
        Collections.reverse(reversedPath);

        walk1.addAll(reversedPath);
        walk2.addAll(reversedPath);

        //create the main floor polygons :)
        MapPolygon sp1Floor = createMapPolygon(walk1, false);
        MapPolygon sp2Floor = createMapPolygon(walk2, false);

        // Step 3: for all obstacles who have a vertex in the shortest path:
        //          see if another vertex of the obstacle lie inside sP1 or sP2
        //          and add the obstacle according to the position of that vertex
        // Step 4: for all other obstacles:
        //          see if a vertex lies inside sP1 or sP2 and add accordingly
        LinkedList<Obstacle> sp1Obstacles = new LinkedList<>();
        LinkedList<Obstacle> sp2Obstacles = new LinkedList<>();
        for (Obstacle o : this.obstacles)
        {
            if(obstacleInPolygon(o, sp1Floor))
            {
                sp1Obstacles.add(o);
            }
            else if(obstacleInPolygon(o, sp2Floor))
            {
                sp2Obstacles.add(o);
            }
            else
            {
                throw new IllegalArgumentException("Obstacle not in any polygon");
            }
        }

        Floor f1 = new Floor(sp1Floor, 1, sp1Obstacles, null, null, null, null);
        Floor f2 = new Floor(sp2Floor, 2, sp2Obstacles, null, null, null, null);

        ArrayList<Floor> floors = new ArrayList<>();
        floors.add(f1);
        floors.add(f2);

        return floors;
    }

    /**
     * Find the path of vertexes to be able to walk from u to v, given a list of all neighbours
     * for all vertexes in the graph. This excludes the initial u and v
     *
     * @param neighborIndex the list of neighbours of all vertexes
     * @param u the start vertex
     * @param v the end vertex
     * @param notAllowed list of vertexes not allowed to walk to
     * @return the walk from u to v, excluding u and v
     */
    private ArrayList<Vector2D> constructWalkFromUToV(NeighborIndex<Vector2D, DefaultEdge> neighborIndex,
                                                      Vector2D u, Vector2D v, ArrayList<Vector2D> notAllowed)
    {
        ArrayList<Vector2D> list = new ArrayList<>();
        return constructWalkFromUToV(neighborIndex, u, v, notAllowed, list);
    }

    /**
     * Find the path of vertexes to be able to walk from u to v, given a list of all neighbours
     * for all vertexes in the graph. This excludes the initial u and v
     *
     * @param neighborIndex the list of neighbours for each vertex of the graph
     * @param u the start vertex
     * @param v the end vertex
     * @param notAllowed the vertexes which are not allowed to be walked to
     * @param previousVertexes list of vertexes already walked to
     * @return the list of vertexes walked to + 1 new one vertex
     */
    private ArrayList<Vector2D> constructWalkFromUToV(NeighborIndex<Vector2D, DefaultEdge> neighborIndex,
                                                      Vector2D u, Vector2D v, ArrayList<Vector2D> notAllowed,
                                                      ArrayList<Vector2D> previousVertexes)
    {
        if(u.equals(v))
        {
            previousVertexes.remove(v);
            return previousVertexes;
        }
        else
        {
            List<Vector2D> neighbors = neighborIndex.neighborListOf(u);
            Vector2D neighbor = null;
            for(Vector2D w : neighbors)
            {
                if(!w.equals(u) && !notAllowed.contains(w))
                {
                    neighbor = w;
                }
            }
            if(neighbor == null)
            {
                throw new IllegalArgumentException("Cannot find a walk to v");
            }

            previousVertexes.add(neighbor);
            return constructWalkFromUToV(neighborIndex, neighbor, v, notAllowed, previousVertexes);
        }
    }

    /**
     * Create the MapPolygon based on a given array of vertexes of the polygon to be
     *
     * @param vertexes the list of vertexes
     * @param solid if the MapPolygon will be solid or not
     * @return the MapPolygon
     */
    private MapPolygon createMapPolygon(ArrayList<Vector2D> vertexes, boolean solid)
    {
        MapPolygon p = new MapPolygon();
        for(Vector2D v : vertexes)
        {
            p.addPoint(new Double(v.getX()).intValue(), new Double(v.getY()).intValue());
        }
        return p;
    }

    /**
     * Checks whether a given obstacle is in a given Polygon
     *
     * @param o the obstacle
     * @param polygon the polygon
     * @return
     */
    private boolean obstacleInPolygon(Obstacle o, MapPolygon polygon)
    {
        for(Vector2D v : o.getPolygon().getPoints())
        {
            boolean vInsideP = super.contains(v);

            if(!vInsideP)
            {
                return false;
            }
        }

        return true;
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

}
