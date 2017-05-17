package nl.dke.pursuitevasion;

import nl.dke.pursuitevasion.game.Vector2D;
import nl.dke.pursuitevasion.game.agents.AbstractAgent;
import nl.dke.pursuitevasion.game.agents.Direction;
import nl.dke.pursuitevasion.game.agents.impl.SimpleAgent;
import nl.dke.pursuitevasion.map.impl.Floor;
import nl.dke.pursuitevasion.map.impl.Map;
import org.junit.Test;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;


/**
 * Created by Jan on 2-5-2017.
 */
public class VisionArcTest {


    private Map map = Map.getSimpleMap();
    private Collection<Floor> floors = map.getFloors();
    private Floor floor = floors.iterator().next();
    @Test
    public void testInRange(){
        org.junit.Assert.assertTrue(!floors.isEmpty());
        //create simple agent
        AbstractAgent agent = new SimpleAgent(map, floor, new Vector2D(10, 10), Direction.SOUTH,
                5, 30, 120);
        // create agent in vision range of the agent
        AbstractAgent inRangeAgent = new SimpleAgent(map, floor, new Vector2D(10, 20), Direction.SOUTH, 5, 30, 120);
        // create agent not in vision range
        AbstractAgent notInRangeAgent = new SimpleAgent(map, floor, new Vector2D(5,5), Direction.NORTH,
                5,30,120);
        Collection<AbstractAgent> agents = new ArrayList<>();
        agents.add(inRangeAgent);
        agents.add(notInRangeAgent);
        agent.getVisionArc().update(agents);
        Collection<AbstractAgent> visAgents = agent.getVisibleAgents();
        org.junit.Assert.assertTrue("In arc agent should be visible", visAgents.contains(inRangeAgent));
        org.junit.Assert.assertFalse("out of Arc agent should not be visible", visAgents.contains(notInRangeAgent));
    }

    @Test
    public void TestObstructed(){

        // create agent with infinite vision
        AbstractAgent agent = new SimpleAgent(map, floor, new Vector2D(10, 10), Direction.SOUTH,
                5, 30, 120);
        // create agent behind obstacle
        AbstractAgent hiddenAgent;

        Collection<AbstractAgent> agents = new ArrayList<>();
        //agents.add(hiddenAgent);
        // update visionArc
        agent.getVisionArc().update(agents);
        // check if hidden agent is actually hidden.
        //org.junit.Assert.assertFalse("Agent should be hidden", agent.getVisibleAgents().contains(hiddenAgent));
    }

}
