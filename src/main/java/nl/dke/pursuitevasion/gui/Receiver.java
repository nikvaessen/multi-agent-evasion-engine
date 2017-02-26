package nl.dke.pursuitevasion.gui;

/**
 * Created by nik on 26/02/17.
 */
public interface Receiver<E>
{
    void notify(E e);
}
