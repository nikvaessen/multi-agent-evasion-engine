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
import java.awt.geom.Rectangle2D;
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

    public ArrayList<Point> newSimplePolygon1 = new ArrayList<>();
    public ArrayList<Point> newSimplePolygon2 = new ArrayList<>();
    public ArrayList<Polygon> trianglesToDraw = new ArrayList<>();

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
                boolean edgeInsidePolygon = insidePolygon(v, u);

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
                if(!vertexesEqual && !edgeExists && vertexesLineOfSight && edgeInsidePolygon)
                {
                    DefaultWeightedEdge e = new DefaultWeightedEdge();
                    g.addEdge(v, u, e);
                    g.setEdgeWeight(e, v.distance(u));
                }
            }
        }

        return g;
    }

    private boolean insidePolygon(Vector2D v, Vector2D u) {
        Vector2D midPoint = v.add(u).scale(0.5);
        return this.getPolygon().contains(midPoint);
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
            boolean vInsideP = polygon.contains(v);
            logger.trace("{} inside {}: {}", v, polygon, vInsideP);
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

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder("Floor[");
        builder.append(String.format("mainPolygon: %s,", this.getPolygonGraph().vertexSet()));
        for(Obstacle o: this.getObstacles())
        {
            builder.append(String.format("\n\tobstacle: %s \n", o.getPolygonGraph().vertexSet()));
        }
        builder.append("]");
        return builder.toString();
    }

    public ArrayList<ArrayList<Point>> getTriangulation(){
        ArrayList<Polygon> triangles = new ArrayList<>();

        ArrayList<Obstacle> ob = new ArrayList<>(this.obstacles);

        // compute closest distance to outer polygon
        Polygon mPoly = getPolygon();
        // create list for connections, for each obstacle a connection and with the two vertices of new connection
        //index 0=obstacle points
        //index 1=floor points
        ArrayList<ArrayList<Point>> conns = new ArrayList<ArrayList<Point>>();

//            System.out.println("1st: compute first link to outer polygon");
        //find first connection to outer polygon
        Point floorCon = null;
        Point obsCon = null;
        double smallestDistance=Integer.MAX_VALUE;
        for (Obstacle obs  : obstacles){
            Polygon oPoly = obs.getPolygon();

            for(int i=0; i<oPoly.xpoints.length; i++){
                //for all cornerpoints per obstacles,
                for (int j=0; j<getPolygon().xpoints.length; j++){
                    // compute the difference to each of the cornerpoints of the main/outer polygon
                    // maybe turn this into method?
                    if (computeDistance(oPoly.xpoints[i],mPoly.xpoints[j],oPoly.ypoints[i],mPoly.ypoints[j])<smallestDistance){
                        smallestDistance = computeDistance(oPoly.xpoints[i],mPoly.xpoints[j],oPoly.ypoints[i],mPoly.ypoints[j]);
                        obsCon = new Point(oPoly.xpoints[i], oPoly.ypoints[i]);
                        floorCon = new Point(mPoly.xpoints[j], mPoly.ypoints[j]);
//                            System.out.println("distance: " + smallestDistance);
//                            System.out.println("obsCon: " + obsCon);
//                            System.out.println("floorCon: " + floorCon);
                    }
                }
            }
        }
        conns.add(new ArrayList<>());
        conns.get(conns.size()-1).add(obsCon);
        conns.get(conns.size()-1).add(floorCon);
//            System.out.println("conns" + conns.toString());

        // compute closest link between obstacles

        ArrayList<Obstacle> obstacleCopy = new ArrayList<>(ob);
        smallestDistance=Integer.MAX_VALUE;
        Point floorCon1 = null;
        Point obsCon1 = null;

        //loop through obstaclelist
        for (int k=0; k<obstacleCopy.size(); k++) {
            //loops through points of each obstacle
            for (int l = 0; l < obstacleCopy.get(k).getPolygon().xpoints.length; l++) {

                Point oneObsConn = null;
                Point anotherObsConn = null;

                if (k != obstacleCopy.size() - 1) {
                    //System.out.println("2nd: Compute closest link");
                    //loops through points of neighbouring obstacle
                    //find closest connection between obstacles
                    for (int m = 0; m < obstacleCopy.get(k+1).getPolygon().xpoints.length; m++) {
                        if (computeDistance(
                                obstacleCopy.get(k).getPolygon().xpoints[l],
                                obstacleCopy.get(k+1).getPolygon().xpoints[m],
                                obstacleCopy.get(k).getPolygon().ypoints[l],
                                obstacleCopy.get(k+1).getPolygon().ypoints[m]
                        ) < smallestDistance) {
                            smallestDistance = computeDistance(obstacleCopy.get(k).getPolygon().xpoints[l],obstacleCopy.get(k+1).getPolygon().xpoints[m],obstacleCopy.get(k).getPolygon().ypoints[l],obstacleCopy.get(k+1).getPolygon().ypoints[m]);
                            obsCon1 = new Point(obstacleCopy.get(k).getPolygon().xpoints[l], obstacleCopy.get(k).getPolygon().ypoints[l]);
                            floorCon1 = new Point(obstacleCopy.get(k+1).getPolygon().xpoints[m], obstacleCopy.get(k+1).getPolygon().ypoints[m]);
                            //System.out.println("distance: " + smallestDistance);
                            //System.out.println("obsCon: " + obsCon);
                            //System.out.println("floorCon: " + floorCon);
                        }
                    }
                } else {
                    // connect last of the chain to other border
                    //                            System.out.println("3rd: compute last link to outer polygon");
                    smallestDistance = Integer.MAX_VALUE;
                    //loops through points of floor
                    for (int j = 0; j < getPolygon().xpoints.length; j++) {
                        if (computeDistance(
                                obstacleCopy.get(k).getPolygon().xpoints[l],
                                getPolygon().xpoints[j],
                                obstacleCopy.get(k).getPolygon().ypoints[l],
                                getPolygon().ypoints[j])
                                <= smallestDistance) {
                            smallestDistance = computeDistance(obstacleCopy.get(k).getPolygon().xpoints[l],getPolygon().xpoints[l],obstacleCopy.get(k).getPolygon().ypoints[l],getPolygon().ypoints[l]);
                            obsCon = new Point(obstacleCopy.get(k).getPolygon().xpoints[l], obstacleCopy.get(k).getPolygon().ypoints[l]);
                            floorCon = new Point(getPolygon().xpoints[j], getPolygon().ypoints[j]);
                        }
                    }
                }
            }
            if (obsCon1!=null) {
                conns.add(new ArrayList<>());
                conns.get(conns.size() - 1).add(obsCon1);
                conns.get(conns.size() - 1).add(floorCon1);
            }
            obsCon1 =null;
            floorCon1 = null;
            smallestDistance = Integer.MAX_VALUE;
        }
     /*   if (obsCon1!=null) {
            conns.add(new ArrayList<>());
            conns.get(conns.size() - 1).add(obsCon1);
            conns.get(conns.size() - 1).add(floorCon1);
        }
*/
        conns.add(new ArrayList<>());
        conns.get(conns.size()-1).add(obsCon);
        conns.get(conns.size()-1).add(floorCon);


        System.out.println("CONNECTIONS");
        for (int i=0; i<conns.size(); i++){
            System.out.println();
            for (int j=0; j<conns.get(i).size(); j++){
                System.out.println(conns.get(i).get(j).x+","+conns.get(i).get(j).y);
            }
        }

        // find point on where to "cut" the connection in int[] x and y list, meaning, where to insert the new vertices in the list
        //  write method for this, used for other obstacles as well

        // split poylgon in two parts

        // check for every cornerpoint if it is in connection list
        // if it is, then follow the connection to the obstacle and follow it in the opposite direction
        // need to check if point belongs to floor (normal search direction) or obstacle(s) (opposite direction)
        // either connection need to be dubbeled, or the list doesn't get deleted

        Point[] floorVertices = new Point[getPolygon().xpoints.length];
        for (int i=0; i<getPolygon().xpoints.length; i++){
            floorVertices[i]=new Point(getPolygon().xpoints[i], getPolygon().ypoints[i]);
        }
        ArrayList<ArrayList<Point>> obstacleVertices = new ArrayList<>();
        for (int i=0; i<ob.size(); i++){
            obstacleVertices.add(new ArrayList<>());
            for (int j=0; j<ob.get(i).getPolygon().xpoints.length; j++){
                obstacleVertices.get(obstacleVertices.size()-1).add(new Point(ob.get(i).getPolygon().xpoints[j], ob.get(i).getPolygon().ypoints[j]));
            }
        }

        //create new arraylists to store the new vertices of the new simple polygons
        ArrayList<Point> finalPolygon1 = new ArrayList<>();
        ArrayList<Point> finalPolygon2 = new ArrayList<>();

        //get order for first polygon
        System.out.println();
        System.out.println("Polygon 1");
        ArrayList<Point> polygon1 = findFirstPartOfFloor(0, floorVertices, conns, finalPolygon1);
        int index = polygon1.get(polygon1.size()-1).x;
        polygon1.remove(polygon1.size()-1);
        Point firstConnection1 = polygon1.get(polygon1.size()-1);
        polygon1.remove(polygon1.size()-1);

        polygon1 = findObstaclePartSimpler(obstacleVertices, conns, firstConnection1, polygon1);
        Point secondConnection1 = polygon1.get(polygon1.size()-1);
        //polygon1.remove(polygon1.size()-1);

        polygon1 = findLastPartOfFloor(floorVertices, firstConnection1, secondConnection1, conns, polygon1);
        newSimplePolygon1= new ArrayList<>(polygon1);

        ArrayList<Point> toBeTriangulatedPolygon = new ArrayList<>(newSimplePolygon1);

       // trianglesToDraw = getTriangles(toBeTriangulatedPolygon);

        System.out.println();
        System.out.println("polygon1");
        for (Point p: polygon1){
            System.out.println(p.x+","+p.y);
        }

        System.out.println();
        System.out.println("newSimplePolygon1");
        for (Point p: newSimplePolygon1){
            System.out.println(p.x+","+p.y);
        }

        System.out.println();
        System.out.println("toBeTriangulatedPolygon");
        for (Point p: toBeTriangulatedPolygon){
            System.out.println(p.x+","+p.y);
        }


        System.out.println();
        System.out.println("TrianglesToDraw");
        for (Polygon p: trianglesToDraw){
            System.out.println("triangle:");
            for (int i=0; i<p.xpoints.length; i++){
                System.out.println(p.xpoints[i]);
                System.out.println(p.ypoints[i]);
            }
        }
/*
        System.out.println();
        System.out.println("Polygon 2");
        //get order for second polygon
        ArrayList<Point> polygon2 = findFirstPartOfFloor(index+1, floorVertices, conns, finalPolygon2);
        //since we don't need index anymore, last element can be deleted immediately
        polygon2.remove(polygon2.size()-1);
        Point firstConnection2 = polygon2.get(polygon2.size()-1);
        //polygon2.remove(polygon2.size()-1);
        polygon2 = findObstaclePart(obstacleVertices, conns, firstConnection2, polygon2);
        Point secondConnection2 = polygon2.get(polygon2.size()-1);
        //polygon2.remove(polygon2.size()-1);
        polygon2 = findLastPartOfFloor(floorVertices, firstConnection2, secondConnection2, conns, polygon2);
        newSimplePolygon2=polygon2;
*/
        return conns;
    }

    public ArrayList<Point> findFirstPartOfFloor(int startingIndex, Point[] floorVertices, ArrayList<ArrayList<Point>> conns, ArrayList<Point> newPolygon){
        System.out.println("STARTING INDEX; "+ startingIndex);
        Point firstConnection = null;
        Point index=null;
        boolean firstConnectionFound = false;
        for (int h=startingIndex; h<floorVertices.length; h++) {
            //checks if the given floor point is equal to one of the floor points in the connections list
            System.out.println("boolean: "+firstConnectionFound);
            System.out.println("vertex: " + floorVertices[h]);
            if (!firstConnectionFound) {
                if (checkForConn(floorVertices[h], conns)) {
                    index = new Point(h,h);
                    firstConnection = getConn(floorVertices[h], conns);
                    //if it is, store both the connected points in new simple polygon list
                    newPolygon.add(floorVertices[h]);
                    newPolygon.add(firstConnection);
                    firstConnectionFound = true;
                }
            }
        }
        System.out.println("1st conn: "+firstConnection);
        System.out.println(index);
        newPolygon.add(index);
        return newPolygon;
    }
