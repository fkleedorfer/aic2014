package com.github.aic2014.onion.directorynode.aws;

import com.amazonaws.services.simpleworkflow.model.Run;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * This class performs the duty to install a PENDING/ACTIVE AWS instance to be used as a
 * chain node
 */
public class ChainNodeInstaller {

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

                    if (!awsCN.isReady() && startupTime > 20) {
                        logger.warn("AWS instance " + awsCN.getInstanceId() + " did not response in time. Terminate!");
                        awsConnector.terminateChainNode(awsCN.getInstanceId());
                        pendingChainNodes.remove(awsCN);
                    }
                    else if (!awsCN.isReady()) {
                        logger.info(awsCN.getInstanceId() + " not yet ready! Increase timeout time");
                        pendingChainNodes.put(awsCN, startupTime + POLL_TIME);
                    }
                    else {
                        logger.info(awsCN.getInstanceId() + " is yet ready! Run install script");
                        runScriptFor(awsCN);
                        pendingChainNodes.remove(awsCN);
                    }
                }

            }
        }
    }

    private static final int POLL_TIME = 1;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Map<AWSChainNode, Integer> pendingChainNodes;
    private final String installationCommand;
    private AWSConnector awsConnector;
    private int timeoutPending;
    private Timer isReadyCheckTimer;
    private IsReadyCheckTask checkTask;

    public ChainNodeInstaller(Environment env, AWSConnector awsConnector) {
        timeoutPending = 0;
        installationCommand = String.format(env.getProperty("aws.chainnode.setupCommand"),
                env.getProperty("aws.chainnode.setupIDfile"),
                env.getProperty("aws.chainnode.setupSource"),
                "%s");
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

    private void runScriptFor(AWSChainNode awsChainNode) {
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
            logger.warn("Process to run the installation script could not be started.", e);
        }
    }
}
