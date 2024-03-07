package cc.geektip.geekojcodesandbox.security;

import java.security.Permission;

/**
 * @description: 禁用所有权限安全管理器
 * @author: Fish
 * @date: 2024/3/7
 */
public class DenySecurityManager extends SecurityManager {

    // 检查所有的权限
    @Override
    public void checkPermission(Permission perm) {
        throw new SecurityException("权限异常：" + perm.toString());
    }
}