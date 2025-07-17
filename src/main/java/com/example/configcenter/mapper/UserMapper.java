package com.example.configcenter.mapper;

import com.example.configcenter.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 用户Mapper接口
 *
 * @author ConfigCenter Team
 * @version 1.0.0
 */
@Mapper
public interface UserMapper {

    /**
     * 根据ID查询用户
     */
    User findById(@Param("id") Long id);

    /**
     * 根据用户名查询用户
     */
    User findByUsername(@Param("username") String username);

    /**
     * 根据邮箱查询用户
     */
    User findByEmail(@Param("email") String email);

    /**
     * 查询所有用户
     */
    List<User> findAll();

    /**
     * 分页查询用户
     */
    List<User> findByPage(@Param("offset") int offset,
                         @Param("limit") int limit,
                         @Param("keyword") String keyword);

    /**
     * 统计用户数量
     */
    int count(@Param("keyword") String keyword);

    /**
     * 插入用户
     */
    int insert(User user);

    /**
     * 更新用户
     */
    int update(User user);

    /**
     * 根据ID删除用户
     */
    int deleteById(@Param("id") Long id);

    /**
     * 更新最后登录信息
     */
    int updateLastLogin(@Param("id") Long id, 
                       @Param("lastLoginTime") Long lastLoginTime,
                       @Param("lastLoginIp") String lastLoginIp);

    /**
     * 更新密码
     */
    int updatePassword(@Param("id") Long id, @Param("password") String password);

    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(@Param("username") String username);

    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(@Param("email") String email);

    /**
     * 根据角色统计用户数量
     */
    int countByRole(@Param("role") String role);

    /**
     * 根据状态统计用户数量
     */
    int countByStatus(@Param("status") String status);

    /**
     * 获取用户统计信息
     */
    Map<String, Long> getUserStatistics();

    /**
     * 检查是否为最后一个管理员
     */
    boolean isLastAdmin(@Param("excludeId") Long excludeId);

    /**
     * 根据部门查询用户列表
     */
    List<User> findByDepartment(@Param("department") String department);

    /**
     * 批量更新用户状态
     */
    int batchUpdateStatus(@Param("ids") List<Long> ids, @Param("status") String status);
} 