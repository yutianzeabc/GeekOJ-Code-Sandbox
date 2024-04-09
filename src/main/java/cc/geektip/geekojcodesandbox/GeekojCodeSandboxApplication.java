package cc.geektip.geekojcodesandbox;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class GeekojCodeSandboxApplication {

    public static void main(String[] args) {
        SpringApplication.run(GeekojCodeSandboxApplication.class, args);
    }

}
