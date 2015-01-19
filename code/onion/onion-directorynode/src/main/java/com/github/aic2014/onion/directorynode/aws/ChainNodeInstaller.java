package com.github.aic2014.onion.directorynode.aws;

import com.amazonaws.services.ec2.model.InstanceState;
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

            List<AWSChainNode> awsChainNodes = Collections.synchronizedList(new ArrayList<AWSChainNode>());
            //des wern ma zuvü listen :)

            // 2. Request updated information about the chain nodes
            synchronized (awsConnector) {
                awsChainNodes = awsConnector.getAllChainNodes(false);
            }

            //
            // 3. Update pendingChainNodes list and (if ready) execute install script

            InstanceState state;
            List<String> awsCNToRestart = new ArrayList<>();
            for (AWSChainNode awsCN : awsChainNodes) {
                state = awsCN.getState();

                //restart
                if ((state.getCode() == 0) && timeoutPending > TIMEOUT_LIMIT) {
                    // Terminate non-responding chainnode
                    logger.warn("AWS instance " + awsCN.getId() + " did not response in time. Terminate!");
                    synchronized (awsConnector) {
                        awsConnector.terminateChainNode(awsCN.getId(), false);
                    }
                }//32 : shutting-down, 48 : terminated, 64 : stopping, 80 : stopped = restart Chainnode
                else if ((state.getCode() == 32) || (state.getCode() == 48) || (state.getCode() == 64) || (state.getCode() == 80)) {
                    awsCNToRestart.add(awsCN.getInstanceName());
                } else if ((state.getCode() == 16) && (!awsCN.isScriptDone())) {
                    //
                    // Chainnode is ready. Run deployment script
                    logger.info(awsCN.getId() + " is yet ready! Run install script");
                    awsCN.setScriptDone(true);
                    runScriptFor(awsCN);

                }
                }


            if (awsCNToRestart.size() > 0) {
                //naja also da volle java freind bin i net :) haha
                String [] restart = new String [awsCNToRestart.size()];
                int i = 0;
                for (String s : awsCNToRestart) {
                    restart[i] = s;
                    i ++;
                }

                synchronized (awsConnector) {
                    awsConnector.createAWSChainNodes(awsCNToRestart.size(), restart);
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

    private final String installationCommand;
    private final String startChainCommand;
    private AWSConnector awsConnector;
    private int timeoutPending;
    private Timer isReadyCheckTimer;
    private IsReadyCheckTask checkTask;

    public ChainNodeInstaller(Environment env, AWSConnector awsConnector) {
        timeoutPending = 0;
        installationCommand = env.getProperty("aws.chainnode.deploymentCommand");
        startChainCommand = env.getProperty("aws.chainnode.startChainCommand");
        this.awsConnector = awsConnector;

        checkTask = new IsReadyCheckTask();

        isReadyCheckTimer = new Timer();
        isReadyCheckTimer.scheduleAtFixedRate(checkTask, 0l, 1000l);
    }

    //Liste wird immer neu geladen vom Connector.- neu Registrierungen werden im Connector geadded

    /**
     * Runs the deployment script for the for the given chainnode within a separate thread.
     * @param awsChainNode
     */
    private void runScriptFor(AWSChainNode awsChainNode) {

        new Thread(() -> {
            try {

                //split run scritp first copy than start
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

                Thread.sleep(2000);

                rawCommand = String.format(startChainCommand, awsChainNode.getPublicIP());
                logger.info("Executing Command: " + rawCommand);

                cmdSplitted = rawCommand.split(" ");
                pb = new ProcessBuilder(Arrays.asList(cmdSplitted));
                p = pb.start();

                s = new Scanner(p.getInputStream());
                sbInput = new StringBuilder();
                while (s.hasNextLine())
                    sbInput.append(s.nextLine());
                logger.info("Respones of command: " + sbInput.toString());

                s = new Scanner(p.getErrorStream());
                while (s.hasNextLine())
                    sbError.append(s.nextLine());
                logger.info("Error-Respones of command: " + sbError.toString());

            } catch (Exception e) {
                logger.warn("Process to run the deployment script could not be started.", e);
            }
       }){{ setName("Run-Script-Thread (aws-id=" + awsChainNode.getId()+")"); }}.start();


        setChanged();
        notifyObservers(awsChainNode.getId());
    }



}