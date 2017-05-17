package nl.dke.pursuitevasion.game.agents.impl;

import nl.dke.pursuitevasion.game.Engine;
import nl.dke.pursuitevasion.game.EngineConstants;
import nl.dke.pursuitevasion.game.agents.AbstractAgent;
import nl.dke.pursuitevasion.game.agents.AgentCommand;
import nl.dke.pursuitevasion.game.agents.AgentRequest;
import nl.dke.pursuitevasion.game.agents.Direction;
import nl.dke.pursuitevasion.game.agents.tasks.AbstractAgentTask;
import nl.dke.pursuitevasion.game.agents.tasks.RotateTask;
import nl.dke.pursuitevasion.game.agents.tasks.Task;
import nl.dke.pursuitevasion.game.agents.tasks.WalkForwardTask;
import nl.dke.pursuitevasion.gui.KeyboardInputListener;
import nl.dke.pursuitevasion.gui.simulator.MapViewPanel;
import nl.dke.pursuitevasion.map.impl.Floor;
import nl.dke.pursuitevasion.map.impl.Map;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Jan on 15-5-2017.
 */
public class RandomAgent extends AbstractAgent{

    public RandomAgent(Map map, Floor startingFloor, Point startLocation, Direction startsFacing, int radius, double visionRange, double visionAngle){
        super(map, startingFloor, startLocation, startsFacing, radius, visionRange, visionAngle);
    }

    AgentRequest currentRequest;

    public boolean hasNewRequest(){
        return (currentRequest == null || currentRequest.isCompleted());
    };

    public boolean isEvader(){return true;};

    public void completeRequest(AgentRequest request){
        double angle = ThreadLocalRandom.current().nextDouble(360)/100;
        if(ThreadLocalRandom.current().nextDouble() < 0.5){
            angle = angle * -1;
        }
        double newAngle = this.getFacingAngle() + angle;
        System.out.println(String.format("old angle: %f. new angle: %f", this.getFacingAngle(), newAngle));
        request.add(new RotateTask(newAngle));

        double distance = ThreadLocalRandom.current().nextDouble(EngineConstants.WALKING_SPEED);
        request.add(new WalkForwardTask(distance));


    };

    public static void main(String[] args){
        Map map = Map.getSimpleMap();
        //Map map = Map.getMap("balbul.ser");
        ArrayList<AbstractAgent> agents = new ArrayList<>();
        MapViewPanel panel = new MapViewPanel(map, agents);
        Engine engine = new Engine(map, agents, panel, 60);
        JFrame frame = new JFrame();
        Floor floor = null;
        for(Floor f : map.getFloors())
        {
            floor = f;
            break;
        }

        //agents.add(new SimpleAgent(new Point(5,5), Direction.SOUTH, 5));
        agents.add(new RandomAgent(map, floor, new Point(100,100), Direction.SOUTH, 5, EngineConstants.VISION_RANGE, EngineConstants.VISION_ANGLE));
        /*agents.add(new UserAgent(map, floor, new Point(10, 10), Direction.SOUTH, 5,
                EngineConstants.VISION_RANGE, EngineConstants.VISION_ANGLE, keyboardInputListener));*/

        frame.getContentPane().add(panel);
        frame.pack();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);

        engine.start();
    }



}
