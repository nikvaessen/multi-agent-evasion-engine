package nl.dke.pursuitevasion.game.agents.tasks;

import nl.dke.pursuitevasion.game.agents.AbstractAgent;
import nl.dke.pursuitevasion.game.agents.AgentCommand;
import nl.dke.pursuitevasion.game.agents.Angle;

/**
 * Created by nik on 03/03/17.
 */
public class RotateTask
    extends AbstractAgentTask
{

    private double rotateToAngle;
    private boolean clockwise;
    private boolean shortest;

    public RotateTask(double rotateToAngle, boolean clockwise)
    {
        this.rotateToAngle = rotateToAngle;
        this.clockwise = clockwise;
    }

    public RotateTask(double rotateToAngle)
    {
        this.rotateToAngle = rotateToAngle;
        this.shortest = true;
    }

    /**
     * Compute the AgentCommand based on the allowed distance and rotation travel
     *
     * @param agent       The agent on which the AgentCommand will be applied
     * @param maxDistance the distance allowed to travel
     * @param maxRotation the rotation allowed to spin
     * @return the AgentCommand which will be able to (partially) complete this task
     */
    @Override
    protected AgentCommand computeAgentCommand(AbstractAgent agent, double maxDistance, double maxRotation)
    {
        double agentAngle = agent.getFacingAngle();
        if(shortest)
        {
            double clockwiseDistance = (agentAngle - rotateToAngle) % 360;
            double antiClockwiseDistance = (rotateToAngle - agentAngle) % 360;
            if(clockwiseDistance > antiClockwiseDistance)
            {
                return new AgentCommand(agent, new Angle((agentAngle - clockwiseDistance) % 360));
            }
            else
            {
                return new AgentCommand(agent, new Angle((agentAngle + antiClockwiseDistance) % 360));
            }
        }

        double angleDistance = clockwise ?
                (agentAngle - rotateToAngle) % 360:
                (rotateToAngle - agentAngle) % 360;

        if(maxRotation >= angleDistance)
        {
            return new AgentCommand(agent, new Angle(rotateToAngle));
        }
        else
        {
            double newAngle = clockwise ? (agentAngle - angleDistance) % 360 : (agentAngle + angleDistance) % 360;
            return new AgentCommand(agent, new Angle(newAngle));
        }
    }

    /**
     * Checks whether the given command will manage to complete this task
     *
     * @param command the command to check
     * @return whether the given command completed this task
     */
    @Override
    protected boolean completesTask(AgentCommand command)
    {
        return Math.abs(command.getAngle().getAngle() - rotateToAngle) < 0.0001;
    }
}
