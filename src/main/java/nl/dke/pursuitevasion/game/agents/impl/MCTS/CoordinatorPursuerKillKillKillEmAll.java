package nl.dke.pursuitevasion.game.agents.impl.MCTS;

import nl.dke.pursuitevasion.game.Vector2D;
import nl.dke.pursuitevasion.game.agents.AbstractAgent;
import nl.dke.pursuitevasion.game.agents.AgentRequest;
import nl.dke.pursuitevasion.game.agents.Direction;
import nl.dke.pursuitevasion.map.impl.Floor;
import nl.dke.pursuitevasion.map.impl.Map;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nibbla on 19.05.2017.
 */
public class CoordinatorPursuerKillKillKillEmAll{
    List<PursuerKillKillKillEmAll> pursuers = new ArrayList<>(3);

    public CoordinatorPursuerKillKillKillEmAll() {

    }

    public void addPursuers(PursuerKillKillKillEmAll pursuers) {
        this.pursuers.add(pursuers);
    }
}
