package cc.geektip.geekojcodesandbox.model;

import lombok.Data;

/**
 * @description: 进程执行信息
 * @author: Fish
 * @date: 2024/3/1
 */
@Data
public class ExecuteMessage {
    private Integer exitValue;

    private String message;

    private String errorMessage;
}
