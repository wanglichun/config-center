import org.apache.zookeeper.*;
import java.util.List;

public class ZooKeeperClient {
    private static final String ZK_SERVER = "config-center-zookeeper:2181";
    private static final String NOTIFICATION_PATH = "/config-center/notifications/demo-app/dev/common";
    private static final String CONFIG_PATH = "/config-center/configs/demo-app/dev/common";
    private static final String INSTANCE_ID = "simple-container";
    
    private ZooKeeper zk;
    
    public void connect() throws Exception {
        zk = new ZooKeeper(ZK_SERVER, 30000, new Watcher() {
            public void process(WatchedEvent event) {
                // 处理事件
            }
        });
    }
    
    public void watchNotifications() throws Exception {
        while (true) {
            try {
                // 检查通知节点
                List<String> notifications = zk.getChildren(NOTIFICATION_PATH, false);
                
                for (String configKey : notifications) {
                    String notificationPath = NOTIFICATION_PATH + "/" + configKey + "/" + INSTANCE_ID;
                    
                    try {
                        // 尝试获取通知数据
                        byte[] notificationData = zk.getData(notificationPath, false, null);
                        if (notificationData != null) {
                            System.out.println("收到配置变更通知: " + configKey);
                            
                            // 获取配置值
                            String configPath = CONFIG_PATH + "/" + configKey;
                            byte[] data = zk.getData(configPath, false, null);
                            String configValue = new String(data);
                            
                            System.out.println("配置值: " + configValue);
                            
                            // 保存到文件
                            java.nio.file.Files.write(
                                java.nio.file.Paths.get("/tmp/" + configKey), 
                                configValue.getBytes()
                            );
                            System.out.println("配置已保存到: /tmp/" + configKey);
                            
                            // 删除通知节点
                            zk.delete(notificationPath, -1);
                            System.out.println("已删除通知节点");
                        }
                    } catch (Exception e) {
                        // 忽略错误
                    }
                }
                
                Thread.sleep(3000);
            } catch (Exception e) {
                System.err.println("错误: " + e.getMessage());
                Thread.sleep(5000);
            }
        }
    }
    
    public void close() throws Exception {
        if (zk != null) {
            zk.close();
        }
    }
    
    public static void main(String[] args) {
        ZooKeeperClient client = new ZooKeeperClient();
        try {
            client.connect();
            System.out.println("开始监听配置变更通知...");
            client.watchNotifications();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
} 