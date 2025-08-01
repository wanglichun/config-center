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
            return "unknown";
        }
    }

    /**
     * Main method
     */
    public static void main(String[] args) {
        ConfigCenterPushListenerEn listener = new ConfigCenterPushListenerEn();
        listener.start();
    }

    /**
     * Start the listener
     */
    public void start() {
        try {
            System.out.println("Starting Config Center Push Listener...");
            System.out.println("Instance ID: " + INSTANCE_ID);
            System.out.println("Instance IP: " + INSTANCE_IP);
            System.out.println("ZooKeeper Server: " + ZK_SERVER);
            System.out.println("Local Config Dir: " + LOCAL_CONFIG_DIR);

            // Connect to ZooKeeper
            connectToZooKeeper();

            // Watch the notifications base path
            watchNotifications();

            // Keep running
            while (running.get()) {
                Thread.sleep(1000);
            }

        } catch (Exception e) {
            System.err.println("Error in listener: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeZooKeeper();
        }
    }

    /**
     * Connect to ZooKeeper
     */
    private void connectToZooKeeper() throws Exception {
        zk = new ZooKeeper(ZK_SERVER, 30000, new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                System.out.println("ZooKeeper Event: " + event.getType() + " - " + event.getState());
                
                if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    System.out.println("Connected to ZooKeeper: " + ZK_SERVER);
                    connectedSignal.countDown();
                } else if (event.getState() == Watcher.Event.KeeperState.Disconnected) {
                    System.out.println("Disconnected from ZooKeeper");
                } else if (event.getState() == Watcher.Event.KeeperState.Expired) {
                    System.out.println("ZooKeeper session expired, reconnecting...");
                    try {
                        connectToZooKeeper();
                        watchNotifications();
                    } catch (Exception e) {
                        System.err.println("Failed to reconnect: " + e.getMessage());
                    }
                }
            }
        });

        connectedSignal.await();
    }

    /**
     * Watch notifications recursively
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

            // Process each group
            for (String group : groups) {
                watchGroupNotifications(group);
            }

        } catch (Exception e) {
            System.err.println("Error watching notifications: " + e.getMessage());
        }
    }

    /**
     * Watch notifications for a specific group
     */
    private void watchGroupNotifications(String group) {
        try {
            String groupPath = NOTIFICATION_BASE_PATH + "/" + group;
            List<String> configKeys = zk.getChildren(groupPath, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if (event.getType() == Watcher.Event.EventType.NodeChildrenChanged) {
                        watchGroupNotifications(group);
                    }
                }
            });

            // Process each config key
            for (String configKey : configKeys) {
                watchConfigNotifications(group, configKey);
            }

        } catch (Exception e) {
            System.err.println("Error watching group notifications: " + e.getMessage());
        }
    }

    /**
     * Watch notifications for a specific config
     */
    private void watchConfigNotifications(String group, String configKey) {
        try {
            String configPath = NOTIFICATION_BASE_PATH + "/" + group + "/" + configKey;
            List<String> instances = zk.getChildren(configPath, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if (event.getType() == Watcher.Event.EventType.NodeChildrenChanged) {
                        watchConfigNotifications(group, configKey);
                    }
                }
            });

            // Process each instance
            for (String instance : instances) {
                if (instance.equals(INSTANCE_IP)) {
                    processNotification(group, configKey, instance);
                }
            }

        } catch (Exception e) {
            System.err.println("Error watching config notifications: " + e.getMessage());
        }
    }

    /**
     * Process a notification
     */
    private void processNotification(String group, String configKey, String instance) {
        try {
            System.out.println("[NOTIFICATION] Processing notification: " + group + "/" + configKey + "/" + instance);

            // Get notification data
            String notificationPath = NOTIFICATION_BASE_PATH + "/" + group + "/" + configKey + "/" + instance;
            Stat stat = zk.exists(notificationPath, false);
            if (stat != null) {
                byte[] data = zk.getData(notificationPath, false, stat);
                String notificationJson = new String(data, StandardCharsets.UTF_8);
                System.out.println("[DATA] Notification data: " + notificationJson);

                // Parse notification data
                String configValue = parseNotificationData(notificationJson);
                System.out.println("[PARSE] Parsed config value: " + configValue + ", version: " + getVersionFromJson(notificationJson));

                // Save config to local file
                String localFilePath = LOCAL_CONFIG_DIR + "/" + group + "/" + configKey;
                saveConfigToLocal(localFilePath, configValue, group, configKey);

                // Report config status
                reportConfigStatus(group, configKey, configValue, getVersionFromJson(notificationJson));

                // Delete notification node
                System.out.println("[DELETE] Deleting notification node: " + notificationPath);
                zk.delete(notificationPath, -1);
                System.out.println("[SUCCESS] Successfully processed and deleted notification node: " + notificationPath);
            }
        } catch (Exception e) {
            System.err.println("Error processing notification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Parse notification data - treat newValue as complete string
     */
    private String parseNotificationData(String notificationJson) {
        try {
            // Parse new JSON format: {"configKey":"xxx","newValue":"xxx","version":xxx}
            // Treat newValue as a complete string, don't try to parse it further
            String newValueStart = "\"newValue\":\"";
            String newValueEnd = "\",\"version\"";
            
            int startIndex = notificationJson.indexOf(newValueStart);
            if (startIndex != -1) {
                startIndex += newValueStart.length();
                int endIndex = notificationJson.indexOf(newValueEnd, startIndex);
                if (endIndex != -1) {
                    String newValue = notificationJson.substring(startIndex, endIndex);
                    System.out.println("[PARSE] Parsed newValue: " + newValue);
                    return newValue;
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
     * Get version from JSON
     */
    private long getVersionFromJson(String notificationJson) {
        try {
            String versionStart = "\"version\":";
            String versionEnd = "}";
            int versionStartIndex = notificationJson.indexOf(versionStart);
            if (versionStartIndex != -1) {
                versionStartIndex += versionStart.length();
                int versionEndIndex = notificationJson.indexOf(versionEnd, versionStartIndex);
                if (versionEndIndex != -1) {
                    String versionStr = notificationJson.substring(versionStartIndex, versionEndIndex);
                    return Long.parseLong(versionStr);
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing version: " + e.getMessage());
        }
        return System.currentTimeMillis();
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
            try (FileWriter writer = new FileWriter(filePath)) {
                writer.write(configValue);
            }

            System.out.println("Config saved to local: " + filePath);
            System.out.println("Config value: " + configValue);
        } catch (Exception e) {
            System.err.println("Error saving config to local: " + e.getMessage());
        }
    }

    /**
     * Report config status to ZooKeeper
     */
    private void reportConfigStatus(String groupName, String configKey, String configValue, long version) {
        try {
            System.out.println("=== Starting config status report ===");
            System.out.println("Group: " + groupName + ", Config: " + configKey + ", Value: " + configValue);

            // Create status path
            String statusPath = CONFIG_STATUS_BASE_PATH + "/" + groupName + "/" + configKey + "/" + INSTANCE_IP;

            // Create status data
            String statusData = createStatusData(groupName, configKey, configValue, version);

            // Ensure parent directories exist
            createParentDirectories(statusPath);

            // Create or update status node
            Stat stat = zk.exists(statusPath, false);
            if (stat != null) {
                System.out.println("Updating existing status node...");
                zk.setData(statusPath, statusData.getBytes(StandardCharsets.UTF_8), -1);
            } else {
                System.out.println("Creating new status node...");
                zk.create(statusPath, statusData.getBytes(StandardCharsets.UTF_8), 
                         org.apache.zookeeper.ZooDefs.Ids.OPEN_ACL_UNSAFE, 
                         org.apache.zookeeper.CreateMode.EPHEMERAL);
            }

            System.out.println("[SUCCESS] Successfully updated config status node: " + statusPath);
            System.out.println("=== Config status report completed ===");

        } catch (Exception e) {
            System.err.println("Error reporting config status: " + e.getMessage());
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

    /**
     * Create parent directories in ZooKeeper
     */
    private void createParentDirectories(String path) {
        try {
            String[] parts = path.split("/");
            String currentPath = "";
            
            for (String part : parts) {
                if (!part.isEmpty()) {
                    currentPath += "/" + part;
                    Stat stat = zk.exists(currentPath, false);
                    if (stat == null) {
                        System.out.println("Creating directory: " + currentPath);
                        zk.create(currentPath, new byte[0], 
                                 org.apache.zookeeper.ZooDefs.Ids.OPEN_ACL_UNSAFE, 
                                 org.apache.zookeeper.CreateMode.PERSISTENT);
                    } else {
                        System.out.println("Directory already exists: " + currentPath);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error creating parent directories: " + e.getMessage());
        }
    }

    /**
     * Close ZooKeeper connection
     */
    private void closeZooKeeper() {
        try {
            if (zk != null) {
                zk.close();
            }
        } catch (Exception e) {
            System.err.println("Error closing ZooKeeper: " + e.getMessage());
        }
    }
} 