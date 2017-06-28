package nl.dke.pursuitevasion.game.agents.impl;

import nl.dke.pursuitevasion.game.Vector2D;
import nl.dke.pursuitevasion.game.agents.AbstractAgent;
import nl.dke.pursuitevasion.game.agents.AgentRequest;
import nl.dke.pursuitevasion.game.agents.Direction;
import nl.dke.pursuitevasion.game.agents.tasks.WalkForwardTask;
import nl.dke.pursuitevasion.game.agents.tasks.WalkToTask;
import nl.dke.pursuitevasion.map.impl.Floor;
import nl.dke.pursuitevasion.map.impl.Map;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;

/**
 * Created by Carla on 28/06/2017.
 */
public class TriangulationEvader extends AbstractAgent
{
    /**
     * Create an agent in the given Map
     *
     * @param map           the map where the agent is going to be interacting in
     * @param startingFloor the Floor (which is in the Map) the agent will be placed on
     * @param startLocation the location on the given floor the agent will be put on
     * @param startsFacing  the direction the agent will start facing in
     * @param radius        the radius of the agent
     * @param visionRange
     * @param visionAngle
     */
    public TriangulationEvader(Map map, Floor startingFloor, Vector2D startLocation, Direction startsFacing, int radius, double visionRange, double visionAngle)
    {
        super(map, startingFloor, startLocation, startsFacing, radius, visionRange, visionAngle);
    }

    private AgentRequest currentRequest;
    private Vector2D newGoal;
    private Vector2D finalGoal;

    @Override
    protected void completeRequest(AgentRequest request)
    {
        //int random = (int) Math.random()*floor.midpoints.size()-1;
        Vector2D currentLocation = getLocation();
        ArrayList<ArrayList<Point2D>> adjacent =floor.adjacent;
        Point2D randomMidpoint = new Point((int) currentLocation.getX(), (int) currentLocation.getY());

        for (int i=0; i<adjacent.size(); i++){
            System.out.println("I'm so here");
            if (adjacent.get(i).get(0).equals(new Point((int) currentLocation.getX(), (int) currentLocation.getY()))){
                int random = (int) Math.abs(Math.random()* ((adjacent.get(i).size()-1) ));
                System.out.println("random"+random);
                randomMidpoint = adjacent.get(i).get(random);
                newGoal = new Vector2D(randomMidpoint.getX(), randomMidpoint.getY());
                finalGoal = new Vector2D(randomMidpoint.getX(), randomMidpoint.getY());
            }
                newGoal = finalGoal;

        }
    /*    if (!getLocation().equals(finalGoal)){
            request.add(new WalkToTask(newGoal));
        } else {
            //newGoal = new Vector2D(randomMidpoint.getX(), randomMidpoint.getY());
            request.add(new WalkToTask(finalGoal));
        }*/
        request.add(new WalkToTask(newGoal));
        currentRequest = request;
    }

    @Override
    protected boolean hasNewRequest()
    {
        //if (newGoal!=null && location.distance(newGoal)<6)return true;
        return (currentRequest == null || currentRequest.isCompleted());
    }

    @Override
    public boolean isEvader()
    {
        return true;
    }
}
