package com.github.aic2014.onion.directorynode.aws;

import com.github.aic2014.onion.directorynode.DirectoryNodeService;
import com.github.aic2014.onion.model.ChainNodeInfo;
import com.github.aic2014.onion.model.ChainNodeRoutingStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.InetAddress;
import java.net.URL;
import java.util.*;

/**
 * Concrete implementation of the directory node service using AWS SDK to access EC2 instances
 */
@EnableScheduling
public class AWSDirectoryNodeService implements DirectoryNodeService {

    private final static int DEFAULT_NUM_TOTAL_CHAINS = 6;
    private final static int DEFAULT_CHAIN_SIZE = 3;
    private final static int DEFAULT_LIFECHECK_CONNECT_TIMEOUT = 5000;          //msec
    private final static int DEFAULT_LIFECHECK_READ_TIMEOUT = 5000;             //msec

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private Environment env;

    private AWSConnector awsConnector;
    private boolean terminateExisting = false;
    private int latestNodeIndex = 0;
    private int numberOfTotalChainNodes;         //total number of available chains
    private int chainNodeSize;                  //minimum number of nodes for a "chain"

    private String publicIP = null;
    private boolean starting;
    private ChainNodeInstaller cnInstaller;
    private RestTemplate restTemplate = new RestTemplate();

    /**
     * Initializes the AWS Directory Node Service
     */
    @PostConstruct
    public void onInit() {

        starting = true;

        //
        //1. read configuration,
        //1.1 update chainnode deployment config. file
        initConfiguration();
        retrievePublicIP();
        updateChainNodeInfo();

        //
        //2. init AWS-EC2 client
        awsConnector = new AWSConnector(env);

        //
        //3. search for existing chain nodes
        if (terminateExisting) {
            List<AWSChainNode> existingChainNodes = awsConnector.getAllChainNodes(true);
            logger.info("Found " + existingChainNodes.size() + " existing chain nodes on startup. Let's terminate them all!");
            existingChainNodes.forEach(cni -> awsConnector.terminateChainNode(cni.getId(), true));
        }

        //4. create new chain nodes
        String[] chainNodeNames = new String[numberOfTotalChainNodes];
        for (int i = 0; i < numberOfTotalChainNodes; i++) {
            int serial = latestNodeIndex + i;
            chainNodeNames[i] = String.format("%s%d", env.getProperty("aws.chainnode.prefix"), serial);
        }
        latestNodeIndex = numberOfTotalChainNodes;
        awsConnector.createAWSChainNodes(numberOfTotalChainNodes, chainNodeNames);
        logger.info("Created (requested) " + numberOfTotalChainNodes + " new chain nodes within AWS.");

        //6. Run setup-script for new chainnodes
        cnInstaller = new ChainNodeInstaller(env, awsConnector);
        starting = false;
        //der Thread  läuft Liste wird vom Connector geladen
    }

    @Override
    public String registerChainNode(ChainNodeInfo remoteChainNodeInfo) {

        String id = "";
        synchronized (this.awsConnector) {
            AWSChainNode awsChainNode = this.awsConnector.findChainNodeByIP(remoteChainNodeInfo.getPublicIP());
            awsChainNode.setPublicKey(remoteChainNodeInfo.getPublicKey());
            awsChainNode.setPort(remoteChainNodeInfo.getPort());
            id = awsChainNode.getId();
            //routing Status will be registered when public key has been received
            this.awsConnector.registerRoutingStatus(awsChainNode);
        }

        logger.info(String.format("Register awsChainNode.id " + id + "  chain node with IP %s.", remoteChainNodeInfo.getPublicIP()));
        return id;
    }

    @Override
    public String getIPAddress() {
        return publicIP;
    }

    @Override
    public void unregisterChainNode(String id) {
        /*
        synchronized(awsChain
        Nodes) {
            awsChainNodes.removeIf(cn -> cn.getId().equals(id));
        }
        */
    }

    @Override
    public ChainNodeInfo getChainNode(String id) {
        Optional<AWSChainNode> result = awsConnector.getAllChainNodes(false).stream().filter(cni -> id.equals(cni.getId())).findAny();
        return result.isPresent() ? result.get() : null;
    }

    @Override
    public Collection<ChainNodeInfo> getAllChainNodes() {
        return new LinkedList<ChainNodeInfo>() {{
            getAllAWSChainNodes().forEach(cni -> add(cni));
        }};
    }

    public Collection<AWSChainNode> getAllAWSChainNodes() {
        return new LinkedList<AWSChainNode>() {{
            awsConnector.getAllChainNodes(false).forEach(cni -> add(cni));
        }};
    }

