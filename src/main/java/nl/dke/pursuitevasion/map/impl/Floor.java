package nl.dke.pursuitevasion.map.impl;

import javafx.beans.property.ObjectPropertyBase;
import nl.dke.pursuitevasion.map.AbstractObject;
import nl.dke.pursuitevasion.map.ObjectType;
import nl.dke.pursuitevasion.map.MapPolygon;

import java.awt.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

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

    public ArrayList<Polygon> getTriangulation(){
        ArrayList<Polygon> triangles = new ArrayList<>();

        // compute closest distance to outer polygon
        Polygon mPoly = getPolygon();
        Point[][] connection = new Point[obstacles.size()][2];   // create list for connections, for each obstacle a connection and with the two vertices of new connection
        ArrayList<ArrayList<Point>> conns = new ArrayList<>();
        for (Obstacle obs  : obstacles){
            double smallestDistance=Integer.MAX_VALUE;
            Polygon oPoly = obs.getPolygon();
            for(int i=0; i<oPoly.xpoints.length; i++){
            //for all cornerpoints per obstacles,
                for (int j=0; j<getPolygon().xpoints.length; j++){
                // compute the difference to each of the cornerpoints of the main/outer polygon
                    // maybe turn this into method?
                    if (computeDistance(oPoly.xpoints[i],mPoly.xpoints[i],oPoly.ypoints[i],mPoly.ypoints[i])<smallestDistance){
                        connection[0][0]=new Point(oPoly.xpoints[j], oPoly.ypoints[j]);
                        connection[0][1]=new Point(mPoly.xpoints[j], mPoly.ypoints[j]);
                    }
                }
                // put here code from below?
            }
        }

        // compute closest link between obstacles
        Obstacle[] obstacleList = (Obstacle[]) obstacles.toArray();
        double smallestDistance=Integer.MAX_VALUE;
        for (int k=0; k<obstacleList.length; k++){
            for (int l=0; k<obstacleList[k].getPolygon().xpoints.length; k++){
                if (k!=obstacleList.length-1){
                    for (int m=0; m<obstacleList[k+1].getPolygon().xpoints.length; m++)     {
                        if (computeDistance(obstacleList[k].getPolygon().xpoints[l],
                                            obstacleList[k+1].getPolygon().xpoints[l],
                                            obstacleList[k].getPolygon().ypoints[l],
                                            obstacleList[k+1].getPolygon().ypoints[l],
                                            )<smallestDistance){
                            connection[0][0]=new Point(obstacleList[k].getPolygon().xpoints[l], obstacleList[k].getPolygon().ypoints[l]);
                            connection[0][1]=new Point(obstacleList[k+1].getPolygon().xpoints[l], obstacleList[k+1].getPolygon().ypoints[l]);
                        }
                    }
                } else {

                }
            }
        }

        // connect last of the chain to other border (?)

        // find point on where to "cut" the connection in int[] x and y list, meaning, where to insert the new vertices in the list
        //  write method for this, used for other obstacles as well

        // split poylgon in two parts

        //implement triangulation by ear-clipping

        return triangles;
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
