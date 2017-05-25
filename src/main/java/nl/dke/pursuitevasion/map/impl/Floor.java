package nl.dke.pursuitevasion.map.impl;

import nl.dke.pursuitevasion.map.AbstractObject;
import nl.dke.pursuitevasion.map.ObjectType;
import nl.dke.pursuitevasion.map.MapPolygon;

import java.awt.*;
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

    public ArrayList<Point> newSimplePolygon1 = new ArrayList<>();
    public ArrayList<Point> newSimplePolygon2 = new ArrayList<>();

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
        smallestDistance=Integer.MAX_VALUE;
        Point floorCon1 = null;
        Point obsCon1 = null;

        //loop through obstaclelist
        for (int k=0; k<ob.size(); k++) {
            //loops through points of each obstacle
            for (int l = 0; l < ob.get(k).getPolygon().xpoints.length; l++) {
                if (k != ob.size() - 1) {
                    //                            System.out.println("2nd: Compute closest link");
                    //loops through points of neighbouring obstacle
                    for (int m = 0; m < ob.get(k+1).getPolygon().xpoints.length; m++) {
                        if (computeDistance(
                                ob.get(k).getPolygon().xpoints[l],
                                ob.get(k+1).getPolygon().xpoints[m],
                                ob.get(k).getPolygon().ypoints[l],
                                ob.get(k+1).getPolygon().ypoints[m]
                        ) < smallestDistance) {
                            smallestDistance = computeDistance(ob.get(k).getPolygon().xpoints[l],ob.get(k+1).getPolygon().xpoints[m],ob.get(k).getPolygon().ypoints[l],ob.get(k+1).getPolygon().ypoints[m]);
                            obsCon1 = new Point(ob.get(k).getPolygon().xpoints[l], ob.get(k).getPolygon().ypoints[l]);
                            floorCon1 = new Point(ob.get(k+1).getPolygon().xpoints[m], ob.get(k+1).getPolygon().ypoints[m]);
                            //                                    System.out.println("distance: " + smallestDistance);
                            //                                    System.out.println("obsCon: " + obsCon);
                            //                                    System.out.println("floorCon: " + floorCon);
                        }
                    }
                } else {
                    // connect last of the chain to other border
                    //                            System.out.println("3rd: compute last link to outer polygon");
                    smallestDistance = Integer.MAX_VALUE;
                    //loops through points of floor
                    for (int j = 0; j < getPolygon().xpoints.length; j++) {
                        if (computeDistance(
                                ob.get(k).getPolygon().xpoints[l],
                                getPolygon().xpoints[j],
                                ob.get(k).getPolygon().ypoints[l],
                                getPolygon().ypoints[j])
                                <= smallestDistance) {
                            smallestDistance = computeDistance(ob.get(k).getPolygon().xpoints[l],getPolygon().xpoints[l],ob.get(k).getPolygon().ypoints[l],getPolygon().ypoints[l]);
                            obsCon = new Point(ob.get(k).getPolygon().xpoints[l], ob.get(k).getPolygon().ypoints[l]);
                            floorCon = new Point(getPolygon().xpoints[j], getPolygon().ypoints[j]);
                        }
                    }
                }
            }
        }
        if (obsCon1!=null) {
            conns.add(new ArrayList<>());
            conns.get(conns.size() - 1).add(obsCon1);
            conns.get(conns.size() - 1).add(floorCon1);
        }

        conns.add(new ArrayList<>());
        conns.get(conns.size()-1).add(obsCon);
        conns.get(conns.size()-1).add(floorCon);



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

        polygon1 = findObstaclePart(obstacleVertices, conns, firstConnection1, polygon1);
        Point secondConnection1 = polygon1.get(polygon1.size()-1);
        polygon1.remove(polygon1.size()-1);

        polygon1 = findLastPartOfFloor(floorVertices, firstConnection1, secondConnection1, conns, polygon1);
        newSimplePolygon1=polygon1;

        System.out.println();
        System.out.println("Polygon 2");
        //get order for second polygon
        ArrayList<Point> polygon2 = findFirstPartOfFloor(index+1, floorVertices, conns, finalPolygon2);
        //since we don't need index anymore, last element can be deleted immediately
        polygon2.remove(polygon2.size()-1);
        Point firstConnection2 = polygon2.get(polygon2.size()-1);
        polygon2.remove(polygon2.size()-1);

        polygon2 = findObstaclePart(obstacleVertices, conns, firstConnection2, polygon2);
        Point secondConnection2 = polygon2.get(polygon2.size()-1);
        polygon2.remove(polygon2.size()-1);

        polygon2 = findLastPartOfFloor(floorVertices, firstConnection2, secondConnection2, conns, polygon2);
        newSimplePolygon2=polygon2;

        return conns;
    }

    public ArrayList<Point> findFirstPartOfFloor(int startingIndex, Point[] floorVertices, ArrayList<ArrayList<Point>> conns, ArrayList<Point> newPolygon){
        Point firstConnection = null;
        Point index=null;
        boolean firstConnectionFound = false;
        for (int h=startingIndex; h<floorVertices.length; h++) {
            //checks if the given floor point is equal to one of the floor points in the connections list
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
        newPolygon.add(index);
        return newPolygon;
    }

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

        for (int i=indexOfObstaclePoint[0]; i>=0; i--){
            System.out.println("index: " + i);
            // is this constraint right?
            if (i!=0) {
                System.out.println("first part");
                //start searching from found point backwards until you reach the first index
                for (int j = indexOfObstaclePoint[1]; j >= 0; j--) {
                    for (int k=0; k<otherObstacleConnections.length; k++) {
                        if (otherObstacleConnections[k].equals(new Point(-1,-1))) {
                            otherObstacleConnections = searchForIntraObstacleConnection(otherObstacleConnections, newPolygon, obstacleVertices, indexOfObstaclePoint[1], conns, i, j, k);
                        }
                    }
                    System.out.println("HELLO");
                    System.out.println("secondConn.toString: "+secondConnection.getX());
                    if (secondConnection.getLocation().equals(new Point(-1,-1))) {
                        secondConnection = searchForSecondConnection(secondConnection, newPolygon, obstacleVertices, indexOfObstaclePoint[1], conns, i, j);
                    }
                }
            } else {
                System.out.println("second part");
                for (int l=obstacleVertices.size()-1; l > indexOfObstaclePoint[0]; l--){
                    //start searching from found point backwards until you reach the first index
                    for (int j = indexOfObstaclePoint[1]; j >= 0; j--) {
                        System.out.println(otherObstacleConnections[0].x);
                        for (int k=0; k<otherObstacleConnections.length; k++) {
                            if (otherObstacleConnections[k].equals(new Point(-1,-1))) {
                                otherObstacleConnections = searchForIntraObstacleConnection(otherObstacleConnections, newPolygon, obstacleVertices, indexOfObstaclePoint[1], conns, l, j, k);
                            }
                        }
                        System.out.println("HOI");
                        System.out.println("secondConn.toString: "+secondConnection.getX());
                        if (secondConnection.getLocation().equals(new Point(-1,-1))) {
                            secondConnection = searchForSecondConnection(secondConnection, newPolygon, obstacleVertices, indexOfObstaclePoint[1], conns, l, j);
                        }

                    /*    if (!secondConnectionFound) {
                            newPolygon.add(obstacleVertices.get(l).get(j));
                            if (j != indexOfObstaclePoint[1] && checkForConn(obstacleVertices.get(l).get(j), conns)) {
                                secondConnection = getConn(obstacleVertices.get(l).get(j), conns);
                                newPolygon.add(secondConnection);
                                secondConnectionFound = true;
                            } else {
                                //if you reached the first element, continue at the end of the list until the found first connection
                                for (int k = obstacleVertices.get(l).size() - 1; k > indexOfObstaclePoint[1]; k--) {
                                    if (!secondConnectionFound) {
                                        newPolygon.add(obstacleVertices.get(l).get(k));
                                        if (checkForConn(obstacleVertices.get(l).get(k), conns)) {
                                            secondConnection = getConn(obstacleVertices.get(l).get(k), conns);
                                            newPolygon.add(secondConnection);
                                            secondConnectionFound = true;
                                        }
                                    }
                                }
                            }
                        } */
                    }
                }
            }
        }
        System.out.println();
        for (int i=0; i<newPolygon.size(); i++){
            System.out.println(newPolygon.get(i));
        }
        System.out.println();
        return  newPolygon;
    }

    public Point[] searchForIntraObstacleConnection(Point[] otherObstacleConnections, ArrayList<Point> newPolygon,
                                                    ArrayList<ArrayList<Point>> obstacleVertices, int indexOfObstaclePoint,
                                                    ArrayList<ArrayList<Point>> conns, int i, int j, int m){

        boolean found=false;
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
                                           ArrayList<ArrayList<Point>> obstacleVertices, int indexOfObstaclePoint,
                                           ArrayList<ArrayList<Point>> conns, int i, int j){
        boolean found=false;
        newPolygon.add(obstacleVertices.get(i).get(j));
        if (j != indexOfObstaclePoint && checkForConn(obstacleVertices.get(i).get(j), conns)) {
            secondConnection = getConn(obstacleVertices.get(i).get(j), conns);
            newPolygon.add(secondConnection);
            found = true;
        } else {
            //if you reached the first element, continue at the end of the list until the found first connection
            for (int k = obstacleVertices.get(i).size() - 1; k > indexOfObstaclePoint; k--) {
                if (!found) {
                    newPolygon.add(obstacleVertices.get(i).get(k));
                    if (checkForConn(obstacleVertices.get(i).get(k), conns)) {
                        secondConnection = getConn(obstacleVertices.get(i).get(k), conns);
                        newPolygon.add(secondConnection);
                        found = true;
                    }
                }
            }
        }

        return secondConnection;
    }

    public ArrayList<Point> findLastPartOfFloor(Point[] floorVertices, Point firstConnection, Point secondConnection, ArrayList<ArrayList<Point>> conns, ArrayList<Point> newPolygon){
        int indexOfConnectionFromObstacle=-1;
        for (int i=0; i<floorVertices.length; i++){
            if (secondConnection.equals(floorVertices[i])){
                indexOfConnectionFromObstacle = i;
            }
        }
        for (int i=indexOfConnectionFromObstacle; i<floorVertices.length; i++){
            newPolygon.add(floorVertices[i]);
            if ((floorVertices[i].equals(getConn(firstConnection, conns))) && !(i>=floorVertices.length-1)){
                return  newPolygon;
            }
            if (!(floorVertices[i].equals(getConn(firstConnection, conns))) && i>=floorVertices.length-1){
                for (int j=0; j<indexOfConnectionFromObstacle; j++){
                    newPolygon.add(floorVertices[j]);
                    if ((floorVertices[j].equals(getConn(firstConnection, conns)))){
                        return newPolygon;
                    }
                }
            }
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
}