    @Override
    public List<ChainNodeInfo> getChain() {
        return Arrays.asList(this.awsConnector.getChain(chainNodeSize));
    }

    /**
     * Performs all configuration-related tasks.
     * (Basically, just read the configuration file and perform error handling)
     */
    private void initConfiguration() {
        try {
            numberOfTotalChainNodes = Integer.parseInt(env.getProperty("aws.chainnode.quantity"));
        } catch (NumberFormatException e) {
            numberOfTotalChainNodes = DEFAULT_NUM_TOTAL_CHAINS;
        }
        try {
            chainNodeSize = Integer.parseInt(env.getProperty("aws.chainnode.minQuantity"));
        } catch (NumberFormatException e) {
            chainNodeSize = DEFAULT_CHAIN_SIZE;
        }

        terminateExisting = Boolean.parseBoolean(env.getProperty("aws.terminateExisting"));

        //
        // connect/read timeout for life checks
        int connectTimeout;
        int readTimeout;
        try {
            connectTimeout = Integer.parseInt(env.getProperty("lifecheck.connectTimeout"));
        } catch (NumberFormatException e) {
            connectTimeout = DEFAULT_LIFECHECK_CONNECT_TIMEOUT;
        }

        try {
            readTimeout = Integer.parseInt(env.getProperty("lifecheck.readTimeout"));
        } catch (NumberFormatException e) {
            readTimeout = DEFAULT_LIFECHECK_READ_TIMEOUT;
        }

        ((SimpleClientHttpRequestFactory) restTemplate.getRequestFactory()).setConnectTimeout(connectTimeout);
        ((SimpleClientHttpRequestFactory) restTemplate.getRequestFactory()).setReadTimeout(readTimeout);
    }

    private void retrievePublicIP() {
        //
        // Code-Snippet for IP-check from: http://stackoverflow.com/a/14541376
        BufferedReader in = null;
        try {
            String ip;
            URL whatismyip = new URL("http://checkip.amazonaws.com");

            in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
            ip = in.readLine();
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
            publicIP = ip;
        } catch (IOException e) {
            try {
                in.close();
            } catch (Exception innerE) {
            }
        }
    }

    /**
     * Updates the chainnode configuration file, which will be deployed.
     * Adds the public IP of this directory service into the config. file.
     */
    private void updateChainNodeInfo() {

        try {
            if (getIPAddress() == null || getIPAddress().isEmpty()) {
                throw new Exception("Public IP directory node not available. Cannot update chain node config file.");
            }

            Properties props = new Properties();

            String path = env.getProperty("aws.chainnode.deploymentConfPath");
            InputStream is = new FileInputStream(new File(path));
            if (is == null) {
                is = getClass().getResourceAsStream(path);
            }

            props.load(is);

            props.setProperty("directorynode.hostname", "http://" + getIPAddress());
            props.store(new FileOutputStream(path), null);
        } catch (Exception e) {
            logger.error("IP of Directorynode could not be set in Config of Chainnode.", e);
        }
    }

    @Scheduled(fixedDelay = 5000)
    public void LifeCheck() {
        if (starting)
            return;

        //es müssen mindestens
        int countAlive = 0;
        Collection<AWSChainNode> aWSChainNodes = (this).getAllAWSChainNodes();

        InetAddress inetAddress = null;

        Date start, stop;
        Iterator itAwsChainNode = aWSChainNodes.iterator();
        AWSChainNode aWSChainNode;
        ChainNodeRoutingStats chainNodeRoutingStats;
        while (itAwsChainNode.hasNext()) {
            aWSChainNode = (AWSChainNode) itAwsChainNode.next();

            //instance is running check responsetime  16 : running
            if ((aWSChainNode.getState() != null) && (aWSChainNode.getState().getCode() == 16) && (!aWSChainNode.isShuttingDown()) && (aWSChainNode.getPublicKey() != null)) {

                try {
                    chainNodeRoutingStats = restTemplate.getForObject(aWSChainNode.getUri().toString() + "/ping", ChainNodeRoutingStats.class);
                    synchronized (this.awsConnector) {
                        this.awsConnector.updateRoutingStatus(aWSChainNode, chainNodeRoutingStats);
                    }
                    aWSChainNode.setLastLifeCheck(new Date());
                } catch (Exception e) {
                    logger.debug("caught exception while trying to ping node " + aWSChainNode.getId(), e.getMessage());
                    logger.info("could not ping node %s, unregistering it Error: " + e.getMessage(), aWSChainNode.getId());
                    aWSChainNode.setShuttingDown(true);
                    this.awsConnector.loadBalancerDeleteNode(aWSChainNode.getId());
                }
            }
        }
    }
}