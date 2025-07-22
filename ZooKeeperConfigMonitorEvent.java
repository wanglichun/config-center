import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

/**
 * ZooKeeper Configuration Monitor with Event Notification
 * Monitor configuration changes using ZooKeeper Watchers
 */
public class ZooKeeperConfigMonitorEvent {
    
    // Configuration parameters
    private static final String ZK_SERVER = System.getenv().getOrDefault("ZK_SERVER", "config-center-zookeeper:2181");
    private static final String CONFIG_PATH = System.getenv().getOrDefault("CONFIG_PATH", "/config-center/configs");
    private static final String LOCAL_CONFIG_DIR = System.getenv().getOrDefault("LOCAL_CONFIG_DIR", "./configs");
    
    private ZooKeeper zk;
    private CountDownLatch connectedSignal = new CountDownLatch(1);
    private AtomicBoolean running = new AtomicBoolean(true);
    
    public void start() throws Exception {
        System.out.println("=== ZooKeeper Configuration Monitor (Event-based) Starting ===");
        System.out.println("ZooKeeper Server: " + ZK_SERVER);
        System.out.println("Config Path: " + CONFIG_PATH);
        System.out.println("Local Config Dir: " + LOCAL_CONFIG_DIR);
        
        // Create local config directory
        createLocalConfigDir();
        
        // Connect to ZooKeeper
        connectToZooKeeper();
        
        // Wait for connection
        connectedSignal.await();
        System.out.println("Configuration monitor started, monitoring config changes...");
        
        // Initial pull of all configs
        pullAllConfigs();
        
        // Set up watchers for all config paths
        setupWatchers();
        
        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down configuration monitor...");
            stop();
        }));
        
        // Keep program running
        while (running.get()) {
            Thread.sleep(1000);
        }
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
                    running.set(false);
                }
            }
        });
    }
    
    private void pullAllConfigs() throws Exception {
        System.out.println("Pulling all existing configs...");
        
        // Get all child nodes under config path
        List<String> configGroups = zk.getChildren(CONFIG_PATH, false);
        
        for (String groupName : configGroups) {
            String groupPath = CONFIG_PATH + "/" + groupName;
            List<String> configKeys = zk.getChildren(groupPath, false);
            
            for (String configKey : configKeys) {
                String configPath = groupPath + "/" + configKey;
                String localFilePath = LOCAL_CONFIG_DIR + "/" + groupName + "/" + configKey;
                
                pullConfigFromZooKeeper(configPath, localFilePath, groupName, configKey);
            }
        }
    }
    
    private void setupWatchers() throws Exception {
        System.out.println("Setting up watchers for config changes...");
        
        // Set up watcher for config groups
        setupGroupWatcher();
        
        // Set up watchers for existing configs
        List<String> configGroups = zk.getChildren(CONFIG_PATH, new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                if (event.getType() == Event.EventType.NodeChildrenChanged) {
                    System.out.println("Config group structure changed: " + event.getPath());
                    try {
                        // Re-setup watchers when group structure changes
                        setupWatchers();
                    } catch (Exception e) {
                        System.err.println("Error re-setting up watchers: " + e.getMessage());
                    }
                }
            }
        });
        
        for (String groupName : configGroups) {
            String groupPath = CONFIG_PATH + "/" + groupName;
            setupConfigWatchers(groupPath, groupName);
        }
    }
    
    private void setupGroupWatcher() throws Exception {
        zk.getChildren(CONFIG_PATH, new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                if (event.getType() == Event.EventType.NodeChildrenChanged) {
                    System.out.println("Config group structure changed: " + event.getPath());
                    try {
                        // Re-setup watchers when group structure changes
                        setupWatchers();
                    } catch (Exception e) {
                        System.err.println("Error re-setting up watchers: " + e.getMessage());
                    }
                }
            }
        });
    }
    
    private void setupConfigWatchers(String groupPath, String groupName) throws Exception {
        List<String> configKeys = zk.getChildren(groupPath, new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                if (event.getType() == Event.EventType.NodeChildrenChanged) {
                    System.out.println("Config keys changed in group: " + event.getPath());
                    try {
                        // Re-setup watchers for this group
                        setupConfigWatchers(event.getPath(), groupName);
                    } catch (Exception e) {
                        System.err.println("Error re-setting up config watchers: " + e.getMessage());
                    }
                }
            }
        });
        
        for (String configKey : configKeys) {
            String configPath = groupPath + "/" + configKey;
            setupConfigValueWatcher(configPath, groupName, configKey);
        }
    }
    
    private void setupConfigValueWatcher(String configPath, String groupName, String configKey) throws Exception {
        // Set up watcher for config value changes
        zk.getData(configPath, new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                if (event.getType() == Event.EventType.NodeDataChanged) {
                    System.out.println("Config value changed: " + event.getPath());
                    try {
                        String localFilePath = LOCAL_CONFIG_DIR + "/" + groupName + "/" + configKey;
                        pullConfigFromZooKeeper(event.getPath(), localFilePath, groupName, configKey);
                        
                        // Re-setup watcher for this config
                        setupConfigValueWatcher(event.getPath(), groupName, configKey);
                    } catch (Exception e) {
                        System.err.println("Error handling config change: " + e.getMessage());
                    }
                }
            }
        }, null);
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
                writer.write("updateMethod=event\n");
            }
            
            System.out.println("Config updated via event: " + localPath);
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
        running.set(false);
        
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
        ZooKeeperConfigMonitorEvent monitor = new ZooKeeperConfigMonitorEvent();
        
        try {
            monitor.start();
        } catch (Exception e) {
            System.err.println("Program failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
} 