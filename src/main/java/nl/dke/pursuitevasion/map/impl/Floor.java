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
                            smallestDistance = computeDistance(ob.get(k).getPolygon().xpoints[l],ob.get(k+1).getPolygon().xpoints[l],ob.get(k).getPolygon().ypoints[l],ob.get(k+1).getPolygon().ypoints[l]);
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

/*        System.out.println("CONNECTIONS");
        for (int i=0; i<conns.size(); i++){
            System.out.println("Obstacles: " + conns.get(i).get(0).x + "," + conns.get(i).get(0).y);
            System.out.println("Floor: " + conns.get(i).get(1).x + "," + conns.get(i).get(1).y);
        }
        System.out.println("");
*/
        //create new arraylists to store the new vertices of the new simple polygons
        ArrayList<Point> finalPolygon1 = new ArrayList<>();
        ArrayList<Point> finalPolygon2 = new ArrayList<>();

        //get order for first polygon
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

/*        //get order for second polygon
        ArrayList<Point> polygon2 = findFirstPartOfFloor(1+1, floorVertices, conns, finalPolygon2);
        //since we don't need index anymore, last element can be deleted immediately
        polygon2.remove(polygon2.size()-1);
        System.out.println("WTF: "+polygon2.get(polygon2.size()-1));
        Point firstConnection2 = polygon2.get(polygon2.size()-1);
        polygon2.remove(polygon2.size()-1);

        polygon2 = findObstaclePart(obstacleVertices, conns, firstConnection2, polygon2);
        Point secondConnection2 = polygon2.get(polygon2.size()-1);
        polygon2.remove(polygon2.size()-1);

        polygon2 = findLastPartOfFloor(floorVertices, firstConnection2, secondConnection2, conns, polygon2);
        newSimplePolygon2=polygon2;
*/
        System.out.println("1");
        for (int i=0; i<newSimplePolygon1.size(); i++){
            System.out.println(newSimplePolygon1.get(i));
        }
        System.out.println();
        System.out.println("2");
        for (int i=0; i<newSimplePolygon2.size(); i++){
            System.out.println(newSimplePolygon2.get(i));
        }

        return conns;
    }

    public ArrayList<Point> findFirstPartOfFloor(int startingIndex, Point[] floorVertices, ArrayList<ArrayList<Point>> conns, ArrayList<Point> newPolygon){
        System.out.println("PART 1");
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
                    //newSimplePolygon1.add(floorVertices[h]);
                    //newSimplePolygon1.add(firstConnection);
                    firstConnectionFound = true;
                }
            }
        }

        System.out.println();
        System.out.println("POLYGON AFTER 1");
        for (int i=0; i<newPolygon.size(); i++){
            System.out.println(newPolygon.get(i));
        }
        System.out.println();

        newPolygon.add(index);
        return newPolygon;
    }

    public ArrayList<Point> findObstaclePart(ArrayList<ArrayList<Point>> obstacleVertices, ArrayList<ArrayList<Point>> conns, Point firstConnection, ArrayList<Point> newPolygon){
        System.out.println("PART 2");
        Point secondConnection = null;
        boolean secondConnectionFound = false;
        //now continue searching for the next connected vertex inside the obstacles array in opposite direction
        //first determine to which obstacle the connected vertex belongs to
        //loop over array of obstacles
        int[] indexOfObstaclePoint = getIndexOfObstacleConn(firstConnection, obstacleVertices);
        //maybe check later if this isnt the first obstacle, if it is, then its fine,
        //if its second or higher, then the missing first parts need to be covered as well in another loop
        System.out.println(firstConnection);
        for (int i=indexOfObstaclePoint[0]; i<obstacleVertices.size(); i++){
            //start searching from found point backwards until you reach the first index
            System.out.println("indexObstacleP: "+ (indexOfObstaclePoint[1]));
            for (int j=indexOfObstaclePoint[1]; j>=0; j--){
                System.out.println("j: "+j);
                //newSimplePolygon1.add(obstacleVertices.get(i).get(j));
                if (!secondConnectionFound) {
                    System.out.println("hello");
                    System.out.println("adding: "+obstacleVertices.get(i).get(j));
                    System.out.println(checkForConn(obstacleVertices.get(i).get(j), conns));
                    newPolygon.add(obstacleVertices.get(i).get(j));
                    if (j!=indexOfObstaclePoint[1] && checkForConn(obstacleVertices.get(i).get(j), conns) && !(j <= 0)) {
                        System.out.println("hey");
                        secondConnection = getConn(obstacleVertices.get(i).get(j), conns);
                        newPolygon.add(secondConnection);
                        //newSimplePolygon1.add(secondConnection);
                        secondConnectionFound = true;
                    }
                    if (j <= 0) {
                        System.out.println("hi");
                        //if you reached the first element, continue at the end of the list until the found first connection
                        for (int k = obstacleVertices.get(i).size() - 1; k > indexOfObstaclePoint[1]; k--) {
                            System.out.println("k: "+k);
                            if (!secondConnectionFound) {
                                newPolygon.add(obstacleVertices.get(i).get(k));
                                //newSimplePolygon1.add(obstacleVertices.get(i).get(k));
                                if (checkForConn(obstacleVertices.get(i).get(k), conns)) {
                                    secondConnection = getConn(obstacleVertices.get(i).get(k), conns);
                                    newPolygon.add(secondConnection);
                                    //newSimplePolygon1.add(secondConnection);
                                    secondConnectionFound = true;
                                    // end
                                }
                            }
                        }
                    }
                }
            }

        }
        System.out.println();
        System.out.println("POLYGON AFTER 2");
        for (int i=0; i<newPolygon.size(); i++){
            System.out.println(newPolygon.get(i));
        }
        System.out.println();
        //newPolygon.add(secondConnection);
        return  newPolygon;
    }

    public ArrayList<Point> findLastPartOfFloor(Point[] floorVertices, Point firstConnection, Point secondConnection, ArrayList<ArrayList<Point>> conns, ArrayList<Point> newPolygon){
        System.out.println("PART 3");
        int indexOfConnectionFromObstacle=-1;
        System.out.println(firstConnection);
        System.out.println(secondConnection);
        for (int i=0; i<floorVertices.length; i++){
            if (secondConnection.equals(floorVertices[i])){
                indexOfConnectionFromObstacle = i;
            }
        }
        System.out.println("indexOfConnectionFromObstacle: " + indexOfConnectionFromObstacle);
        System.out.println("floorVertices: "+floorVertices.length);
        for (int i=indexOfConnectionFromObstacle; i<floorVertices.length; i++){
            System.out.println("i: " +i);
//            System.out.println(floorVertices[i]);
            System.out.println(floorVertices[i].equals(getConn(firstConnection, conns)));
            newPolygon.add(floorVertices[i]);
            if ((floorVertices[i].equals(getConn(firstConnection, conns))) && !(i>=floorVertices.length-1)){
                System.out.println("HELLO");
                System.out.println(floorVertices[i]);
                return  newPolygon;
            }
            if (i>=floorVertices.length-1){
                System.out.println("HEY");
                for (int j=0; j<indexOfConnectionFromObstacle; j++){
                    newPolygon.add(floorVertices[j]);
                    //newSimplePolygon1.add(floorVertices[j]);
                    if ((floorVertices[j].equals(getConn(firstConnection, conns)))){
                        System.out.println("HOI");
                        System.out.println(floorVertices[i]);
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

    //public int getIndexOfConnection()

    public double computeDistance(int x1,int x2,int y1,int y2){
        return Math.abs(Math.sqrt(Math.pow((x1-x2), 2)+ Math.pow((y1-y2), 2)));
    }
}
