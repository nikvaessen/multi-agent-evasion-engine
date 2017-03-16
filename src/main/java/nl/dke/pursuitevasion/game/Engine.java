package nl.dke.pursuitevasion.game;

import nl.dke.pursuitevasion.game.agents.AbstractAgent;
import nl.dke.pursuitevasion.game.agents.AgentCommand;
import nl.dke.pursuitevasion.game.agents.AgentRequest;
import nl.dke.pursuitevasion.game.agents.tasks.AbstractAgentTask;
import nl.dke.pursuitevasion.gui.simulator.MapViewPanel;
import nl.dke.pursuitevasion.map.impl.Floor;
import nl.dke.pursuitevasion.map.impl.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.function.Predicate;
;

/**
 * Created by nik on 2/8/17.
 */
public class Engine
{

    private final static Logger logger = LoggerFactory.getLogger(Engine.class);

    private final int desiredFPS;

    private long desiredIterationLength; //in ms

    private Map map;

    private Collection<AbstractAgent> agents;

    private MapViewPanel mapViewPanel;

    private Thread gameLoopThread;

    public Engine(Map map, Collection<AbstractAgent> agents, MapViewPanel viewPanel, int desiredFPS)
    {
        this.map = map;
        this.desiredFPS = desiredFPS;
        this.desiredIterationLength = Math.round(1000d / (double) desiredFPS);
        this.agents = agents;
        this.mapViewPanel = viewPanel;
    }

    public Engine(Map map, Collection<AbstractAgent> agents, int desiredFPS)
    {
        this(map, agents, null, desiredFPS);
    }

    public synchronized void start()
        throws IllegalStateException
    {
        if(gameLoopThread == null || !gameLoopThread.isAlive())
        {
            gameLoopThread = new Thread(new GameLoopRunnable());
            gameLoopThread.start();
        }
        else
        {
            throw new IllegalStateException("Starting engine while it's already running");
        }
    }

    private class GameLoopRunnable
        implements Runnable
    {
        private LinkedList<AgentRequest> requests = new LinkedList<>();

        private LinkedList<AgentCommand> commands = new LinkedList<>();

        private double metersPerIteration = EngineConstants.WALKING_SPEED / desiredIterationLength;

        private double rotationPerIteration = EngineConstants.TURNING_SPEED / desiredIterationLength;

        @Override
        public void run()
        {
            loop();
        }

        /**
         * Continuous-time loop:
         * 1. Check for game over, or if any agent has been spotted
         * 2. Request agents to change their direction if they so desire
         * 3. Validate if current behaviour of agents is legal
         * 4. Update the environment based on current behaviour
         * 5. Agents which are allowed to have hidden information are given an update
         * 6. Render on screen
         * 7. Wait if there is time left.
         */
        private void loop()
        {
            // house keeping variables for loop
            long startTime = System.currentTimeMillis(), iterationStartTime, msPassed;
            int count = 0;
            while(true)
            {
                //update housekeeping variables and log
                iterationStartTime = System.currentTimeMillis();
                count++;
                if(logger.isDebugEnabled())
                {
                    logger.debug("Starting game loop iteration {} at {} ms", count, iterationStartTime);
                }

                // 1. Check if game is over
                //todo implement game over checking
                if(System.currentTimeMillis() - startTime > 1000000) //1000 seconds
                {
                    break;
                }

                // 2. Check agents
                for(AbstractAgent agent : agents)
                {
                    if(agent.hasRequest()) {
                        AgentRequest request = agent.getRequest();
                        requests.add(request);

                        if (logger.isTraceEnabled())
                        {
                            logger.trace("Added new Request to list. Request: {} Size: {}", request, requests.size());
                        }
                    }
                }

                // 3. Validate legality of moves

                //first filter out any invalidated/completed requests
                if(logger.isDebugEnabled())
                {
                    logger.debug("Handling {} requests", requests.size());
                }
                requests.removeIf(new Predicate<AgentRequest>() {
                    @Override
                    public boolean test(AgentRequest agentRequest) {
                        return agentRequest.isCompleted();
                    }
                });

                //go over all requests and get the agent commands
                if(logger.isDebugEnabled())
                {
                    logger.debug("{} requests left after deleting all completed requests", requests.size());
                }

                for(AgentRequest request : requests)
                {
                    logger.trace("is completed: {}", request.isCompleted());
                    handleRequest(request);
                }

                //check if all commands are valid
                validateCommands();

                // 4. Make the moves
                if(logger.isDebugEnabled())
                {
                    logger.debug("Applying {} commands", commands.size());
                }
                commands.forEach(AgentCommand::apply);
                commands.clear();

                // 5. Update the view
                if(mapViewPanel != null) {
                    mapViewPanel.repaint();
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Updated MapViewPanel");
                    }
                }

                // 6. wait
                msPassed = System.currentTimeMillis() - iterationStartTime;
                if(msPassed < desiredIterationLength)
                {
                    try
                    {
                        if(logger.isDebugEnabled())
                        {
                            logger.debug("waiting for {} ms", desiredIterationLength - msPassed);
                        }
                        Thread.sleep(desiredIterationLength - msPassed);
                    }
                    catch(InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
                else
                {
                    logger.warn("GameLoop took {} ms while only allowed to take {} ms. Exceeded: {} ms",
                            msPassed, desiredIterationLength, msPassed - desiredIterationLength);
                }
            }

            logger.info("Game successfully terminated in {} ms", System.currentTimeMillis() - startTime);
        }

        private void handleRequest(AgentRequest request)
        {
            if(logger.isTraceEnabled())
            {
                logger.trace("Resolving request {}", request);
            }

            double allowedMeters = metersPerIteration;
            double allowedRotation = rotationPerIteration;
            AbstractAgentTask task = null;
            do {
                try
                {
                    task = request.peek();
                    AgentCommand command = task.handle(request.getAgent(), allowedMeters, allowedRotation);
                    commands.add(command);
                    allowedMeters -= command.getMovedDistance();
                    allowedRotation -= command.getRotatedDistance();

                    if (logger.isTraceEnabled()) {
                        logger.trace("Added to commands: {} ", command);
                        logger.trace("allowed meters left: {}", allowedMeters);
                        logger.trace("allowed rotation left: {}", allowedRotation);
                    }
                }
                catch (IllegalStateException e)
                {
                    logger.error("Cannot resolve new task", e);
                    break;
                }
            }
            while(!request.isCompleted() && allowedMeters > 0 && allowedRotation > 0);
        }

        private void validateCommands()
        {
            commands.forEach(this::outOfBoundCorrection);
        }

        private void outOfBoundCorrection(AgentCommand command)
        {
            if(command.isLocationChanged())
            {
                Point p = command.getLocation();
                Floor floor = command.getAgent().getFloor();
                if (!floor.getPolygon().contains(p)) {
                    //todo fix it
                    logger.error("Currently agent is going out of bounds");
                }
            }
        }

    }

}