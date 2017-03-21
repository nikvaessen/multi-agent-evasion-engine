package nl.dke.pursuitevasion.gui;

import nl.dke.pursuitevasion.gui.editor.MapEditor;
import nl.dke.pursuitevasion.map.impl.Map;
import nl.dke.pursuitevasion.map.impl.Floor;
import nl.dke.pursuitevasion.map.impl.Obstacle;
import nl.dke.pursuitevasion.map.impl.Gate;

import nl.dke.pursuitevasion.map.AbstractObject;
import nl.dke.pursuitevasion.map.ObjectType;


import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;


/**
 * Created by Nibbla on 11.02.2017.
 *
 * This classed is used to draw the actuall model on the screen.
 */
public class ModelView extends JPanel {

    private final MapEditor mapEditor;

    //variables for the voronoi generation
    public ArrayList<Point> centerPoints = new ArrayList<>();
    public HashMap<Point,Polygon> areas = new HashMap<>(0);
    public HashMap<Polygon,SelectionState> selectionStates = new HashMap<>(0);
    public boolean vronoiGeneration;
    private Dimension dimension;


    private Map map;
    private AffineTransform affineTransform;
    private Point2D lastClickedPoint;
    private java.util.List<EditorObject> objects = new ArrayList<>();
    private EditorObject selectedObject;
    private int LastID = 0;


    public void finalizeVoronoi() {
        vronoiGeneration = false;
        //convert wall and such to real polygones
        //untoggle voronoi mapbilder

        for (Polygon polygon: areas.values()){
            if (selectionStates.get(polygon) == SelectionState.floor){
                //EditorObject ep = new EditorObject();

            }
            if (selectionStates.get(polygon) == SelectionState.waal){
                //EditorObject ep = new EditorObject();

            }



        }

        areas.clear();
        centerPoints.clear();


    }

    public void setAreas(ArrayList<Point> cp, HashMap<Point, Polygon> areas, Dimension d) {
        this.dimension = d;
        centerPoints =  cp;
        this.areas = areas;
        for(Polygon p : areas.values()){
            selectionStates.put(p,SelectionState.empty);
        }
    }

    // everything in the editor is an abstractobject
    // It is the same as the objects in the map, but
    // the type is variable
    private class EditorObject extends AbstractObject{
        public ObjectType type;
        public AbstractObject parentObject;

        public EditorObject(Polygon p, int ID){
            super(p, ID);
            type = ObjectType.OBSTACLE;
        };

        // Error is not correct. just ignore
        @Override
        public ObjectType getType(){return type;}
        public void setType(ObjectType t){type = t;}
        public void setParentObject(AbstractObject o){
            // Check for validity here

        }

    }




    public ModelView(int prefaredWidht, int prefaredHeight, MapEditor mapEditor) {
        this.setPreferredSize(new Dimension(prefaredWidht,prefaredHeight));
        this.affineTransform = new AffineTransform();
        this.mapEditor = mapEditor;

        mouseListenerFunction();
        addKeyboardListener();
        this.addMouseMotionListener(new MouseMotionListener() {


            @Override
            public void mouseDragged(MouseEvent e) {
                if(selectedObject != null){

                }
                System.out.println(e.getPoint());

            }

            @Override
            public void mouseMoved(MouseEvent e) {

            }
        });
    }


    public Map buildMap(){
        List<Floor> floors = new ArrayList<>();
        // Separate floors from objects
        for(EditorObject e : objects){
            if(e.getType() == ObjectType.FLOOR){
                objects.remove(e);
                List<Obstacle> obstacles = getObstacles(e, objects);
                floors.add(new Floor(e.getPolygon(), e.getID(), obstacles, new ArrayList<Gate>()));
            }
        }
        Map m = new Map(floors);
        return m;
    }

