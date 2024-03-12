package cc.geektip.geekojcodesandbox.config;

import lombok.Data;

/**
 * @description:
 * @author: Fish
 * @date: 2024/3/11
 */
@Data
public class LangSpecSetting {
    // 容器镜像
    private String image;
    // 主文件名
    private String mainFile;
    // 编译超时
    private Long compileTimeOut;
    // 运行超时
    private Long runTimeOut;
    // 编译命令
    private String compileCommand;
    // 运行命令
    private String runCommand;
}