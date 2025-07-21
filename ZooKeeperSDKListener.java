import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import java.util.List;
import java.io.FileWriter;
import java.io.IOException;

public class ZooKeeperSDKListener {
    private static final String ZK_SERVER = "config-center-zookeeper:2181";
    private static final String NOTIFICATION_PATH = "/config-center/notifications/demo-app/dev/redis";
    private static final String CONFIG_PATH = "/config-center/configs/demo-app/dev/redis";
    private static final String INSTANCE_ID = "simple-container";
    
    private ZooKeeper zk;
    
    public void connect() throws Exception {
        zk = new ZooKeeper(ZK_SERVER, 30000, new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                // 处理连接事件
                if (event.getState() == Event.KeeperState.SyncConnected) {
                    System.out.println("已连接到ZooKeeper: " + ZK_SERVER);
                }
            }
        });
        
        // 等待连接建立
        while (zk.getState() != ZooKeeper.States.CONNECTED) {
            Thread.sleep(100);
        }
        System.out.println("已连接到ZooKeeper: " + ZK_SERVER);
    }
    
    public void watchNotifications() throws Exception {
        while (true) {
            try {
                // 检查通知节点
                List<String> notifications = zk.getChildren(NOTIFICATION_PATH, false);
                
                for (String configKey : notifications) {
                    String notificationPath = NOTIFICATION_PATH + "/" + configKey + "/" + INSTANCE_ID;
                    
                    try {
                        // 检查是否有针对本实例的通知
                        Stat stat = zk.exists(notificationPath, false);
                        if (stat != null) {
                            System.out.println("收到配置变更通知: " + configKey);
                            
                            // 获取配置值
                            String configPath = CONFIG_PATH + "/" + configKey;
                            byte[] data = zk.getData(configPath, false, null);
                            String configValue = new String(data, "UTF-8");
                            
                            System.out.println("配置值: " + configValue);
                            
                            // 保存到文件
                            try (FileWriter writer = new FileWriter("/tmp/" + configKey)) {
                                writer.write(configValue);
                            }
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
        ZooKeeperSDKListener client = new ZooKeeperSDKListener();
        try {
            client.connect();
            System.out.println("开始监听配置变更通知...");
            client.watchNotifications();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
} 