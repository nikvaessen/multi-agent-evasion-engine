package nl.dke.pursuitevasion.game.agents.impl;

import nl.dke.pursuitevasion.game.Engine;
import nl.dke.pursuitevasion.game.EngineConstants;
import nl.dke.pursuitevasion.game.Vector2D;
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



    public RandomAgent(Map map, Floor startingFloor, Vector2D startLocation, Direction startsFacing, int radius, double visionRange, double visionAngle){
        super(map, startingFloor, startLocation, startsFacing, radius, visionRange, visionAngle);
    }

    private AgentRequest currentRequest;

    public boolean hasNewRequest(){
        return (currentRequest == null || currentRequest.isCompleted());
    };

    public boolean isEvader(){return true;};

    public void completeRequest(AgentRequest request){
        if(ThreadLocalRandom.current().nextDouble() < 0.01){
            double angle = ThreadLocalRandom.current().nextDouble(360);
            if(ThreadLocalRandom.current().nextDouble() < 0.5){
                angle = angle * -1;
            }
            double newAngle = this.getFacingAngle() + angle;
            request.add(new RotateTask(newAngle));
        }

        double scale = ThreadLocalRandom.current().nextDouble();
        request.add(new WalkForwardTask(scale));
        currentRequest = request;
    };

}