    private List<Obstacle> getObstacles(EditorObject floor, List<EditorObject> obstacles){
        List<EditorObject> floorObstacles = new ArrayList<>();
        Polygon floorPolygon = floor.getPolygon();

        for(EditorObject obstacle : obstacles){
            boolean contains = true;
            // check if all point are within the floor
            Polygon obstaclePolygon = obstacle.getPolygon();
            for (int i = 0; i < obstaclePolygon.npoints; i++){
                Point p = new Point(obstaclePolygon.xpoints[i], obstaclePolygon.ypoints[i]);
                if(!floorPolygon.contains(p)){
                    contains = false;
                }
            }
            if(contains){
                floorObstacles.add(obstacle);
            }
        }
        obstacles.removeAll(floorObstacles);
        List<Obstacle> os = new ArrayList<>();
        // Create floor objects
        for(EditorObject e : floorObstacles){
            Obstacle obstacle = new Obstacle(e.getPolygon(), e.getID(), floor.getID());
            os.add(obstacle);
        }
        return os;

    }
    private void addKeyboardListener(){
        System.out.println("adding keyboardlistener");
        Action undoAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("control c pressed");
                if(selectedObject != null){
                    Polygon p = selectedObject.getPolygon();
                    p.npoints -= 1;
                    if(p.npoints <= 0) {
                        objects.remove(selectedObject);
                        selectedObject = null;
                    }
                    else{
                        p.invalidate();}
                    repaint();
                }
            }
        };
        Action MarkFloorAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(selectedObject != null){
                    selectedObject.setType(ObjectType.FLOOR);
                }
            }
        };
        Action MarkObstacleAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(selectedObject != null){
                    selectedObject.setType(ObjectType.OBSTACLE);
                }
            }
        };
        Action BuildMapAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buildMap();
            }
        };

        String UndoActionName = "removeLastPoint";
        String MarkFloorActionName = "MarkFloor";
        String MarkObstaceActionName = "MarkObastacle";
        String BuildMapActionName = "BuildMap";
        InputMap inputs = this.getInputMap();
        ActionMap actions = this.getActionMap();
        KeyStroke z = KeyStroke.getKeyStroke('z');
        KeyStroke f = KeyStroke.getKeyStroke('f');
        KeyStroke o = KeyStroke.getKeyStroke('o');
        KeyStroke b = KeyStroke.getKeyStroke('b');
        inputs.put(z , UndoActionName );
        inputs.put(f, MarkFloorActionName);
        inputs.put(o, MarkObstaceActionName);
        inputs.put(b, BuildMapActionName);
        actions.put(UndoActionName, undoAction);
        actions.put(MarkFloorActionName, MarkFloorAction);
        actions.put(MarkObstaceActionName, MarkObstacleAction);
        actions.put(BuildMapActionName, BuildMapAction);
    }

    private void mouseListenerFunction() {

        System.out.print("adding listener");
        this.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                System.out.print("got mouse event");
                Point click = new Point(e.getX(), e.getY());

                //middle mouse button event
                if (e.getButton() == MouseEvent.BUTTON2) {
                    if (vronoiGeneration == true) {
                        removePointFromVoronoi(e.getPoint());

                    }
                }


                // Right mouse button event
                // Adds point to selected polygon
                if (e.getButton() == MouseEvent.BUTTON3) {
                    if (vronoiGeneration == true){
                        addPointToVoronoi(e.getPoint());
                    }else {


                        System.out.print("button 2");
                        if (selectedObject == null) {
                            Polygon poly = new Polygon();
                            poly.addPoint(click.x, click.y);
                            EditorObject p = new EditorObject(poly, LastID++);
                            selectedObject = p;
                            objects.add(p);
                        }
                        selectedObject.getPolygon().addPoint(click.x, click.y);
                    }
                }
                // Left mouse button event
                // Select polygon
                if (e.getButton() == MouseEvent.BUTTON1) {
                    System.out.print("button 1");
                    selectedObject = null;
                    for (EditorObject o : objects) {
                        // If the click is inside the polygon
                        Polygon p = o.getPolygon();
                        if (p.contains(click) && (selectedObject == null
                                || !p.contains(selectedObject.getPolygon().getBounds()))) {
                            selectedObject = o;
                        }
                    }



                    //check predrawn
                    for (Polygon p : areas.values()){
                        if (p.contains(e.getPoint())){
                            SelectionState selState = selectionStates.get(p);
                           if (selState == SelectionState.empty) selectionStates.put(p,SelectionState.floor);
                           else if (selState == SelectionState.floor)  selectionStates.put(p,SelectionState.waal);
                           else if (selState == SelectionState.waal)  selectionStates.put(p,SelectionState.empty);

                        }
                    }
                }



                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
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

        });
    }




    public void setMap(Map m){
        this.map  = m;

        repaint();
    }

    private void removePointFromVoronoi(Point point) {
        for(Point c : centerPoints){
            if (areas.get(c).contains(point)){
                centerPoints.remove(c);
                //recalc
                HashMap<Point, Polygon> areas = Voronoi.listOfCentresToPolygon(centerPoints, dimension);
                setAreas(centerPoints,areas, dimension);
               repaint();
                break;
            }
        }



    }

    private void addPointToVoronoi(Point point) {
        centerPoints.add(point);
        HashMap<Point, Polygon> areas = Voronoi.listOfCentresToPolygon(centerPoints, dimension);
        setAreas(centerPoints,areas, dimension);
        repaint();
    }
    @Override
    protected void paintComponent(Graphics g1d) {
        super.paintComponent(g1d);

        final ModelView mv = this;
        if(lastClickedPoint == null) lastClickedPoint = new Point(mv.getWidth()/2,mv.getHeight()/2);

        Graphics2D g2d = (Graphics2D) g1d;
        AffineTransform saveXform = g2d.getTransform();
        Stroke saveStroke = g2d.getStroke();
        g2d.setColor(new Color((float)0.8,(float)0.8,(float)0.8));
        g2d.fillRect(0, 0, this.getWidth(), this.getHeight());

        g2d.transform(affineTransform);

        for(AbstractObject o : objects)
        {
            Color c = Color.PINK;

            switch (o.getType()){
                case FLOOR:
                    c = Color.blue; break;
                case OBSTACLE:
                    c = Color.orange; break;
            }
            if(selectedObject == o){
                c = Color.RED;
            }
            g2d.setColor(c);
            g2d.draw(o.getPolygon());
        }

        for(Point p : centerPoints){


            Polygon poly = areas.get(p);
            SelectionState ss = selectionStates.get(poly);
            Stroke oldStrocke = g2d.getStroke();
           // g2d.setStroke(new BasicStroke(2));
            switch (ss) {
                case floor:
                    g2d.setColor(Color.ORANGE);
                    g2d.fillPolygon(poly);
                    g2d.setColor(Color.BLACK);
                    g2d.drawPolygon(poly);
                    break;
                case waal:
                    g2d.setColor(Color.GREEN);
                    g2d.fillPolygon(poly);
                    g2d.setColor(Color.BLACK);
                    g2d.drawPolygon(poly);
                    break;
                case empty:
                    g2d.setColor(Color.BLACK);
                    g2d.drawPolygon(poly);
                    break;
            }

          //  g2d.setColor(new Color((float)Math.random(),(float)Math.random(),(float)Math.random()));
          //  g2d.fillPolygon(poly);
            g2d.setColor(Color.BLACK);
            g2d.fillOval(p.x-2,p.y-2,4,4);
        }
/*
        if (map == null){
            int x = this.getWidth();
            int y = this.getHeight();
            g2d.setStroke(new BasicStroke(5));
            g2d.setColor(Color.cyan);
            g2d.drawLine(x*1/3,y*1/3,x*2/3,y*2/3);

            g2d.setColor(Color.RED);
            g2d.fillOval((int)lastClickedPoint.getX()-5,(int)lastClickedPoint.getY()-5,10,10);
        }*/

        g2d.setTransform(saveXform);
        g2d.setStroke(saveStroke);
    }

    protected void resetView(){
        if (affineTransform == null) return;
        affineTransform.setToIdentity();
    }


    private enum SelectionState{
        floor,waal,empty;

    }
