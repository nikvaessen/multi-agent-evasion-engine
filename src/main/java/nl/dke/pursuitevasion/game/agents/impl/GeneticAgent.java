package nl.dke.pursuitevasion.game.agents.impl;

import nl.dke.pursuitevasion.game.agents.AbstractAgent;
import nl.dke.pursuitevasion.game.agents.AgentRequest;
import nl.dke.pursuitevasion.game.agents.Direction;
import nl.dke.pursuitevasion.map.impl.Floor;
import nl.dke.pursuitevasion.map.impl.Map;

import java.awt.*;

/** One single Genetic Agent.
 *  Its behaviour is defined by its genetic code, wich will according to the time
 *  do a different action
 *
 * Created by nibbla on 13.04.17.
 */
public class GeneticAgent extends AbstractAgent{
    String code = null;
    long tick = 0;

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
    public GeneticAgent(Map map, Floor startingFloor, Point startLocation, Direction startsFacing, int radius, double visionRange, double visionAngle, String code) {
        super(map, startingFloor, startLocation, startsFacing, radius, visionRange, visionAngle);
        this.code = code;
    }



    @Override
    protected void completeRequest(AgentRequest request) {

    }

    @Override
    protected boolean hasNewRequest() {
        return false;
    }

    @Override
    public boolean isEvader() {
        return false;
    }
}
