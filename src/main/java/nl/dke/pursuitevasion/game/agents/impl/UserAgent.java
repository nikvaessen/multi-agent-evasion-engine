package nl.dke.pursuitevasion.game.agents.impl;

import nl.dke.pursuitevasion.game.agents.AbstractAgent;
import nl.dke.pursuitevasion.game.agents.Direction;
import nl.dke.pursuitevasion.gui.KeyboardInputListener;
import nl.dke.pursuitevasion.gui.Receiver;

import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * Created by nik on 26/02/17.
 */
public class UserAgent
    extends AbstractAgent
    implements Receiver<KeyEvent>
{

    private boolean north;
    private boolean south;
    private boolean west;
    private boolean east;

    public UserAgent(Point startLocation, Direction startsFacing, int radius, KeyboardInputListener listener)
    {
        super(startLocation, startsFacing, radius);
        listener.subscribe(this);
    }

    @Override
    public void notify(KeyEvent keyEvent)
    {
        boolean setTo;
        switch(keyEvent.getKeyCode())
        {
            case KeyEvent.KEY_PRESSED:
                setTo = true;
                break;
            case KeyEvent.KEY_RELEASED:
                setTo = false;
                break;
            default:
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
            default:
                break;
        }
    }



}
