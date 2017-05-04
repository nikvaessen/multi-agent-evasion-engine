package nl.dke.pursuitevasion;

import nl.dke.pursuitevasion.game.EngineConstants;
import nl.dke.pursuitevasion.game.agents.AgentCommand;
import nl.dke.pursuitevasion.game.agents.Direction;
import nl.dke.pursuitevasion.game.agents.impl.SimpleAgent;
import nl.dke.pursuitevasion.game.agents.tasks.WalkToTask;
import nl.dke.pursuitevasion.map.impl.Floor;
import nl.dke.pursuitevasion.map.impl.Map;
import org.junit.Test;

import java.awt.*;

/**
 * Created by nik on 3/29/17.
 */
public class WalkToTaskTest {

    @Test
    public void testAgentCommand()
    {
        Map map = Map.getSimpleMap();
        Floor floor = null;
        Point.Double startLocation = new Point.Double(15, 15);
        Point.Double goalLocation = new Point.Double(500, 500);
        for(Floor f : map.getFloors())
        {
            floor = f;
            break;
        }
        SimpleAgent agent = new SimpleAgent(Map.getSimpleMap(), floor, startLocation, Direction.EAST,
                5, EngineConstants.VISION_RANGE, EngineConstants.VISION_ANGLE);

        WalkToTask t = new WalkToTask(goalLocation);

        double distance = agent.getLocation().distance(goalLocation);
        int desiredFPS = 60;
        double desiredIterationLength = Math.round(1000d / (double) desiredFPS);
        double metersPerIteration = EngineConstants.WALKING_SPEED / desiredIterationLength;
        double rotationPerIteration = EngineConstants.TURNING_SPEED / desiredIterationLength;

        double allowedMeters = metersPerIteration;
        double allowedRotation = rotationPerIteration;

        System.out.printf("distance: %f, allowed: %f, can complete it in 1 go: %b\n",
                distance, allowedMeters, distance < allowedMeters);

        AgentCommand command = t.handle(agent, allowedMeters, allowedRotation);
        System.out.println(command);
    }
}
