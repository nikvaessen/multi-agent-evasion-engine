package nl.dke.pursuitevasion.game.agents.impl;

import nl.dke.pursuitevasion.game.agents.AbstractAgent;
import nl.dke.pursuitevasion.game.agents.Direction;

import java.awt.*;

/**
 * Created by nik on 26/02/17.
 */
public class SimpleAgent
    extends AbstractAgent
{
    public SimpleAgent(Point startLocation, Direction startsFacing, int radius)
    {
        super(startLocation, startsFacing, radius);
    }

}
