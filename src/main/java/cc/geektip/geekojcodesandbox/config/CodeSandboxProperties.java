package cc.geektip.geekojcodesandbox.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * @description: CodeSandboxProperties
 * @author: Fish
 * @date: 2024/3/11
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "code-sandbox")
public class CodeSandboxProperties {
    // 代码沙箱全局代码路径
    private String globalCodePath;
    // 代码沙箱语言设置
    private Map<String, LangSpecSetting> languageSettings;
}