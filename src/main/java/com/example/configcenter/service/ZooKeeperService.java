package com.example.configcenter.service;

import java.util.List;
import java.util.Map;

/**
 * ZooKeeper服务接口
 *
 * @author ConfigCenter Team
 * @version 1.0.0
 */
public interface ZooKeeperService {

    /**
     * 发布配置到ZooKeeper
     *
     * @param path 配置路径
     * @param data 配置数据
     * @return 是否成功
     */
    boolean publishConfig(String path, String data);

    /**
     * 获取配置
     *
     * @param path 配置路径
     * @return 配置数据
     */
    String getConfig(String path);

    /**
     * 删除配置
     *
     * @param path 配置路径
     * @return 是否成功
     */
    boolean deleteConfig(String path);

    /**
     * 获取子节点列表
     *
     * @param path 路径
     * @return 子节点列表
     */
    List<String> getChildren(String path);

    /**
     * 检查节点是否存在
     *
     * @param path 路径
     * @return 是否存在
     */
    boolean exists(String path);

    /**
     * 创建持久节点
     *
     * @param path 路径
     * @param data 数据
     * @return 是否成功
     */
    boolean createPersistentNode(String path, String data);

    /**
     * 创建临时节点
     *
     * @param path 路径
     * @param data 数据
     * @return 是否成功
     */
    boolean createEphemeralNode(String path, String data);

    /**
     * 更新节点数据
     *
     * @param path 路径
     * @param data 数据
     * @return 是否成功
     */
    boolean updateNode(String path, String data);

    /**
     * 监听节点变化
     *
     * @param path 路径
     * @param listener 监听器
     */
    void watchNode(String path, ConfigChangeListener listener);

    /**
     * 监听子节点变化
     *
     * @param path 路径
     * @param listener 监听器
     */
    void watchChildren(String path, ConfigChangeListener listener);

    /**
     * 获取节点状态信息
     *
     * @param path 路径
     * @return 状态信息
     */
    Map<String, Object> getNodeStat(String path);

    /**
     * 配置变化监听器
     */
    interface ConfigChangeListener {
        void onConfigChanged(String path, String data, String eventType);
    }
} 