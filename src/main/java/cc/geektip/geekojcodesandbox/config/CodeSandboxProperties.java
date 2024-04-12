package cc.geektip.geekojcodesandbox.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * @description: CodeSandboxProperties
 * @author: Bill Yu
 * @date: 2024/3/11
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "code-sandbox")
public class CodeSandboxProperties {
    // 代码沙箱服务密钥
    private String xServiceKey;
    // 代码沙箱代码缓存路径（容器内部）
    private String codeCachePath;
    // 代码沙箱代码缓存路径（宿主机）
    private String hostCodeCachePath;
    // 代码沙箱评测模式
    private String judgeMode;
    // 代码沙箱语言设置
    private Map<String, LangSpecSetting> languageSettings;
}