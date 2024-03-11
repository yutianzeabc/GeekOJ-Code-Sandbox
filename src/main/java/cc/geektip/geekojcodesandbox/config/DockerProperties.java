package cc.geektip.geekojcodesandbox.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @description: DockerProperties
 * @author: Fish
 * @date: 2024/3/8
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "docker")
public class DockerProperties {
    private String host;
    private String image;
    private boolean firstInit;
    private Container container;

    @Data
    public static class Container {
        private long memory;
        private long memorySwap;
        private long cpuCount;
        private boolean networkDisabled;
        private boolean readOnlyRootfs;
    }
}
