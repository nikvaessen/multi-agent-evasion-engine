package nl.dke.pursuitevasion.game.agents.impl;

import nl.dke.pursuitevasion.game.Engine;
import nl.dke.pursuitevasion.game.EngineConstants;
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

import java.awt.*;
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

    public UserAgent(Map map, Floor startingFloor, Point startLocation, Direction startsFacing, int radius,
                     KeyboardInputListener listener)
    {
        super(map, startingFloor, startLocation, startsFacing, radius);
        listener.subscribe(this);
    }

    @Override
    public void notify(KeyEvent keyEvent)
    {
        logger.trace("received keyevent: {}", keyEvent);
        boolean setTo;
        switch(keyEvent.getKeyCode())
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
        switch(keyEvent.getKeyChar())
        {
            case 'w':
                north = setTo;
                break;
            case 's':
                south = setTo;
                break;
            case 'a':
                west  = setTo;
                break;
            case 'd':
                east  = setTo;
                break;
            default: // other characters not interesting
                break;
        }
    }

    private Point getLocation(Point location, Direction direction)
    {
        int x = location.x;
        int y = location.y;
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
        return new Point(x, y);
    }

    @Override
    protected void completeRequest(AgentRequest request)
    {
        if(north || south || west || east)
        {
            logger.debug("Making a request");
            Direction direction = Direction.getDirection(north, south, east, west);
            request.add(new RotateTask(Direction.getAngle(direction)));
            request.add(new WalkToTask(getLocation(request.getAgent().getLocation(), direction)));
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
}
