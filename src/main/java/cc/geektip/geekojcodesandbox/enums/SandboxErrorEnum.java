package cc.geektip.geekojcodesandbox.enums;

import lombok.Getter;

/**
 * @description: 沙箱错误枚举类
 * @author: Bill Yu
 */
@Getter
public enum SandboxErrorEnum {
    CODE_COMPILE_TLE(1001, "编译时间超出代码沙箱最大时限"),
    CODE_RUN_TLE(1002, "运行时间超出代码沙箱最大时限");

    private final int code;
    private final String msg;

    SandboxErrorEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
