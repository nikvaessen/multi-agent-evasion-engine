package nl.dke.pursuitevasion.gui;

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

    private Map map;
    private AffineTransform affineTransform;
    private Point2D lastClickedPoint;
    private java.util.List<EditorObject> objects = new ArrayList<>();
    private EditorObject selectedObject;
    private int LastID = 0;


    public ModelView(int prefaredWidht, int prefaredHeight) {
        this.setPreferredSize(new Dimension(prefaredWidht,prefaredHeight));
        this.affineTransform = new AffineTransform();

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
                // Right mouse button event
                // Adds point to selected polygon
                if (e.getButton() == MouseEvent.BUTTON3) {
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


    @Override
    protected void paintComponent(Graphics g1d) {
        super.paintComponent(g1d);

        final ModelView mv = this;
        if(lastClickedPoint == null) lastClickedPoint = new Point(mv.getWidth()/2,mv.getHeight()/2);

        Graphics2D g2d = (Graphics2D) g1d;
        AffineTransform saveXform = g2d.getTransform();
        Stroke saveStroke = g2d.getStroke();

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

}
