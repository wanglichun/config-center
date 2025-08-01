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
 * Config Center Push Listener for Machine-003
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
    private static final String INSTANCE_ID = System.getenv().getOrDefault("INSTANCE_ID", "machine-003");

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
            System.out.println("Starting Config Center Push Listener for Machine-003...");
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
                if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    System.out.println("Connected to ZooKeeper successfully");
                    connectedSignal.countDown();
                }
            }
        });
        connectedSignal.await();
        System.out.println("ZooKeeper connection established");
    }

    /**
     * Watch notifications
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
            for (String group : groups) {
                watchGroupNotifications(group);
            }
        } catch (Exception e) {
            System.err.println("Error watching notifications: " + e.getMessage());
        }
    }

    /**
     * Watch group notifications
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

            // Watch each config key
            for (String configKey : configKeys) {
                watchConfigNotifications(group, configKey);
            }
        } catch (Exception e) {
            System.err.println("Error watching group notifications: " + e.getMessage());
        }
    }

    /**
     * Watch config notifications
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

            // Process each instance notification
            for (String instance : instances) {
                processNotification(group, configKey, instance);
            }
        } catch (Exception e) {
            System.err.println("Error watching config notifications: " + e.getMessage());
        }
    }

    /**
     * Process notification
     */
    private void processNotification(String group, String configKey, String instance) {
        try {
            String notificationPath = NOTIFICATION_BASE_PATH + "/" + group + "/" + configKey + "/" + instance;
            byte[] data = zk.getData(notificationPath, false, null);
            String notificationJson = new String(data, StandardCharsets.UTF_8);

            System.out.println("Received notification for group: " + group + ", config: " + configKey + ", instance: " + instance);

            // Parse notification data
            String configValue = parseNotificationData(notificationJson);
            long version = getVersionFromJson(notificationJson);

            // Save config to local
            String filePath = LOCAL_CONFIG_DIR + "/" + group + "/" + configKey + ".properties";
            saveConfigToLocal(filePath, configValue, group, configKey);

            // Report config status
            reportConfigStatus(group, configKey, configValue, version);

        } catch (Exception e) {
            System.err.println("Error processing notification: " + e.getMessage());
        }
    }

    /**
     * Parse notification data
     */
    private String parseNotificationData(String notificationJson) {
        // Simple JSON parsing for config value
        // In real implementation, use proper JSON library
        try {
            if (notificationJson.contains("\"value\":")) {
                int start = notificationJson.indexOf("\"value\":") + 9;
                int end = notificationJson.indexOf("\"", start);
                if (end == -1) {
                    end = notificationJson.indexOf("}", start);
                }
                return notificationJson.substring(start, end).replace("\"", "");
            }
            return notificationJson;
        } catch (Exception e) {
            System.err.println("Error parsing notification data: " + e.getMessage());
            return notificationJson;
        }
    }

    /**
     * Get version from JSON
     */
    private long getVersionFromJson(String notificationJson) {
        try {
            if (notificationJson.contains("\"version\":")) {
                int start = notificationJson.indexOf("\"version\":") + 11;
                int end = notificationJson.indexOf(",", start);
                if (end == -1) {
                    end = notificationJson.indexOf("}", start);
                }
                return Long.parseLong(notificationJson.substring(start, end).trim());
            }
            return System.currentTimeMillis();
        } catch (Exception e) {
            System.err.println("Error parsing version: " + e.getMessage());
            return System.currentTimeMillis();
        }
    }

    /**
     * Save config to local file
     */
    private void saveConfigToLocal(String filePath, String configValue, String groupName, String configKey) {
        try {
            // Create parent directories
            File file = new File(filePath);
            file.getParentFile().mkdirs();

            // Write config to file
            try (FileWriter writer = new FileWriter(file)) {
                writer.write("# Config for " + groupName + "/" + configKey + "\n");
                writer.write("# Generated at: " + System.currentTimeMillis() + "\n");
                writer.write(configKey + "=" + configValue + "\n");
            }

            System.out.println("Config saved to: " + filePath);
        } catch (Exception e) {
            System.err.println("Error saving config to local: " + e.getMessage());
        }
    }

    /**
     * Report config status to ZooKeeper
     */
    private void reportConfigStatus(String groupName, String configKey, String configValue, long version) {
        try {
            String statusPath = CONFIG_STATUS_BASE_PATH + "/" + groupName + "/" + configKey + "/" + INSTANCE_ID;
            String statusData = createStatusData(groupName, configKey, configValue, version);

            // Create parent directories
            createParentDirectories(statusPath);

            // Set status data
            zk.setData(statusPath, statusData.getBytes(StandardCharsets.UTF_8), -1);

            System.out.println("Config status reported: " + statusPath);
        } catch (Exception e) {
            System.err.println("Error reporting config status: " + e.getMessage());
        }
    }

    /**
     * Create status data JSON
     */
    private String createStatusData(String groupName, String configKey, String configValue, long version) {
        return String.format(
            "{\"instanceId\":\"%s\",\"instanceIp\":\"%s\",\"group\":\"%s\",\"configKey\":\"%s\",\"version\":%d,\"timestamp\":%d,\"status\":\"updated\"}",
            INSTANCE_ID, INSTANCE_IP, groupName, configKey, version, System.currentTimeMillis()
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
                    try {
                        zk.create(currentPath, new byte[0], null, null);
                    } catch (Exception e) {
                        // Node already exists, ignore
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
                System.out.println("ZooKeeper connection closed");
            }
        } catch (Exception e) {
            System.err.println("Error closing ZooKeeper: " + e.getMessage());
        }
    }
} 