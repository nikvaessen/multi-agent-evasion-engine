package nl.dke.pursuitevasion.gui;

import nl.dke.pursuitevasion.game.Engine;
import nl.dke.pursuitevasion.game.EngineConstants;
import nl.dke.pursuitevasion.game.Vector2D;
import nl.dke.pursuitevasion.game.agents.AbstractAgent;
import nl.dke.pursuitevasion.game.agents.Direction;
import nl.dke.pursuitevasion.game.agents.impl.MCTS.CoordinatorPursuerKillKillKillEmAll;
import nl.dke.pursuitevasion.game.agents.impl.DistanceAgent;
import nl.dke.pursuitevasion.game.agents.impl.SimpleAgent;
import nl.dke.pursuitevasion.game.agents.impl.TriangulationEvader;
import nl.dke.pursuitevasion.game.agents.impl.UserAgent;
import nl.dke.pursuitevasion.gui.simulator.MapViewPanel;
import nl.dke.pursuitevasion.map.impl.Floor;
import nl.dke.pursuitevasion.map.impl.Map;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by nik on 2/8/17.
 */
public class MainFrame extends JFrame
{
    public MainFrame()
    {
        //Map map = Map.getSimpleMap();
        Map map = Map.getMap("balbul.ser");
        ArrayList<AbstractAgent> agents = new ArrayList<>();
        MapViewPanel panel = new MapViewPanel(map, agents);
        Engine engine = new Engine(map, agents, panel, 60);
        KeyboardInputListener keyboardInputListener = new KeyboardInputListener();

        Floor floor = null;
        for(Floor f : map.getFloors())
        {
            floor = f;
            break;
        }

        ArrayList<ArrayList<Point>> conns = floor.getTriangulation();
        ArrayList<Polygon> triangles = floor.trianglesToDraw;
        ArrayList<Point2D> midpoints = floor.midpoints;

        //agents.add(new SimpleAgent(new Point(5,5), Direction.SOUTH, 5));
       // agents.add(new UserAgent(map, floor, new Vector2D(200, 310), Direction.SOUTH, 5,
       //                          EngineConstants.VISION_RANGE, EngineConstants.VISION_ANGLE, keyboardInputListener,true));

        //get random starting posistion
        int randomStart = (int) Math.abs(Math.random()* ((floor.midpoints.size()-1) ));
        Point2D start = midpoints.get(randomStart);
        agents.add(new TriangulationEvader(map, floor, new Vector2D(start.getX(), start.getY()), Direction.SOUTH, 5,
                        EngineConstants.VISION_RANGE, EngineConstants.VISION_ANGLE));

        //CoordinatorPursuerKillKillKillEmAll hunter = new CoordinatorPursuerKillKillKillEmAll(engine,map, floor, new Vector2D(20, 20), Direction.SOUTH, 5,
          //      EngineConstants.VISION_RANGE, EngineConstants.VISION_ANGLE, agents);
        //agents.add(new DistanceAgent(map, floor, new Vector2D(20,20), Direction.SOUTH, 5, EngineConstants.VISION_RANGE, EngineConstants.VISION_ANGLE));
//        agents.add(new SimpleAgent(map, floor, new Vector2D(15,15), Direction.SOUTH, 5,
//                EngineConstants.VISION_RANGE, EngineConstants.VISION_ANGLE));

       // hunter.setViewPort(panel);
        // agents.add(new UserAgent(map, floor, new Vector2D(20, 20), Direction.SOUTH, 5,
        //     EngineConstants.VISION_RANGE, EngineConstants.VISION_ANGLE, keyboardInputListener,false));
        //   agents.add(new UserAgent(map, floor, new Vector2D(30, 30), Direction.SOUTH, 5,
        //          EngineConstants.VISION_RANGE, EngineConstants.VISION_ANGLE, keyboardInputListener,false));
        //  agents.add(new UserAgent(map, floor, new Vector2D(40, 40), Direction.SOUTH, 5,
        //         EngineConstants.VISION_RANGE, EngineConstants.VISION_ANGLE, keyboardInputListener,false));
//        agents.add(new SimpleAgent(map, floor, new Vector2D(15,15), Direction.SOUTH, 5,
//                EngineConstants.VISION_RANGE, EngineConstants.VISION_ANGLE));

        this.getContentPane().add(panel);
        this.pack();
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.addKeyListener(keyboardInputListener);
        this.setVisible(true);

        engine.start();
    }
}
