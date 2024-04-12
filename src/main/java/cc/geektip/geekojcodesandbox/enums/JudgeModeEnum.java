package cc.geektip.geekojcodesandbox.enums;

import lombok.Getter;

/**
 * @description: 判题模式枚举类
 * @author: Bill Yu
 */
@Getter
public enum JudgeModeEnum {
    ACM("acm"),
    ARGS("args");

    final String value;

    JudgeModeEnum(String value) {
        this.value = value;
    }
}
