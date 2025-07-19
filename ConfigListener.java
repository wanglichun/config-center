import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ConfigListener {
    private static final String ZK_HOST = "config-center-zookeeper";
    private static final int ZK_PORT = 2181;
    private static final String INSTANCE_ID = "simple-container";
    
    public static void main(String[] args) {
        System.out.println("开始监听配置变更通知...");
        
        while (true) {
            try {
                // 检查通知节点
                String notifications = sendZkCommand("ls /config-center/notifications/demo-app/dev/common");
                if (notifications != null && !notifications.isEmpty()) {
                    String[] configKeys = notifications.split("\\s+");
                    
                    for (String configKey : configKeys) {
                        if (configKey.trim().isEmpty()) continue;
                        
                        // 检查是否有针对本实例的通知
                        String notificationPath = "/config-center/notifications/demo-app/dev/common/" + configKey + "/" + INSTANCE_ID;
                        String notificationData = sendZkCommand("get " + notificationPath);
                        
                        if (notificationData != null && !notificationData.contains("NoNodeException")) {
                            System.out.println("收到配置变更通知: " + configKey);
                            
                            // 获取配置值
                            String configPath = "/config-center/configs/demo-app/dev/common/" + configKey;
                            String configValue = sendZkCommand("get " + configPath);
                            
                            if (configValue != null && !configValue.contains("NoNodeException")) {
                                // 提取配置值（去掉ZooKeeper的元数据）
                                String[] lines = configValue.split("\n");
                                if (lines.length > 1) {
                                    String value = lines[1].trim();
                                    System.out.println("配置值: " + value);
                                    
                                    // 保存到文件
                                    try (FileWriter writer = new FileWriter("/tmp/" + configKey)) {
                                        writer.write(value);
                                    }
                                    System.out.println("配置已保存到: /tmp/" + configKey);
                                }
                            }
                            
                            // 删除通知节点
                            sendZkCommand("delete " + notificationPath);
                            System.out.println("已删除通知节点");
                        }
                    }
                }
                
                Thread.sleep(3000);
            } catch (Exception e) {
                System.err.println("错误: " + e.getMessage());
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ie) {
                    break;
                }
            }
        }
    }
    
    private static String sendZkCommand(String command) {
        try (Socket socket = new Socket(ZK_HOST, ZK_PORT)) {
            // 发送命令
            OutputStream out = socket.getOutputStream();
            out.write((command + "\n").getBytes(StandardCharsets.UTF_8));
            out.flush();
            
            // 读取响应
            InputStream in = socket.getInputStream();
            byte[] buffer = new byte[1024];
            int bytesRead = in.read(buffer);
            
            if (bytesRead > 0) {
                return new String(buffer, 0, bytesRead, StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            // 忽略连接错误
        }
        return null;
    }
} 