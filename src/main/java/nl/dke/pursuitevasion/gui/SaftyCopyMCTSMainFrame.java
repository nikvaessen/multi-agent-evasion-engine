package nl.dke.pursuitevasion.gui;

import nl.dke.pursuitevasion.game.Engine;
import nl.dke.pursuitevasion.game.EngineConstants;
import nl.dke.pursuitevasion.game.Vector2D;
import nl.dke.pursuitevasion.game.agents.AbstractAgent;
import nl.dke.pursuitevasion.game.agents.Direction;
import nl.dke.pursuitevasion.game.agents.impl.MCTS.CoordinatorPursuerKillKillKillEmAll;
import nl.dke.pursuitevasion.game.agents.impl.UserAgent;
import nl.dke.pursuitevasion.gui.simulator.MapViewPanel;
import nl.dke.pursuitevasion.map.impl.Floor;
import nl.dke.pursuitevasion.map.impl.Map;

import javax.swing.*;
import java.util.ArrayList;

/**
 * Created by nik on 2/8/17.
 */
public class SaftyCopyMCTSMainFrame extends JFrame
{
    public SaftyCopyMCTSMainFrame()
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

        //agents.add(new SimpleAgent(new Point(5,5), Direction.SOUTH, 5));
        agents.add(new UserAgent(map, floor, new Vector2D(600, 600), Direction.SOUTH, 5,
                                 EngineConstants.VISION_RANGE, EngineConstants.VISION_ANGLE, keyboardInputListener,true));
        CoordinatorPursuerKillKillKillEmAll hunter = new CoordinatorPursuerKillKillKillEmAll(engine,map, floor, new Vector2D(20, 20), Direction.SOUTH, 5,
                EngineConstants.VISION_RANGE, EngineConstants.VISION_ANGLE, agents);
        hunter.setViewPort(panel);
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
