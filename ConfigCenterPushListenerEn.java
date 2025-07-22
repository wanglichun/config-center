import java.io.File;
import java.io.FileWriter;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

/**
 * Config Center Push Listener
 * Listen to config center push notifications and save config content to local
 */
public class ConfigCenterPushListenerEn {
    
    // Configuration parameters
    private static final String ZK_SERVER = System.getenv().getOrDefault("ZK_SERVER", "config-center-zookeeper:2181");
    private static final String NOTIFICATION_BASE_PATH = "/config-center/notifications";
    private static final String CONFIG_BASE_PATH = "/config-center/configs";
    private static final String LOCAL_CONFIG_DIR = System.getenv().getOrDefault("LOCAL_CONFIG_DIR", "./configs");
    private static final String INSTANCE_ID = System.getenv().getOrDefault("INSTANCE_ID", "machine-001");
    
    // Get IP address for notification listening
    private static final String INSTANCE_IP = getInstanceIp();
    
    private ZooKeeper zk;
    private CountDownLatch connectedSignal = new CountDownLatch(1);
    private AtomicBoolean running = new AtomicBoolean(true);
    
    /**
     * Get instance IP address
     */
    private static String getInstanceIp() {
        try {
            // Try to get IP from environment variable first
            String envIp = System.getenv("INSTANCE_IP");
            if (envIp != null && !envIp.trim().isEmpty()) {
                return envIp.trim();
            }
            
            // Get local IP address
            InetAddress localHost = InetAddress.getLocalHost();
            String ip = localHost.getHostAddress();
            System.out.println("Detected instance IP: " + ip);
            return ip;
        } catch (Exception e) {
            System.err.println("Failed to get instance IP: " + e.getMessage());
            // Fallback to INSTANCE_ID if IP detection fails
            return INSTANCE_ID;
        }
    }
    
    public void start() throws Exception {
        System.out.println("=== Config Center Push Listener Starting ===");
        System.out.println("ZooKeeper Server: " + ZK_SERVER);
        System.out.println("Instance ID: " + INSTANCE_ID);
        System.out.println("Local Config Dir: " + LOCAL_CONFIG_DIR);
        
        // Create local config directory
        createLocalConfigDir();
        
        // Connect to ZooKeeper
        connectToZooKeeper();
        
        // Wait for connection
        connectedSignal.await();
        System.out.println("Config center push listener started, listening for notifications...");
        
        // Start listening for notifications
        watchNotifications();
        
        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down config center push listener...");
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
    
    private void watchNotifications() throws Exception {
        while (running.get()) {
            try {
                // Check if notification base path exists
                Stat baseStat = zk.exists(NOTIFICATION_BASE_PATH, false);
                if (baseStat == null) {
                    System.out.println("Notification base path does not exist, waiting for creation...");
                    Thread.sleep(5000);
                    continue;
                }
                
                // Get all config groups
                List<String> groups = zk.getChildren(NOTIFICATION_BASE_PATH, false);
                
                for (String groupName : groups) {
                    String groupNotificationPath = NOTIFICATION_BASE_PATH + "/" + groupName;
                    
                    try {
                        // Get all config keys under this group
                        List<String> configKeys = zk.getChildren(groupNotificationPath, false);
                        
                        for (String configKey : configKeys) {
                            String configNotificationPath = groupNotificationPath + "/" + configKey;
                            
                            try {
                                // Check if there's a notification for this instance
                                String instanceNotificationPath = configNotificationPath + "/" + INSTANCE_IP;
                                Stat stat = zk.exists(instanceNotificationPath, false);
                                
                                if (stat != null) {
                                    System.out.println("Received config change notification: group=" + groupName + ", key=" + configKey);
                                    
                                    // Get notification data
                                    byte[] notificationData = zk.getData(instanceNotificationPath, false, null);
                                    String notificationJson = new String(notificationData, StandardCharsets.UTF_8);
                                    
                                    System.out.println("Notification data: " + notificationJson);
                                    
                                    // Parse notification data
                                    String configValue = parseNotificationData(notificationJson);
                                    
                                    // Save config to local
                                    String localFilePath = LOCAL_CONFIG_DIR + "/" + groupName + "/" + configKey;
                                    saveConfigToLocal(localFilePath, configValue, groupName, configKey);
                                    
                                    // Delete notification node
                                    zk.delete(instanceNotificationPath, -1);
                                    System.out.println("Processed and deleted notification node: " + instanceNotificationPath);
                                }
                            } catch (Exception e) {
                                // Ignore errors for individual config keys
                                System.err.println("Failed to process config key notification: " + configKey + " - " + e.getMessage());
                            }
                        }
                    } catch (Exception e) {
                        // Ignore errors for individual groups
                        System.err.println("Failed to process group notification: " + groupName + " - " + e.getMessage());
                    }
                }
                
                // Wait before next check
                Thread.sleep(3000);
                
            } catch (Exception e) {
                System.err.println("Error while listening for notifications: " + e.getMessage());
                Thread.sleep(5000);
            }
        }
    }
    
    private String parseNotificationData(String notificationJson) {
        try {
            // Simple JSON parsing to extract newValue field
            // Format: {"configKey":"xxx","newValue":"xxx","timestamp":xxx}
            int startIndex = notificationJson.indexOf("\"newValue\":\"") + 12;
            int endIndex = notificationJson.indexOf("\"", startIndex);
            if (startIndex > 11 && endIndex > startIndex) {
                return notificationJson.substring(startIndex, endIndex);
            }
        } catch (Exception e) {
            System.err.println("Failed to parse notification data: " + e.getMessage());
        }
        return "";
    }
    
    private void saveConfigToLocal(String localPath, String configValue, String groupName, String configKey) {
        try {
            // Create local directory
            File localDir = new File(LOCAL_CONFIG_DIR + "/" + groupName);
            if (!localDir.exists()) {
                localDir.mkdirs();
            }
            
            // Write config file
            try (FileWriter writer = new FileWriter(localPath)) {
                writer.write(configValue);
            }
            
            // Create metadata file
            String metaPath = localPath + ".meta";
            try (FileWriter writer = new FileWriter(metaPath)) {
                writer.write("groupName=" + groupName + "\n");
                writer.write("configKey=" + configKey + "\n");
                writer.write("instanceId=" + INSTANCE_ID + "\n");
                writer.write("updateTime=" + System.currentTimeMillis() + "\n");
                writer.write("dataLength=" + configValue.length() + "\n");
                writer.write("updateMethod=push\n");
            }
            
            System.out.println("Config saved to local: " + localPath);
            System.out.println("Config value: " + configValue);
            
        } catch (Exception e) {
            System.err.println("Failed to save config to local: " + localPath + " - " + e.getMessage());
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
        ConfigCenterPushListenerEn listener = new ConfigCenterPushListenerEn();
        
        try {
            listener.start();
        } catch (Exception e) {
            System.err.println("Program failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
} 