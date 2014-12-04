package com.github.aic2014.onion.directorynode.aws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import java.util.*;

/**
 * This class performs the duty to install a PENDING/ACTIVE AWS instance to be used as a
 * chain node
 */
public class ChainNodeInstaller extends Observable {

    private class IsReadyCheckTask extends TimerTask {

        @Override
        public void run() {

            //1. Grab a temporary list of pending chain nodes
            List<String> tempPendingChainNodes = new LinkedList<>();
            synchronized (pendingChainNodes) {
                pendingChainNodes.keySet().forEach(awsCN -> tempPendingChainNodes.add(awsCN.getInstanceId()));
            }

            //
            // 2. Request updated information about the chain nodes
            List<AWSChainNode> awsChainNodes = awsConnector.getById(tempPendingChainNodes);

            //
            // 3. Update pendingChainNodes list and (if ready) execute install script
            synchronized (pendingChainNodes) {

                for (AWSChainNode awsCN : awsChainNodes) {
                    int startupTime = pendingChainNodes.get(awsCN);
                    if (!awsCN.isReady() && startupTime > TIMEOUT_LIMIT) {
                        //
                        // Terminate non-responding chainnode
                        logger.warn("AWS instance " + awsCN.getInstanceId() + " did not response in time. Terminate!");
                        awsConnector.terminateChainNode(awsCN.getInstanceId());
                        pendingChainNodes.remove(awsCN);
                    }
                    else if (!awsCN.isReady()) {
                        //
                        // Update timeout-counter on not-yet-ready chainnode
                        logger.info(awsCN.getInstanceId() + " not yet ready! Increase timeout time");
                        pendingChainNodes.put(awsCN, startupTime + POLL_TIME);
                    }
                    else {
                        //
                        // Chainnode is ready. Run deployment script
                        logger.info(awsCN.getInstanceId() + " is yet ready! Run install script");
                        runScriptFor(awsCN);
                        pendingChainNodes.remove(awsCN);
                    }
                }
            }
        }
    }

    /**
     * Polling every n second(s) to check if the AWS instance is READY
     */
    private static final int POLL_TIME = 1;
    /**
     * Terminate not-READY AWS instance after n seconds
     */
    private static final int TIMEOUT_LIMIT = 45;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Map<AWSChainNode, Integer> pendingChainNodes;
    private final String installationCommand;
    private AWSConnector awsConnector;
    private int timeoutPending;
    private Timer isReadyCheckTimer;
    private IsReadyCheckTask checkTask;

    public ChainNodeInstaller(Environment env, AWSConnector awsConnector) {
        timeoutPending = 0;
        installationCommand = env.getProperty("aws.chainnode.deploymentCommand");
        pendingChainNodes = new HashMap<>();
        this.awsConnector = awsConnector;

        //TODO: init check-thread
        checkTask = new IsReadyCheckTask();

        isReadyCheckTimer = new Timer();
        isReadyCheckTimer.scheduleAtFixedRate(checkTask, 0l, 1000l);
    }

    /**
     * Adds the given list of AWS chain nodes to the installer, in order to be configured/installed as chain nodes
     * @param awsChainNodes
     */
    public void runInstallerFor(List<AWSChainNode> awsChainNodes) {
        synchronized (pendingChainNodes) {
            awsChainNodes.forEach(awsCN -> pendingChainNodes.put(awsCN, 0));
        }
    }

    /**
     * Runs the deployment script for the for the given chainnode within a separate thread.
     * @param awsChainNode
     */
    private void runScriptFor(AWSChainNode awsChainNode) {

        new Thread(() -> {
            try {
                String rawCommand = String.format(installationCommand, awsChainNode.getPublicIP());
                logger.info("Executing Command: " + rawCommand);

                String[] cmdSplitted = rawCommand.split(" ");
                ProcessBuilder pb = new ProcessBuilder(Arrays.asList(cmdSplitted));
                Process p = pb.start();

                Scanner s = new Scanner(p.getInputStream());
                StringBuilder sbInput = new StringBuilder();
                while (s.hasNextLine())
                    sbInput.append(s.nextLine());
                logger.info("Respones of command: " + sbInput.toString());

                s = new Scanner(p.getErrorStream());
                StringBuilder sbError = new StringBuilder();
                while (s.hasNextLine())
                    sbError.append(s.nextLine());
                logger.info("Error-Respones of command: " + sbError.toString());

            } catch (Exception e) {
                logger.warn("Process to run the deployment script could not be started.", e);
            }
        }){{ setName("Run-Script-Thread (aws-id=" + awsChainNode.getInstanceId()+")"); }}.start();


        setChanged();
        notifyObservers(awsChainNode.getInstanceId());
    }
}