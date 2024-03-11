package cc.geektip.geekojcodesandbox.config;

import lombok.Data;

/**
 * @description:
 * @author: Fish
 * @date: 2024/3/11
 */
@Data
public class LangSpecSetting {
    // 主文件名
    private String mainFile;
    // 编译命令
    private String compileCommand;
    // 运行命令
    private String runCommand;
}