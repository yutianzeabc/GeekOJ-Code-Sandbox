package cc.geektip.geekojcodesandbox.docker;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @description: DockerProperties
 * @author: Fish
 * @date: 2024/3/8
 */
@Configuration
@ConfigurationProperties(prefix = "docker")
@Data
public class DockerProperties {
    private String host;
    private String image;
    private boolean firstInit;
    private Container container;
    @Data
    static class Container {
        private long memory;
        private long memorySwap;
        private long cpuCount;
        private boolean networkDisabled;
        private boolean readOnlyRootfs;
    }
}
