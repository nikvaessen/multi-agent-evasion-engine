package nl.dke.pursuitevasion.gui;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.*;

/**
 * Created by nik on 26/02/17.
 */
public class KeyboardInputListener
    implements KeyListener, Provider<KeyEvent>
{

    private LinkedList<Receiver<KeyEvent>> receivers = new LinkedList<>();

    @Override
    public void keyTyped(KeyEvent e)
    {
        notifySubscribers(e);
    }

    @Override
    public void keyPressed(KeyEvent e)
    {
        notifySubscribers(e);
    }

    public void keyReleased(KeyEvent e)
    {
        notifySubscribers(e);
    }

    @Override
    public void subscribe(Receiver<KeyEvent> receiver)
    {
        receivers.push(receiver);
    }

    @Override
    public void unsubscribe(Receiver<KeyEvent> receiver)
    {
        receivers.remove(receiver);
    }

    private void notifySubscribers(KeyEvent event)
    {
        for(Receiver<KeyEvent> receiver : receivers)
        {
            receiver.notify(event);
        }
    }
}