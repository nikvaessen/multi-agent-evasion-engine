package nl.dke.pursuitevasion.gui;

/**
 * Created by nik on 26/02/17.
 */
public interface Provider<E>
{
    void subscribe(Receiver<E> receiver);

    void unsubscribe(Receiver<E> receiver);

}