/*
    public ArrayList<Point> findObstaclePart(ArrayList<ArrayList<Point>> obstacleVertices, ArrayList<ArrayList<Point>> conns, Point firstConnection, ArrayList<Point> newPolygon){
        Point secondConnection = new Point(-1, -1);
        Point[] otherObstacleConnections = new Point[obstacleVertices.size()-1];
        for (int i=0; i<otherObstacleConnections.length; i++){
            otherObstacleConnections[i]=new Point(-1,-1);
        }
        boolean secondConnectionFound = false;
        //now continue searching for the next connected vertex inside the obstacles array in opposite direction
        //first determine to which obstacle the connected vertex belongs to
        //loop over array of obstacles
        int[] indexOfObstaclePoint = getIndexOfObstacleConn(firstConnection, obstacleVertices);
        //maybe check later if this isnt the first obstacle, if it is, then its fine,
        //if its second or higher, then the missing first parts need to be covered as well in another loop

        System.out.println();
        for (int i=0; i<obstacleVertices.size(); i++){
            System.out.println("Obstacle: "+i);
            for (int j=0; j<obstacleVertices.get(i).size(); j++) {
                System.out.println(obstacleVertices.get(i).get(j));
            }
        }
        System.out.println();

        boolean found = false;
        int[] indexOfLastObstacleConnection = new int[2];
        for (int k = 0; k < otherObstacleConnections.length; k++) {
            for (int i = indexOfObstaclePoint[0]; i >= 0; i--) {
                System.out.println("index: " + i);
                // is this constraint right?

                System.out.println("first part");
                //start searching from found point backwards until you reach the first index

                for (int j = indexOfObstaclePoint[1]; j >= 0; j--) {
                    System.out.println("otherObstacleConnections.length1: " + (otherObstacleConnections.length - 1));

                    System.out.println(obstacleVertices.get(i).get(j));
                    newPolygon.add(obstacleVertices.get(i).get(j));
                    if (j != indexOfObstaclePoint[1] && checkForConn(obstacleVertices.get(i).get(j), conns)) {
                        otherObstacleConnections[k] = getConn(obstacleVertices.get(i).get(j), conns);
                        newPolygon.add(otherObstacleConnections[k]);
                        System.out.println(otherObstacleConnections[k]);
                        System.out.println("1");
                        if (k==otherObstacleConnections.length-1){
                            indexOfLastObstacleConnection = getIndexOfObstacleConn(otherObstacleConnections[k], obstacleVertices);
                            found = true;
                        }
                    } else {
                        //if you reached the first element, continue at the end of the list until the found first connection
                        for (int m = obstacleVertices.get(i).size() - 1; m > indexOfObstaclePoint[1]; m--) {
                            if (!found) {
                                newPolygon.add(obstacleVertices.get(i).get(m));
                                System.out.println(obstacleVertices.get(i).get(m));
                                if (checkForConn(obstacleVertices.get(i).get(m), conns)) {
                                    otherObstacleConnections[k] = getConn(obstacleVertices.get(i).get(m), conns);
                                    newPolygon.add(otherObstacleConnections[k]);
                                    System.out.println(otherObstacleConnections[k]);
                                    System.out.println("2");
                                    System.out.println("k: "+k);
                                    if (k==otherObstacleConnections.length-1){
                                        indexOfLastObstacleConnection = getIndexOfObstacleConn(otherObstacleConnections[k], obstacleVertices);
                                        found = true;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            System.out.println("second part");
            for (int l = obstacleVertices.size() - 1; l > indexOfObstaclePoint[0]; l--) {
                //start searching from found point backwards until you reach the first index
                for (int j = indexOfObstaclePoint[1]; j >= 0; j--) {

                    System.out.println(obstacleVertices.get(l).get(j));
                    //commented out because this connection was added twice
                    // newPolygon.add(obstacleVertices.get(l).get(j));
                    if (j != indexOfObstaclePoint[1] && checkForConn(obstacleVertices.get(l).get(j), conns)) {
                        otherObstacleConnections[k] = getConn(obstacleVertices.get(l).get(j), conns);
                        newPolygon.add(otherObstacleConnections[k]);
                        System.out.println(otherObstacleConnections[k]);
                        System.out.println("3");
                        if (k==otherObstacleConnections.length-1){
                            indexOfLastObstacleConnection = getIndexOfObstacleConn(otherObstacleConnections[k], obstacleVertices);
                            found = true;
                        }
                    } else {
                        //if you reached the first element, continue at the end of the list until the found first connection
                        for (int n = obstacleVertices.get(l).size() - 1; n > indexOfObstaclePoint[1]; n--) {
                            if (!found) {
                                newPolygon.add(obstacleVertices.get(l).get(n));
                                System.out.println(obstacleVertices.get(l).get(n));
                                if (checkForConn(obstacleVertices.get(l).get(n), conns)) {
                                    otherObstacleConnections[k] = getConn(obstacleVertices.get(l).get(n), conns);
                                    newPolygon.add(otherObstacleConnections[k]);
                                    System.out.println(otherObstacleConnections[k]);
                                    System.out.println("4");
                                    System.out.println("k: "+k);
                                    if (k==otherObstacleConnections.length-1){
                                        indexOfLastObstacleConnection = getIndexOfObstacleConn(otherObstacleConnections[k], obstacleVertices);
                                        found = true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        System.out.println();
        System.out.println("after obstacles");
        for (int i=0; i<newPolygon.size(); i++){
            System.out.println(newPolygon.get(i));
        }

        boolean lastConnectionFound = false;
        for (int i=indexOfLastObstacleConnection[0]; i>=0; i--) {
            System.out.println("indexOfLastObstacleConnection0: " + indexOfLastObstacleConnection[0]);
            System.out.println("indexOfLastObstacleConnection1: " + indexOfLastObstacleConnection[1]);
            for (int j = indexOfLastObstacleConnection[1]; j >= 0; j--) {
                if (j != indexOfObstaclePoint[1] && checkForConn(obstacleVertices.get(i).get(j), conns)) {
                    System.out.println("HELLO");
                    if (!otherObstacleConnections[otherObstacleConnections.length - 1].equals(new Point(-1, -1)) && secondConnection.getLocation().equals(new Point(-1, -1))) {
                        System.out.println("Do I get here?");
                        secondConnection = searchForSecondConnection(secondConnection, newPolygon, obstacleVertices, indexOfObstaclePoint[1], conns, i, j);
                        System.out.println("secondConn.toString: " + secondConnection.getX());
                        System.out.println("secondConn.toString: " + secondConnection.getY());
                        lastConnectionFound = true;
                    }
                } else {
                    for (int n = obstacleVertices.get(i).size() - 1; n > indexOfObstaclePoint[1]; n--) {
                        System.out.println("HELLO");
                        if (!otherObstacleConnections[otherObstacleConnections.length - 1].equals(new Point(-1, -1)) && secondConnection.getLocation().equals(new Point(-1, -1))) {
                            System.out.println("Do I get here?");
                            secondConnection = searchForSecondConnection(secondConnection, newPolygon, obstacleVertices, indexOfObstaclePoint[1], conns, i, n);
                            System.out.println("secondConn.toString: " + secondConnection.getX());
                            System.out.println("secondConn.toString: " + secondConnection.getY());
                            lastConnectionFound = true;
                        }
                    }
                }
            }
        }
        if (!lastConnectionFound) {
            for (int l = obstacleVertices.size() - 1; l > indexOfLastObstacleConnection[0]; l--) {
                for (int j = indexOfLastObstacleConnection[1]; j >= 0; j--) {
                    if (j != indexOfObstaclePoint[1] && checkForConn(obstacleVertices.get(l).get(j), conns)) {
                        if (!lastConnectionFound) {
                            System.out.println("HOI");
                            System.out.println("secondConn.toString: " + secondConnection.getX());
                            System.out.println("secondConn.toString: " + secondConnection.getY());
                            if (!otherObstacleConnections[otherObstacleConnections.length - 1].equals(new Point(-1, -1)) && secondConnection.getLocation().equals(new Point(-1, -1))) {
                                System.out.println("is it here?");
                                secondConnection = searchForSecondConnection(secondConnection, newPolygon, obstacleVertices, indexOfObstaclePoint[1], conns, l, j);
                                lastConnectionFound = true;
                            }
                        }
                    } else {
                        for (int n = obstacleVertices.get(l).size() - 1; n > indexOfObstaclePoint[1]; n--) {
                            if (!lastConnectionFound) {
                                System.out.println("HOI");
                                System.out.println("secondConn.toString: " + secondConnection.getX());
                                System.out.println("secondConn.toString: " + secondConnection.getY());
                                if (!otherObstacleConnections[otherObstacleConnections.length - 1].equals(new Point(-1, -1)) && secondConnection.getLocation().equals(new Point(-1, -1))) {
                                    System.out.println("is it here?");
                                    secondConnection = searchForSecondConnection(secondConnection, newPolygon, obstacleVertices, indexOfObstaclePoint[1], conns, l, n);
                                    lastConnectionFound = true;
                                }
                            }
                        }
                    }
                }
            }
        }

        System.out.println("last conn: "+ secondConnection.getX());
        System.out.println("last conn: "+ secondConnection.getY());

        System.out.println();
        for (int i=0; i<newPolygon.size(); i++){
            System.out.println(newPolygon.get(i));
        }
        System.out.println();
        return  newPolygon;
    }
*/
    public ArrayList<Point> findObstaclePartSimpler(ArrayList<ArrayList<Point>> obstacleVertices, ArrayList<ArrayList<Point>> conns, Point firstConnection, ArrayList<Point> newPolygon){
        Point secondConnection = new Point(-1, -1);
        Point[] otherObstacleConnections = new Point[obstacleVertices.size()-1];
        for (int i=0; i<otherObstacleConnections.length; i++){
            otherObstacleConnections[i]=new Point(-1,-1);
        }
        boolean secondConnectionFound = false;
        //now continue searching for the next connected vertex inside the obstacles array in opposite direction
        //first determine to which obstacle the connected vertex belongs to
        //loop over array of obstacles
        int[] indexOfObstaclePoint = getIndexOfObstacleConn(firstConnection, obstacleVertices);
        //maybe check later if this isnt the first obstacle, if it is, then its fine,
        //if its second or higher, then the missing first parts need to be covered as well in another loop

        System.out.println();
        for (int i=0; i<obstacleVertices.size(); i++){
            System.out.println("Obstacle: "+i);
            for (int j=0; j<obstacleVertices.get(i).size(); j++) {
                System.out.println(obstacleVertices.get(i).get(j));
            }
        }
        System.out.println();

        boolean found = false;
        int[] indexOfLastObstacleConnection = new int[2];

        System.out.println();
        System.out.println("obstacleVertices.size()-1: "+ (obstacleVertices.size()-1) );
        for (int h=0; h<obstacleVertices.size()-1; h++){
            int k=h;
            //for (int k = 0; k < otherObstacleConnections.length; k++) {
                System.out.println("k: " + k);
                int i = indexOfObstaclePoint[0];

                int verticesInbetween = 0;

                //int j = indexOfObstaclePoint[1];
                for (int j = indexOfObstaclePoint[1]; j >= -1; j--) {
                   // System.out.println("j: " + j);
                    //System.out.println(obstacleVertices.get(i).get(j));
                    //newPolygon.add(obstacleVertices.get(i).get(j));
                    //verticesInbetween++;

                    if (j != -1 && j != indexOfObstaclePoint[1] && checkForConn(obstacleVertices.get(i).get(j), conns)) {
                        if (!found) {
                            otherObstacleConnections[k] = getConn(obstacleVertices.get(i).get(j), conns);
                            newPolygon.add(otherObstacleConnections[k]);
                            System.out.println(otherObstacleConnections[k]);
                            indexOfObstaclePoint = getIndexOfObstacleConn(otherObstacleConnections[k], obstacleVertices);
                            // delete dubble vertex
                            //newPolygon.remove(newPolygon.size()-1);
                            if (k == otherObstacleConnections.length - 1) {
                                //indexOfLastObstacleConnection = getIndexOfObstacleConn(otherObstacleConnections[k], obstacleVertices);
                                indexOfObstaclePoint
                                        = getIndexOfObstacleConn(otherObstacleConnections[k], obstacleVertices);

                                found = true;
                            }
                        }
                    } else if (j!=-1){
                        newPolygon.add(obstacleVertices.get(i).get(j));
                        verticesInbetween++;
                        if (j!=indexOfObstaclePoint[1])
                        {
                            if (checkForConn(obstacleVertices.get(i).get(j), conns)){
                                if (!found) {
                                    otherObstacleConnections[k] = getConn(obstacleVertices.get(i).get(j), conns);
                                    newPolygon.add(otherObstacleConnections[k]);
                                    System.out.println(otherObstacleConnections[k]);
                                    indexOfObstaclePoint = getIndexOfObstacleConn(otherObstacleConnections[k], obstacleVertices);
                                    // delete dubble vertex
                                    //newPolygon.remove(newPolygon.size()-1);
                                    if (k == otherObstacleConnections.length - 1) {
                                        //indexOfLastObstacleConnection = getIndexOfObstacleConn(otherObstacleConnections[k], obstacleVertices);
                                        indexOfObstaclePoint
                                                = getIndexOfObstacleConn(otherObstacleConnections[k], obstacleVertices);

                                        found = true;
                                    }
                                }
                            }
                        }
                    } else {
                        //if you reached the first element, continue at the end of the list until the found first connection
                        for (int m = obstacleVertices.get(i).size() - 1; m > indexOfObstaclePoint[1]; m--) {
                            System.out.println("m: " + m);
                            if (!found) {
                                verticesInbetween++;
                                newPolygon.add(obstacleVertices.get(i).get(m));
                                System.out.println(obstacleVertices.get(i).get(m));
                                if (checkForConn(obstacleVertices.get(i).get(m), conns)) {
                                    otherObstacleConnections[k] = getConn(obstacleVertices.get(i).get(m), conns);
                                    newPolygon.add(otherObstacleConnections[k]);
                                    System.out.println(otherObstacleConnections[k]);
                                    indexOfObstaclePoint = getIndexOfObstacleConn(otherObstacleConnections[k], obstacleVertices);
                                    // delete dubble vertex
                                    //newPolygon.remove(newPolygon.size()-1);
                                    if (k == otherObstacleConnections.length - 1) {
                                        //indexOfLastObstacleConnection = getIndexOfObstacleConn(otherObstacleConnections[k], obstacleVertices);
                                        indexOfObstaclePoint = getIndexOfObstacleConn(otherObstacleConnections[k], obstacleVertices);

                                        found = true;
                                    }
                                }
                            }
                        }
                    }
                    System.out.println();
                    System.out.println("for each obstacle?:");
                    for (int ö=0; ö<newPolygon.size(); ö++){
                        System.out.println(newPolygon.get(ö));
                    }
                    System.out.println();
                }
                // delete dubble vertex
           // newPolygon.remove(newPolygon.size()-1);
            System.out.println("verticesInbetween: " +verticesInbetween);
            System.out.println("before doubla point if:");
            for (int ö=0; ö<newPolygon.size(); ö++){
                System.out.println(newPolygon.get(ö));
            }

            System.out.println("after if: "+otherObstacleConnections[k].x+","+otherObstacleConnections[k].y);
            System.out.println(otherObstacleConnections[k].equals(new Point(-1,-1)));
            if (otherObstacleConnections[k].equals(new Point(-1,-1))){

                System.out.println("v "+verticesInbetween);
                for (int v = 0; v<verticesInbetween-1; v++){
                    newPolygon.remove(newPolygon.size()-1);
                }

                System.out.println("get Conn: "+getConn(obstacleVertices.get(i).get(indexOfObstaclePoint[1]), conns));
                otherObstacleConnections[k] = getConn(obstacleVertices.get(i).get(indexOfObstaclePoint[1]), conns);
                newPolygon.add(otherObstacleConnections[k]);

                //or indexOfObstaclePoint?
                //indexOfLastObstacleConnection = getIndexOfObstacleConn(otherObstacleConnections[k], obstacleVertices);
                indexOfObstaclePoint = getIndexOfObstacleConn(otherObstacleConnections[k], obstacleVertices);

            }
            //}
        }

        System.out.println(indexOfObstaclePoint[0]+","+indexOfObstaclePoint[1]);
        System.out.println(indexOfLastObstacleConnection[0]+","+indexOfLastObstacleConnection[1]);

        System.out.println(otherObstacleConnections.length-1);

        System.out.println("getConn of: "+ otherObstacleConnections[otherObstacleConnections.length-1]);
        //secondConnection=getConn(otherObstacleConnections[otherObstacleConnections.length-1], conns);

        //indexOfObstaclePoint[1]
        System.out.println("HALLIHALLO");
        secondConnection = searchForSecondConnectionForSimpleMethod(obstacleVertices, newPolygon, conns, indexOfObstaclePoint);

        newPolygon.add(secondConnection);

        System.out.println("last conn: "+ secondConnection.getX());
        System.out.println("last conn: "+ secondConnection.getY());

        System.out.println();
        for (int i=0; i<newPolygon.size(); i++){
            System.out.println(newPolygon.get(i));
        }
        System.out.println();
        return  newPolygon;
    }

    public Point searchForSecondConnectionForSimpleMethod(ArrayList<ArrayList<Point>> obstacleVertices,
                                                          ArrayList<Point> newPolygon, ArrayList<ArrayList<Point>> conns,
                                                          int[] indexOfObstaclePoint){
        System.out.println("SEARCH FOR 2nd CONN");
        Point secondConn = new Point(-1,-1);

        int lastObstacle = indexOfObstaclePoint[0];
        int lastObstacleVertex = indexOfObstaclePoint[1];

        boolean found=false;

        int verticesInbetween = 0;

        for (int j = lastObstacleVertex; j >= -1; j--) {
            //System.out.println("j: " + j);
            //System.out.println(obstacleVertices.get(lastObstacle).get(j));
            //newPolygon.add(obstacleVertices.get(lastObstacle).get(j));
            if (j != -1 && j!= lastObstacleVertex && checkForConn(obstacleVertices.get(lastObstacle).get(j), conns)) {
                //newPolygon.add(obstacleVertices.get(lastObstacle).get(j));
                if (!found) {
                    secondConn = getConn(obstacleVertices.get(lastObstacle).get(j), conns);
                    newPolygon.add(secondConn);
                    System.out.println("secondConn0: " + secondConn);
                    found = true;
                }
            } else if (j != -1) {
                newPolygon.add(obstacleVertices.get(lastObstacle).get(j));
                verticesInbetween++;
                if (j!= lastObstacleVertex){
                    //newPolygon.add(obstacleVertices.get(lastObstacle).get(j));
                    if (checkForConn(obstacleVertices.get(lastObstacle).get(j), conns)) {
                        if (!found) {
                            secondConn = getConn(obstacleVertices.get(lastObstacle).get(j), conns);
                            newPolygon.add(secondConn);
                            System.out.println("secondConn1: " + secondConn);
                            found = true;
                        }
                    }
                }
            } else {
                //if you reached the first element, continue at the end of the list until the found first connection
                for (int m = obstacleVertices.get(lastObstacle).size() - 1; m > lastObstacleVertex; m--) {
                    //System.out.println("m: " + m);
                    //newPolygon.add(obstacleVertices.get(lastObstacle).get(m));
                    //verticesInbetween++;
                    if (!found) {
                        System.out.println(obstacleVertices.get(lastObstacle).get(m));
                        newPolygon.add(obstacleVertices.get(lastObstacle).get(m));
                        verticesInbetween++;
                        if (checkForConn(obstacleVertices.get(lastObstacle).get(m), conns)) {
                            secondConn = getConn(obstacleVertices.get(lastObstacle).get(m), conns);
                            System.out.println("secondConn2: " + secondConn);
                            newPolygon.add(secondConn);
                            found = true;
                        }
                    }
                }
            }
            System.out.println("I want to sleep");
            for (int ö=0; ö<newPolygon.size(); ö++){
                System.out.println(newPolygon.get(ö));
            }
        }
        //is this right? compare with method above
        System.out.println("after if: ");
        System.out.println("verticesInbetween: " +verticesInbetween);
        if (!found){
            System.out.println("v "+verticesInbetween);
            for (int v = 0; v<verticesInbetween-1; v++){
                newPolygon.remove(newPolygon.size()-1);
            }
            System.out.println("get Conn: "+getConn(obstacleVertices.get(lastObstacle).get(lastObstacleVertex), conns));
            secondConn = getConn(obstacleVertices.get(lastObstacle).get(lastObstacleVertex), conns);
            found =true;

        }
        System.out.println("SECOND CONN: "+secondConn);
        return secondConn;
    }

    public Point[] searchForIntraObstacleConnection(Point[] otherObstacleConnections, ArrayList<Point> newPolygon,
                                                    ArrayList<ArrayList<Point>> obstacleVertices, int indexOfObstaclePoint,
                                                    ArrayList<ArrayList<Point>> conns, int i, int j, int m){

        boolean found=false;
        System.out.println(obstacleVertices.get(i).get(j));
        newPolygon.add(obstacleVertices.get(i).get(j));
        if (j != indexOfObstaclePoint && checkForConn(obstacleVertices.get(i).get(j), conns)) {
            otherObstacleConnections[m] = getConn(obstacleVertices.get(i).get(j), conns);
            newPolygon.add(otherObstacleConnections[m]);
            found = true;
        } else {
            //if you reached the first element, continue at the end of the list until the found first connection
            for (int k = obstacleVertices.get(i).size() - 1; k > indexOfObstaclePoint; k--) {
                if (!found) {
                    newPolygon.add(obstacleVertices.get(i).get(k));
                    if (checkForConn(obstacleVertices.get(i).get(k), conns)) {
                        otherObstacleConnections[m] = getConn(obstacleVertices.get(i).get(k), conns);
                        newPolygon.add(otherObstacleConnections[m]);
                        found = true;
                    }
                }
            }
        }

        return otherObstacleConnections;
    }

    public Point searchForSecondConnection(Point secondConnection, ArrayList<Point> newPolygon,
                                           ArrayList<ArrayList<Point>> obstacleVertices, int indexOfLastObstaclePoint,
                                           ArrayList<ArrayList<Point>> conns, int i, int j){
        System.out.println("SEARCH FOR 2nd CONN");
        boolean found=false;
        System.out.println(obstacleVertices.get(i).get(j));
        newPolygon.add(obstacleVertices.get(i).get(j));
        if (j != indexOfLastObstaclePoint && checkForConn(obstacleVertices.get(i).get(j), conns)) {
            System.out.println("1?");
            secondConnection = getConn(obstacleVertices.get(i).get(j), conns);
            newPolygon.add(secondConnection);
            found = true;
        } else {
            System.out.println("2?");
            System.out.println(obstacleVertices.get(i).get(j));
            //if you reached the first element, continue at the end of the list until the found first connection
            for (int k = obstacleVertices.get(i).size() - 1; k > indexOfLastObstaclePoint; k--) {
                if (!found) {
                    System.out.println("3?");
                    System.out.println(obstacleVertices.get(i).get(k));
                    newPolygon.add(obstacleVertices.get(i).get(k));
                    if (checkForConn(obstacleVertices.get(i).get(k), conns)) {
                        secondConnection = getConn(obstacleVertices.get(i).get(k), conns);
                        newPolygon.add(secondConnection);
                        found = true;
                    }
                }
            }
        }
        System.out.println();
        return secondConnection;
    }

    public ArrayList<Point> findLastPartOfFloor(Point[] floorVertices, Point firstConnection, Point secondConnection, ArrayList<ArrayList<Point>> conns, ArrayList<Point> newPolygon){

        System.out.println("WTF");
        System.out.println();
        System.out.println("after everything");
        for (int i=0; i<newPolygon.size(); i++){
            System.out.println(newPolygon.get(i));
        }
        System.out.println("Last part of polygon still missing");
        int indexOfConnectionFromObstacle=-1;
        for (int i=0; i<floorVertices.length; i++){
            System.out.println(secondConnection.x+","+secondConnection.y);
            System.out.println(floorVertices[i].x+","+floorVertices[i].y);
            System.out.println(secondConnection.equals(floorVertices[i]));
            if (secondConnection.equals(floorVertices[i])){
                indexOfConnectionFromObstacle = i;
            }
        }
        for (int i=indexOfConnectionFromObstacle; i<floorVertices.length; i++){
            newPolygon.add(floorVertices[i]);
            if ((floorVertices[i].equals(getConn(firstConnection, conns))) && !(i>=floorVertices.length-1)){
                newPolygon.remove(newPolygon.size()-1);
                return  newPolygon;
            }
            if (!(floorVertices[i].equals(getConn(firstConnection, conns))) && i>=floorVertices.length-1){
                for (int j=0; j<indexOfConnectionFromObstacle; j++){
                    newPolygon.add(floorVertices[j]);
                    if ((floorVertices[j].equals(getConn(firstConnection, conns)))){
                        newPolygon.remove(newPolygon.size()-1);
                        return newPolygon;
                    }
                }
            }
        }
        System.out.println();
        System.out.println("after everything");
        for (int i=0; i<newPolygon.size(); i++){
            System.out.println(newPolygon.get(i));
        }
        return newPolygon;
    }

    //finds the 2 indices of specific element in obstacle list
    public int[] getIndexOfObstacleConn(Point obstaclePointWithConnection, ArrayList<ArrayList<Point>> obstacleList){
        int[] indexOfObstaclePoint = new int[2];
        for (int i=0; i<obstacleList.size(); i++){
            for (int j=0; j<obstacleList.get(i).size(); j++){
                if (obstaclePointWithConnection.equals(obstacleList.get(i).get(j))){
                    indexOfObstaclePoint[0]=i;
                    indexOfObstaclePoint[1]=j;
                }
            }
        }
        return indexOfObstaclePoint;
    }

    public boolean checkForConn(Point possibleConn, ArrayList<ArrayList<Point>> allConns){
        for (int i=0; i<allConns.size(); i++){
            for (int j=0; j<allConns.get(i).size(); j++){
                if (possibleConn.equals(allConns.get(i).get(j))){
                    return true;
                }
            }
        }
        return false;
    }

    public Point getConn(Point possibleConn, ArrayList<ArrayList<Point>> allConns){
        Point foundConn = null;
        for (int i=0; i<allConns.size(); i++){
            if (possibleConn.equals(allConns.get(i).get(0))){
                foundConn = allConns.get(i).get(1);
            }
            if (possibleConn.equals(allConns.get(i).get(1))){
                foundConn = allConns.get(i).get(0);
            }
        }
        return foundConn;
    }

    public double computeDistance(int x1,int x2,int y1,int y2){
        return Math.abs(Math.sqrt(Math.pow((x1-x2), 2)+ Math.pow((y1-y2), 2)));
    }

    private Polygon createNewTriangle(Point viminus, Point vi, Point viplus){
        int[] xpoints = new int[3];
        xpoints[0] = viminus.x;
        xpoints[1] = vi.x;
        xpoints[2] = viplus.x;
        int[] ypoints = new int[3];
        ypoints[0] = viminus.y;
        ypoints[1] = vi.y;
        ypoints[2] = viplus.y;
        return new Polygon(xpoints, ypoints, 3);
    }

    public Point getIntersectionPoints(Line2D line1, Line2D line2){

        if (line1.intersectsLine(line2)){

            double x1 = line1.getX1();
            double x2 = line1.getX2();
            double y1 = line1.getY1();
            double y2 = line1.getY2();

            double x3 = line2.getX1();
            double x4 = line2.getX2();
            double y3 = line2.getY1();
            double y4 = line2.getY2();

            double a1 = (y2-y1)/(x2-x1);
            double b1 = y1 - a1*x1;
            double a2 = (y4-y3)/(x4-x3);
            double b2 = y3 - a2*x3;

            System.out.println("a1: "+ Math.abs(a1));
            System.out.println("a2: "+ Math.abs(a2));
            if (Math.abs(a1)==0 && Math.abs(a2)==0){
                return null;
            }

            double d = (x1-x2)*(y3-y4) - (y1-y2)*(x3-x4);
            if (d == 0) return null;

            int xi = (int) (((x3-x4)*(x1*y2-y1*x2)-(x1-x2)*(x3*y4-y3*x4))/d);
            int yi = (int) (((y3-y4)*(x1*y2-y1*x2)-(y1-y2)*(x3*y4-y3*x4))/d);


            System.out.println("Intersection point: "+ new Point(xi, yi));

            return new Point(xi, yi);
        }
        return null;
    }

    public boolean isMiddlePointInside(Line2D foundDiagonal, ArrayList<Point> newPolygon){
        int middlePointX = (int) ((foundDiagonal.getX1()+foundDiagonal.getX2())/2);
        int middlePointY = (int) ((foundDiagonal.getY1()+foundDiagonal.getY2())/2);
        Point middlePoint = new Point(middlePointX, middlePointY);
        System.out.println("Middle point: " + middlePoint);

        int[] xpoints = new int[newPolygon.size()];
        int[] ypoints = new int[newPolygon.size()];
        for (int i=0; i<newPolygon.size(); i++){
            xpoints[i] = ((int) newPolygon.get(i).getX());
            ypoints[i] = ((int) newPolygon.get(i).getY());
        }
        Polygon p = new Polygon(xpoints, ypoints, newPolygon.size());

        if(p.contains(middlePoint)){
            return true;
        }
        return false;


    }

    public ArrayList<Line2D> getPolygonLines(ArrayList<Point> polygon){
        ArrayList<Line2D> linesOfPolygon = new ArrayList<>();
        for(int j=0; j<polygon.size()-1;j++){
            linesOfPolygon.add(new Line2D.Double(polygon.get(j), polygon.get(j+1)));
        }
        linesOfPolygon.add(new Line2D.Double(polygon.get(polygon.size()-1), polygon.get(0)));
        return linesOfPolygon;
    }

    public boolean liesCompletelyInside(Point viminus, Point vi, Point viplus, ArrayList<Point> polygon,
                                        ArrayList<Line2D> linesOfPolygon, ArrayList<Polygon> triangles){
    Line2D diagonal = new Line2D.Double(viminus, viplus);
        System.out.println("Diagonal: " + diagonal.getP1() + "," + diagonal.getP2());
        System.out.println("isMiddlePointInside: " + isMiddlePointInside(diagonal, polygon));
        if (isMiddlePointInside(diagonal, polygon)){
            //loop over the lines
            ArrayList<Point> intersectionPoints = new ArrayList<>();
            for(int j=0; j<linesOfPolygon.size(); j++) {
                Point intersection = getIntersectionPoints(diagonal, linesOfPolygon.get(j));
                if (intersection!=null){
                    intersectionPoints.add(intersection);
                }
            }
            ArrayList<Boolean> allIntersectionsAreCornerpoints = new ArrayList<>();
//            boolean allIntersectionsAreCornerpoints = false;
            for (int j=0; j< intersectionPoints.size(); j++){
                for (int k=0; k<polygon.size(); k++){

                    if ( (intersectionPoints.get(j).x == polygon.get(k).x) && (intersectionPoints.get(j).y == polygon.get(k).y))
                    {
                        allIntersectionsAreCornerpoints.add(new Boolean(true));
                    }
                }
            }
            for (Boolean b: allIntersectionsAreCornerpoints){
                if (b.booleanValue() == false){
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public ArrayList<Polygon> getTriangles(ArrayList<Point> polygon){
        ArrayList<Polygon> triangles = new ArrayList<>();

//        System.out.println("POLYGON");
//        for (Point p: polygon) {
//            System.out.println(p.x + "," + p.y);
//        }

        Point viminus = new Point();
        Point vi = new Point();
        Point viplus = new Point();

        ArrayList<Line2D> linesOfPolygon = getPolygonLines(polygon);

        while (polygon.size()>3){
        System.out.println(polygon.size());
//        for(int h=polygon.size()-1; h>2; h--){
            for (int i=0; i<polygon.size()-1; i++){
                System.out.println();
                System.out.println("i: "+i);
                if (i!=polygon.size() && i!=0){
                    viminus = new Point(polygon.get(i-1));
                    vi = new Point(polygon.get(i));
                    viplus = new Point(polygon.get(i+1));

                    boolean liesInside = liesCompletelyInside(viminus, vi, viplus, polygon, linesOfPolygon, triangles);
                    if (liesInside) {
                        Polygon toBeAdded = createNewTriangle(viminus, vi, viplus);
                        System.out.println("POLYGON toBeAdded");
                        for(int l=0; l<toBeAdded.xpoints.length; l++){
                            System.out.println(toBeAdded.xpoints[l]+","+toBeAdded.ypoints[l]);
                        }
                        triangles.add(toBeAdded);
                        polygon.remove(i);
                    }



                    //if line between viminus and viplus lies COMPLETELY inside polygon, add the triangle...
                    System.out.println(polygon.size());
                    System.out.println("BOOLEAN: " + liesInside);
                    System.out.println("viminus: " + viminus.x + "," + viminus.y);
                    System.out.println("vi: " + vi.x + "," + vi.y);
                    System.out.println("viplus: " + viplus.x + "," + viplus.y);



                } else if (i==polygon.size()-1){

                    viminus = new Point(polygon.get(i-1));
                    vi = new Point(polygon.get(i));
                    viplus = new Point(polygon.get(0));
                    boolean liesInside = liesCompletelyInside(viminus, vi, viplus, polygon, linesOfPolygon, triangles);
                    if (liesInside) {
                        Polygon toBeAdded = createNewTriangle(viminus, vi, viplus);
                        triangles.add(toBeAdded);
                        polygon.remove(i);
                    }

                    //if line between viminus and viplus lies COMPLETELY inside polygon, add the triangle...
                    System.out.println(polygon.size());
                    System.out.println("BOOLEAN: " + liesInside);
                    System.out.println("viminus: " + viminus.x + "," + viminus.y);
                    System.out.println("vi: " + vi.x + "," + vi.y);
                    System.out.println("viplus: " + viplus.x + "," + viplus.y);


                } else {
                    viminus = new Point(polygon.get(polygon.size()-1));
                    vi = new Point(polygon.get(i));
                    viplus = new Point(polygon.get(i+1));

                    boolean liesInside = liesCompletelyInside(viminus, vi, viplus, polygon, linesOfPolygon, triangles);
                    if (liesInside) {
                        Polygon toBeAdded = createNewTriangle(viminus, vi, viplus);
                        triangles.add(toBeAdded);
                        polygon.remove(i);
                    }
                    //if line between viminus and viplus lies COMPLETELY inside polygon, add the triangle...
                    System.out.println(polygon.size());
                    System.out.println("BOOLEAN: " + liesInside);
                    System.out.println("viminus: " + viminus.x + "," + viminus.y);
                    System.out.println("vi: " + vi.x + "," + vi.y);
                    System.out.println("viplus: " + viplus.x + "," + viplus.y);

                }
            }
        }
        Polygon remainingPolygon = createNewTriangle(polygon.get(0), polygon.get(1), polygon.get(2));
        triangles.add(remainingPolygon);
        polygon=null;
        System.out.println("FINAL triangles");
        for (int i=0; i<triangles.size(); i++){
            System.out.println("triangle: "+ i);
            Polygon p = triangles.get(i);
            for (int j=0; j<triangles.get(i).xpoints.length; j++){
                int x = p.xpoints[j];
                int y = p.ypoints[j];
                System.out.println(x+","+y);
            }
        }


        return triangles;
    }

    public ArrayList<Polygon> getTrianglesWithRatioTest(ArrayList<Point> polygon){
        ArrayList<Polygon> triangles = new ArrayList<>();

        Point viminus = new Point();
        Point vi = new Point();
        Point viplus = new Point();

        ArrayList<Line2D> linesOfPolygon = getPolygonLines(polygon);

        while (polygon.size()>3){

            Polygon biggestEar = new Polygon();
            double biggestRatio =  Double.MIN_VALUE;
            int respectiveIndex = -1;

            System.out.println();
            System.out.println(polygon.size());
            for (int i=0; i<polygon.size()-1; i++){
                System.out.println("i: "+i);
                if (i!=polygon.size() && i!=0){
                    viminus = new Point(polygon.get(i-1));
                    vi = new Point(polygon.get(i));
                    viplus = new Point(polygon.get(i+1));

                    boolean liesInside = liesCompletelyInside(viminus, vi, viplus, polygon, linesOfPolygon, triangles);
                    if (liesInside) {
                        Polygon toBeAdded = createNewTriangle(viminus, vi, viplus);
                        Rectangle2D boundingBox = toBeAdded.getBounds2D();
                        System.out.println("current biggestRatio: "+biggestRatio);
                        double ratio = boundingBox.getWidth()/boundingBox.getHeight();
                        System.out.println("current triangleRatio: "+ ratio);

                        if (ratio>biggestRatio){
                            biggestEar=toBeAdded;
                            biggestRatio=ratio;
                            respectiveIndex = i;
                            System.out.println("new biggestRatio: "+ biggestRatio);
                            System.out.println("respectiveIndex: "+respectiveIndex);
                        }
                    }



                    //if line between viminus and viplus lies COMPLETELY inside polygon, add the triangle...
                    System.out.println(polygon.size());
                    System.out.println("BOOLEAN: " + liesInside);
                    System.out.println("viminus: " + viminus.x + "," + viminus.y);
                    System.out.println("vi: " + vi.x + "," + vi.y);
                    System.out.println("viplus: " + viplus.x + "," + viplus.y);



                } else if (i==polygon.size()-1){

                    viminus = new Point(polygon.get(i-1));
                    vi = new Point(polygon.get(i));
                    viplus = new Point(polygon.get(0));

                    boolean liesInside = liesCompletelyInside(viminus, vi, viplus, polygon, linesOfPolygon, triangles);
                    if (liesInside) {
                        Polygon toBeAdded = createNewTriangle(viminus, vi, viplus);
                        Rectangle2D boundingBox = toBeAdded.getBounds2D();
                        System.out.println("current biggestRatio: "+biggestRatio);
                        double ratio = boundingBox.getWidth()/boundingBox.getHeight();
                        System.out.println("current triangleRatio: "+ ratio);

                        if (ratio>biggestRatio){
                            biggestEar=toBeAdded;
                            biggestRatio=ratio;
                            respectiveIndex = i;
                            System.out.println("new biggestRatio: "+ biggestRatio);
                            System.out.println("respectiveIndex: "+respectiveIndex);
                        }
                    }

                    //if line between viminus and viplus lies COMPLETELY inside polygon, add the triangle...
                    System.out.println(polygon.size());
                    System.out.println("BOOLEAN: " + liesInside);
                    System.out.println("viminus: " + viminus.x + "," + viminus.y);
                    System.out.println("vi: " + vi.x + "," + vi.y);
                    System.out.println("viplus: " + viplus.x + "," + viplus.y);


                } else {
                    viminus = new Point(polygon.get(polygon.size()-1));
                    vi = new Point(polygon.get(i));
                    viplus = new Point(polygon.get(i+1));

                    boolean liesInside = liesCompletelyInside(viminus, vi, viplus, polygon, linesOfPolygon, triangles);
                    if (liesInside) {
                        Polygon toBeAdded = createNewTriangle(viminus, vi, viplus);
                        Rectangle2D boundingBox = toBeAdded.getBounds2D();
                        System.out.println("current biggestRatio: "+biggestRatio);
                        double ratio = boundingBox.getWidth()/boundingBox.getHeight();
                        System.out.println("current triangleRatio: "+ ratio);

                        if (ratio>biggestRatio){
                            biggestEar=toBeAdded;
                            biggestRatio=ratio;
                            respectiveIndex = i;
                            System.out.println("new biggestRatio: "+ biggestRatio);
                            System.out.println("respectiveIndex: "+respectiveIndex);
                        }
                    }
                    //if line between viminus and viplus lies COMPLETELY inside polygon, add the triangle...
                    System.out.println(polygon.size());
                    System.out.println("BOOLEAN: " + liesInside);
                    System.out.println("viminus: " + viminus.x + "," + viminus.y);
                    System.out.println("vi: " + vi.x + "," + vi.y);
                    System.out.println("viplus: " + viplus.x + "," + viplus.y);

                }
            }
            System.out.println();
            System.out.println("Biggest Ear: ");
            for (int i=0;i<biggestEar.xpoints.length; i++){
                System.out.println(biggestEar.xpoints[i]+","+biggestEar.ypoints[i]);
            }
            System.out.println("index: "+respectiveIndex);
            triangles.add(biggestEar);
            polygon.remove(respectiveIndex);
        }
        Polygon remainingPolygon = createNewTriangle(polygon.get(0), polygon.get(1), polygon.get(2));
        triangles.add(remainingPolygon);
        polygon=null;
        System.out.println("FINAL triangles");
        for (int i=0; i<triangles.size(); i++){
            System.out.println("triangle: "+ i);
            Polygon p = triangles.get(i);
            for (int j=0; j<triangles.get(i).xpoints.length; j++){
                int x = p.xpoints[j];
                int y = p.ypoints[j];
                System.out.println(x+","+y);
            }
        }


        return triangles;
    }
}
