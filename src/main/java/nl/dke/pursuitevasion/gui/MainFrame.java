package nl.dke.pursuitevasion.gui;

import nl.dke.pursuitevasion.game.Engine;
import nl.dke.pursuitevasion.game.agents.AbstractAgent;
import nl.dke.pursuitevasion.game.agents.Direction;
import nl.dke.pursuitevasion.game.agents.impl.UserAgent;
import nl.dke.pursuitevasion.gui.simulator.MapViewPanel;
import nl.dke.pursuitevasion.map.impl.Floor;
import nl.dke.pursuitevasion.map.impl.Map;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by nik on 2/8/17.
 */
public class MainFrame extends JFrame
{
    public MainFrame()
    {
        Map map = Map.getSimpleMap();
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
        agents.add(new UserAgent(map, floor, new Point(10, 10), Direction.SOUTH, 5,
                0, 0, keyboardInputListener));

        this.getContentPane().add(panel);
        this.pack();
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.addKeyListener(keyboardInputListener);
        this.setVisible(true);

        engine.start();
    }
}
