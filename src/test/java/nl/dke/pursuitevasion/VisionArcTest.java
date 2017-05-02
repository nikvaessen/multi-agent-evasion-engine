package nl.dke.pursuitevasion;

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

    @Test
    public void testInRange(){
        Map map = Map.getSimpleMap();
        Collection<Floor> floors = map.getFloors();
        org.junit.Assert.assertTrue(!floors.isEmpty());
        Floor floor = floors.iterator().next();
        //create simple agent
        AbstractAgent agent = new SimpleAgent(map, floor, new Point(10, 10), Direction.SOUTH,
                5, 30, 120);
        // create agent in vision range of the agent
        AbstractAgent inRangeAgent = new SimpleAgent(map, floor, new Point(10, 20), Direction.SOUTH, 5, 30, 120);
        // create agent not in vision range
        AbstractAgent notInRangeAgent = new SimpleAgent(map, floor, new Point(5,5), Direction.NORTH,
                5,30,120);
        Collection<AbstractAgent> agents = new ArrayList<>();
        agents.add(inRangeAgent);
        agents.add(notInRangeAgent);
        agent.getVisionArc().update(agents);
        Collection<AbstractAgent> visAgents = agent.getVisibleAgents();
        org.junit.Assert.assertTrue(visAgents.contains(inRangeAgent));
        org.junit.Assert.assertTrue(!visAgents.contains(notInRangeAgent));
    }

}