/*


    public Point koordinateToScreenPointNeu(Koordinate k){

        double transPY;
        double transPX;

		*//*Point2D transPoint = getTranslatedPoint(point.getX(), point.getY());
		// transPoint = getTranslatedPointInvers(point.getX(), point.getY());
		double zwischenY =  LOLat - RULat;
		lat = (leBild.getHeight(null) - transPoint.getY()) / leBild.getHeight(null) * zwischenY + RULat;

		double zwischenX =  RULong - LOLong;
		longi = transPoint.getX() / leBild.getWidth(null) * zwischenX + LOLong;


		Koordinate k = new Koordinate(longi,lat);*//*
        double pointx;
        double pointy;
        double longi = k.longitude;
        double lat = k.latitude;


        double zwischenX =  RULong - LOLong;
        pointx = (longi - LOLong) * leBild.getWidth(null) / zwischenX;

        double zwischenY =  LOLat - RULat;
        pointy = -1 * ((lat - RULat) /zwischenY * leBild.getHeight(null)) + this.leBild.getHeight(null);

//		double zwischenX =  RULong - LOLong;
//		double thisW = leBild.getWidth(null);
//		transPX = ((k.longitude - LOLong) * thisW / zwischenX);

//		double zwischenY =  LOLat - RULat;
//		double thisH =  leBild.getHeight(null);
//		transPY = -1*((k.latitude - RULat) * thisH /zwischenY - thisH);

        //Point2D transPoint = getTranslatedPoint(pointx, pointy);
        Point2D transPoint = getTranslatedPointInvers2(pointx,pointy);

        pointx =(transPoint.getX() + currentX) * zoom + this.getWidth()/2;
        pointy = (transPoint.getY() + currentY) * zoom + this.getHeight()/2 ;;

        Point p =  new Point( (int)(pointx),  (int)(pointy));
        System.out.println(p.x + ":::" + p.y + "center" + currentX + "  " + currentY);
        return p;




    }
    public Koordinate getKoordinateFromBildschirmKoordinate(Point point) {
        double lat;
        double longi;

        Point2D transPoint = getTranslatedPoint(point.getX(), point.getY());
        // transPoint = getTranslatedPointInvers(point.getX(), point.getY());
        double th = leBild.getHeight(null) * zoom;
        double thW = this.getWidth();
        double zwischenY =  LOLat - RULat;
        lat = (leBild.getHeight(null) - transPoint.getY()) / leBild.getHeight(null) * zwischenY + RULat;

        double zwischenX =  RULong - LOLong;
        longi = transPoint.getX() / leBild.getWidth(null) * zwischenX + LOLong;


        Koordinate k = new Koordinate(longi,lat);
        return k;

    }

    public Point koordinateToScreenPoint(Koordinate k){
        Point p = new Point();
        double thisH = this.getHeight();


        //Point2D transPoint = getTranslatedPoint(point.getX(), point.getY());

        double zwischenY =  ((k.latitude - RULat) / LatDelt) * this.getHeight();

        p.y = (int) Math.round((thisH - zwischenY));
        double thisW = this.getWidth();
        double zwischenX = ((k.longitude - LOLong) / LongDelt);

        p.x = (int) (Math.round(zwischenX));
        //Point2D transPoint = getTranslatedPoint2(p.getX(), p.getY());
        Point2D transPoint = getTranslatedPointInvers(p.getX(), p.getY());
        p =  new Point( (int)(transPoint.getX()+currentX),  (int)(transPoint.getY()+currentY));
        System.out.println(p.x + ":::" + p.y);
        return p;
        //Return








    }



    private AffineTransform getCurrentTransformMouseDrag() {

        AffineTransform tx = new AffineTransform();

        double centerX = (double)getWidth() / 2;
        double centerY = (double)getHeight() / 2;

        tx.translate(centerX, centerY);

        tx.scale(zoom, zoom);
        tx.translate(currentX, currentY);

        //System.out.println(zoom + " " + currentX + "  " + currentY);
        return tx;

    }

    private AffineTransform getCurrentTransformAnders() {

        AffineTransform tx = new AffineTransform();

        double centerX = leBild.getWidth(null) / 2;
        double centerY = leBild.getHeight(null) / 2;

        tx.translate(centerX, centerY);

        tx.scale(zoom, zoom);
        tx.translate(currentX, currentY);

        //System.out.println(zoom + " " + currentX + "  " + currentY);
        return tx;

    }




    public void mouseDragged(MouseEvent e){
        // Determine the old and new mouse coordinates based on the translated coordinate space.
        Point2D adjPreviousPoint = getTranslatedPoint(previousX, previousY);
        Point2D adjNewPoint = getTranslatedPoint(e.getX(), e.getY());

        double newX = adjNewPoint.getX() - adjPreviousPoint.getX();
        double newY = adjNewPoint.getY() - adjPreviousPoint.getY();

        previousX = e.getX();
        previousY = e.getY();

        currentX += newX;
        currentY += newY;

    }
    private Point2D getTranslatedPoint(double panelX, double panelY) {
        AffineTransform tx = getCurrentTransformMouseDrag();
        Point2D point2d = new Point2D.Double(panelX, panelY);
        try {
            return tx.inverseTransform(point2d, null);

        } catch (NoninvertibleTransformException ex) {
            ex.printStackTrace();
            return null;
        }
    }
    private Point2D getTranslatedPoint2(double panelX, double panelY) {
        AffineTransform tx = getCurrentTransformAnders();
        Point2D point2d = new Point2D.Double(panelX, panelY);
        try {
            return tx.inverseTransform(point2d, null);

        } catch (NoninvertibleTransformException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private Point getTranslatedPointInvers(double panelX, double panelY) {
        AffineTransform tx = getCurrentTransformMouseDrag();

        Point2D point2d = new Point2D.Double(panelX, panelY);

        tx.transform(point2d, null);


        int x = (int) point2d.getX();
        int y = (int) point2d.getY();
        Point p = new Point(x,y);
        return p;

    }
    private Point getTranslatedPointInvers2(double panelX, double panelY) {
        AffineTransform tx = getCurrentTransformAnders();

        Point2D point2d = new Point2D.Double(panelX, panelY);

        tx.transform(point2d, null);


        int x = (int) point2d.getX();
        int y = (int) point2d.getY();
        Point p = new Point(x,y);
        return p;

    }


    public void mousePressed(MouseEvent e){
        previousX = e.getX();
        previousY = e.getY();
    }

    public void zoomAendern(double scrollAmount) {

        zoom += 0.1*scrollAmount*zoom;
        zoom = Math.max(0.00001, zoom);
        //System.out.println("zoom: " + zoom);

    }

    public void setCenter(Point point) {
        Koordinate cent = getKoordinateFromBildschirmKoordinate(point);
        CenterLat = cent.latitude;
        CenterLong = cent.longitude;

    }


    public void paintComponent(Graphics g2) {
        super.paintComponent(g2);
        Graphics2D g = (Graphics2D) g2.create();
        AffineTransform tx = getCurrentTransformMouseDrag();
        g.drawImage(leBild, tx, this);

        ubahn.drawUbahn(g, jahreszahl, auswahl, this);

    }*/
}
