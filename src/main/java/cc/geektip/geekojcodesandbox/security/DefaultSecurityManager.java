package cc.geektip.geekojcodesandbox.security;

import java.security.Permission;

/**
 * @description: 默认安全管理器
 * @author: Bill Yu
 * @date: 2024/3/7
 */
public class DefaultSecurityManager extends SecurityManager {

    @Override
    public void checkPermission(Permission perm) {
        System.out.println("默认不做任何限制");
        System.out.println(perm);
//        super.checkPermission(perm);
    }
}
