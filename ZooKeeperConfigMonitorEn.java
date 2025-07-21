import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

/**
 * ZooKeeper Configuration Monitor
 * Monitor configuration changes and pull latest configs to local files
 */
public class ZooKeeperConfigMonitorEn {
    
    // Configuration parameters
    private static final String ZK_SERVER = System.getenv().getOrDefault("ZK_SERVER", "config-center-zookeeper:2181");
    private static final String CONFIG_PATH = System.getenv().getOrDefault("CONFIG_PATH", "/config-center/configs");
    private static final String LOCAL_CONFIG_DIR = System.getenv().getOrDefault("LOCAL_CONFIG_DIR", "./configs");
    private static final int MONITOR_INTERVAL = Integer.parseInt(System.getenv().getOrDefault("MONITOR_INTERVAL", "5000"));
    
    private ZooKeeper zk;
    private CountDownLatch connectedSignal = new CountDownLatch(1);
    private ScheduledExecutorService scheduler;
    private boolean running = true;
    
    public void start() throws Exception {
        System.out.println("=== ZooKeeper Configuration Monitor Starting ===");
        System.out.println("ZooKeeper Server: " + ZK_SERVER);
        System.out.println("Config Path: " + CONFIG_PATH);
        System.out.println("Local Config Dir: " + LOCAL_CONFIG_DIR);
        System.out.println("Monitor Interval: " + MONITOR_INTERVAL + "ms");
        
        // Create local config directory
        createLocalConfigDir();
        
        // Connect to ZooKeeper
        connectToZooKeeper();
        
        // Start scheduled monitoring
        startScheduledMonitor();
        
        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down configuration monitor...");
            stop();
        }));
        
        // Wait for connection
        connectedSignal.await();
        System.out.println("Configuration monitor started, monitoring config changes...");
    }
    
    private void connectToZooKeeper() throws Exception {
        zk = new ZooKeeper(ZK_SERVER, 30000, new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                System.out.println("ZooKeeper Event: " + event.getType() + " - " + event.getState());
                
                if (event.getState() == Event.KeeperState.SyncConnected) {
                    System.out.println("Connected to ZooKeeper: " + ZK_SERVER);
                    connectedSignal.countDown();
                } else if (event.getState() == Event.KeeperState.Disconnected) {
                    System.out.println("Disconnected from ZooKeeper");
                } else if (event.getState() == Event.KeeperState.Expired) {
                    System.out.println("ZooKeeper session expired");
                    running = false;
                }
            }
        });
    }
    
    private void startScheduledMonitor() {
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            if (!running) {
                scheduler.shutdown();
                return;
            }
            
            try {
                monitorConfigChanges();
            } catch (Exception e) {
                System.err.println("Error monitoring config changes: " + e.getMessage());
            }
        }, 0, MONITOR_INTERVAL, TimeUnit.MILLISECONDS);
    }
    
    private void monitorConfigChanges() throws Exception {
        // Get all child nodes under config path
        List<String> configGroups = zk.getChildren(CONFIG_PATH, false);
        
        for (String groupName : configGroups) {
            String groupPath = CONFIG_PATH + "/" + groupName;
            List<String> configKeys = zk.getChildren(groupPath, false);
            
            for (String configKey : configKeys) {
                String configPath = groupPath + "/" + configKey;
                String localFilePath = LOCAL_CONFIG_DIR + "/" + groupName + "/" + configKey;
                
                // Check if config has been updated
                if (isConfigUpdated(configPath, localFilePath)) {
                    System.out.println("Config change detected: " + configPath);
                    pullConfigFromZooKeeper(configPath, localFilePath, groupName, configKey);
                }
            }
        }
    }
    
    private boolean isConfigUpdated(String zkPath, String localPath) throws Exception {
        File localFile = new File(localPath);
        if (!localFile.exists()) {
            return true; // Local file doesn't exist, need to pull
        }
        
        // Get ZooKeeper node modification time
        Stat stat = zk.exists(zkPath, false);
        if (stat == null) {
            return false; // Node doesn't exist
        }
        
        // Compare modification times
        long zkMtime = stat.getMtime();
        long localMtime = localFile.lastModified();
        
        return zkMtime > localMtime;
    }
    
    private void pullConfigFromZooKeeper(String zkPath, String localPath, String groupName, String configKey) {
        try {
            // Create local directory
            File localDir = new File(LOCAL_CONFIG_DIR + "/" + groupName);
            if (!localDir.exists()) {
                localDir.mkdirs();
            }
            
            // Get config data
            byte[] data = zk.getData(zkPath, false, null);
            String configValue = new String(data, StandardCharsets.UTF_8);
            
            // Write to local file
            try (FileWriter writer = new FileWriter(localPath)) {
                writer.write(configValue);
            }
            
            // Create metadata file
            String metaPath = localPath + ".meta";
            try (FileWriter writer = new FileWriter(metaPath)) {
                writer.write("groupName=" + groupName + "\n");
                writer.write("configKey=" + configKey + "\n");
                writer.write("zkPath=" + zkPath + "\n");
                writer.write("updateTime=" + System.currentTimeMillis() + "\n");
                writer.write("dataLength=" + data.length + "\n");
            }
            
            System.out.println("Config updated: " + localPath);
            System.out.println("Config value: " + configValue);
            
        } catch (Exception e) {
            System.err.println("Failed to pull config: " + zkPath + " - " + e.getMessage());
        }
    }
    
    private void createLocalConfigDir() {
        File configDir = new File(LOCAL_CONFIG_DIR);
        if (!configDir.exists()) {
            configDir.mkdirs();
            System.out.println("Created local config directory: " + LOCAL_CONFIG_DIR);
        }
    }
    
    public void stop() {
        running = false;
        
        if (scheduler != null) {
            scheduler.shutdown();
        }
        
        if (zk != null) {
            try {
                zk.close();
                System.out.println("ZooKeeper connection closed");
            } catch (InterruptedException e) {
                System.err.println("Error closing ZooKeeper connection: " + e.getMessage());
            }
        }
    }
    
    public static void main(String[] args) {
        ZooKeeperConfigMonitorEn monitor = new ZooKeeperConfigMonitorEn();
        
        try {
            monitor.start();
            
            // Keep program running
            while (monitor.running) {
                Thread.sleep(1000);
            }
            
        } catch (Exception e) {
            System.err.println("Program failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
} 