package nl.dke.pursuitevasion;

import nl.dke.pursuitevasion.game.Engine;
import nl.dke.pursuitevasion.game.EngineConstants;
import nl.dke.pursuitevasion.game.Vector2D;
import nl.dke.pursuitevasion.game.agents.AbstractAgent;
import nl.dke.pursuitevasion.game.agents.Direction;
import nl.dke.pursuitevasion.game.agents.impl.minimalPath.MinimalPathOverseer;
import nl.dke.pursuitevasion.game.agents.impl.UserAgent;
import nl.dke.pursuitevasion.gui.KeyboardInputListener;
import nl.dke.pursuitevasion.gui.simulator.MapViewPanel;
import nl.dke.pursuitevasion.map.impl.Floor;
import nl.dke.pursuitevasion.map.impl.Map;
import org.jgrapht.*;
import org.jgrapht.graph.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * Created by Jan on 24-5-2017.
 */
public class MinimalPathTest {

    public static void main(String[] args) {
        // do init stuff
//        Map map = Map.getMap("MinPathTest.ser");
        Map map = Map.getSimpleMap();
        //Map map = Map.getMap("simpleMap.ser");
        //Map map = Map.getMap("NikMap.ser");
        //Map map = Map.getMap("newMap.ser");
        ArrayList<AbstractAgent> agents = new ArrayList<>();
        JFrame frame = new JFrame();
        Floor floor = map.getFloors().iterator().next();

        MapViewPanel panel = new MapViewPanel(map, agents);
        KeyboardInputListener l = new KeyboardInputListener();
        frame.addKeyListener(l);

        MinimalPathOverseer overseer = new MinimalPathOverseer(map, new Vector2D(10, 10));
        panel.setMinimalPathOverseer(overseer);
        for(int i = 0 ; i < overseer.getAmountOfAgents(); i++)
        {
            agents.add(overseer.getAgent(i));
        }
        //agents.add(new RandomAgent(map, floor, new Vector2D(200.0, 100.0), Direction.NORTH, 5, 100, 120));
        agents.add(new UserAgent(map, floor, new Vector2D(200.0, 500.0), Direction.NORTH, 5, EngineConstants.VISION_RANGE,  EngineConstants.VISION_ANGLE, l, true));

        Engine simulationEngine = new Engine(map, agents, panel, 60);

        frame.getContentPane().add(panel);
        frame.pack();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
        simulationEngine.start();

        //drawFrame(frame, map, visibilityGraph, null);
    }

    private static void drawGraph(JFrame frame, Floor floor, Graph<Vector2D, DefaultWeightedEdge> vgraph){
        Graphics g = frame.getGraphics();
        g.drawPolygon(floor.getPolygon());
        for (DefaultWeightedEdge defaultWeightedEdge : vgraph.edgeSet()) {
            Vector2D start = vgraph.getEdgeSource(defaultWeightedEdge);
            Vector2D end = vgraph.getEdgeTarget(defaultWeightedEdge);
            g.drawLine((int)Math.round(start.getX()), (int)Math.round(start.getY()),
                    (int)Math.round(end.getX()), (int)Math.round(end.getY()));
        }
    }
}
/*
class TestPanel extends JPanel{

    Floor floor;
    GraphPath<Vector2D, DefaultWeightedEdge> path;


    TestPanel(Floor f, GraphPath<Vector2D, DefaultWeightedEdge> g){
        floor = f;
        path = g;

    }

    @Override
    public void paintComponent(Graphics g){
        g.setColor(EngineConstants.FLOOR_COLOR);
        g.fillPolygon(floor.getPolygon());
        g.setColor(Color.BLACK);
        g.drawPolygon(floor.getPolygon());

        g.setColor(Color.green);
        List<Vector2D> points = path.getVertexList();
        Vector2D lastPoint = points.get(0);
        for (int i = 1; i < points.size(); i++) {
            Vector2D newPoint = points.get(i);
            g.drawLine((int)Math.round(lastPoint.getX()), (int)Math.round(lastPoint.getY()),
                    (int)Math.round(newPoint.getX()), (int)Math.round(newPoint.getY()));
            lastPoint = newPoint;
        }
        Vector2D u = path.getStartVertex();
        Vector2D v = path.getEndVertex();
        g.drawOval((int)Math.round(u.getX()), (int)Math.round(u.getY()), 5, 5);
        g.drawOval((int)Math.round(v.getX()), (int)Math.round(v.getY()), 5, 5);

    }
}*/

