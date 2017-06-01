package nl.dke.pursuitevasion.game.agents.impl.MinimalPath;

import nl.dke.pursuitevasion.game.Vector2D;
import nl.dke.pursuitevasion.game.agents.AbstractAgent;
import nl.dke.pursuitevasion.game.agents.AgentRequest;
import nl.dke.pursuitevasion.game.agents.Direction;
import nl.dke.pursuitevasion.map.impl.Floor;
import nl.dke.pursuitevasion.map.impl.Map;

import java.util.Collection;

/**
 * Created by Jan on 24-5-2017.
 */
public class MinimalPathAgent extends AbstractAgent{

    private int agentNumber;

    public MinimalPathOverseer overseer;

    private MinimalPathAgentState state = MinimalPathAgentState.NO_PATH;

    public MinimalPathAgent(Map map, Floor floor, Vector2D startLocation, Direction startsFacing, int radius, double visionRange, double visionAngle){
        super(map, floor, startLocation, startsFacing, radius, visionRange, visionAngle);
        try{
            overseer = MinimalPathOverseer.getIntance();
        }
        catch (NullPointerException e){
            overseer = MinimalPathOverseer.init(map);
        }
        agentNumber = overseer.registerAgent(this);
    }

    public int getAgentNumber(){return agentNumber;}

    @Override
    protected void completeRequest(AgentRequest request) {
        // Aks the static "overseer" what this agent should do
        overseer.getTask(this, request, this.mapInfo);
    }

    @Override
    protected boolean hasNewRequest() {
        // ask the static "overseer" if this agent should do something
        return overseer.getShouldDoSomething(this);
    }

    @Override
    public boolean isEvader() {
        return false;
    }

    public MinimalPathAgentState getState(){return state;}

    public void setState(MinimalPathAgentState newState){state = newState;}

}
