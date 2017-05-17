package nl.dke.pursuitevasion.game.agents.tasks;

import nl.dke.pursuitevasion.game.Vector2D;
import nl.dke.pursuitevasion.game.agents.AbstractAgent;
import nl.dke.pursuitevasion.game.agents.AgentCommand;
import nl.dke.pursuitevasion.game.agents.Angle;

import java.awt.*;

/**
 * Created by Jan on 16-5-2017.
 */
public class WalkForwardTask extends AbstractAgentTask {

    double walkDistance;

    public WalkForwardTask(double distance){
        super();
        walkDistance = distance;
    }

    @Override
    protected AgentCommand computeAgentCommand(AbstractAgent agent, double maxDistance, double maxRotation) {
        double angle = agent.getFacingAngle();
        angle = Math.toRadians(angle);
        double opposite = -Math.sin(angle) * walkDistance;
        double adjacent = Math.cos(angle) * walkDistance;

        Vector2D location = agent.getLocation();
        Vector2D newLocation = location.add(adjacent, opposite);

        return new AgentCommand(agent, newLocation);
    }

    @Override
    protected boolean completesTask(AgentCommand command) {
        return true;
    }
}
