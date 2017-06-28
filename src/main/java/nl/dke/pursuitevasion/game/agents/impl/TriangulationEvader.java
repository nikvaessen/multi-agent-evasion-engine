package nl.dke.pursuitevasion.game.agents.impl;

import nl.dke.pursuitevasion.game.MapInfo;
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
import java.util.LinkedList;

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
        //calculate closest pursuer like in distance agent
        Vector2D currentLocation = getLocation();
        MapInfo info = super.mapInfo;
        LinkedList<Vector2D> agentPoint = new LinkedList<>(info.getAgentPoints());
        double shortestDistance = Double.MAX_VALUE;
        int agentIndex=-1;
        for (int i=0; i<agentPoint.size(); i++){
            double distance = Math.abs(agentPoint.get(i).distance(currentLocation));
            System.out.println("Distance: "+distance);
            if (distance<shortestDistance){
                shortestDistance = distance;
                agentIndex = i;
            }
        }

        //get list of adjacent vertices of our current location
        ArrayList<ArrayList<Point2D>> adjacent =floor.adjacent;
        ArrayList<Point2D> adjacentToCurrentLocation = new ArrayList<>();
        for (int i=0; i<adjacent.size(); i++) {
            if (adjacent.get(i).get(0).equals(new Point((int) currentLocation.getX(), (int) currentLocation.getY()))) {
                adjacentToCurrentLocation = adjacent.get(i);
            }
        }

        //compare the location of that pursuer with the adjacent vertices of current own location
        // take the one with biggest Distance
        double longestDistance = Double.MIN_VALUE;
        int chosenAdjacentVertexIndex = -1;
        for (int i=0; i<adjacentToCurrentLocation.size(); i++){
            Vector2D oneAdjacentVertex = new Vector2D(adjacentToCurrentLocation.get(i).getX(), adjacentToCurrentLocation.get(i).getY());
            double distance = Math.abs(agentPoint.get(agentIndex).distance(oneAdjacentVertex));
            if (distance>longestDistance){
                longestDistance = distance;
                chosenAdjacentVertexIndex = i;
            }
        }

        //set the new walktotask to that point
        for (int i=0; i<adjacent.size(); i++){
            if (adjacent.get(i).get(0).equals(new Point((int) currentLocation.getX(), (int) currentLocation.getY()))){
                newGoal = new Vector2D(adjacentToCurrentLocation.get(chosenAdjacentVertexIndex).getX(), adjacentToCurrentLocation.get(chosenAdjacentVertexIndex).getY());
                finalGoal = new Vector2D(adjacentToCurrentLocation.get(chosenAdjacentVertexIndex).getX(), adjacentToCurrentLocation.get(chosenAdjacentVertexIndex).getY());
            }
            newGoal = finalGoal;

        }
        //newGoal = new Vector2D(adjacentToCurrentLocation.get(chosenAdjacentVertexIndex).getX(), adjacentToCurrentLocation.get(chosenAdjacentVertexIndex).getY());

        //maybe add some more code for the case when you are stuck at one vertex?
        // hen break out of path and randomly walk around?

        //int random = (int) Math.random()*floor.midpoints.size()-1;


        //Point2D randomMidpoint = new Point((int) currentLocation.getX(), (int) currentLocation.getY());


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
