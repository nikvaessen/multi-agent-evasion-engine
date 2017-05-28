package nl.dke.pursuitevasion.gui.editor;

import nl.dke.pursuitevasion.game.EngineConstants;
import nl.dke.pursuitevasion.gui.Voronoi;
import nl.dke.pursuitevasion.map.MapPolygon;
import nl.dke.pursuitevasion.map.impl.*;

import nl.dke.pursuitevasion.map.AbstractObject;
import nl.dke.pursuitevasion.map.ObjectType;
import nl.dke.pursuitevasion.map.impl.Map;


import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
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
    private boolean movePointEnabled;

    private ArrayList<MoveObject> selectedPoints = new ArrayList<>();

    private ArrayList<MoveObject> allPoints = new ArrayList<>();
    private ArrayList<MoveObject> allPointsUndo = new ArrayList<>();
    private Point pressed; //for moving purposes


    public void finalizeVoronoi() {
        vronoiGeneration = false;
        //convert wall and such to real polygones
        //untoggle voronoi mapbilder

        for (Polygon polygon: areas.values()){
            if (selectionStates.get(polygon) == SelectionState.empty)continue;

            EditorObject ep = new EditorObject(polygon,LastID);
            if (selectionStates.get(polygon) == SelectionState.floor){
                ep.setType(ObjectType.FLOOR);
            }
            if (selectionStates.get(polygon) == SelectionState.waal){
                ep.setType(ObjectType.FLOOR);
                objects.add(ep);
                LastID++;
                ep = new EditorObject(polygon,LastID);
                ep.setType(ObjectType.OBSTACLE);
            }
            if (selectionStates.get(polygon) == SelectionState.exit){
                ep.setType(ObjectType.FLOOR);
                objects.add(ep);
                LastID++;
                ep = new EditorObject(polygon,LastID);
                ep.setType(ObjectType.EXIT);
            }
            if (selectionStates.get(polygon) == SelectionState.entry_evader){
                ep.setType(ObjectType.FLOOR);
                objects.add(ep);
                LastID++;
                ep = new EditorObject(polygon,LastID);
                ep.setType(ObjectType.ENTRY_EVADER);
            }
            if (selectionStates.get(polygon) == SelectionState.entry_pursuer){
                ep.setType(ObjectType.FLOOR);
                objects.add(ep);
                LastID++;
                ep = new EditorObject(polygon,LastID);
                ep.setType(ObjectType.ENTRY_PURSUER);
            }

            objects.add(ep);
            LastID++;
        }

        areas.clear();
        centerPoints.clear();
        repaint();

    }

    public void setAreas(ArrayList<Point> cp, HashMap<Point, Polygon> areas, Dimension d) {
        this.dimension = d;
        centerPoints =  cp;
        this.areas = areas;
        for(Polygon p : areas.values()){
            selectionStates.put(p,SelectionState.empty);
        }
    }

    public void setMovePointEnabled(boolean movePointEnabled) {
        this.movePointEnabled = movePointEnabled;
    }

    public void univfy() {
        if (selectedPoints.size() <=1) return;
        int n = 0;
        double x = 0;
        double y = 0;
        for (MoveObject selectedPoint :selectedPoints){
            n++;
            x += selectedPoint.poly.xpoints[selectedPoint.i];
            y += selectedPoint.poly.ypoints[selectedPoint.i];
        }
        x/=n;
        y/=n;

        for (MoveObject selectedPoint :selectedPoints){

            selectedPoint.poly.xpoints[selectedPoint.i] = (int)x;
            selectedPoint.poly.ypoints[selectedPoint.i] = (int)y;
        }
        System.out.println("Unified " + selectedPoints.size() + " Points");
        repaint();
    }

    public void simplyfyAll() {

        //go through all points
        //select all points in a 5 or 10 pixel radius
        //use unify on them
        //repeat for every non selected point
        //store old data
        //redundant point removal is send to converting to map object in the save feature
        Point p = new Point(0,0);
        selectedPoints.clear();
        allPoints.clear();
        allPointsUndo.clear();
        for (EditorObject object : objects) {
            Polygon poly = object.getPolygon();
            for (int i = 0; i < object.getPolygon().npoints; i++) {
                allPoints.add(new MoveObject(poly,i ));
                allPointsUndo.add(new MoveObject(new Polygon(poly.xpoints,poly.ypoints,poly.npoints),i ));
                //new Polygon(selectedTriangle.xpoints,selectedTriangle.ypoints,selectedTriangle.npoints);
            }
        }
        for (int i = 0; i < allPoints.size(); i++) {
            MoveObject movi = allPoints.get(i);
            ArrayList<MoveObject> toRemove = new ArrayList<>();
            selectedPoints.add(movi);
            for (int j = 0; j < allPoints.size(); j++){
                MoveObject movi2 = allPoints.get(j);
                if (movi.equals(movi2))continue;

                double d = (movi.getX()-movi2.getX())*(movi.getX()-movi2.getX()) + (movi.getY()-movi2.getY())*(movi.getY()-movi2.getY());

                if (d<400) { // distance smaller then 20
                    //unify 1 and two
                    selectedPoints.add(movi2);
                    //remove 2 from all points , remember to add all before deunify
                    if (!toRemove.contains(movi2))toRemove.add(movi2);
                }
            }
            univfy();
            selectedPoints.clear();
            for (int j = toRemove.size()-1; j >= 0 ; j--) {
                MoveObject a = toRemove.get(j);
                int  index = allPoints.indexOf(a);
                allPoints.remove(a);
                if (index<=i) i--;
            }


        }




    }

    public void undoSimplyfyAll() {
        allPoints.clear();
        for (EditorObject object : objects) {
            Polygon poly = object.getPolygon();
            for (int i = 0; i < object.getPolygon().npoints; i++) {
                allPoints.add(new MoveObject(poly,i ));
            }
        }
        System.out.println("Tries to undo: " + allPoints.size() + "points" );
        System.out.println("Within: " + objects.size() + "Objects" );
        for (int i = 0; i < allPoints.size(); i++) {
            MoveObject mo = allPoints.get(i);
            MoveObject old = allPointsUndo.get(i);
            System.out.println( mo.poly.xpoints[mo.i] + " " +  old.poly.xpoints[old.i] + " " + mo.poly.ypoints[mo.i] + " " + old.poly.ypoints[old.i]);
            mo.poly.xpoints[mo.i] = old.poly.xpoints[old.i];
            mo.poly.ypoints[mo.i] = old.poly.ypoints[old.i];

            System.out.println( mo.poly.xpoints[mo.i] + " " +  old.poly.xpoints[old.i] + " " + mo.poly.ypoints[mo.i] + " " + old.poly.ypoints[old.i]);
        }

        repaint();

    }

    public void combinePolygones(){
            Area allArea = new Area();
        for (int i = objects.size()-1; i >= 0 ; i--) {
            EditorObject object = objects.get(i);

            if (object.getType() == ObjectType.FLOOR) {
                allArea.add(new Area((Polygon) object.getPolygon()));
                objects.remove(i);
            }
        }


    PathIterator iterator = allArea.getPathIterator(null);
    float[] floats = new float[6];
    Polygon polygon = new Polygon();
    while (!iterator.isDone()) {
        int type = iterator.currentSegment(floats);
        int x = (int) floats[0];
        int y = (int) floats[1];
        if(type != PathIterator.SEG_CLOSE) {
            polygon.addPoint(x, y);
            System.out.println("adding x = " + x + ", y = " + y);
        }
        iterator.next();
    }

        objects.add(0,new EditorObject(polygon,LastID));
        objects.get(0).setType(ObjectType.FLOOR);
        repaint();
    }

    // everything in the editor is an abstractobject
    // It is the same as the objects in the map, but
    // the type is variable
    private class EditorObject extends AbstractObject{
        public ObjectType type;
        public AbstractObject parentObject;

        public EditorObject(Polygon p, int ID){
            super(new MapPolygon(p, false), ID);
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
                if(selectedPoints.size() != 0){
                    for (MoveObject selectedPoint :selectedPoints){

                        selectedPoint.poly.xpoints[selectedPoint.i] +=  e.getX()-pressed.x;
                        selectedPoint.poly.ypoints[selectedPoint.i] += e.getY()-pressed.y;

                        selectedPoint.poly.invalidate();
                    }
                    System.out.println((e.getX()-pressed.x)  +  "  " +   (e.getY()-pressed.y));
                    pressed = e.getPoint();

                    repaint();
                }


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
                List<Obstacle> obstacles = getObstacles(e, objects);
                List<Exit> exits = getExits(e, objects);
                List<EntryEvader> entryEvader = getEntryEvader(e, objects);
                List<EntryPursuer> entryPursuer = getEntryPursuer(e, objects);

                floors.add(new Floor(e.getPolygon(), e.getID(), obstacles, new ArrayList<Gate>(), exits,entryPursuer,entryEvader));
            }
        }
        Map m = new Map(floors);
        Map.saveToFile(m);

        return m;
    }

    private List<EntryPursuer> getEntryPursuer(EditorObject floor, List<EditorObject> objects) {
        List<EditorObject> floorObstacles;
        floorObstacles = getObjectsInsideFloorObject(floor,objects);
        List<EntryPursuer> os = new ArrayList<>();
        // Create floor objects
        for(EditorObject e : floorObstacles){
            if (e.getType()!= ObjectType.ENTRY_PURSUER) continue;
            EntryPursuer ep = new EntryPursuer(e.getPolygon(), e.getID(), floor.getID());
            os.add(ep);
        }
        return os;
    }

    private List<EntryEvader> getEntryEvader(EditorObject floor, List<EditorObject> objects) {
        List<EditorObject> floorObstacles;
        floorObstacles = getObjectsInsideFloorObject(floor,objects);
        List<EntryEvader> os = new ArrayList<>();
        // Create floor objects
        for(EditorObject e : floorObstacles){
            if (e.getType()!= ObjectType.ENTRY_EVADER) continue;
            EntryEvader obstacle = new EntryEvader(e.getPolygon(), e.getID(), floor.getID());
            os.add(obstacle);
        }
        return os;
    }
    private List<Exit> getExits(EditorObject floor, List<EditorObject> objects){
        List<EditorObject> floorObstacles;
        floorObstacles = getObjectsInsideFloorObject(floor,objects);
        List<Exit> os = new ArrayList<>();
        // Create floor objects
        for(EditorObject e : floorObstacles){
            if (e.getType()!= ObjectType.EXIT) continue;
            Exit obstacle = new Exit(e.getPolygon(), e.getID(), floor.getID());
            os.add(obstacle);
        }
        return os;

    }
    private List<Obstacle> getObstacles(EditorObject floor, List<EditorObject> obstacles){
        List<EditorObject> floorObstacles;
        floorObstacles = getObjectsInsideFloorObject(floor,obstacles);
        List<Obstacle> os = new ArrayList<>();
        // Create floor objects
        for(EditorObject e : floorObstacles){
            if (e.getType()!= ObjectType.OBSTACLE) continue;
            Obstacle obstacle = new Obstacle(e.getPolygon(), e.getID(), floor.getID());
            os.add(obstacle);
        }
        return os;

    }



    private  List<EditorObject> getObjectsInsideFloorObject(EditorObject floor, List<EditorObject> obstacles) {
        List<EditorObject> floorObstacles = new ArrayList<>();
        Polygon floorPolygon = floor.getPolygon();

        for(EditorObject obstacle : obstacles){
            if (floor == obstacle) continue;
            boolean contains = true;
            // check if all point are within the floor
            Polygon obstaclePolygon = obstacle.getPolygon();
            for (int i = 0; i < obstaclePolygon.npoints; i++){
                Point p = new Point(obstaclePolygon.xpoints[i], obstaclePolygon.ypoints[i]);
                if(!floor.contains(p)){
                    contains = false;
                }
            }
            if(contains){
                floorObstacles.add(obstacle);
            }
        }
        return floorObstacles;
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
                System.out.println(" " + click);
                //middle mouse button event
                if (e.getButton() == MouseEvent.BUTTON2) {
                    if (vronoiGeneration == true) {
                        removePointFromVoronoi(e.getPoint());

                    }

                    if (selectedObject != null) {
                        if (selectedObject.getPolygon().contains(e.getPoint())) {
                            objects.remove(selectedObject);
                            selectedObject = null;
                        }

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
                            p.setType(ObjectType.FLOOR);
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


                    if (movePointEnabled){
                        selectPolygonPoints(e);
                    }else {
                        if (selectedObject != null) {
                            if (selectedObject.getPolygon().contains(e.getPoint())) {
                                if (selectedObject.getType()==ObjectType.FLOOR) selectedObject.setType(ObjectType.OBSTACLE);
                                else if (selectedObject.getType()==ObjectType.OBSTACLE) selectedObject.setType(ObjectType.FLOOR);
                               // objects.remove(selectedObject);

                            }
                        }

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
                            for (Polygon p : areas.values()) {
                                if (p.contains(e.getPoint())) {
                                    SelectionState selState = selectionStates.get(p);
                                    if (selState == SelectionState.empty) selectionStates.put(p, SelectionState.floor);
                                    else if (selState == SelectionState.floor)
                                        selectionStates.put(p, SelectionState.waal);
                                    else if (selState == SelectionState.waal)
                                        selectionStates.put(p, SelectionState.entry_evader);
                                    else if (selState == SelectionState.entry_evader)
                                        selectionStates.put(p, SelectionState.entry_pursuer);
                                    else if (selState == SelectionState.entry_pursuer)
                                        selectionStates.put(p, SelectionState.exit);
                                    else if (selState == SelectionState.exit)
                                        selectionStates.put(p, SelectionState.empty);

                                }
                            }

                    }
                }



                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (selectedPoints.size() != 0){
                    pressed = e.getPoint();

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

        });
    }

    private void selectPolygonPoints(MouseEvent e) {
        if (!e.isShiftDown()) selectedPoints.clear();
        if (e.getClickCount() == 2 && !e.isConsumed()) {
            e.consume();
            System.out.println("Double Click");
            for (EditorObject object : objects) {
                Polygon poly = object.getPolygon();

                if (poly.contains(e.getPoint())){
                    for (int i = 0; i < object.getPolygon().npoints; i++) {
                        selectedPoints.add(new MoveObject(poly,i ));
                    }


                }
            }
            return;
        }

        Point mp = e.getPoint();
        for (EditorObject object : objects){
            Polygon poly = object.getPolygon();

            for (int i = 0; i < object.getPolygon().npoints; i++) {
                if (sqaredDistanceBetweenTwoPoints(mp,poly.xpoints[i],poly.ypoints[i])<64){ // distance smaller then 20
                    MoveObject moveObject = new MoveObject(poly,i );
                    if (selectedPoints.contains(moveObject)) selectedPoints.remove(moveObject);
                        else selectedPoints.add(moveObject);
                }

            }

        }


    }

    private double sqaredDistanceBetweenTwoPoints(Point mp, int xpoint, int ypoint) {
         double d = (mp.x-xpoint)*(mp.x-xpoint) + (mp.y-ypoint)*(mp.y-ypoint);
        return d;
    }


    public void setMap(Map m){


        this.map  = m;
        objects.clear();
        selectedObject = null;
        LastID = 0;
        selectedPoints.clear();


        for (Floor f : m.getFloors()){
            EditorObject eo = new EditorObject(f.getPolygon(),LastID++);
            eo.setType(ObjectType.FLOOR);
           objects.add(eo);


            for (Obstacle o : f.getObstacles()){
                EditorObject eo2 = new EditorObject(o.getPolygon(),LastID++);
                eo2.setType(ObjectType.OBSTACLE);
                objects.add(eo2);
            };


            for (Gate g : f.getGates()){
                EditorObject eo2 = new EditorObject(g.getPolygon(),LastID++);
                eo2.setType(ObjectType.GATE);
                objects.add(eo2);
            };

            for (AbstractObject g : f.getExit()){
                EditorObject eo2 = new EditorObject(g.getPolygon(),LastID++);
                eo2.setType(ObjectType.EXIT);
                objects.add(eo2);
            };

            for (AbstractObject g : f.getEntryPursuer()){
                EditorObject eo2 = new EditorObject(g.getPolygon(),LastID++);
                eo2.setType(ObjectType.ENTRY_PURSUER);
                objects.add(eo2);
            };
            for (AbstractObject g : f.getEntryEvader()){
                EditorObject eo2 = new EditorObject(g.getPolygon(),LastID++);
                eo2.setType(ObjectType.ENTRY_EVADER);
                objects.add(eo2);
            };


        }


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
                    c = EngineConstants.FLOOR_ALT_COLOR; break;

                case OBSTACLE:
                    c = EngineConstants.OBSTACLE_ALT_COLOR; break;
                case EXIT:
                    c = EngineConstants.EXIT_COLOR; break;
                case ENTRY_EVADER:
                    c = EngineConstants.ENTRY_EVADER_COLOR; break;
                case ENTRY_PURSUER:
                    c = EngineConstants.ENTRY_PURSUER_COLOR; break;

            }

            g2d.setColor(c);
            g2d.fill(o.getPolygon());
            if(selectedObject == o){
                //c = Color.RED;
                c = new Color(1f,0,0,0.6f);
                g2d.setColor(c);
                g2d.fill(o.getPolygon());
            }

            System.out.println("Painting: " + o.getType());
            g2d.setColor(Color.BLACK);
            g2d.draw(o.getPolygon());
        }

        for(Point p : centerPoints){


            Polygon poly = areas.get(p);
            SelectionState ss = selectionStates.get(poly);
            Stroke oldStrocke = g2d.getStroke();
           // g2d.setStroke(new BasicStroke(2));
            switch (ss) {
                case floor:
                    g2d.setColor(EngineConstants.FLOOR_ALT_COLOR);
                    g2d.fillPolygon(poly);
                    g2d.setColor(Color.BLACK);
                    g2d.drawPolygon(poly);
                    break;
                case waal:
                    g2d.setColor(EngineConstants.OBSTACLE_ALT_COLOR);
                    g2d.fillPolygon(poly);
                    g2d.setColor(Color.BLACK);
                    g2d.drawPolygon(poly);
                    break;
                case empty:
                    g2d.setColor(Color.BLACK);
                    g2d.drawPolygon(poly);
                    break;
                case entry_evader:
                    g2d.setColor(EngineConstants.ENTRY_EVADER_COLOR);
                    g2d.fillPolygon(poly);
                    g2d.setColor(Color.BLACK);
                    g2d.drawPolygon(poly);
                    break;
                case entry_pursuer:
                    g2d.setColor(EngineConstants.ENTRY_PURSUER_COLOR);
                    g2d.fillPolygon(poly);
                    g2d.setColor(Color.BLACK);
                    g2d.drawPolygon(poly);
                    break;
                case exit:
                    g2d.setColor(EngineConstants.EXIT_COLOR);
                    g2d.fillPolygon(poly);
                    g2d.setColor(Color.BLACK);
                    g2d.drawPolygon(poly);
                    break;
            }

          //  g2d.setTurn(new Color((float)Math.random(),(float)Math.random(),(float)Math.random()));
          //  g2d.fillPolygon(poly);
            g2d.setColor(Color.BLACK);
            g2d.fillOval(p.x-2,p.y-2,4,4);
        }

        g2d.setStroke(new BasicStroke(2));
        for(MoveObject mo : selectedPoints){
            int x = mo.poly.xpoints[mo.i];
            int y = mo.poly.ypoints[mo.i];

            g2d.setColor(Color.BLACK);
            g2d.drawOval(x-8,y-8,16,16);

        }



/*
        if (map == null){
            int x = this.getWidth();
            int y = this.getHeight();
            g2d.setStroke(new BasicStroke(5));
            g2d.setTurn(Color.cyan);
            g2d.drawLine(x*1/3,y*1/3,x*2/3,y*2/3);

            g2d.setTurn(Color.RED);
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
        floor,waal,empty, exit,  entry_pursuer, entry_evader;

    }

    private class MoveObject {
        Polygon poly; int i;
        public MoveObject(Polygon poly, int i) {
            this.poly = poly;
            this.i = i;
        }
        public int getX(){
            return poly.xpoints[i];
        }
        public int getY(){
            return poly.ypoints[i];
        }

        public boolean equals(Object o){
            if (!(o instanceof MoveObject))return false;
            MoveObject object = (MoveObject) o;
            if (object.poly == poly && object.i == i) return true;
            return false;

        }
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
