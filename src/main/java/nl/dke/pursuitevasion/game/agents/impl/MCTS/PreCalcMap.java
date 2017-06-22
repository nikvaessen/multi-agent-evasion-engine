package nl.dke.pursuitevasion.game.agents.impl.MCTS;

import nl.dke.pursuitevasion.game.EngineConstants;
import nl.dke.pursuitevasion.game.Vector2D;
import nl.dke.pursuitevasion.game.agents.AbstractAgent;
import nl.dke.pursuitevasion.gui.simulator.MapViewPanel;
import nl.dke.pursuitevasion.map.impl.Floor;
import nl.dke.pursuitevasion.map.impl.Map;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.HashMap;

/**
 * Created by Nibbla on 22.06.2017.
 */
public class PreCalcMap {
    private final double deltaX;
    private final double deltaY;
    private final int offsetX;
    private final int offsetY;
    private final Rectangle2D r;
    private final Map map;
    private Sink sink;
    private HashMap<Point, Sink> sinks = new HashMap<Point, Sink>(EngineConstants.preCalcSize*EngineConstants.preCalcSize);


    public boolean[][] getIsLegal() {
        return isLegal;
    }

    boolean[][] isLegal = new boolean[EngineConstants.preCalcSize][EngineConstants.preCalcSize];

    public Vector2D[][] getPos() {
        return pos;
    }

    Vector2D[][] pos = new Vector2D[EngineConstants.preCalcSize][EngineConstants.preCalcSize];

    public PreCalcMap(Map map) {
        this.map = map;

        Rectangle2D d = null;
        for (Floor f : map.getFloors()){
            d = f.getPolygon().getBounds2D();
        }
        r = d;
         deltaX = (r.getMaxX()-r.getMinX())/EngineConstants.preCalcSize;
         deltaY = (r.getMaxY()-r.getMinY())/EngineConstants.preCalcSize;

         offsetX = EngineConstants.preCalcSize/2;
         offsetY = EngineConstants.preCalcSize/2;

        for (int x = 0; x < EngineConstants.preCalcSize; x++) {
            for (int y = 0; y < EngineConstants.preCalcSize; y++) {
                Vector2D v = new Vector2D(offsetX+r.getMinX()+x*deltaX,offsetY+r.getMinY()+y*deltaY);
                isLegal[x][y] = map.isInsideAndLegal(v);
                pos[x][y] = v;


            }
        }
        prepareSinks();
    }

    public Point ScreenPosToXY (Point s){
        Point result = new Point((int) ((s.getX()-r.getMinX())/deltaX),(int) ((s.getY()-r.getMinY())/deltaY));


        return result;
    }

    public Point ScreenPosToXY (Vector2D s){
        Point result = new Point((int) ((s.getX()-r.getMinX())/deltaX),(int) ((s.getY()-r.getMinY())/deltaY));


        return result;
    }

    public Vector2D XYToScreenPos (int x,int y){
        //Point p = new Point((int)(offsetX+r.getMinX()+x*deltaX),(int)(offsetY+r.getMinY()+y*deltaY));


        return pos[x][y];
    }



    public Sink getSink(Point pp) {

        Sink s = sinks.get(pp);
        if (s==null) {
            s= new Sink(pp);
            sinks.put(pp,s);
        }
        //System.out.println("eee" + s);
        return s;
    }

    public void draw(Graphics g, MCTS_2.MCTSViewSettings mctsViewSettings, MapViewPanel mapViewPanel, Collection<AbstractAgent> agents) {
        boolean[][] il = getIsLegal();
        Vector2D[][] pos = getPos();
        int w = il.length;
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < w; y++) {
                Vector2D v = pos[x][y];
                if (il[x][y]) g.fillOval((int)v.getX()-6,(int)v.getY()-6,12,12);
            }

        }

        g.setColor(Color.GREEN);
       // if (sink == null) return;

       // Vector2D p = XYToScreenPos(sink.centerX,sink.centerY);
       // g.fillOval((int)p.x-10,(int)p.y-10,20,20);
        g.setColor(Color.BLUE);
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < w; y++) {
                Vector2D sp = XYToScreenPos(x,y);


                double v = getValueOfEvador(x,y,agents);
                Color c = new Color((float)(1-(v/900)),(float)(v/900),0.f);
                //int v = 3;
                g.setColor(c);
                int r = 20;
               // radius = 10;
                if (il[x][y]) g.fillOval((int)(sp.x-r/2),(int)(sp.y-r/2),r,r);
            }
        }
    }

    private double getValueOfEvador(int x, int y, Collection<AbstractAgent> agents) {
        double minVal = 9999999;
     //   if (x==19&&y==19){
          //  System.out.println(x + " " + y );}
        for(AbstractAgent a:agents){
            if (!a.isEvader()) {
                Vector2D b = a.getLocation();
                Point xy = ScreenPosToXY(b);
                Sink s = getSink(xy);
                if (s.val[x][y]<minVal) minVal = s.val[x][y];
            }
        }
  //      System.out.println(x + " " + y + " " +minVal);
        //double diagonal = 50;
       // int r = (int) (minVal/ diagonal) ;

        return minVal;
    }

    public void prepareSinks() {
        boolean[][] il = getIsLegal();
       // Vector2D[][] pos = getPos();
        int w = il.length;
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < w; y++) {
                //int[] xy = new int[2];
                //xy[0] = x;
                //xy[1] = y;
                Point p = new Point(x,y);
                getSink(p);
            }
        }
        return;
    }

    private class Sink {
        private final int centerX;
        private final int centerY;

        private double[][] val = new double[EngineConstants.preCalcSize][EngineConstants.preCalcSize];

        Vector2D[][] pos = new Vector2D[EngineConstants.preCalcSize][EngineConstants.preCalcSize];
        public Sink(Point pp) {
            centerX = pp.x;
            centerY = pp.y;

            calcDistances();
        }

        private void calcDistances() {
            int w = EngineConstants.preCalcSize;
            Vector2D center = XYToScreenPos(centerX,centerY);

            for (int x = 0; x < w; x++) {
                for (int y = 0; y < w; y++) {
                    Vector2D current = XYToScreenPos(x,y);
                    val[x][y] = center.distance(current);
                    System.out.print(val[x][y]);
                    if (x==19&&y==19&&center.x>=19&&center.y>=19) {
                        System.out.print("eheh");
                    }
                }
            }
            System.out.println();



        }
    }
}
