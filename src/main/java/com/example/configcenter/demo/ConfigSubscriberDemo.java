package com.example.configcenter.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

/**
 * 配置订阅演示
 * 演示如何使用ConfigSubscriber订阅和监听配置变更
 */
@Component
public class ConfigSubscriberDemo implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(ConfigSubscriberDemo.class);
    
    private final ConfigSubscriber configSubscriber;
    
    public ConfigSubscriberDemo() {
        this.configSubscriber = new ConfigSubscriber();
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("🚀 开始配置中心订阅演示...");
        
        try {
            // 启动配置订阅客户端
            configSubscriber.start();
            
            // 启动配置监控线程
            startConfigMonitor();
            
            // 启动交互模式
            startInteractiveMode();
            
        } catch (Exception e) {
            logger.error("演示运行失败", e);
        } finally {
            configSubscriber.close();
        }
    }
    
    /**
     * 启动配置监控线程，定期打印当前配置
     */
    private void startConfigMonitor() {
        CompletableFuture.runAsync(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(15000); // 每15秒打印一次
                    printCurrentConfig();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }
    
    /**
     * 打印当前配置
     */
    private void printCurrentConfig() {
        Map<String, String> allConfig = configSubscriber.getAllConfig();
        logger.info("📋 当前配置快照 (共{}项):", allConfig.size());
        allConfig.entrySet().stream()
            .sorted((e1, e2) -> e1.getKey().compareTo(e2.getKey()))
            .forEach(entry -> logger.info("  {} = {}", entry.getKey(), entry.getValue()));
        logger.info("📋 配置快照结束");
    }
    
    /**
     * 启动交互模式，允许用户手动修改配置
     */
    private void startInteractiveMode() {
        Scanner scanner = new Scanner(System.in);
        
        logger.info("🎮 进入交互模式，可以手动修改配置进行测试");
        logger.info("命令格式:");
        logger.info("  update <key> <value> - 更新配置");
        logger.info("  get <key> - 获取配置");
        logger.info("  list - 列出所有配置");
        logger.info("  demo - 运行自动演示");
        logger.info("  help - 显示帮助");
        logger.info("  exit - 退出程序");
        
        while (true) {
            System.out.print("\n> ");
            String input = scanner.nextLine().trim();
            
            if (input.isEmpty()) {
                continue;
            }
            
            String[] parts = input.split("\\s+");
            String command = parts[0].toLowerCase();
            
            try {
                switch (command) {
                    case "update":
                        if (parts.length >= 3) {
                            String key = parts[1];
                            String value = String.join(" ", java.util.Arrays.copyOfRange(parts, 2, parts.length));
                            configSubscriber.updateConfig(key, value);
                            logger.info("✅ 配置更新命令已发送");
                        } else {
                            logger.warn("用法: update <key> <value>");
                        }
                        break;
                        
                    case "get":
                        if (parts.length >= 2) {
                            String key = parts[1];
                            String value = configSubscriber.getConfig(key);
                            if (value != null) {
                                logger.info("📖 配置值: {} = {}", key, value);
                            } else {
                                logger.warn("❌ 配置不存在: {}", key);
                            }
                        } else {
                            logger.warn("用法: get <key>");
                        }
                        break;
                        
                    case "list":
                        printCurrentConfig();
                        break;
                        
                    case "demo":
                        runAutoDemo();
                        break;
                        
                    case "help":
                        showHelp();
                        break;
                        
                    case "exit":
                        logger.info("👋 退出程序...");
                        return;
                        
                    default:
                        logger.warn("❌ 未知命令: {}，输入 help 查看帮助", command);
                        break;
                }
            } catch (Exception e) {
                logger.error("❌ 命令执行失败: {}", e.getMessage());
            }
        }
    }
    
    /**
     * 运行自动演示
     */
    private void runAutoDemo() {
        logger.info("🎬 开始自动演示配置变更...");
        
        CompletableFuture.runAsync(() -> {
            try {
                // 场景1: 修改数据库密码
                Thread.sleep(2000);
                logger.info("🎭 场景1: 修改数据库密码");
                configSubscriber.updateConfig("database.password", "newPassword123");
                
                // 场景2: 修改服务端口
                Thread.sleep(3000);
                logger.info("🎭 场景2: 修改服务端口");
                configSubscriber.updateConfig("server.port", "8081");
                
                // 场景3: 启用调试日志
                Thread.sleep(3000);
                logger.info("🎭 场景3: 启用调试日志");
                configSubscriber.updateConfig("logging.level", "DEBUG");
                
                // 场景4: 修改缓存设置
                Thread.sleep(3000);
                logger.info("🎭 场景4: 禁用缓存");
                configSubscriber.updateConfig("cache.enabled", "false");
                
                // 场景5: 新增功能开关
                Thread.sleep(3000);
                logger.info("🎭 场景5: 新增功能开关");
                configSubscriber.updateConfig("feature.newFeature", "enabled");
                
                // 场景6: 回滚一些配置
                Thread.sleep(3000);
                logger.info("🎭 场景6: 回滚配置");
                configSubscriber.updateConfig("database.password", "123456");
                configSubscriber.updateConfig("server.port", "8080");
                configSubscriber.updateConfig("logging.level", "INFO");
                configSubscriber.updateConfig("cache.enabled", "true");
                
                logger.info("🎬 自动演示完成");
                
            } catch (Exception e) {
                logger.error("自动演示失败", e);
            }
        });
    }
    
    /**
     * 显示帮助信息
     */
    private void showHelp() {
        logger.info("📚 帮助信息:");
        logger.info("  update <key> <value> - 更新配置项");
        logger.info("    示例: update database.password newPassword");
        logger.info("  get <key> - 获取指定配置项的值");
        logger.info("    示例: get database.url");
        logger.info("  list - 列出所有当前配置项");
        logger.info("  demo - 运行自动演示，展示各种配置变更场景");
        logger.info("  help - 显示此帮助信息");
        logger.info("  exit - 退出程序");
        logger.info("💡 提示: 配置变更会实时推送到所有订阅的客户端");
    }
} 