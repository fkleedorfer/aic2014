package com.github.aic2014.onion.directorynode.aws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import java.util.*;

/**
 * This class performs the duty to install a PENDING/ACTIVE AWS instance to be used as a
 * chain node
 */
public class ChainNodeInstaller {

    /**
     * FYI: http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/ec2/model/InstanceState.html#getCode()
     * 0 : pending
     * 16 : running
     * 32 : shutting-down
     * 48 : terminated
     * 64 : stopping
     * 80 : stopped
     */
    private class IsReadyCheckTask extends TimerTask {

        @Override
        public void run() {

            List<AWSChainNode> awsChainNodes;

            //1. Request updated information about the chain nodes
            awsConnector.updateCurrentChainNodes();
            awsChainNodes = awsConnector.getInternalChainNodeList();
            logger.info("Investigating " + awsChainNodes.size() + " chain nodes...");

            List<String> awsCNToReCreate = new ArrayList<>();
            for (AWSChainNode awsCN : awsChainNodes) {

                //restart
                if (awsCN.isScheduledForShuttingDown())  {
                    // Terminate non-responding chainnode
                    logger.warn("AWS instance " + awsCN.getId() + " was scheduled to shutdown. Terminate and request new instance!");
                    awsConnector.terminateChainNode(awsCN.getId(), false);
                    awsCNToReCreate.add(awsCN.getInstanceName());
                }
                else if ((awsCN.getAWSState() == AWSState.SHUTTING_DOWN ||
                        awsCN.getAWSState() == AWSState.TERMINATED ||
                        awsCN.getAWSState() == AWSState.STOPPING ||
                        awsCN.getAWSState() == AWSState.STOPPED) && !awsCN.isScheduledForShuttingDown()) {
                    //
                    // somehow the chain node is not RUNNING any more, but was NOT scheduled to shutdown
                    // --> create a new one
                    logger.warn("AWS instance " + awsCN.getId() + " was externally disabled. Request new instance!");
                    awsCNToReCreate.add(awsCN.getInstanceName());
                    awsConnector.loadBalancerDeleteNode(awsCN.getId());
                }
                else if (awsCN.getAWSState() == AWSState.RUNNING && !(awsCN.isInstalling() || awsCN.isRegistered())) {
                    //
                    // Chainnode is ready. Run deployment script
                    logger.info(awsCN.getId() + " is ready! Run install script");
                    awsCN.confirmInstalling();
                    runScriptFor(awsCN);
                }
            }

            if (awsCNToReCreate.size() > 0) {
                logger.info("Recreate " + awsCNToReCreate.size() + " chain nodes...");
                String[] restart = awsCNToReCreate.toArray(new String[awsCNToReCreate.size()]);
                awsConnector.createAWSChainNodes(awsCNToReCreate.size(), restart);
            }
        }
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final String installationCommand;
    private AWSConnector awsConnector;
    private Timer isReadyCheckTimer;
    private IsReadyCheckTask checkTask;

    public ChainNodeInstaller(Environment env, AWSConnector awsConnector) {
        installationCommand = env.getProperty("aws.chainnode.deploymentCommand");
        this.awsConnector = awsConnector;

        checkTask = new IsReadyCheckTask();
    }

    /**
     * Starts the installer thread, which periodically tries to run the deployment script
     * for new chain-nodes (scp + ssh)
     *
     * Period: 5 seconds
     */
    public void startInstallerThread() {
        isReadyCheckTimer = new Timer();
        isReadyCheckTimer.scheduleAtFixedRate(checkTask, 0l, 5000);
    }

    //Liste wird immer neu geladen vom Connector.- neu Registrierungen werden im Connector geadded

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
       }){{ setName("Run-Script-Thread (aws-id=" + awsChainNode.getId() + ")"); }}.start();
    }
}