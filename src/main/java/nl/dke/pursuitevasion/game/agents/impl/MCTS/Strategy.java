package nl.dke.pursuitevasion.game.agents.impl.MCTS;


/**
 * Created by giogio on 1/14/17.
 */
public interface Strategy {
    public Move getMove();
    public NodeTree_2 getRootTreeMcts();
    public void resetTree();
    public void updateState(State state);
}
