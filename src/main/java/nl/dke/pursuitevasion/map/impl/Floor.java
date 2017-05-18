package nl.dke.pursuitevasion.map.impl;

import javafx.beans.property.ObjectPropertyBase;
import nl.dke.pursuitevasion.map.AbstractObject;
import nl.dke.pursuitevasion.map.ObjectType;
import nl.dke.pursuitevasion.map.MapPolygon;

import java.awt.*;
import java.lang.reflect.Array;
import java.util.*;

/**
 * A floor is a 2d level in which agents can walk
 *
 * Created by nik on 2/8/17.
 */
public class Floor extends AbstractObject
{

    /**
     * Obstacles can be placed on the floor to obstruct vision
     */
    private final Collection<Obstacle> obstacles;

    /**
     * Gates can be placed on the floor to go to another floor
     */
    private final Collection<Gate> gates;

    /**
     * Create a floor object
     *
     * @param polygon the polygon of this floor
     * @param floodID the id of this floor
     * @param obstacles a list of obstacles placed on this floor
     * @param gates a list of gates placed on this floor
     */
    public Floor(MapPolygon polygon, int floodID, Collection<Obstacle> obstacles, Collection<Gate> gates)
        throws IllegalArgumentException
    {
        super(polygon, floodID);
        verifyInsideFloor(obstacles);
        verifyInsideFloor(gates);
        this.obstacles = Collections.unmodifiableCollection(obstacles);
        this.gates = Collections.unmodifiableCollection(gates);
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

    public ArrayList<ArrayList<Point>> getTriangulation(){
        ArrayList<Polygon> triangles = new ArrayList<>();

        ArrayList<Obstacle> ob = new ArrayList<>(this.obstacles);

        // compute closest distance to outer polygon
        Polygon mPoly = getPolygon();
        // create list for connections, for each obstacle a connection and with the two vertices of new connection
        ArrayList<ArrayList<Point>> conns = new ArrayList<ArrayList<Point>>();

        System.out.println("1st: compute first link to outer polygon");
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
                        System.out.println("distance: " + smallestDistance);
                        System.out.println("obsCon: " + obsCon);
                        System.out.println("floorCon: " + floorCon);
                    }
                }
                // put here code from below?
            }
        }
        conns.add(new ArrayList<>());
        conns.get(conns.size()-1).add(obsCon);
        conns.get(conns.size()-1).add(floorCon);
        System.out.println("conns" + conns.toString());


        // compute closest link between obstacles
        smallestDistance=Integer.MAX_VALUE;
        Point floorCon1 = null;
        Point obsCon1 = null;
        //loop through obstaclelist
        System.out.println("2nd: Compute closest link");
            for (int k=0; k<ob.size(); k++) {
                //loops through points of each obstacle
                for (int l = 0; l < ob.get(k).getPolygon().xpoints.length; l++) {
                    if (k != ob.size() - 1) {
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
                                System.out.println("distance: " + smallestDistance);
                                System.out.println("obsCon: " + obsCon);
                                System.out.println("floorCon: " + floorCon);
                            }
                        }
                        //add adding here




                    } else {
                        System.out.println("3rd: compute last link to outer polygon");
                        smallestDistance = Integer.MAX_VALUE;
                        //loops through points of floor
                        for (int j = 0; j < getPolygon().xpoints.length; j++) {
                            if (computeDistance(
                                    ob.get(k).getPolygon().xpoints[l],
                                    getPolygon().xpoints[j],
                                    ob.get(k).getPolygon().ypoints[l],
                                    getPolygon().ypoints[j])
                                    < smallestDistance) {
                                smallestDistance = computeDistance(ob.get(k).getPolygon().xpoints[l],getPolygon().xpoints[l],ob.get(k).getPolygon().ypoints[l],getPolygon().ypoints[l]);
                                obsCon = new Point(ob.get(k).getPolygon().xpoints[l], ob.get(k).getPolygon().ypoints[l]);
                                floorCon = new Point(getPolygon().xpoints[j], getPolygon().ypoints[j]);
                                System.out.println("distance: " + smallestDistance);
                                System.out.println("obsCon: " + obsCon);
                                System.out.println("floorCon: " + floorCon);
                            }
                        }
                        //add adding here




                    }
                }
            }

            conns.add(new ArrayList<>());
            conns.get(conns.size()-1).add(obsCon1);
            conns.get(conns.size()-1).add(floorCon1);
            System.out.println("conns" + conns.toString());

            conns.add(new ArrayList<>());
            conns.get(conns.size()-1).add(obsCon);
            conns.get(conns.size()-1).add(floorCon);
            System.out.println("conns" + conns.toString());


        // connect last of the chain to other border (?)

        // find point on where to "cut" the connection in int[] x and y list, meaning, where to insert the new vertices in the list
        //  write method for this, used for other obstacles as well

        // split poylgon in two parts

        //implement triangulation by ear-clipping

        return conns;
    }

    public double computeDistance(int x1,int x2,int y1,int y2){
        return Math.abs(Math.sqrt(Math.pow((x1-x2), 2)+ Math.pow((y1-y2), 2)));
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
}
