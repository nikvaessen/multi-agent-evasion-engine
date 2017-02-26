package nl.dke.pursuitevasion.gui;

import java.util.Queue;

/**
 * Created by nik on 26/02/17.
 */
public interface Provider<E>
{
    void subscribe(Receiver<E> receiver);

    void unsubscribe(Receiver<E> receiver);

}
