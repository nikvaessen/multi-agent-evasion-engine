package nl.dke.pursuitevasion.game.agents.impl;

import nl.dke.pursuitevasion.game.EngineConstants;
import nl.dke.pursuitevasion.game.Vector2D;
import nl.dke.pursuitevasion.game.agents.AbstractAgent;
import nl.dke.pursuitevasion.game.agents.AgentRequest;
import nl.dke.pursuitevasion.game.agents.Direction;
import nl.dke.pursuitevasion.game.agents.tasks.RotateTask;
import nl.dke.pursuitevasion.game.agents.tasks.WalkToTask;
import nl.dke.pursuitevasion.gui.KeyboardInputListener;
import nl.dke.pursuitevasion.gui.Receiver;
import nl.dke.pursuitevasion.map.impl.Floor;
import nl.dke.pursuitevasion.map.impl.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.KeyEvent;

/**
 * Created by nik on 26/02/17.
 */
public class UserAgent
    extends AbstractAgent
    implements Receiver<KeyEvent>
{
    private static Logger logger = LoggerFactory.getLogger(UserAgent.class);

    private boolean north;
    private boolean south;
    private boolean west;
    private boolean east;

    public UserAgent(Map map, Floor startingFloor, Vector2D startLocation, Direction startsFacing, int radius,
                     double visionRange, double visionAngle, KeyboardInputListener listener)
    {
        super(map, startingFloor, startLocation, startsFacing, radius, visionRange, visionAngle);
        listener.subscribe(this);
    }

    @Override
    public void notify(KeyEvent keyEvent)
    {
        if(logger.isTraceEnabled())
        {
            logger.trace("received keyevent: {}", keyEvent);
            logger.trace("number: {}, pressed: {}, released: {}, typed: {}",
                    keyEvent.getID(),
                    KeyEvent.KEY_PRESSED, KeyEvent.KEY_RELEASED, KeyEvent.KEY_TYPED);
        }
        boolean setTo;
        switch(keyEvent.getID())
        {
            case KeyEvent.KEY_PRESSED:
                setTo = true;
                break;
            case KeyEvent.KEY_RELEASED:
                setTo = false;
                break;
            default: // key typed events not interesting
                return;
        }

        if(logger.isTraceEnabled())
        {
            logger.trace("Character of the key event: " + keyEvent.getKeyChar());
        }

        switch(keyEvent.getKeyCode())
        {
            case KeyEvent.VK_W:
                handleNorthCommand(setTo);
                break;
            case KeyEvent.VK_UP:
                handleNorthCommand(setTo);
                break;
            case KeyEvent.VK_S:
                handleSouthCommand(setTo);
                break;
            case KeyEvent.VK_DOWN:
                handleSouthCommand(setTo);
                break;
            case KeyEvent.VK_A:
                handleWestCommand(setTo);
                break;
            case KeyEvent.VK_LEFT:
                handleWestCommand(setTo);
                break;
            case KeyEvent.VK_D:
                handleEastCommand(setTo);
                break;
            case KeyEvent.VK_RIGHT:
                handleEastCommand(setTo);
                break;
            default: // other characters not interesting
                break;
        }

        if(logger.isTraceEnabled())
        {
            logger.trace("Set UserAgent to north:{},south:{}, west:{}, east:{}",
                    north, south, west, east);
        }
    }

    private void handleNorthCommand(boolean setTo)
    {
        if(south && setTo)
        {
            south = false;
        }
        north = setTo;
    }

    private void handleSouthCommand(boolean setTo)
    {
        if(north && setTo)
        {
            north = false;
        }
        south = setTo;
    }

    private void handleWestCommand(boolean setTo)
    {
        if(east && setTo)
        {
            east = false;
        }
        west = setTo;
    }

    private void handleEastCommand(boolean setTo)
    {
        if(west && setTo)
        {
            west = false;
        }
        east = setTo;
    }

    private Vector2D getMoveToLocation(Vector2D location)
    {
        double x = location.getX();
        double y = location.getY();
        if(north)
        {
            y -= EngineConstants.WALKING_SPEED;
        }
        if(south)
        {
            y += EngineConstants.WALKING_SPEED;
        }
        if(west)
        {
            x -= EngineConstants.WALKING_SPEED;
        }
        if(east)
        {
            x += EngineConstants.WALKING_SPEED;
        }
        return new Vector2D(x, y);
    }

    @Override
    protected void completeRequest(AgentRequest request)
    {
        if(logger.isTraceEnabled())
        {
            logger.trace("UserAgent:[north:{},south:{}, west:{}, east:{}",
                    north, south, west, east);
        }
        if(north || south || west || east)
        {
            if(logger.isDebugEnabled())
            {
                logger.debug("Making a request");
            }

            Direction direction = Direction.getDirection(north, south, east, west);
            request.add(new RotateTask(Direction.getAngle(direction)));
            request.add(new WalkToTask(getMoveToLocation(request.getAgent().getLocation())));
        }
    }

    /**
     * Calculate whether a new Request is to be determined
     *
     * @return whether a new Request should be created
     */
    @Override
    protected boolean hasNewRequest()
    {
        return true;
    }

    @Override
    public boolean isEvader() {
        return false;
    }

    @Override
    public String toString() {
        return "User" + super.toString();
    }
}
