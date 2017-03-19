package nl.dke.pursuitevasion.gui;
/******************************************************************************
 *  Compilation:  javac Voronoi.java
 *  Execution:    java Voronoi
 *  Dependencies: Draw.java DrawListener.java
 *
 *  Plots the points that the user clicks, and draws the Voronoi diagram.
 *  We assume the points lie on an M-by-M grid and use a brute force
 *  discretized algorithm. Each insertion takes time proportional to M^2.
 *
 *  Limitations
 *  -----------
 *    - Running time scales (badly) with M
 *    - Fortune's algorithm can compute a Voronoi diagram on N
 *      points in time proportional to N log N, but it is
 *      subtantially more complicated than this program which is intended
 *      to demonstrate callbacks and GUI operations.
 *
 ******************************************************************************/

import nl.dke.pursuitevasion.gui.experimental.Draw;
import nl.dke.pursuitevasion.gui.experimental.DrawListener;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Arc2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Voronoi implements DrawListener,MouseListener {
    private static int SIZE = 512;

    private Point[][] nearest = new Point[SIZE][SIZE];  // which point is pixel (i, j) nearest?

    private Draw draw = new Draw();


    public Voronoi() {
        draw.setCanvasSize(SIZE, SIZE);
        draw.setXscale(0, SIZE);
        draw.setYscale(0, SIZE);
        draw.addListener(this);
        draw.show(20);
    }

    public static HashMap<Point,Polygon> listOfCentresToPolygon(ArrayList<Point> centers, Dimension size){
        ArrayList<ArrayList<Point>> voronoiMask = listOfCentresToVoronoi(centers, size);
        HashMap<Point, Polygon> polyPoint = new HashMap<>(centers.size());

        for (int j = 0; j < centers.size(); j++) {

            ArrayList<Point> shadow = voronoiMask.get(j);

            QuickHull qh = new QuickHull();
            ArrayList<Point> p = qh.quickHull(shadow);
            int[] xs2 = new int[p.size()];
            int[] ys2 = new int[p.size()];
            int n = p.size();

            for (int i = 0; i < p.size(); i++) {
                Point ps =  p.get(i);
                xs2[i] = ps.x;
                ys2[i] = ps.y;
            }

            Polygon poly = new Polygon(xs2,ys2,p.size());
            polyPoint.put(centers.get(j),poly);
        }

        return polyPoint;
    }

    public static ArrayList<ArrayList<Point>> listOfCentresToVoronoi(ArrayList<Point> centers, Dimension size){
        int width = size.width;
        int height = size.height;


        Point[][] nearest = new Point[size.width][size.height];
        int[][] nearestInt = new int[size.width][size.height];
        Point[][] points = new Point[size.width][size.height];
        ArrayList<ArrayList<Point>> returnValue = new ArrayList<>(centers.size());
        for (int i = 0; i < centers.size(); i++) {
            returnValue.add(new ArrayList<>(size.width*size.height/centers.size()));
        }

        if (centers.size()==0) return returnValue;

        //setting all the points to the nearest "well"
        for (int c = 0; c < centers.size(); c++) {
            Point c_x = centers.get(c);

            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    Point q = new Point(i, j);
                    points[i][j] = q;
                    if ((nearest[i][j] == null) || (DistanceFromToSqrt(q, c_x) < DistanceFromToSqrt(q, nearest[i][j]))) {
                        nearest[i][j] = c_x;
                        nearestInt[i][j]=c;
                    }
                }
            }

        }


        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                ArrayList<Point> drawer = returnValue.get(nearestInt[i][j]);
                drawer.add(points[i][j]);
            }
        }

        return returnValue;
    }

    public void mousePressed(double x, double y) {
        Point p = new Point((int)x,(int) y);
        System.out.println("Inserting:       " + p);

        // compare each pixel (i, j) and find nearest point
        draw.setPenColor(Color.getHSBColor((float) Math.random(), .7f, .7f));
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                Point q = new Point(i, j);
                if ((nearest[i][j] == null) || (DistanceFromToSqrt(q, p) < DistanceFromToSqrt(q, nearest[i][j]))) {
                    nearest[i][j] = p;
                    draw.filledSquare(i+0.5, j+0.5, 0.5);
                }
            }
        }

        // draw the point afterwards
        draw.setPenColor(Color.BLACK);
        draw.filledCircle(x, y, 4);


        draw.show(20);
        System.out.println("Inserting:       " + p);
    }

    private static double DistanceFromToSqrt(Point q, Point p) {
        //double d = Math.sqrt((q.x-p.x)*(q.x-p.x)+(q.y-p.y)*(q.y-p.y));

        double e = (q.x-p.x)*(q.x-p.x)+(q.y-p.y)*(q.y-p.y);
       // if (Double.isNaN(d)){
        //    System.out.println("ere");
       // }
        //System.out.println("Distance from " + q  + " to p: " + p + ": " + d);
        return e;
    }


    // save the screen to a file
    public void keyTyped(char c) {  }

    // must implement these since they're part of the interface
    public void keyPressed(int keycode)  { }
    public void keyReleased(int keycode) { }
    public void mouseDragged(double x, double y)  { }
    public void mouseReleased(double x, double y) { }


    // test client
    public static void main(String[] args) {
        new Voronoi();
    }


    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton()==1){
            mousePressed((double)e.getX(),(double)e.getY());
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}