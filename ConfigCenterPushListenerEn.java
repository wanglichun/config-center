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
 * Then report current config status to ZooKeeper
 */
public class ConfigCenterPushListenerEn {

    // Configuration parameters
    private static final String ZK_SERVER = System.getenv().getOrDefault("ZK_SERVER", "config-center-zookeeper:2181");
    private static final String NOTIFICATION_BASE_PATH = "/config-center/notifications";
    private static final String CONFIG_BASE_PATH = "/config-center/configs";
    private static final String CONFIG_STATUS_BASE_PATH = "/config-center/container-status";
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

    public static void main(String[] args) {
        ConfigCenterPushListenerEn listener = new ConfigCenterPushListenerEn();
        listener.start();
    }

    public void start() {
        System.out.println("=== Config Center Push Listener Starting ===");
        System.out.println("ZooKeeper Server: " + ZK_SERVER);
        System.out.println("Instance ID: " + INSTANCE_ID);
        System.out.println("Instance IP: " + INSTANCE_IP);
        System.out.println("Local Config Dir: " + LOCAL_CONFIG_DIR);

        try {
            // Connect to ZooKeeper
            zk = new ZooKeeper(ZK_SERVER, 5000, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    System.out.println("ZooKeeper Event: " + event.getType() + " - " + event.getState());
                    if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                        System.out.println("Connected to ZooKeeper: " + ZK_SERVER);
                        connectedSignal.countDown();
                    }
                }
            });

            // Wait for connection
            connectedSignal.await();
            System.out.println("Config center push listener started, listening for notifications...");

            // Start watching notifications
            watchNotifications();

            // Keep running
            while (running.get()) {
                Thread.sleep(1000);
            }

        } catch (Exception e) {
            System.err.println("Error in config center push listener: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (zk != null) {
                try {
                    zk.close();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    /**
     * Watch for config change notifications
     */
    private void watchNotifications() {
        try {
            // Watch the notifications base path
            List<String> groups = zk.getChildren(NOTIFICATION_BASE_PATH, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if (event.getType() == Watcher.Event.EventType.NodeChildrenChanged) {
                        watchNotifications();
                    }
                }
            });

            // Watch each group
            for (String groupName : groups) {
                watchGroupNotifications(groupName);
            }

        } catch (Exception e) {
            System.err.println("Error watching notifications: " + e.getMessage());
        }
    }

    /**
     * Watch notifications for a specific group
     */
    private void watchGroupNotifications(String groupName) {
        try {
            String groupPath = NOTIFICATION_BASE_PATH + "/" + groupName;
            List<String> configKeys = zk.getChildren(groupPath, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if (event.getType() == Watcher.Event.EventType.NodeChildrenChanged) {
                        watchGroupNotifications(groupName);
                    }
                }
            });

            // Watch each config key
            for (String configKey : configKeys) {
                watchConfigNotifications(groupName, configKey);
            }

        } catch (Exception e) {
            System.err.println("Error watching group notifications: " + e.getMessage());
        }
    }

    /**
     * Watch notifications for a specific config
     */
    private void watchConfigNotifications(String groupName, String configKey) {
        try {
            String configNotificationPath = NOTIFICATION_BASE_PATH + "/" + groupName + "/" + configKey;
            List<String> instances = zk.getChildren(configNotificationPath, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if (event.getType() == Watcher.Event.EventType.NodeChildrenChanged) {
                        watchConfigNotifications(groupName, configKey);
                    }
                }
            });

            // Check if there's a notification for this instance
            String instanceNotificationPath = configNotificationPath + "/" + INSTANCE_IP;
            Stat stat = zk.exists(instanceNotificationPath, false);
            
            if (stat != null) {
                // Process the notification
                processNotification(groupName, configKey, instanceNotificationPath);
            }

        } catch (Exception e) {
            System.err.println("Error watching config notifications: " + e.getMessage());
        }
    }

    /**
     * Process a config change notification
     */
    private void processNotification(String groupName, String configKey, String notificationPath) {
        try {
            System.out.println("[NOTIFICATION] Received config change notification: group=" + groupName + ", key=" + configKey);

            // Get notification data
            byte[] notificationData = zk.getData(notificationPath, false, null);
            String notificationJson = new String(notificationData, StandardCharsets.UTF_8);
            System.out.println("[DATA] Notification data: " + notificationJson);

            // Parse notification data
            String configValue = parseNotificationData(notificationJson);
            long version = parseNotificationDataVersion(notificationJson);
            System.out.println("[PARSE] Parsed config value: " + configValue + ", version: " + version);

            // Save config to local
            String localFilePath = LOCAL_CONFIG_DIR + "/" + groupName + "/" + configKey;
            System.out.println("[SAVE] Saving config to local file: " + localFilePath);
            saveConfigToLocal(localFilePath, configValue, groupName, configKey);

            // Report current config status to ZooKeeper
            System.out.println("[REPORT] Starting status report to ZooKeeper...");
            reportConfigStatus(groupName, configKey, configValue, version);

            // Delete the notification node
            System.out.println("[DELETE] Deleting notification node: " + notificationPath);
            zk.delete(notificationPath, -1);
            System.out.println("[SUCCESS] Successfully processed and deleted notification node: " + notificationPath);

        } catch (Exception e) {
            System.err.println("[ERROR] Error processing notification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Parse notification data to extract config value and version
     */
    private String parseNotificationData(String notificationJson) {
        try {
            // Parse new JSON format: {"configKey":"xxx","newValue":"xxx","version":xxx}
            String newValueStart = "\"newValue\":\"";
            String newValueEnd = "\",\"version\"";
            
            int startIndex = notificationJson.indexOf(newValueStart);
            if (startIndex != -1) {
                startIndex += newValueStart.length();
                int endIndex = notificationJson.indexOf(newValueEnd, startIndex);
                if (endIndex != -1) {
                    String configValue = notificationJson.substring(startIndex, endIndex);
                    
                    // Also extract version for logging
                    String versionStart = "\"version\":";
                    String versionEnd = "}";
                    int versionStartIndex = notificationJson.indexOf(versionStart);
                    if (versionStartIndex != -1) {
                        versionStartIndex += versionStart.length();
                        int versionEndIndex = notificationJson.indexOf(versionEnd, versionStartIndex);
                        if (versionEndIndex != -1) {
                            String versionStr = notificationJson.substring(versionStartIndex, versionEndIndex);
                            try {
                                long version = Long.parseLong(versionStr);
                                System.out.println("[PARSE] Parsed config value: " + configValue + ", version: " + version);
                            } catch (NumberFormatException e) {
                                System.out.println("[PARSE] Parsed config value: " + configValue + ", version: unknown");
                            }
                        }
                    }
                    
                    return configValue;
                }
            }
            
            // Fallback: return the whole JSON if parsing fails
            System.out.println("[PARSE] Failed to parse notification data, using raw JSON");
            return notificationJson;
        } catch (Exception e) {
            System.err.println("[ERROR] Error parsing notification data: " + e.getMessage());
            return notificationJson;
        }
    }
    /**
     * Parse notification data to extract version
     */
    private long parseNotificationDataVersion(String notificationJson) {
        try {
            // Parse version from JSON format: {"configKey":"xxx","newValue":"xxx","version":xxx}
            String versionStart = "\"version\":";
            String versionEnd = "}";
            
            int startIndex = notificationJson.indexOf(versionStart);
            if (startIndex != -1) {
                startIndex += versionStart.length();
                int endIndex = notificationJson.indexOf(versionEnd, startIndex);
                if (endIndex != -1) {
                    String versionStr = notificationJson.substring(startIndex, endIndex);
                    try {
                        return Long.parseLong(versionStr);
                    } catch (NumberFormatException e) {
                        System.err.println("[ERROR] Failed to parse version number: " + versionStr);
                    }
                }
            }
            
            // Fallback: return 0 if parsing fails
            return 0;
        } catch (Exception e) {
            System.err.println("[ERROR] Error parsing version from notification data: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Save config to local file
     */
    private void saveConfigToLocal(String filePath, String configValue, String groupName, String configKey) {
        try {
            // Create directory if not exists
            File file = new File(filePath);
            File parentDir = file.getParentFile();
            if (!parentDir.exists()) {
                parentDir.mkdirs();
            }

            // Write config value to file
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(configValue);
            }

            System.out.println("Config saved to local: " + filePath);
            System.out.println("Config value: " + configValue);

        } catch (Exception e) {
            System.err.println("Error saving config to local: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Report current config status to ZooKeeper
     */
    private void reportConfigStatus(String groupName, String configKey, String configValue, long version) {
        try {
            System.out.println("=== Starting config status report ===");
            System.out.println("Group: " + groupName + ", Config: " + configKey + ", Value: " + configValue);
            
            // Create status path: /config-center/container-status/{groupName}/{configKey}/{instanceIp}
            String statusPath = CONFIG_STATUS_BASE_PATH + "/" + groupName + "/" + configKey + "/" + INSTANCE_IP;
            System.out.println("Status path: " + statusPath);
            
            // Create status data
            String statusData = createStatusData(groupName, configKey, configValue, version);
            System.out.println("Status data: " + statusData);
            
            // Create parent directories if they don't exist
            createParentDirectories(statusPath);
            
            // Create or update status node
            Stat stat = zk.exists(statusPath, false);
            if (stat == null) {
                // Create new status node
                System.out.println("Creating new status node...");
                zk.create(statusPath, statusData.getBytes(StandardCharsets.UTF_8), 
                         org.apache.zookeeper.ZooDefs.Ids.OPEN_ACL_UNSAFE,
                         org.apache.zookeeper.CreateMode.PERSISTENT);
                System.out.println("[SUCCESS] Successfully created config status node: " + statusPath);
            } else {
                // Update existing status node
                System.out.println("Updating existing status node...");
                zk.setData(statusPath, statusData.getBytes(StandardCharsets.UTF_8), -1);
                System.out.println("[SUCCESS] Successfully updated config status node: " + statusPath);
            }
            
            System.out.println("=== Config status report completed ===");

        } catch (Exception e) {
            System.err.println("[ERROR] Error reporting config status: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Create parent directories for status path
     */
    private void createParentDirectories(String statusPath) {
        try {
            String[] pathParts = statusPath.split("/");
            String currentPath = "";
            
            for (String part : pathParts) {
                if (part.isEmpty()) continue;
                
                currentPath += "/" + part;
                System.out.println("Checking path: " + currentPath);
                
                Stat stat = zk.exists(currentPath, false);
                if (stat == null) {
                    System.out.println("Creating directory: " + currentPath);
                    zk.create(currentPath, "".getBytes(StandardCharsets.UTF_8), 
                             org.apache.zookeeper.ZooDefs.Ids.OPEN_ACL_UNSAFE,
                             org.apache.zookeeper.CreateMode.PERSISTENT);
                    System.out.println("[SUCCESS] Created directory: " + currentPath);
                } else {
                    System.out.println("Directory already exists: " + currentPath);
                }
            }
        } catch (Exception e) {
            System.err.println("Error creating parent directories: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Create status data JSON
     */
    private String createStatusData(String groupName, String configKey, String configValue, long version) {
        long timestamp = System.currentTimeMillis();
        return String.format(
            "{\"ip\":\"%s\",\"configValue\":\"%s\",\"version\":%d,\"lastUpdateTime\":%d,\"status\":\"Running\"}",
            INSTANCE_IP, configValue, version, timestamp
        );
    }

    public void stop() {
        running.set(false);
    }
} 