import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.WatchedEvent;
import java.util.List;

public class ZooKeeperConfigViewer {
    private static ZooKeeper zk;
    private static final String ZK_HOST = "172.18.0.2:2181";
    private static final int SESSION_TIMEOUT = 5000;

    public static void main(String[] args) {
        try {
            // 连接ZooKeeper
            zk = new ZooKeeper(ZK_HOST, SESSION_TIMEOUT, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    System.out.println("ZooKeeper事件: " + event.getType());
                }
            });

            // 等待连接建立
            Thread.sleep(2000);

            System.out.println("=== ZooKeeper配置查看器 ===");
            System.out.println("ZooKeeper连接状态: " + zk.getState());

            // 查看根节点
            System.out.println("\n1. 根节点列表:");
            List<String> rootNodes = zk.getChildren("/", false);
            for (String node : rootNodes) {
                System.out.println("  /" + node);
            }

            // 查看config-center节点
            System.out.println("\n2. config-center节点:");
            try {
                List<String> configNodes = zk.getChildren("/config-center", false);
                for (String node : configNodes) {
                    System.out.println("  /config-center/" + node);
                }
            } catch (Exception e) {
                System.out.println("  /config-center 节点不存在");
            }

            // 查看configs节点
            System.out.println("\n3. configs节点:");
            try {
                List<String> configsNodes = zk.getChildren("/config-center/configs", false);
                for (String node : configsNodes) {
                    System.out.println("  /config-center/configs/" + node);
                }
            } catch (Exception e) {
                System.out.println("  /config-center/configs 节点不存在");
            }

            // 查看dev环境配置
            System.out.println("\n4. dev环境配置:");
            try {
                List<String> devNodes = zk.getChildren("/config-center/configs/dev", false);
                for (String node : devNodes) {
                    System.out.println("  /config-center/configs/dev/" + node);
                }
            } catch (Exception e) {
                System.out.println("  /config-center/configs/dev 节点不存在");
            }

            // 查看common组配置
            System.out.println("\n5. common组配置:");
            try {
                List<String> commonNodes = zk.getChildren("/config-center/configs/dev/common", false);
                for (String node : commonNodes) {
                    System.out.println("  /config-center/configs/dev/common/" + node);
                }
            } catch (Exception e) {
                System.out.println("  /config-center/configs/dev/common 节点不存在");
            }

            // 获取debug.enabled的值
            System.out.println("\n6. debug.enabled配置值:");
            try {
                byte[] data = zk.getData("/config-center/configs/dev/common/debug.enabled", false, null);
                if (data != null) {
                    String value = new String(data);
                    System.out.println("  debug.enabled = " + value);
                } else {
                    System.out.println("  debug.enabled 值为空");
                }
            } catch (Exception e) {
                System.out.println("  无法获取 debug.enabled: " + e.getMessage());
            }

            // 查看namespaces
            System.out.println("\n7. namespaces:");
            try {
                List<String> namespaceNodes = zk.getChildren("/config-center/namespaces", false);
                for (String node : namespaceNodes) {
                    System.out.println("  /config-center/namespaces/" + node);
                    try {
                        byte[] data = zk.getData("/config-center/namespaces/" + node, false, null);
                        if (data != null) {
                            String value = new String(data);
                            System.out.println("    值: " + value);
                        }
                    } catch (Exception e) {
                        System.out.println("    无法获取值: " + e.getMessage());
                    }
                }
            } catch (Exception e) {
                System.out.println("  /config-center/namespaces 节点不存在");
            }

            zk.close();
            System.out.println("\n=== 查看完成 ===");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
} 