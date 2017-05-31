package nl.dke.pursuitevasion.game;

import java.util.LinkedList;

/**
 * Created by Carla on 26/05/2017.
 */
public class MapInfo {

    private LinkedList<Vector2D> agentPoints = new LinkedList<>();

    public  MapInfo(LinkedList<Vector2D> agentPoints){
        this.agentPoints = agentPoints;
    }

    public LinkedList<Vector2D> getAgentPoints()
    {
        return agentPoints;
    }
}
